# 当前时刻布局模式实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 在主页新增第四种布局模式「当前时刻」（MOMENT），上方为 Reef 风格聚焦卡片（三态动态切换 + 滑动交互），下方为今日全部行为列表。

**架构：** HomeLayout 枚举新增 MOMENT 值，3 个新 UI 组件文件（SlideActionPill、MomentFocusCard、MomentView），HomeScreen when 分支新增 MOMENT case。全部复用现有数据流，无新增 ViewModel/Repository 方法。

**技术栈：** Jetpack Compose, Material Design 3, Kotlin Coroutines

---

## 文件清单

| 文件 | 操作 | 职责 |
|------|------|------|
| `core/designsystem/.../theme/HomeLayout.kt` | 修改 | 新增 MOMENT 枚举值 |
| `core/designsystem/.../theme/EnumExt.kt` | 修改 | 新增 MOMENT.toDisplayString() |
| `feature/home/.../ui/components/SlideActionPill.kt` | 新建 | 滑动操作拉条组件 |
| `feature/home/.../ui/components/MomentFocusCard.kt` | 新建 | 聚焦卡片组件（三态） |
| `feature/home/.../ui/components/MomentView.kt` | 新建 | 主布局：卡片 + 行为列表 |
| `feature/home/.../ui/HomeScreen.kt` | 修改 | when(layout) 新增 MOMENT 分支 |

---

## 任务 1：HomeLayout 枚举扩展

**文件：**
- 修改：`core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/HomeLayout.kt`
- 修改：`core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/EnumExt.kt`

- [ ] **步骤 1：修改 HomeLayout.kt 添加 MOMENT 值**

在 `HomeLayout.kt` 中，将枚举改为：

```kotlin
enum class HomeLayout {
    GRID,
    TIMELINE_REVERSE,
    LOG,
    MOMENT,
}
```

- [ ] **步骤 2：修改 EnumExt.kt 添加 toDisplayString 映射**

在 `EnumExt.kt` 的 `HomeLayout.toDisplayString()` when 表达式中新增：

```kotlin
HomeLayout.MOMENT -> "当前时刻"
```

- [ ] **步骤 3：Commit**

```
feat(home): HomeLayout 新增 MOMENT 枚举值
```

---

## 任务 2：创建 SlideActionPill 组件

**文件：**
- 创建：`feature/home/src/main/java/com/nltimer/feature/home/ui/components/SlideActionPill.kt`

- [ ] **步骤 1：创建 SlideActionPill.kt**

借鉴 Reef 的 FocusTogglePill，适配 NLtimer 场景。完整代码如下：

```kotlin
package com.nltimer.feature.home.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun SlideActionPill(
    onActivate: () -> Unit,
    modifier: Modifier = Modifier,
    onSlideProgress: (Float) -> Unit = {},
    activeLabel: String = "滑动",
    activatedLabel: String = "释放",
    leadingIcon: ImageVector = Icons.Filled.Check,
    activatedIcon: ImageVector = Icons.Filled.Check,
    pillWidth: androidx.compose.ui.unit.Dp = 200.dp,
) {
    val thumbSize = 60.dp
    val padding = 6.dp
    val maxOffset = with(LocalDensity.current) { (pillWidth - thumbSize - padding * 2).toPx() }

    var offsetXState by remember { mutableFloatStateOf(0f) }
    var isDraggingState by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue = if (isDraggingState) offsetXState else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "thumb_offset"
    )

    val progress = (animatedOffset / maxOffset).coerceIn(0f, 1f)

    LaunchedEffect(progress) {
        onSlideProgress(progress)
    }

    Surface(
        modifier = modifier
            .width(pillWidth)
            .height(72.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { isDraggingState = true },
                    onDragEnd = {
                        if (offsetXState >= maxOffset * 0.7f) {
                            onActivate()
                        }
                        offsetXState = 0f
                        isDraggingState = false
                    },
                    onDragCancel = {
                        offsetXState = 0f
                        isDraggingState = false
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetXState = (offsetXState + dragAmount).coerceIn(0f, maxOffset)
                    }
                )
            },
        shape = RoundedCornerShape(36.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f + progress * 0.4f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.CenterStart
        ) {
            if (progress > 0.5f) {
                Row(
                    modifier = Modifier.width(pillWidth - padding * 2),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activatedLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                            alpha = (1f - progress).coerceIn(0.3f, 0.7f)
                        ),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.width(pillWidth - padding * 2),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activeLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                            alpha = (1f - progress).coerceIn(0.3f, 0.7f)
                        ),
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .size(thumbSize)
                    .offset { IntOffset(animatedOffset.roundToInt(), 0) },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp + (4.dp * progress)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        if (progress > 0.5f) activatedIcon else leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
```

- [ ] **步骤 2：Commit**

```
feat(home): 创建 SlideActionPill 滑动操作拉条组件
```

---

## 任务 3：创建 MomentFocusCard 组件

**文件：**
- 创建：`feature/home/src/main/java/com/nltimer/feature/home/ui/components/MomentFocusCard.kt`

- [ ] **步骤 1：创建 MomentFocusCard.kt**

三态聚焦卡片，包含实时计时器：

```kotlin
package com.nltimer.feature.home.ui.components

import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.util.formatDuration
import com.nltimer.core.designsystem.theme.appBorder
import com.nltimer.feature.home.model.GridCellUiState

@Composable
fun MomentFocusCard(
    activeCell: GridCellUiState?,
    nextPendingCell: GridCellUiState?,
    onCompleteBehavior: (Long) -> Unit,
    onStartNextPending: () -> Unit,
    onEmptyCellClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasActive = activeCell != null
    val hasPending = nextPendingCell != null

    when {
        hasActive -> ActiveCard(
            cell = activeCell!!,
            onComplete = { activeCell.behaviorId?.let(onCompleteBehavior) },
            modifier = modifier,
        )
        hasPending -> PendingCard(
            cell = nextPendingCell!!,
            onStart = onStartNextPending,
            modifier = modifier,
        )
        else -> EmptyCard(
            onClick = onEmptyCellClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun ActiveCard(
    cell: GridCellUiState,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val startMs = cell.startEpochMs ?: System.currentTimeMillis()
    val elapsedTime by produceState(initialValue = System.currentTimeMillis() - startMs) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            value = System.currentTimeMillis() - startMs
        }
    }
    val durationText = formatDuration(elapsedTime)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .appBorder(
                borderProducer = {
                    BorderStroke(
                        2.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha)
                    )
                },
                shape = RoundedCornerShape(32.dp)
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .height(260.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SlideActionPill(
                onActivate = onComplete,
                activeLabel = "滑动完成",
                activatedLabel = "释放完成",
                leadingIcon = Icons.Filled.Check,
                activatedIcon = Icons.Filled.Check,
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "${cell.activityIconKey ?: ""} ${cell.activityName ?: ""}",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = durationText,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "正在专注...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PendingCard(
    cell: GridCellUiState,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val estimatedText = cell.estimatedDuration?.let { "预计 ${formatDuration(it)}" } ?: ""

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .height(260.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SlideActionPill(
                onActivate = onStart,
                activeLabel = "滑动开启",
                activatedLabel = "释放开启",
                leadingIcon = Icons.Filled.PlayArrow,
                activatedIcon = Icons.Filled.Check,
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "${cell.activityIconKey ?: ""} ${cell.activityName ?: ""}",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(Modifier.height(8.dp))

            if (estimatedText.isNotEmpty()) {
                Text(
                    text = estimatedText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
            }

            Text(
                text = "滑动开启目标",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun EmptyCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .height(260.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "添加行为",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "点击开始记录你的行为",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
```

- [ ] **步骤 2：Commit**

```
feat(home): 创建 MomentFocusCard 三态聚焦卡片组件
```

---

## 任务 4：创建 MomentView 组件

**文件：**
- 创建：`feature/home/src/main/java/com/nltimer/feature/home/ui/components/MomentView.kt`

- [ ] **步骤 1：创建 MomentView.kt**

主布局：聚焦卡片 + 行为列表：

```kotlin
package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.util.formatDuration
import com.nltimer.core.data.util.hhmmFormatter
import com.nltimer.core.designsystem.theme.HomeLayout
import com.nltimer.core.designsystem.theme.appBorder
import com.nltimer.feature.home.model.GridCellUiState
import java.time.LocalTime

@Composable
fun MomentView(
    cells: List<GridCellUiState>,
    hasActiveBehavior: Boolean,
    activeBehaviorId: Long?,
    onCompleteBehavior: (Long) -> Unit,
    onStartNextPending: () -> Unit,
    onEmptyCellClick: (idleStart: LocalTime?, idleEnd: LocalTime?) -> Unit,
    onCellLongClick: (GridCellUiState) -> Unit,
    onLayoutChange: (HomeLayout) -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeCell = remember(cells) {
        cells.firstOrNull { it.isCurrent && it.behaviorId != null && it.status == BehaviorNature.ACTIVE }
    }
    val nextPendingCell = remember(cells) {
        cells.firstOrNull { it.behaviorId != null && it.status == BehaviorNature.PENDING }
    }

    val behaviors = remember(cells) {
        cells.filter { it.behaviorId != null }
            .sortedBy { it.startTime }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp
        ),
    ) {
        item {
            LayoutMenuHeader(
                title = "当前时刻",
                onLayoutChange = onLayoutChange,
            )
        }

        item {
            MomentFocusCard(
                activeCell = activeCell,
                nextPendingCell = nextPendingCell,
                onCompleteBehavior = onCompleteBehavior,
                onStartNextPending = onStartNextPending,
                onEmptyCellClick = { onEmptyCellClick(null, null) },
            )
        }

        if (behaviors.isNotEmpty()) {
            item {
                Text(
                    text = "今日行为",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            items(items = behaviors, key = { it.behaviorId!! }) { behavior ->
                MomentBehaviorItem(
                    behavior = behavior,
                    onLongClick = { onCellLongClick(behavior) },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MomentBehaviorItem(
    behavior: GridCellUiState,
    onLongClick: () -> Unit,
) {
    val isActive = behavior.isCurrent && behavior.status == BehaviorNature.ACTIVE
    val isPending = behavior.status == BehaviorNature.PENDING

    val cardBackground = if (isActive) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    }
    val borderColor = when {
        isActive -> MaterialTheme.colorScheme.primary
        isPending -> MaterialTheme.colorScheme.outlineVariant
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .behaviorCardStyle(cardBackground, borderColor)
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick,
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${behavior.activityIconKey ?: ""} ${behavior.activityName ?: ""}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )

            when {
                isActive -> {
                    Text(
                        text = "进行中",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                isPending -> {
                    Text(
                        text = "目标",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
                else -> {
                    val startStr = behavior.startTime?.format(hhmmFormatter) ?: ""
                    val endStr = behavior.endTime?.format(hhmmFormatter) ?: ""
                    if (startStr.isNotEmpty()) {
                        Text(
                            text = if (endStr.isNotEmpty()) "$startStr - $endStr" else startStr,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        BehaviorTagRow(behavior.tags)

        val duration = behavior.durationMs ?: (behavior.actualDuration ?: 0L)
        if (duration > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
```

- [ ] **步骤 2：Commit**

```
feat(home): 创建 MomentView 当前时刻布局组件
```

---

## 任务 5：HomeScreen 集成 MOMENT 布局

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt`

- [ ] **步骤 1：在 HomeScreen when(layout) 中新增 MOMENT 分支**

在 `HomeScreen.kt` 中，找到 `when (layout)` 块（约 296 行），在 `HomeLayout.LOG -> { ... }` 分支之后，`}` 闭合之前，新增：

```kotlin
HomeLayout.MOMENT -> {
    val allCells = remember(uiState.rows) { uiState.rows.flatMap { it.cells } }
    MomentView(
        cells = allCells,
        hasActiveBehavior = uiState.hasActiveBehavior,
        activeBehaviorId = activeBehaviorId,
        onCompleteBehavior = onCompleteBehavior,
        onStartNextPending = onStartNextPending,
        onEmptyCellClick = onEmptyCellClick,
        onCellLongClick = onCellLongClick,
        onLayoutChange = onLayoutChange,
        modifier = Modifier.weight(1f),
    )
}
```

- [ ] **步骤 2：添加 MomentView 的 import**

在 `HomeScreen.kt` 文件头部的 import 区域新增：

```kotlin
import com.nltimer.feature.home.ui.components.MomentView
```

- [ ] **步骤 3：构建验证**

运行：`.\gradlew :feature:home:compileDebugKotlin`

预期：编译成功，无错误

- [ ] **步骤 4：Commit**

```
feat(home): HomeScreen 集成 MOMENT 当前时刻布局
```
