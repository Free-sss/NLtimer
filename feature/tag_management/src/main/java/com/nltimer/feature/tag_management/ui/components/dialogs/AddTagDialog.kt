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
import com.nltimer.core.designsystem.theme.appOutlinedTextFieldColors

/**
 * 添加标签对话框
 *
 * 输入标签名称，可选择指定归属分类。
 *
 * @param initialCategory 初始分类，如果非空则标签将添加到该分类
 * @param onDismiss 关闭对话框回调
 * @param onConfirm 确认回调，参数为标签名称和分类名
 */
@Composable
fun AddTagDialog(
    initialCategory: String?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String?) -> Unit,
) {
    // 标签名称输入状态
    var tagName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建标签") },
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

                // 如果在分类内添加，显示归属提示
                if (!initialCategory.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "将添加到「$initialCategory」分类",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            // 名称非空时才允许确认
            TextButton(
                onClick = {
                    if (tagName.isNotBlank()) {
                        onConfirm(tagName.trim(), initialCategory)
                    }
                },
                enabled = tagName.isNotBlank(),
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
