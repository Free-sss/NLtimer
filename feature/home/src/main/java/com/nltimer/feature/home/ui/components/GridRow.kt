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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.nltimer.feature.home.model.GridCellUiState
import com.nltimer.feature.home.model.GridRowUiState
import com.nltimer.core.data.util.formatDuration
import com.nltimer.core.data.util.hhmmFormatter
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
 * 展示活动图标、名称、标签、状态、预计时长和实际时长等信息。
 *
 * @param cell 要展示详情的单元格数据
 * @param onDismiss 关闭对话框回调
 */
@Composable
private fun BehaviorDetailDialog(
    cell: GridCellUiState,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    fun epochToMsString(epochMs: Long?): String {
        if (epochMs == null) return "(空)"
        val instant = java.time.Instant.ofEpochMilli(epochMs)
            .atZone(java.time.ZoneId.systemDefault())
        return instant.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
    }

    fun buildExportText(): String {
        val sb = StringBuilder()
        sb.appendLine("=== 行为详情 ===")
        sb.appendLine("behaviorId: ${cell.behaviorId ?: "null"}")
        sb.appendLine("activityIconKey: ${cell.activityIconKey ?: "(空)"}")
        sb.appendLine("activityName: ${cell.activityName ?: "(空)"}")
        sb.appendLine("status: ${cell.status?.name ?: "null"}")
        sb.appendLine("isCurrent: ${cell.isCurrent}")
        sb.appendLine("wasPlanned: ${cell.wasPlanned}")
        sb.appendLine("isAddPlaceholder: ${cell.isAddPlaceholder}")
        sb.appendLine("tags: ${cell.tags.joinToString("、") { "id=${it.id}, name=${it.name}, color=${it.color}, isActive=${it.isActive}" }.ifEmpty { "(空)" }}")
        sb.appendLine("startTime: ${cell.startTime?.format(DateTimeFormatter.ofPattern("HH:mm:ss")) ?: "(空)"}")
        sb.appendLine("startEpochMs: ${epochToMsString(cell.startEpochMs)}")
        sb.appendLine("endTime: ${cell.endTime?.format(DateTimeFormatter.ofPattern("HH:mm:ss")) ?: "(空)"}")
        sb.appendLine("endEpochMs: ${epochToMsString(cell.endEpochMs)}")
        sb.appendLine("estimatedDuration: ${cell.estimatedDuration?.let { "${formatDuration(it)} (${it}ms)" } ?: "(空)"}")
        sb.appendLine("actualDuration: ${cell.actualDuration?.let { "${formatDuration(it)} (${it}ms)" } ?: "(空)"}")
        sb.appendLine("durationMs: ${cell.durationMs?.let { "${formatDuration(it)} (${it}ms)" } ?: "(空)"}")
        sb.appendLine("achievementLevel: ${cell.achievementLevel?.toString() ?: "(空)"}")
        sb.appendLine("pomodoroCount: ${cell.pomodoroCount}")
        sb.appendLine("note: ${cell.note ?: "(空)"}")
        return sb.toString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "行为详情 #${cell.behaviorId ?: "N/A"}",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                DetailRow("behaviorId", "${cell.behaviorId ?: "null"}")
                DetailRow("activityIconKey", cell.activityIconKey ?: "(空)")
                DetailRow("activityName", cell.activityName ?: "(空)")
                DetailRow("status", cell.status?.name ?: "null")
                DetailRow("isCurrent", "${cell.isCurrent}")
                DetailRow("wasPlanned", "${cell.wasPlanned}")
                DetailRow("isAddPlaceholder", "${cell.isAddPlaceholder}")
                DetailRow("tags", cell.tags.joinToString("、") {
                    "id=${it.id}, name=${it.name}, color=${it.color}, isActive=${it.isActive}"
                }.ifEmpty { "(空)" })
                DetailRow("startTime", cell.startTime?.format(DateTimeFormatter.ofPattern("HH:mm:ss")) ?: "(空)")
                DetailRow("startEpochMs", epochToMsString(cell.startEpochMs))
                DetailRow("endTime", cell.endTime?.format(DateTimeFormatter.ofPattern("HH:mm:ss")) ?: "(空)")
                DetailRow("endEpochMs", epochToMsString(cell.endEpochMs))
                DetailRow("estimatedDuration", cell.estimatedDuration?.let {
                    "${formatDuration(it)} (${it}ms)"
                } ?: "(空)")
                DetailRow("actualDuration", cell.actualDuration?.let {
                    "${formatDuration(it)} (${it}ms)"
                } ?: "(空)")
                DetailRow("durationMs", cell.durationMs?.let {
                    "${formatDuration(it)} (${it}ms)"
                } ?: "(空)")
                DetailRow("achievementLevel", cell.achievementLevel?.toString() ?: "(空)")
                DetailRow("pomodoroCount", "${cell.pomodoroCount}")
                DetailRow("note", cell.note.let { it ?: "(空)" })
            }
        },
        dismissButton = {
            TextButton(onClick = {
                clipboardManager.setText(AnnotatedString(buildExportText()))
                Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
            }) {
                Text("导出到剪贴板")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(0.6f),
        )
    }
}
