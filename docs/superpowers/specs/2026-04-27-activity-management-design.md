# 活动管理功能设计文档

**日期**: 2026-04-27
**模块名**: feature/management_activities
**状态**: ✅ 已批准，待实现

---

## 1. 功能概述

### 1.1 目标

创建独立的**活动管理页面**，用于管理应用中的活动项（Activity）和活动分组。该页面与现有的"分类管理"页面（CategoriesScreen）并存，职责分离：
- **分类管理**：管理分类名称（活动分类、标签分类）
- **活动管理**：管理具体的活动项及其分组归属

### 1.2 核心功能

1. **预设活动库**：提供少量预设活动（8个示例），用户可直接使用
2. **自定义分组**：用户可创建分组（如"生活"、"工作"），将活动归类
3. **活动CRUD**：对活动进行增、删、改、查、移动分组等操作

### 1.3 用户故事

- 作为用户，我希望看到预设的活动列表，以便快速开始使用
- 作为用户，我希望创建自定义分组来组织我的活动
- 作为用户，我希望添加、编辑、删除活动项
- 作为用户，我希望将活动在不同分组间移动

---

## 2. UI 设计

### 2.1 页面布局

采用**分组卡片式布局**，整体结构如下：

```
┌─────────────────────────────────────┐
│  ← 返回    活动管理            [+]  │  TopAppBar
├─────────────────────────────────────┤
│                                     │
│  未分类                              │ Section Header
│  ┌────┐ ┌────┐ ┌────┐ ┌────┐      │
│  📺番剧│ 🎬娱乐│ 🎮游戏│ 📖学习│     │ FlowRow (Chips)
│  └────┘ └────┘ └────┘ └────┘      │
│                                     │
│  ┌─────────────────────────────┐    │
│  │ 📂 生活               ⋮  ▼ │    │ GroupCard (可折叠)
│  ├─────────────────────────────┤    │
│  │ ┌────┐                    │    │
│  │ 🍚饮食                     │    │ ActivityChips
│  │ └────┘                    │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │ 📂 工作               ⋮  ▲ │    │ GroupCard (已折叠)
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │   + 增加活动分类             │    │ AddGroupButton
│  └─────────────────────────────┘    │
│                                     │
│                        ┌────────┐   │
│                        │   ⁝    │   │ FAB (更多操作)
│                        └────────┘   │
└─────────────────────────────────────┘
```

### 2.2 核心组件

#### 2.2.1 ActivityChip（活动芯片）

**文件**: `ui/components/ActivityChip.kt`

**视觉样式**:
- 圆角矩形背景（Material3 AssistChip 风格）
- 左侧：Emoji 图标（16sp）
- 右侧：活动名称文字（14sp medium）
- 尺寸：高度 36dp，内边距 horizontal 12dp, vertical 4dp
- 交互：点击选中效果，长按显示操作菜单

**Props**:
```kotlin
@Composable
fun ActivityChip(
    activity: Activity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```

#### 2.2.2 GroupCard（分组卡片）

**文件**: `ui/components/GroupCard.kt`

**视觉样式**:
- Material3 Card 容器（圆角 16dp，elevation 1dp）
- 标题栏：分组图标 + 分组名称 + 展开/折叠图标 + 更多菜单按钮
- 内容区：FlowRow 展示活动项（仅展开时可见）
- 默认状态：展开
- 空分组提示：显示"暂无活动"文字

**Props**:
```kotlin
@Composable
fun GroupCard(
    group: ActivityGroup,
    activities: List<Activity>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Void,
    onActivityClick: (Activity) -> Unit,
    onActivityLongClick: (Activity) -> Unit,
    modifier: Modifier = Modifier,
)
```

#### 2.2.3 对话框组件

**目录**: `ui/components/dialogs/`

| 组件 | 文件 | 用途 |
|------|------|------|
| AddActivityDialog | AddActivityDialog.kt | 添加新活动 |
| EditActivityDialog | EditActivityDialog.kt | 编辑现有活动 |
| AddGroupDialog | AddGroupDialog.kt | 新建分组 |
| ConfirmDialog | ConfirmDialog.kt | 删除确认 |

**AddActivityDialog 表单字段**:
- 活动名称（必填，OutlinedTextField）
- Emoji 选择器（可选，预设常用emoji列表）
- 所属分组（可选，DropdownMenu选择）
- 确定取消按钮

### 2.3 交互流程

#### 场景1：添加活动到分组
1. 点击右下角 FAB 按钮
2. 弹出 AddActivityDialog
3. 填写名称、选择 emoji、选择分组
4. 点击确定 → 对话框关闭 → 活动出现在对应分组

#### 场景2：长按活动项
1. 长按任意 ActivityChip
2. 显示 DropdownMenu：
   - ✏️ 编辑
   - 📁 移动到分组 > （子菜单列出所有分组）
   - 🗑️ 删除
3. 选择操作 → 执行对应逻辑

#### 场景3：管理分组
1. 点击分组卡片的 ⋮ 菜单按钮
2. 显示选项：
   - ✏️ 重命名
   - 🗑️ 删除分组（提示：活动将移至未分类）

#### 场景4：新建分组
1. 点击底部"+ 增加活动分类"按钮
2. 弹出 AddGroupDialog
3. 输入分组名称
4. 点击确定 → 新分组出现在列表底部

---

## 3. 数据层设计

### 3.1 数据库 Schema (Room)

#### 3.1.1 activities 表

```sql
CREATE TABLE activities (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    emoji TEXT,
    icon_key TEXT,
    group_id INTEGER,
    is_preset INTEGER NOT NULL DEFAULT 0,
    is_archived INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,

    FOREIGN KEY(group_id) REFERENCES activity_groups(id) ON DELETE SET NULL
);
```

**索引**:
```sql
CREATE INDEX idx_activities_group ON activities(group_id);
CREATE INDEX idx_activities_archived ON activities(is_archived);
```

#### 3.1.2 activity_groups 表

```sql
CREATE TABLE activity_groups (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL
);
```

### 3.2 Entity 定义

#### ActivityEntity
```kotlin
@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String? = null,
    val iconKey: String? = null,
    val groupId: Long? = null,
    val isPreset: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
```

#### ActivityGroupEntity
```kotlin
@Entity(tableName = "activity_groups")
data class ActivityGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)
```

### 3.3 DAO 接口

#### ActivityDao
```kotlin
@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities WHERE isArchived = 0 ORDER BY name")
    fun getAllActive(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE groupId IS NULL AND isArchived = 0 ORDER BY name")
    fun getUncategorized(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE groupId = :groupId AND isArchived = 0 ORDER BY name")
    fun getByGroup(groupId: Long): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE isPreset = 1 AND isArchived = 0 ORDER BY name")
    fun getAllPresets(): Flow<List<ActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: ActivityEntity): Long

    @Update
    suspend fun update(activity: ActivityEntity)

    @Query("UPDATE activities SET groupId = :groupId WHERE id = :activityId")
    suspend fun moveToGroup(activityId: Long, groupId: Long?)

    @Query("UPDATE activities SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun deleteById(id: Long)
}
```

#### ActivityGroupDao
```kotlin
@Dao
interface ActivityGroupDao {
    @Query("SELECT * FROM activity_groups ORDER BY sortOrder ASC, id ASC")
    fun getAll(): Flow<List<ActivityGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: ActivityGroupEntity): Long

    @Update
    suspend fun update(group: ActivityGroupEntity)

    @Delete
    suspend fun delete(group: ActivityGroupEntity)

    @Query("UPDATE activities SET groupId = NULL WHERE groupId = :groupId")
    suspend fun ungroupAllActivities(groupId: Long)
}
```

### 3.4 Repository 实现

**文件**: `core/data/.../repository/impl/ActivityManagementRepositoryImpl.kt`

```kotlin
class ActivityManagementRepositoryImpl(
    private val activityDao: ActivityDao,
    private val groupDao: ActivityGroupDao,
) : ActivityManagementRepository {

    override fun getAllActivities(): Flow<List<Activity>> =
        activityDao.getAllActive().map { entities -> entities.map { it.toDomain() } }

    override fun getUncategorizedActivities(): Flow<List<Activity>> =
        activityDao.getUncategorized().map { entities -> entities.map { it.toDomain() } }

    override fun getActivitiesByGroup(groupId: Long): Flow<List<Activity>> =
        activityDao.getByGroup(groupId).map { entities -> entities.map { it.toDomain() } }

    override fun getAllGroups(): Flow<List<ActivityGroup>> =
        groupDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addActivity(activity: Activity): Long {
        val entity = activity.toEntity()
        return activityDao.insert(entity)
    }

    override suspend fun updateActivity(activity: Activity) {
        activityDao.update(activity.toEntity())
    }

    override suspend fun deleteActivity(id: Long) {
        activityDao.deleteById(id)
    }

    override suspend fun moveActivityToGroup(activityId: Long, groupId: Long?) {
        activityDao.moveToGroup(activityId, groupId)
    }

    override suspend fun addGroup(name: String): Long {
        val maxOrder = groupDao.getAll().first().maxOfOrNull { it.sortOrder } ?: -1
        val entity = ActivityGroupEntity(name = name, sortOrder = maxOrder + 1)
        return groupDao.insert(entity)
    }

    override suspend fun renameGroup(id: Long, newName: String) {
        val group = // get by id
        groupDao.update(group.copy(name = newName))
    }

    override suspend fun deleteGroup(id: Long) {
        groupDao.ungroupAllActivities(id)  // 先将该组活动移至未分类
        val group = // get by id
        groupDao.delete(group)
    }

    override suspend fun initializePresets() {
        val existingPresets = activityDao.getAllPresets().first()
        if (existingPresets.isEmpty()) {
            PRESET_ACTIVITIES.forEach { preset ->
                activityDao.insert(preset.toEntity())
            }
        }
    }
}
```

### 3.5 预设数据

**定义位置**: `ActivityManagementRepository` 伴生对象或独立常量文件

```kotlin
val PRESET_ACTIVITIES = listOf(
    Activity(name = "番剧视频", emoji = "📺", isPreset = true),
    Activity(name = "娱乐视频", emoji = "🎬", isPreset = true),
    Activity(name = "玩游戏", emoji = "🎮", isPreset = true),
    Activity(name = "主动学习", emoji = "📖", isPreset = true),
    Activity(name = "运动健身", emoji = "💪", isPreset = true),
    Activity(name = "社交聚会", emoji = "👥", isPreset = true),
    Activity(name = "本职工作", emoji = "💼", isPreset = true),
    Activity(name = "休息放松", emoji = "😌", isPreset = true),
)
```

**初始化时机**: ViewModel init 块中调用 `repository.initializePresets()`

---

## 4. 业务逻辑层

### 4.1 ViewModel

**文件**: `viewmodel/ActivityManagementViewModel.kt`

```kotlin
@HiltViewModel
class ActivityManagementViewModel @Inject constructor(
    private val repository: ActivityManagementRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityManagementUiState())
    val uiState: StateFlow<ActivityManagementUiState> = _uiState.asStateFlow()

    init {
        loadData()
        viewModelScope.launch {
            repository.initializePresets()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getUncategorizedActivities(),
                repository.getAllGroups(),
            ) { uncategorized, groups ->
                val groupsWithActivities = groups.map { group ->
                    runBlocking { repository.getActivitiesByGroup(group.id).first() }
                        .let { activities -> GroupWithActivities(group, activities) }
                }
                _uiState.value.update {
                    it.copy(
                        isLoading = false,
                        uncategorizedActivities = uncategorized,
                        groups = groupsWithActivities,
                    )
                }
            }.collect()
        }
    }

    fun toggleGroupExpand(groupId: Long) {
        val current = _uiState.value.expandedGroupIds
        val newSet = if (current.contains(groupId)) {
            current - groupId
        } else {
            current + groupId
        }
        _uiState.value.update { it.copy(expandedGroupIds = newSet) }
    }

    fun showAddActivityDialog() {
        _uiState.value.update { it.copy(dialogState = DialogState.AddActivity) }
    }

    fun showEditActivityDialog(activity: Activity) {
        _uiState.value.update { it.copy(dialogState = DialogState.EditActivity(activity)) }
    }

    fun addActivity(name: String, emoji: String?, groupId: Long?) {
        viewModelScope.launch {
            val activity = Activity(
                name = name.trim(),
                emoji = emoji,
                groupId = groupId,
                isPreset = false,
            )
            repository.addActivity(activity)
            dismissDialog()
        }
    }

    fun updateActivity(activity: Activity) {
        viewModelScope.launch {
            repository.updateActivity(activity)
            dismissDialog()
        }
    }

    fun deleteActivity(id: Long) {
        viewModelScope.launch {
            repository.deleteActivity(id)
            dismissDialog()
        }
    }

    fun moveActivityToGroup(activityId: Long, groupId: Long?) {
        viewModelScope.launch {
            repository.moveActivityToGroup(activityId, groupId)
            dismissDialog()
        }
    }

    fun showAddGroupDialog() {
        _uiState.value.update { it.copy(dialogState = DialogState.AddGroup) }
    }

    fun addGroup(name: String) {
        viewModelScope.launch {
            repository.addGroup(name.trim())
            dismissDialog()
        }
    }

    fun renameGroup(id: Long, newName: String) {
        viewModelScope.launch {
            repository.renameGroup(id, newName.trim())
            dismissDialog()
        }
    }

    fun showDeleteGroupDialog(group: ActivityGroup) {
        _uiState.value.update { it.copy(dialogState = DialogState.DeleteGroup(group)) }
    }

    fun deleteGroup(id: Long) {
        viewModelScope.launch {
            repository.deleteGroup(id)
            dismissDialog()
        }
    }

    fun dismissDialog() {
        _uiState.value.update { it.copy(dialogState = null) }
    }
}
```

### 4.2 UiState 定义

**文件**: `model/ActivityManagementUiState.kt`

```kotlin
data class ActivityManagementUiState(
    val uncategorizedActivities: List<Activity> = emptyList(),
    val groups: List<GroupWithActivities> = emptyList(),
    val allGroups: List<ActivityGroup> = emptyList(),  // 用于下拉选择
    val isLoading: Boolean = true,
    val expandedGroupIds: Set<Long> = emptySet(),
    val dialogState: DialogState? = null,
)

data class GroupWithActivities(
    val group: ActivityGroup,
    val activities: List<Activity>,
)

sealed interface DialogState {
    object AddActivity : DialogState
    data class EditActivity(val activity: Activity) : DialogState
    object AddGroup : DialogState
    data class RenameGroup(val group: ActivityGroup) : DialogState
    data class DeleteGroup(val group: ActivityGroup) : DialogState
    data class DeleteActivity(val activity: Activity) : DialogState
    data class MoveToGroup(val activity: Activity) : DialogState
}
```

---

## 5. 模块结构

### 5.1 目录树

```
feature/management_activities/
├── build.gradle.kts
├── src/main/
│   ├── AndroidManifest.xml
│   └── java/com/nltimer/feature/management_activities/
│       ├── model/
│       │   ├── ActivityManagementUiState.kt
│       │   ├── Activity.kt
│       │   └── ActivityGroup.kt
│       ├── ui/
│       │   ├── ActivityManagementRoute.kt
│       │   ├── ActivityManagementScreen.kt
│       │   └── components/
│       │       ├── ActivityChip.kt
│       │       ├── GroupCard.kt
│       │       └── dialogs/
│       │           ├── AddActivityDialog.kt
│       │           ├── EditActivityDialog.kt
│       │           ├── AddGroupDialog.kt
│       │           └── ConfirmDialog.kt
│       └── viewmodel/
│           └── ActivityManagementViewModel.kt
└── src/test/
    └── java/com/nltimer/feature/management_activities/
        └── viewmodel/
            └── ActivityManagementViewModelTest.kt
```

### 5.2 依赖配置 (build.gradle.kts)

```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.compose.material3:material3:1.2.0")

    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
```

---

## 6. 导航集成

### 6.1 修改 AppDrawer.kt

**文件**: `app/src/main/java/com/nltimer/app/component/AppDrawer.kt`
**位置**: 第 37-38 行

**修改前**:
```kotlin
DrawerMenuItem("categories", "分类管理", Icons.Default.Category),
DrawerMenuItem("categories", "活动管理", Icons.Default.Category),
```

**修改后**:
```kotlin
DrawerMenuItem("categories", "分类管理", Icons.Default.Category),
DrawerMenuItem("activity_management", "活动管理", Icons.Default.Category),
```

### 6.2 修改 NLtimerNavHost.kt

**文件**: `app/src/main/java/com/nltimer/app/navigation/NLtimerNavHost.kt`

**新增路由**:
```kotlin
import com.nltimer.feature.management_activities.ui.ActivityManagementRoute

// 在 NavHost block 中添加：
composable("activity_management") {
    ActivityManagementRoute(
        onNavigateBack = { navController.popBackStack() },
    )
}
```

### 6.3 Route 入口实现

**文件**: `ui/ActivityManagementRoute.kt`

```kotlin
@Composable
fun ActivityManagementRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ActivityManagementViewModel = hiltViewModel()

    ActivityManagementScreen(
        uiState = viewModel.uiState.collectAsStateWithLifecycle().value,
        onBackClick = onNavigateBack,
        onToggleGroupExpand = viewModel::toggleGroupExpand,
        onAddActivity = viewModel::showAddActivityDialog,
        onEditActivity = viewModel::showEditActivityDialog,
        onDeleteActivity = { activity -> viewModel.showDeleteActivityDialog(activity) },
        onMoveToGroup = viewModel::showMoveToGroupDialog,
        onAddGroup = viewModel::showAddGroupDialog,
        onRenameGroup = viewModel::renameGroup,
        onDeleteGroup = viewModel::showDeleteGroupDialog,
        onDismissDialog = viewModel::dismissDialog,
        onConfirmAddActivity = viewModel::addActivity,
        onConfirmUpdateActivity = viewModel::updateActivity,
        onConfirmDeleteActivity = viewModel::deleteActivity,
        onConfirmMoveToGroup = viewModel::moveActivityToGroup,
        onConfirmAddGroup = viewModel::addGroup,
        onConfirmDeleteGroup = viewModel::deleteGroup,
        modifier = modifier,
    )
}
```

---

## 7. 现有代码修改清单

| 文件路径 | 修改类型 | 具体内容 |
|---------|---------|---------|
| `app/.../AppDrawer.kt` | 修改 | 第38行 route 改为 `"activity_management"` |
| `app/.../NLtimerNavHost.kt` | 修改 | 添加 `activity_management` composable 路由 |
| `core/data/.../NLtimerDatabase.kt` | 修改 | 添加 entities = [ActivityEntity::class, ActivityGroupEntity::class] |
| `core/data/di/DataModule.kt` | 修改 | 提供 ActivityManagementRepository |
| **新建模块** | 新增 | `feature/management_activities` (完整模块) |

---

## 8. 测试策略

### 8.1 单元测试

**文件**: `viewmodel/ActivityManagementViewModelTest.kt`

**测试用例**:
- [ ] 初始状态应为 loading=true
- [ ] 加载数据后应更新 uiState
- [ ] addActivity 应调用 repository 并关闭对话框
- [ ] deleteActivity 应调用 repository 并关闭对话框
- [ ] moveActivityToGroup 应正确移动活动
- [ ] addGroup 应创建新分组
- [ ] deleteGroup 应将活动移至未分类后删除分组
- [ ] toggleGroupExpand 应切换分组展开状态

### 8.2 测试依赖

```kotlin
testImplementation("junit:junit:4.13.2")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
testImplementation("io.mockk:mockk:1.13.8")
testImplementation("app.cash.turbine:turbine:1.0.0")
```

---

## 9. 实现优先级与时间估算

### Phase 1: 基础框架（必须）
- [ ] 创建 `feature/management_activities` 模块及目录结构
- [ ] 实现 Entity、DAO、Repository 数据层
- [ ] 更新 Database 添加新表
- [ ] 配置 DI 模块

### Phase 2: 核心UI（必须）
- [ ] 实现 ActivityManagementUiState 和 ViewModel
- [ ] 实现 ActivityManagementScreen 主屏幕
- [ ] 实现 ActivityChip 组件
- [ ] 实现 GroupCard 组件
- [ ] 实现基础对话框（Add/Edit Activity, Add Group）

### Phase 3: 导航集成（必须）
- [ ] 修改 AppDrawer.kt
- [ ] 修改 NLtimerNavHost.kt
- [ ] 实现 ActivityManagementRoute

### Phase 4: 完善（重要）
- [ ] 实现长按菜单（编辑/删除/移动）
- [ ] 实现删除确认对话框
- [ ] 实现移动到分组功能
- [ ] 添加单元测试

### Phase 5: 优化（可选）
- [ ] 搜索/筛选功能
- [ ] 拖拽排序
- [ ] 批量操作
- [ ] 导入/导出配置

---

## 10. 技术约束与决策记录

### 10.1 技术选型

| 决策点 | 选择 | 理由 |
|-------|------|------|
| UI框架 | Jetpack Compose + Material3 | 项目统一标准 |
| 架构模式 | MVVM + Repository | 与现有模块一致 |
| 依赖注入 | Hilt | 项目统一标准 |
| 数据库 | Room | Android本地存储最佳实践 |
| 异步处理 | Kotlin Flow + Coroutines | 响应式编程 |

### 10.2 设计决策

1. **为什么是独立模块而不是扩展现有 categories？**
   - 职责单一原则：categories管分类名，activities管活动实例
   - 未来可独立演进，互不影响
   - 符合项目现有模块化架构

2. **为什么只提供少量预设而非完整列表？**
   - YAGNI原则：避免过度设计
   - 用户可自由定制，无需维护大量预设
   - 减少初始数据库体积

3. **为什么使用 groupId 外键而非嵌套结构？**
   - 关系型数据库更灵活
   - 支持活动在分组间移动
   - 支持未分类活动（groupId=null）

4. **为什么使用 Flow 而非 LiveData？**
   - 项目其他模块已使用 Flow
   - Flow 更适合 Kotlin 协程生态
   - 与 Compose 集成更好

---

## 11. 风险与缓解措施

| 风险 | 影响 | 缓解措施 |
|-----|------|---------|
| Room 数据库迁移冲突 | 中 | 使用 AutoMigration 或手动迁移策略 |
| 预设数据重复插入 | 低 | initializePresets() 检查是否已存在 |
| 大量活动时性能问题 | 低 | 当前阶段数据量小，后续可考虑分页 |
| 分组删除时数据一致性 | 中 | 事务处理：先 ungroup 再 delete |

---

## 12. 验收标准

### 功能验收
- [ ] 可以查看未分类活动列表
- [ ] 可以查看自定义分组及其活动
- [ ] 可以添加新活动（名称、emoji、分组）
- [ ] 可以编辑现有活动
- [ ] 可以删除活动（带确认）
- [ ] 可以创建新分组
- [ ] 可以重命名分组
- [ ] 可以删除分组（活动自动移至未分类）
- [ ] 可以将活动移动到不同分组
- [ ] 预设活动在首次启动时自动初始化

### UI/UX 验收
- [ ] 页面布局与设计稿一致（分组卡片式）
- [ ] ActivityChip 视觉样式符合 Material3 规范
- [ ] GroupCard 支持展开/折叠动画
- [ ] 对话框交互流畅
- [ ] 长按菜单正常工作
- [ ] FAB 按钮位置和功能正确

### 代码质量验收
- [ ] 遵循项目现有代码风格
- [ ] 单元测试覆盖率 > 80%
- [ ] 无明显内存泄漏风险
- [ ] Compose 性能优化（remember、key 使用正确）

---

## 附录 A: 参考资料

- [Jetpack Compose Material3](https://developer.android.com/jetpack/androidx/releases/compose-material3)
- [Room 持久化库](https://developer.android.com/training/data-storage/room)
- [Hilt 依赖注入](https://dagger.dev/hilt/)
- [Kotlin Flow](https://kotlinlang.org/docs/flow.html)
- 现有 CategoriesScreen 实现（参考模式）

---

## 附录 B: 术语表

| 术语 | 定义 |
|-----|------|
| Activity（活动） | 用户可以记录的具体行为项，如"番剧视频"、"运动健身" |
| ActivityGroup（活动分组） | 用户创建的分类容器，用于组织多个活动 |
| Preset Activity（预设活动） | 应用内置的示例活动，供用户快速开始使用 |
| Uncategorized（未分类） | 未归入任何分组的活动集合 |
| Chip（芯片） | Compose 中用于展示可选择项的紧凑组件 |

---

**文档版本**: 1.0
**最后更新**: 2026-04-27
**作者**: AI Assistant
**审批状态**: ✅ 已通过
