package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
    // Mark-style-main
    Box(modifier = modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Column {
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
                row.cells.forEach { cell ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        when {
                            row.isLocked -> GridCellLocked()
                            cell.behaviorId != null -> GridCell(cell = cell)
                            else -> GridCellEmpty(onClick = onEmptyCellClick)
                        }
                    }
                }
            }
        }
    }
}
