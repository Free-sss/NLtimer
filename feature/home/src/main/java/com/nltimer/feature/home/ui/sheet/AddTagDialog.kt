package com.nltimer.feature.home.ui.sheet

import androidx.compose.runtime.Composable
import com.nltimer.core.designsystem.form.ActivityFormSpecs
import com.nltimer.core.designsystem.form.GenericFormDialog

@Composable
fun AddTagDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit,
) {
    GenericFormDialog(
        spec = ActivityFormSpecs.createTag,
        initialData = null,
        onDismiss = onDismiss,
        onSubmit = { formState ->
            val name = formState["name"]?.trim() ?: ""
            if (name.isNotBlank()) {
                onConfirm(name)
            }
        },
    )
}
