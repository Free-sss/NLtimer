package com.nltimer.feature.management_activities.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.model.ActivityStats
import com.nltimer.core.data.repository.ActivityManagementRepository
import com.nltimer.core.data.repository.TagRepository
import com.nltimer.core.data.usecase.AddActivityUseCase
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ActivityManagementViewModel @Inject constructor(
    private val repository: ActivityManagementRepository,
    private val addActivityUseCase: AddActivityUseCase,
    private val tagRepository: TagRepository,
) : ViewModel() {

    private var groupActivityJobs = mutableListOf<Job>()

    private val _uiState = MutableStateFlow(ActivityManagementUiState())
    val uiState: StateFlow<ActivityManagementUiState> = _uiState.asStateFlow()

    private val _selectedActivityId = MutableStateFlow<Long?>(null)
    val currentActivityStats: StateFlow<ActivityStats> = _selectedActivityId
        .flatMapLatest { id ->
            if (id != null) repository.getActivityStats(id) else flowOf(ActivityStats())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ActivityStats())

    init {
        loadData()
        loadTags()
        viewModelScope.launch {
            repository.initializePresets()
        }
    }

    private fun loadData() {
        combine(
            repository.getUncategorizedActivities(),
            repository.getAllGroups(),
        ) { uncategorized, groups ->
            val groupsWithActivities = groups.map { group ->
                GroupWithActivities(group, emptyList())
            }
            _uiState.update {
                val groupIds = groups.map { group -> group.id }.toSet()
                it.copy(
                    isLoading = false,
                    uncategorizedActivities = uncategorized,
                    groups = groupsWithActivities,
                    allGroups = groups,
                    expandedGroupIds = if (it.groups.isEmpty() && it.expandedGroupIds.isEmpty()) {
                        groupIds
                    } else {
                        it.expandedGroupIds + (groupIds - it.expandedGroupIds)
                    },
                )
            }
        }
            .catch { _uiState.update { it.copy(isLoading = false) } }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            repository.getAllGroups().collect { groups ->
                val currentGroupIds = groups.map { it.id }.toSet()
                _uiState.update { state ->
                    state.copy(groups = state.groups.filter { it.group.id in currentGroupIds })
                }
                groupActivityJobs.forEach { it.cancel() }
                groupActivityJobs.clear()
                groups.forEach { group ->
                    val job = repository.getActivitiesByGroup(group.id)
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
                    groupActivityJobs.add(job)
                }
            }
        }
    }

    private fun loadTags() {
        tagRepository.getAllActive()
            .onEach { tags ->
                _uiState.update { it.copy(allTags = tags) }
            }
            .launchIn(viewModelScope)
    }

    fun toggleGroupExpand(groupId: Long) {
        val current = _uiState.value.expandedGroupIds
        val newSet = if (current.contains(groupId)) current - groupId else current + groupId
        _uiState.update { it.copy(expandedGroupIds = newSet) }
    }

    fun setAllGroupsExpanded(expanded: Boolean) {
        _uiState.update { state ->
            state.copy(
                expandedGroupIds = if (expanded) {
                    state.groups.map { it.group.id }.toSet()
                } else {
                    emptySet()
                },
            )
        }
    }

    fun reorderGroups(orderedIds: List<Long>) {
        viewModelScope.launch {
            repository.reorderGroups(orderedIds)
        }
    }

    fun showAddActivityDialog() {
        _uiState.update { it.copy(dialogState = DialogState.AddActivity) }
    }

    fun showAddActivityToGroupDialog(group: ActivityGroup) {
        _uiState.update { it.copy(dialogState = DialogState.AddActivityToGroup(group)) }
    }

    fun showActivityDetail(activity: Activity) {
        _selectedActivityId.value = activity.id
        _uiState.update { it.copy(dialogState = DialogState.ActivityDetail(activity)) }
    }

    fun showEditActivityDialog(activity: Activity) {
        viewModelScope.launch {
            val tagIds = repository.getTagIdsForActivity(activity.id).toSet()
            _uiState.update { it.copy(dialogState = DialogState.EditActivity(activity, tagIds)) }
        }
    }

    fun addActivity(name: String, iconKey: String?, color: Long?, groupId: Long?, keywords: String?, tagIds: List<Long>) {
        viewModelScope.launch {
            addActivityUseCase(name, iconKey, color, groupId, keywords, tagIds)
            dismissDialog()
        }
    }

    fun updateActivity(activity: Activity, tagIds: List<Long>) {
        viewModelScope.launch {
            repository.updateActivity(activity)
            repository.setActivityTagBindings(activity.id, tagIds)
            dismissDialog()
        }
    }

    fun deleteActivity(id: Long) {
        viewModelScope.launch {
            repository.deleteActivity(id)
            dismissDialog()
        }
    }

    fun moveActivityToGroup(activityId: Long, groupId: Long?) {
        viewModelScope.launch {
            repository.moveActivityToGroup(activityId, groupId)
            dismissDialog()
        }
    }

    fun showAddGroupDialog() {
        _uiState.update { it.copy(dialogState = DialogState.AddGroup) }
    }

    fun addGroup(name: String) {
        viewModelScope.launch {
            repository.addGroup(name.trim())
            dismissDialog()
        }
    }

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

    fun dismissDialog() {
        _selectedActivityId.value = null
        _uiState.update { it.copy(dialogState = null) }
    }
}
