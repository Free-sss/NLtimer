package com.nltimer.feature.home.ui.sheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.nltimer.core.designsystem.form.ActivityFormSpecs
import com.nltimer.core.designsystem.form.GenericFormDialog
import com.nltimer.core.designsystem.theme.NLtimerTheme

@Composable
fun AddActivityDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, emoji: String) -> Unit,
) {
    GenericFormDialog(
        spec = ActivityFormSpecs.createActivity,
        initialData = null,
        onDismiss = onDismiss,
        onSubmit = { formState ->
            val name = formState["name"]?.trim() ?: ""
            val emoji = formState["icon"]?.trim() ?: ""
            if (name.isNotBlank()) {
                onConfirm(name, emoji)
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun AddActivityDialogPreview() {
    NLtimerTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            AddActivityDialog(
                onDismiss = {},
                onConfirm = { _, _ -> }
            )
        }
    }
}
