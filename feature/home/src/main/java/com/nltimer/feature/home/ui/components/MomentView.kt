package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.feature.home.model.GridCellUiState
import java.time.LocalTime
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

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
    onLoadMore: () -> Unit = {},
    isLoadingMore: Boolean = false,
    hasReachedEarliest: Boolean = false,
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

    val listState = rememberLazyListState()

    LaunchedEffect(cells, hasReachedEarliest) {
        if (hasReachedEarliest) return@LaunchedEffect
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .filter { it <= 5 }
            .collect { onLoadMore() }
    }

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
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = 16.dp, bottom = 180.dp
        ),
    ) {
        if (isLoadingMore) item("loading-top") {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }
        if (hasReachedEarliest) item("reached-earliest") {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "已到最早一条记录",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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

        items(items = behaviors, key = { it.behaviorId!! }) { behavior ->
            MomentBehaviorItem(
                behavior = behavior,
                onClick = { detailCell = behavior },
                onLongClick = { onCellLongClick(behavior) },
            )
        }
    }
}
