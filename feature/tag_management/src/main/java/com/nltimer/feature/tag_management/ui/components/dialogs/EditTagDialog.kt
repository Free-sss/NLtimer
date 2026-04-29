package com.nltimer.feature.tag_management.ui.components.dialogs

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.nltimer.core.data.model.Tag
import com.nltimer.core.designsystem.form.FormRow
import com.nltimer.core.designsystem.form.FormSection
import com.nltimer.core.designsystem.form.FormSpec
import com.nltimer.core.designsystem.form.GenericFormDialog

private val editTagSpec = FormSpec(
    title = "编辑标签",
    submitLabel = "保存",
    sections = listOf(
        FormSection(
            rows = listOf(
                FormRow.IconColor(iconKey = "icon", colorKey = "color", initialEmoji = "🏷️"),
            ),
        ),
        FormSection(
            rows = listOf(
                FormRow.TextInput(key = "name", label = "名称", placeholder = "请输入标签名"),
                FormRow.NumberInput(key = "priority", label = "优先级", initialValue = 0, range = 0..99),
            ),
        ),
        FormSection(
            rows = listOf(
                FormRow.LabelAction(key = "category", label = "所属分类", actionText = "未分类"),
            ),
        ),
        FormSection(
            rows = listOf(
                FormRow.Switch(key = "isArchived", label = "归档"),
            ),
        ),
    ),
)

@Composable
fun EditTagDialog(
    tag: Tag,
    onDismiss: () -> Unit,
    onConfirm: (Tag) -> Unit,
    onDelete: () -> Unit = {},
) {
    val categoryName = tag.category ?: "未分类"

    val specWithCategory = editTagSpec.copy(
        sections = editTagSpec.sections.map { section ->
            section.copy(
                rows = section.rows.map { row ->
                    if (row is FormRow.LabelAction && row.key == "category") {
                        row.copy(actionText = categoryName)
                    } else row
                },
            )
        },
    )

    val initialData = mapOf(
        "icon" to (tag.icon ?: "🏷️"),
        "color" to (tag.color?.let { (it and 0xFFFFFFFF.toLong()).toString(16) } ?: ""),
        "name" to tag.name,
        "priority" to tag.priority.toString(),
        "isArchived" to tag.isArchived.toString(),
    )

    GenericFormDialog(
        spec = specWithCategory,
        initialData = initialData,
        onDismiss = onDismiss,
        onSubmit = { formState ->
            val name = formState["name"]?.trim() ?: tag.name
            val icon = formState["icon"]?.trim()?.ifBlank { null }
            val colorHex = formState["color"]?.trim()?.ifBlank { null }
            val priority = formState["priority"]?.toIntOrNull() ?: tag.priority
            val isArchived = formState["isArchived"]?.toBooleanStrictOrNull() ?: tag.isArchived
            val color = colorHex?.let {
                try { it.toLong(16).or(0xFF000000.toLong()) } catch (_: Exception) { null }
            }
            onConfirm(
                tag.copy(
                    name = name,
                    icon = icon,
                    color = color,
                    priority = priority,
                    isArchived = isArchived,
                ),
            )
        },
        trailing = {
            TextButton(
                onClick = {
                    onDismiss()
                    onDelete()
                },
            ) {
                Text("删除标签")
            }
        },
    )
}
