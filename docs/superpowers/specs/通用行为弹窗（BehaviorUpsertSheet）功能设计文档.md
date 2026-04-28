# 通用行为弹窗（BehaviorUpsertSheet）功能设计文档

> **版本：v2.1** | 日期：2026-04-27 | 状态：审查修订完成，准予实施\
> **变更说明：v2.0审查后修正——SnapFlingBehavior替代animateScrollToItem、ViewModel单向数据流替代双向绑定标记、增加联动约束与状态降级确认、4项交互增强**

***

## 目录

- [1. 概述](#1-概述)
- [2. 组件架构与可复用性](#2-组件架构与可复用性)
- [3. 数据流与状态管理](#3-数据流与状态管理)
- [4. 核心组件详细规格](#4-核心组件详细规格)
- [5. 界面布局与视觉规范](#5-界面布局与视觉规范)
- [6. 异常处理与边界情况](#6-异常处理与边界情况)
- [7. 实现难点分析](#7-实现难点分析)

***

## 1. 概述

### 1.1 背景

NLtimer 当前已实现行为的添加（`AddBehaviorSheet`）和完成操作，但存在以下问题：

1. 缺少对已有行为的查看与编辑能力
2. 原有 `AddBehaviorSheet` 的UI设计与目标产品截图差异较大
3. 增加页和详情页的组件重复度高，未实现真正的复用

**v2.0 目标：构建一个通用的** **`BehaviorUpsertSheet`** **组件**，通过模式参数（ADD/EDIT）同时服务于"新增行为"和"编辑行为详情"两种场景，UI完全对齐提供的截图设计。

### 1.2 核心设计理念

| 设计原则       | 说明                                           |
| ---------- | -------------------------------------------- |
| **通用复用**   | 单一组件服务 ADD/EDIT 双模式，通过 `mode: UpsertMode` 区分 |
| **截图对齐**   | UI布局、交互方式、视觉风格严格匹配目标截图                       |
| **渐进增强**   | 时间选择器从简单点击升级为一体化滚轮体验                         |
| **心智模型优化** | 信息架构调整为"时间→标签→活动"（先定时间再选内容）                  |

### 1.3 目标功能清单

| #  | 功能             | 说明                                       | 适用模式           |
| -- | -------------- | ---------------------------------------- | -------------- |
| 1  | **日期+时间一体化滚轮** | 三列独立滚轮（日期/时/分），5项可见，中间高亮                 | ADD + EDIT     |
| 2  | **快捷按钮**       | \[上尾]（仅开始时间）、\[当前]（开始/结束各一）              | EDIT为主         |
| 3  | **持续时间滑块**     | 圆形滑块手柄，拖动调整endTime，双向绑定滚轮                | EDIT-COMPLETED |
| 4  | **活动选择器**      | 蓝色+号按钮 + 描述输入框，替代原ActivityPicker流式布局     | ADD + EDIT     |
| 5  | **标签多选**       | 复用现有TagPicker，支持关联标签+所有标签双区域             | ADD + EDIT     |
| 6  | **描述/备注合并**    | 原"备注"字段重命名为"描述"，合并到活动选择区域                | ADD + EDIT     |
| 7  | **删除功能**       | EDIT模式显示独立删除按钮，ADD模式不提供                  | EDIT only      |
| 8  | **自动类型推断**     | 根据是否有endTime自动判断ACTIVE/COMPLETED，移除显式选择器 | 自动             |
| 9  | **用时显示**       | EDIT模式顶部栏实时显示"⏱ 用时:XhYm"                 | EDIT only      |
| 10 | **可复用性**       | 所有组件均可被未来弹窗或页面复用                         | 全部             |

### 1.4 技术栈

| 维度      | 选型                                               |
| ------- | ------------------------------------------------ |
| UI      | Jetpack Compose + Material3                      |
| 弹窗      | `ModalBottomSheet`（skipPartiallyExpanded = true） |
| 架构      | MVVM + Repository，Hilt DI                        |
| 数据      | Room Database + Flow 响应式                         |
| Min SDK | API 31（Android 12）                               |

***

## 2. 组件架构与可复用性

### 2.1 架构总览

```
┌─────────────────────────────────────────────────────────────┐
│                    BehaviorUpsertSheet                       │
│                  (ModalBottomSheet 外壳)                      │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                  BehaviorUpsertContent                       │
│              (通用内容体 - 可被Dialog等容器复用)               │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────┐   │
│  │ TopBar (模式敏感)                                   │   │
│  │  ADD: "新增行为" + ⋮                               │   │
│  │  EDIT: "⏱ 用时:2h30m" + 行为名 + ⋮(含删除)        │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ DateTimeSelectionRow (水平并排双列)                  │   │
│  │  ┌──────────────────┐  ┌──────────────────┐         │   │
│  │  │ StartDateTimeCol  │  │ EndDateTimeCol    │         │   │
│  │  │ Label:"开始:"     │  │ Label:"结束:"     │         │   │
│  │  │ Quick:[上尾][当前]│  │ Quick:[当前]      │         │   │
│  │  │ DateTimeWheelPicker│  │ DateTimeWheelPicker│       │   │
│  │  └──────────────────┘  └──────────────────┘         │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ DurationSlider (持续时间调整滑块)                     │   │
│  │  ┌───●──────────────────────────────────────┐       │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ RelatedTagsSection (相关标签)                        │   │
│  │  Header: "相关标签" + [+]按钮                        │   │
│  │  TagPicker (FlowRow流式布局)                         │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ ActivitySection (活动内容)                           │   │
│  │  Header: "活动内容" + [?]帮助按钮                   │   │
│  │  ┌────────┐  ┌────────────────────┐                │   │
│  │  │ + 按钮 │  │ DescriptionInput    │                │   │
│  │  │(蓝色)  │  │ (描述/备注输入框)   │                │   │
│  │  └────────┘  └────────────────────┘                │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ BottomActionBar (模式敏感)                           │   │
│  │  ADD:    [ 💡 增加 ]          (全宽filled主色)       │   │
│  │  EDIT:   [ ✅保存 ] [ 🗑删 ]  (filled + outlined)   │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 文件结构（v2.0 最终版）

```
feature/home/ui/sheet/
│
├── [核心新增] BehaviorUpsertSheet.kt        通用弹窗外壳 (ModalBottomSheet)
├── [核心新增] BehaviorUpsertContent.kt      通用内容体 (可被Dialog复用)
│
├── [新增] DateTimeWheelPicker.kt           日期+时间一体化滚轮选择器 ⭐核心组件
├── [新增] DurationSlider.kt                持续时间调整滑块组件
├── [新增] ActivitySelector.kt              蓝色+号活动选择按钮 + 描述输入框
│
├── [保持复用] TagPicker.kt                 标签选择器 (FlowRow流式布局)
├── [保持复用] AddActivityDialog.kt         添加活动弹窗
├── [保持复用] AddTagDialog.kt              添加标签弹窗
├── [保持复用] NoteInput.kt                 ← 可能被DescriptionInput替代或合并
│
├── [标记废弃] TimePickerCompact.kt         ❌ 替换为DateTimeWheelPicker
├── [标记废弃] AddBehaviorSheet.kt          ❌ 合并到BehaviorUpsertSheet
├── [标记废弃] AddBehaviorSheetContent.kt   ❌ 同上
├── [标记废弃] ActivityPicker.kt            ❌ 替换为ActivitySelector
├── [标记废弃] BehaviorNatureSelector.kt    ❌ 移除(自动推断类型)
│
└── [可选新增] DeleteConfirmDialog.kt       删除确认弹窗 (如需独立文件)
```

### 2.3 关键组件接口定义

#### **BehaviorUpsertSheet（弹窗外壳）**

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BehaviorUpsertSheet(
    mode: UpsertMode,                          // ADD 或 EDIT
    upsertState: UpsertUiState,               // 完整状态对象
    allActivities: List<Activity>,            // 所有可用活动
    allTags: List<Tag>,                      // 所有可用标签
    onDismiss: () -> Unit,                   // 关闭弹窗
    onConfirm: () -> Unit,                   // 确认(增加/保存)
    onDelete: (() -> Unit)? = null,          // 删除回调(仅EDIT模式)
    onAddActivity: (name: String, emoji: String) -> Unit,
    onAddTag: (name: String) -> Unit,
    modifier: Modifier = Modifier,
)
```

#### **BehaviorUpsertContent（可复用内容体）**

```kotlin
@Composable
fun BehaviorUpsertContent(
    mode: UpsertMode,
    state: UpsertUiState,
    allActivities: List<Activity>,
    allTags: List<Tag>,
    onStartTimeChange: (LocalDateTime) -> Unit,
    onEndTimeChange: (LocalDateTime?) -> Unit,
    onQuickFillHead: () -> Unit,             // [上尾] 快捷按钮
    onQuickFillCurrentStart: () -> Unit,     // [当前]-开始
    onQuickFillCurrentEnd: () -> Unit,       // [当前]-结束
    onActivitySelect: (Activity) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTagToggle: (Long) -> Unit,
    onAddActivity: (String, String) -> Unit,
    onAddTag: (String) -> Unit,
    onConfirm: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
)
```

#### **DateTimeWheelPicker（日期+时间滚轮）⭐**

```kotlin
@Composable
fun DateTimeWheelPicker(
    dateTime: LocalDateTime,                  // 当前选中值
    onDateTimeChange: (LocalDateTime) -> Unit, // 值变更回调
    modifier: Modifier = Modifier,
    minDateTime: LocalDateTime? = null,       // 可选最小值限制
    maxDateTime: LocalDateTime? = null,       // 可选最大值限制
    enabled: Boolean = true,                  // 是否启用交互
)
```

**内部结构：**

```
DateTimeWheelPicker
├── DateColumn (LazyColumn)     // 15项: today±7天, 格式 MM/dd 或 今天/明天/昨天
├── HourColumn (LazyColumn)     // 24项: 00-23, 格式 HH
└── MinuteColumn (LazyColumn)   // 12项: 00-55步长5分, 格式 mm
```

#### **DurationSlider（持续时间滑块）**

```kotlin
@Composable
fun DurationSlider(
    startTime: LocalDateTime,
    endTime: LocalDateTime?,
    onEndTimeChange: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,                  // EDIT模式且非PENDING时启用
    minDurationMinutes: Long = 5L,            // 最小持续时长
    maxDurationHours: Int = 24,              // 最大持续时长
)
```

#### **ActivitySelector（活动选择器）**

```kotlin
@Composable
fun ActivitySelector(
    selectedActivity: Activity?,             // 当前选中的活动(null=未选)
    allActivities: List<Activity>,           // 所有可用活动列表
    description: String,                     // 描述/备注文字
    onActivitySelect: (Activity) -> Unit,    // 选择活动回调
    onDescriptionChange: (String) -> Unit,   // 描述文字变更
    onAddActivity: (name: String, emoji: String) -> Unit, // 添加新活动
    modifier: Modifier = Modifier,
)
```

### 2.4 组件复用场景矩阵

| 组件                    | BehaviorUpsertSheet | 未来Dialog    | 未来FullScreen | 独立使用        |
| --------------------- | ------------------- | ----------- | ------------ | ----------- |
| DateTimeWheelPicker   | ✅ 起止时间各一            | ✅           | ✅            | ✅ 可作为独立选择器  |
| DurationSlider        | ✅ EDIT模式            | ✅           | ✅            | ⚠️ 需配合时间上下文 |
| ActivitySelector      | ✅                   | ✅           | ✅            | ✅ 可独立使用     |
| TagPicker             | ✅ 两处复用              | ✅           | ✅            | ✅ 已有独立接口    |
| BehaviorUpsertContent | ✅                   | ✅ 可嵌入Dialog | ✅ 可嵌入Page    | ✅ 核心复用单元    |

***

## 3. 数据流与状态管理

### 3.1 整体数据流架构

```
┌──────────────┐     ┌──────────────────────┐     ┌──────────────┐
│  HomeScreen  │────▶│   HomeViewModel      │────▶│  Repository  │
│   (UI层)     │◀────│   (ViewModel层)      │◀────│   (数据层)   │
└──────────────┘     └──────────────────────┘     └──────────────┘
       │                      │                            │
       ▼                      ▼                            ▼
  显示弹窗              管理UpsertState               Room Database
  用户交互              业务逻辑验证                  CRUD操作
       │                      │                            │
       └──────────────────────┴────────────────────────────┘
                         Flow 响应式更新
                         
触发链路:
GridCell.onLongClick(behaviorId)
  → HomeScreen.onCellLongPress(behaviorId)
  → viewModel.showEditSheet(behaviorId)
  → Repository.getBehaviorWithDetails(id) 
  → 构建 UpsertUiState(mode=EDIT, ...)
  → _upsertState.emit(newState)
  → BehaviorUpsertSheet 渲染
```

### 3.2 数据模型定义

#### **UpsertMode（模式枚举）**

```kotlin
enum class UpsertMode {
    ADD,   // 新增行为模式
    EDIT   // 编辑行为详情模式
}
```

#### **UpsertUiState（通用状态类）**

```kotlin
data class UpsertUiState(
    // ======== 模式标识 ========
    val mode: UpsertMode,
    
    // ======== EDIT模式专属字段 ========
    val behaviorId: Long? = null,                // ADD模式为null
    val originalBehavior: BehaviorWithDetails? = null, // 用于变更检测
    
    // ======== 时间相关 ========
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime?,             // null表示ACTIVE状态
    val isStartTimeExpanded: Boolean = false,    // 控制滚轮展开/折叠(v2.1)
    val isEndTimeExpanded: Boolean = false,
    
    // ======== 活动与描述 ========
    val selectedActivityId: Long? = null,
    val selectedActivity: Activity? = null,
    val description: String = "",               // 合并原"备注"字段
    
    // ======== 标签 ========
    val relatedTagIds: Set<Long> = emptySet(),   // 已选标签ID集合
    val availableTags: List<Tag> = emptyList(),  // 可选标签列表
    val availableActivities: List<Activity> = emptyList(),
    
    // ======== UI状态 ========
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val errorMessage: String? = null,            // 一次性消费(Snackbar)
    
    // ======== 计算属性(非持久化) ========
    val durationMinutes: Long? = null,           // 自动计算
    val formattedDuration: String = "",          // 格式化 "2h30m"
)
```

### 3.3 ViewModel 方法清单

#### **模式切换方法**

```kotlin
// 显示增加弹窗
fun showAddSheet()

// 显示编辑弹窗(挂起函数，需加载详情数据)
suspend fun showEditSheet(behaviorId: Long)

// 关闭弹窗
fun hideUpsertSheet()
```

#### **数据更新方法**

```kotlin
// 更新开始时间
fun updateStartDateTime(dateTime: LocalDateTime)

// 更新结束时间
fun updateEndDateTime(dateTime: LocalDateTime?)

// 更新选中活动
fun updateSelectedActivity(activity: Activity)

// 更新描述文字
fun updateDescription(description: String)

// 切换标签选中状态
fun toggleTag(tagId: Long)
```

#### **快捷操作方法**

```kotlin
// [上尾] 开始时间设为前一个行为的结束时间
fun fillStartToPreviousEnd()

// [当前] 开始/结束时间设为当前时刻
fun fillToNow(isStart: Boolean)
```

#### **持久化方法**

```kotlin
// 保存(创建或更新)行为
suspend fun saveUpsert()

// 删除行为(仅EDIT模式)
suspend fun deleteBehavior()

// 错误消息消费
fun consumeError()
```

### 3.4 saveUpsert() 核心逻辑

```kotlin
suspend fun saveUpsert() {
    val state = _upsertState.value ?: return
    
    // 1. 设置保存中状态
    _upsertState.update { it?.copy(isSaving = true) }
    
    try {
        when (state.mode) {
            UpsertMode.ADD -> {
                // 2a. 自动推断behaviorNature
                val nature = if (state.endDateTime != null) 
                    BehaviorNature.COMPLETED 
                else 
                    BehaviorNature.ACTIVE
                
                // 3a. 执行创建
                repository.createBehavior(
                    activityId = state.selectedActivityId!!,
                    startTime = state.startDateTime,
                    endTime = state.endDateTime,
                    nature = nature,
                    note = state.description.ifBlank { null },
                    tagIds = state.relatedTagIds.toList(),
                )
            }
            
            UpsertMode.EDIT -> {
                // 2b. 自动推断behaviorNature
                val nature = if (state.endDateTime != null) 
                    BehaviorNature.COMPLETED 
                else 
                    BehaviorNature.ACTIVE
                
                // 3b. 执行更新
                repository.updateBehavior(
                    id = state.behaviorId!!,
                    activityId = state.selectedActivityId!!,
                    startTime = state.startDateTime,
                    endTime = state.endDateTime,
                    nature = nature,
                    note = state.description.ifBlank { null},
                )
                
                // 3c. 全量替换关联标签
                repository.updateTagsForBehavior(
                    behaviorId = state.behaviorId,
                    tagIds = state.relatedTagIds.toList(),
                )
            }
        }
        
        // 4. 成功后关闭弹窗并刷新主页
        hideUpsertSheet()
        refreshHomeBehaviors()  // 触发Room Flow重新查询
        
    } catch (e: Exception) {
        // 5. 异常处理
        _upsertState.update { it?.copy(
            isSaving = false,
            errorMessage = when (e) {
                is SQLiteException -> "数据库保存失败，请重试"
                is TimeOverlapException -> "存在时间冲突: ${e.message}"
                is BehaviorNotFoundException -> "该行为已被删除"
                else -> "操作失败: ${e.message}"
            }
        )}
    }
}
```

### 3.5 数据同步机制（Room Flow）

```
保存/删除成功路径:
saveUpsert()/deleteBehavior()
  → Repository 写入 Room Database
  → Room 触发 Flow emit (INSERT/UPDATE/DELETE)
  → HomeViewModel.init() 中订阅的 loadHomeBehaviors() 收到新数据
  → _uiState.update { buildUiState(newBehaviors) } 重新构建主页状态
  → HomeScreen Compose Recomposition
  → 主页网格列表即时更新 + UpsertSheet关闭

关键保证:
✅ 不依赖隐式刷新，数据变更路径可追踪
✅ loadHomeBehaviors() 通过 stateIn(WhileSubscribed(5000)) 订阅
✅ 时间填充(fillLeftGap/fillRightGap)本质是修改startTime/endTime，复用同一路径
✅ isSaving=true期间禁用所有交互，防止重复提交
```

### 3.6 长按触发链路（完整版）

```kotlin
// 1. GridCell.kt - 注册长按事件
modifier = Modifier.combinedClickable(
    onClick = { onClick(behaviorId) },
    onLongClick = { onLongPress(behaviorId) }
)

// 2. TimeAxisGrid.kt → GridRow.kt - 传递回调
onCellLongPress = { behaviorId ->
    onBehaviorLongPress(behaviorId)
}

// 3. HomeScreen.kt - 连接ViewModel
onBehaviorLongPress = { behaviorId ->
    viewModelScope.launch {
        viewModel.showEditSheet(behaviorId)
    }
}

// 4. HomeScreen.kt - 条件渲染弹窗
val upsertState by viewModel.upsertState.collectAsStateWithLifecycle()

if (upsertState != null) {
    BehaviorUpsertSheet(
        mode = upsertState!!.mode,
        upsertState = upsertState!!,
        allActivities = activities,
        allTags = allTags,
        onDismiss = { viewModel.hideUpsertSheet() },
        onConfirm = { 
            viewModelScope.launch { viewModel.saveUpsert() } 
        },
        onDelete = if (upsertState?.mode == UpsertMode.EDIT) 
            ({ viewModelScope.launch { viewModel.deleteBehavior() } }) 
        else null,
        onAddActivity = { name, emoji -> viewModel.addActivity(name, emoji) },
        onAddTag = { name -> viewModel.addTag(name) },
    )
}
```

### 3.7 Repository 层新增/修改的方法

| 方法签名                                                                              | 用途               | 模式    |
| --------------------------------------------------------------------------------- | ---------------- | ----- |
| `suspend fun getBehaviorWithDetails(id: Long): BehaviorWithDetails`               | 加载行为完整详情(含活动、标签) | EDIT  |
| `suspend fun createBehavior(...)`                                                 | 创建新行为            | ADD   |
| `suspend fun updateBehavior(...)`                                                 | 批量更新行为字段         | EDIT  |
| `suspend fun updateTagsForBehavior(behaviorId: Long, tagIds: List<Long>)`         | 全量替换关联标签         | EDIT  |
| `suspend fun deleteBehavior(id: Long)`                                            | 删除行为及级联数据        | EDIT  |
| `suspend fun getPreviousEndTime(date: LocalDate): LocalDateTime?`                 | 获取前一个行为的结束时间     | \[上尾] |
| `fun getBehaviorsByDate(date: LocalDate, excludeIds: List<Long>): List<Behavior>` | 查询当天行为列表         | 重叠检测  |
| `fun getAllActivities(): Flow<List<Activity>>`                                    | 加载所有活动(缓存)       | 预加载   |
| `fun getAllTags(): Flow<List<Tag>>`                                               | 加载所有标签(缓存)       | 预加载   |

***

## 4. 核心组件详细规格

### 4.1 DateTimeWheelPicker（日期+时间一体化滚轮）⭐

这是v2.0最核心的新组件，完全替代v1.1的 `TimePickerCompact` 弹窗式选择器。

#### **视觉规格**

```
╭──────────────────────────────────────────╮
│  开始:    [上尾]  [当前]                  │ ← 快捷按钮行
│  ┌─────┬──────┬─────┐                   │
│  │04/26│  19  │ 12  │                   │ ← 上方第2项(alpha 0.4)
│  │今天 │  20  │ 13  │                   │ ← 中间项(高亮选中) ★
│  │04/28│  21  │ 14  │                   │ ← 下方第2项(alpha 0.4)
│  └─────┴──────┴─────┘                   │
│   日期    时    分                       │ ← 列标题
╰──────────────────────────────────────────╯
```

#### **尺寸与布局参数**

| 属性   | 数值                     | 说明                 |
| ---- | ---------------------- | ------------------ |
| 整体宽度 | \~320dp (自适应)          | 三列均分               |
| 整体高度 | 200dp                  | 固定高度               |
| 每列宽度 | 日期96dp / 时64dp / 分64dp | 按内容适配              |
| 每项高度 | 40dp                   | 固定行高               |
| 可见项数 | 5项                     | 上下各2项 + 中间1项       |
| 滚轮圆角 | 12dp                   | RoundedCornerShape |
| 背景   | surfaceContainerHigh   | Material3语义色       |

#### **数据源配置**

| 列名               | 数据范围              | 格式化规则                       | 总项数 |
| ---------------- | ----------------- | --------------------------- | --- |
| **DateColumn**   | today ± 7天 (共15天) | `MM/dd` 特殊值: `今天`/`明天`/`昨天` | 15  |
| **HourColumn**   | 00-23 (24小时制)     | `%02d` (补零两位)               | 24  |
| **MinuteColumn** | 00-55 (步长5分钟)     | `%02d` (补零两位)               | 12  |

#### **选中样式系统**

| 状态            | 字号        | 字重       | 背景               | 文字颜色               | Alpha     |
| ------------- | --------- | -------- | ---------------- | ------------------ | --------- |
| **中间项(选中)**   | bodyLarge | SemiBold | primaryContainer | onPrimaryContainer | 1.0       |
| **相邻第1项**     | bodySmall | Normal   | Transparent      | onSurface          | 0.7       |
| **相邻第2项**     | bodySmall | Normal   | Transparent      | onSurface          | 0.4       |
| **边缘项(不可见区)** | -         | -        | -                | -                  | 0.2 (理论值) |

#### **吸附与动画**

```kotlin
// 滚动停止时的吸附逻辑
LaunchedEffect(firstVisibleItemIndex) {
    // 节流100ms避免频繁触发
    delay(100)
    
    // 计算最近的整数索引(避免半格偏移)
    val targetIndex = calculateNearestSnapIndex(scrollState)
    
    // 平滑滚动到目标位置
    scrollState.animateScrollToItem(targetIndex)
    
    // 回调通知外部值变更
    onDateTimeChange(calculateDateTimeFromIndexes())
}
```

**动画参数：**

- 使用 `animateScrollToItem()` 默认弹簧动画
- 动画时长约 250ms (SpringSpec dampingRatio=0.8)
- 快捷按钮触发的滚动优先级高于手动拖拽

#### **边界约束逻辑**

```kotlin
// 开始时间 ≤ 结束时间的约束
if (isStartWheel && endDateTime != null) {
    maxDateTime = endDateTime.minusMinutes(5)  // 至少留5分钟间隙
}

if (isEndWheel) {
    minDateTime = startDateTime.plusMinutes(5)  // 不能早于开始+5分钟
}

// 跨天支持：允许选择 today±7天 的任意日期
// 但同一行为的 start/end 差值不超过 24 小时
```

#### **无障碍支持**

```kotlin
Modifier.semantics {
    role = Role.DropdownList
    contentDescription = "时间选择器，当前选中 ${formatDateTime(selected)}"
    stateDescription = if (isExpanding) "正在展开" else "已收起"
}
```

**列级别无障碍：**

- DateColumn: `"日期选择，${visibleItems.joinToString()}"`
- HourColumn: `"小时选择，${selectedHour}时"`
- MinuteColumn: `"分钟选择，${selectedMinute}分"`

**动作反馈：**

- 滚动停止时: `AccessibilityEvent.TYPE_VIEW_SELECTED` + 朗读 "已选择 ${date} ${hour}:${minute}"

### 4.2 DurationSlider（持续时间滑块）

用于快速调整行为的持续时间，提供比滚轮更直观的操作方式。

#### **视觉规格**

```
╭──────────────────────────────────────────────╮
│                                              │
│   ┌──●─────────────────────────────────┐     │
│   │  ← 圆形手柄 (直径24dp)             │     │
│   └────────────────────────────────────┘     │
│                                              │
│   0分钟                           8小时      │ ← 起止标签(可选)
╰──────────────────────────────────────────────╯
```

#### **尺寸参数**

| 属性   | 数值                                   | 说明          |
| ---- | ------------------------------------ | ----------- |
| 高度   | 48dp                                 | 包含轨道和手柄     |
| 轨道高度 | 4dp                                  | 细线轨道        |
| 手柄直径 | 24dp                                 | 可触摸圆形       |
| 手柄阴影 | 8dp elevation                        | Material3标准 |
| 轨道颜色 | outlineVariant (未激活) / primary (已激活) | 语义色         |
| 手柄颜色 | primaryContainer + border primary    | 与主题一致       |

#### **双向绑定机制**

```
用户拖动滑块
  → onValueChanged(progress: Float)
  → 计算 newEndTime = startTime + progress * maxDuration
  → 回调 onEndTimeChange(newEndTime)
  → ViewModel更新endDateTime
  → DateTimeWheelPicker(结束列)自动滚动到对应位置

反之亦然：
用户滚动结束时间滚轮
  → onDateTimeChange(newEnd)
  → ViewModel更新endDateTime
  → DurationSlider自动计算progress并重绘手柄位置
```

#### **计算公式**

```kotlin
val totalMinutes = Duration.between(startTime, endTime).toMinutes()
val maxMinutes = 24 * 60L  // 最大24小时
val progress = (totalMinutes.toFloat() / maxMinutes).coerceIn(0f, 1f)

// 反向计算
fun progressToEndTime(progress: Float): LocalDateTime {
    val targetMinutes = (progress * 24 * 60).toLong().coerceAtLeast(5L)
    return startTime.plusMinutes(targetMinutes)
}
```

#### **启用条件**

| 模式             | 条件            | 原因         |
| -------------- | ------------- | ---------- |
| ADD            | ❌ 始终隐藏        | 无结束时间概念    |
| EDIT-ACTIVE    | ⚠️ 需用户先设置结束时间 | 否则无法计算持续时间 |
| EDIT-COMPLETED | ✅ 完全可用        | 有明确的起止时间   |

#### **交互细节**

- **拖动手感**: 跟随手指实时移动(非离散步进)
- **吸附点**: 每5分钟一个微吸附(松手时自动对齐)
- **最小值保护**: 拖到最左侧时自动回弹到5分钟位置
- **视觉反馈**: 拖动时手柄放大1.1倍 + 增大阴影

### 4.3 ActivitySelector（活动选择器）

替代v1.1的 `ActivityPicker` 流式布局，采用截图中的蓝色圆形+号按钮设计。

#### **视觉规格**

```
╭──────────────────────────────────────────────╮
│  活动内容                              [?]  │ ← 标题行+帮助按钮
│                                              │
│  ┌──────────┐  ┌────────────────────────┐   │
│  │          │  │                        │   │
│  │    +     │  │                        │   │
│  │  (蓝色)  │  │  描述输入框...          │   │
│  │  圆形    │  │                        │   │
│  │  按钮    │  │                        │   │
│  └──────────┘  └────────────────────────┘   │
│               选择活动                      │ ← 占位符(未选时)
╰──────────────────────────────────────────────╯
```

#### **尺寸参数**

| 元素    | 尺寸                  | 样式                      |
| ----- | ------------------- | ----------------------- |
| +号按钮  | 56dp × 56dp         | 圆形, primary色背景, 白色+图标   |
| +号图标  | 24dp × 24dp         | Material Icons add      |
| 描述框高度 | MinHeight 120dp     | 多行文本, 圆角12dp            |
| 描述框背景 | surfaceContainerLow | 输入态surfaceContainerHigh |
| 间距    | 水平12dp              | 按钮与输入框之间                |

#### **状态变化**

| 状态      | 按钮外观                              | 描述框占位符          |
| ------- | --------------------------------- | --------------- |
| **未选择** | 蓝色背景 + 白色+号                       | "选择活动" (灰色提示文字) |
| **已选择** | 显示活动emoji图标 (56dp圆形, activity色背景) | "补充描述..." (可选)  |

#### **交互流程**

```
1. 点击蓝色+号按钮(未选状态)
   → 弹出 ActivitySelectionDialog (底部弹出或覆盖层)
   
2. ActivitySelectionDialog 内容:
   ╭──────────────────────────────╮
   │  选择活动              [✕]  │
   │  ┌──────┐ ┌──────┐ ┌────┐ │
   │  │😊健身│ │📚学习│ │🎮游戏│ │  ← 网格/列表
   │  └──────┘ └──────┘ └────┘ │
   │  [+ 添加新活动]            │
   ╰──────────────────────────────╯
   
3. 用户选择一项
   → Dialog关闭
   → 按钮显示该活动的emoji
   → 回调 onActivitySelect(activity)
   → 描述框获得焦点(可选)

4. 再次点击已选择的按钮
   → 弹出确认: "更换活动?" 或直接打开选择器
   → 支持清除选择(回到未选状态)
```

#### **描述输入框规格**

```kotlin
OutlinedTextField(
    value = description,
    onValueChange = onDescriptionChange,
    placeholder = { Text(if (selectedActivity == null) "选择活动" else "补充描述...") },
    minLines = 3,
    maxLines = 6,
    shape = RoundedCornerShape(12.dp),
)
```

**字符限制:** 最大200字（单行约40中文字符 × 5行）

### 4.4 TopBar（顶部栏 - 模式敏感）

#### **ADD模式布局**

```
╭──────────────────────────────────────────╮
│                                          │
│  新增行为                         ⋮     │ ← titleMedium
│                                          │
╰──────────────────────────────────────────╯
```

- 左侧: "新增行为" (`titleMedium`, `onSurface`)
- 右侧: 更多选项按钮 (⋮ icon, 24dp)
- ⋮菜单内容: 设置(可选)、帮助(可选)

#### **EDIT模式布局**

```
╭──────────────────────────────────────────╮
│                                          │
│  ⏱ 用时:2h30m                     ⋯     │ ← titleMedium (主标题)
│  🔥 深度工作                    COMPLETED │ ← bodyMedium (副标题)
│                                          │
╰──────────────────────────────────────────╯
```

- 主标题: "⏱ 用时:XhYm" (动态计算，`titleMedium`, primary色)
- 副标题: "{emoji} {activityName}" + 状态标签 (`bodyMedium`, `onSurface`)
- 右侧: 更多选项按钮 (⋮ icon, 24dp)

**⋮ 更多菜单内容 (EDIT模式专属)：**

```
┌─────────────────┐
│ 📋 复制行为      │  → 创建副本(预填相同数据)
│ ─────────────── │
│ 🗑️ 删除行为     │  → error色高亮，需二次确认
│ ─────────────── │
│ ℹ️ 查看详细信息  │  → 打开只读详情页(可选)
└─────────────────┘
```

#### **用时计算逻辑**

```kotlin
fun formatDuration(start: LocalDateTime, end: LocalDateTime?): String {
    if (end == null) return "进行中..."
    
    val duration = Duration.between(start, end)
    val hours = duration.toHours()
    val minutes = duration.toMinutesPart()  // Java 9+
    
    return when {
        hours > 0 && minutes > 0 -> "${hours}h${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}
```

### 4.5 BottomActionBar（底部操作栏 - 模式敏感）

#### **ADD模式**

```
╭──────────────────────────────────────────╮
│                                          │
│            [ 💡 增加 ]                   │ ← 全宽, filled, primary色
│                                          │
╰──────────────────────────────────────────╯
```

- **样式**: ButtonDefaults.filledButtonColors()
- **尺寸**: fillMaxWidth, height = 56dp
- **启用条件**: `selectedActivityId != null` (必须选择活动)
- **禁用状态**: 半透明 + 文字变灰 + 点击无效
- **保存中**: 显示 CircularProgressIndicator 替代文字

#### **EDIT模式**

```
╭──────────────────────────────────────────╮
│                                          │
│  [ ✅ 保存修改 ]      [ 🗑 删除 ]       │
│  (filled, flex=1)    (outlined, error)  │
╰──────────────────────────────────────────╯
```

**\[保存修改] 按钮:**

- 样式: filled, primary色, weight填充剩余空间
- 启用条件: `hasUnsavedChanges == true`
- 无修改时: 文字变为 "\[ 关闭 ]" 或直接禁用
- 保存中: 显示 CircularProgressIndicator

**\[删除] 按钮:**

- 样式: outlined, error色背景, fixed width
- 点击后: 弹出 DeleteConfirmDialog
- Dialog内容:
  ```
  ╭─────────────────────────────╮
  │     确认删除                 │
  │                             │
  │  确定要删除行为              │
  │  "{emoji} {name}" 吗？       │
  │                             │
  │  此操作不可撤销。            │
  │                             │
  │      [取消]  [确认删除]      │  ← 确认按钮error色
  ╰─────────────────────────────╯
  ```

***

## 5. 界面布局与视觉规范

### 5.1 整体布局结构图

```
╭═══════════════════════════════════════════════════════════════╮
║  ═══ 拖拽手柄 ═══                                            ║  ← ModalBottomSheet自带
╠═══════════════════════════════════════════════════════════════╣
║                                                              ║
║  ⏱ 用时:2h30m (或"新增行为")                      [⋮]       ║  ← TopBar
║  🔥 深度工作                                       COMPLETED ║  ← 副标题(EDIT)
║                                                              ║
║  ─────────── 时间设置 ───────────                             ║  ← Section分隔线
║  开始: [上尾][当前]    结束:        [当前]                    ║  ← 快捷按钮行
║  ┌─────┬────┬────┐  ┌─────┬────┬────┐                      ║
║  │today │ 20 │ 13 │  │today │ 20 │ 13 │                     ║  ← DateTimeWheelPicker
║  │04/28│ 21 │ 14 │  │04/28 │ 21 │ 14 │                     ║
║  └─────┴────┴────┘  └─────┴────┴────┘                      ║
║  ┌───●──────────────────────────────────────────┐           ║  ← DurationSlider
║  └──────────────────────────────────────────────┘           ║
║                                                              ║
║  ─────────── 相关标签 ───────────        [+]                 ║  ← SectionHeader
║  [#深度] [#专注] [#重要] ...                                ║  ← TagPicker(FlowRow)
║                                                              ║
║  ─────────── 活动内容 ───────────                 [?]       ║  ← SectionHeader
║  ┌──────────┐  ┌────────────────────────────┐             ║
║  │    😊     │  │                            │             ║  ← ActivitySelector
║  │ (56dp)   │  │  补充描述...               │             ║
║  └──────────┘  └────────────────────────────┘             ║
║                                                              ║
║  ─────────── 操作 ───────────                               ║  ← 分隔线
║  [ ✅ 保存修改 ]              [ 🗑 删除 ]                   ║  ← BottomActionBar
║                                                              ║
║  (底部安全区域内边距 32dp)                                    ║
╰═══════════════════════════════════════════════════════════════╝
```

### 5.2 各区域详细规范

#### **ModalBottomSheet 容器**

```kotlin
ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true  // 全屏展开，不半屏停靠
    ),
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    dragHandle = { DefaultDragHandle() },  // 自带拖拽条
    contentWindowInsets = WindowInsets(  // 底部安全区
        bottom = WindowInsets.navigationBars.getBottom(density)
    ),
)
```

#### **Section 标题样式**

```kotlin
Text(
    text = sectionTitle,
    style = MaterialTheme.typography.labelMedium,
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
)
```

**右侧辅助元素:**

- 相关标签: `[+]` 添加按钮 (labelSmall, primary色, 70%透明度)
- 活动内容: `[?]` 帮助按钮 (icon button, 20dp, outlineVariant色)

#### **DateTimeWheelPicker 容器**

```kotlin
Surface(
    modifier = Modifier
        .fillMaxWidth()
        .height(200.dp),
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
) {
    Row(
        modifier = Modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        DateColumn(modifier = Modifier.weight(1f))
        HourColumn(modifier = Modifier.weight(0.7f))
        MinuteColumn(modifier = Modifier.weight(0.7f))
    }
}
```

#### **快捷按钮 Chip 样式**

```kotlin
// [上尾] 按钮
FilterChip(
    selected = false,
    onClick = onQuickFillHead,
    label = { Text("上尾", style = MaterialTheme.typography.labelSmall) },
    colors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    ),
)

// [当前] 按钮
FilterChip(
    selected = false,
    onClick = onQuickFillCurrent,
    label = { Text("当前", style = MaterialTheme.typography.labelSmall) },
    colors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ),
)
```

**统一规格:** 高度28dp, 圆角20dp, labelSmall字号, 水平内边距12dp

#### **TagPicker 区域**

```kotlin
FlowRow(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
) {
    tags.forEach { tag ->
        Surface(
            onClick = { onToggle(tag.id) },
            shape = RoundedCornerShape(16.dp),
            color = if (isSelected) {
                tag.color.copy(alpha = 0.2f)  // 标签色的20%
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
            border = BorderStroke(
                1.dp,
                if (isSelected) tag.color else MaterialTheme.colorScheme.outlineVariant
            ),
        ) {
            Text(
                text = "#${tag.name}",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            )
        }
    }
    
    // 添加按钮
    Surface(
        onClick = onAddTag,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.dashed()),
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "添加标签",
            modifier = Modifier.size(16.dp).padding(8.dp),
        )
    }
}
```

### 5.3 颜色与主题集成

所有颜色值使用Material3语义色系统，确保自动支持亮色/暗色主题：

| 元素        | 亮色主题    | 暗色主题    | 语义色Token             |
| --------- | ------- | ------- | -------------------- |
| Sheet背景   | #F5F5F5 | #1C1C1E | surfaceContainerLow  |
| 滚轮容器      | #E8E8EA | #2C2C2E | surfaceContainerHigh |
| 选中项背景     | #E3F2FD | #1A3A5C | primaryContainer     |
| 选中项文字     | #1A73E8 | #93C5FD | onPrimaryContainer   |
| 主按钮背景     | #1A73E8 | #0B57D0 | primary              |
| 删除按钮文字    | #B3261E | #F2B8B5 | error                |
| Section标题 | #1A73E8 | #93C5FD | primary              |
| 分割线       | #E0E0E0 | #38383A | outlineVariant       |

### 5.4 响应式布局策略

| 屏幕宽度             | 布局调整                                           |
| ---------------- | ---------------------------------------------- |
| < 360dp (小屏手机)   | DateTimeWheelPicker三列紧凑排列(减少padding), 按钮堆叠     |
| 360-600dp (常规手机) | 标准布局(如上图)                                      |
| > 600dp (平板/横屏)  | DateTimeWheelPicker增加列宽, BottomActionBar按钮横向拉伸 |

***

## 6. 异常处理与边界情况

### 6.1 时间校验规则体系

#### **层级1：实时校验（UI层 - 交互即时反馈）**

**触发时机:** 用户滚动滚轮或拖动滑块时立即执行

```kotlin
sealed class TimeValidationResult {
    object Valid : TimeValidationResult()
    data class Warning(val message: String) : TimeValidationResult()  // 允许继续
    data class Error(val message: String) : TimeValidationResult()    // 阻止操作
}

fun validateRealTime(start: LocalDateTime, end: LocalDateTime?): TimeValidationResult {
    if (end == null) return TimeValidationResult.Valid  // ACTIVE允许null
    
    val duration = Duration.between(start, end).toMinutes()
    
    return when {
        start.isAfter(end) -> TimeValidationResult.Error("开始时间不能晚于结束时间")
        duration < 5 -> TimeValidationResult.Warning("持续时间不足5分钟")
        duration > 24 * 60 -> TimeValidationResult.Error("不能超过24小时")
        else -> TimeValidationResult.Valid
    }
}
```

**UI反馈映射：**

- `Valid`: 正常显示，无额外提示
- `Warning`: 滑块/滚轮边框变为橙色(warning色)，显示Toast提示
- `Error`: 禁用保存按钮，错误文字显示在组件下方（红色error色）

#### **层级2：保存前校验（ViewModel层 - 业务完整性检查）**

**触发时机:** 用户点击\[增加]/\[保存修改]按钮时

```kotlin
suspend fun validateBeforeSave(state: UpsertUiState) {
    // 1. 必填字段检查
    requireNotNull(state.selectedActivityId) { 
        ValidationError.MissingField("请选择活动") 
    }
    
    // 2. 时间逻辑检查
    if (state.endDateTime != null) {
        require(state.startDateTime.isBefore(state.endDateTime)) {
            ValidationError.InvalidTimeRange("开始时间必须早于结束时间")
        }
        
        val duration = Duration.between(state.startDateTime, state.endDateTime).toMinutes()
        require(duration >= 5) {
            ValidationError.TooShort("最短持续时间为5分钟，当前${duration}分钟")
        }
    }
    
    // 3. 时间重叠检测（仅EDIT模式）
    if (state.mode == UpsertMode.EDIT) {
        val overlaps = checkTimeOverlap(
            behaviorId = state.behaviorId!!,
            newStart = state.startDateTime,
            newEnd = state.endDateTime,
        )
        if (overlaps.isNotEmpty()) {
            throw TimeOverlapException(overlaps)  // 交由UI层处理
        }
    }
}
```

#### **层级3：Repository层校验（数据一致性保障）**

**触发时机:** 实际写入数据库前

```kotlin
@Transaction
suspend fun saveWithDatabaseValidation(state: UpsertUiState) {
    // 事务内再次检查（防止并发冲突）
    val currentVersion = dao.getVersion(state.behaviorId!!)
    if (currentVersion != state.originalBehavior?.version) {
        throw ConcurrentModificationException("数据已被其他地方修改")
    }
    
    // 执行写入
    dao.update(/* ... */)
    dao.replaceTags(/* ... */)
}
```

### 6.2 时间重叠检测算法

#### **精确判断函数**

```kotlin
/**
 * 判断两个时间段是否重叠
 * 
 * 规则: aStart < bEnd && aEnd > bStart
 * 边界相等(aEnd == bStart)视为相邻不重叠，允许无缝衔接
 */
fun isOverlapping(
    aStart: Long,  // epochMillis
    aEnd: Long,    // epochMillis (可为null表示进行中)
    bStart: Long,
    bEnd: Long     // epochMillis (可为null)
): Boolean {
    val effectiveAEnd = aEnd ?: Instant.now().toEpochMilli()
    val effectiveBEnd = bEnd ?: Instant.now().toEpochMilli()
    
    return aStart < effectiveBEnd && effectiveAEnd > bStart
}
```

#### **批量检测流程**

```kotlin
data class OverlappingConflict(
    val behaviorId: Long,
    val activityName: String,
    val emoji: String?,
    val conflictStart: LocalDateTime,
    val conflictEnd: LocalDateTime?,
    val overlapDescription: String,  // 人类可读描述
)

suspend fun checkTimeOverlap(
    behaviorId: Long,
    newStart: LocalDateTime,
    newEnd: LocalDateTime?
): List<OverlappingConflict> {
    // 1. 查询范围: 当天 ±1天 (防止跨天边界问题)
    val queryDate = newStart.toLocalDate()
    val dayBehaviors = repository.getBehaviorsInRange(
        startDate = queryDate.minusDays(1),
        endDate = queryDate.plusDays(2),
        excludeIds = listOf(behaviorId)
    ).filter { it.nature != BehaviorNature.PENDING }  // 排除无确定时间的
    
    // 2. 逐个比对
    return dayBehaviors.mapNotNull { other ->
        val otherEnd = other.endTime ?: other.startTime
        
        if (isOverlapping(
            aStart = newStart.toInstant(ZoneOffset.UTC).toEpochMilli(),
            aEnd = newEnd?.toInstant(ZoneOffset.UTC)?.toEpochMilli(),
            bStart = other.startTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
            bEnd = otherEnd.toInstant(ZoneOffset.UTC).toEpochMilli(),
        )) {
            OverlappingConflict(
                behaviorId = other.id,
                activityName = other.activityName,
                emoji = other.activityEmoji,
                conflictStart = other.startTime,
                conflictEnd = other.endTime,
                overlapDescription = buildOverlapDescription(
                    newStart, newEnd, other.startTime, otherEnd
                )
            )
        } else null
    }
}

private fun buildOverlapDescription(
    newStart: LocalDateTime, newEnd: LocalDateTime?,
    existStart: LocalDateTime, existEnd: LocalDateTime
): String {
    val newRange = "${formatTime(newStart)}-${newEnd?.let { formatTime(it) } ?: "进行中"}"
    val existRange = "${formatTime(existStart)}-${existEnd?.let { formatTime(it) } ?: "进行中"}"
    return "新时间[$newRange] 与 [$existRange] 重叠"
}
```

#### **重叠冲突UI处理流程**

```
用户点击[保存]
  ↓
ViewModel.saveUpsert()
  ↓
Repository抛出 TimeOverlapException(conflicts)
  ↓
ViewModel捕获异常
  ↓
设置 _upsertState.errorMessage = "检测到时间冲突"
  ↓
UI层检测到errorMessage
  ↓
弹出 AlertDialog:
╭─────────────────────────────────────╮
│  ⚠️ 检测到时间冲突                  │
│                                     │
│  以下行为与新设置的时间存在重叠：     │
│                                     │
│  • 🔥 深度工作                      │
│    14:00 - 16:00 (与你的15:00重叠)  │
│                                     │
│  • 📚 学习                          │
│    16:30 - 18:00 (与你的17:00重叠)  │
│                                     │
│  [返回调整]        [强制保存]        │
╰─────────────────────────────────────╯
  ↓
用户选择:
  ├─→ [返回调整]: 关闭Dialog，让用户修改时间
  └─→ [强制保存]: 调用 repository.forceSave() 并记录日志
```

### 6.3 状态锁定矩阵（v2.0 完整版）

| 功能/组件                        | ADD        | EDIT-PENDING | EDIT-ACTIVE         | EDIT-COMPLETED |
| ---------------------------- | ---------- | ------------ | ------------------- | -------------- |
| **DateTimeWheelPicker (开始)** | ✅ 完全可编辑    | ❌ PENDING无时间 | ⚠️ ≥ 前一个endTime     | ✅ 完全可编辑        |
| **DateTimeWheelPicker (结束)** | ❌ 隐藏       | ❌ 隐藏         | ✅ 可编辑(自动切COMPLETED) | ✅ 可编辑          |
| **\[上尾] 快捷按钮**               | ❌ 隐藏       | ❌ 隐藏         | ✅ 可用                | ✅ 可用           |
| **\[当前] 快捷按钮 (开始)**          | ✅ 可用       | ❌ 隐藏         | ✅ 可用                | ✅ 可用           |
| **\[当前] 快捷按钮 (结束)**          | ❌ 隐藏       | ❌ 隐藏         | ✅ 可用+自动完成           | ✅ 可用           |
| **DurationSlider**           | ❌ 隐藏       | ❌ 隐藏         | ⚠️ 需先设endTime       | ✅ 可用           |
| **ActivitySelector**         | ✅ 必填       | ⚠️ 可选(通常已有)  | ✅ 可修改               | ✅ 可修改          |
| **TagPicker**                | ✅ 可选       | ✅ 可修改        | ✅ 可修改               | ✅ 可修改          |
| **DescriptionInput**         | ✅ 可选       | ✅ 可修改        | ✅ 可修改               | ✅ 可修改          |
| **TopBar-用时显示**              | ❌ 隐藏       | N/A          | "进行中..."            | "XhYm"         |
| **\[增加] 按钮**                 | ✅ 显示(需选活动) | ❌ 隐藏         | ❌ 隐藏                | ❌ 隐藏           |
| **\[保存修改] 按钮**               | ❌ 隐藏       | ✅ 有变更时启用     | ✅ 有变更时启用            | ✅ 有变更时启用       |
| **\[删除] 按钮**                 | ❌ 隐藏       | ✅ 显示         | ✅ 显示                | ✅ 显示           |
| **⋮ 更多菜单**                   | 仅设置项       | 含删除选项        | 含删除选项               | 含删除选项          |

**图例:**

- ✅ 完全可用
- ⚠️ 有条件限制
- ❌ 隐藏或禁用
- N/A 不适用

### 6.4 关键边界场景处理

#### **场景1：跨天时间**

```
输入: 开始 04/27 23:50 → 结束 04/28 00:30

✅ 允许跨天（日期滚轮范围 ±7天）
⚠️ 重叠检测需扩展查询范围至两天: 04/27 和 04/28
✅ 用时计算正确: Duration.between = 40分钟
✅ 显示格式: "0h40m" (不显示天数)
```

#### **场景2：当天唯一行为的时间填充**

```
当前: 当天只有这一个行为

[← 填满左侧空闲]:
  currentIndex == 0 (第一个也是最后一个)
  → newStartTime = dayStart (当天00:00:00)

[填满右侧空闲 →]:
  currentIndex == size-1
  → newEndTime = dayEnd (当天24:00:00 =次日00:00)

结果: 行为占满整天 00:00 - 24:00
```

#### **场景3：前一个行为endTime为null (ACTIVE状态)**

```
时间轴: [行为A ACTIVE 10:00-???] [当前行为B 14:00-16:00]

点击 [上尾] 填充左侧:
  前一个(A).endTime == null
  → 使用 A.startTime 作为兜底 (保守策略)
  → newStartTime_B = 10:00 (而非期望的"当前时刻")

备选方案(可选):
  → 使用 LocalDateTime.now() 作为兜底
  → 或弹出提示让用户选择
```

#### **场景4：并发删除保护**

```
时间线:
T1: 用户打开行为X的详情页
T2: 其他设备/同步删除了行为X
T3: 用户点击[保存修改]

检测机制(T3):
1. 保存前重新查询 behaviorId 是否存在
2. SELECT version FROM behaviors WHERE id = X
3. 若返回0行 → BehaviorNotFoundException
4. ViewModel捕获 → 设置 errorMessage = "该行为已被删除，无法保存"

UI响应:
→ Snackbar显示错误信息 (红色, 3秒自动消失)
→ 延迟2秒后自动调用 hideUpsertSheet()
→ 主页已通过Flow自动刷新(显示删除后的状态)
```

#### **场景5：快速连续点击防抖**

```
问题: 用户快速双击[保存]导致重复提交

解决方案:
private var saveJob: Job? = null

fun saveUpsert() {
    // 取消前一次未完成的保存
    saveJob?.cancel()
    
    saveJob = viewModelScope.launch {
        // 500ms防抖窗口
        delay(500)
        
        // 检查是否仍处于可保存状态
        val state = _upsertState.value ?: return@launch
        if (state.isSaving) return@launch  // 已在保存中
        
        // 执行实际保存
        performSave(state)
    }
}
```

#### **场景6：空数据处理**

```kotlin
// 无活动时 ActivitySelector 显示:
if (allActivities.isEmpty()) {
    Column {
        ActivitySelectorButton(
            onClick = { showAddActivityDialog = true },
            isEmpty = true,  // 特殊状态
        )
        Text(
            text = "暂无活动，点击+添加",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// 无标签时 TagPicker 区域:
if (availableTags.isEmpty()) {
    Column {
        SectionHeader(title = "相关标签", onAddClick = onShowAddTagDialog)
        Surface(
            onClick = onShowAddTagDialog,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.dashed()),
        ) {
            Text(
                text = "+ 点击添加第一个标签",
                modifier = Modifier.padding(12.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}
```

### 6.5 统一异常处理策略

#### **异常分类体系**

```kotlin
sealed class UpsertException(message: String) : Exception(message)

// 用户输入错误
class ValidationException(val field: String, reason: String) : 
    UpsertException("$field: $reason")

// 业务规则冲突
class TimeOverlapException(val conflicts: List<OverlappingConflict>) :
    UpsertException("存在${conflicts.size}个时间冲突")

// 数据不存在
class BehaviorNotFoundException(val behaviorId: Long) :
    UpsertException("行为$behaviorId不存在")

// 并发冲突
class ConcurrentModificationException(expectedVersion: Long, actualVersion: Long) :
    UpsertException("数据已被修改(预期v$expectedVersion, 实际v$actualVersion)")

// 基础设施故障
class DatabaseException(cause: Throwable) :
    UpsertException("数据库操作失败", cause)

class NetworkException(cause: Throwable) :  // 如涉及同步
    UpsertException("网络错误", cause)
```

#### **异常处理管道**

```
Repository层 (throw)
  ↓
ViewModel层 (catch → map to UiState.errorMessage)
  ↓
UI层 (consume → showSnackbar → autoClear)
```

**ViewModel层统一catch块:**

```kotlin
catch (e: UpsertException) {
    _upsertState.update { currentState ->
        currentState?.copy(
            isSaving = false,
            errorMessage = when (e) {
                is ValidationException -> e.message  // 直接展示
                is TimeOverlapException -> "⚠️ ${e.message}"
                is BehaviorNotFoundException -> "❌ 该行为已被删除"
                is ConcurrentModificationException -> "🔄 数据已过期，请刷新"
                is DatabaseException -> "💾 保存失败，请检查网络后重试"
                is NetworkException -> "🌐 网络连接失败"
                else -> "未知错误: ${e.message}"
            }
        )
    }
    
    // 特殊情况: 自动关闭
    if (e is BehaviorNotFoundException) {
        viewModelScope.launch {
            delay(2000)
            hideUpsertSheet()
        }
    }
}
```

**UI层消费错误:**

```kotlin
LaunchedEffect(upsertState.errorMessage) {
    upsertState.errorMessage?.let { msg ->
        snackbarHost.showSnackbar(
            message = msg,
            duration = SnackbarDuration.Short  // 3秒
        )
        viewModel.consumeError()  // 一次性消费，避免重复显示
    }
}
```

### 6.6 无障碍（Accessibility）完整支持

#### **语义化角色与描述**

| 组件                      | Role         | contentDescription                 | 状态描述                    |
| ----------------------- | ------------ | ---------------------------------- | ----------------------- |
| **BehaviorUpsertSheet** | Dialog       | "行为${if(mode==ADD)"新增"else"}编辑弹窗"  | -                       |
| **DateTimeWheelPicker** | DropdownList | "时间选择器，当前${formattedDateTime}"     | expanding/collapsed     |
| **DateColumn**          | List         | "日期选择，${visibleItemsDescriptions}" | -                       |
| **HourColumn**          | List         | "小时选择，${selected}时"                | -                       |
| **MinuteColumn**        | List         | "分钟选择，${selected}分"                | -                       |
| **\[上尾] Chip**          | Button       | "将开始时间设为上一个行为的结束时间"                | -                       |
| **\[当前] Chip**          | Button       | "将${startOrEnd}时间设为当前时刻${now}"     | -                       |
| **DurationSlider**      | Slider       | "持续时间调整，当前${duration}分钟"           | value range             |
| **ActivitySelector按钮**  | Button       | "选择活动，${selected?:'未选择'}"          | -                       |
| **DescriptionInput**    | EditText     | "描述输入框，${length}个字符"               | -                       |
| **TagChip (选中)**        | Chip         | "标签${name}，已选中，双击取消"               | selected                |
| **TagChip (未选)**        | Chip         | "标签${name}，单击选择"                   | not selected            |
| **\[增加] Button**        | Button       | "创建新行为"                            | enabled/disabled        |
| **\[保存修改] Button**      | Button       | "保存对行为的修改"                         | saving/enabled/disabled |
| **\[删除] Button**        | Button       | "删除此行为，此操作不可撤销"                    | -                       |
| **⋮ Menu**              | PopupMenu    | "更多选项菜单"                           | expanded                |
| **DeleteConfirmDialog** | AlertDialog  | "确认删除对话框"                          | -                       |

#### **动态朗读示例**

```kotlin
// 滚轮选中项变化时
Modifier.semantics {
    liveRegion = LiveRegionMode.Polite  // 不打断当前语音
    contentDescription = "已选择 ${date} ${hour}时${minute}分"
}

// 保存状态变化时
LaunchedEffect(upsertState.isSaving) {
    if (upsertState.isSaving) {
        // 朗读"正在保存"
        announceForAccessibility(context, "正在保存，请稍候")
    }
}

// 错误发生时
LaunchedEffect(upsertState.errorMessage) {
    upsertState.errorMessage?.let {
        // 朗读错误信息 (高优先级，打断当前语音)
        announceForAccessibility(context, it, interruptCurrent = true)
    }
}
```

#### **键盘导航支持**

```
Tab键焦点顺序:
TopBar → [开始滚轮-日期] → [开始滚轮-时] → [开始滚轮-分]
→ [上尾按钮] → [当前-开始] → [结束滚轮-日期] → ...
→ [当前-结束] → DurationSlider → [标签区域] 
→ [活动按钮] → [描述框] → [增加/保存] → [删除]

方向键控制:
  在DateTimeWheelPicker内:
    ↑/↓: 滚动当前聚焦的列
    ←/→: 在三列间移动焦点
  
  在DurationSlider内:
    ←/↓: 减少时间
    ↑/→: 增加时间
  
  在TagPicker内:
    ←/↑/↓/→: 在标签网格中移动焦点
    Space/Enter: 切换选中状态
  
  在ActivitySelector:
    Enter/Space: 打开选择对话框
```

### 6.7 性能优化策略

#### **DateTimeWheelPicker 性能**

```kotlin
// 1. 使用稳定的key避免不必要的重组
items(
    count = items.size,
    key = { index -> items[index].hashCode() }  // 基于内容的稳定key
) { index ->
    WheelItem(item = items[index], ...)
}

// 2. 滚动节流 (避免频繁回调)
var scrollJob by remember { mutableStateOf<Job?>(null) }

LaunchedEffect(firstVisibleItemIndex) {
    scrollJob?.cancel()
    scrollJob = launch {
        delay(100)  // 100ms节流窗口
        onScrollStop(firstVisibleItemIndex)
    }
}

// 3. remember缓存计算结果
val visibleItems by remember(items) {
    derivedStateOf { 
        items.slice(centerIndex - 2..centerIndex + 2) 
    }
}
```

#### **UpsertUiState 快照优化**

```kotlin
// 只在真正变化时才创建新的state实例
_upsertState.update { current ->
    when {
        current.startDateTime == newStart -> current  // 返回原引用
        else -> current.copy(
            startDateTime = newStart,
            hasUnsavedChanges = true
        )
    }
}
```

#### **列表预加载与缓存**

```kotlin
// ViewModel初始化时预加载
private val _cachedTags = MutableStateFlow<List<Tag>>(emptyList())
private val _cachedActivities = MutableStateFlow<List<Activity>>(emptyList())

init {
    viewModelScope.launch {
        // 并行加载
        async { _cachedTags.emit(repository.getAllTags().first()) }
        async { _cachedActivities.emit(repository.getAllActivities().first()) }
    }
}

// showAddSheet/showEditSheet时直接使用缓存
fun showAddSheet() {
    _upsertState.value = UpsertUiState(
        mode = UpsertMode.ADD,
        availableTags = _cachedTags.value,  // 从缓存读取
        availableActivities = _cachedActivities.value,
        // ...
    )
}
```

#### **Compose重组优化**

```kotlin
// 使用@Stable注解确保数据类的稳定性
@Stable
data class UpsertUiState(...)  // 所有属性都是val或不可变集合

// 避免在Composable中进行复杂计算
val formattedDuration by remember(startDateTime, endDateTime) {
    derivedStateOf { formatDuration(startDateTime, endDateTime) }
}

// 使用key()控制子组件重组
key(upsertState.selectedActivityId) {
    ActivitySelector(
        activity = upsertState.selectedActivity,
        // 只有activityId变化时才重组此区域
    )
}
```

***

## 7. 实现难点分析与对策

### 7.1 核心难点清单

| 难点            | 复杂度   | 影响范围                         | 对策                                        |
| ------------- | ----- | ---------------------------- | ----------------------------------------- |
| **自定义三列联动滚轮** | ⭐⭐⭐⭐⭐ | DateTimeWheelPicker          | LazyColumn + 自实现SnapFlingBehavior吸附 (Canvas为降级方案) |
| **滚轮联动约束** | ⭐⭐⭐⭐  | DateTimeWheelPicker + ViewModel |  ViewModel层自动推后结束时间，保持minGap(5min) |
| **双向绑定滑块与滚轮** | ⭐⭐⭐   | DurationSlider ↔ WheelPicker | ViewModel StateFlow作为单一真相源，组件均为受控组件 |
| **弹窗嵌套滚动** | ⭐⭐⭐   | BehaviorUpsertContent        | Column(verticalScroll)外壳 + 滚轮区独立消费手势 |
| **时间重叠检测性能**  | ⭐⭐⭐   | 保存前校验                        | 空间索引 + 只查询当日±1天数据                         |
| **状态锁定矩阵实现**  | ⭐⭐⭐   | BehaviorUpsertContent        | when(mode)分支 + 可见性控制逻辑                    |
| **Nature状态降级确认** | ⭐⭐   | saveUpsert()                 | EDIT模式清除endTime时弹出二次确认 |
| **并发安全**      | ⭐⭐⭐   | ViewModel/Repository         | 乐观锁 + 防抖 + 异常捕获                           |
| **无障碍适配**     | ⭐⭐⭐   | 全部组件                         | semantics{} + announceForAccessibility    |

### 7.2 DateTimeWheelPicker 滚轮吸附算法详解

#### **设计决策**

**方案选型：** 采用 `LazyColumn` + **自实现 `SnapFlingBehavior`** 方案，不使用 `animateScrollToItem`。
- `animateScrollToItem` 的 `spring` 动画会与 `SnapFlingBehavior` 的物理惯性计算产生冲突
- `SnapFlingBehavior` 本身就是 Compose 为"惯性滚动后吸附到整项"设计的 API，体验最原生
- 若低端设备(2GB RAM)出现掉帧，降级为 `Canvas` + `PointerInput` 重写

#### **解决方案：自定义 SnapFlingBehavior**

```kotlin
@Composable
fun rememberWheelSnapFlingBehavior(
    lazyListState: LazyListState,
    itemHeightPx: Float,
): SnapFlingBehavior {
    return remember(lazyListState, itemHeightPx) {
        object : SnapFlingBehavior {
            override fun ScrollScope.performFling(initialVelocity: Float): Float {
                // 1. 基于物理惯性计算滚动的最终偏移量
                val remainingVelocity = initialVelocity
                val flingDistance = initialVelocity * 0.15f  // 阻尼系数
                
                // 2. 用 decayAnimation 模拟减速
                var consumed = 0f
                var velocity = initialVelocity
                // 简化的物理计算（生产代码可用 FlingCalculator）
                consumed = scrollBy(flingDistance)
                velocity *= (1 - abs(consumed / (flingDistance + 1f)))
                
                // 3. 计算吸附目标：找到最近整项位置
                val currentOffset = lazyListState.firstVisibleItemScrollOffset.toFloat()
                val targetItemIndex: Int
                val targetOffset: Float
                
                if (abs(currentOffset) < itemHeightPx / 2f) {
                    // 当前项已接近中心，吸附到当前项
                    targetItemIndex = lazyListState.firstVisibleItemIndex
                    targetOffset = 0f
                } else {
                    // 吸附到下一项
                    targetItemIndex = lazyListState.firstVisibleItemIndex + 
                        if (velocity > 0) 1 else 
                        (if (currentOffset > 0) 1 else 0)
                    targetOffset = 0f
                }
                
                // 4. 平滑吸附到目标
                lazyListState.animateScrollToItem(
                    index = targetItemIndex.coerceIn(0, lazyListState.layoutInfo.totalItemsCount - 1),
                    scrollOffset = targetOffset.toInt(),
                )
                
                return velocity  // 剩余速度
            }
        }
    }
}

@Composable
fun WheelColumn(
    items: List<T>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    itemHeight: Dp = 40.dp,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = maxOf(0, selectedIndex - 2)
    )
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }
    val snapBehavior = rememberWheelSnapFlingBehavior(listState, itemHeightPx)
    
    // 监听吸附完成事件
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val viewportCenter = listState.layoutInfo.viewportSize.height / 2
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            
            val nearestItem = visibleItems.minByOrNull { item ->
                abs((item.offset + item.size / 2) - viewportCenter)
            }
            
            nearestItem?.let { target ->
                if (target.index != selectedIndex) {
                    onSelectedChange(target.index)
                }
            }
        }
    }
    
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxHeight(),
        flingBehavior = snapBehavior,
        contentPadding = PaddingValues(vertical = itemHeightPx.toDp() * 2),  // 上下各2项空间
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(count = items.size, key = { index -> index }) { index ->
            WheelItem(
                item = items[index],
                isSelected = (index == selectedIndex),
                distanceFromCenter = abs(index - selectedIndex),
                itemHeight = itemHeight,
                onClick = {
                    listState.animateScrollToItem(index)
                },
            )
        }
    }
}
```

#### **关键优化点**

1. **专用 SnapFlingBehavior**：不再与 `animateScrollToItem` 混用，吸附逻辑全部在 `performFling` 内完成
2. **contentPadding**：上下各 2 项高度（80dp），确保首尾项也能滚到中心
3. **节流**：通过 `isScrollInProgress` 状态变化触发回调，避免频繁计算
4. **稳定key**：使用 `key = { index -> index }` 稳定重组
5. **Canvas 降级方案**（预留）：若性能不达预期，用 `Canvas` + `drawText` + `PointerInput` 重写单个视图替代三个 `LazyColumn`

#### **三列同步策略**

```kotlin
@Composable
fun DateTimeWheelPicker(...) {
    var dateIndex by remember { mutableIntStateOf(initialDateIndex) }
    var hourIndex by remember { mutableIntStateOf(initialHourIndex) }
    var minuteIndex by remember { mutableIntStateOf(initialMinuteIndex) }
    
    // 当任一列变化时，重新组装LocalDateTime并回调外部
    LaunchedEffect(dateIndex, hourIndex, minuteIndex) {
        val newDateTime = LocalDateTime.of(
            dateItems[dateIndex].toLocalDate(),
            LocalTime.of(hourItems[hourIndex], minuteItems[minuteIndex])
        )
        
        // 边界约束由 ViewModel 层处理（7.2.1节联动约束）
        onDateTimeChange(newDateTime)
    }
    
    Row {
        WheelColumn(items = dateItems, selected = dateIndex, 
            onSelectedChange = { dateIndex = it }, ...)
        WheelColumn(items = hourItems, selected = hourIndex,
            onSelectedChange = { hourIndex = it }, ...)
        WheelColumn(items = minuteItems, selected = minuteIndex,
            onSelectedChange = { minuteIndex = it }, ...)
    }
}
```

#### **7.2.1 开始-结束联动约束**

当用户在编辑模式下调整开始时间超过结束时间时，ViewModel 自动推后结束时间：

```kotlin
// ViewModel 层
fun updateStartDateTime(newStart: LocalDateTime) {
    val current = _upsertState.value ?: return
    
    val newEnd = if (current.endDateTime != null && 
                     !newStart.isBefore(current.endDateTime)) {
        // 自动将结束时间推后，保持 minGap 间隙
        newStart.plusMinutes(MIN_TIME_GAP_MINUTES)
    } else {
        current.endDateTime
    }
    
    _upsertState.update { 
        it?.copy(
            startDateTime = newStart,
            endDateTime = newEnd,
            hasUnsavedChanges = true
        ) 
    }
}

// 同理，调整结束时间时检查不会早于开始时间
fun updateEndDateTime(newEnd: LocalDateTime?) {
    val current = _upsertState.value ?: return
    
    val validEnd = if (newEnd != null && 
                       !newEnd.isAfter(current.startDateTime)) {
        null  // 非法值，丢弃
    } else {
        newEnd
    }
    
    _upsertState.update {
        it?.copy(endDateTime = validEnd, hasUnsavedChanges = true)
    }
}

companion object {
    const val MIN_TIME_GAP_MINUTES = 5L
}
```

滚轮本身不做任何回弹——所有约束在 ViewModel 层处理，UI 层仅展示 ViewModel 返回的最终值。

### 7.3 DurationSlider 与 DateTimeWheelPicker 双向绑定

#### **核心挑战**

两个组件操作同一个数据源(endTime)，需要避免循环更新。

#### **解决方案：单一真相源 + 方向标记**

```kotlin
@Composable
fun BehaviorUpsertContent(...) {
    // 内部状态：区分"滑块驱动"和"滚轮驱动"
    var updateSource by remember { mutableStateOf<UpdateSource?>(null) }
    
    enum class UpdateSource { SLIDER, WHEEL }
    
    // DurationSlider 回调
    val onSliderChange: (Float) -> Unit = { progress ->
        updateSource = UpdateSource.SLIDER
        val newEnd = progressToEndTime(progress)
        onEndTimeChange(newEnd)
    }
    
    // DateTimeWheelPicker(结束列)回调  
    val onWheelChange: (LocalDateTime) -> Unit = { newEnd ->
        updateSource = UpdateSource.WHEEL
        onEndTimeChange(newEnd)
    }
    
    // DurationSlider 的进度值：仅在非WHEEL驱动时响应state变化
    val sliderProgress by remember(state.endDateTime) {
        derivedStateOf {
            if (updateSource != UpdateSource.WHEEL) {  // 避免WHEEL更新时重置slider
                calculateProgress(state.startDateTime, state.endDateTime)
            } else {
                currentSliderProgress  // 保持当前位置
            }
        }
    }
    
    // 重置source标记（下一帧生效）
    LaunchedEffect(updateSource) {
        delay(16)  // 等待一帧
        updateSource = null
    }
}
```

#### **防抖优化**

```kotlin
// 滚轮快速滑动时不实时更新slider，停止后再同步
var wheelDebounceJob by remember { mutableStateOf<Job?>(null) }

LaunchedEffect(state.endDateTime) {
    wheelDebounceJob?.cancel()
    wheelDebounceJob = launch {
        delay(150)  // 150ms后同步给slider
        updateSliderPosition()
    }
}
```

### 7.4 时间重叠检测性能优化

#### **问题场景**

一天有100个行为时，O(n²)两两比较可能导致UI卡顿。

#### **优化方案1：时间线索引**

```kotlin
// Repository层预构建索引
class TimelineIndex private constructor(
    private val sortedBehaviors: List<Behavior>
) {
    companion object {
        fun build(behaviors: List<Behavior>): TimelineIndex {
            return TimelineIndex(
                behaviors.sortedBy { it.startTime.toEpochSecond() }
            )
        }
    }
    
    /**
     * 只检查相邻区间而非全量比对
     * 利用已排序特性提前终止
     */
    fun findOverlaps(
        start: Long,
        end: Long,
        excludeId: Long?
    ): List<Behavior> {
        val result = mutableListOf<Behavior>()
        
        for (behavior in sortedBehaviors) {
            // 排除自身
            if (excludeId != null && behavior.id == excludeId) continue
            
            val bStart = behavior.startTime.toEpochSecond()
            val bEnd = behavior.endTime?.toEpochSecond() ?: Long.MAX_VALUE
            
            // 提前终止：后续行为开始时间已晚于查询结束时间
            if (bStart >= end) break
            
            // 判断重叠
            if (start < bEnd && (end ?: Long.MAX_VALUE) > bStart) {
                result.add(behavior)
            }
        }
        
        return result
    }
}
```

**复杂度:** O(n) 最坏情况，实际平均 O(k) (k=重叠邻居数)

#### **优化方案2：Room查询级过滤**

```kotlin
@Query("""
    SELECT * FROM behaviors 
    WHERE date(startTime / 1000, 'unixepoch', 'localtime') = :queryDate
      AND nature IN ('ACTIVE', 'COMPLETED')
      AND id != :excludeId
      AND (
          (startTime <= :newEnd AND (endTime IS NULL OR endTime > :newStart))
      )
    ORDER BY startTime ASC
""")
suspend fun findPotentiallyOverlapping(
    queryDate: String,
    excludeId: Long,
    newStart: Long,
    newEnd: Long?
): List<BehaviorEntity>
```

**优势:** 在数据库层面完成初步过滤，减少传输到内存的数据量。

### 7.5 状态锁定矩阵的实现模式

```kotlin
@Composable
fun BehaviorUpsertContent(
    mode: UpsertMode,
    state: UpsertUiState,
    ...
) {
    Column {
        // ===== 时间区域 =====
        Row {
            // 开始时间滚轮
            DateTimeWheelPicker(
                dateTime = state.startDateTime,
                enabled = mode == UpsertMode.EDIT || 
                         (mode == UpsertMode.ADD && true),
                onDateTimeChange = onStartTimeChange,
                modifier = Modifier.weight(1f)
            )
            
            // 结束时间滚轮（条件显示）
            if (mode == UpsertMode.EDIT || 
                (mode == UpsertMode.ADD && state.selectedNature == COMPLETED)) {
                
                DateTimeWheelPicker(
                    dateTime = state.endDateTime ?: LocalDateTime.now(),
                    enabled = true,
                    minDateTime = state.startDateTime.plusMinutes(5),
                    onDateTimeChange = onEndTimeChange,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // 快捷按钮行
        Row {
            // [上尾] - 仅EDIT模式且非PENDING
            if (mode == UpsertMode.EDIT && state.endDateTime != null) {
                FilterChip(onClick = onQuickFillHead, ...) { Text("上尾") }
            }
            
            // [当前]-开始 - ADD和EDIT都可用（除PENDING）
            if (mode != UpsertMode.EDIT || state.endDateTime != null) {
                FilterChip(onClick = onQuickFillCurrentStart, ...) { Text("当前") }
            }
            
            // [当前]-结束 - 仅当结束时间可见时
            if (/* 结束滚轮显示 */) {
                FilterChip(onClick = onQuickFillCurrentEnd, ...) { Text("当前") }
            }
        }
        
        // DurationSlider - 仅EDIT-COMPLETED
        if (mode == UpsertMode.EDIT && state.endDateTime != null) {
            DurationSlider(
                startTime = state.startDateTime,
                endTime = state.endDateTime,
                enabled = true,
                onEndTimeChange = onEndTimeChange,
            )
        }
        
        // ===== 底部按钮区域 =====
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (mode) {
                UpsertMode.ADD -> {
                    Button(
                        onClick = onConfirm,
                        enabled = state.selectedActivityId != null && !state.isSaving,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Text("增加")
                        }
                    }
                }
                
                UpsertMode.EDIT -> {
                    Button(
                        onClick = onConfirm,
                        enabled = state.hasUnsavedChanges && !state.isSaving,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (state.isSaving) CircularProgressIndicator(...)
                        else Text("保存修改")
                    }
                    
                    OutlinedButton(
                        onClick = onDelete ?: {},
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, "删除")
                        Text("删除")
                    }
                }
            }
        }
    }
}
```

### 7.6 测试策略建议

#### **单元测试重点**

| 测试目标                     | 测试方法  | 覆盖场景                    |
| ------------------------ | ----- | ----------------------- |
| **isOverlapping算法**      | 参数化测试 | 相邻、包含、部分重叠、跨天、边界值       |
| **UpsertUiState.copy()** | 属性校验  | 所有字段独立变更、组合变更           |
| **formatDuration格式化**    | 表驱动测试 | 0分钟、30分、1h、1h30m、24h、跨天 |
| **validateRealTime**     | 状态机测试 | Valid→Warning→Error转换路径 |

#### **UI测试建议**

```kotlin
@Test
fun testDateTimeWheelPicker_snapBehavior() = runComposeUiTest {
    setContent {
        DateTimeWheelPicker(
            dateTime = LocalDateTime.now(),
            onDateTimeChange = { /* capture */ }
        )
    }
    
    // 模拟拖拽到半格位置
    onNodeWithTag("dateColumn").performTouchInput {
        swipeWithVelocity(center, center.copy(y = center.y - 50.dp.toPx()))
    }
    
    // 验证吸附到整数项
    onNodeWithText("今天").assertIsDisplayed()
}

@Test
fun testDurationSlider双向绑定() = runComposeUiTest {
    var end by mutableStateOf<LocalDateTime?>(null)
    
    setContent {
        DurationSlider(
            startTime = LocalDateTime.of(2026,4,27,10,0),
            endTime = end,
            onEndTimeChange = { end = it }
        )
    }
    
    // 拖动滑块
    onNodeWithTag("sliderThumb").performTouchInput {
        swipeWithVelocity(/* 向右拖动 */)
    }
    
    // 验证end时间更新
    assertNotNull(end)
    assertTrue(end!!.isAfter(LocalDateTime.of(2026,4,27,10,0)))
}
```

***

## 附录A：v1.1 → v2.0 变更对照表

| v1.1 组件/概念                              | v2.0 替代品                  | 变更原因                     |
| --------------------------------------- | ------------------------- | ------------------------ |
| `AddBehaviorSheet`                      | `BehaviorUpsertSheet`     | 合并为通用组件                  |
| `AddBehaviorSheetContent`               | `BehaviorUpsertContent`   | 同上                       |
| `TimeSelectionSection`                  | `DateTimeSelectionRow`    | 改为水平并排布局                 |
| `DateScrollPicker` + `TimeScrollPicker` | `DateTimeWheelPicker`     | 三列一体化滚轮                  |
| `TimePickerCompact`                     | ❌ 废弃                      | 功能被DateTimeWheelPicker覆盖 |
| `ActivityPicker` (FlowRow)              | `ActivitySelector` (+号按钮) | 匹配截图设计                   |
| `NoteInput`                             | DescriptionInput (内嵌)     | 合并到活动选择区                 |
| `BehaviorNatureSelector`                | ❌ 移除                      | 自动推断类型                   |
| `TimeFillActions`                       | ❌ 移至⋮菜单                   | 简化主界面                    |
| `DeleteConfirmDialog`                   | 保留或内联                     | EDIT模式使用                 |
| `BehaviorDetailUiState`                 | `UpsertUiState`           | 通用化，支持ADD/EDIT           |
| PENDING/ACTIVE/COMPLETED显式选择            | 自动推断                      | 根据endTime是否存在判断          |
| 垂直堆叠时间布局                                | 水平并排双列                    | 匹配截图设计                   |
| "备注"字段名                                 | "描述"                      | 更符合用户心智                  |
| 单一详情页功能                                 | ADD/EDIT双模式               | 复用同一套组件                  |

## 附录B：术语表

| 术语                   | 定义                              |
| -------------------- | ------------------------------- |
| **Upsert**           | UPDATE + INSERT 的缩写，表示"新增或更新"操作 |
| **Mode**             | ADD(新增) 或 EDIT(编辑)两种运行模式        |
| **WheelPicker**      | 滚轮式选择器，类似iOS UIDatePicker       |
| **Snapping**         | 滚动停止后自动对齐到整项的吸附行为               |
| **FlowRow**          | Compose流式布局容器，自动换行              |
| **Chip**             | 小型圆角标签按钮(Material3组件)           |
| **ModalBottomSheet** | 从底部弹出的模态面板                      |
| **LazyColumn**       | Compose懒加载列表组件                  |
| **derivedStateOf**   | Compose派生状态，避免不必要的重组            |
| **stateIn**          | Flow转StateFlow的操作符，控制订阅生命周期     |
| **WhileSubscribed**  | StateFlow订阅策略，在有观察者时保持活跃        |

## 附录C：参考资源

- [Material3 Design Spec](https://m3.material.io/)
- [Compose Picker最佳实践](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary)
- [Now in Android: Compose Performance](https://medium.com/androiddevelopers)
- [Kotlin Coroutines Patterns](https://kotlinlang.org/docs/coroutines-guide.html)

***

> **文档版本历史**
>
> - v1.0 (2026-04-27): 初版，基于原始需求文档
> - v1.1 (2026-04-27): 增加时间填充、删除确认等细节
> - **v2.0 (2026-04-27)**: **重构为通用组件架构，完全对齐截图UI设计** ✅ 当前版本

> **下一步行动**: 调用 `writing-plans` skill 创建详细的实现计划

