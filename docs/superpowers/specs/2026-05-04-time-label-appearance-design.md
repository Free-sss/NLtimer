# 时间标题外观配置设计

## 概述

为首页网格视图的 `TimeFloatingLabel` 组件添加可配置项，用户可在右上角菜单中打开"时间标题外观"设置面板，控制时间标签的显示/隐藏、样式和格式。

## 数据模型

### TimeLabelConfig

```kotlin
// core:designsystem/TimeLabelConfig.kt
@Serializable
data class TimeLabelConfig(
    val visible: Boolean = true,
    val style: TimeLabelStyle = TimeLabelStyle.PILL,
    val format: TimeLabelFormat = TimeLabelFormat.HH_MM,
)
```

### TimeLabelStyle

```kotlin
enum class TimeLabelStyle {
    PILL,       // 药丸：圆角背景 + padding（当前默认样式）
    PLAIN,      // 纯文字：仅 Text，无背景
    UNDERLINE,  // 下划线：Text + 底部 1dp 横线
    DOT,        // 圆点：小圆点 + Text，紧凑排列
}
```

### TimeLabelFormat

```kotlin
enum class TimeLabelFormat {
    HH_MM,  // 24小时制补零：09:00
    H_MM,   // 24小时制不补零：9:00
    H_MM_A, // 12小时制：9:00 AM
}
```

## 持久化

### 单 Key JSON 序列化

`TimeLabelConfig` 整体序列化为 JSON 字符串，存储在单个 DataStore key 中，确保原子性。

```kotlin
// SettingsPrefsImpl
private val timeLabelConfigKey = stringPreferencesKey("time_label_config")

override fun getTimeLabelConfigFlow(): Flow<TimeLabelConfig> = dataStore.data.map { prefs ->
    val json = prefs[timeLabelConfigKey]
    if (json != null) {
        try { Json.decodeFromString<TimeLabelConfig>(json) } catch (_: Exception) { TimeLabelConfig() }
    } else TimeLabelConfig()
}

override suspend fun updateTimeLabelConfig(config: TimeLabelConfig) {
    dataStore.edit { it[timeLabelConfigKey] = Json.encodeToString(config) }
}
```

### SettingsPrefs 接口扩展

```kotlin
// SettingsPrefs 新增
fun getTimeLabelConfigFlow(): Flow<TimeLabelConfig>
suspend fun updateTimeLabelConfig(config: TimeLabelConfig)
```

## UI 入口

### 右上角菜单

在 `HomeScreen` 右上角添加 `IconButton`（`Icons.Default.MoreVert`），点击弹出 `DropdownMenu`，包含"时间标题外观"菜单项。

### 设置面板

点击"时间标题外观"后弹出 `TimeLabelSettingsDialog`：

- 使用 `Dialog` + `Surface` 构建（非 AlertDialog），`DialogProperties(usePlatformDefaultWidth = false)`
- **显示开关** — 顶部 Switch，带分隔线
- **样式选择** — `SingleChoiceSegmentedButtonRow`（4 段：药丸 / 纯文字 / 下划线 / 圆点）
- **格式选择** — `SingleChoiceSegmentedButtonRow`（3 段：HH:mm / H:mm / h:mm a）

### 可见性联动

当 `visible = false` 时：
- 样式和格式选择区域整体 `enabled = false`
- 视觉上降低 alpha（0.38f），不可操作

## 数据流

```
用户在 Dialog 中选择
  → HomeViewModel.updateTimeLabelConfig(config)
  → settingsPrefs.updateTimeLabelConfig(config)
  → DataStore 写入（单 Key 原子操作）

读取路径：
  DataStore → getTimeLabelConfigFlow()
  → HomeViewModel 中 collect
  → 通过参数传递：HomeScreen → TimeAxisGrid → GridRow → TimeFloatingLabel
```

## TimeFloatingLabel 渲染逻辑

根据 `TimeLabelConfig` 参数渲染：

| 样式 | 渲染方式 |
|------|---------|
| PILL | 圆角背景 + padding（当前样式不变） |
| PLAIN | 仅 Text，color 使用主题色，无背景 |
| UNDERLINE | Text + 底部 1dp Divider，颜色跟随主题 |
| DOT | 小圆点（8dp CircleShape）+ Text，Row 排列 |

格式映射：
- `HH_MM` → `DateTimeFormatter.ofPattern("HH:mm")`
- `H_MM` → `DateTimeFormatter.ofPattern("H:mm")`
- `H_MM_A` → `DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())`

当 `visible = false` 时，`GridRow` 中不渲染 `TimeFloatingLabel`。

## 文件变更清单

| 操作 | 文件 | 模块 |
|------|------|------|
| 新增 | `TimeLabelConfig.kt` | core:designsystem |
| 修改 | `SettingsPrefs.kt` | core:data |
| 修改 | `SettingsPrefsImpl.kt` | core:data |
| 修改 | `HomeViewModel.kt` | feature:home |
| 修改 | `HomeScreen.kt` | feature:home |
| 修改 | `HomeRoute.kt` | feature:home |
| 修改 | `TimeAxisGrid.kt` | feature:home |
| 修改 | `GridRow.kt` | feature:home |
| 修改 | `TimeFloatingLabel.kt` | feature:home |
| 新增 | `TimeLabelSettingsDialog.kt` | feature:home |

## 不做

- 不修改 `Theme` 数据类，`TimeLabelConfig` 独立于主题系统
- 不影响时间轴视图和时间线视图的时间显示
- 不添加动画过渡效果
