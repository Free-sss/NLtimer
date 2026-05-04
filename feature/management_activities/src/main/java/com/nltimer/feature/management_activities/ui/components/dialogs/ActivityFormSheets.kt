package com.nltimer.feature.management_activities.ui.components.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.designsystem.form.ActivityFormSpecs
import com.nltimer.core.designsystem.form.FormRow
import com.nltimer.core.designsystem.form.GenericFormSheet

@Composable
fun AddActivityFormSheet(
    allGroups: List<ActivityGroup>,
    initialGroupId: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, emoji: String?, color: Long?, groupId: Long?, note: String?) -> Unit,
) {
    var selectedGroupId by remember(initialGroupId) { mutableStateOf(initialGroupId) }
    var showGroupPicker by remember { mutableStateOf(false) }

    val groupName = allGroups.find { it.id == selectedGroupId }?.name ?: "未分类"

    val specWithCategory = ActivityFormSpecs.createActivity.copy(
        sections = ActivityFormSpecs.createActivity.sections.map { section ->
            section.copy(
                rows = section.rows.map { row ->
                    if (row is FormRow.LabelAction && row.key == "category") {
                        row.copy(
                            actionText = groupName,
                            onClick = { showGroupPicker = true },
                        )
                    } else row
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
            val emoji = formState["icon"]?.trim()?.ifBlank { null }
            val colorHex = formState["color"]?.trim()?.ifBlank { null }
            val note = formState["note"]?.trim()?.ifBlank { null }
            val color = colorHex?.let {
                try { it.toLong(16).or(0xFF000000.toLong()) } catch (_: Exception) { null }
            }
            onConfirm(name, emoji, color, selectedGroupId, note)
        },
        overlay = if (showGroupPicker) {
            {
                GroupPickerPopup(
                    groups = allGroups,
                    selectedId = selectedGroupId,
                    onSelected = { selectedGroupId = it },
                    onDismiss = { showGroupPicker = false },
                )
            }
        } else null,
    )
}

@Composable
fun EditActivityFormSheet(
    activity: Activity,
    allGroups: List<ActivityGroup>,
    onDismiss: () -> Unit,
    onConfirm: (Activity) -> Unit,
) {
    var selectedGroupId by remember(activity.id) { mutableStateOf(activity.groupId) }
    var showGroupPicker by remember { mutableStateOf(false) }

    val groupName = allGroups.find { it.id == selectedGroupId }?.name ?: "未分类"

    val specWithCategory = ActivityFormSpecs.editActivity().copy(
        sections = ActivityFormSpecs.editActivity().sections.map { section ->
            section.copy(
                rows = section.rows.map { row ->
                    if (row is FormRow.LabelAction && row.key == "category") {
                        row.copy(
                            actionText = groupName,
                            onClick = { showGroupPicker = true },
                        )
                    } else row
                },
            )
        },
    )

    val initialData = mapOf(
        "icon" to (activity.emoji ?: "📖"),
        "color" to (activity.color?.let { (it and 0xFFFFFFFF.toLong()).toString(16) } ?: ""),
        "name" to activity.name,
        "note" to "",
        "isArchived" to activity.isArchived.toString(),
    )

    GenericFormSheet(
        spec = specWithCategory,
        initialData = initialData,
        onDismiss = onDismiss,
        onSubmit = { formState ->
            val name = formState["name"]?.trim() ?: activity.name
            val emoji = formState["icon"]?.trim()?.ifBlank { null }
            val colorHex = formState["color"]?.trim()?.ifBlank { null }
            val isArchived = formState["isArchived"]?.toBooleanStrictOrNull() ?: activity.isArchived
            val colorLong = colorHex?.let {
                try { it.toLong(16).or(0xFF000000.toLong()) } catch (_: Exception) { null }
            }
            onConfirm(
                activity.copy(
                    name = name,
                    emoji = emoji,
                    groupId = selectedGroupId,
                    isArchived = isArchived,
                    color = colorLong ?: activity.color,
                ),
            )
        },
        overlay = if (showGroupPicker) {
            {
                GroupPickerPopup(
                    groups = allGroups,
                    selectedId = selectedGroupId,
                    onSelected = { selectedGroupId = it },
                    onDismiss = { showGroupPicker = false },
                )
            }
        } else null,
    )
}
