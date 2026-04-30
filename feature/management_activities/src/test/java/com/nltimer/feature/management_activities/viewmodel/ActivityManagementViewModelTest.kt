package com.nltimer.feature.management_activities.viewmodel

import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.model.ActivityStats
import com.nltimer.core.data.repository.ActivityManagementRepository
import com.nltimer.feature.management_activities.model.DialogState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeActivityManagementRepository : ActivityManagementRepository {

    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    private val _groups = MutableStateFlow<List<ActivityGroup>>(emptyList())

    override fun getAllActivities(): Flow<List<Activity>> = _activities

    override fun getUncategorizedActivities(): Flow<List<Activity>> =
        _activities.map { activities -> activities.filter { it.groupId == null } }

    override fun getActivitiesByGroup(groupId: Long): Flow<List<Activity>> =
        _activities.map { activities -> activities.filter { it.groupId == groupId } }

    override fun getAllGroups(): Flow<List<ActivityGroup>> = _groups

    private var nextActivityId = 1L
    private var nextGroupId = 1L

    override suspend fun addActivity(activity: Activity): Long {
        val newId = nextActivityId++
        val newActivity = activity.copy(id = newId)
        _activities.value = _activities.value + newActivity
        return newId
    }

    override suspend fun updateActivity(activity: Activity) {
        _activities.value = _activities.value.map {
            if (it.id == activity.id) activity else it
        }
    }

    override suspend fun deleteActivity(id: Long) {
        _activities.value = _activities.value.filter { it.id != id }
    }

    override suspend fun moveActivityToGroup(activityId: Long, groupId: Long?) {
        _activities.value = _activities.value.map {
            if (it.id == activityId) it.copy(groupId = groupId) else it
        }
    }

    override suspend fun addGroup(name: String): Long {
        val newId = nextGroupId++
        val newGroup = ActivityGroup(id = newId, name = name, sortOrder = _groups.value.size)
        _groups.value = _groups.value + newGroup
        return newId
    }

    override suspend fun renameGroup(id: Long, newName: String) {
        _groups.value = _groups.value.map {
            if (it.id == id) it.copy(name = newName) else it
        }
    }

    override suspend fun deleteGroup(id: Long) {
        _groups.value = _groups.value.filter { it.id != id }
        _activities.value = _activities.value.map {
            if (it.groupId == id) it.copy(groupId = null) else it
        }
    }

    override suspend fun initializePresets() {}

    override fun getActivityStats(activityId: Long): Flow<ActivityStats> =
        MutableStateFlow(ActivityStats())
}

class ActivityManagementViewModelTest {

    private lateinit var repository: FakeActivityManagementRepository
    private lateinit var viewModel: ActivityManagementViewModel

    @Before
    fun setup() {
        repository = FakeActivityManagementRepository()
        viewModel = ActivityManagementViewModel(repository)
    }

    @Test
    fun `initial state should be loading`() = runTest {
        val uiState = viewModel.uiState.value
        assertTrue(uiState.isLoading)
        assertTrue(uiState.uncategorizedActivities.isEmpty())
        assertTrue(uiState.groups.isEmpty())
        assertNull(uiState.dialogState)
    }

    @Test
    fun `add activity should add to uncategorized list`() = runTest {
        viewModel.addActivity("测试活动", "📝", null, null, null)

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(1, uiState.uncategorizedActivities.size)
        assertEquals("测试活动", uiState.uncategorizedActivities[0].name)
        assertEquals("📝", uiState.uncategorizedActivities[0].emoji)
        assertNull(uiState.uncategorizedActivities[0].groupId)
    }

    @Test
    fun `delete activity should remove from list`() = runTest {
        viewModel.addActivity("待删除", "❌", null, null, null)

        val afterAdd = viewModel.uiState.value
        assertEquals(1, afterAdd.uncategorizedActivities.size)

        val activityId = afterAdd.uncategorizedActivities[0].id
        viewModel.deleteActivity(activityId)

        val afterDelete = viewModel.uiState.value
        assertEquals(0, afterDelete.uncategorizedActivities.size)
    }

    @Test
    fun `add group should create group`() = runTest {
        viewModel.addGroup("我的分组")

        val uiState = viewModel.uiState.value
        assertEquals(1, uiState.allGroups.size)
        assertEquals("我的分组", uiState.allGroups[0].name)
    }

    @Test
    fun `move activity to group should update groupId`() = runTest {
        viewModel.addActivity("移动测试", "🔄", null, null, null)
        viewModel.addGroup("目标分组")

        val beforeMove = viewModel.uiState.value
        val activityId = beforeMove.uncategorizedActivities[0].id
        val groupId = beforeMove.allGroups[0].id

        viewModel.moveActivityToGroup(activityId, groupId)

        val afterMove = viewModel.uiState.value
        assertEquals(0, afterMove.uncategorizedActivities.size)
        assertEquals(1, afterMove.groups[0].activities.size)
        assertEquals(activityId, afterMove.groups[0].activities[0].id)
        assertEquals(groupId, afterMove.groups[0].activities[0].groupId)
    }

    @Test
    fun `rename group should update group name`() = runTest {
        viewModel.addGroup("旧名称")

        val beforeRename = viewModel.uiState.value
        val groupId = beforeRename.allGroups[0].id

        viewModel.renameGroup(groupId, "新名称")

        val afterRename = viewModel.uiState.value
        assertEquals("新名称", afterRename.allGroups[0].name)
    }

    @Test
    fun `delete group should ungroup all activities`() = runTest {
        viewModel.addGroup("待删除组")
        viewModel.addActivity("组内活动", "📌", null, null, null)

        val beforeDelete = viewModel.uiState.value
        val groupId = beforeDelete.allGroups[0].id
        val activityId = beforeDelete.uncategorizedActivities[0].id

        viewModel.moveActivityToGroup(activityId, groupId)
        viewModel.deleteGroup(groupId)

        val afterDelete = viewModel.uiState.value
        assertEquals(0, afterDelete.allGroups.size)
        assertEquals(1, afterDelete.uncategorizedActivities.size)
        assertNull(afterDelete.uncategorizedActivities[0].groupId)
    }

    @Test
    fun `show add activity dialog should set dialog state`() = runTest {
        viewModel.showAddActivityDialog()

        val uiState = viewModel.uiState.value
        assertNotNull(uiState.dialogState)
        assertTrue(uiState.dialogState is DialogState.AddActivity)
    }

    @Test
    fun `dismiss dialog should clear dialog state`() = runTest {
        viewModel.showAddActivityDialog()
        
        val withDialog = viewModel.uiState.value
        assertNotNull(withDialog.dialogState)

        viewModel.dismissDialog()

        val withoutDialog = viewModel.uiState.value
        assertNull(withoutDialog.dialogState)
    }

    @Test
    fun `toggle group expand should toggle expanded state`() = runTest {
        viewModel.addGroup("可展开组")

        val beforeToggle = viewModel.uiState.value
        val groupId = beforeToggle.allGroups[0].id
        assertFalse(beforeToggle.expandedGroupIds.contains(groupId))

        viewModel.toggleGroupExpand(groupId)

        val afterToggle = viewModel.uiState.value
        assertTrue(afterToggle.expandedGroupIds.contains(groupId))
    }

    @Test
    fun `update activity should modify existing activity`() = runTest {
        viewModel.addActivity("原始名称", "✏️", null, null, null)

        val beforeUpdate = viewModel.uiState.value
        val originalActivity = beforeUpdate.uncategorizedActivities[0]

        val updatedActivity = originalActivity.copy(
            name = "修改后名称",
            emoji = "📝"
        )
        viewModel.updateActivity(updatedActivity)

        val afterUpdate = viewModel.uiState.value
        assertEquals(1, afterUpdate.uncategorizedActivities.size)
        assertEquals("修改后名称", afterUpdate.uncategorizedActivities[0].name)
        assertEquals("📝", afterUpdate.uncategorizedActivities[0].emoji)
    }

    @Test
    fun `show delete group dialog should set correct dialog state`() = runTest {
        viewModel.addGroup("测试组")

        val uiState = viewModel.uiState.value
        val group = uiState.allGroups[0]

        viewModel.showDeleteGroupDialog(group)

        val withDialog = viewModel.uiState.value
        assertNotNull(withDialog.dialogState)
        assertTrue(withDialog.dialogState is DialogState.DeleteGroup)
        assertEquals(group, (withDialog.dialogState as DialogState.DeleteGroup).group)
    }

    @Test
    fun `show move to group dialog should set correct dialog state`() = runTest {
        viewModel.addActivity("移动测试", "🚀", null, null, null)

        val uiState = viewModel.uiState.value
        val activity = uiState.uncategorizedActivities[0]

        viewModel.showMoveToGroupDialog(activity)

        val withDialog = viewModel.uiState.value
        assertNotNull(withDialog.dialogState)
        assertTrue(withDialog.dialogState is DialogState.MoveToGroup)
        assertEquals(activity, (withDialog.dialogState as DialogState.MoveToGroup).activity)
    }
}
