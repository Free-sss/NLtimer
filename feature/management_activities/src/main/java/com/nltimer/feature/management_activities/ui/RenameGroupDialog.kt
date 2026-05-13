package com.nltimer.feature.management_activities.ui

import androidx.compose.runtime.Composable
import com.nltimer.core.designsystem.component.TextInputDialog

@Composable
fun RenameGroupDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    TextInputDialog(
        title = "重命名分组",
        label = "新名称",
        initialValue = currentName,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        enableCondition = { newName, old -> newName != old },
    )
}
