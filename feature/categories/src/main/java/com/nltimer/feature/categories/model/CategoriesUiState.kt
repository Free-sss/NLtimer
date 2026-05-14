package com.nltimer.feature.categories.model

import androidx.compose.runtime.Immutable

@Immutable
data class CategoriesUiState(
    val groups: List<CategoryGroup> = emptyList(),
    val expandedGroupIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
    val dialogState: DialogState? = null,
)

data class CategoryGroup(
    val id: Long,
    val name: String,
    val type: SectionType,
    val items: List<CategoryItem> = emptyList(),
)

data class CategoryItem(
    val name: String,
)

sealed interface DialogState {
    data class AddCategory(val sectionType: SectionType) : DialogState
    data class RenameCategory(
        val oldName: String,
        val sectionType: SectionType,
    ) : DialogState
    data class DeleteCategory(
        val category: String,
        val sectionType: SectionType,
    ) : DialogState
}

enum class SectionType { ACTIVITY, TAG }
