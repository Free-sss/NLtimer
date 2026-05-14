package com.nltimer.feature.categories.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.core.data.SettingsPrefs
import com.nltimer.core.data.repository.CategoryRepository
import com.nltimer.feature.categories.model.CategoriesUiState
import com.nltimer.feature.categories.model.CategoryGroup
import com.nltimer.feature.categories.model.CategoryItem
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

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val settingsPrefs: SettingsPrefs,
) : ViewModel() {

    private val _renameConflict = MutableStateFlow<String?>(null)
    val renameConflict: StateFlow<String?> = _renameConflict.asStateFlow()

    private val _dialogState = MutableStateFlow<DialogState?>(null)

    private val _addedTagCategories = MutableStateFlow<Set<String>>(emptySet())

    private val _expandedGroupIds = MutableStateFlow<Set<Long>>(setOf(GROUP_ID_ACTIVITY, GROUP_ID_TAG))

    init {
        viewModelScope.launch {
            _addedTagCategories.value = settingsPrefs.getSavedTagCategories().first()
        }
    }

    val uiState: StateFlow<CategoriesUiState> = combine(
        categoryRepository.getDistinctActivityCategories(),
        categoryRepository.getDistinctTagCategories(),
        _dialogState,
        _addedTagCategories,
        _expandedGroupIds,
    ) { activityCats, tagCats, dialog, addedTag, expanded ->
        val mergedTag = (tagCats + addedTag).distinct().sorted()
        val groups = listOf(
            CategoryGroup(
                id = GROUP_ID_ACTIVITY,
                name = "活动分类",
                type = SectionType.ACTIVITY,
                items = activityCats.map { CategoryItem(it) },
            ),
            CategoryGroup(
                id = GROUP_ID_TAG,
                name = "标签分类",
                type = SectionType.TAG,
                items = mergedTag.map { CategoryItem(it) },
            ),
        )
        CategoriesUiState(
            groups = groups,
            expandedGroupIds = expanded,
            isLoading = false,
            dialogState = dialog,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoriesUiState(),
    )

    fun toggleGroupExpand(groupId: Long) {
        _expandedGroupIds.value = if (groupId in _expandedGroupIds.value) {
            _expandedGroupIds.value - groupId
        } else {
            _expandedGroupIds.value + groupId
        }
    }

    fun setAllGroupsExpanded(expanded: Boolean) {
        _expandedGroupIds.value = if (expanded) {
            uiState.value.groups.map { it.id }.toSet()
        } else {
            emptySet()
        }
    }

    fun showAddCategoryDialog(sectionType: SectionType) {
        _dialogState.value = DialogState.AddCategory(sectionType)
    }

    fun showRenameCategoryDialog(sectionType: SectionType, oldName: String) {
        _dialogState.value = DialogState.RenameCategory(oldName, sectionType)
    }

    fun showDeleteCategoryDialog(sectionType: SectionType, category: String) {
        _dialogState.value = DialogState.DeleteCategory(category, sectionType)
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
                SectionType.ACTIVITY -> categoryRepository.addActivityCategory(trimmed)
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
        val group = currentState.groups.firstOrNull { it.type == sectionType }
        val conflict = group?.items?.any { it.name.equals(newName, ignoreCase = true) } == true
        if (conflict) {
            _renameConflict.value = newName
            return
        }
        viewModelScope.launch {
            when (sectionType) {
                SectionType.ACTIVITY -> categoryRepository.renameActivityCategory(oldName, newName)
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
                SectionType.ACTIVITY -> categoryRepository.resetActivityCategory(category)
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

    companion object {
        const val GROUP_ID_ACTIVITY = 0L
        const val GROUP_ID_TAG = 1L
    }
}
