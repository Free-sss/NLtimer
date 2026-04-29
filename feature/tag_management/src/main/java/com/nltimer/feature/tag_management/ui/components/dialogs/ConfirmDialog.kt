package com.nltimer.feature.tag_management.ui.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * 通用确认对话框
 *
 * 用于删除等需要二次确认的操作。
 *
 * @param title 对话框标题
 * @param message 确认提示信息
 * @param confirmText 确认按钮文字，默认"确定"
 * @param dismissText 取消按钮文字，默认"取消"
 * @param onDismiss 取消回调
 * @param onConfirm 确认回调
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "确定",
    dismissText: String = "取消",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
    )
}
