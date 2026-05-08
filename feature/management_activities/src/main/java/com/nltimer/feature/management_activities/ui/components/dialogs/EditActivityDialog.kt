package com.nltimer.feature.management_activities.ui.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.nltimer.core.designsystem.icon.IconPickerSheet
import com.nltimer.core.designsystem.icon.IconRenderer
import com.nltimer.core.designsystem.theme.appOutlinedTextFieldColors
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup

/**
 * 编辑活动弹窗
 *
 * 预填当前活动信息，支持修改名称、图标和所属分组。
 *
 * @param activity 待编辑的活动
 * @param allGroups 全部分组列表
 * @param onDismiss 关闭弹窗回调
 * @param onConfirm 确认编辑回调，参数为更新后的 Activity 对象
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActivityDialog(
    activity: Activity,
    allGroups: List<ActivityGroup>,
    onDismiss: () -> Unit,
    onConfirm: (Activity) -> Unit,
) {
    // 以 activity.id 为 key，切换活动时重置状态
    var name by remember(activity.id) { mutableStateOf(activity.name) }
    var iconKey by remember(activity.id) { mutableStateOf(activity.iconKey ?: "") }
    var selectedGroupId by remember(activity.id) { mutableStateOf(activity.groupId) }
    var groupName by remember(activity.id) {
        mutableStateOf(
            allGroups.find { it.id == activity.groupId }?.name ?: "未分类"
        )
    }
    var expanded by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑活动") },
        text = {
            Column {
                // 活动名称编辑框
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("活动名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = appOutlinedTextFieldColors(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconRenderer(
                        iconKey = iconKey.ifBlank { null },
                        defaultEmoji = "📌",
                        iconSize = 24.dp,
                        modifier = Modifier.clickable { showIconPicker = true },
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("图标", style = MaterialTheme.typography.bodyMedium)
                }

                if (showIconPicker) {
                    IconPickerSheet(
                        currentIconKey = iconKey.ifBlank { null },
                        onIconSelected = { newKey ->
                            iconKey = newKey ?: ""
                            showIconPicker = false
                        },
                        onDismiss = { showIconPicker = false },
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 分组选择下拉框，支持切换到其他分组或取消分类
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
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
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
            // 构建更新后的 Activity 对象并回调
            TextButton(
                onClick = {
                    onConfirm(
                        activity.copy(
                            name = name.trim(),
                            iconKey = iconKey.ifBlank { null },
                            groupId = selectedGroupId,
                        ),
                    )
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
