package com.nltimer.core.data.repository.impl

import com.nltimer.core.data.database.dao.ActivityDao
import com.nltimer.core.data.database.dao.ActivityGroupDao
import com.nltimer.core.data.database.entity.ActivityEntity
import com.nltimer.core.data.database.entity.ActivityGroupEntity
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val activityDao: ActivityDao,
    private val groupDao: ActivityGroupDao,
) : ActivityRepository {

    override fun getAllActive(): Flow<List<Activity>> =
        activityDao.getAllActive().map { list -> list.map { it.toModel() } }

    override fun getAll(): Flow<List<Activity>> =
        activityDao.getAll().map { list -> list.map { it.toModel() } }

    override fun getAllGroups(): Flow<List<ActivityGroup>> =
        groupDao.getAll().map { list -> list.map { it.toModel() } }

    override fun search(query: String): Flow<List<Activity>> =
        activityDao.search(query).map { list -> list.map { it.toModel() } }

    override suspend fun getById(id: Long): Activity? =
        activityDao.getById(id)?.toModel()

    override suspend fun getByName(name: String): Activity? =
        activityDao.getByName(name)?.toModel()

    override suspend fun insert(activity: Activity): Long =
        activityDao.insert(activity.toEntity())

    override suspend fun update(activity: Activity) =
        activityDao.update(activity.toEntity())

    override suspend fun setArchived(id: Long, archived: Boolean) =
        activityDao.setArchived(id, archived)

    private fun ActivityEntity.toModel() = Activity(
        id = id,
        name = name,
        emoji = emoji,
        iconKey = iconKey,
        groupId = groupId,
        isPreset = isPreset,
        isArchived = isArchived,
    )

    private fun Activity.toEntity() = ActivityEntity(
        id = id,
        name = name,
        emoji = emoji,
        iconKey = iconKey,
        groupId = groupId,
        isPreset = isPreset,
        isArchived = isArchived,
    )

    private fun ActivityGroupEntity.toModel() = ActivityGroup(
        id = id,
        name = name,
        sortOrder = sortOrder,
    )
}
