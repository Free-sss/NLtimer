package com.nltimer.feature.tag_management.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.core.data.SettingsPrefs
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 标签管理 ViewModel
 *
 * 负责标签和分类的增删改查业务逻辑，维护 UI 状态流。
 */
@HiltViewModel
class TagManagementViewModel @Inject constructor(
    private val tagRepository: TagRepository,
    private val settingsPrefs: SettingsPrefs,
) : ViewModel() {

    // 内部可变 UI 状态流
    private val _uiState = MutableStateFlow(TagManagementUiState())
    // 对外暴露不可变状态流
    val uiState: StateFlow<TagManagementUiState> = _uiState.asStateFlow()

    // 本地新增的标签分类（用户自行添加但尚无标签归属）
    private val _addedCategories = MutableStateFlow<Set<String>>(emptySet())

    init {
        viewModelScope.launch {
            _addedCategories.value = settingsPrefs.getSavedTagCategories().first()
        }
        loadData()
    }

    /**
     * 加载标签和分类数据
     *
     * 合并两个 Flow（所有标签 + 所有分类），按分类分组后更新 UI 状态。
     */
    private fun loadData() {
        combine(
            tagRepository.getAllActive(),
            tagRepository.getDistinctCategories(),
            _addedCategories,
        ) { allTags, dbCategories, addedCategories ->
            // 分离未分类标签
            val uncategorizedTags = allTags.filter { it.category.isNullOrBlank() }
            // 提取已分类标签
            val categorizedTags = allTags.filter { !it.category.isNullOrBlank() }

            // 合并数据库分类与本地新增分类，去重排序
            val allCategories = (dbCategories.toSet() + addedCategories).toList().sorted()

            // 按分类名分组
            val categoriesWithTags = allCategories.map { categoryName ->
                CategoryWithTags(
                    categoryName = categoryName,
                    tags = categorizedTags.filter { it.category == categoryName },
                )
            }

            // 更新 UI 状态，清除加载标记
            _uiState.update {
                it.copy(
                    isLoading = false,
                    uncategorizedTags = uncategorizedTags,
                    categories = categoriesWithTags,
                )
            }
        }
            // 数据流异常时仅隐藏加载状态
            .catch { e ->
                _uiState.update { it.copy(isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    /** 显示添加标签对话框，可指定初始分类 */
    fun showAddTagDialog(category: String? = null) {
        _uiState.update { it.copy(dialogState = DialogState.AddTag(category)) }
    }

    /** 显示编辑标签对话框 */
    fun showEditTagDialog(tag: Tag) {
        _uiState.update { it.copy(dialogState = DialogState.EditTag(tag)) }
    }

    /** 显示删除标签确认对话框 */
    fun showDeleteTagDialog(tag: Tag) {
        _uiState.update { it.copy(dialogState = DialogState.DeleteTag(tag)) }
    }

    /** 显示移动标签对话框 */
    fun showMoveTagDialog(tag: Tag, currentCategory: String?) {
        _uiState.update { it.copy(dialogState = DialogState.MoveTag(tag, currentCategory)) }
    }

    /** 显示添加分类对话框 */
    fun showAddCategoryDialog() {
        _uiState.update { it.copy(dialogState = DialogState.AddCategory) }
    }

    /** 显示重命名分类对话框 */
    fun showRenameCategoryDialog(name: String) {
        _uiState.update { it.copy(dialogState = DialogState.RenameCategory(name)) }
    }

    /** 显示删除分类确认对话框 */
    fun showDeleteCategoryDialog(name: String, tagCount: Int) {
        _uiState.update { it.copy(dialogState = DialogState.DeleteCategory(name, tagCount)) }
    }

    /** 关闭当前对话框 */
    fun dismissDialog() {
        _uiState.update { it.copy(dialogState = null) }
    }

    /**
     * 添加新标签
     *
     * @param name 标签名称
     * @param color 背景色 ARGB
     * @param textColor 文字色 ARGB
     * @param icon 图标标识
     * @param category 归属分类名
     */
    fun addTag(name: String, color: Long?, textColor: Long?, icon: String?, priority: Int, category: String?) {
        viewModelScope.launch {
            val tag = Tag(
                id = 0, name = name, color = color, textColor = textColor,
                icon = icon, category = category, priority = priority,
                usageCount = 0, sortOrder = 0, isArchived = false,
            )
            tagRepository.insert(tag)
            dismissDialog()
        }
    }

    /** 更新标签信息 */
    fun updateTag(tag: Tag) {
        viewModelScope.launch {
            tagRepository.update(tag)
            dismissDialog()
        }
    }

    /** 软删除标签：标记为 archived */
    fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            tagRepository.setArchived(tag.id, true)
            dismissDialog()
        }
    }

    /** 将标签移动到指定分类（null 表示未分类） */
    fun moveTagToCategory(tagId: Long, newCategory: String?) {
        viewModelScope.launch {
            val updatedTag = tagRepository.getById(tagId)?.copy(category = newCategory)
            if (updatedTag != null) {
                tagRepository.update(updatedTag)
            }
            dismissDialog()
        }
    }

    /** 添加新分类 */
    fun addCategory(name: String) {
        viewModelScope.launch {
            val updated = _addedCategories.value + name.trim()
            _addedCategories.value = updated
            settingsPrefs.saveTagCategories(updated)
            dismissDialog()
        }
    }

    /** 重命名分类 */
    fun renameCategory(oldName: String, newName: String) {
        viewModelScope.launch {
            tagRepository.renameCategory(oldName, newName)
            // 同步更新本地新增集合
            if (oldName in _addedCategories.value) {
                val updated = _addedCategories.value - oldName + newName
                _addedCategories.value = updated
                settingsPrefs.saveTagCategories(updated)
            }
            dismissDialog()
        }
    }

    /** 删除分类：将该分类下所有标签的 category 置空 */
    fun deleteCategory(name: String) {
        viewModelScope.launch {
            tagRepository.resetCategory(name)
            // 从本地新增集合中移除
            val updated = _addedCategories.value - name
            _addedCategories.value = updated
            settingsPrefs.saveTagCategories(updated)
            dismissDialog()
        }
    }
}
