package com.nltimer.feature.tag_management.ui.components.dialogs

import androidx.compose.runtime.Composable
import com.nltimer.core.designsystem.form.FormRow
import com.nltimer.core.designsystem.form.FormSection
import com.nltimer.core.designsystem.form.FormSpec
import com.nltimer.core.designsystem.form.GenericFormDialog

private val createTagSpec = FormSpec(
    title = "新增标签",
    submitLabel = "新增标签",
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
    ),
)

@Composable
fun AddTagDialog(
    initialCategory: String?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, color: Long?, icon: String?, priority: Int, category: String?) -> Unit,
) {
    val categoryName = initialCategory ?: "未分类"

    val specWithCategory = createTagSpec.copy(
        sections = createTagSpec.sections.map { section ->
            section.copy(
                rows = section.rows.map { row ->
                    if (row is FormRow.LabelAction && row.key == "category") {
                        row.copy(actionText = categoryName)
                    } else row
                },
            )
        },
    )

    GenericFormDialog(
        spec = specWithCategory,
        initialData = null,
        onDismiss = onDismiss,
        onSubmit = { formState ->
            val name = formState["name"]?.trim() ?: ""
            val icon = formState["icon"]?.trim()?.ifBlank { null }
            val colorHex = formState["color"]?.trim()?.ifBlank { null }
            val priority = formState["priority"]?.toIntOrNull() ?: 0
            val color = colorHex?.let {
                try { it.toLong(16).or(0xFF000000.toLong()) } catch (_: Exception) { null }
            }
            onConfirm(name, color, icon, priority, initialCategory)
        },
    )
}
