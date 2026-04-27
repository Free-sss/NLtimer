package com.nltimer.feature.categories.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.core.data.SettingsPrefs
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private fun mergeAndSort(dbCategories: List<String>, addedCategories: Set<String>): List<String> =
    (dbCategories + addedCategories).distinct().sorted()

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val settingsPrefs: SettingsPrefs,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _renameConflict = MutableStateFlow<String?>(null)
    val renameConflict: StateFlow<String?> = _renameConflict.asStateFlow()

    private val _dialogState = MutableStateFlow<DialogState?>(null)

    private val _addedActivityCategories = MutableStateFlow<Set<String>>(emptySet())
    private val _addedTagCategories = MutableStateFlow<Set<String>>(emptySet())

    init {
        viewModelScope.launch {
            _addedActivityCategories.value = settingsPrefs.getSavedActivityCategories().first()
            _addedTagCategories.value = settingsPrefs.getSavedTagCategories().first()
        }
    }

    val uiState: StateFlow<CategoriesUiState> = combine(
        categoryRepository.getDistinctActivityCategories(),
        categoryRepository.getDistinctTagCategories(),
        _searchQuery,
        _dialogState,
        combine(_addedActivityCategories, _addedTagCategories, ::Pair),
    ) { activityCats, tagCats, query, dialog, (addedActivity, addedTag) ->
        val mergedActivity = mergeAndSort(activityCats, addedActivity)
        val mergedTag = mergeAndSort(tagCats, addedTag)
        CategoriesUiState(
            activityCategories = if (query.isBlank()) mergedActivity
                else mergedActivity.filter { it.contains(query, ignoreCase = true) },
            tagCategories = if (query.isBlank()) mergedTag
                else mergedTag.filter { it.contains(query, ignoreCase = true) },
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
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            dismissDialog()
            return
        }
        viewModelScope.launch {
            when (sectionType) {
                SectionType.ACTIVITY -> {
                    val updated = _addedActivityCategories.value + trimmed
                    _addedActivityCategories.value = updated
                    settingsPrefs.saveActivityCategories(updated)
                }
                SectionType.TAG -> {
                    val updated = _addedTagCategories.value + trimmed
                    _addedTagCategories.value = updated
                    settingsPrefs.saveTagCategories(updated)
                }
            }
            dismissDialog()
        }
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
                SectionType.ACTIVITY -> {
                    if (oldName in _addedActivityCategories.value) {
                        val updated = _addedActivityCategories.value - oldName + newName
                        _addedActivityCategories.value = updated
                        settingsPrefs.saveActivityCategories(updated)
                    }
                    categoryRepository.renameActivityCategory(oldName, newName)
                }
                SectionType.TAG -> {
                    if (oldName in _addedTagCategories.value) {
                        val updated = _addedTagCategories.value - oldName + newName
                        _addedTagCategories.value = updated
                        settingsPrefs.saveTagCategories(updated)
                    }
                    categoryRepository.renameTagCategory(oldName, newName)
                }
            }
            dismissDialog()
        }
    }

    fun confirmDeleteCategory(sectionType: SectionType, category: String) {
        viewModelScope.launch {
            when (sectionType) {
                SectionType.ACTIVITY -> {
                    val updated = _addedActivityCategories.value - category
                    _addedActivityCategories.value = updated
                    settingsPrefs.saveActivityCategories(updated)
                    categoryRepository.resetActivityCategory(category)
                }
                SectionType.TAG -> {
                    val updated = _addedTagCategories.value - category
                    _addedTagCategories.value = updated
                    settingsPrefs.saveTagCategories(updated)
                    categoryRepository.resetTagCategory(category)
                }
            }
            dismissDialog()
        }
    }

    fun clearConflict() {
        _renameConflict.value = null
    }
}
