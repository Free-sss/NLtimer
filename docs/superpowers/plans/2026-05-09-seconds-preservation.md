# 计时起始秒数保留 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 未拨动时间滚轮时保留精确秒数（打开弹窗或确认时刻），拨动滚轮后秒数归零，并在设置中提供策略选择。

**架构：** 在 `AddBehaviorState` 中记录弹窗打开时刻 `sheetOpenTime` 和 `userAdjustedTime` 标志。确认时通过 `resolveStartTime()` 根据策略和是否调整过时间决定秒数。新增 `SecondsStrategy` 枚举和 DataStore 持久化，在弹窗配置页面添加 UI 选项。

**技术栈：** Kotlin, Jetpack Compose, DataStore Preferences, Hilt

---

## 文件结构

| 文件 | 职责 | 改动类型 |
|------|------|----------|
| `core/data/.../model/SecondsStrategy.kt` | 秒数策略枚举（OPEN_TIME / CONFIRM_TIME） | 新增 |
| `core/data/.../SettingsPrefs.kt` | 新增 secondsStrategy 接口方法 | 修改 |
| `core/data/.../SettingsPrefsImpl.kt` | 新增 DataStore key + 读写实现 | 修改 |
| `core/data/.../model/DialogGridConfig.kt` | 新增 secondsStrategy 字段 | 修改 |
| `core/behaviorui/.../sheet/AddBehaviorState.kt` | 核心逻辑：sheetOpenTime、userAdjustedTime、resolveStartTime | 修改 |
| `core/behaviorui/.../sheet/DualTimePickerComponent.kt` | 移除 startProperty/endProperty 的 withSecond(0).withNano(0) | 修改 |
| `core/behaviorui/.../sheet/AddBehaviorSheetContent.kt` | 确认流程改造：调用 resolveStartTime | 修改 |
| `core/behaviorui/.../sheet/TimeAdjustmentComponent.kt` | 重置/现在按钮标记 userAdjustedTime | 修改 |
| `feature/settings/.../ui/DialogConfigScreen.kt` | 新增秒数策略选择 UI | 修改 |
| `feature/settings/.../ui/DialogConfigViewModel.kt` | 无需改动（通过 DialogGridConfig 透传） | — |
| `feature/settings/.../ui/DialogConfigViewModelTest.kt` | 补充秒数策略测试 | 修改 |

---

### 任务 1：新增 SecondsStrategy 枚举

**文件：**
- 创建：`core/data/src/main/java/com/nltimer/core/data/model/SecondsStrategy.kt`

- [ ] **步骤 1：创建枚举文件**

```kotlin
package com.nltimer.core.data.model

enum class SecondsStrategy(val key: String) {
    OPEN_TIME("open_time"),
    CONFIRM_TIME("confirm_time")
}
```

- [ ] **步骤 2：确认编译通过**

运行：`./gradlew :core:data:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/model/SecondsStrategy.kt
git commit -m "feat: 新增 SecondsStrategy 枚举 — 秒数保留策略"
```

---

### 任务 2：DialogGridConfig 新增 secondsStrategy 字段

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/model/DialogGridConfig.kt`

- [ ] **步骤 1：在 DialogGridConfig 添加字段**

在 `pathDrawMode` 字段后添加：

```kotlin
data class DialogGridConfig(
    // ... 现有字段 ...
    val pathDrawMode: PathDrawMode = PathDrawMode.StartToEnd,
    val secondsStrategy: SecondsStrategy = SecondsStrategy.OPEN_TIME,
)
```

同时添加 import：`import com.nltimer.core.data.model.SecondsStrategy`（同包内无需 import）

- [ ] **步骤 2：确认编译通过**

运行：`./gradlew :core:data:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/model/DialogGridConfig.kt
git commit -m "feat: DialogGridConfig 新增 secondsStrategy 字段"
```

---

### 任务 3：SettingsPrefs 接口 + SettingsPrefsImpl 实现

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/SettingsPrefs.kt`
- 修改：`core/data/src/main/java/com/nltimer/core/data/SettingsPrefsImpl.kt`

- [ ] **步骤 1：SettingsPrefs 接口无需额外方法**

secondsStrategy 已作为 DialogGridConfig 的一部分，通过 `getDialogConfigFlow()` / `updateDialogConfig()` 透传，无需新增接口方法。

- [ ] **步骤 2：SettingsPrefsImpl 在 getDialogConfigFlow 添加 secondsStrategy 读取**

在 `getDialogConfigFlow()` 的 `DialogGridConfig(...)` 构造中追加：

```kotlin
DialogGridConfig(
    // ... 现有字段 ...
    pathDrawMode = try { PathDrawMode.valueOf(prefs[pathDrawModeKey] ?: PathDrawMode.StartToEnd.name) } catch (_: IllegalArgumentException) { PathDrawMode.StartToEnd },
    secondsStrategy = try { SecondsStrategy.valueOf(prefs[secondsStrategyKey] ?: SecondsStrategy.OPEN_TIME.name) } catch (_: IllegalArgumentException) { SecondsStrategy.OPEN_TIME },
)
```

- [ ] **步骤 3：SettingsPrefsImpl 在 updateDialogConfig 添加 secondsStrategy 写入**

在 `updateDialogConfig()` 的 `dataStore.edit` 块中追加：

```kotlin
prefs[secondsStrategyKey] = config.secondsStrategy.name
```

- [ ] **步骤 4：SettingsPrefsImpl companion object 新增 key**

```kotlin
private val secondsStrategyKey = stringPreferencesKey("seconds_strategy")
```

- [ ] **步骤 5：确认编译通过**

运行：`./gradlew :core:data:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 6：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/SettingsPrefsImpl.kt
git commit -m "feat: SettingsPrefsImpl 支持 secondsStrategy 持久化"
```

---

### 任务 4：AddBehaviorState 核心逻辑改造

**文件：**
- 修改：`core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorState.kt`

- [ ] **步骤 1：修改 now 和 startTime/endTime 初始化**

将第 70-76 行：

```kotlin
private val now = LocalDateTime.now().withSecond(0).withNano(0)
var startTime by mutableStateOf(
    initialStartTime?.let { now.withHour(it.hour).withMinute(it.minute) } ?: now
)
var endTime by mutableStateOf(
    initialEndTime?.let { now.withHour(it.hour).withMinute(it.minute) } ?: now
)
```

替换为：

```kotlin
val sheetOpenTime: LocalDateTime = LocalDateTime.now()
private val now = sheetOpenTime
var userAdjustedTime by mutableStateOf(false)
var startTime by mutableStateOf(
    initialStartTime?.let { now.withHour(it.hour).withMinute(it.minute).withSecond(it.second) } ?: now
)
var endTime by mutableStateOf(
    initialEndTime?.let { now.withHour(it.hour).withMinute(it.minute).withSecond(it.second) } ?: now
)
```

关键变化：
- `now` 不再 `.withSecond(0).withNano(0)`，保留完整时间
- `sheetOpenTime` 记录进入弹窗的精确时刻
- `userAdjustedTime` 默认 false
- `startTime`/`endTime` 初始化时保留 incoming LocalTime 的秒数

- [ ] **步骤 2：新增 resolveStartTime 方法**

在 `AddBehaviorState` 类末尾（`optionsRowHeight` 之后）添加：

```kotlin
fun resolveStartTime(strategy: SecondsStrategy, confirmTime: LocalDateTime): LocalDateTime {
    return if (userAdjustedTime) {
        startTime.withSecond(0).withNano(0)
    } else {
        val sourceSeconds = when (strategy) {
            SecondsStrategy.OPEN_TIME -> sheetOpenTime.second
            SecondsStrategy.CONFIRM_TIME -> confirmTime.second
        }
        startTime.withSecond(sourceSeconds).withNano(0)
    }
}
```

添加 import：`import com.nltimer.core.data.model.SecondsStrategy`

- [ ] **步骤 3：确认编译通过**

运行：`./gradlew :core:behaviorui:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorState.kt
git commit -m "feat: AddBehaviorState 保留秒数 + resolveStartTime 逻辑"
```

---

### 任务 5：DualTimePickerComponent 移除强制截断

**文件：**
- 修改：`core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/DualTimePickerComponent.kt`

- [ ] **步骤 1：DualTimePicker 中修改 startProperty/endProperty**

将第 47-48 行：

```kotlin
val startProperty = remember(startTime) { startTime.withSecond(0).withNano(0) }
val endProperty = remember(endTime) { endTime.withSecond(0).withNano(0) }
```

替换为：

```kotlin
val startProperty = remember(startTime) { startTime.withNano(0) }
val endProperty = remember(endTime) { endTime.withNano(0) }
```

保留纳秒归零（无意义），但保留秒数。

- [ ] **步骤 2：SingleTimePicker 中修改 startProperty**

将第 210 行：

```kotlin
val startProperty = remember(startTime) { startTime.withSecond(0).withNano(0) }
```

替换为：

```kotlin
val startProperty = remember(startTime) { startTime.withNano(0) }
```

- [ ] **步骤 3：确认编译通过**

运行：`./gradlew :core:behaviorui:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/DualTimePickerComponent.kt
git commit -m "fix: DualTimePicker/SingleTimePicker 保留秒数，不再强制截断"
```

---

### 任务 6：TimeAdjustmentComponent 标记 userAdjustedTime

**文件：**
- 修改：`core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/TimeAdjustmentComponent.kt`

需要将 `onTimeChanged` 回调改为同时通知上层"用户主动调整了时间"。

- [ ] **步骤 1：新增 onUserAdjusted 参数**

将 `TimeAdjustmentComponent` 函数签名改为：

```kotlin
@Composable
fun TimeAdjustmentComponent(
    currentTime: LocalDateTime,
    onTimeChanged: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
    maxTime: LocalDateTime? = null,
    onUserAdjusted: () -> Unit = {},
)
```

在所有 `onTimeChanged(...)` 调用之前添加 `onUserAdjusted()`。具体修改：

- 第 36 行 "重置" 按钮：
  ```kotlin
  "重置" to { onUserAdjusted(); onTimeChanged(LocalDateTime.now().withSecond(0).withNano(0)) },
  ```
- 第 37-39 行 "-1"/"-5"/"-15" 按钮：
  ```kotlin
  "-1" to { onUserAdjusted(); onTimeChanged(currentTime.plusMinutes(-1)) },
  "-5" to { onUserAdjusted(); onTimeChanged(currentTime.plusMinutes(-5)) },
  "-15" to { onUserAdjusted(); onTimeChanged(currentTime.plusMinutes(-15)) },
  ```
- 第 42 行 "现在" 按钮：
  ```kotlin
  "现在" to { onUserAdjusted(); onTimeChanged(LocalDateTime.now().withSecond(0).withNano(0)) },
  ```
- 第 43-54 行 "+1"/"+5"/"+15" 按钮：
  ```kotlin
  "+1" to {
      onUserAdjusted()
      val newTime = currentTime.plusMinutes(1)
      onTimeChanged(if (maxTime != null && newTime > maxTime) maxTime else newTime)
  },
  "+5" to {
      onUserAdjusted()
      val newTime = currentTime.plusMinutes(5)
      onTimeChanged(if (maxTime != null && newTime > maxTime) maxTime else newTime)
  },
  "+15" to {
      onUserAdjusted()
      val newTime = currentTime.plusMinutes(15)
      onTimeChanged(if (maxTime != null && newTime > maxTime) maxTime else newTime)
  },
  ```

- [ ] **步骤 2：TimeAdjustmentCard 透传 onUserAdjusted**

在 `TimeAdjustmentSection.kt` 的 `TimeAdjustmentCard` 中透传：

```kotlin
@Composable
fun TimeAdjustmentCard(
    currentTime: LocalDateTime,
    onTimeChanged: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
    maxTime: LocalDateTime? = null,
    onUserAdjusted: () -> Unit = {},
) {
    // ... Surface 不变 ...
    TimeAdjustmentComponent(
        currentTime = currentTime,
        onTimeChanged = onTimeChanged,
        maxTime = maxTime,
        onUserAdjusted = onUserAdjusted,
    )
}
```

- [ ] **步骤 3：TimeAdjustmentOverlay 透传 onUserAdjusted**

在 `TimeAdjustmentSection.kt` 的 `TimeAdjustmentOverlay` 中：

函数签名新增：
```kotlin
onUserAdjusted: () -> Unit = {},
```

所有 `TimeAdjustmentCard` 调用添加 `onUserAdjusted = onUserAdjusted`。

- [ ] **步骤 4：AddBehaviorSheetContent 中连接 onUserAdjusted 和滚轮调整标记**

在 `AddBehaviorSheetContent.kt` 中：

**(a) TimeAdjustmentOverlay 传参**（约第 120-129 行）：

```kotlin
if (state.showTimeAdjustments && mode != BehaviorNature.PENDING) {
    TimeAdjustmentOverlay(
        mode = mode,
        startTime = state.startTime,
        endTime = state.endTime,
        innerBoxPositionInWindow = state.innerBoxPositionInWindow,
        boxPositionInWindow = state.boxPositionInWindow,
        onStartTimeChanged = { state.startTime = it },
        onEndTimeChanged = { state.endTime = it },
        onUserAdjusted = { state.userAdjustedTime = true },
    )
}
```

**(b) 滚轮变化标记 userAdjustedTime** — 在 `TimePickerSection` 的调用中：

将 `SingleTimePicker` 的 `onTimeChanged`（约第 350 行）：
```kotlin
onTimeChanged = { state.startTime = it },
```
改为：
```kotlin
onTimeChanged = { state.startTime = it; state.userAdjustedTime = true },
```

将 `DualTimePicker` 的 `onTimesChanged`（约第 338-341 行）：
```kotlin
onTimesChanged = { start, end ->
    if (state.startTime != start) state.startTime = start
    if (state.endTime != end) state.endTime = end
},
```
改为：
```kotlin
onTimesChanged = { start, end ->
    if (state.startTime != start) { state.startTime = start; state.userAdjustedTime = true }
    if (state.endTime != end) { state.endTime = end; state.userAdjustedTime = true }
},
```

注意：`DualTimePicker` 和 `SingleTimePicker` 内部 `LaunchedEffect` 首次同步也会触发 `onTimesChanged`/`onTimeChanged`，但这不会影响逻辑——因为初始值与 `startTime` 相同，`if (state.startTime != start)` 会跳过。只有用户真正拨动滚轮后值才会不同，才会设置 `userAdjustedTime = true`。

但存在一个边界情况：`DualTimePickerComponent` 中 `startProperty = remember(startTime) { startTime.withNano(0) }`，当 `startTime` 有秒数时 `startProperty` 会丢失秒数的显示（滚轮不显示秒），然后 `leftDateTime = selectedDate.date.atTime(hour, minute)` 构造出的时间秒数为 0。这会导致首次 `LaunchedEffect(leftDateTime, rightDateTime)` 触发时 `leftDateTime(秒=0) != lastNotifiedStart(秒=非0)`，从而调用 `onTimesChanged` 并设置 `userAdjustedTime = true`。

**解决方案**：在 `DualTimePicker` 的 `LaunchedEffect` 中，对外回调的时间保留原始秒数。修改 `leftDateTime` 和 `rightDateTime`：

```kotlin
val leftDateTime = remember(leftSelectedDate, leftSelectedHour, leftSelectedMinute) {
    leftSelectedDate.date.atTime(leftSelectedHour.toInt(), leftSelectedMinute.toInt())
        .withSecond(startProperty.second)
}
val rightDateTime = remember(rightSelectedDate, rightSelectedHour, rightSelectedMinute) {
    rightSelectedDate.date.atTime(rightSelectedHour.toInt(), rightSelectedMinute.toInt())
        .withSecond(endProperty.second)
}
```

同样地，`SingleTimePicker` 的 `currentDateTime`：

```kotlin
val currentDateTime = remember(selectedDate, selectedHour, selectedMinute) {
    selectedDate.date.atTime(selectedHour.toInt(), selectedMinute.toInt())
        .withSecond(startProperty.second)
}
```

这样当用户没有拨动滚轮时，回调的时间秒数与初始值一致，`state.startTime != start` 为 false，不会触发 `userAdjustedTime = true`。当用户拨动滚轮后，时/分改变，秒数保留（来自 startProperty），但此时时/分已变，`state.startTime != start` 为 true，正确标记。

- [ ] **步骤 5：确认编译通过**

运行：`./gradlew :core:behaviorui:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 6：Commit**

```bash
git add core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/TimeAdjustmentComponent.kt core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/TimeAdjustmentSection.kt core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/DualTimePickerComponent.kt core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorSheetContent.kt
git commit -m "feat: TimeAdjustment/滚轮调整标记 userAdjustedTime + 保留秒数回传"
```

---

### 任务 7：确认流程改造 — 使用 resolveStartTime

**文件：**
- 修改：`core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorSheetContent.kt`

- [ ] **步骤 1：修改 ConfirmButtonRow 确认回调**

`AddBehaviorSheetContent` 需要接收 `dialogConfig` 中的 `secondsStrategy`，并在确认时调用 `state.resolveStartTime()`。

但 `AddBehaviorSheetContent` 已有 `dialogConfig` 参数，可以直接使用 `dialogConfig.secondsStrategy`。

修改 `ConfirmButtonRow` 的签名，新增 `secondsStrategy`：

```kotlin
@Suppress("LongMethod")
@Composable
private fun ConfirmButtonRow(
    state: AddBehaviorState,
    mode: BehaviorNature,
    secondsStrategy: SecondsStrategy,
    onConfirm: (Long, List<Long>, LocalTime, LocalTime?, BehaviorNature, String?) -> Unit,
    onDismiss: () -> Unit,
)
```

修改确认按钮 onClick 中的 `onConfirm` 调用（约第 430-438 行）：

```kotlin
onClick = {
    if (mode == BehaviorNature.COMPLETED
        && !state.startTime.toLocalTime().isBefore(state.endTime.toLocalTime())
    ) {
        Toast.makeText(context, "开始时间必须早于结束时间", Toast.LENGTH_SHORT).show()
        return@Button
    }
    val confirmTime = LocalDateTime.now()
    val resolvedStartTime = state.resolveStartTime(secondsStrategy, confirmTime)
    val resolvedEndTime = if (mode == BehaviorNature.COMPLETED) {
        if (state.userAdjustedTime) state.endTime.withSecond(0).withNano(0) else state.endTime.withSecond(confirmTime.second).withNano(0)
    } else null
    state.selectedActivityId?.let { activityId ->
        onConfirm(
            activityId,
            state.selectedTagIds.toList(),
            resolvedStartTime.toLocalTime(),
            resolvedEndTime?.toLocalTime(),
            mode,
            state.note.ifBlank { null }
        )
    }
}
```

添加 import：`import com.nltimer.core.data.model.SecondsStrategy`

- [ ] **步骤 2：SheetMainContent 传递 secondsStrategy**

在 `SheetMainContent` 中，将 `ConfirmButtonRow` 调用添加 `secondsStrategy = dialogConfig.secondsStrategy`。

- [ ] **步骤 3：确认编译通过**

运行：`./gradlew :core:behaviorui:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorSheetContent.kt
git commit -m "feat: 确认流程使用 resolveStartTime 保留精确秒数"
```

---

### 任务 8：弹窗配置页面新增秒数策略选择 UI

**文件：**
- 修改：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/DialogConfigScreen.kt`

- [ ] **步骤 1：在"其他"分区添加秒数策略选择**

在 `DialogConfigContent` 中的"其他" Surface 内，在 `PathDrawModeSelector` 之后添加：

```kotlin
Spacer(modifier = Modifier.height(8.dp))
SecondsStrategySelector(
    currentStrategy = config.secondsStrategy,
    onStrategyChange = { onUpdateConfig(config.copy(secondsStrategy = it)) },
)
```

- [ ] **步骤 2：新增 SecondsStrategySelector 组件**

在 `DialogConfigScreen.kt` 文件末尾添加：

```kotlin
@Composable
private fun SecondsStrategySelector(
    currentStrategy: SecondsStrategy,
    onStrategyChange: (SecondsStrategy) -> Unit,
) {
    Text(
        text = "未调整时间时的秒数策略",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SecondsStrategy.entries.forEach { strategy ->
            Surface(
                onClick = { onStrategyChange(strategy) },
                shape = RoundedCornerShape(6.dp),
                color = if (strategy == currentStrategy) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Text(
                    text = when (strategy) {
                        SecondsStrategy.OPEN_TIME -> "打开时"
                        SecondsStrategy.CONFIRM_TIME -> "确认时"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (strategy == currentStrategy) FontWeight.Bold else FontWeight.Normal,
                    ),
                    color = if (strategy == currentStrategy) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}
```

添加 import：`import com.nltimer.core.data.model.SecondsStrategy`

- [ ] **步骤 3：确认编译通过**

运行：`./gradlew :feature:settings:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add feature/settings/src/main/java/com/nltimer/feature/settings/ui/DialogConfigScreen.kt
git commit -m "feat: 弹窗配置新增秒数策略选择 UI"
```

---

### 任务 9：补充测试

**文件：**
- 修改：`feature/settings/src/test/java/com/nltimer/feature/settings/ui/DialogConfigViewModelTest.kt`

- [ ] **步骤 1：在 FakeSettingsPrefs 中验证 secondsStrategy 透传**

现有测试通过 `DialogGridConfig` 整体透传，`secondsStrategy` 已作为其字段。添加测试验证新字段：

```kotlin
@Test
fun `dialogConfig default secondsStrategy is OPEN_TIME`() = runTest {
    advanceUntilIdle()
    val config = viewModel.dialogConfig.value
    assertEquals(SecondsStrategy.OPEN_TIME, config.secondsStrategy)
}

@Test
fun `updateConfig with secondsStrategy CONFIRM_TIME is preserved`() = runTest {
    val newConfig = DialogGridConfig(secondsStrategy = SecondsStrategy.CONFIRM_TIME)
    viewModel.updateConfig(newConfig)
    advanceUntilIdle()
    assertEquals(SecondsStrategy.CONFIRM_TIME, fakeSettingsPrefs.lastDialogConfig?.secondsStrategy)
}
```

添加 import：`import com.nltimer.core.data.model.SecondsStrategy`

- [ ] **步骤 2：运行测试**

运行：`./gradlew :feature:settings:testDebugUnitTest`
预期：所有测试 PASS

- [ ] **步骤 3：Commit**

```bash
git add feature/settings/src/test/java/com/nltimer/feature/settings/ui/DialogConfigViewModelTest.kt
git commit -m "test: 补充 secondsStrategy 配置测试"
```

---

### 任务 10：全量编译验证

- [ ] **步骤 1：运行全量编译**

运行：`./gradlew assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 2：运行全部单元测试**

运行：`./gradlew testDebugUnitTest`
预期：所有测试 PASS

- [ ] **步骤 3：手动验证**

在模拟器上测试以下场景：

1. **当前模式，不拨动滚轮**：16:22:33 进入弹窗 → 确认 → 验证起始时间为 16:22:33
2. **当前模式，拨动滚轮**：16:22:33 进入弹窗 → 拨动分钟到 23 → 确认 → 验证起始时间为 16:23:00
3. **配置切换为 CONFIRM_TIME**：不拨动滚轮 → 确认 → 验证秒数为点击确认那一刻的秒数
4. **点击"重置"/"现在"快捷按钮**：验证秒数归零
