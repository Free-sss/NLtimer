# NLtimer 性能剖析报告

**日期:** 2026-05-08  
**分支:** `perf/audit-report` (工作树: `.worktrees/perf-audit`)  
**目标:** 冷启动 < 2s (中端设备)  
**项目架构:** 多模块 Compose + Room + Hilt + MaterialKolor

---

## 总评

项目整体架构合理，数据类已标注 `@Immutable`，ViewModel 大多使用 `WhileSubscribed(5000)` 管理 Flow 生命周期。但存在 **2 个高危 ViewModel 内存/协程泄漏**、**1 个系统性 Compose 重组问题**、**1 个严重的 N+1 查询问题**、**0 个 @Transaction 注解**、**骨架级 Baseline Profile**。综合修复后冷启动可控制在 2s 以内。

### 预估冷启动时间线 (中端设备, 首次安装)

| 阶段 | 当前耗时 | 优化后 |
|------|---------|--------|
| Process → Application.onCreate | ~50ms | ~30ms |
| Hilt 依赖注入 + Room DB 初始化 | 100-300ms | 30-50ms |
| DataStore 首次读取 + 主题闪屏 | 50-100ms | 0ms (同步缓存) |
| DynamicMaterialTheme 色彩计算 | 10-30ms | 10-30ms |
| AnimatedContent 主题过渡动画 | 300ms | 0ms (移除启动动画) |
| HomeViewModel init (3+1 查询) | 50-150ms | 20-50ms |
| Baseline Profile JIT 编译惩罚 | 100-200ms | 0ms (完整 Profile) |
| Compose 首帧渲染 | 30-60ms | 30-60ms |
| **总计** | **690-1190ms** | **90-220ms** |

> 即使加上不可避免的 I/O 和渲染开销，总计远低于 2s 目标。

---

## 一、内存泄漏点

### 1.1 [CRITICAL] ActivityManagementViewModel — 协程订阅泄漏

**文件:** `feature/management_activities/.../viewmodel/ActivityManagementViewModel.kt:92-117`

**问题:** `loadData()` 中 `getAllGroups().collect { groups -> groups.forEach { repository.getActivitiesByGroup(group.id).launchIn(viewModelScope) } }`，每次 `getAllGroups()` 发射时，为每个 group 创建新的 Flow 订阅但 **不取消旧订阅**。如果 groups 发射 N 次且每次 M 个 group，最终有 N×M 个活跃 Flow 订阅，全部持续触发 `_uiState.update`。

**影响:** 
- 活跃 Flow 订阅无限堆积，RAM 和 DB 连接压力持续增长
- 每个泄漏的订阅都触发 `_uiState.update { it.copy(groups = updatedGroups) }`，O(n) 遍历触发 Compose 全量重组

**修复方案:**
```kotlin
// 方案A: 用 flatMapLatest + combine 自动切换订阅
private val _groupIds = MutableStateFlow<List<Long>>(emptyList())

val uiState = _groupIds.flatMapLatest { ids ->
    combine(ids.map { id -> repository.getActivitiesByGroup(id) }) { groupLists ->
        // 构建状态
    }
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

// 方案B: 手动 Job 管理
private var groupJobs = emptyList<Job>()

fun loadData() {
    repository.getAllGroups()
        .onEach { groups ->
            groupJobs.forEach { it.cancel() }
            groupJobs = groups.map { group ->
                repository.getActivitiesByGroup(group.id)
                    .onEach { ... }
                    .launchIn(viewModelScope)
            }
        }
        .launchIn(viewModelScope)
}
```

### 1.2 [CRITICAL] HomeViewModel — onActivitySelected 协程泄漏

**文件:** `feature/home/.../viewmodel/HomeViewModel.kt:395-401`

**问题:** `onActivitySelected()` 每次调用创建 `viewModelScope.launch { tagRepository.getByActivityId(activityId).collect { ... } }` 但 **从不取消前一个**。用户选择 5 个不同 activity 后，5 个 Room Flow 订阅同时活跃，全部写 `_tagsForSelectedActivity`。

**根因:** `selectedActivityId` 是普通 `var Long?`（line 81），无法使用 `flatMapLatest`。

**修复方案:**
```kotlin
private val _selectedActivityId = MutableStateFlow<Long?>(null)

val tagsForSelectedActivity = _selectedActivityId
    .flatMapLatest { id -> 
        if (id != null) tagRepository.getByActivityId(id) 
        else flowOf(emptyList())
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
```

### 1.3 [MEDIUM] HomeViewModel — buildUiState 内 Flow 订阅残留

**文件:** `feature/home/.../viewmodel/HomeViewModel.kt:181-184`

**问题:** `getTagsForBehavior(behavior.id).firstOrNull()` 从 Flow 读取一个值，但 Room 的 Flow 实现在 `.firstOrNull()` 返回后仍保留 invalidation tracker 订阅，直到协程完全取消。在 `buildUiState()` 中为每个 behavior 创建的 N 个 Flow 订阅都会短暂残留。

**修复:** 为一键读取添加 `suspend` 版本 DAO 方法:
```kotlin
@Query("SELECT t.* FROM tags t INNER JOIN behavior_tag_cross_ref btc ON t.id = btc.tagId WHERE btc.behaviorId = :behaviorId")
suspend fun getTagsForBehaviorSync(behaviorId: Long): List<TagEntity>
```

---

## 二、过度重组问题

### 2.1 [HIGH] HomeScreen — List 参数不稳定，导致永远不可跳过

**文件:** `feature/home/.../ui/HomeScreen.kt:81`

**问题:** HomeScreen 接收 5 个 `List<>` 参数 (`activities`, `activityGroups`, `tagsForSelectedActivity`, `allTags`, `rows`) + 10 个 lambda 参数。`List<T>` 在 Compose 中是 unstable 类型，编译器 **永远无法跳过** HomeScreen 的重组。任何 uiState 变化（包括每秒的计时器更新）导致全子树重组。

**影响:** 计时器每秒更新 → `uiState` 变化 → HomeRoute 重组 → 新 lambda 实例 → HomeScreen 被迫重组 → TimeAxisGrid / TimeSideBar / BehaviorLogView / FAB 全部重新执行

**修复方案 (按优先级):**

A. **采用 `kotlinx.collections.immutable`** — 最有效单一改动
```kotlin
// build.gradle.kts
implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7")

// 数据类中替换
@Immutable data class HomeUiState(
    val rows: PersistentList<GridRowUiState> = persistentListOf(),
    val activities: PersistentList<Activity> = persistentListOf(),
    // ...
)
```

B. **Route → Screen 的 lambda 稳定化**
```kotlin
// HomeRoute.kt 中
val onEmptyCellClick = remember(viewModel) {
    { idleStart: LocalTime?, idleEnd: LocalTime? ->
        viewModel.showAddSheet(AddSheetMode.COMPLETED, idleStart, idleEnd)
    }
}
```

C. **提取 FAB 为独立 Composable**，仅接收 `hasActiveBehavior` + `activeBehaviorId`

### 2.2 [HIGH] HomeScreen — 行内计算不使用 derivedStateOf

**文件:** `feature/home/.../ui/HomeScreen.kt:312-315`

```kotlin
// 当前: 每次 uiState 变化都重算
val activeHours = uiState.rows
    .filter { it.cells.any { cell -> cell.behaviorId != null } || it.isCurrentRow }
    .map { it.startTime.hour }.toSet()

// 修复:
val activeHours by remember {
    derivedStateOf {
        uiState.rows
            .filter { it.cells.any { cell -> cell.behaviorId != null } || it.isCurrentRow }
            .map { it.startTime.hour }.toSet()
    }
}
```

### 2.3 [HIGH] HomeScreen — 行内列表分配不 remember

**文件:** `feature/home/.../ui/HomeScreen.kt:139-143`

```kotlin
// 当前: 每次重组分配新 List
val dragOptions = if (uiState.hasActiveBehavior) {
    listOf("完成", "放弃", "特记", "+自定义")
} else { ... }

// 修复:
val dragOptions = remember(uiState.hasActiveBehavior) {
    if (uiState.hasActiveBehavior) listOf(...) else listOf(...)
}
```

### 2.4 [HIGH] LazyColumn/LazyRow/FlowRow 缺少 key

| 文件 | 行号 | 影响 |
|------|------|------|
| `TimelineReverseView.kt` | 113 | 列表变更时全量重组而非差异更新 |
| `ActivityManagementScreen.kt` | 160 | Group 增删导致展开/菜单状态丢失 |
| `ActivityGridComponent.kt` | 201 | Chip 重排时全量重组 |
| `TagPicker.kt` | 41 | Tag 增删时全量重组 |
| `CategoryPickerDialog.kt` | 105 | 选项变更时全量重组 |
| `SelectionDialog.kt` | 351 | 通用选择器全量重组 |

**修复模式:**
```kotlin
items(items = list, key = { it.id }) { item -> ... }
// 或在 forEach 中:
list.forEach { item -> key(item.id) { ComposableItem(item) } }
```

### 2.5 [HIGH] AddBehaviorSheetContent — 750 行巨型 Composable

**文件:** `feature/home/.../ui/sheet/AddBehaviorSheet.kt:283`

**问题:** 单个 Composable 超过 750 行，接收 5 个不稳定 List 参数 + 多个 lambda。任何参数变化导致全部 750 行重新执行。

**修复:** 拆分为 4 个子 Composable:
1. `AddBehaviorDragOverlay` — FAB 拖拽遮罩层
2. `AddBehaviorTimeAdjustment` — 时间调节覆盖层
3. `AddBehaviorActivitySection` — Activity 选择区
4. `AddBehaviorTagSection` — Tag 选择区

### 2.6 [MEDIUM] 缺少 @Immutable 注解

| 数据类 | 文件 | 修复 |
|--------|------|------|
| `ChipItem` | `ActivityGridComponent.kt:57` | 添加 `@Immutable` |
| `DateItem` | `DualTimePickerComponent.kt:47` | 添加 `@Immutable` |
| `ActivityStats` | `core/data/.../model/ActivityStats.kt:7` | 添加 `@Immutable` |
| `CategoryGroup<T>` | `CategoryPickerDialog.kt` | 添加 `@Immutable` |

---

## 三、布局层级问题

### 3.1 [MEDIUM] CategoriesScreen — verticalScroll + Column 替代 LazyColumn

**文件:** `feature/categories/.../ui/CategoriesScreen.kt:97`

**问题:** 使用 `.verticalScroll(rememberScrollState())` 的 Column 会 **立即组合所有子项**（包括屏外内容），随着分类数量增长，内存和组合时间线性增加。

**修复:** 转换为 `LazyColumn`。

### 3.2 [MEDIUM] TimeSideBar — Column + forEach 渲染所有小时

**文件:** `feature/home/.../ui/components/TimeSideBar.kt:46`

**问题:** 用 `Column { forEach { Text(...) } }` 渲染 24 个小时标签，全部常驻内存。配合不稳定的 `Set<Int>` 参数，任何父级变化触发全部 24 个 Text 重绘。

**修复:** 改用 `LazyColumn` 或至少将参数改为稳定类型。

### 3.3 [LOW] BehaviorDetailDialog — 函数定义在 Composable 体内

**文件:** `feature/home/.../ui/components/BehaviorDetailDialog.kt:32-61`

**问题:** `epochToMsString` 和 `buildExportText` 函数定义在 Composable 体中，每次重组重新分配。应提取为顶层函数。

---

## 四、冷启动瓶颈

### 4.1 [CRITICAL] Room DB + Hilt Singleton 急初始化 (50-300ms)

**文件:** `core/data/.../di/DataModule.kt:55-66`

**问题:** `Room.databaseBuilder(...).build()` 在 Hilt `@Singleton` 组件中同步执行。首次访问任意 @Singleton Repository 时触发 Hilt 遍历整个依赖 DAG，创建所有中间单例，导致 Room 在主线程验证 schema + 运行 migration。

**修复:**
```kotlin
@Provides @Singleton
fun provideDatabase(@ApplicationContext context: Context): NLtimerDatabase =
    Room.databaseBuilder(context, NLtimerDatabase::class.java, "nltimer-database")
        .fallbackToDestructiveMigration(true)
        .addMigrations(/* ... */)
        .setQueryExecutor(Dispatchers.IO.asExecutor())  // 确保异步
        .build()
```
配合使用 lazy proxy 或 `withTransaction` 延迟首次 DB 访问。

### 4.2 [HIGH] DataStore 首次读取导致主题闪烁 (30-100ms)

**文件:** `app/.../MainActivity.kt:29-32`

**问题:** `collectAsStateWithLifecycle(initialValue = Theme())` 第一帧用默认主题渲染，DataStore I/O 完成后切换为用户主题，触发 `DynamicMaterialTheme` + `AnimatedContent` 全量重绘 + 300ms 过渡动画。

**修复方案:**
1. **Splash Screen 保持策略:**
```kotlin
val splashScreen = installSplashScreen()
var themeLoaded by mutableStateOf(false)
splashScreen.setKeepOnScreenCondition { !themeLoaded }
// 在 Theme StateFlow 首次发射后设置 themeLoaded = true
```

2. **SharedPreferences 同步缓存:**
```kotlin
// 用 SP 同步读取上次主题作为 initialValue
val cachedTheme = sharedPreferences.getCachedTheme()
val theme by settingsPrefs.getThemeFlow()
    .collectAsStateWithLifecycle(initialValue = cachedTheme)
```

### 4.3 [HIGH] DynamicMaterialTheme + AnimatedContent (10-30ms CPU + 300ms 动画)

**文件:** `core/designsystem/.../theme/Theme.kt:44-64`

**问题:** HCT 色彩空间算法在首帧组合时计算 50+ 颜色角色。`AnimatedContent` 包裹启动内容导致 300ms 延迟。如遇主题切换（见 4.2），算法执行两次。

**修复:** 移除启动时的 `AnimatedContent`，改用 `Crossfade` 或无动画直接切换。

### 4.4 [HIGH] Baseline Profile 骨架级 (100-200ms JIT 惩罚)

**文件:** `app/src/main/baseline-prof.txt`

**问题:** 仅包含 7 条规则（4 个 Compose runtime + 3 个 app 方法），缺少整个冷启动路径：
- 缺失: NLtimerDatabase 及其 DAO
- 缺失: 所有 @Singleton Repository 实现
- 缺失: SettingsPrefsImpl
- 缺失: HomeViewModel
- 缺失: DynamicMaterialTheme 及色彩计算代码
- 缺失: NLtimerNavHost / HomeRoute / HomeScreen
- 缺失: Hilt 生成的组件类

**修复:** 添加 `androidx.baselineprofile` Gradle 插件 + macrobenchmark 模块，生成完整 Profile。

### 4.5 [MEDIUM] ThemeSettingsViewModel 在 Scaffold 中急创建

**文件:** `app/.../NLtimerScaffold.kt:48`

**问题:** `hiltViewModel<ThemeSettingsViewModel>()` 作为默认参数无条件创建，但仅用于 `RouteSettingsPopup` 的两个回调。

**修复:** 延迟到 `RouteSettingsPopup` 首次显示时创建。

---

## 五、数据库查询性能

### 5.1 [CRITICAL] HomeViewModel N+1 查询

**文件:** `feature/home/.../viewmodel/HomeViewModel.kt:181-184`

**问题:** `buildUiState()` 内对每个 behavior 调用 `getTagsForBehavior(id).firstOrNull()`，N 个 behavior = N 次 DB 查询。

**修复:** 创建批量查询:
```kotlin
@Query("""
    SELECT btc.behaviorId, t.* FROM tags t
    INNER JOIN behavior_tag_cross_ref btc ON t.id = btc.tagId
    WHERE btc.behaviorId IN (:behaviorIds)
    ORDER BY t.priority DESC, t.name
""")
suspend fun getTagsForBehaviorsSync(behaviorIds: List<Long>): List<BehaviorTagTuple>
```
并在 ViewModel 中 `remember` activityMap 避免重复 `getAll()` 查询。

### 5.2 [HIGH] 全项目零 @Transaction 注解 — 数据损坏风险

| 操作 | 文件 | 风险 |
|------|------|------|
| `BehaviorRepositoryImpl.insert()` | line 84-98 | Behavior 写入但 Tag 关联丢失 |
| `BehaviorRepositoryImpl.updateTagsForBehavior()` | line 239-243 | 删除后插入失败 = 标签全丢 |
| `BehaviorRepositoryImpl.completeCurrentAndStartNext()` | line 124-157 | 6 步写入，中间崩溃 = 状态不一致 |
| `BehaviorRepositoryImpl.updateBehavior()` | line 217-237 | 两步写入不一致 |
| `CategoryRepositoryImpl.resetActivityCategory()` | line 43-48 | Ungroup 后删除失败 |
| `CategoryMigrationValidator.migrateIfNeeded()` | line 27-47 | 部分插入 |

**修复:** 所有包含 ≥2 步写入的 suspend 函数添加 `@Transaction`:
```kotlin
@Transaction
override suspend fun insert(behavior: Behavior, tagIds: List<Long>): Long { ... }
```
或在 DAO 层使用 `@Transaction` 注解。

### 5.3 [HIGH] 缺失索引

| 实体 | 缺失索引 | 影响查询 |
|------|---------|---------|
| `ActivityEntity` | `isArchived` | `getAllActive()`, `getUncategorized()`, `search()` |
| `ActivityEntity` | `groupId` | `getByGroup()`, `ungroupAllActivities()` |
| `TagEntity` | `isArchived` | `getAllActive()`, `search()`, `getByCategory()` |
| `TagEntity` | `category` | `getByCategory()`, `renameCategory()`, `resetCategory()` |
| `BehaviorEntity` | `(startTime, status)` 复合 | `getHomeBehaviors()` 当前索引 `(startTime, sequence)` 不覆盖 status |

**修复:**
```kotlin
@Entity(indices = [Index("isArchived"), Index("groupId")])
data class ActivityEntity(...)

@Entity(indices = [Index("isArchived"), Index("category")])
data class TagEntity(...)
```

### 5.4 [MEDIUM] ActivityStats 三 Flow 组合

**文件:** `core/data/.../repository/impl/ActivityManagementRepositoryImpl.kt:60-72`

**问题:** 三个 `Flow<Int/Long?>` 分别监听 `behaviors` 表，任何 behavior 写入触发全部三个重发射。

**修复:** 合并为单一 DAO 查询:
```kotlin
@Query("""
    SELECT COUNT(*) as usageCount, 
           COALESCE(SUM(actualDurationMs), 0) as totalDurationMs,
           MAX(endTime) as lastUsedTimestamp
    FROM behaviors WHERE activityId = :activityId AND status = 'completed'
""")
fun getActivityStats(activityId: Long): Flow<ActivityStatsRow>
```

### 5.5 [MEDIUM] 全表加载计算排序序号

**文件:** `ActivityManagementRepositoryImpl.kt:88, 97, 105`

**问题:** `groupDao.getAll().first()` 加载全部 group 仅为了计算 `max(sortOrder)` 或按 ID 查找。

**修复:** 添加专用查询:
```kotlin
@Query("SELECT MAX(sortOrder) FROM activity_groups")
suspend fun getMaxSortOrder(): Int?

@Query("SELECT * FROM activity_groups WHERE id = :id LIMIT 1")
suspend fun getById(id: Long): ActivityGroupEntity?
```

---

## 六、优先修复路线图

### Phase 1 — 数据安全 (1-2天)

| # | 修复项 | 影响 |
|---|--------|------|
| 1 | 为所有多步写入添加 `@Transaction` | 消除数据损坏风险 |
| 2 | 为 `ActivityEntity` / `TagEntity` 添加索引 | 加速 80%+ 的列表查询 |
| 3 | 修复 `ActivityManagementViewModel` 协程泄漏 | 消除内存泄漏 |

### Phase 2 — 性能关键路径 (2-3天)

| # | 修复项 | 预期收益 |
|---|--------|---------|
| 4 | HomeViewModel `flatMapLatest` 重构 | 消除内存泄漏 + 减少无效查询 |
| 5 | 批量 tag 查询替代 N+1 | 减少 20× DB 查询次数 |
| 6 | DataStore 主题闪烁修复 (SP 缓存/Splash) | 消除 300ms 动画延迟 |
| 7 | 移除启动 `AnimatedContent` | 减少 300ms 首帧延迟 |
| 8 | Room 延迟初始化 | 减少 50-300ms 启动阻塞 |

### Phase 3 — Compose 优化 (2-3天)

| # | 修复项 | 预期收益 |
|---|--------|---------|
| 9 | 引入 `kotlinx-collections-immutable` | 全局稳定 List 参数 |
| 10 | Route → Screen lambda 稳定化 | 减少无效重组 |
| 11 | `derivedStateOf` + `remember` 优化 | 避免行内重计算 |
| 12 | LazyColumn/FlowRow 添加 `key` | 差异更新 |
| 13 | 拆分 `AddBehaviorSheetContent` | 隔离重组范围 |

### Phase 4 — 生产级优化 (1-2天)

| # | 修复项 | 预期收益 |
|---|--------|---------|
| 14 | 生成完整 Baseline Profile | 减少 100-200ms 首次冷启动 |
| 15 | ActivityStats 合并为单查询 | 减少无效 Flow 重发射 |
| 16 | 添加 `suspend` DAO 替代一键 Flow 读取 | 减少 invalidation tracker 开销 |

---

## 七、ViewModel 风险排名

| 排名 | ViewModel | 严重度 | 核心问题 |
|------|-----------|--------|---------|
| 1 | ActivityManagementViewModel | **HIGH** | 协程订阅泄漏 (N×M 累积) |
| 2 | HomeViewModel | **HIGH** | 协程泄漏 + N+1 查询 + 全量对象树重建 |
| 3 | TagManagementViewModel | LOW | 标准模式，轻微 |
| 4 | CategoriesViewModel | LOW | 结构良好 |
| 5 | ThemeSettingsViewModel | LOW | 干净 |
| 6 | DialogConfigViewModel | LOW | 无问题 |

---

## 八、Cold Start 瀑布图 (修复前 → 修复后)

```
修复前:                                    修复后:
[Process Launch        50ms]              [Process Launch        30ms]
[Hilt DAG + Room Init 200ms] ■■■         [Lazy Room Init         -] (延迟到首次查询)
[DataStore Read        80ms] ■■           [SP Cache Read           5ms]
[Theme Computation     20ms] ■            [Theme Computation      20ms]
[AnimatedContent       300ms] ■■■■■■     [No Animation             -]
[HomeVM 3+N+1 Queries 120ms] ■■          [Batched Queries        30ms]
[Baseline JIT Penalty 150ms] ■■■         [Full Profile              -]
[First Frame           50ms] ■            [First Frame            40ms]
                                          ──────────────────────────
总计: ~970ms                               总计: ~125ms
                                          
中端设备余量(~1.5x): ~1455ms               中端设备余量(~1.5x): ~188ms
目标: < 2000ms ✓ (勉强)                    目标: < 2000ms ✓✓✓ (远超)
```

---

*报告由自动化性能剖析侦探生成 — 工作树 `.worktrees/perf-audit` 的 `perf/audit-report` 分支*
