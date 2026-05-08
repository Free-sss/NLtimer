package com.nltimer.core.data.repository.impl

import com.nltimer.core.data.database.dao.ActivityDao
import com.nltimer.core.data.database.dao.ActivityGroupDao
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ActivityRepositoryImpl 活动基础仓库实现
 * 委托 ActivityDao 和 ActivityGroupDao 完成活动及分组的查询操作
 *
 * @param activityDao 活动数据访问对象
 * @param groupDao 活动分组数据访问对象
 */
@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val activityDao: ActivityDao,
    private val groupDao: ActivityGroupDao,
) : ActivityRepository {

    override fun getAllActive(): Flow<List<Activity>> =
        activityDao.getAllActive().map { list -> list.map { Activity.fromEntity(it) } }

    override fun getAll(): Flow<List<Activity>> =
        activityDao.getAll().map { list -> list.map { Activity.fromEntity(it) } }

    override fun getAllGroups(): Flow<List<ActivityGroup>> =
        groupDao.getAll().map { list -> list.map { ActivityGroup.fromEntity(it) } }

    override fun search(query: String): Flow<List<Activity>> =
        activityDao.search(query).map { list -> list.map { Activity.fromEntity(it) } }

    override suspend fun getById(id: Long): Activity? =
        activityDao.getById(id)?.let { Activity.fromEntity(it) }

    override suspend fun getByName(name: String): Activity? =
        activityDao.getByName(name)?.let { Activity.fromEntity(it) }

    override suspend fun insert(activity: Activity): Long =
        activityDao.insert(activity.toEntity())

    override suspend fun update(activity: Activity) =
        activityDao.update(activity.toEntity())

    override suspend fun setArchived(id: Long, archived: Boolean) =
        activityDao.setArchived(id, archived)
}
