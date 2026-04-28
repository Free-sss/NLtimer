package com.nltimer.feature.tag_management.model

import com.nltimer.core.data.model.Tag

data class TagManagementUiState(
    val uncategorizedTags: List<Tag> = emptyList(),
    val categories: List<CategoryWithTags> = emptyList(),
    val isLoading: Boolean = true,
    val dialogState: DialogState? = null,
)

data class CategoryWithTags(
    val categoryName: String,
    val tags: List<Tag>,
)

sealed interface DialogState {
    data class AddTag(val category: String? = null) : DialogState
    data class EditTag(val tag: Tag) : DialogState
    data class DeleteTag(val tag: Tag) : DialogState
    data class MoveTag(val tag: Tag, val currentCategory: String?) : DialogState
    object AddCategory : DialogState
    data class RenameCategory(val name: String) : DialogState
    data class DeleteCategory(val name: String, val tagCount: Int) : DialogState
}
