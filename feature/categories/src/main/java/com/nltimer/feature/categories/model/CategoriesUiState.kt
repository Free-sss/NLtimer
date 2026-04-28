package com.nltimer.feature.categories.model

data class CategoriesUiState(
    val activityCategories: List<String> = emptyList(),
    val tagCategories: List<String> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val dialogState: DialogState? = null,
)

sealed interface DialogState {
    data class AddActivityCategory(val sectionType: SectionType) : DialogState
    data class AddTagCategory(val sectionType: SectionType) : DialogState
    data class RenameActivityCategory(
        val oldName: String,
        val sectionType: SectionType,
    ) : DialogState
    data class RenameTagCategory(
        val oldName: String,
        val sectionType: SectionType,
    ) : DialogState
    data class DeleteActivityCategory(
        val category: String,
        val sectionType: SectionType,
    ) : DialogState
    data class DeleteTagCategory(
        val category: String,
        val sectionType: SectionType,
    ) : DialogState
}

enum class SectionType { ACTIVITY, TAG }
