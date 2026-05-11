package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.feature.home.model.GridCellUiState
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
    modifier: Modifier = Modifier,
) {
    val momentFilterState = LocalMomentFilterState.current
    val filterTab = remember(momentFilterState.filterKey) {
        when (momentFilterState.filterKey) {
            "COMPLETED" -> MomentFilterTab.COMPLETED
            "PENDING" -> MomentFilterTab.PENDING
            else -> MomentFilterTab.ALL
        }
    }
    val sortMode = remember(momentFilterState.sortKey) {
        when (momentFilterState.sortKey) {
            "TIME_ASC" -> MomentSortMode.TIME_ASC
            "DURATION" -> MomentSortMode.DURATION
            else -> MomentSortMode.TIME_DESC
        }
    }

    val activeCell = remember(cells) {
        cells.firstOrNull {
            it.isCurrent && it.behaviorId != null && it.status == BehaviorNature.ACTIVE
        }
    }
    val nextPendingCell = remember(cells) {
        cells.firstOrNull { it.behaviorId != null && it.status == BehaviorNature.PENDING }
    }

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
        pending + sortedNonPending
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
            MomentFocusCard(
                activeCell = activeCell,
                nextPendingCell = nextPendingCell,
                onCompleteBehavior = onCompleteBehavior,
                onStartNextPending = onStartNextPending,
                onStartBehavior = onStartBehavior,
                onEmptyCellClick = { onEmptyCellClick(null, null) },
            )
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
