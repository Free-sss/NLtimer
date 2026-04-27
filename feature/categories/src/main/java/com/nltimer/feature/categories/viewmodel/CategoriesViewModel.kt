package com.nltimer.feature.categories.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.core.data.repository.CategoryRepository
import com.nltimer.feature.categories.model.CategoriesUiState
import com.nltimer.feature.categories.model.DialogState
import com.nltimer.feature.categories.model.SectionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _renameConflict = MutableStateFlow<String?>(null)
    val renameConflict: StateFlow<String?> = _renameConflict.asStateFlow()

    private val _dialogState = MutableStateFlow<DialogState?>(null)

    val uiState: StateFlow<CategoriesUiState> = combine(
        categoryRepository.getDistinctActivityCategories(),
        categoryRepository.getDistinctTagCategories(),
        _searchQuery,
        _dialogState,
    ) { activityCats, tagCats, query, dialog ->
        CategoriesUiState(
            activityCategories = if (query.isBlank()) activityCats
                else activityCats.filter { it.contains(query, ignoreCase = true) },
            tagCategories = if (query.isBlank()) tagCats
                else tagCats.filter { it.contains(query, ignoreCase = true) },
            searchQuery = query,
            isLoading = false,
            dialogState = dialog,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = CategoriesUiState(),
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onAddCategory(sectionType: SectionType) {
        _dialogState.value = when (sectionType) {
            SectionType.ACTIVITY -> DialogState.AddActivityCategory(sectionType)
            SectionType.TAG -> DialogState.AddTagCategory(sectionType)
        }
    }

    fun onRenameCategory(sectionType: SectionType, oldName: String) {
        _dialogState.value = when (sectionType) {
            SectionType.ACTIVITY -> DialogState.RenameActivityCategory(oldName, sectionType)
            SectionType.TAG -> DialogState.RenameTagCategory(oldName, sectionType)
        }
    }

    fun onDeleteCategory(sectionType: SectionType, category: String) {
        _dialogState.value = when (sectionType) {
            SectionType.ACTIVITY -> DialogState.DeleteActivityCategory(category, sectionType)
            SectionType.TAG -> DialogState.DeleteTagCategory(category, sectionType)
        }
    }

    fun dismissDialog() {
        _dialogState.value = null
        _renameConflict.value = null
    }

    fun confirmAddCategory(sectionType: SectionType, name: String) {
        dismissDialog()
    }

    fun confirmRenameCategory(sectionType: SectionType, oldName: String, newName: String) {
        if (oldName == newName) {
            dismissDialog()
            return
        }

        val currentState = uiState.value
        val conflict = when (sectionType) {
            SectionType.ACTIVITY -> currentState.activityCategories.any {
                it.equals(newName, ignoreCase = true)
            }
            SectionType.TAG -> currentState.tagCategories.any {
                it.equals(newName, ignoreCase = true)
            }
        }

        if (conflict) {
            _renameConflict.value = newName
            return
        }

        viewModelScope.launch {
            when (sectionType) {
                SectionType.ACTIVITY -> categoryRepository.renameActivityCategory(oldName, newName)
                SectionType.TAG -> categoryRepository.renameTagCategory(oldName, newName)
            }
            dismissDialog()
        }
    }

    fun confirmDeleteCategory(sectionType: SectionType, category: String) {
        viewModelScope.launch {
            when (sectionType) {
                SectionType.ACTIVITY -> categoryRepository.resetActivityCategory(category)
                SectionType.TAG -> categoryRepository.resetTagCategory(category)
            }
            dismissDialog()
        }
    }

    fun clearConflict() {
        _renameConflict.value = null
    }
}
