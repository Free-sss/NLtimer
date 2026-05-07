package com.nltimer.feature.home.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.util.formatDuration
import com.nltimer.feature.home.model.GridCellUiState
import java.time.format.DateTimeFormatter

@Composable
fun BehaviorDetailDialog(
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
fun DetailRow(label: String, value: String) {
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
