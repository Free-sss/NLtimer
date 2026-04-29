package com.nltimer.feature.home.ui.sheet

import androidx.compose.runtime.Composable
import com.nltimer.core.designsystem.form.FormRow
import com.nltimer.core.designsystem.form.FormSection
import com.nltimer.core.designsystem.form.FormSpec
import com.nltimer.core.designsystem.form.GenericFormDialog

private val addTagSpec = FormSpec(
    title = "添加标签",
    submitLabel = "添加标签",
    sections = listOf(
        FormSection(
            rows = listOf(
                FormRow.IconColor(iconKey = "icon", colorKey = "color", initialEmoji = "🏷️"),
            ),
        ),
        FormSection(
            rows = listOf(
                FormRow.TextInput(key = "name", label = "名称", placeholder = "标签名称"),
            ),
        ),
    ),
)

@Composable
fun AddTagDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit,
) {
    GenericFormDialog(
        spec = addTagSpec,
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
