# 全量架构优化实现计划 — 业务逻辑函数抽离复用

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:dispatching-parallel-agents 并行实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 对 NLtimer 项目进行全量架构优化，抽离重复业务逻辑到公共 utils/组件层，消除 15+ 处内联重复代码。

**架构：** 按依赖层级从底向上执行——先扩展 core:data 工具层，再扩展 core:designsystem 组件层，最后替换各 feature 模块的内联代码。每步可独立编译验证。

**技术栈：** Kotlin, Jetpack Compose, java.time API, Hilt

---

## 文件结构

### 新建文件
| 文件 | 职责 |
|------|------|
| `core/designsystem/.../component/ColorExt.kt` | Long→Compose Color 扩展函数 |
| `core/designsystem/.../component/PlaceholderScreen.kt` | 通用占位页面组件 |
| `core/designsystem/.../component/TextUtils.kt` | 标签计数等文本工具函数 |

### 修改文件
| 文件 | 变更内容 |
|------|---------|
| `core/data/.../util/TimeFormatUtils.kt` | 添加共享 formatter、epoch 转换、紧凑时长格式化 |
| `core/data/.../model/BehaviorNature.kt` | 添加 displaySymbol 属性 |
| `feature/behavior_management/.../ui/BehaviorTimelineItem.kt` | 替换内联 formatter、颜色解析、状态符号 |
| `feature/behavior_management/.../ui/BehaviorListItem.kt` | 替换内联 formatter、颜色解析、状态符号、formatTimeRange |
| `feature/behavior_management/.../ui/BehaviorManagementScreen.kt` | 替换 epoch 转换、紧凑时长格式化 |
| `feature/behavior_management/.../ui/ImportExportDialog.kt` | 替换内联 formatter |
| `feature/behavior_management/.../viewmodel/BehaviorManagementViewModel.kt` | 使用 TimeConflictUtils、替换 epoch 转换 |
| `feature/home/.../ui/components/BehaviorDetailDialog.kt` | 替换内联 DateTimeFormatter |
| `feature/home/.../ui/components/TimeFloatingLabel.kt` | 替换内联 DateTimeFormatter |
| `feature/home/.../viewmodel/HomeViewModel.kt` | 替换 epoch 转换 |
| `feature/management_activities/.../ui/components/ActivityDetailSheet.kt` | 替换 SimpleDateFormat |
| `feature/settings/.../ui/DataManagementScreen.kt` | 替换 SimpleDateFormat |
| `core/behaviorui/.../sheet/DualTimePickerComponent.kt` | 替换内联 MM/dd formatter |
| `core/behaviorui/.../sheet/AddActivityDialog.kt` | 使用 tagCountLabel |
| `feature/management_activities/.../ui/components/dialogs/ActivityFormSheets.kt` | 使用 tagCountLabel |
| `feature/stats/.../ui/StatsScreen.kt` | 使用 PlaceholderScreen |
| `feature/sub/.../ui/SubScreen.kt` | 使用 PlaceholderScreen |

---

## 任务 1：扩展 TimeFormatUtils.kt — 共享 formatter 和 epoch 转换

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/util/TimeFormatUtils.kt`

- [ ] **步骤 1：扩展 TimeFormatUtils.kt**

在文件末尾添加以下内容：

```kotlin
val hhmmssFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
val mmddFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd")
val yyyyMMddHHmmFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
val exportTimestampFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

fun Long.epochToLocalTime(): LocalTime =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalTime()

fun Long.epochToLocalDateTime(): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()

fun Long.epochToLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

fun LocalTime.toEpochMillisToday(): Long =
    LocalDate.now().atTime(this).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun LocalDate.startOfDayMillis(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun LocalDate.endOfDayMillis(): Long =
    plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun LocalDate.atTimeToEpochMillis(time: LocalTime): Long =
    atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun formatDurationCompact(ms: Long): String {
    val totalMinutes = ms / 60000
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return if (h > 0) "${h}.${m}h" else "${m}m"
}

fun formatDurationCompactHm(ms: Long): String {
    val totalMinutes = ms / 60000
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return if (h > 0) "${h}h${m}m" else "${m}m"
}

fun formatEpochTimeRange(startMs: Long, endMs: Long?): String {
    val start = startMs.epochToLocalTime().format(hhmmFormatter)
    val end = endMs?.let { it.epochToLocalTime().format(hhmmFormatter) }
    return if (end != null) "$start - $end" else start
}

fun formatTimestamp(timestamp: Long): String {
    return java.util.Date(timestamp).let {
        yyyyMMddHHmmFormatter.format(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime())
    }
}

fun formatExportTimestamp(): String =
    exportTimestampFormatter.format(java.time.LocalDateTime.now())
```

同时在文件顶部添加 imports：
```kotlin
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
```

- [ ] **步骤 2：编译验证**

运行：`cd /home/arsucar/Projects/NLtimer/.worktrees/arch-refactor && ./gradlew :core:data:compileDebugKotlin 2>&1 | tail -5`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/util/TimeFormatUtils.kt
git commit -m "refactor: 扩展 TimeFormatUtils 添加共享 formatter 和 epoch 转换函数"
```

---

## 任务 2：BehaviorNature 添加 displaySymbol

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/model/BehaviorNature.kt`

- [ ] **步骤 1：添加 displaySymbol 属性**

在 `BehaviorNature` 枚举中，`companion object` 之前添加：

```kotlin
    val displaySymbol: String
        get() = when (this) {
            COMPLETED -> "✓"
            ACTIVE -> "▶"
            PENDING -> "○"
        }
```

- [ ] **步骤 2：编译验证**

运行：`./gradlew :core:data:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git commit -m "refactor: BehaviorNature 添加 displaySymbol 属性"
```

---

## 任务 3：新建 ColorExt.kt — 颜色解析扩展

**文件：**
- 新建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/ColorExt.kt`

- [ ] **步骤 1：创建 ColorExt.kt**

```kotlin
package com.nltimer.core.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun Long?.toComposeColor(default: Color = MaterialTheme.colorScheme.primary): Color {
    return this?.let { c ->
        android.graphics.Color.valueOf(c).let { cc ->
            Color(cc.red(), cc.green(), cc.blue(), cc.alpha())
        }
    } ?: default
}
```

- [ ] **步骤 2：编译验证**

运行：`./gradlew :core:designsystem:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add core/designsystem/src/main/java/com/nltimer/core/designsystem/component/ColorExt.kt
git commit -m "refactor: 新建 ColorExt.kt 统一颜色解析扩展"
```

---

## 任务 4：新建 TextUtils.kt 和 PlaceholderScreen.kt

**文件：**
- 新建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/TextUtils.kt`
- 新建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/PlaceholderScreen.kt`

- [ ] **步骤 1：创建 TextUtils.kt**

```kotlin
package com.nltimer.core.designsystem.component

fun tagCountLabel(count: Int, emptyLabel: String = "+ 增加"): String =
    if (count == 0) emptyLabel else "$count 个标签"
```

- [ ] **步骤 2：创建 PlaceholderScreen.kt**

```kotlin
package com.nltimer.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun PlaceholderScreen(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
```

- [ ] **步骤 3：编译验证**

运行：`./gradlew :core:designsystem:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add -A && git commit -m "refactor: 新建 TextUtils 和 PlaceholderScreen 公共组件"
```

---

## 任务 5：替换 feature:behavior_management 内联代码

**文件：**
- 修改：`feature/behavior_management/src/main/java/com/nltimer/feature/behavior_management/ui/BehaviorTimelineItem.kt`
- 修改：`feature/behavior_management/src/main/java/com/nltimer/feature/behavior_management/ui/BehaviorListItem.kt`
- 修改：`feature/behavior_management/src/main/java/com/nltimer/feature/behavior_management/ui/BehaviorManagementScreen.kt`
- 修改：`feature/behavior_management/src/main/java/com/nltimer/feature/behavior_management/ui/ImportExportDialog.kt`

- [ ] **步骤 1：修改 BehaviorTimelineItem.kt**

替换 imports — 移除 `java.time.Duration`, `java.time.Instant`, `java.time.ZoneId`, `java.time.format.DateTimeFormatter`，添加：
```kotlin
import com.nltimer.core.data.util.formatDurationCompact
import com.nltimer.core.data.util.epochToLocalTime
import com.nltimer.core.data.util.hhmmFormatter
import com.nltimer.core.designsystem.component.toComposeColor
```

替换颜色解析（42-46行）：
```kotlin
    val dotColor = activity.color.toComposeColor()
```

替换时间格式化（48-54行）：
```kotlin
    val startTime = behavior.startTime.epochToLocalTime().format(hhmmFormatter)
    val endTime = behavior.endTime?.let { it.epochToLocalTime().format(hhmmFormatter) }
```

替换时长格式化（56-64行）：
```kotlin
    val durationText = behavior.endTime?.let { end ->
        formatDurationCompact(end - behavior.startTime)
    }
```

替换状态符号（159-163行）：
```kotlin
                    val statusText = behavior.status.displaySymbol
```

- [ ] **步骤 2：修改 BehaviorListItem.kt**

替换 imports — 移除 `java.time.Instant`, `java.time.ZoneId`, `java.time.format.DateTimeFormatter`，添加：
```kotlin
import com.nltimer.core.data.util.formatEpochTimeRange
import com.nltimer.core.designsystem.component.toComposeColor
```

替换颜色解析（73-77行）：
```kotlin
        val dotColor = activity.color.toComposeColor()
```

替换状态符号（113-117行）：
```kotlin
                val statusText = behavior.status.displaySymbol
```

替换时间范围调用（140行）：
```kotlin
        val timeText = formatEpochTimeRange(behavior.startTime, behavior.endTime)
```

删除整个 `formatTimeRange` 私有函数（150-163行）。

- [ ] **步骤 3：修改 BehaviorManagementScreen.kt**

替换 imports — 移除 `java.time.Instant`, `java.time.ZoneId`，添加：
```kotlin
import com.nltimer.core.data.util.epochToLocalTime
import com.nltimer.core.data.util.formatDurationCompactHm
```

替换 epoch 转换（212-215行）：
```kotlin
        val initialStartTime = bwd.behavior.startTime.epochToLocalTime()
        val initialEndTime = bwd.behavior.endTime?.let { it.epochToLocalTime() }
```

替换 SummaryBar 中的时长格式化（265-267行）：
```kotlin
    val durationText = formatDurationCompactHm(totalDurationMinutes * 60_000)
```

- [ ] **步骤 4：修改 ImportExportDialog.kt**

替换 imports — 移除 `java.time.Instant`, `java.time.ZoneId`, `java.time.format.DateTimeFormatter`，添加：
```kotlin
import com.nltimer.core.data.util.formatEpochTimeRange
```

替换 formatDuplicateItem 函数（251-265行）为：
```kotlin
private fun formatDuplicateItem(item: ImportPreviewItem): String {
    val timeRange = formatEpochTimeRange(item.startTime, item.endTime).replace(" - ", "-")
    return "${item.activityName} $timeRange"
}
```

- [ ] **步骤 5：编译验证**

运行：`./gradlew :feature:behavior_management:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 6：Commit**

```bash
git add -A && git commit -m "refactor: behavior_management 替换内联时间/颜色/状态逻辑为公共工具"
```

---

## 任务 6：替换 BehaviorManagementViewModel 内的重复逻辑

**文件：**
- 修改：`feature/behavior_management/src/main/java/com/nltimer/feature/behavior_management/viewmodel/BehaviorManagementViewModel.kt`

- [ ] **步骤 1：替换 imports 和 epoch 转换**

添加 imports：
```kotlin
import com.nltimer.core.data.util.atTimeToEpochMillis
import com.nltimer.core.data.util.startOfDayMillis
```

替换 observeBehaviors 中的 epoch 计算（75-79行）：
```kotlin
                    val startEpoch = date.startOfDayMillis()
```

替换 updateBehavior 中的 epoch 计算（247-251行）：
```kotlin
            val today = LocalDate.now()
            val startEpoch = today.atTimeToEpochMillis(startTime)
            val endEpoch = endTime?.let { today.atTimeToEpochMillis(it) }
```

替换 timeOverlaps（321-328行）——删除整个函数，改为使用 hasTimeConflict。但在 executeImport 中调用 timeOverlaps 的地方需要替换。注意 timeOverlaps 的签名和 hasTimeConflict 不同，需要适配。将 executeImport 中的调用替换为直接内联的区间重叠判断（因为 hasTimeConflict 需要完整的 Behavior 列表参数，而这里只做两两比较）：

保持 timeOverlaps 为简单的区间重叠检测函数不变（它比 hasTimeConflict 更轻量，场景不同），但更新为使用更清晰的命名。实际上保持原样即可。

- [ ] **步骤 2：编译验证**

运行：`./gradlew :feature:behavior_management:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git commit -m "refactor: BehaviorManagementViewModel 使用公共 epoch 转换函数"
```

---

## 任务 7：替换 feature:home 内联代码

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/components/BehaviorDetailDialog.kt`
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/components/TimeFloatingLabel.kt`
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeViewModel.kt`

- [ ] **步骤 1：修改 BehaviorDetailDialog.kt**

替换 imports — 移除 `java.time.format.DateTimeFormatter`，添加：
```kotlin
import com.nltimer.core.data.util.hhmmssFormatter
import com.nltimer.core.data.util.yyyyMMddHHmmFormatter
```

替换 epochToMsString 中的 formatter（36行）：
```kotlin
        return instant.format(yyyyMMddHHmmFormatter)
```

替换所有 `DateTimeFormatter.ofPattern("HH:mm:ss")` 为 `hhmmssFormatter`（50, 52, 86, 88行）。

- [ ] **步骤 2：修改 TimeFloatingLabel.kt**

替换 imports — 移除 `java.time.format.DateTimeFormatter`，添加：
```kotlin
import com.nltimer.core.data.util.hhmmFormatter
```

替换 formatTime 函数（174-181行）：
```kotlin
private fun formatTime(time: LocalTime, format: TimeLabelFormat): String {
    val formatter = when (format) {
        TimeLabelFormat.HH_MM -> hhmmFormatter
        TimeLabelFormat.H_MM -> DateTimeFormatter.ofPattern("H:mm")
        TimeLabelFormat.H_MM_A -> DateTimeFormatter.ofPattern("h:mm a")
    }
    return time.format(formatter)
}
```

需要在文件中保留 `import java.time.format.DateTimeFormatter` 因为 H_MM 和 H_MM_A 仍需要它。

- [ ] **步骤 3：修改 HomeViewModel.kt**

替换 imports — 添加：
```kotlin
import com.nltimer.core.data.util.startOfDayMillis
import com.nltimer.core.data.util.endOfDayMillis
```

替换 loadHomeBehaviors 中的 epoch 计算（127-128行）：
```kotlin
            val dayStart = today.startOfDayMillis()
            val dayEnd = today.endOfDayMillis()
```

- [ ] **步骤 4：编译验证**

运行：`./gradlew :feature:home:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 5：Commit**

```bash
git add -A && git commit -m "refactor: home 替换内联时间格式化和 epoch 转换"
```

---

## 任务 8：替换 feature:management_activities 和 feature:settings

**文件：**
- 修改：`feature/management_activities/src/main/java/com/nltimer/feature/management_activities/ui/components/ActivityDetailSheet.kt`
- 修改：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/DataManagementScreen.kt`

- [ ] **步骤 1：修改 ActivityDetailSheet.kt**

替换 imports — 移除 `java.text.SimpleDateFormat`, `java.util.Date`, `java.util.Locale`，添加：
```kotlin
import com.nltimer.core.data.util.formatTimestamp
```

替换 formatTimestamp 函数（173-177行）为：
```kotlin
private fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null || timestamp == 0L) return "从未使用"
    return formatTimestamp(timestamp)
}
```

注意：局部函数名和导入的顶层函数名冲突。需要重命名局部函数或直接内联。最佳做法是删除整个局部 formatTimestamp 函数，改为在调用处直接使用：

在调用处（140行）改为：
```kotlin
                    StatRow("最近一次使用", if (stats.lastUsedTimestamp == null || stats.lastUsedTimestamp == 0L) "从未使用" else formatTimestamp(stats.lastUsedTimestamp))
```

并删除私有的 formatTimestamp 函数。

- [ ] **步骤 2：修改 DataManagementScreen.kt**

替换（87-88行）的 SimpleDateFormat 调用：
```kotlin
            val timestamp = com.nltimer.core.data.util.formatExportTimestamp()
```

- [ ] **步骤 3：编译验证**

运行：`./gradlew :feature:management_activities:compileDebugKotlin :feature:settings:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add -A && git commit -m "refactor: management_activities/settings 替换 SimpleDateFormat 为公共工具"
```

---

## 任务 9：替换 core:behaviorui 内联代码

**文件：**
- 修改：`core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/DualTimePickerComponent.kt`
- 修改：`core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddActivityDialog.kt`

- [ ] **步骤 1：修改 DualTimePickerComponent.kt**

替换 imports — 移除 `java.time.format.DateTimeFormatter`，添加：
```kotlin
import com.nltimer.core.data.util.mmddFormatter
```

替换两处 `DateTimeFormatter.ofPattern("MM/dd")`（61行和225行）为 `mmddFormatter`。

- [ ] **步骤 2：修改 AddActivityDialog.kt**

添加 import：
```kotlin
import com.nltimer.core.designsystem.component.tagCountLabel
```

替换（32行）：
```kotlin
    val tagCountText = tagCountLabel(selectedTagIds.size)
```

- [ ] **步骤 3：编译验证**

运行：`./gradlew :core:behaviorui:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add -A && git commit -m "refactor: behaviorui 替换内联 formatter 和标签计数逻辑"
```

---

## 任务 10：替换 feature:management_activities 中的 tagCountLabel

**文件：**
- 修改：`feature/management_activities/src/main/java/com/nltimer/feature/management_activities/ui/components/dialogs/ActivityFormSheets.kt`

- [ ] **步骤 1：修改 ActivityFormSheets.kt**

添加 import：
```kotlin
import com.nltimer.core.designsystem.component.tagCountLabel
```

替换两处 tagCountText（40行和115行）：
```kotlin
    val tagCountText = tagCountLabel(selectedTagIds.size)
```

- [ ] **步骤 2：编译验证**

运行：`./gradlew :feature:management_activities:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add -A && git commit -m "refactor: ActivityFormSheets 使用公共 tagCountLabel"
```

---

## 任务 11：替换 feature:stats 和 feature:sub 为 PlaceholderScreen

**文件：**
- 修改：`feature/stats/src/main/java/com/nltimer/feature/stats/ui/StatsScreen.kt`
- 修改：`feature/sub/src/main/java/com/nltimer/feature/sub/ui/SubScreen.kt`

- [ ] **步骤 1：修改 StatsScreen.kt**

替换整个文件内容为：
```kotlin
package com.nltimer.feature.stats.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.runtime.Composable
import com.nltimer.core.designsystem.component.PlaceholderScreen

@Composable
fun StatsRoute() {
    StatsScreen()
}

@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        icon = Icons.Default.BarChart,
        title = "统计",
        modifier = modifier,
    )
}
```

需要添加 `import androidx.compose.ui.Modifier`。

- [ ] **步骤 2：修改 SubScreen.kt**

替换整个文件内容为：
```kotlin
package com.nltimer.feature.sub.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nltimer.core.designsystem.component.PlaceholderScreen

@Composable
fun SubRoute() {
    SubScreen()
}

@Composable
fun SubScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        icon = Icons.Default.Apps,
        title = "副页",
        modifier = modifier,
    )
}
```

- [ ] **步骤 3：编译验证**

运行：`./gradlew :feature:stats:compileDebugKotlin :feature:sub:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add -A && git commit -m "refactor: stats/sub 使用公共 PlaceholderScreen 组件"
```

---

## 任务 12：全量编译和测试验证

- [ ] **步骤 1：全量编译**

运行：`./gradlew assembleDebug 2>&1 | tail -20`
预期：BUILD SUCCESSFUL

- [ ] **步骤 2：运行现有测试**

运行：`./gradlew test 2>&1 | tail -20`
预期：所有测试通过

- [ ] **步骤 3：最终 Commit（如有 lint 修复）**

```bash
git add -A && git commit -m "chore: 修复 lint 问题" || true
```
