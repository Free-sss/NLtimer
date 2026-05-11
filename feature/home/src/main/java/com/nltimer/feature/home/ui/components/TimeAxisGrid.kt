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
import com.nltimer.core.designsystem.theme.TimeLabelConfig
import com.nltimer.feature.home.model.GridCellUiState
import com.nltimer.feature.home.model.GridRowUiState
import java.time.LocalTime

/**
 * 网格时间轴 Composable — 纵向滚动的网格布局。
 * 支持布局切换菜单，并根据当前小时自动滚动到对应位置。
 *
 * @param modifier 修饰符
 * @param rows 所有网格行数据
 * @param onEmptyCellClick 点击空单元格回调
 * @param onCellLongClick 长按单元格回调
 * @param onLayoutChange 切换布局模式回调
 * @param currentHour 当前选中的小时
 */
@Composable
fun TimeAxisGrid(
    modifier: Modifier = Modifier,
    rows: List<GridRowUiState>,
    onEmptyCellClick: (idleStart: LocalTime?, idleEnd: LocalTime?) -> Unit,
    onCellLongClick: (GridCellUiState) -> Unit = {},
    currentHour: Int = 0,
    showTimeSideBar: Boolean = false,
    timeLabelConfig: TimeLabelConfig = TimeLabelConfig(),
    onTimeLabelSettingsClick: () -> Unit = {},
) {
    val listState = rememberLazyListState()

    // 当 currentHour 变化时，自动滚动到对应时间行
    LaunchedEffect(currentHour) {
        val targetIndex = rows.indexOfFirst { it.startTime.hour >= currentHour }
        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.padding(start = 10.dp, end = if (showTimeSideBar) 0.dp else 10.dp, top = 0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        items(items = rows, key = { it.rowId }) { row ->
            GridRow(
                row = row,
                onEmptyCellClick = onEmptyCellClick,
                onCellLongClick = onCellLongClick,
                timeLabelConfig = timeLabelConfig,
            )
        }
    }
}
