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

/**
 * ViewModel for the category management screen.
 * Coordinates categories from [CategoryRepository], local tag additions via [SettingsPrefs],
 * search filtering, and dialog lifecycle.
 */
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val settingsPrefs: SettingsPrefs,
) : ViewModel() {

    // 搜索查询关键字流
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 重命名冲突（目标名称已存在时非空）
    private val _renameConflict = MutableStateFlow<String?>(null)
    val renameConflict: StateFlow<String?> = _renameConflict.asStateFlow()

    // 当前活跃的对话框状态
    private val _dialogState = MutableStateFlow<DialogState?>(null)

    // 本地新增的标签分类（用户自行添加但尚未持久化到远端）
    private val _addedTagCategories = MutableStateFlow<Set<String>>(emptySet())

    init {
        // 初始化时从 SharedPreferences 恢复已保存的标签分类
        viewModelScope.launch {
            _addedTagCategories.value = settingsPrefs.getSavedTagCategories().first()
        }
    }

    // 合并多个数据源生成最终 UI 状态：活动分类、标签分类（含本地新增）、搜索过滤、对话框
    val uiState: StateFlow<CategoriesUiState> = combine(
        categoryRepository.getDistinctActivityCategories(),
        categoryRepository.getDistinctTagCategories(),
        _searchQuery,
        _dialogState,
        _addedTagCategories,
    ) { activityCats, tagCats, query, dialog, addedTag ->
        // 合并远端与本地新增的标签分类，去重并排序
        val mergedTag = (tagCats + addedTag).distinct().sorted()
        CategoriesUiState(
            activityCategories = if (query.isBlank()) activityCats
                else activityCats.filter { it.contains(query, ignoreCase = true) },
            tagCategories = if (query.isBlank()) mergedTag
                else mergedTag.filter { it.contains(query, ignoreCase = true) },
            searchQuery = query,
            isLoading = false,
            dialogState = dialog,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoriesUiState(),
    )

    /** 更新搜索关键字 */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /** 打开新增分类对话框 */
    fun onAddCategory(sectionType: SectionType) {
        _dialogState.value = when (sectionType) {
            SectionType.ACTIVITY -> DialogState.AddActivityCategory(sectionType)
            SectionType.TAG -> DialogState.AddTagCategory(sectionType)
        }
    }

    /** 打开重命名分类对话框 */
    fun onRenameCategory(sectionType: SectionType, oldName: String) {
        _dialogState.value = when (sectionType) {
            SectionType.ACTIVITY -> DialogState.RenameActivityCategory(oldName, sectionType)
            SectionType.TAG -> DialogState.RenameTagCategory(oldName, sectionType)
        }
    }

    /** 打开删除确认对话框 */
    fun onDeleteCategory(sectionType: SectionType, category: String) {
        _dialogState.value = when (sectionType) {
            SectionType.ACTIVITY -> DialogState.DeleteActivityCategory(category, sectionType)
            SectionType.TAG -> DialogState.DeleteTagCategory(category, sectionType)
        }
    }

    /** 关闭对话框，同时清除冲突状态 */
    fun dismissDialog() {
        _dialogState.value = null
        _renameConflict.value = null
    }

    /** 确认新增分类：活动分类写入 Repository，标签分类本地持久化 */
    fun confirmAddCategory(sectionType: SectionType, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            dismissDialog()
            return
        }
        viewModelScope.launch {
            when (sectionType) {
                SectionType.ACTIVITY -> {
                    categoryRepository.addActivityCategory(trimmed)
                }
                SectionType.TAG -> {
                    // 本地维护新增标签集合并持久化到 SharedPreferences
                    val updated = _addedTagCategories.value + trimmed
                    _addedTagCategories.value = updated
                    settingsPrefs.saveTagCategories(updated)
                }
            }
            dismissDialog()
        }
    }

    /** 确认重命名分类：先检查冲突，无冲突才执行重命名 */
    fun confirmRenameCategory(sectionType: SectionType, oldName: String, newName: String) {
        // 新旧名称相同，直接关闭对话框
        if (oldName == newName) {
            dismissDialog()
            return
        }

        // 检查新名称是否与当前列表中的条目冲突（大小写不敏感）
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
            // 设置冲突状态，让 UI 展示错误提示
            _renameConflict.value = newName
            return
        }

        // 无冲突，执行重命名
        viewModelScope.launch {
            when (sectionType) {
                SectionType.ACTIVITY -> {
                    categoryRepository.renameActivityCategory(oldName, newName)
                }
                SectionType.TAG -> {
                    // 如果是本地新增的标签，同步更新本地集合
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

    /** 确认删除分类：重置该分类下所有活动/标签为未分类 */
    fun confirmDeleteCategory(sectionType: SectionType, category: String) {
        viewModelScope.launch {
            when (sectionType) {
                SectionType.ACTIVITY -> {
                    categoryRepository.resetActivityCategory(category)
                }
                SectionType.TAG -> {
                    // 从本地新增集合中移除，并同步持久化
                    val updated = _addedTagCategories.value - category
                    _addedTagCategories.value = updated
                    settingsPrefs.saveTagCategories(updated)
                    categoryRepository.resetTagCategory(category)
                }
            }
            dismissDialog()
        }
    }

    /** 清除重命名冲突状态 */
    fun clearConflict() {
        _renameConflict.value = null
    }
}
