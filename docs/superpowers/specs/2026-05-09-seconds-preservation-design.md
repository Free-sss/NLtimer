# 计时起始秒数保留设计

## 问题

当前模式（AddSheetMode.CURRENT / BehaviorNature.ACTIVE）下，用户打开弹窗开始计时时，秒数被强制归零。例如 16:22:33 进入弹窗，计时开始时间显示为 16:22:00，丢弃了 33 秒。

根因：`AddBehaviorState` 构造函数中 `now = LocalDateTime.now().withSecond(0).withNano(0)`，以及 `DualTimePickerComponent` 中 `startTime.withSecond(0).withNano(0)` 双重截断。

## 需求

1. 如果用户**未拨动时间滚轮**，保留秒数（精确起始时间）
2. 如果用户**拨动了滚轮**，秒数归零（用户对分钟有明确意图）
3. 在设置→弹窗配置中添加选项，让用户选择未调整时间时秒数来源：
   - 打开弹窗时的秒数
   - 点击确认时的秒数

## 设计

### 1. 数据层：新增 SecondsStrategy 枚举与设置项

**新增文件**：`core/data/src/main/java/com/nltimer/core/data/model/SecondsStrategy.kt`

```kotlin
enum class SecondsStrategy(val key: String) {
    OPEN_TIME("open_time"),
    CONFIRM_TIME("confirm_time")
}
```

**修改文件**：`core/data/src/main/java/com/nltimer/core/data/SettingsPrefs.kt`

接口新增：
```kotlin
val secondsStrategy: Flow<SecondsStrategy>
suspend fun setSecondsStrategy(strategy: SecondsStrategy)
```

**修改文件**：`core/data/src/main/java/com/nltimer/core/data/SettingsPrefsImpl.kt`

- 新增 DataStore key `seconds_strategy`，默认值 `OPEN_TIME`
- 实现 `secondsStrategy` Flow 和 `setSecondsStrategy()` 方法

### 2. 状态层：AddBehaviorState 改造

**修改文件**：`core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorState.kt`

改造点：

- `now` 移除 `.withSecond(0).withNano(0)`，保留完整精度
- 新增 `val sheetOpenTime: LocalDateTime = LocalDateTime.now()` 记录弹窗打开时刻
- 新增 `var userAdjustedTime: Boolean by mutableStateOf(false)` 标记是否拨动过滚轮
- `startTime` 初始化保留秒数
- 新增方法：

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

- 滚轮 `onTimeChanged` 回调中设置 `userAdjustedTime = true`

### 3. UI 层：DualTimePickerComponent 同步改造

**修改文件**：`core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/DualTimePickerComponent.kt`

- `startProperty` / `endProperty` 不再强制 `.withSecond(0).withNano(0)`
- 滚轮 UI 仍然只显示时/分列，秒数仅用于确认时注入

### 4. 确认流程改造

**修改文件**：`core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorSheetContent.kt`

确认按钮回调改造：
1. 获取 `LocalDateTime.now()` 作为 confirmTime
2. 读取 secondsStrategy 设置
3. 调用 `state.resolveStartTime(strategy, confirmTime)` 得到精确时间
4. 传给 `onConfirm()`

**修改文件**：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeRoute.kt`

`startTime.toLocalTime()` 现在携带秒数，无需额外改动。

### 5. 设置页面：新增配置项

**修改文件**：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/DialogConfigScreen.kt`

在"其他"分区新增：
- 标题："未调整时间时的秒数策略"
- 两个单选项：
  - 使用打开弹窗时的秒数（默认）
  - 使用点击确认时的秒数

**修改文件**：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/DialogConfigViewModel.kt`

新增读取/写入 `secondsStrategy`。

**修改文件**：`feature/settings/src/test/java/com/nltimer/feature/settings/ui/DialogConfigViewModelTest.kt`

补充秒数策略相关测试。

### 6. TimeAdjustmentComponent 改造

**修改文件**：`core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/TimeAdjustmentComponent.kt`

"重置"和"现在"按钮触发时：
- 设置 `userAdjustedTime = true`（用户主动调整了时间）
- 时间仍走 `.withSecond(0).withNano(0)`（手动选时间，秒归零）

## 行为矩阵

| 场景 | userAdjustedTime | 秒数来源 |
|------|------------------|----------|
| 未拨动滚轮，策略=OPEN_TIME | false | sheetOpenTime 的秒数 |
| 未拨动滚轮，策略=CONFIRM_TIME | false | 确认按钮点击时刻的秒数 |
| 拨动过滚轮 | true | 归零（00） |
| 点击"重置"/"现在"快捷按钮 | true | 归零（00） |

## 涉及文件清单

| 文件 | 改动类型 |
|------|----------|
| `core/data/.../model/SecondsStrategy.kt` | 新增 |
| `core/data/.../SettingsPrefs.kt` | 修改（接口） |
| `core/data/.../SettingsPrefsImpl.kt` | 修改（实现+key） |
| `core/behaviorui/.../sheet/AddBehaviorState.kt` | 修改（核心逻辑） |
| `core/behaviorui/.../sheet/DualTimePickerComponent.kt` | 修改（移除截断） |
| `core/behaviorui/.../sheet/AddBehaviorSheetContent.kt` | 修改（确认流程） |
| `core/behaviorui/.../sheet/TimeAdjustmentComponent.kt` | 修改（标记调整） |
| `feature/home/.../ui/HomeRoute.kt` | 可能微调 |
| `feature/settings/.../ui/DialogConfigScreen.kt` | 修改（新增选项） |
| `feature/settings/.../ui/DialogConfigViewModel.kt` | 修改（新增字段） |
| `feature/settings/.../ui/DialogConfigViewModelTest.kt` | 修改（补充测试） |
