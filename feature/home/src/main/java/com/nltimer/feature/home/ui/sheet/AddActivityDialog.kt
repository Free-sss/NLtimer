package com.nltimer.feature.home.ui.sheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.nltimer.core.designsystem.form.FormRow
import com.nltimer.core.designsystem.form.FormSection
import com.nltimer.core.designsystem.form.FormSpec
import com.nltimer.core.designsystem.form.GenericFormDialog
import com.nltimer.core.designsystem.theme.NLtimerTheme

private val addActivitySpec = FormSpec(
    title = "添加活动",
    submitLabel = "添加活动",
    sections = listOf(
        FormSection(
            rows = listOf(
                FormRow.IconColor(iconKey = "icon", colorKey = "color", initialEmoji = "📖"),
            ),
        ),
        FormSection(
            rows = listOf(
                FormRow.TextInput(key = "name", label = "名称", placeholder = "请输入活动名"),
                FormRow.TextInput(key = "note", label = "备注", placeholder = "请输入"),
            ),
        ),
    ),
)

@Composable
fun AddActivityDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, emoji: String) -> Unit,
) {
    GenericFormDialog(
        spec = addActivitySpec,
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
