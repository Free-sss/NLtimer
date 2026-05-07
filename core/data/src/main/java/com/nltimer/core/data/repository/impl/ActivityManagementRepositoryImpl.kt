package com.nltimer.core.data.repository.impl

import com.nltimer.core.data.database.dao.ActivityDao
import com.nltimer.core.data.database.dao.ActivityGroupDao
import com.nltimer.core.data.database.dao.BehaviorDao
import com.nltimer.core.data.database.entity.ActivityGroupEntity
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.model.ActivityStats
import com.nltimer.core.data.repository.ActivityManagementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ActivityManagementRepositoryImpl 活动管理仓库实现
 * 协调 ActivityDao、GroupDao 和 BehaviorDao 完成活动/分组的完整管理逻辑
 *
 * @param activityDao 活动数据访问对象
 * @param groupDao 活动分组数据访问对象
 * @param behaviorDao 行为记录数据访问对象
 */
@Singleton
class ActivityManagementRepositoryImpl @Inject constructor(
    private val activityDao: ActivityDao,
    private val groupDao: ActivityGroupDao,
    private val behaviorDao: BehaviorDao,
) : ActivityManagementRepository {

    companion object {
        // 首次使用时创建的预设活动列表
        val PRESET_ACTIVITIES = listOf(
            Activity(name = "番剧视频", iconKey = "📺", isPreset = true),
            Activity(name = "娱乐视频", iconKey = "🎬", isPreset = true),
            Activity(name = "玩游戏", iconKey = "🎮", isPreset = true),
            Activity(name = "主动学习", iconKey = "📖", isPreset = true),
            Activity(name = "运动健身", iconKey = "💪", isPreset = true),
            Activity(name = "社交聚会", iconKey = "👥", isPreset = true),
            Activity(name = "本职工作", iconKey = "💼", isPreset = true),
            Activity(name = "休息放松", iconKey = "😌", isPreset = true),
        )
    }

    // 实体转领域模型的基础查询
    override fun getAllActivities(): Flow<List<Activity>> =
        activityDao.getAllActive().map { list -> list.map { Activity.fromEntity(it) } }

    override fun getUncategorizedActivities(): Flow<List<Activity>> =
        activityDao.getUncategorized().map { list -> list.map { Activity.fromEntity(it) } }

    override fun getActivitiesByGroup(groupId: Long): Flow<List<Activity>> =
        activityDao.getByGroup(groupId).map { list -> list.map { Activity.fromEntity(it) } }

    override fun getAllGroups(): Flow<List<ActivityGroup>> =
        groupDao.getAll().map { list -> list.map { ActivityGroup.fromEntity(it) } }

    override fun getActivityStats(activityId: Long): Flow<ActivityStats> =
        // 合并三个 Flow 拼装统计对象
        combine(
            behaviorDao.getUsageCount(activityId),
            behaviorDao.getTotalDurationMs(activityId),
            behaviorDao.getLastUsedTimestamp(activityId)
        ) { count, durationMs, lastUsed ->
            ActivityStats(
                usageCount = count,
                totalDurationMinutes = (durationMs ?: 0L) / 60000,
                lastUsedTimestamp = lastUsed
            )
        }

    override suspend fun addActivity(activity: Activity): Long =
        activityDao.insert(activity.toEntity())

    override suspend fun updateActivity(activity: Activity) =
        activityDao.update(activity.toEntity())

    override suspend fun deleteActivity(id: Long) =
        activityDao.deleteById(id)

    override suspend fun moveActivityToGroup(activityId: Long, groupId: Long?) =
        activityDao.moveToGroup(activityId, groupId)

    override suspend fun addGroup(name: String): Long {
        // 计算当前最大排序值，新分组追加到最后
        val groups = groupDao.getAll().first()
        val maxOrder = groups.maxOfOrNull { it.sortOrder } ?: -1
        return groupDao.insert(
            ActivityGroupEntity(name = name, sortOrder = maxOrder + 1)
        )
    }

    override suspend fun renameGroup(id: Long, newName: String) {
        // 查找目标分组后更新名称
        val groups = groupDao.getAll().first()
        val group = groups.find { it.id == id } ?: return
        groupDao.update(group.copy(name = newName))
    }

    override suspend fun deleteGroup(id: Long) {
        // 先解除该分组下所有活动的关联，再删除分组
        groupDao.ungroupAllActivities(id)
        val groups = groupDao.getAll().first()
        val group = groups.find { it.id == id } ?: return
        groupDao.delete(group)
    }

    override suspend fun initializePresets() {
        // 仅在无预设记录时插入
        val existingPresets = activityDao.getAllPresets().first()
        if (existingPresets.isEmpty()) {
            PRESET_ACTIVITIES.forEach { preset ->
                activityDao.insert(preset.toEntity())
            }
        }
    }
}
