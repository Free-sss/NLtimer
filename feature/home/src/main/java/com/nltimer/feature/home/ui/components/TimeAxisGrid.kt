package com.nltimer.feature.home.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.GridLayoutStyle
import com.nltimer.core.designsystem.theme.LocalImmersiveTopPadding
import com.nltimer.core.designsystem.theme.TimeLabelConfig
import com.nltimer.feature.home.model.GridCellUiState
import com.nltimer.feature.home.model.GridDaySection
import com.nltimer.feature.home.model.GridRowUiState
import java.time.LocalTime
import java.util.TreeMap
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
    footer: @Composable (LazyItemScope.() -> Unit)? = null,
) {
    val listState = rememberLazyListState()
    val visibleDateLabelState = LocalVisibleDateLabel.current
    val initialScrollDone = rememberSaveable { mutableStateOf(false) }

    // 初始定位到今天及当前小时
    LaunchedEffect(sections) {
        if (sections.isNotEmpty() && !initialScrollDone.value) {
            val todaySection = sections.lastOrNull()
            if (todaySection != null) {
                val precedingItems = sections.dropLast(1).sumOf { 1 + it.rows.size }
                val todayHeaderIndex = if (isLoadingMore) precedingItems + 1 else precedingItems
                val targetRowIndex = todaySection.rows.indexOfFirst { it.startTime.hour >= currentHour }
                val absoluteIndex = todayHeaderIndex + 1 + (if (targetRowIndex >= 0) targetRowIndex else 0)
                
                listState.scrollToItem(absoluteIndex)
                initialScrollDone.value = true
            }
        }
    }

    val dateIndexMap = remember(sections) {
        val map = TreeMap<Int, String>()
        var index = 0
        if (isLoadingMore) index++
        sections.forEach { section ->
            map[index] = section.label
            index++
            index += section.rows.size
        }
        map
    }

    val currentLabel by remember(dateIndexMap) {
        derivedStateOf {
            val firstIndex = listState.firstVisibleItemIndex
            dateIndexMap.floorEntry(firstIndex)?.value
        }
    }

    LaunchedEffect(currentLabel) {
        visibleDateLabelState.value = currentLabel
    }

    LaunchedEffect(currentHour) {
        if (!initialScrollDone.value) return@LaunchedEffect
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
            .filter { it <= 5 && initialScrollDone.value }
            .collect { onLoadMore() }
    }

    val alphaState = animateFloatAsState(
        targetValue = if (initialScrollDone.value) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "GridFadeIn"
    )

    LazyColumn(
        state = listState,
        modifier = modifier
            .graphicsLayer { this.alpha = alphaState.value }
            .padding(start = 10.dp, end = if (showTimeSideBar) 0.dp else 10.dp, top = 0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(bottom = 100.dp, top = LocalImmersiveTopPadding.current),
    ) {
        if (isLoadingMore) item("loading-top") { LoadingMoreIndicator() }
        sections.forEach { section ->
            item(
                key = "header-${section.date}",
                contentType = "day_divider"
            ) {
                DayDividerRow(label = section.label)
            }
            items(
                items = section.rows,
                key = { it.rowId },
                contentType = { "grid_row" }
            ) { row ->
                GridRow(
                    row = row,
                    onEmptyCellClick = onEmptyCellClick,
                    onCellLongClick = onCellLongClick,
                    timeLabelConfig = timeLabelConfig,
                    gridStyle = gridStyle,
                )
            }
        }
        if (footer != null) {
            item(key = "footer", contentType = "footer") {
                footer()
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
