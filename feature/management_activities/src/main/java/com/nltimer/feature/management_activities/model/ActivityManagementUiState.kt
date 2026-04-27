package com.nltimer.feature.management_activities.model

import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup

data class ActivityManagementUiState(
    val uncategorizedActivities: List<Activity> = emptyList(),
    val groups: List<GroupWithActivities> = emptyList(),
    val allGroups: List<ActivityGroup> = emptyList(),
    val isLoading: Boolean = true,
    val expandedGroupIds: Set<Long> = emptySet(),
    val dialogState: DialogState? = null,
)

data class GroupWithActivities(
    val group: ActivityGroup,
    val activities: List<Activity>,
)

sealed interface DialogState {
    object AddActivity : DialogState
    data class EditActivity(val activity: Activity) : DialogState
    object AddGroup : DialogState
    data class RenameGroup(val group: ActivityGroup) : DialogState
    data class DeleteGroup(val group: ActivityGroup) : DialogState
    data class DeleteActivity(val activity: Activity) : DialogState
    data class MoveToGroup(val activity: Activity) : DialogState
}
