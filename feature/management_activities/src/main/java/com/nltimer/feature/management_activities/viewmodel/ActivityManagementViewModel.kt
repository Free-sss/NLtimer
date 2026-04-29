package com.nltimer.feature.management_activities.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.model.ActivityStats
import com.nltimer.core.data.repository.ActivityManagementRepository
import com.nltimer.feature.management_activities.model.ActivityManagementUiState
import com.nltimer.feature.management_activities.model.DialogState
import com.nltimer.feature.management_activities.model.GroupWithActivities
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 活动管理页面的 ViewModel
 *
 * 负责管理活动和分组的增删改查，协调 UI 状态与 Repository 之间的数据流。
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ActivityManagementViewModel @Inject constructor(
    private val repository: ActivityManagementRepository,
) : ViewModel() {

    // 页面整体 UI 状态，包括活动列表、分组列表、弹窗状态等
    private val _uiState = MutableStateFlow(ActivityManagementUiState())
    val uiState: StateFlow<ActivityManagementUiState> = _uiState.asStateFlow()

    // 当前选中的活动 ID，用于实时获取该活动的统计信息
    private val _selectedActivityId = MutableStateFlow<Long?>(null)
    val currentActivityStats: StateFlow<ActivityStats> = _selectedActivityId
        // 根据选中的活动 ID 切换监听对应统计流
        .flatMapLatest { id ->
            if (id != null) repository.getActivityStats(id) else flowOf(ActivityStats())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ActivityStats())

    init {
        // 初始化加载数据，同时确保预设活动已入库
        loadData()
        viewModelScope.launch {
            repository.initializePresets()
        }
    }

    /**
     * 加载初始数据
     *
     * 合并未分类活动流和全部分组流，初始化 UI 状态。
     * 同时为每个分组异步加载其活动列表。
     */
    private fun loadData() {
        // 合并未分类活动和全部分组，构建初始 UI 状态
        combine(
            repository.getUncategorizedActivities(),
            repository.getAllGroups(),
        ) { uncategorized, groups ->
            val groupsWithActivities = groups.map { group ->
                GroupWithActivities(group, emptyList())
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    uncategorizedActivities = uncategorized,
                    groups = groupsWithActivities,
                    allGroups = groups,
                )
            }
        }
            .catch { e ->
                // 加载失败时仍关闭加载状态
                _uiState.update { it.copy(isLoading = false) }
            }
            .launchIn(viewModelScope)

        // 为每个分组单独监听其内部活动列表的变化
        viewModelScope.launch {
            repository.getAllGroups().collect { groups ->
                groups.forEach { group ->
                    repository.getActivitiesByGroup(group.id)
                        .onEach { activities ->
                            _uiState.update { uiState ->
                                val updatedGroups = uiState.groups.map { gwa ->
                                    if (gwa.group.id == group.id) {
                                        gwa.copy(activities = activities)
                                    } else {
                                        gwa
                                    }
                                }
                                uiState.copy(groups = updatedGroups)
                            }
                        }
                        .launchIn(viewModelScope)
                }
            }
        }
    }

    /**
     * 切换分组卡片展开/折叠状态
     *
     * @param groupId 目标分组 ID
     */
    fun toggleGroupExpand(groupId: Long) {
        val current = _uiState.value.expandedGroupIds
        val newSet = if (current.contains(groupId)) current - groupId else current + groupId
        _uiState.update { it.copy(expandedGroupIds = newSet) }
    }

    fun showAddActivityDialog() {
        _uiState.update { it.copy(dialogState = DialogState.AddActivity) }
    }

    fun showAddActivityToGroupDialog(group: ActivityGroup) {
        _uiState.update { it.copy(dialogState = DialogState.AddActivityToGroup(group)) }
    }

    /**
     * 显示活动详情弹窗
     *
     * 同时设置选中的活动 ID 以便加载其统计数据。
     *
     * @param activity 目标活动
     */
    fun showActivityDetail(activity: Activity) {
        _selectedActivityId.value = activity.id
        _uiState.update { it.copy(dialogState = DialogState.ActivityDetail(activity)) }
    }

    fun showEditActivityDialog(activity: Activity) {
        _uiState.update { it.copy(dialogState = DialogState.EditActivity(activity)) }
    }

    /**
     * 添加新活动
     *
     * @param name 活动名称
     * @param emoji 可选的 Emoji
     * @param groupId 可选的分组 ID，null 表示未分类
     */
    fun addActivity(name: String, emoji: String?, color: Long?, groupId: Long?, note: String?) {
        viewModelScope.launch {
            val activity = Activity(
                name = name.trim(),
                emoji = emoji,
                color = color,
                groupId = groupId,
                isPreset = false,
            )
            repository.addActivity(activity)
            dismissDialog()
            // TODO: note field belongs to Behavior table, handle separately when creating initial behavior
        }
    }

    /**
     * 更新活动信息
     *
     * @param activity 包含更新后字段的 Activity 对象
     */
    fun updateActivity(activity: Activity) {
        viewModelScope.launch {
            repository.updateActivity(activity)
            dismissDialog()
        }
    }

    /**
     * 删除指定活动
     *
     * @param id 活动 ID
     */
    fun deleteActivity(id: Long) {
        viewModelScope.launch {
            repository.deleteActivity(id)
            dismissDialog()
        }
    }

    /**
     * 将活动移动到指定分组
     *
     * @param activityId 活动 ID
     * @param groupId 目标分组 ID，null 表示移回未分类
     */
    fun moveActivityToGroup(activityId: Long, groupId: Long?) {
        viewModelScope.launch {
            repository.moveActivityToGroup(activityId, groupId)
            dismissDialog()
        }
    }

    fun showAddGroupDialog() {
        _uiState.update { it.copy(dialogState = DialogState.AddGroup) }
    }

    /**
     * 创建新分组
     *
     * @param name 分组名称
     */
    fun addGroup(name: String) {
        viewModelScope.launch {
            repository.addGroup(name.trim())
            dismissDialog()
        }
    }

    /**
     * 重命名分组
     *
     * @param id 分组 ID
     * @param newName 新名称
     */
    fun renameGroup(id: Long, newName: String) {
        viewModelScope.launch {
            repository.renameGroup(id, newName.trim())
            dismissDialog()
        }
    }

    fun showDeleteGroupDialog(group: ActivityGroup) {
        _uiState.update { it.copy(dialogState = DialogState.DeleteGroup(group)) }
    }

    fun showRenameGroupDialog(group: ActivityGroup) {
        _uiState.update { it.copy(dialogState = DialogState.RenameGroup(group)) }
    }

    /**
     * 删除分组及其关联的活动
     *
     * @param id 分组 ID
     */
    fun deleteGroup(id: Long) {
        viewModelScope.launch {
            repository.deleteGroup(id)
            dismissDialog()
        }
    }

    fun showDeleteActivityDialog(activity: Activity) {
        _uiState.update { it.copy(dialogState = DialogState.DeleteActivity(activity)) }
    }

    fun showMoveToGroupDialog(activity: Activity) {
        _uiState.update { it.copy(dialogState = DialogState.MoveToGroup(activity)) }
    }

    /**
     * 关闭当前弹窗
     *
     * 同时清除选中的活动 ID 和弹窗状态。
     */
    fun dismissDialog() {
        _selectedActivityId.value = null
        _uiState.update { it.copy(dialogState = null) }
    }
}
