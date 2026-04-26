package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.feature.home.model.GridRowUiState

@Composable
fun GridRow(
    row: GridRowUiState,
    onEmptyCellClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Column {
            if (row.cells.isNotEmpty()) {
                TimeFloatingLabel(
                    time = row.startTime,
                    isCurrentRow = row.isCurrentRow,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val addPlaceholderIndex = row.cells.indexOfFirst { it.isAddPlaceholder }
                val firstEmptyIndex = row.cells.indexOfFirst { it.behaviorId == null }
                val targetEmptyIndex = if (addPlaceholderIndex != -1) addPlaceholderIndex else firstEmptyIndex

                repeat(4) { index ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        val cell = row.cells.getOrNull(index)
                        if (cell != null) {
                            when {
                                row.isLocked -> GridCellLocked()
                                cell.behaviorId != null -> GridCell(cell = cell)
                                index == targetEmptyIndex -> GridCellEmpty(
                                    onClick = onEmptyCellClick,
                                    isAddPlaceholder = cell.isAddPlaceholder,
                                )
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}
