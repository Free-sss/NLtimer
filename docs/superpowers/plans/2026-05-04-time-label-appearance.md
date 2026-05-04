# 时间标题外观配置 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 为首页网格视图的时间悬浮标签添加可配置的显示/隐藏、样式和格式，通过右上角菜单进入设置面板。

**架构：** 新增 `TimeLabelConfig` 数据类及两个枚举，通过 `SettingsPrefs` 单 Key JSON 序列化持久化。`HomeViewModel` 暴露 Flow 和更新方法，`HomeScreen` 右上角菜单触发 `TimeLabelSettingsDialog`，配置通过参数逐层传递至 `TimeFloatingLabel`。

**技术栈：** Kotlin, Jetpack Compose, Material3 SingleChoiceSegmentedButtonRow, DataStore Preferences, kotlinx.serialization

---

## 文件结构

| 操作 | 文件 | 职责 |
|------|------|------|
| 新增 | `core/designsystem/.../TimeLabelConfig.kt` | TimeLabelConfig 数据类 + TimeLabelStyle 枚举 + TimeLabelFormat 枚举 |
| 修改 | `core/data/.../SettingsPrefs.kt` | 接口新增 getTimeLabelConfigFlow / updateTimeLabelConfig |
| 修改 | `core/data/.../SettingsPrefsImpl.kt` | 实现 getTimeLabelConfigFlow / updateTimeLabelConfig（单 Key JSON） |
| 修改 | `feature/home/.../viewmodel/HomeViewModel.kt` | 暴露 timeLabelConfig Flow + updateTimeLabelConfig 方法 |
| 修改 | `feature/home/.../ui/HomeScreen.kt` | 右上角 MoreVert 菜单 + 传递 timeLabelConfig |
| 修改 | `feature/home/.../ui/HomeRoute.kt` | 传递 timeLabelConfig 和回调 |
| 修改 | `feature/home/.../ui/components/TimeAxisGrid.kt` | 接收并传递 timeLabelConfig |
| 修改 | `feature/home/.../ui/components/GridRow.kt` | 接收并传递 timeLabelConfig |
| 修改 | `feature/home/.../ui/components/TimeFloatingLabel.kt` | 根据 config 渲染不同样式和格式 |
| 新增 | `feature/home/.../ui/components/TimeLabelSettingsDialog.kt` | 设置面板 Dialog |

---

### 任务 1：新增 TimeLabelConfig 数据模型

**文件：**
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/TimeLabelConfig.kt`
- 修改：`core/designsystem/build.gradle.kts`（添加 kotlinx-serialization 依赖）

- [ ] **步骤 1：在 core/designsystem/build.gradle.kts 添加 kotlinx-serialization 插件和依赖**

在 `plugins` 块中添加：
```kotlin
id("kotlinx-serialization")
```

在 `dependencies` 块中添加：
```kotlin
implementation(libs.kotlinx.serialization.json)
```

确认 `gradle/libs.versions.toml` 中有 `kotlinx-serialization-json` 条目，若无则添加。

- [ ] **步骤 2：创建 TimeLabelConfig.kt**

```kotlin
package com.nltimer.core.designsystem.theme

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class TimeLabelConfig(
    val visible: Boolean = true,
    val style: TimeLabelStyle = TimeLabelStyle.PILL,
    val format: TimeLabelFormat = TimeLabelFormat.HH_MM,
)

enum class TimeLabelStyle {
    PILL,
    PLAIN,
    UNDERLINE,
    DOT,
}

enum class TimeLabelFormat {
    HH_MM,
    H_MM,
    H_MM_A,
}
```

- [ ] **步骤 3：在 EnumExt.kt 中添加显示名称方法**

在 `core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/EnumExt.kt` 末尾添加：

```kotlin
fun TimeLabelStyle.toDisplayString(): String = when (this) {
    TimeLabelStyle.PILL -> "药丸"
    TimeLabelStyle.PLAIN -> "纯文字"
    TimeLabelStyle.UNDERLINE -> "下划线"
    TimeLabelStyle.DOT -> "圆点"
}

fun TimeLabelFormat.toDisplayString(): String = when (this) {
    TimeLabelFormat.HH_MM -> "09:00"
    TimeLabelFormat.H_MM -> "9:00"
    TimeLabelFormat.H_MM_A -> "9:00 AM"
}
```

- [ ] **步骤 4：运行构建验证**

运行：`.\gradlew :core:designsystem:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 5：Commit**

```bash
git add core/designsystem/
git commit -m "feat: 新增 TimeLabelConfig 数据模型及枚举"
```

---

### 任务 2：扩展 SettingsPrefs 接口与实现

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/SettingsPrefs.kt`
- 修改：`core/data/src/main/java/com/nltimer/core/data/SettingsPrefsImpl.kt`
- 修改：`core/data/build.gradle.kts`（添加 kotlinx-serialization 依赖）

- [ ] **步骤 1：在 core/data/build.gradle.kts 添加 kotlinx-serialization 依赖**

在 `dependencies` 块中添加：
```kotlin
implementation(libs.kotlinx.serialization.json)
```

- [ ] **步骤 2：在 SettingsPrefs 接口中新增方法**

在 `SettingsPrefs.kt` 末尾（`}` 之前）添加：

```kotlin
    fun getTimeLabelConfigFlow(): Flow<TimeLabelConfig>
    suspend fun updateTimeLabelConfig(config: TimeLabelConfig)
```

同时在文件顶部添加 import：
```kotlin
import com.nltimer.core.designsystem.theme.TimeLabelConfig
```

- [ ] **步骤 3：在 SettingsPrefsImpl 中实现新方法**

在 `SettingsPrefsImpl.kt` 中：

添加 import：
```kotlin
import com.nltimer.core.designsystem.theme.TimeLabelConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
```

添加 key 和实现：

```kotlin
private val timeLabelConfigKey = stringPreferencesKey("time_label_config")

private val json = Json { ignoreUnknownKeys = true }

override fun getTimeLabelConfigFlow(): Flow<TimeLabelConfig> = dataStore.data.map { prefs ->
    val jsonString = prefs[timeLabelConfigKey]
    if (jsonString != null) {
        try { json.decodeFromString<TimeLabelConfig>(jsonString) } catch (_: Exception) { TimeLabelConfig() }
    } else TimeLabelConfig()
}

override suspend fun updateTimeLabelConfig(config: TimeLabelConfig) {
    dataStore.edit { prefs ->
        prefs[timeLabelConfigKey] = json.encodeToString(config)
    }
}
```

- [ ] **步骤 4：运行构建验证**

运行：`.\gradlew :core:data:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 5：Commit**

```bash
git add core/data/
git commit -m "feat: SettingsPrefs 新增 TimeLabelConfig 持久化接口与实现"
```

---

### 任务 3：HomeViewModel 暴露 TimeLabelConfig

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeViewModel.kt`

- [ ] **步骤 1：添加 timeLabelConfig Flow 和更新方法**

在 `HomeViewModel` 类中添加：

```kotlin
val timeLabelConfig: StateFlow<TimeLabelConfig> = settingsPrefs.getTimeLabelConfigFlow()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimeLabelConfig())

fun updateTimeLabelConfig(config: TimeLabelConfig) {
    viewModelScope.launch {
        settingsPrefs.updateTimeLabelConfig(config)
    }
}
```

添加 import：
```kotlin
import com.nltimer.core.designsystem.theme.TimeLabelConfig
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
```

- [ ] **步骤 2：运行构建验证**

运行：`.\gradlew :feature:home:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeViewModel.kt
git commit -m "feat: HomeViewModel 暴露 TimeLabelConfig Flow 和更新方法"
```

---

### 任务 4：创建 TimeLabelSettingsDialog 组件

**文件：**
- 创建：`feature/home/src/main/java/com/nltimer/feature/home/ui/components/TimeLabelSettingsDialog.kt`

- [ ] **步骤 1：创建 TimeLabelSettingsDialog.kt**

```kotlin
package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nltimer.core.designsystem.theme.TimeLabelConfig
import com.nltimer.core.designsystem.theme.TimeLabelFormat
import com.nltimer.core.designsystem.theme.TimeLabelStyle
import com.nltimer.core.designsystem.theme.toDisplayString

@Composable
fun TimeLabelSettingsDialog(
    config: TimeLabelConfig,
    onConfigChange: (TimeLabelConfig) -> Unit,
    onDismiss: () -> Unit,
) {
    var localConfig by remember(config) { mutableStateOf(config) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "时间标题外观",
                    style = MaterialTheme.typography.headlineSmall,
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "显示时间标签",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = localConfig.visible,
                        onCheckedChange = { checked ->
                            localConfig = localConfig.copy(visible = checked)
                            onConfigChange(localConfig)
                        },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "样式",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (localConfig.visible) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                )

                Spacer(modifier = Modifier.height(8.dp))

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TimeLabelStyle.entries.forEachIndexed { index, style ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index, TimeLabelStyle.entries.size),
                            onClick = {
                                if (localConfig.visible) {
                                    localConfig = localConfig.copy(style = style)
                                    onConfigChange(localConfig)
                                }
                            },
                            selected = localConfig.style == style,
                            enabled = localConfig.visible,
                        ) {
                            Text(style.toDisplayString())
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "格式",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (localConfig.visible) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                )

                Spacer(modifier = Modifier.height(8.dp))

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TimeLabelFormat.entries.forEachIndexed { index, format ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index, TimeLabelFormat.entries.size),
                            onClick = {
                                if (localConfig.visible) {
                                    localConfig = localConfig.copy(format = format)
                                    onConfigChange(localConfig)
                                }
                            },
                            selected = localConfig.format == format,
                            enabled = localConfig.visible,
                        ) {
                            Text(format.toDisplayString())
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("关闭")
                }
            }
        }
    }
}
```

- [ ] **步骤 2：运行构建验证**

运行：`.\gradlew :feature:home:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/components/TimeLabelSettingsDialog.kt
git commit -m "feat: 新增 TimeLabelSettingsDialog 设置面板组件"
```

---

### 任务 5：改造 TimeFloatingLabel 支持多样式和格式

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/components/TimeFloatingLabel.kt`

- [ ] **步骤 1：重写 TimeFloatingLabel**

将整个文件替换为：

```kotlin
package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.theme.TimeLabelConfig
import com.nltimer.core.designsystem.theme.TimeLabelFormat
import com.nltimer.core.designsystem.theme.TimeLabelStyle
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TimeFloatingLabel(
    time: LocalTime,
    isCurrentRow: Boolean,
    config: TimeLabelConfig = TimeLabelConfig(),
    modifier: Modifier = Modifier,
) {
    val formatter = rememberFormatter(config.format)
    val timeText = time.format(formatter)

    val textColor = if (isCurrentRow) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }

    when (config.style) {
        TimeLabelStyle.PILL -> PillLabel(
            timeText = timeText,
            isCurrentRow = isCurrentRow,
            modifier = modifier,
        )
        TimeLabelStyle.PLAIN -> PlainLabel(
            timeText = timeText,
            textColor = textColor,
            modifier = modifier,
        )
        TimeLabelStyle.UNDERLINE -> UnderlineLabel(
            timeText = timeText,
            textColor = textColor,
            modifier = modifier,
        )
        TimeLabelStyle.DOT -> DotLabel(
            timeText = timeText,
            isCurrentRow = isCurrentRow,
            modifier = modifier,
        )
    }
}

@Composable
private fun PillLabel(
    timeText: String,
    isCurrentRow: Boolean,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isCurrentRow) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }
    val contentColor = if (isCurrentRow) {
        MaterialTheme.colorScheme.onTertiary
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    Text(
        text = timeText,
        color = contentColor,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 2.dp),
    )
}

@Composable
private fun PlainLabel(
    timeText: String,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = timeText,
        color = textColor,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 2.dp),
    )
}

@Composable
private fun UnderlineLabel(
    timeText: String,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Column(modifier = modifier) {
        Text(
            text = timeText,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        )
        HorizontalDivider(
            color = textColor,
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 2.dp),
        )
    }
}

@Composable
private fun DotLabel(
    timeText: String,
    isCurrentRow: Boolean,
    modifier: Modifier = Modifier,
) {
    val dotColor = if (isCurrentRow) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }
    val textColor = if (isCurrentRow) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(dotColor, CircleShape),
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = timeText,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun rememberFormatter(format: TimeLabelFormat): DateTimeFormatter {
    return when (format) {
        TimeLabelFormat.HH_MM -> DateTimeFormatter.ofPattern("HH:mm")
        TimeLabelFormat.H_MM -> DateTimeFormatter.ofPattern("H:mm")
        TimeLabelFormat.H_MM_A -> DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    }
}
```

- [ ] **步骤 2：运行构建验证**

运行：`.\gradlew :feature:home:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/components/TimeFloatingLabel.kt
git commit -m "feat: TimeFloatingLabel 支持多样式和格式配置"
```

---

### 任务 6：GridRow 传递 TimeLabelConfig

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/components/GridRow.kt`

- [ ] **步骤 1：修改 GridRow 函数签名，添加 timeLabelConfig 参数**

在 `GridRow` 函数参数中添加 `timeLabelConfig: TimeLabelConfig = TimeLabelConfig()`，并在 `TimeFloatingLabel` 调用处传递该参数，同时根据 `visible` 控制是否渲染。

将 GridRow 函数签名改为：
```kotlin
fun GridRow(
    row: GridRowUiState,
    onEmptyCellClick: () -> Unit,
    timeLabelConfig: TimeLabelConfig = TimeLabelConfig(),
    modifier: Modifier = Modifier,
)
```

添加 import：
```kotlin
import com.nltimer.core.designsystem.theme.TimeLabelConfig
```

将 `TimeFloatingLabel` 调用从：
```kotlin
if (row.cells.isNotEmpty()) {
    TimeFloatingLabel(
        time = row.startTime,
        isCurrentRow = row.isCurrentRow,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
    )
}
```
改为：
```kotlin
if (row.cells.isNotEmpty() && timeLabelConfig.visible) {
    TimeFloatingLabel(
        time = row.startTime,
        isCurrentRow = row.isCurrentRow,
        config = timeLabelConfig,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
    )
}
```

- [ ] **步骤 2：运行构建验证**

运行：`.\gradlew :feature:home:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/components/GridRow.kt
git commit -m "feat: GridRow 传递 TimeLabelConfig 并控制标签显隐"
```

---

### 任务 7：TimeAxisGrid 传递 TimeLabelConfig

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/components/TimeAxisGrid.kt`

- [ ] **步骤 1：修改 TimeAxisGrid 函数签名，添加 timeLabelConfig 参数**

在 `TimeAxisGrid` 函数参数中添加 `timeLabelConfig: TimeLabelConfig = TimeLabelConfig()`，并在 `GridRow` 调用处传递。

添加 import：
```kotlin
import com.nltimer.core.designsystem.theme.TimeLabelConfig
```

函数签名添加参数：
```kotlin
timeLabelConfig: TimeLabelConfig = TimeLabelConfig(),
```

在 `GridRow` 调用处添加：
```kotlin
timeLabelConfig = timeLabelConfig,
```

- [ ] **步骤 2：运行构建验证**

运行：`.\gradlew :feature:home:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/components/TimeAxisGrid.kt
git commit -m "feat: TimeAxisGrid 传递 TimeLabelConfig"
```

---

### 任务 8：HomeScreen 添加右上角菜单和设置面板

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt`
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeRoute.kt`

- [ ] **步骤 1：在 HomeScreen 函数签名中添加 timeLabelConfig 和回调参数**

在 `HomeScreen` 参数列表末尾添加：
```kotlin
timeLabelConfig: TimeLabelConfig = TimeLabelConfig(),
onUpdateTimeLabelConfig: (TimeLabelConfig) -> Unit = {},
```

添加 import：
```kotlin
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import com.nltimer.core.designsystem.theme.TimeLabelConfig
import com.nltimer.feature.home.ui.components.TimeLabelSettingsDialog
```

- [ ] **步骤 2：在 HomeScreen 中添加右上角菜单和 Dialog 状态**

在 `HomeScreen` 函数体中（`val layout = ...` 之后）添加：
```kotlin
var showOverflowMenu by remember { mutableStateOf(false) }
var showTimeLabelSettings by remember { mutableStateOf(false) }
```

在 `Scaffold` 的 `content` lambda中，`Column` 之前添加右上角菜单。在 `if (uiState.isLoading)` 判断之前，用 `Box` 包裹整个内容区，右上角放置菜单按钮：

将现有 `if (uiState.isLoading) { ... } else { ... }` 部分保持不变，在 `Column` 的顶部（`if (uiState.isLoading)` 之前）添加一个 `Box` 作为顶栏：

```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth()
        .padding(end = 8.dp, top = 4.dp),
    contentAlignment = Alignment.TopEnd,
) {
    IconButton(onClick = { showOverflowMenu = true }) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "菜单",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    DropdownMenu(
        expanded = showOverflowMenu,
        onDismissRequest = { showOverflowMenu = false },
    ) {
        DropdownMenuItem(
            text = { Text("时间标题外观") },
            onClick = {
                showOverflowMenu = false
                showTimeLabelSettings = true
            },
        )
    }
}
```

- [ ] **步骤 3：在 HomeScreen 中添加 TimeLabelSettingsDialog**

在 `HomeScreen` 函数末尾（最后一个 `}` 之前）添加：
```kotlin
if (showTimeLabelSettings) {
    TimeLabelSettingsDialog(
        config = timeLabelConfig,
        onConfigChange = onUpdateTimeLabelConfig,
        onDismiss = { showTimeLabelSettings = false },
    )
}
```

- [ ] **步骤 4：在 TimeAxisGrid 调用处传递 timeLabelConfig**

在 `HomeScreen` 中 `TimeAxisGrid` 调用处添加 `timeLabelConfig = timeLabelConfig` 参数。

- [ ] **步骤 5：修改 HomeRoute 传递 timeLabelConfig 和回调**

在 `HomeRoute.kt` 中：

添加 import：
```kotlin
import com.nltimer.core.designsystem.theme.TimeLabelConfig
```

在 `HomeScreen` 调用处添加两个参数：
```kotlin
timeLabelConfig = viewModel.timeLabelConfig.collectAsState().value,
onUpdateTimeLabelConfig = { viewModel.updateTimeLabelConfig(it) },
```

- [ ] **步骤 6：运行构建验证**

运行：`.\gradlew :feature:home:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 7：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt feature/home/src/main/java/com/nltimer/feature/home/ui/HomeRoute.kt
git commit -m "feat: HomeScreen 右上角菜单添加时间标题外观设置入口"
```

---

### 任务 9：构建验证

- [ ] **步骤 1：运行完整构建**

运行：`.\gradlew assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 2：运行 lint 检查**

运行：`.\gradlew :feature:home:lintDebug`
预期：无严重错误

- [ ] **步骤 3：Commit（如有 lint 自动修复）**

```bash
git add -A
git commit -m "chore: 构建验证通过"
```
