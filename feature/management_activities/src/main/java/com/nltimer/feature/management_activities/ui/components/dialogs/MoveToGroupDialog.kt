package com.nltimer.feature.management_activities.ui.components.dialogs

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
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
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup

/**
 * 移动活动到分组弹窗
 *
 * 将指定活动移动到目标分组，支持移回"未分类"。
 *
 * @param activity 待移动的活动
 * @param allGroups 全部分组列表
 * @param onDismiss 关闭弹窗回调
 * @param onConfirm 确认移动回调，参数为目标分组 ID（null 表示未分类）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveToGroupDialog(
    activity: Activity,
    allGroups: List<ActivityGroup>,
    onDismiss: () -> Unit,
    onConfirm: (Long?) -> Unit,
) {
    // 当前选中的目标分组，默认为活动所属分组
    var selectedGroupId by remember { mutableStateOf(activity.groupId) }
    var groupName by remember {
        mutableStateOf(
            allGroups.find { it.id == activity.groupId }?.name ?: "未分类"
        )
    }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("移动活动到分组") },
        text = {
            Text(text = "将「${activity.name}」移动到：")

            Spacer(modifier = Modifier.height(12.dp))

            // 分组选择下拉框
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    // 移回未分类
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
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedGroupId) }) {
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
