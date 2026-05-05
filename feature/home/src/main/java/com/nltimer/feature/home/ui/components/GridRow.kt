package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import com.nltimer.core.designsystem.theme.TimeLabelConfig
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 网格单行 Composable，包含时间标签和最多 4 个单元格。
 * 支持点击单元格弹出行为详情对话框，长按单元格触发编辑弹窗。
 *
 * @param row 行 UI 状态
 * @param onEmptyCellClick 点击空单元格回调
 * @param onCellLongClick 长按单元格回调，传递被长按的单元格数据
 * @param modifier 修饰符
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridRow(
    row: GridRowUiState,
    onEmptyCellClick: (idleStart: LocalTime?, idleEnd: LocalTime?) -> Unit,
    onCellLongClick: (GridCellUiState) -> Unit = {},
    timeLabelConfig: TimeLabelConfig = TimeLabelConfig(),
    modifier: Modifier = Modifier,
) {
    // 记录当前点击的详情单元格，用于弹出对话框
    var detailCell by remember { mutableStateOf<GridCellUiState?>(null) }

    val gridMinHeight  = 100.dp
    Box(modifier = modifier.fillMaxWidth()) {
        Column {
            if (row.cells.isNotEmpty() && timeLabelConfig.visible) {
                TimeFloatingLabel(
                    time = row.startTime,
                    isCurrentRow = row.isCurrentRow,
                    config = timeLabelConfig,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                // 确定唯一的空单元格位置：优先用添加占位，其次第一个空位
                val addPlaceholderIndex = row.cells.indexOfFirst { it.isAddPlaceholder }
                val firstEmptyIndex = row.cells.indexOfFirst { it.behaviorId == null }
                val targetEmptyIndex = if (addPlaceholderIndex != -1) addPlaceholderIndex else firstEmptyIndex

                // 一行固定渲染 4 列，根据状态显示不同组件
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
                                        .combinedClickable(
                                            onClick = { detailCell = cell },
                                            onLongClick = { onCellLongClick(cell) },
                                        ),
                                )
                                index == targetEmptyIndex -> GridCellEmpty(
                                    onClick = { onEmptyCellClick(cell.startTime, cell.endTime) },
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

    // 非空时弹出行为详情弹窗
    detailCell?.let { cell ->
        BehaviorDetailDialog(
            cell = cell,
            onDismiss = { detailCell = null },
        )
    }
}

/**
 * 行为详情对话框 Composable。
 * 展示活动 emoji、名称、标签、状态、预计时长和实际时长等信息。
 *
 * @param cell 要展示详情的单元格数据
 * @param onDismiss 关闭对话框回调
 */
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
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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
                if (cell.isCurrent) {
                    Text(
                        text = "正在进行",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (cell.wasPlanned) {
                    Text(
                        text = "计划行为",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (cell.startTime != null) {
                    val timeText = if (cell.endTime != null) {
                        "时间: ${cell.startTime.format(timeFormatter)} - ${cell.endTime.format(timeFormatter)}"
                    } else {
                        "开始: ${cell.startTime.format(timeFormatter)}"
                    }
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
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
                if (cell.achievementLevel != null) {
                    Text(
                        text = "成就等级: ${cell.achievementLevel}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (cell.pomodoroCount > 0) {
                    Text(
                        text = "番茄钟: ${cell.pomodoroCount} 个",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (!cell.note.isNullOrBlank()) {
                    Text(
                        text = "备注: ${cell.note}",
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
