package com.nltimer.core.data.repository.impl

import com.nltimer.core.data.database.dao.ActivityDao
import com.nltimer.core.data.database.dao.ActivityGroupDao
import com.nltimer.core.data.database.entity.ActivityGroupEntity
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.repository.ActivityManagementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityManagementRepositoryImpl @Inject constructor(
    private val activityDao: ActivityDao,
    private val groupDao: ActivityGroupDao,
) : ActivityManagementRepository {

    companion object {
        val PRESET_ACTIVITIES = listOf(
            Activity(name = "番剧视频", emoji = "📺", isPreset = true),
            Activity(name = "娱乐视频", emoji = "🎬", isPreset = true),
            Activity(name = "玩游戏", emoji = "🎮", isPreset = true),
            Activity(name = "主动学习", emoji = "📖", isPreset = true),
            Activity(name = "运动健身", emoji = "💪", isPreset = true),
            Activity(name = "社交聚会", emoji = "👥", isPreset = true),
            Activity(name = "本职工作", emoji = "💼", isPreset = true),
            Activity(name = "休息放松", emoji = "😌", isPreset = true),
        )
    }

    override fun getAllActivities(): Flow<List<Activity>> =
        activityDao.getAllActive().map { list -> list.map { Activity.fromEntity(it) } }

    override fun getUncategorizedActivities(): Flow<List<Activity>> =
        activityDao.getUncategorized().map { list -> list.map { Activity.fromEntity(it) } }

    override fun getActivitiesByGroup(groupId: Long): Flow<List<Activity>> =
        activityDao.getByGroup(groupId).map { list -> list.map { Activity.fromEntity(it) } }

    override fun getAllGroups(): Flow<List<ActivityGroup>> =
        groupDao.getAll().map { list -> list.map { ActivityGroup.fromEntity(it) } }

    override suspend fun addActivity(activity: Activity): Long =
        activityDao.insert(activity.toEntity())

    override suspend fun updateActivity(activity: Activity) =
        activityDao.update(activity.toEntity())

    override suspend fun deleteActivity(id: Long) =
        activityDao.deleteById(id)

    override suspend fun moveActivityToGroup(activityId: Long, groupId: Long?) =
        activityDao.moveToGroup(activityId, groupId)

    override suspend fun addGroup(name: String): Long {
        val groups = groupDao.getAll().first()
        val maxOrder = groups.maxOfOrNull { it.sortOrder } ?: -1
        return groupDao.insert(
            ActivityGroupEntity(name = name, sortOrder = maxOrder + 1)
        )
    }

    override suspend fun renameGroup(id: Long, newName: String) {
        val groups = groupDao.getAll().first()
        val group = groups.find { it.id == id } ?: return
        groupDao.update(group.copy(name = newName))
    }

    override suspend fun deleteGroup(id: Long) {
        groupDao.ungroupAllActivities(id)
        val groups = groupDao.getAll().first()
        val group = groups.find { it.id == id } ?: return
        groupDao.delete(group)
    }

    override suspend fun initializePresets() {
        val existingPresets = activityDao.getAllPresets().first()
        if (existingPresets.isEmpty()) {
            PRESET_ACTIVITIES.forEach { preset ->
                activityDao.insert(preset.toEntity())
            }
        }
    }
}
