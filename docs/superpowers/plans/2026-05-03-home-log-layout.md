# 主页日志模式实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 在 NLtimer 主页新增第三种布局模式"行为日志"，以卡片列表形式详细展示所有行为记录字段。

**架构：** 在 `HomeLayout` 枚举新增 `LOG` 值，新增 `BehaviorLogView.kt` 组件复用现有 `GridCellUiState` 数据，在 `HomeScreen.kt` 中增加第三分支渲染。

**技术栈：** Jetpack Compose, Material Design 3, Kotlin

---

## 文件结构

| 文件 | 职责 |
|------|------|
| `core/designsystem/theme/HomeLayout.kt` | 枚举新增 `LOG` 值 |
| `core/designsystem/theme/EnumExt.kt` | `HomeLayout.toDisplayString()` 新增 LOG 分支 |
| `feature/home/model/GridCellUiState.kt` | 新增 `pomodoroCount` 字段 |
| `feature/home/viewmodel/HomeViewModel.kt` | `buildUiState()` 中补充 `pomodoroCount` 透传 |
| `feature/home/ui/components/BehaviorLogView.kt` | 新增日志模式主组件（卡片列表） |
| `feature/home/ui/HomeScreen.kt` | `when(layout)` 新增 `HomeLayout.LOG` 分支 |

---

### 任务 1：扩展 HomeLayout 枚举

**文件：**
- 修改：`core/designsystem/theme/HomeLayout.kt`

- [ ] **步骤 1：新增 LOG 枚举值**

```kotlin
package com.nltimer.core.designsystem.theme

/**
 * 首页布局模式枚举
 * - GRID: 网格时间展示
 * - TIMELINE_REVERSE: 反向时间轴展示
 * - LOG: 行为日志列表展示
 */
enum class HomeLayout {
    GRID,
    TIMELINE_REVERSE,
    LOG,
}
```

- [ ] **步骤 2：验证编译**

运行：`./gradlew.bat :core:designsystem:compileDebugKotlin`
预期：编译成功，无错误

---

### 任务 2：更新 toDisplayString() 扩展

**文件：**
- 修改：`core/designsystem/theme/EnumExt.kt`

- [ ] **步骤 1：新增 LOG 分支**

在 `HomeLayout.toDisplayString()` 函数中新增：

```kotlin
fun HomeLayout.toDisplayString(): String = when (this) {
    HomeLayout.GRID -> "网格时间"
    HomeLayout.TIMELINE_REVERSE -> "时间轴(反)"
    HomeLayout.LOG -> "行为日志"
}
```

- [ ] **步骤 2：验证编译**

运行：`./gradlew.bat :core:designsystem:compileDebugKotlin`
预期：编译成功

---

### 任务 3：GridCellUiState 新增 pomodoroCount 字段

**文件：**
- 修改：`feature/home/model/GridCellUiState.kt`

- [ ] **步骤 1：新增字段**

```kotlin
package com.nltimer.feature.home.model

import androidx.compose.runtime.Immutable
import com.nltimer.core.data.model.BehaviorNature
import java.time.LocalTime

@Immutable
data class GridCellUiState(
    val behaviorId: Long?,
    val activityEmoji: String?,
    val activityName: String?,
    val tags: List<TagUiState>,
    val status: BehaviorNature?,
    val isCurrent: Boolean,
    val wasPlanned: Boolean = false,
    val achievementLevel: Int? = null,
    val estimatedDuration: Long? = null,
    val actualDuration: Long? = null,
    val durationMs: Long? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val isAddPlaceholder: Boolean = false,
    val note: String? = null,
    val pomodoroCount: Int = 0, // 新增
)
```

- [ ] **步骤 2：验证编译**

运行：`./gradlew.bat :feature:home:compileDebugKotlin`
预期：编译成功（此时可能因 HomeViewModel 未更新而有警告，下一步修复）

---

### 任务 4：HomeViewModel 补充 pomodoroCount 透传

**文件：**
- 修改：`feature/home/viewmodel/HomeViewModel.kt`

- [ ] **步骤 1：在 buildUiState 的 cells map 中补充 pomodoroCount**

找到 `GridCellUiState(...)` 的构造调用（约第 170 行附近），在 `note = behavior.note` 下方新增：

```kotlin
            GridCellUiState(
                behaviorId = behavior.id,
                activityEmoji = activity?.emoji,
                activityName = activity?.name,
                tags = tags.map { TagUiState(id = it.id, name = it.name, color = it.color, isActive = !it.isArchived) },
                status = behavior.status,
                isCurrent = isActive,
                wasPlanned = behavior.wasPlanned,
                achievementLevel = behavior.achievementLevel,
                estimatedDuration = behavior.estimatedDuration,
                actualDuration = behavior.actualDuration,
                durationMs = if (isActive && behavior.startTime > 0) {
                    System.currentTimeMillis() - behavior.startTime
                } else null,
                startTime = startLocal,
                endTime = endLocal,
                note = behavior.note,
                pomodoroCount = behavior.pomodoroCount, // 新增
            )
```

- [ ] **步骤 2：验证编译**

运行：`./gradlew.bat :feature:home:compileDebugKotlin`
预期：编译成功

---

### 任务 5：创建 BehaviorLogView 组件

**文件：**
- 创建：`feature/home/ui/components/BehaviorLogView.kt`

- [ ] **步骤 1：创建完整组件文件**

```kotlin
package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.theme.HomeLayout
import com.nltimer.core.designsystem.theme.appBorder
import com.nltimer.core.designsystem.theme.toDisplayString
import com.nltimer.feature.home.model.GridCellUiState
import java.time.format.DateTimeFormatter

@Composable
fun BehaviorLogView(
    cells: List<GridCellUiState>,
    onLayoutChange: (HomeLayout) -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    var showLayoutMenu by remember { mutableStateOf(false) }

    val behaviors = remember(cells) {
        cells.filter { it.behaviorId != null && it.startTime != null }
            .sortedByDescending { it.startTime }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showLayoutMenu = true }
                    ) {
                        Text(
                            text = "行为日志",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showLayoutMenu,
                        onDismissRequest = { showLayoutMenu = false }
                    ) {
                        HomeLayout.entries.forEach { layout ->
                            DropdownMenuItem(
                                text = { Text(layout.toDisplayString()) },
                                onClick = {
                                    onLayoutChange(layout)
                                    showLayoutMenu = false
                                }
                            )
                        }
                    }
                }
            }

            if (behaviors.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无行为记录",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(items = behaviors, key = { it.behaviorId!! }) { behavior ->
                    BehaviorLogCard(
                        behavior = behavior,
                        timeFormatter = timeFormatter
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BehaviorLogCard(
    behavior: GridCellUiState,
    timeFormatter: DateTimeFormatter,
) {
    val cardBackground = if (behavior.isCurrent) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBackground)
            .appBorder(
                borderProducer = {
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                },
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        // 头部：活动名称 + 状态标签
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${behavior.activityEmoji ?: "❓"} ${behavior.activityName ?: "未知"}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            behavior.status?.let { status ->
                val (bgColor, textColor) = when (status) {
                    com.nltimer.core.data.model.BehaviorNature.ACTIVE ->
                        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                    com.nltimer.core.data.model.BehaviorNature.COMPLETED ->
                        MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
                    com.nltimer.core.data.model.BehaviorNature.PENDING ->
                        MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgColor)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = status.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 时间信息行
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val startText = behavior.startTime?.format(timeFormatter) ?: "--:--"
            val endText = behavior.endTime?.format(timeFormatter) ?: "进行中"
            Text(
                text = "起始: $startText → 结束: $endText",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val duration = behavior.durationMs
                ?: ((behavior.actualDuration ?: 0L) * 1000)
            if (duration > 0) {
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "用时: ${formatDuration(duration)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 标签区
        if (behavior.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                behavior.tags.forEach { tag ->
                    TagChipSmall(tag.name)
                }
            }
        }

        // 备注（可选）
        behavior.note?.let { note ->
            if (note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "备注: $note",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 详细字段区
        Spacer(modifier = Modifier.height(8.dp))
        val details = buildList {
            if (behavior.pomodoroCount > 0) add("番茄钟: ${behavior.pomodoroCount}")
            behavior.estimatedDuration?.let { add("预估: ${formatDuration(it)}") }
            behavior.actualDuration?.let { add("实际: ${formatDuration(it * 1000)}") }
            behavior.achievementLevel?.let { add("完成度: $it") }
            add("计划内: ${if (behavior.wasPlanned) "是" else "否"}")
        }
        if (details.isNotEmpty()) {
            Text(
                text = details.joinToString("    "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }

        // 开发者字段区
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "behaviorId: ${behavior.behaviorId ?: "-"} | activityId: ${behavior.activityName?.let { "-" } ?: "-"}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun TagChipSmall(name: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = "#$name",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

private fun formatDuration(ms: Long): String {
    val hours = ms / 3600000
    val minutes = (ms % 3600000) / 60000
    val seconds = (ms % 60000) / 1000
    return buildString {
        if (hours > 0) append("${hours}h")
        if (minutes > 0 || hours > 0) append("${minutes}m")
        if (hours == 0L && minutes == 0L) append("${seconds}s")
    }
}
```

- [ ] **步骤 2：验证编译**

运行：`./gradlew.bat :feature:home:compileDebugKotlin`
预期：编译成功

---

### 任务 6：HomeScreen 新增 LOG 分支

**文件：**
- 修改：`feature/home/ui/HomeScreen.kt`

- [ ] **步骤 1：导入 BehaviorLogView**

在文件顶部 import 区域新增：

```kotlin
import com.nltimer.feature.home.ui.components.BehaviorLogView
```

- [ ] **步骤 2：在 when(layout) 中新增 LOG 分支**

找到 `when (layout)` 代码块（约第 95 行附近），在 `TIMELINE_REVERSE` 分支之后新增：

```kotlin
                HomeLayout.LOG -> {
                    BehaviorLogView(
                        cells = uiState.rows.flatMap { it.cells },
                        onLayoutChange = onLayoutChange,
                        modifier = Modifier.weight(1f)
                    )
                }
```

完整 `when` 块应如下所示：

```kotlin
                when (layout) {
                    HomeLayout.GRID -> {
                        Row(modifier = Modifier.weight(1f)) {
                            TimeAxisGrid(
                                rows = uiState.rows,
                                onEmptyCellClick = onEmptyCellClick,
                                currentHour = uiState.selectedTimeHour,
                                onLayoutChange = onLayoutChange,
                                modifier = Modifier.weight(1f),
                            )
                            TimeSideBar(
                                activeHours = uiState.rows
                                    .filter { it.cells.any { cell -> cell.behaviorId != null } || it.isCurrentRow }
                                    .map { it.startTime.hour }
                                    .toSet(),
                                currentHour = uiState.selectedTimeHour,
                                onHourClick = onHourClick,
                            )
                        }
                    }
                    HomeLayout.TIMELINE_REVERSE -> {
                        TimelineReverseView(
                            cells = uiState.rows.flatMap { it.cells },
                            onAddClick = onEmptyCellClick,
                            onLayoutChange = onLayoutChange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    HomeLayout.LOG -> {
                        BehaviorLogView(
                            cells = uiState.rows.flatMap { it.cells },
                            onLayoutChange = onLayoutChange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
```

- [ ] **步骤 3：验证编译**

运行：`./gradlew.bat :app:assembleDebug`
预期：编译成功，APK 生成

---

### 任务 7：运行并验证

**文件：**
- 不涉及文件修改

- [ ] **步骤 1：构建并安装 APK**

运行：
```powershell
.\gradlew.bat :app:assembleDebug; if ($LASTEXITCODE -eq 0) { adb -s ebc3de22 install -r "D:\2026Code\Group_android\NLtimer\app\build\outputs\apk\debug\app-debug.apk"; adb -s ebc3de22 shell am start -n "com.nltimer.app/com.nltimer.feature.debug.ui.DebugActivity" }
```

- [ ] **步骤 2：手动验证**

1. 打开应用，进入主页
2. 点击顶部布局切换菜单（"网格时间"或"时间轴(反)"旁的下拉箭头）
3. 确认菜单中出现"行为日志"选项
4. 点击"行为日志"切换到日志模式
5. 确认行为记录以卡片形式展示，包含：活动名称、状态标签、起止时间、用时、标签、备注、番茄钟、预估/实际时长、完成度、计划内、behaviorId
6. 当天无记录时确认显示"暂无行为记录"
7. 从日志模式切换回其他模式，确认正常

---

## 自检

**规格覆盖度：**
- [x] HomeLayout 枚举新增 LOG — 任务 1
- [x] toDisplayString() 扩展 — 任务 2
- [x] GridCellUiState 新增 pomodoroCount — 任务 3
- [x] HomeViewModel 透传 pomodoroCount — 任务 4
- [x] BehaviorLogView 组件创建 — 任务 5
- [x] HomeScreen 新增分支 — 任务 6
- [x] 运行验证 — 任务 7

**占位符扫描：** 无 TODO、无待定、所有步骤包含完整代码

**类型一致性：**
- `pomodoroCount` 在 GridCellUiState、HomeViewModel、BehaviorLogView 中均为 `Int`
- `HomeLayout.LOG` 在枚举、扩展函数、when 分支、BehaviorLogView 参数中一致
