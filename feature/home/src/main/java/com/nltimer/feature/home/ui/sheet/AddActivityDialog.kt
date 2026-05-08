package com.nltimer.feature.home.ui.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.model.Tag
import com.nltimer.core.designsystem.component.MultiSelectPickerPopup
import com.nltimer.core.designsystem.component.SingleSelectPickerPopup
import com.nltimer.core.designsystem.form.ActivityFormSpecs
import com.nltimer.core.designsystem.form.FormRow
import com.nltimer.core.designsystem.form.GenericFormSheet
import com.nltimer.core.designsystem.form.parseColorHex

@Composable
fun AddActivityDialog(
    allGroups: List<ActivityGroup>,
    allTags: List<Tag>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, iconKey: String?, color: Long?, groupId: Long?, keywords: String?, tagIds: List<Long>) -> Unit,
) {
    var selectedGroupId by remember { mutableStateOf(null as Long?) }
    var selectedTagIds by remember { mutableStateOf(emptySet<Long>()) }
    var showGroupPicker by remember { mutableStateOf(false) }
    var showTagPicker by remember { mutableStateOf(false) }

    val groupName = allGroups.find { it.id == selectedGroupId }?.name ?: "未分类"
    val tagCountText = if (selectedTagIds.isEmpty()) "+ 增加" else "${selectedTagIds.size} 个标签"
    val groupItems = listOf(null to "未分类") + allGroups.map { it.id to it.name }

    // DIFF: 复杂多字段变更，无法用 withUpdatedLabelAction 简化
    val specWithCategory = ActivityFormSpecs.createActivity.copy(
        sections = ActivityFormSpecs.createActivity.sections.map { section ->
            section.copy(
                rows = section.rows.map { row ->
                    when {
                        row is FormRow.LabelAction && row.key == "category" -> row.copy(
                            actionText = groupName,
                            onClick = { showGroupPicker = true },
                        )
                        row is FormRow.LabelAction && row.key == "tags" -> row.copy(
                            actionText = tagCountText,
                            onClick = { showTagPicker = true },
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
            val iconKey = formState["icon"]?.trim()?.ifBlank { null }
            val colorHex = formState["color"]?.trim()?.ifBlank { null }
            val keywords = formState["keywords"]?.trim()?.ifBlank { null }
            val color = parseColorHex(colorHex)
            if (name.isNotBlank()) {
                onConfirm(name, iconKey, color, selectedGroupId, keywords, selectedTagIds.toList())
            }
        },
        overlay = {
            if (showGroupPicker) {
                SingleSelectPickerPopup(
                    title = "选择所属分组",
                    items = groupItems,
                    selectedId = selectedGroupId,
                    onSelected = { selectedGroupId = it },
                    onDismiss = { showGroupPicker = false },
                )
            }
            if (showTagPicker) {
                MultiSelectPickerPopup(
                    title = "关联标签",
                    items = allTags,
                    label = { it.name },
                    selectedIds = selectedTagIds,
                    itemId = { it.id },
                    onSelectionChanged = { selectedTagIds = it },
                    onDismiss = { showTagPicker = false },
                )
            }
        },
    )
}
