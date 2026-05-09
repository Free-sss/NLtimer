package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.util.formatDuration
import com.nltimer.core.data.util.hhmmFormatter
import com.nltimer.core.designsystem.icon.IconRenderer
import com.nltimer.core.designsystem.theme.BorderTokens
import com.nltimer.core.designsystem.theme.HomeLayout
import com.nltimer.core.designsystem.theme.ShapeTokens
import com.nltimer.core.designsystem.theme.appBorder
import com.nltimer.core.designsystem.theme.styledAlpha
import com.nltimer.core.designsystem.theme.styledBorder
import com.nltimer.core.designsystem.theme.styledCorner
import com.nltimer.feature.home.model.GridCellUiState
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TimelineReverseView(
    cells: List<GridCellUiState>,
    onAddClick: (idleStart: LocalTime?, idleEnd: LocalTime?) -> Unit,
    onCellLongClick: (GridCellUiState) -> Unit = {},
    onLayoutChange: (HomeLayout) -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeFormatter = hhmmFormatter

    val behaviors = remember(cells) {
        cells.filter { it.behaviorId != null }
            .sortedBy { it.startTime }
    }

    val timelineItems = remember(behaviors) {
        val items = mutableListOf<TimelineItemData>()

        if (behaviors.isNotEmpty()) {
            val latest = behaviors.last()
            if (latest.status != BehaviorNature.ACTIVE && latest.endTime != null) {
                val now = LocalTime.now()
                if (now.isAfter(latest.endTime)) {
                    val gap = Duration.between(latest.endTime, now)
                    if (gap.toMinutes() >= 1) {
                        items.add(TimelineItemData.Idle(latest.endTime, now))
                    }
                }
            }

            for (i in behaviors.indices.reversed()) {
                val behavior = behaviors[i]
                items.add(TimelineItemData.Behavior(behavior))

                if (i > 0) {
                    val prevEnd = behaviors[i - 1].endTime
                    val currentStart = behavior.startTime
                    if (prevEnd != null && currentStart != null && currentStart.isAfter(prevEnd)) {
                        val gap = Duration.between(prevEnd, currentStart)
                        if (gap.toMinutes() >= 1) {
                            items.add(TimelineItemData.Idle(prevEnd, currentStart))
                        }
                    }
                }
            }
        }
        items
    }

    var detailCell by remember { mutableStateOf<GridCellUiState?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                LayoutMenuHeader(
                    title = "时间轴",
                    onLayoutChange = onLayoutChange,
                )
            }

            items(items = timelineItems, key = { item ->
                when (item) {
                    is TimelineItemData.Behavior -> "b_${item.behavior.behaviorId}"
                    is TimelineItemData.Idle -> "i_${item.start}_${item.end}"
                }
            }) { item ->
                when (item) {
                    is TimelineItemData.Behavior -> {
                        TimelineBehaviorItem(
                            behavior = item.behavior,
                            timeFormatter = timeFormatter,
                            onClick = { detailCell = item.behavior },
                            onLongClick = { onCellLongClick(item.behavior) },
                        )
                    }
                    is TimelineItemData.Idle -> {
                        TimelineIdleItem(
                            start = item.start,
                            end = item.end,
                            timeFormatter = timeFormatter,
                            onAddClick = { onAddClick(item.start, item.end) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    detailCell?.let { cell ->
        BehaviorDetailDialog(
            cell = cell,
            onDismiss = { detailCell = null },
        )
    }
}

sealed class TimelineItemData {
    data class Behavior(val behavior: GridCellUiState) : TimelineItemData()
    data class Idle(val start: LocalTime, val end: LocalTime) : TimelineItemData()
}

@Composable
private fun TimelineIdleItem(
    start: LocalTime,
    end: LocalTime,
    timeFormatter: DateTimeFormatter,
    onAddClick: () -> Unit
) {
    val duration = Duration.between(start, end)
    val durationText = formatDuration(duration.toMillis())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.width(50.dp)
        ) {
            Text(
                text = end.format(timeFormatter),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = start.format(timeFormatter),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = styledAlpha(0.6f))
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(styledCorner(ShapeTokens.CORNER_MEDIUM)))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = styledAlpha(0.1f)))
                .appBorder(
                    borderProducer = {
                        BorderStroke(
                            styledBorder(BorderTokens.THIN),
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = styledAlpha(0.3f))
                        )
                    },
                    shape = RoundedCornerShape(styledCorner(ShapeTokens.CORNER_MEDIUM))
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "❓ 空闲 : $durationText",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onAddClick, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = styledAlpha(0.5f)),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun TimelineBehaviorItem(
    behavior: GridCellUiState,
    timeFormatter: DateTimeFormatter,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.width(50.dp)
        ) {
            Text(
                text = behavior.startTime?.format(timeFormatter) ?: "",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            behavior.endTime?.let {
                Text(
                    text = it.format(timeFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val cardBackground = if (behavior.isCurrent) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        }
        val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

        Column(
            modifier = Modifier
                .weight(1f)
                .behaviorCardStyle(cardBackground, borderColor)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconRenderer(
                        iconKey = behavior.activityIconKey,
                        defaultEmoji = "❓",
                        iconSize = 16.dp,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = behavior.activityName ?: "未知",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }

                val duration = if (behavior.isCurrent && behavior.startEpochMs != null) {
                    LiveElapsedDuration(
                        startEpochMs = behavior.startEpochMs,
                        isCurrent = true,
                        fallbackDurationMs = behavior.durationMs ?: (behavior.actualDuration ?: 0L),
                    )
                } else {
                    behavior.durationMs ?: (behavior.actualDuration ?: 0L)
                }
                if (duration > 0) {
                    Text(
                        text = "⏱ ${formatDuration(duration)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            BehaviorTagRow(behavior.tags)

            behavior.note?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
