package com.nltimer.feature.tag_management.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.core.data.model.Tag
import com.nltimer.core.data.repository.TagRepository
import com.nltimer.feature.tag_management.model.CategoryWithTags
import com.nltimer.feature.tag_management.model.DialogState
import com.nltimer.feature.tag_management.model.TagManagementUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagManagementViewModel @Inject constructor(
    private val tagRepository: TagRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagManagementUiState())
    val uiState: StateFlow<TagManagementUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        combine(
            tagRepository.getAllActive(),
            tagRepository.getDistinctCategories(),
        ) { allTags, categories ->
            val uncategorizedTags = allTags.filter { it.category.isNullOrBlank() }
            val categorizedTags = allTags.filter { !it.category.isNullOrBlank() }

            val categoriesWithTags = categories.map { categoryName ->
                CategoryWithTags(
                    categoryName = categoryName,
                    tags = categorizedTags.filter { it.category == categoryName },
                )
            }.filter { it.tags.isNotEmpty() }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    uncategorizedTags = uncategorizedTags,
                    categories = categoriesWithTags,
                )
            }
        }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun showAddTagDialog(category: String? = null) {
        _uiState.update { it.copy(dialogState = DialogState.AddTag(category)) }
    }

    fun showEditTagDialog(tag: Tag) {
        _uiState.update { it.copy(dialogState = DialogState.EditTag(tag)) }
    }

    fun showDeleteTagDialog(tag: Tag) {
        _uiState.update { it.copy(dialogState = DialogState.DeleteTag(tag)) }
    }

    fun showMoveTagDialog(tag: Tag, currentCategory: String?) {
        _uiState.update { it.copy(dialogState = DialogState.MoveTag(tag, currentCategory)) }
    }

    fun showAddCategoryDialog() {
        _uiState.update { it.copy(dialogState = DialogState.AddCategory) }
    }

    fun showRenameCategoryDialog(name: String) {
        _uiState.update { it.copy(dialogState = DialogState.RenameCategory(name)) }
    }

    fun showDeleteCategoryDialog(name: String, tagCount: Int) {
        _uiState.update { it.copy(dialogState = DialogState.DeleteCategory(name, tagCount)) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(dialogState = null) }
    }

    fun addTag(name: String, color: Long?, textColor: Long?, icon: String?, category: String?) {
        viewModelScope.launch {
            val tag = Tag(
                id = 0,
                name = name,
                color = color,
                textColor = textColor,
                icon = icon,
                category = category,
                priority = 0,
                usageCount = 0,
                sortOrder = 0,
                isArchived = false,
            )
            tagRepository.insert(tag)
            dismissDialog()
        }
    }

    fun updateTag(tag: Tag) {
        viewModelScope.launch {
            tagRepository.update(tag)
            dismissDialog()
        }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            tagRepository.setArchived(tag.id, true)
            dismissDialog()
        }
    }

    fun moveTagToCategory(tagId: Long, newCategory: String?) {
        viewModelScope.launch {
            val updatedTag = tagRepository.getById(tagId)?.copy(category = newCategory)
            if (updatedTag != null) {
                tagRepository.update(updatedTag)
            }
            dismissDialog()
        }
    }

    fun addCategory(name: String) {
        dismissDialog()
    }

    fun renameCategory(oldName: String, newName: String) {
        viewModelScope.launch {
            tagRepository.renameCategory(oldName, newName)
            dismissDialog()
        }
    }

    fun deleteCategory(name: String) {
        viewModelScope.launch {
            tagRepository.resetCategory(name)
            dismissDialog()
        }
    }
}
