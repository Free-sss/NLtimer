package com.nltimer.feature.management_activities.ui.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 通用确认弹窗
 *
 * 用于删除等需要用户二次确认的操作场景。
 *
 * @param title 弹窗标题
 * @param message 确认提示信息
 * @param confirmText 确认按钮文本，默认为"确定"
 * @param onDismiss 取消/关闭回调
 * @param onConfirm 确认回调
 * @param modifier 修饰符
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "确定",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
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
                Text("取消")
            }
        },
        modifier = modifier,
    )
}
