# NLtimer 主页设计规格

## 概述

NLtimer 主页采用时间轴网格布局，以4格位为一行展示用户行为记录。每行左侧悬浮时间标签，右侧0-24h时间定位条，MD3暗色主题。用户通过点击空格位弹出混合模式弹窗添加行为。

## 架构决策

- **模块拆分**：`core:data`（Room数据库+Repository）+ `feature:home`（UI层）
- **数据持久化**：Room
- **依赖注入**：Hilt
- **UI框架**：Jetpack Compose + Material Design 3
- **状态管理**：ViewModel + StateFlow + collectAsStateWithLifecycle

## 数据模型

### BehaviorEntity（行为表）

| 字段            | 类型      | 说明                           |
| ------------- | ------- | ---------------------------- |
| id            | Long    | 主键，自增                        |
| activityId    | Long    | FK → ActivityEntity          |
| startTime     | Long    | EpochMillis，开始时间             |
| endTime       | Long?   | EpochMillis，结束时间（当前类型为null）  |
| nature        | String  | CURRENT / COMPLETED / TARGET |
| note          | String? | 细化备注                         |
| pomodoroCount | Int     | 预留字段：番茄钟个数，默认0               |

### ActivityEntity（活动表）

| 字段         | 类型      | 说明           |
| ---------- | ------- | ------------ |
| id         | Long    | 主键，自增        |
| name       | String  | 活动名称，唯一      |
| emoji      | String? | 可定义emoji     |
| iconKey    | String? | 可定义图标key     |
| category   | String? | 分类           |
| isArchived | Boolean | 归档状态，默认false |

### TagEntity（标签表）

| 字段         | 类型      | 说明           |
| ---------- | ------- | ------------ |
| id         | Long    | 主键，自增        |
| name       | String  | 标签名称，唯一      |
| color      | Long?   | ARGB颜色值      |
| category   | String? | 分类           |
| priority   | Int     | 显示优先级，默认0    |
| isArchived | Boolean | 归档状态，默认false |

### ActivityTagBindingEntity（活动-标签绑定表）

| 字段         | 类型   | 说明                  |
| ---------- | ---- | ------------------- |
| activityId | Long | FK → ActivityEntity |
| tagId      | Long | FK → TagEntity      |

主键为 (activityId, tagId) 复合主键。

### BehaviorTagCrossRefEntity（行为-标签关联表）

| 字段         | 类型   | 说明                  |
| ---------- | ---- | ------------------- |
| behaviorId | Long | FK → BehaviorEntity |
| tagId      | Long | FK → TagEntity      |

主键为 (behaviorId, tagId) 复合主键。

## 模块结构

### core:data

```
core/data/
├── model/
│   ├── Behavior.kt
│   ├── Activity.kt
│   ├── Tag.kt
│   └── BehaviorNature.kt           # enum: CURRENT, COMPLETED, TARGET
├── database/
│   ├── NLtimerDatabase.kt
│   ├── dao/
│   │   ├── BehaviorDao.kt
│   │   ├── ActivityDao.kt
│   │   └── TagDao.kt
│   └── entity/
│       ├── BehaviorEntity.kt
│       ├── ActivityEntity.kt
│       ├── TagEntity.kt
│       ├── ActivityTagBindingEntity.kt
│       └── BehaviorTagCrossRefEntity.kt
├── repository/
│   ├── BehaviorRepository.kt        # 接口
│   ├── ActivityRepository.kt        # 接口
│   ├── TagRepository.kt             # 接口
│   └── impl/
│       ├── BehaviorRepositoryImpl.kt
│       ├── ActivityRepositoryImpl.kt
│       └── TagRepositoryImpl.kt
└── di/
    └── DataModule.kt                # @Module provides DAO + Repository
```

### feature:home

```
feature/home/
├── model/
│   ├── HomeUiState.kt
│   ├── GridRowUiState.kt
│   ├── GridCellUiState.kt
│   └── TagUiState.kt
├── viewmodel/
│   └── HomeViewModel.kt
├── ui/
│   ├── HomeRoute.kt
│   ├── HomeScreen.kt
│   ├── components/
│   │   ├── TimeAxisGrid.kt
│   │   ├── GridRow.kt
│   │   ├── GridCell.kt
│   │   ├── GridCellEmpty.kt
│   │   ├── GridCellLocked.kt
│   │   ├── TimeFloatingLabel.kt
│   │   ├── TimeSideBar.kt
│   │   └── TagChip.kt
│   └── sheet/
│       ├── AddBehaviorSheet.kt
│       ├── ActivityPicker.kt
│       ├── TagPicker.kt
│       ├── NoteInput.kt
│       ├── BehaviorNatureSelector.kt
│       └── TimePickerCompact.kt
└── match/
    ├── MatchStrategy.kt             # 接口（预留）
    └── KeywordMatchStrategy.kt      # 默认实现
```

## UI 状态

### HomeUiState

```kotlin
data class HomeUiState(
    val rows: List<GridRowUiState> = emptyList(),
    val currentRowId: String? = null,
    val isAddSheetVisible: Boolean = false,
    val selectedTimeHour: Int = 0,
    val isLoading: Boolean = true,
)
```

### GridRowUiState

```kotlin
data class GridRowUiState(
    val rowId: String,
    val startTime: LocalTime,
    val isCurrentRow: Boolean,
    val isLocked: Boolean,
    val cells: List<GridCellUiState>,
)
```

### GridCellUiState

```kotlin
data class GridCellUiState(
    val behaviorId: Long?,
    val activityEmoji: String?,
    val activityName: String?,
    val tags: List<TagUiState>,
    val nature: BehaviorNature?,
    val isCurrent: Boolean,
)
```

## 交互逻辑

### 行填充锁定

当前行未填满4格位且无当前（CURRENT）行为时，下一行锁定。锁定行显示为半透明，格位不可点击。

### 时间悬浮标签

仅在每行第一个格位上方显示，采用 TimeFloatingLabel 组件，视觉类似微信通讯录字母分组。已完成行标签为主题色，当前行标签为橙色。

### 时间跨度跳过

当行为跨越多个小时（如睡觉 22:00→08:00），中间时间点不生成空行。算法：按 startTime 分组行为为行，跳过被长跨度行为覆盖的时间段。

### 右侧时间定位条

TimeSideBar 组件，0-24h 刻度。有行为的时间点高亮为主题色，当前时间高亮为橙色。点击/滑动跳转到对应时间行。

### 添加行为弹窗

混合模式：顶部搜索框 + 常用活动 chip 列表。搜索时实时过滤匹配。选中活动后自动展开绑定标签。备注输入框可选。行为性质三选一（当前/完成/目标）。当前类型不可选结束时间。

### 当前行为自动结束

点击空格位添加新行为时，若当前有 CURRENT 行为，自动结束前一个行为（设置 endTime 为新行为 startTime）。行为时间不允许交集。

## 样式扩展预留

所有样式相关代码标记 `// Mark-style-main`，包括：

- 网格格位圆角、边框、背景色
- 时间悬浮标签样式
- 标签 chip 样式
- 弹窗圆角、内边距
- 时间定位条刻度样式

## 扩展预留接口

### MatchStrategy

```kotlin
interface MatchStrategy {
    fun matchActivities(query: String, activities: List<Activity>): List<Activity>
    fun matchTags(query: String, tags: List<Tag>): List<Tag>
}
```

默认实现 KeywordMatchStrategy，未来可替换为 RegexMatchStrategy 或 AiMatchStrategy。

### DisplayModeStrategy

```kotlin
interface DisplayModeStrategy {
    fun arrangeBehaviors(behaviors: List<BehaviorWithDetails>): List<GridRowUiState>
}
```

默认实现为时间轴行式，未来可扩展为标签堆积式等。

### AI匹配入口

搜索框右侧预留 AI 按钮，调用 AiMatchService 接口（空实现），未来接入云端大模型。

## 依赖变更

### libs.versions.toml 新增

```toml
room = "2.7.1"

room_runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room_ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
room_compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
```

### settings.gradle.kts 新增

```kotlin
include("core:data")
```

### core:data build.gradle.kts

- plugins: android.library, kotlin.android, hilt.android, ksp, compose.compiler
- dependencies: Room, Hilt, Kotlinx Coroutines

### feature:home build.gradle.kts

- 新增: hilt.android, ksp 插件
- 新增: projects.core.data 依赖
- 新增: Room, Hilt, Lifecycle, Navigation 依赖

