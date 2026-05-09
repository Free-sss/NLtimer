package com.nltimer.feature.behavior_management.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nltimer.feature.behavior_management.model.DuplicateHandling
import com.nltimer.feature.behavior_management.model.ImportNewItem
import com.nltimer.feature.behavior_management.model.ImportPreview
import com.nltimer.feature.behavior_management.model.ImportPreviewItem
import com.nltimer.feature.behavior_management.model.NewItemType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPreviewDialog(
    preview: ImportPreview,
    onDuplicateHandlingChange: (DuplicateHandling) -> Unit,
    selectedHandling: DuplicateHandling,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 8.dp, top = 24.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "导入预览",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "关闭",
                        )
                    }
                }

                HorizontalDivider()

                Text(
                    text = "共 ${preview.totalCount} 条 · 重复 ${preview.duplicateCount} 条 · 新建 ${preview.newCount}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                )

                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (preview.duplicateItems.isNotEmpty()) {
                        Text(
                            text = "⚠ 重复项:",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = MaterialTheme.colorScheme.error,
                            ),
                        )
                        preview.duplicateItems.forEach { item ->
                            Text(
                                text = "· ${formatDuplicateItem(item)}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }

                    if (preview.newItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✚ 需新建:",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                            ),
                        )
                        preview.newItems.forEach { item ->
                            Text(
                                text = "· ${formatNewItem(item)}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }

                HorizontalDivider()

                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                    Text(
                        text = "处理方式:",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    DuplicateHandlingOption(
                        label = "跳过重复项（推荐）",
                        selected = selectedHandling == DuplicateHandling.SKIP,
                        onClick = { onDuplicateHandlingChange(DuplicateHandling.SKIP) },
                    )
                    DuplicateHandlingOption(
                        label = "覆盖重复项",
                        selected = selectedHandling == DuplicateHandling.OVERWRITE,
                        onClick = { onDuplicateHandlingChange(DuplicateHandling.OVERWRITE) },
                    )
                    DuplicateHandlingOption(
                        label = "全部导入（允许重复）",
                        selected = selectedHandling == DuplicateHandling.ALLOW,
                        onClick = { onDuplicateHandlingChange(DuplicateHandling.ALLOW) },
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onConfirm) {
                        Text("确认导入", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DuplicateHandlingOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

@Composable
fun ExportConfirmDialog(
    behaviorCount: Int,
    filterDescription: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确认导出", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        title = { Text("导出确认") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("将导出 ${behaviorCount} 条行为")
                Text(
                    text = filterDescription,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        },
        modifier = modifier,
    )
}

private fun formatDuplicateItem(item: ImportPreviewItem): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val startTime = Instant.ofEpochMilli(item.startTime)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(formatter)
    val endTime = item.endTime?.let {
        Instant.ofEpochMilli(it)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(formatter)
    }
    val timeRange = if (endTime != null) "$startTime-$endTime" else startTime
    return "${item.activityName} $timeRange"
}

private fun formatNewItem(item: ImportNewItem): String {
    val typeLabel = when (item.type) {
        NewItemType.ACTIVITY -> "活动"
        NewItemType.TAG -> "标签"
    }
    return "$typeLabel: \"${item.name}\""
}
