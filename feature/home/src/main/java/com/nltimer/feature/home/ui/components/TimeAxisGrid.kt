package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.GridLayoutStyle
import com.nltimer.core.designsystem.theme.TimeLabelConfig
import com.nltimer.feature.home.model.GridCellUiState
import com.nltimer.feature.home.model.GridDaySection
import com.nltimer.feature.home.model.GridRowUiState
import java.time.LocalTime
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun TimeAxisGrid(
    modifier: Modifier = Modifier,
    sections: List<GridDaySection>,
    onEmptyCellClick: (idleStart: LocalTime?, idleEnd: LocalTime?) -> Unit,
    onCellLongClick: (GridCellUiState) -> Unit = {},
    onLoadMore: () -> Unit = {},
    isLoadingMore: Boolean = false,
    hasReachedEarliest: Boolean = false,
    currentHour: Int = 0,
    showTimeSideBar: Boolean = false,
    timeLabelConfig: TimeLabelConfig = TimeLabelConfig(),
    onTimeLabelSettingsClick: () -> Unit = {},
    gridStyle: GridLayoutStyle = GridLayoutStyle(),
) {
    val listState = rememberLazyListState()
    val visibleDateLabelState = LocalVisibleDateLabel.current

    LaunchedEffect(currentHour, sections) {
        val todaySection = sections.lastOrNull() ?: return@LaunchedEffect
        val targetIndex = todaySection.rows.indexOfFirst { it.startTime.hour >= currentHour }
        if (targetIndex >= 0) {
            val precedingItems = sections.dropLast(1).sumOf { 1 + it.rows.size }
            val absoluteIndex = precedingItems + 1 + targetIndex
            listState.animateScrollToItem(absoluteIndex)
        }
    }

    LaunchedEffect(sections, hasReachedEarliest) {
        if (hasReachedEarliest) return@LaunchedEffect
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .filter { it <= 5 }
            .collect { onLoadMore() }
    }

    val dateIndexMap = remember(sections) {
        val map = mutableMapOf<Int, String>()
        var index = 0
        if (isLoadingMore) index++
        sections.forEach { section ->
            map[index] = section.label
            index++
            index += section.rows.size
        }
        map
    }

    LaunchedEffect(listState, dateIndexMap) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { firstIndex ->
                val label = dateIndexMap.entries
                    .filter { it.key <= firstIndex }
                    .maxByOrNull { it.key }
                    ?.value
                visibleDateLabelState.value = label
            }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.padding(start = 10.dp, end = if (showTimeSideBar) 0.dp else 10.dp, top = 0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(bottom = 630.dp),
    ) {
        if (isLoadingMore) item("loading-top") { LoadingMoreIndicator() }
        sections.forEach { section ->
            item(key = "header-${section.date}") {
                DayDividerRow(label = section.label)
            }
            items(items = section.rows, key = { it.rowId }) { row ->
                GridRow(
                    row = row,
                    onEmptyCellClick = onEmptyCellClick,
                    onCellLongClick = onCellLongClick,
                    timeLabelConfig = timeLabelConfig,
                    gridStyle = gridStyle,
                )
            }
        }
    }
}

@Composable
private fun DayDividerRow(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LoadingMoreIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
    }
}
