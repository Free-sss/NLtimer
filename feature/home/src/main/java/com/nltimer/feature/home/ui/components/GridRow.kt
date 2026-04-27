package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.feature.home.model.GridCellUiState
import com.nltimer.feature.home.model.GridRowUiState

@Composable
fun GridRow(
    row: GridRowUiState,
    onEmptyCellClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var detailCell by remember { mutableStateOf<GridCellUiState?>(null) }

    val gridMinHeight  = 90.dp
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
                        modifier = Modifier
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        val cell = row.cells.getOrNull(index)
                        if (cell != null) {
                            when {
                                row.isLocked -> GridCellLocked(modifier = Modifier.heightIn(min = gridMinHeight))
                                cell.behaviorId != null -> GridCell(
                                    cell = cell,
                                    modifier = Modifier
                                        .heightIn(min = gridMinHeight)
                                        .clickable { detailCell = cell },
                                )
                                index == targetEmptyIndex -> GridCellEmpty(
                                    onClick = onEmptyCellClick,
                                    modifier = Modifier.heightIn(min = gridMinHeight)
                                )
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }

    detailCell?.let { cell ->
        BehaviorDetailDialog(
            cell = cell,
            onDismiss = { detailCell = null },
        )
    }
}

@Composable
private fun BehaviorDetailDialog(
    cell: GridCellUiState,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${cell.activityEmoji ?: ""} ${cell.activityName ?: "未命名"}".trim(),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (cell.tags.isNotEmpty()) {
                    Text(
                        text = "标签: ${cell.tags.joinToString("、") { it.name }}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Text(
                    text = "状态: ${cell.status?.name ?: "未知"}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (cell.estimatedDuration != null) {
                    Text(
                        text = "预计时长: ${cell.estimatedDuration / 60000} 分钟",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (cell.actualDuration != null) {
                    Text(
                        text = "实际时长: ${cell.actualDuration / 60000} 分钟",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (cell.durationMs != null) {
                    val minutes = cell.durationMs / 60000
                    Text(
                        text = "已进行: $minutes 分钟",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
    )
}
