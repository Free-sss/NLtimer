package com.nltimer.core.data.repository.impl

import com.nltimer.core.data.database.dao.ActivityDao
import com.nltimer.core.data.database.dao.ActivityGroupDao
import com.nltimer.core.data.database.dao.BehaviorDao
import com.nltimer.core.data.database.entity.ActivityGroupEntity
import com.nltimer.core.data.database.entity.ActivityTagBindingEntity
import com.nltimer.core.data.database.NLtimerDatabase
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.model.ActivityStats
import com.nltimer.core.data.repository.ActivityManagementRepository
import com.nltimer.core.data.util.mapList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import android.util.Log
import androidx.room.withTransaction
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
    private val database: NLtimerDatabase,
) : ActivityManagementRepository {

    companion object {
        private const val TAG = "DB_ActivityDao"

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
        activityDao.getAllActive().mapList { Activity.fromEntity(it) }

    override fun getUncategorizedActivities(): Flow<List<Activity>> =
        activityDao.getUncategorized().mapList { Activity.fromEntity(it) }

    override fun getActivitiesByGroup(groupId: Long): Flow<List<Activity>> =
        activityDao.getByGroup(groupId).mapList { Activity.fromEntity(it) }

    override fun getAllGroups(): Flow<List<ActivityGroup>> =
        groupDao.getAll().mapList { ActivityGroup.fromEntity(it) }

    override fun getActivityStats(activityId: Long): Flow<ActivityStats> =
        behaviorDao.getActivityStatsSync(activityId).map { row ->
            ActivityStats(
                usageCount = row.usageCount,
                totalDurationMinutes = row.totalDurationMinutes,
                lastUsedTimestamp = row.lastUsedTimestamp,
            )
        }

    override suspend fun addActivity(activity: Activity): Long {
        val id = activityDao.insert(activity.toEntity())
        Log.d(TAG, "✅ addActivity id=$id name=${activity.name}")
        return id
    }

    override suspend fun updateActivity(activity: Activity) {
        activityDao.update(activity.toEntity())
        Log.d(TAG, "✅ updateActivity id=${activity.id} name=${activity.name}")
    }

    override suspend fun deleteActivity(id: Long) {
        database.withTransaction {
            behaviorDao.deleteTagCrossRefsByActivityId(id)
            behaviorDao.deleteByActivityId(id)
            activityDao.deleteActivityTagBindingsForActivity(id)
            activityDao.deleteById(id)
            Log.d(TAG, "✅ deleteActivity id=$id")
        }
    }

    override suspend fun moveActivityToGroup(activityId: Long, groupId: Long?) {
        activityDao.moveToGroup(activityId, groupId)
        Log.d(TAG, "✅ moveActivityToGroup activityId=$activityId groupId=$groupId")
    }

    override suspend fun addGroup(name: String): Long {
        val maxOrder = groupDao.getMaxSortOrder()
        val id = groupDao.insert(
            ActivityGroupEntity(name = name, sortOrder = (maxOrder ?: -1) + 1)
        )
        Log.d(TAG, "✅ addGroup id=$id name=$name")
        return id
    }

    override suspend fun renameGroup(id: Long, newName: String) {
        val group = groupDao.getById(id) ?: return
        groupDao.update(group.copy(name = newName))
        Log.d(TAG, "✅ renameGroup id=$id newName=$newName")
    }

    override suspend fun deleteGroup(id: Long) {
        database.withTransaction {
            groupDao.ungroupAllActivities(id)
            val group = groupDao.getById(id) ?: return@withTransaction
            groupDao.delete(group)
            Log.d(TAG, "✅ deleteGroup id=$id name=${group.name}")
        }
    }

    override suspend fun reorderGroups(orderedIds: List<Long>) {
        database.withTransaction {
            orderedIds.forEachIndexed { index, id ->
                groupDao.updateSortOrder(id, index)
            }
            Log.d(TAG, "✅ reorderGroups orderedIds=$orderedIds")
        }
    }

    override suspend fun initializePresets() {
        val existingPresets = activityDao.getAllPresetsSync()
        if (existingPresets.isEmpty()) {
            activityDao.insertAll(PRESET_ACTIVITIES.map { it.toEntity() })
            Log.d(TAG, "✅ initializePresets count=${PRESET_ACTIVITIES.size}")
        }
    }

    override suspend fun getTagIdsForActivity(activityId: Long): List<Long> =
        activityDao.getTagIdsForActivitySync(activityId)

    override suspend fun setActivityTagBindings(activityId: Long, tagIds: List<Long>) {
        database.withTransaction {
            activityDao.deleteActivityTagBindingsForActivity(activityId)
            if (tagIds.isNotEmpty()) {
                activityDao.insertActivityTagBindings(
                    tagIds.map { tagId ->
                        ActivityTagBindingEntity(activityId = activityId, tagId = tagId)
                    }
                )
            }
            Log.d(TAG, "✅ setActivityTagBindings activityId=$activityId tagIds=$tagIds")
        }
    }

    override suspend fun getAllActivitiesSync(): List<Activity> =
        activityDao.getAllActiveSync().map { Activity.fromEntity(it) }
}
