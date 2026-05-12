# NLtimer 统计页面设计规格 v2

日期：2026-05-12

## 概述

将 `feature:stats` 模块从占位符升级为完整统计页面。采用单页 + SegmentedButton 切换日/周视图，使用 Compose Charts (ehsannarmani) 处理饼图/柱状图，组合式 UI 处理时间轴。

## 设计决策

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 图表库 | Compose Charts (ehsannarmani) | 用户选择，API 现代，支持饼图/柱状图 |
| 页面结构 | 单页 + SegmentedButton 切换 | 与项目主页体验一致，实现简单 |
| 时间轴实现 | 组合式 UI（Row + Box） | 降低 Canvas 自绘复杂度，MD3 一致性好 |
| 时间粒度 | 30 分钟/格 | 平衡信息密度与可读性 |
| 实现范围 | 日视图 + 周视图 | 月视图后续迭代 |
| 配色 | M3 Dynamic Color + 活动自身 color | 与项目 MaterialKolor 主题一致 |

## 页面结构

### 顶层布局

```
StatsScreen (Scaffold)
└── LazyColumn
    ├── SegmentedButton (日视图 | 周视图)
    ├── 当前日期/周选择器 (左右箭头 + 日期文本)
    └── [根据选中 Tab 显示不同内容]
```

### 日视图组件

```
LazyColumn
├── SummaryCards (2x2 网格)
│   ├── 总时长 (PrimaryContainer)
│   ├── 活跃时段数 (TertiaryContainer)
│   ├── 最长活动 (SecondaryContainer)
│   └── 活动种类数 (SurfaceVariant)
├── ActivityPieCard
│   ├── 标题: "活动分布"
│   └── Compose Charts PieChart
├── TagPieCard
│   ├── 标题: "标签分布"
│   └── Compose Charts PieChart
├── TagDetailCards (LazyRow)
│   └── 每个标签: 名称 + 时长 + 占比 + 颜色条
└── DailyTimelineCard
    ├── 标题: "时间分布"
    └── LazyRow (30分钟/格, 共48格)
        └── Box(width=固定, height=40.dp, color=活动色)
```

### 周视图组件

```
LazyColumn
├── WeeklySummaryCards (1x3 横排)
│   ├── 周总时长 (PrimaryContainer)
│   ├── 日均时长 (TertiaryContainer)
│   └── vs 上周变化率 (SurfaceVariant)
├── DailyBarCard
│   ├── 标题: "每日时长"
│   └── Compose Charts BarChart (X=周一~周日, Y=时长)
├── WeeklyActivityPieCard
│   ├── 标题: "本周活动分布"
│   └── Compose Charts PieChart
└── WeeklyTimelineCard
    ├── 标题: "本周时间分布"
    └── Column (7行, 每行一天)
        └── Row: 日期标签 + LazyRow(30分钟/格)
```

## 数据层

### 新增 DAO 查询

```kotlin
// 按日分组统计时长
data class DailyDurationRow(val day: Long, val totalDuration: Long)

@Query("""
    SELECT (startTime / 86400000) as day,
           SUM(COALESCE(actualDuration, (endTime - startTime))) as totalDuration
    FROM behaviors 
    WHERE startTime >= :rangeStart AND startTime < :rangeEnd AND status = 'completed'
    GROUP BY day ORDER BY day ASC
""")
fun getDailyDurations(rangeStart: Long, rangeEnd: Long): Flow<List<DailyDurationRow>>

// 按活动分组统计时长
data class ActivityStatRow(
    val id: Long, val name: String, val color: Long?,
    val totalDuration: Long, val count: Int
)

@Query("""
    SELECT a.id, a.name, a.color,
           SUM(COALESCE(b.actualDuration, (b.endTime - b.startTime))) as totalDuration,
           COUNT(*) as count
    FROM behaviors b 
    JOIN activities a ON b.activityId = a.id
    WHERE b.startTime >= :rangeStart AND b.startTime < :rangeEnd AND b.status = 'completed'
    GROUP BY b.activityId ORDER BY totalDuration DESC
""")
fun getStatsByActivity(rangeStart: Long, rangeEnd: Long): Flow<List<ActivityStatRow>>

// 按标签分组统计时长
data class TagStatRow(
    val id: Long, val name: String, val color: Long?, val totalDuration: Long
)

@Query("""
    SELECT t.id, t.name, t.color,
           SUM(COALESCE(b.actualDuration, (b.endTime - b.startTime))) as totalDuration
    FROM behaviors b
    JOIN behavior_tag_cross_ref btc ON b.id = btc.behaviorId
    JOIN tags t ON btc.tagId = t.id
    WHERE b.startTime >= :rangeStart AND b.startTime < :rangeEnd AND b.status = 'completed'
    GROUP BY t.id ORDER BY totalDuration DESC
""")
fun getStatsByTag(rangeStart: Long, rangeEnd: Long): Flow<List<TagStatRow>>
```

### StatsRepository

```kotlin
class StatsRepository @Inject constructor(
    private val behaviorDao: BehaviorDao,
    private val clockService: ClockService,
) {
    fun getTodayStats(): Flow<TodayStats>
    fun getDailyDurations(days: Int): Flow<List<DailyDurationRow>>
    fun getActivityRanking(start: Long, end: Long): Flow<List<ActivityStatRow>>
    fun getTagDistribution(start: Long, end: Long): Flow<List<TagStatRow>>
    fun getBehaviorsForTimeline(start: Long, end: Long): Flow<List<BehaviorWithDetails>>
}
```

### StatsViewModel

```kotlin
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository
) : ViewModel() {
    val uiState: StateFlow<StatsUiState>
    
    fun selectTab(tab: StatsTab)
    fun navigateDate(delta: Int) // +1/-1 天或周
}
```

### UiState 数据结构

```kotlin
enum class StatsTab { DAY, WEEK }

data class StatsUiState(
    val selectedTab: StatsTab = StatsTab.DAY,
    val selectedDate: LocalDate = LocalDate.now(),
    val todayStats: TodayStats = TodayStats(),
    val dailyDurations: List<DailyDurationRow> = emptyList(),
    val activityStats: List<ActivityStatRow> = emptyList(),
    val tagStats: List<TagStatRow> = emptyList(),
    val timelineData: List<BehaviorWithDetails> = emptyList(),
    val weeklyBarData: List<DailyDurationRow> = emptyList(),
    val weeklyComparison: WeeklyComparison? = null,
)

data class TodayStats(
    val totalDuration: Long = 0,
    val activeSlotCount: Int = 0,
    val longestActivity: String = "",
    val activityTypeCount: Int = 0,
)

data class WeeklyComparison(
    val thisWeekTotal: Long,
    val lastWeekTotal: Long,
    val changeRate: Float, // 百分比变化
)
```

## 交互设计

### 日期/周选择

- 日视图: `←  2026年5月12日  →`，点击箭头切换日期
- 周视图: `←  第19周 (05/06 - 05/12)  →`，点击箭头切换周
- 日期选择器使用现有项目中的日期选择模式（参考 BehaviorManagementViewModel 的日期导航）

### 饼图交互

- 点击扇区后，该扇区半径增大 8dp（Compose Charts 内置 selectedSlice 参数）
- 下方文字区域显示：活动名 + 时长 + 占比百分比（通过回调 onSliceClick 更新状态）
- 再次点击同一扇区取消选中

### 时间轴交互

- 点击色块后，在色块上方显示 Tooltip（使用 Popup composable）：活动名 + 时段范围（如 "14:00 - 14:30"）+ 时长
- Tooltip 自动消失（2 秒后 dismiss 或点击其他区域）

### 动画

| 动画 | 实现 | 参数 |
|------|------|------|
| 卡片入场 | `AnimatedVisibility` + `slideInVertically` | 依次延迟 80ms，duration 300ms |
| 数字计数 | `animateIntAsState` / `animateFloatAsState` | duration 800ms |
| Tab 切换 | `AnimatedContent` | slideInHorizontally + fadeIn |

## 模块依赖变更

### feature/stats/build.gradle.kts

```kotlin
plugins {
    id("nltimer.android.library")
    id("nltimer.android.hilt")  // 新增
}

android {
    namespace = "com.nltimer.feature.stats"
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.data)  // 新增

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // 图表库 - 新增（版本号以 libs.versions.toml 为准）
    implementation(libs.compose.charts)

    // Lifecycle - 新增
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.hilt.navigation.compose)
}
```

### gradle/libs.versions.toml

```toml
[libraries]
# 新增
compose-charts = { module = "com.ehsannarmani:ComposeCharts", version = "0.1.0" }
```

## 文件结构

```
feature/stats/src/main/java/com/nltimer/feature/stats/
├── ui/
│   ├── StatsRoute.kt           # 路由入口，Hilt 注入 ViewModel
│   ├── StatsScreen.kt          # 主页面，LazyColumn + SegmentedButton
│   ├── StatsTab.kt             # Tab 定义（DAY/WEEK）
│   └── components/
│       ├── SummaryCards.kt     # 日视图 2x2 摘要卡片
│       ├── WeeklySummaryCards.kt # 周视图 1x3 摘要卡片
│       ├── DateSelector.kt     # 日期/周选择器
│       ├── ActivityPieCard.kt  # 活动饼图卡片
│       ├── TagPieCard.kt       # 标签饼图卡片
│       ├── TagDetailRow.kt     # 标签详情 LazyRow
│       ├── DailyTimelineCard.kt # 日时间轴卡片
│       ├── WeeklyTimelineCard.kt # 周时间轴卡片
│       ├── DailyBarCard.kt     # 每日柱状图卡片
│       └── AnimatedCounter.kt  # 数字计数动画组件
├── viewmodel/
│   └── StatsViewModel.kt
└── model/
    └── StatsUiState.kt
```

## 边界情况

| 情况 | 处理 |
|------|------|
| 无数据 | 显示空状态占位（图标 + "暂无统计数据"） |
| 当日/当周无记录 | 摘要卡片显示 0，饼图显示空状态 |
| 活动无 color 字段 | 使用 M3 Primary 色阶自动分配 |
| 标签无关联 | 不显示标签分布卡片 |
| 周视图无上周数据 | 对比卡片显示 "N/A" |
| 正在进行的行为 | 不计入统计（status != 'completed'） |

## 不在范围内

- 月视图（后续迭代）
- 活动属性分类（积极/普通/消极）—— 需要 Activity 模型新增字段，本次不做
- 数据缓存优化
- 共享元素转场动画
