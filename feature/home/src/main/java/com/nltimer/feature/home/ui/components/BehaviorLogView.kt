package com.nltimer.feature.home.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.util.formatDuration
import com.nltimer.core.data.util.hhmmFormatter
import com.nltimer.core.designsystem.theme.HomeLayout
import com.nltimer.core.designsystem.theme.ShapeTokens
import com.nltimer.core.designsystem.theme.styledAlpha
import com.nltimer.core.designsystem.theme.styledCorner
import com.nltimer.feature.home.model.GridCellUiState
import java.time.format.DateTimeFormatter

@Composable
fun BehaviorLogView(
    cells: List<GridCellUiState>,
    onCellLongClick: (GridCellUiState) -> Unit = {},
    onLayoutChange: (HomeLayout) -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeFormatter = hhmmFormatter

    val behaviors = remember(cells) {
        cells.filter { it.behaviorId != null && it.startTime != null }
            .sortedByDescending { it.startTime }
    }

    var detailCell by remember { mutableStateOf<GridCellUiState?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                LayoutMenuHeader(
                    title = "\u884c\u4e3a\u65e5\u5fd7",
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
                            text = "\u6682\u65e0\u884c\u4e3a\u8bb0\u5f55",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(items = behaviors, key = { it.behaviorId!! }) { behavior ->
                    BehaviorLogCard(
                        behavior = behavior,
                        timeFormatter = timeFormatter,
                        onClick = { detailCell = behavior },
                        onLongClick = { onCellLongClick(behavior) },
                    )
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun BehaviorLogCard(
    behavior: GridCellUiState,
    timeFormatter: DateTimeFormatter,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val cardBackground = if (behavior.isCurrent) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = styledAlpha(0.15f))
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = styledAlpha(0.2f))
    }
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = styledAlpha(0.5f))

    Column(
        modifier = Modifier
            .fillMaxWidth()
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
            Text(
                text = "${behavior.activityIconKey ?: "\u2753"} ${behavior.activityName ?: "\u672a\u77e5"}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            behavior.status?.let { status ->
                val (bgColor, textColor) = when (status) {
                    BehaviorNature.ACTIVE ->
                        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                    BehaviorNature.COMPLETED ->
                        MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
                    BehaviorNature.PENDING ->
                        MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(styledCorner(ShapeTokens.CORNER_SMALL)))
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
            val endText = behavior.endTime?.format(timeFormatter) ?: "\u8fdb\u884c\u4e2d"
            Text(
                text = "\u8d77\u59cb: $startText \u2192 \u7ed3\u675f: $endText",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val duration = behavior.durationMs
                ?: (behavior.actualDuration ?: 0L)
            if (duration > 0) {
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "\u7528\u65f6: ${formatDuration(duration)}",
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
                    text = "\u5907\u6ce8: $note",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        val details = buildList {
            if (behavior.pomodoroCount > 0) add("\u756a\u8304\u949f: ${behavior.pomodoroCount}")
            behavior.estimatedDuration?.let { add("\u9884\u4f30: ${formatDuration(it)}") }
            behavior.actualDuration?.let { add("\u5b9e\u9645: ${formatDuration(it)}") }
            behavior.achievementLevel?.let { add("\u5b8c\u6210\u5ea6: $it") }
            add("\u8ba1\u5212\u5185: ${if (behavior.wasPlanned) "\u662f" else "\u5426"}")
        }
        if (details.isNotEmpty()) {
            Text(
                text = details.joinToString("    "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = styledAlpha(0.8f))
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "behaviorId: ${behavior.behaviorId ?: "-"}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = styledAlpha(0.5f))
        )
    }
}
