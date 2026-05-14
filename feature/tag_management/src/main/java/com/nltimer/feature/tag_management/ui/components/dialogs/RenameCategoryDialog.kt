package com.nltimer.feature.tag_management.ui.components.dialogs

import androidx.compose.runtime.Composable
import com.nltimer.core.designsystem.component.TextInputDialog

@Composable
fun RenameCategoryDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (newName: String) -> Unit,
) {
    TextInputDialog(
        title = "重命名分类",
        label = "新名称",
        initialValue = currentName,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        confirmText = "保存",
        enableCondition = { newName, old -> newName.isNotBlank() && newName != old },
    )
}
