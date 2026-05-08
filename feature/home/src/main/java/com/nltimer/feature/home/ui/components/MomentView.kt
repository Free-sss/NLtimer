package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.util.formatDuration
import com.nltimer.core.data.util.hhmmFormatter
import com.nltimer.core.designsystem.theme.HomeLayout
import com.nltimer.feature.home.model.GridCellUiState
import com.nltimer.feature.home.model.TagUiState
import java.time.LocalTime

enum class MomentFilterTab {
    ALL,
    COMPLETED,
    PENDING,
}

enum class MomentSortMode(val label: String) {
    TIME_DESC("时间反"),
    TIME_ASC("时间正"),
    DURATION("用时"),
}

@Composable
fun MomentView(
    cells: List<GridCellUiState>,
    hasActiveBehavior: Boolean,
    activeBehaviorId: Long?,
    onCompleteBehavior: (Long) -> Unit,
    onStartNextPending: () -> Unit,
    onStartBehavior: (Long) -> Unit,
    onEmptyCellClick: (idleStart: LocalTime?, idleEnd: LocalTime?) -> Unit,
    onCellLongClick: (GridCellUiState) -> Unit,
    onLayoutChange: (HomeLayout) -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeCell = remember(cells) {
        cells.firstOrNull {
            it.isCurrent && it.behaviorId != null && it.status == BehaviorNature.ACTIVE
        }
    }
    val nextPendingCell = remember(cells) {
        cells.firstOrNull { it.behaviorId != null && it.status == BehaviorNature.PENDING }
    }

    var filterTab by remember { mutableStateOf(MomentFilterTab.ALL) }
    var sortMode by remember { mutableStateOf(MomentSortMode.TIME_DESC) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var detailCell by remember { mutableStateOf<GridCellUiState?>(null) }

    val behaviors = remember(cells, filterTab, sortMode) {
        val filtered = cells.filter { it.behaviorId != null }.let { list ->
            when (filterTab) {
                MomentFilterTab.ALL -> list
                MomentFilterTab.COMPLETED -> list.filter { it.status == BehaviorNature.COMPLETED || it.status == BehaviorNature.ACTIVE }
                MomentFilterTab.PENDING -> list.filter { it.status == BehaviorNature.PENDING }
            }
        }
        val pending = filtered.filter { it.status == BehaviorNature.PENDING }
            .sortedBy { it.startEpochMs }
        val nonPending = filtered.filter { it.status != BehaviorNature.PENDING }
        val sortedNonPending = when (sortMode) {
            MomentSortMode.TIME_DESC -> nonPending.sortedByDescending { it.startEpochMs ?: 0L }
            MomentSortMode.TIME_ASC -> nonPending.sortedBy { it.startEpochMs ?: 0L }
            MomentSortMode.DURATION -> nonPending.sortedByDescending { it.actualDuration ?: it.durationMs ?: 0L }
        }
        sortedNonPending + pending
    }

    detailCell?.let { cell ->
        BehaviorDetailDialog(cell = cell, onDismiss = { detailCell = null })
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
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
                onStartBehavior = onStartBehavior,
                onEmptyCellClick = { onEmptyCellClick(null, null) },
            )
        }

        if (true) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    FilterChip(
                        selected = filterTab == MomentFilterTab.ALL,
                        onClick = { filterTab = MomentFilterTab.ALL },
                        label = { Text("全部", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.padding(end = 4.dp),
                    )   
                    FilterChip(
                        selected = filterTab == MomentFilterTab.COMPLETED,
                        onClick = { filterTab = MomentFilterTab.COMPLETED },
                        label = { Text("经过", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.padding(end = 4.dp),
                    )
                    FilterChip(
                        selected = filterTab == MomentFilterTab.PENDING,
                        onClick = { filterTab = MomentFilterTab.PENDING },
                        label = { Text("目标", style = MaterialTheme.typography.labelSmall) },
                    )

                    Spacer(Modifier.weight(1f))

                    Box {
                        IconButton(onClick = { sortMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "排序",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false },
                        ) {
                            MomentSortMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            mode.label,
                                            color = if (sortMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        )
                                    },
                                    onClick = {
                                        sortMode = mode
                                        sortMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            }

            items(items = behaviors, key = { it.behaviorId!! }) { behavior ->
                MomentBehaviorItem(
                    behavior = behavior,
                    onClick = { detailCell = behavior },
                    onLongClick = { onCellLongClick(behavior) },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun MomentBehaviorItem(
    behavior: GridCellUiState,
    onClick: () -> Unit,
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
                onClick = onClick,
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

        if (behavior.tags.isNotEmpty() || !behavior.note.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                behavior.tags.forEach { tag ->
                    TagChip(tag = tag)
                }
                if (!behavior.note.isNullOrBlank()) {
                    Text(
                        text = behavior.note,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(120.dp),
                    )
                }
            }
        }

        val duration = if (behavior.isCurrent && behavior.startEpochMs != null) {
            val elapsed by produceState(initialValue = System.currentTimeMillis() - behavior.startEpochMs) {
                while (true) {
                    kotlinx.coroutines.delay(1000)
                    value = System.currentTimeMillis() - behavior.startEpochMs
                }
            }
            elapsed
        } else {
            behavior.durationMs ?: (behavior.actualDuration ?: 0L)
        }
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
