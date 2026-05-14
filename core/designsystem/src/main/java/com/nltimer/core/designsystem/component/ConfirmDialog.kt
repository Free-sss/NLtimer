package com.nltimer.core.designsystem.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * 通用确认对话框
 *
 * 用于删除等需要用户二次确认的操作场景。
 *
 * @param title 对话框标题
 * @param message 确认提示信息
 * @param confirmText 确认按钮文本，默认"确定"
 * @param dismissText 取消按钮文本，默认"取消"
 * @param onDismiss 取消回调
 * @param onConfirm 确认回调
 * @param modifier 修饰符
 * @param confirmTextColor 确认按钮文字颜色，可用于删除等破坏性操作的红色高亮
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    confirmText: String = "确定",
    dismissText: String = "取消",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmTextColor: Color? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = confirmTextColor ?: Color.Unspecified,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        modifier = modifier,
    )
}
