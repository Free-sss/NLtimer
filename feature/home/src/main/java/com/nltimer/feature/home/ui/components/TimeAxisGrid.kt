package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.feature.home.model.GridRowUiState

@Composable
fun TimeAxisGrid(
    modifier: Modifier = Modifier,
    rows: List<GridRowUiState>,
    onEmptyCellClick: () -> Unit,
    currentHour: Int = 0,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(currentHour) {
        val targetIndex = rows.indexOfFirst { it.startTime.hour >= currentHour }
        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.padding(start = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = rows, key = { it.rowId }) { row ->
            GridRow(
                row = row,
                onEmptyCellClick = onEmptyCellClick,
            )
        }
    }
}
