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
import com.nltimer.core.data.model.LogLayoutStyle
import com.nltimer.core.data.util.hhmmFormatter
import com.nltimer.core.designsystem.theme.LocalImmersiveTopPadding
import com.nltimer.feature.home.model.GridCellUiState
import com.nltimer.feature.home.model.HomeListItem
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun BehaviorLogView(
    items: List<HomeListItem>,
    onCellLongClick: (GridCellUiState) -> Unit = {},
    onLoadMore: () -> Unit = {},
    isLoadingMore: Boolean = false,
    hasReachedEarliest: Boolean = false,
    modifier: Modifier = Modifier,
    logStyle: LogLayoutStyle = LogLayoutStyle(),
) {
    val timeFormatter = hhmmFormatter
    val listState = rememberLazyListState()
    var detailCell by remember { mutableStateOf<GridCellUiState?>(null) }

    val displayItems = remember(items) { reverseGroupedItems(items) }

    val dateIndexMap = remember(displayItems) {
        val map = mutableMapOf<Int, String>()
        displayItems.forEachIndexed { index, item ->
            if (item is HomeListItem.DayDivider) map[index] = item.label
        }
        map
    }

    val visibleDateLabelState = LocalVisibleDateLabel.current

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

    LaunchedEffect(displayItems, hasReachedEarliest) {
        if (hasReachedEarliest) return@LaunchedEffect
        snapshotFlow {
            val total = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total to lastVisible
        }.distinctUntilChanged()
            .filter { (total, last) -> total > 0 && last >= total - 5 }
            .collect { onLoadMore() }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp + LocalImmersiveTopPadding.current, end = 16.dp, bottom = 180.dp),
        ) {
            if (displayItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "暂无行为记录",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(items = displayItems, key = { it.key }) { item ->
                    when (item) {
                        is HomeListItem.DayDivider -> DayDividerRow(label = item.label)
                        is HomeListItem.CellItem -> BehaviorLogCard(
                            behavior = item.cell,
                            timeFormatter = timeFormatter,
                            onClick = { detailCell = item.cell },
                            onLongClick = { onCellLongClick(item.cell) },
                            logStyle = logStyle,
                        )
                    }
                }
                if (isLoadingMore) item { LoadingMoreIndicator() }
            }
        }
    }

    detailCell?.let { cell ->
        BehaviorDetailDialog(cell = cell, onDismiss = { detailCell = null })
    }
}

private fun reverseGroupedItems(items: List<HomeListItem>): List<HomeListItem> {
    val groups = mutableListOf<Pair<HomeListItem.DayDivider, MutableList<HomeListItem.CellItem>>>()
    items.forEach { item ->
        when (item) {
            is HomeListItem.DayDivider -> groups.add(item to mutableListOf())
            is HomeListItem.CellItem -> groups.lastOrNull()?.second?.add(item)
        }
    }
    return groups.asReversed().flatMap { (divider, cells) ->
        listOf<HomeListItem>(divider) + cells.asReversed()
    }
}

@Composable
private fun DayDividerRow(label: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
        )
    }
}
