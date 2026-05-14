package com.nltimer.feature.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.repository.ImportMode

@Composable
fun ImportModeDialog(
    onConfirm: (ImportMode) -> Unit,
    onDismiss: () -> Unit,
) {
    val selectedMode = remember { mutableStateOf(ImportMode.SMART) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "选择导入模式") },
        text = {
            Column(Modifier.selectableGroup()) {
                ImportModeOption(
                    label = "智能处理",
                    description = "同名数据补全空字段，不同名新增，保留所有现有数据。适合合并数据或回导 AI 处理后的字段。",
                    selected = selectedMode.value == ImportMode.SMART,
                    onClick = { selectedMode.value = ImportMode.SMART },
                )
                ImportModeOption(
                    label = "直接覆盖",
                    description = "清空对应类型的现有数据后写入导入数据。适合设备迁移或全新开始。",
                    selected = selectedMode.value == ImportMode.OVERWRITE,
                    onClick = { selectedMode.value = ImportMode.OVERWRITE },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedMode.value) }) {
                Text(text = "确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "取消")
            }
        },
    )
}

@Composable
private fun ImportModeOption(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
