package com.nltimer.feature.management_activities.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.repository.ActivityManagementRepository
import com.nltimer.feature.management_activities.model.ActivityManagementUiState
import com.nltimer.feature.management_activities.model.DialogState
import com.nltimer.feature.management_activities.model.GroupWithActivities
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ActivityManagementViewModel @Inject constructor(
    private val repository: ActivityManagementRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityManagementUiState())
    val uiState: StateFlow<ActivityManagementUiState> = _uiState.asStateFlow()

    init {
        loadData()
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
                it.copy(
                    isLoading = false,
                    uncategorizedActivities = uncategorized,
                    groups = groupsWithActivities,
                    allGroups = groups,
                )
            }
        }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false) }
            }
            .launchIn(viewModelScope)

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

    fun toggleGroupExpand(groupId: Long) {
        val current = _uiState.value.expandedGroupIds
        val newSet = if (current.contains(groupId)) current - groupId else current + groupId
        _uiState.update { it.copy(expandedGroupIds = newSet) }
    }

    fun showAddActivityDialog() {
        _uiState.update { it.copy(dialogState = DialogState.AddActivity) }
    }

    fun showEditActivityDialog(activity: Activity) {
        _uiState.update { it.copy(dialogState = DialogState.EditActivity(activity)) }
    }

    fun addActivity(name: String, emoji: String?, groupId: Long?) {
        viewModelScope.launch {
            val activity = Activity(
                name = name.trim(),
                emoji = emoji,
                groupId = groupId,
                isPreset = false,
            )
            repository.addActivity(activity)
            dismissDialog()
        }
    }

    fun updateActivity(activity: Activity) {
        viewModelScope.launch {
            repository.updateActivity(activity)
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
        _uiState.update { it.copy(dialogState = null) }
    }
}
