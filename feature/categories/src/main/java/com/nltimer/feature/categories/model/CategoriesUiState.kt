package com.nltimer.feature.categories.model

/**
 * UI state for the categories management screen.
 * Holds the list of activity/tag categories, current search query, loading flag, and dialog state.
 */
data class CategoriesUiState(
    val activityCategories: List<String> = emptyList(),
    val tagCategories: List<String> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val dialogState: DialogState? = null,
)

/**
 * Sealed interface representing the various dialog states on the categories screen.
 */
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

/**
 * Enum distinguishing the two category types: ACTIVITY and TAG.
 */
enum class SectionType { ACTIVITY, TAG }
