package com.nltimer.feature.tag_management.ui.components.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.Tag
import com.nltimer.core.designsystem.component.CategoryPickerPopup
import com.nltimer.core.designsystem.component.SingleSelectPickerPopup
import com.nltimer.core.designsystem.form.ActivityFormSpecs
import com.nltimer.core.designsystem.form.FormRow
import com.nltimer.core.designsystem.form.GenericFormSheet

@Composable
fun AddTagFormSheet(
    initialCategory: String?,
    categories: List<String>,
    allActivities: List<Activity>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, color: Long?, icon: String?, priority: Int, category: String?, keywords: String?, activityId: Long?) -> Unit,
) {
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var selectedActivityId by remember { mutableStateOf(null as Long?) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showActivityPicker by remember { mutableStateOf(false) }

    val activityName = allActivities.find { it.id == selectedActivityId }?.name
    val activityText = activityName ?: "+ 增加"
    val activityItems = listOf(null to "未关联") + allActivities.map { it.id to it.name }

    val specWithCategory = ActivityFormSpecs.createTag.copy(
        sections = ActivityFormSpecs.createTag.sections.map { section ->
            section.copy(
                rows = section.rows.map { row ->
                    when {
                        row is FormRow.LabelAction && row.key == "category" -> row.copy(
                            actionText = selectedCategory ?: "未分类",
                            onClick = { showCategoryPicker = true },
                        )
                        row is FormRow.LabelAction && row.key == "activities" -> row.copy(
                            actionText = activityText,
                            onClick = { showActivityPicker = true },
                        )
                        else -> row
                    }
                },
            )
        },
    )

    GenericFormSheet(
        spec = specWithCategory,
        initialData = null,
        onDismiss = onDismiss,
        onSubmit = { formState ->
            val name = formState["name"]?.trim() ?: ""
            val icon = formState["icon"]?.trim()?.ifBlank { null }
            val colorHex = formState["color"]?.trim()?.ifBlank { null }
            val priority = formState["priority"]?.toIntOrNull() ?: 0
            val keywords = formState["keywords"]?.trim()?.ifBlank { null }
            val color = colorHex?.let {
                try { it.toULong(16).toLong() } catch (_: Exception) { null }
            }
            onConfirm(name, color, icon, priority, selectedCategory, keywords, selectedActivityId)
        },
        overlay = {
            if (showCategoryPicker) {
                CategoryPickerPopup(
                    categories = categories,
                    selected = selectedCategory,
                    onSelected = { selectedCategory = it },
                    onDismiss = { showCategoryPicker = false },
                )
            }
            if (showActivityPicker) {
                SingleSelectPickerPopup(
                    title = "关联活动",
                    items = activityItems,
                    selectedId = selectedActivityId,
                    onSelected = { selectedActivityId = it },
                    onDismiss = { showActivityPicker = false },
                )
            }
        },
    )
}
