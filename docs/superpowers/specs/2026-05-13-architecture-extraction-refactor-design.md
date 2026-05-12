# NLtimer 全量架构优化设计 — 业务逻辑函数抽离复用

## 目标

对 NLtimer 项目进行全量架构优化，核心聚焦业务逻辑函数抽离复用。杜绝页面内重复编写相同逻辑，同类业务逻辑统一归集到公共 utils、hooks（ViewModel 基类）、service 层。逻辑与视图解耦，页面只做组装和配置调用。

## 约束

- 保持原有业务功能、页面样式、交互逻辑完全不变
- 只做结构抽离与复用重构
- 组件命名、文件目录、导出引入统一规范化

---

## 一、时间处理工具统一封装（core/data/util/TimeFormatUtils.kt 扩展）

### 1.1 共享 DateTimeFormatter 常量

当前 `DateTimeFormatter.ofPattern(...)` 在 12+ 处内联创建。统一扩展：

```kotlin
// 已有
val hhmmFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

// 新增
val hhmmssFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
val mmddFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd")
val yyyyMMddHHmmFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
val exportTimestampFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
```

### 1.2 Epoch 转换扩展函数

当前 epoch ↔ LocalTime/LocalDateTime 转换在 8+ 处内联重复：

```kotlin
fun Long.epochToLocalTime(): LocalTime
fun Long.epochToLocalDateTime(): LocalDateTime
fun Long.epochToLocalDate(): LocalDate
fun LocalTime.toEpochMillisToday(): Long
fun LocalDate.startOfDayMillis(): Long
fun LocalDate.endOfDayMillis(): Long
fun LocalDate.atTimeToEpochMillis(time: LocalTime): Long
```

### 1.3 时长格式化统一

当前有 3 种不同风格的内联时长格式化。统一为：

```kotlin
// 已有 formatDuration(ms) → "1时30分"
// 新增紧凑格式
fun formatDurationCompact(ms: Long): String  // → "1.5h" / "45m"
```

### 1.4 时间范围格式化

```kotlin
fun formatEpochTimeRange(startMs: Long, endMs: Long?): String  // → "09:30 - 10:00"
```

### 1.5 时间戳格式化（替换 SimpleDateFormat）

```kotlin
fun formatTimestamp(timestamp: Long): String  // → "2026-05-13 09:30"
fun formatExportTimestamp(): String  // → "20260513_093000"
```

**影响文件：** 15+ 个文件中的内联 formatter 和时间转换代码

---

## 二、状态展示映射统一（core/data/model 层）

### 2.1 BehaviorNature 显示符号

当前 `when(behavior.status)` 映射在 2 处重复：

```kotlin
// 在 BehaviorNature.kt 中添加
val BehaviorNature.displaySymbol: String
    get() = when (this) {
        COMPLETED -> "✓"
        ACTIVE -> "▶"
        PENDING -> "○"
    }
```

**影响文件：** BehaviorTimelineItem.kt, BehaviorListItem.kt

---

## 三、UI 工具函数统一（core/designsystem/component/ 扩展）

### 3.1 颜色解析扩展

当前 android.graphics.Color.valueOf 转换在 2 处重复：

```kotlin
// 新文件 core/designsystem/component/ColorExt.kt
@Composable
fun Long?.toComposeColor(default: Color = MaterialTheme.colorScheme.primary): Color
```

**影响文件：** BehaviorListItem.kt, BehaviorTimelineItem.kt

### 3.2 标签计数文本

当前 `if (selectedTagIds.isEmpty()) "+ 增加" else "${count} 个标签"` 在 3 处重复：

```kotlin
// core/designsystem/component/TextUtils.kt
fun tagCountLabel(count: Int, emptyLabel: String = "+ 增加"): String
```

**影响文件：** AddActivityDialog.kt, ActivityFormSheets.kt (2处)

### 3.3 通用占位页面组件

StatsScreen 和 SubScreen 结构完全相同的占位页：

```kotlin
// core/designsystem/component/PlaceholderScreen.kt
@Composable
fun PlaceholderScreen(icon: ImageVector, title: String, description: String)
```

**影响文件：** StatsScreen.kt, SubScreen.kt

---

## 四、DragFab 页面脚手架统一（core/designsystem/component/）

当前 3 个页面使用完全相同的 `Box + Scaffold + BottomBarDragFab` 布局模式：

```kotlin
// core/designsystem/component/DragFabScreenScaffold.kt
@Composable
fun DragFabScreenScaffold(
    dragFabState: DragFabState,
    fabOptions: FabDragOptions,
    onFabOptionSelected: (FabDragOption) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
)
```

**影响文件：** HomeScreen.kt, TagManagementScreen.kt, ActivityManagementScreen.kt

---

## 五、ViewModel 层规范化

### 5.1 消除 TimeConflictUtils 重复调用

BehaviorManagementViewModel.timeOverlaps() 应直接使用 TimeConflictUtils.hasTimeConflict()。

**影响文件：** BehaviorManagementViewModel.kt

### 5.2 BehaviorManagement 导入逻辑抽离

BehaviorManagementViewModel 中的导入业务逻辑应提取到独立的 ImportBehaviorUseCase。

**影响文件：** BehaviorManagementViewModel.kt → 新建 ImportBehaviorUseCase.kt

---

## 六、执行顺序

按依赖关系排序，确保每步不破坏编译：

1. **Phase 1 — core:data 层扩展**（无外部依赖）
   - TimeFormatUtils.kt 添加共享 formatter 常量
   - TimeFormatUtils.kt 添加 epoch 转换扩展函数
   - TimeFormatUtils.kt 添加 formatDurationCompact、formatEpochTimeRange
   - BehaviorNature 添加 displaySymbol 属性

2. **Phase 2 — core:designsystem 层扩展**（依赖 core:data）
   - 新建 ColorExt.kt（颜色解析扩展）
   - 新建 TextUtils.kt（tagCountLabel 等）
   - 新建 PlaceholderScreen.kt
   - 新建 DragFabScreenScaffold.kt

3. **Phase 3 — feature 模块替换内联代码**（依赖 Phase 1+2）
   - feature:behavior_management — 替换所有内联时间格式化/转换
   - feature:home — 替换内联时间格式化
   - feature:management_activities — 替换 SimpleDateFormat
   - feature:settings — 替换 SimpleDateFormat
   - feature:sub — 使用 PlaceholderScreen
   - feature:stats — 使用 PlaceholderScreen
   - feature:tag_management — 使用 DragFabScreenScaffold

4. **Phase 4 — ViewModel 规范化**（依赖 Phase 1）
   - BehaviorManagementViewModel 使用 TimeConflictUtils
   - 抽离导入逻辑到 ImportBehaviorUseCase

5. **Phase 5 — 验证**
   - 全量编译
   - 运行现有测试
