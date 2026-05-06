package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.util.formatDuration
import com.nltimer.core.data.util.hhmmFormatter
import com.nltimer.core.designsystem.theme.HomeLayout
import com.nltimer.feature.home.model.GridCellUiState
import java.time.format.DateTimeFormatter

@Composable
fun BehaviorLogView(
    cells: List<GridCellUiState>,
    onLayoutChange: (HomeLayout) -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeFormatter = hhmmFormatter

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
                LayoutMenuHeader(
                    title = "行为日志",
                    onLayoutChange = onLayoutChange,
                )
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
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .behaviorCardStyle(cardBackground, borderColor)
    ) {
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

        BehaviorTagRow(behavior.tags)

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

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "behaviorId: ${behavior.behaviorId ?: "-"}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}


