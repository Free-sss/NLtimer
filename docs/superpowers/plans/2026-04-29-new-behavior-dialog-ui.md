# 新增行为弹窗 UI 改造 - 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将现有新增行为弹窗改造为带双栏时间选择器（上尾/当前）的紧凑 Material 3 风格 UI

**架构：** 在 `feature:home` 模块的 `ui.sheet` 包下创建新的双栏时间选择器组件，重构 AddBehaviorSheet 使用新组件，保持与现有 ViewModel 和数据流的兼容性

**技术栈：** Jetpack Compose, Material 3, Kotlin Coroutines, Hilt DI

---

## 文件结构

### 新建文件
1. **`DualTimePicker.kt`** - 双栏时间选择器核心组件
   - 左栏「上尾」：显示上次行为结束时间列表
   - 右栏「当前」：显示系统当前时间及前后选项
   - 选中状态管理、回调接口

2. **`TimeListItem.kt`** - 时间项子组件
   - 单个时间行的 UI 渲染
   - 选中/未选中状态样式

### 修改文件
3. **`AddBehaviorSheet.kt`** - 重构主弹窗
   - 替换 TimePickerCompact 为 DualTimePicker
   - 调整布局使其更紧凑
   - 更新标签圆角和间距

4. **`HomeViewModel.kt`** (如果需要) - 数据准备
   - 提供"上尾"时间数据（上次行为结束时间）
   - 提供"当前"时间数据（系统时间）

---

## 任务分解

### 任务 1：创建 TimeListItem 组件

**文件：**
- 创建：`feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/TimeListItem.kt`

- [ ] **步骤 1：定义数据模型**

```kotlin
data class TimeOption(
    val dateLabel: String,
    val hour: String,
    val minute: String,
    val dateTime: LocalDateTime,
)
```

- [ ] **步骤 2：实现 TimeListItem Composable**

```kotlin
@Composable
fun TimeListItem(
    option: TimeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isSelected) MaterialTheme.colorScheme.surfaceContainerLowest
                else Color.Transparent
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = option.dateLabel,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier.width(56.dp),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = option.hour,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.End,
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            ),
        )

        Text(
            text = option.minute,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier.width(28.dp),
        )
    }
}
```

- [ ] **步骤 3：验证组件渲染**

在 Preview 中测试选中/未选中两种状态的视觉效果

---

### 任务 2：创建 DualTimePicker 核心组件

**文件：**
- 创建：`feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/DualTimePicker.kt`

- [ ] **步骤 1：定义状态和数据接口**

```kotlin
data class DualTimePickerState(
    val lastEndTimeOptions: List<TimeOption>,
    val currentTimeOptions: List<TimeOption>,
    val selectedLastEndIndex: Int = 1,
    val selectedCurrentIndex: Int = 1,
)

interface DualTimePickerCallback {
    fun onLastEndSelected(option: TimeOption, index: Int)
    fun onCurrentSelected(option: TimeOption, index: Int)
}
```

- [ ] **步骤 2：实现双栏布局结构**

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DualTimePicker(
    state: DualTimePickerState,
    callback: DualTimePickerCallback,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column {
            // 标题栏：黑色背景 + 白色文字
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(vertical = 10.dp),
            ) {
                HeaderColumn(text = "上尾", weight = 1f)
                VerticalDivider(color = Color.White.copy(alpha = 0.2f))
                HeaderColumn(text = "当前", weight = 1f)
            }

            // 内容区：左右两栏
            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                // 左栏：上尾
                TimeColumn(
                    options = state.lastEndTimeOptions,
                    selectedIndex = state.selectedLastEndIndex,
                    onSelect = { index ->
                        callback.onLastEndSelected(
                            state.lastEndTimeOptions[index],
                            index
                        )
                    },
                    modifier = Modifier.weight(1f),
                )

                // 分割线
                VerticalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.height(IntrinsicSize.Max),
                )

                // 右栏：当前
                TimeColumn(
                    options = state.currentTimeOptions,
                    selectedIndex = state.selectedCurrentIndex,
                    onSelect = { index ->
                        callback.onCurrentSelected(
                            state.currentTimeOptions[index],
                            index
                        )
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun HeaderColumn(
    text: String,
    weight: Float,
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            ),
            color = Color.White,
        )
    }
}

@Composable
private fun TimeColumn(
    options: List<TimeOption>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        options.forEachIndexed { index, option ->
            TimeListItem(
                option = option,
                isSelected = index == selectedIndex,
                onClick = { onSelect(index) },
            )
            if (index < options.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    thickness = 0.5.dp,
                )
            }
        }
    }
}
```

- [ ] **步骤 3：添加辅助函数生成时间选项**

```kotlin
fun generateLastEndTimeOptions(
    lastEndTime: LocalDateTime?,
): List<TimeOption> {
    if (lastEndTime == null) return emptyList()

    return listOf(
        lastEndTime.minusHours(1).toTimeOption("上次"),
        lastEndTime.toTimeOption(),
        lastEndTime.plusHours(1).toTimeOption(),
    )
}

fun generateCurrentTimeOptions(now: LocalDateTime): List<TimeOption> {
    return listOf(
        now.minusDays(1).toTimeOption("昨天"),
        now.toTimeOption("今天"),
        now.plusDays(1).toTimeOption("明天"),
    )
}

private fun LocalDateTime.toTimeOption(label: String? = null): TimeOption {
    val dateDisplay = label ?: formatToRelativeDate()
    return TimeOption(
        dateLabel = dateDisplay,
        hour = hour.toString().padStart(2, '0'),
        minute = minute.toString().padStart(2, '0'),
        dateTime = this,
    )
}
```

- [ ] **步骤 4：编写 Preview 测试**

验证双栏布局、选中状态切换、样式正确性

---

### 任务 3：重构 AddBehaviorSheet 使用新组件

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/AddBehaviorSheet.kt`

- [ ] **步骤 1：更新导入**

添加：
```kotlin
import com.nltimer.feature.home.ui.sheet.DualTimePicker
import com.nltimer.feature.home.ui.sheet.TimeListItem
import java.time.LocalDateTime
```

- [ ] **步骤 2：修改状态管理**

在 `AddBehaviorSheetContent` 中：

```kotlin
// 移除旧的时间选择器状态
// var startTime by remember { mutableStateOf(LocalTime.now()) }
// var endTime by remember { mutableStateOf<LocalTime?>(null) }

// 添加新的双栏时间选择器状态
var dualTimePickerState by remember {
    mutableStateOf(
        DualTimePickerState(
            lastEndTimeOptions = generateLastEndTimeOptions(lastBehaviorEndTime),
            currentTimeOptions = generateCurrentTimeOptions(LocalDateTime.now()),
        )
    )
}

var selectedStartTime by remember { mutableStateOf<LocalDateTime?>(null) }
```

- [ ] **步骤 3：替换时间选择器 UI**

将原来的 `TimePickerCompact` 调用替换为：

```kotlin
DualTimePicker(
    state = dualTimePickerState,
    callback = object : DualTimePickerCallback {
        override fun onLastEndSelected(option: TimeOption, index: Int) {
            dualTimePickerState = dualTimePickerState.copy(
                selectedLastEndIndex = index,
            )
            selectedStartTime = option.dateTime
        }

        override fun onCurrentSelected(option: TimeOption, index: Int) {
            dualTimePickerState = dualTimePickerState.copy(
                selectedCurrentIndex = index,
            )
            selectedStartTime = option.dateTime
        }
    },
    modifier = Modifier.padding(bottom = 4.dp),
)
```

- [ ] **步骤 4：调整标签区域样式**

更新 TagPicker 或 tags-container 的样式参数：

```kotlin
// 减小圆角和间距
TagPicker(
    // ...
    chipShape = RoundedCornerShape(6.dp),  // 从默认值改为 6dp
    horizontalSpacing = 6.dp,              // 从 8dp 改为 6dp
    verticalSpacing = 4.dp,               // 新增或减小
)
```

或者直接在 `tags-container` 的 Modifier 中调整。

- [ ] **步骤 5：更新提交逻辑**

修改 `onConfirm` 回调调用：

```kotlin
Button(
    onClick = {
        selectedActivityId?.let { activityId ->
            val finalStartTime = selectedStartTime?.toLocalTime() ?: LocalTime.now()
            onConfirm(activityId, selectedTagIds.toList(), finalStartTime, nature, note.ifBlank { null })
        }
    },
    // ... 其他参数不变
)
```

---

### 任务 4：准备 ViewModel 数据源

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeViewModel.kt`
- 或检查是否已有相关数据

- [ ] **步骤 1：查找上次行为结束时间**

```kotlin
// 在 HomeUiState 中添加字段
data class HomeUiState(
    // ... 现有字段
    val lastBehaviorEndTime: LocalDateTime? = null,  // 新增
)

// 在 ViewModel 中计算该值
private fun calculateLastBehaviorEndTime(): LocalDateTime? {
    // 从完成队列中找到最近的一条记录的结束时间
    return completedBehaviors
        .maxByOrNull { it.endTime }
        ?.endTime
}
```

- [ ] **步骤 2：传递给 AddBehaviorSheet**

确保 `AddBehaviorSheet` 接收此参数：

```kotlin
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    // ... 其他参数
) {
    // ...

    if (showAddBehaviorSheet) {
        AddBehaviorSheet(
            // ... 现有参数
            lastBehaviorEndTime = uiState.lastBehaviorEndTime,  // 新增
            onDismiss = { showAddBehaviorSheet = false },
            onConfirm = { /* ... */ },
        )
    }
}
```

---

### 任务 5：编译验证与测试

- [ ] **步骤 1：编译 feature:home 模块**

运行：`.\gradlew.bat :feature:home:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 2：编译 app 模块**

运行：`.\gradlew.bat :app:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：手动测试场景**

1. 打开应用 → 点击空白格子触发弹窗
2. 验证双栏时间选择器显示正确
3. 点击不同行验证选中状态切换
4. 验证"上尾"显示上次结束时间
5. 验证"当前"显示系统当前时间
6. 选择标签、输入备注
7. 点击"添加"按钮验证提交成功
8. 验证标签圆角变小（6px）
9. 验证整体布局更紧凑

- [ ] **步骤 4：修复发现的问题**

如有编译错误或 UI 问题，逐一修复并重新验证

---

### 任务 6：清理与优化

- [ ] **步骤 1：移除未使用的旧代码**

如果 `TimePickerCompact` 不再被其他地方使用，可以标记为 deprecated 或删除

- [ ] **步骤 2：代码格式化**

确保所有新代码符合项目代码风格：
- Kotlin 编码规范
- Compose 最佳实践
- Material Design 3 指南

- [ ] **步骤 3：性能优化**

检查是否有不必要的重组（recomposition）：
- 使用 `remember` 缓存计算结果
- 使用 `derivedStateOf` 派生状态
- 确保 key 参数正确

---

## 自检清单

### ✅ 规格覆盖度

| 规格需求 | 实现任务 |
|---------|---------|
| 左右双栏设计 | 任务 2（DualTimePicker）|
| 「上尾」标题栏 | 任务 2（HeaderColumn）|
| 「当前」标题栏 | 任务 2（HeaderColumn）|
| 列表式时间项 | 任务 1（TimeListItem）|
| 选中高亮状态 | 任务 1（isSelected 样式）|
| 未选中弱化 | 任务 1（颜色透明度）|
| MM/DD 格式（左栏）| 任务 2（generateLastEndTimeOptions）|
| 相对日期（右栏）| 任务 2（generateCurrentTimeOptions）|
| 紧凑标签圆角 6px | 任务 3（TagPicker 样式）|
| 备注输入框 | 已存在（无需改动）|
| 历史备注入口 | 可选（任务 6）|

### ✅ 占位符扫描

无占位符。所有步骤包含具体代码。

### ✅ 类型一致性

- `TimeOption` 在任务 1 定义，任务 2 使用 ✅
- `DualTimePickerState` 在任务 2 定义，任务 3 使用 ✅
- `DualTimePickerCallback` 接口一致 ✅

---

## 执行建议

**推荐方式：内联执行（executing-plans）**

原因：
- 任务数量适中（6 个主要任务）
- 文件相对集中（都在 feature:home 模块）
- 可以快速迭代和调试
- 便于实时查看效果

每个任务完成后进行编译验证，确保增量正确。
