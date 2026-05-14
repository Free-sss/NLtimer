package com.nltimer.feature.management_activities.ui.components.dialogs

import androidx.compose.runtime.Composable
import com.nltimer.core.designsystem.component.TextInputDialog

@Composable
fun AddGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    TextInputDialog(
        title = "新建分组",
        label = "分组名称",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}
