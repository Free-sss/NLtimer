package com.nltimer.feature.management_activities.ui.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.theme.appOutlinedTextFieldColors
import com.nltimer.core.data.model.ActivityGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityDialog(
    allGroups: List<ActivityGroup>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, emoji: String?, groupId: Long?) -> Unit,
    initialGroupId: Long? = null,
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }
    var selectedGroupId by remember(initialGroupId) { mutableStateOf(initialGroupId) }
    var groupName by remember(initialGroupId, allGroups) {
        mutableStateOf(if (initialGroupId == null) "未分类" else allGroups.find { it.id == initialGroupId }?.name ?: "未分类")
    }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加活动") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("活动名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = appOutlinedTextFieldColors(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = emoji,
                    onValueChange = {
                        if (it.length <= 2) emoji = it
                    },
                    label = { Text("Emoji (可选)") },
                    singleLine = true,
                    placeholder = { Text("📺") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = appOutlinedTextFieldColors(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("所属分组") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = appOutlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("未分类") },
                            onClick = {
                                selectedGroupId = null
                                groupName = "未分类"
                                expanded = false
                            },
                        )

                        allGroups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name) },
                                onClick = {
                                    selectedGroupId = group.id
                                    groupName = group.name
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(name.trim(), emoji.ifBlank { null }, selectedGroupId)
                },
                enabled = name.isNotBlank(),
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}
