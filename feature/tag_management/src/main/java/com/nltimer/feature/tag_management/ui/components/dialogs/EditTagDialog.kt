package com.nltimer.feature.tag_management.ui.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import com.nltimer.core.data.model.Tag
import com.nltimer.core.designsystem.theme.appOutlinedTextFieldColors

/**
 * 编辑标签对话框
 *
 * 修改标签名称，并展示当前归属分类。
 *
 * @param tag 要编辑的标签对象
 * @param onDismiss 关闭对话框回调
 * @param onConfirm 确认保存回调，参数为修改后的 Tag
 */
@Composable
fun EditTagDialog(
    tag: Tag,
    onDismiss: () -> Unit,
    onConfirm: (Tag) -> Unit,
) {
    // 初始化输入框为标签现有名称
    var tagName by remember { mutableStateOf(tag.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑标签") },
        text = {
            Column {
                OutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    label = { Text("标签名称") },
                    singleLine = true,
                    colors = appOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 展示当前分类信息（只读）
                Text(
                    text = "当前所属分类：${tag.category ?: "未分类"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            // 名称非空时才允许保存
            TextButton(
                onClick = {
                    if (tagName.isNotBlank()) {
                        onConfirm(tag.copy(name = tagName.trim()))
                    }
                },
                enabled = tagName.isNotBlank(),
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}
