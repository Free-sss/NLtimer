package com.nltimer.core.data.repository.impl

import com.nltimer.core.data.database.dao.ActivityDao
import com.nltimer.core.data.database.dao.BehaviorDao
import com.nltimer.core.data.database.dao.TagDao
import com.nltimer.core.data.database.entity.BehaviorEntity
import com.nltimer.core.data.database.entity.BehaviorTagCrossRefEntity
import com.nltimer.core.data.model.Behavior
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.model.BehaviorWithDetails
import com.nltimer.core.data.model.Tag
import com.nltimer.core.data.repository.BehaviorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BehaviorRepositoryImpl @Inject constructor(
    private val behaviorDao: BehaviorDao,
    private val activityDao: ActivityDao,
    private val tagDao: TagDao,
) : BehaviorRepository {

    override fun getByDayRange(dayStart: Long, dayEnd: Long): Flow<List<Behavior>> =
        behaviorDao.getByDayRange(dayStart, dayEnd).map { list -> list.map { it.toModel() } }

    override fun getCurrentBehavior(): Flow<Behavior?> =
        behaviorDao.getCurrentBehavior().map { it?.toModel() }

    override fun getTagsForBehavior(behaviorId: Long): Flow<List<Tag>> =
        behaviorDao.getTagsForBehavior(behaviorId).map { list ->
            list.map { entity ->
                Tag(
                    id = entity.id,
                    name = entity.name,
                    color = entity.color,
                    category = entity.category,
                    priority = entity.priority,
                    isArchived = entity.isArchived,
                )
            }
        }

    override suspend fun getBehaviorWithDetails(behaviorId: Long): BehaviorWithDetails? {
        val behaviorEntity = behaviorDao.getById(behaviorId) ?: return null
        val behavior = behaviorEntity.toModel()
        val activityEntity = activityDao.getById(behavior.activityId) ?: return null
        val activity = com.nltimer.core.data.model.Activity(
            id = activityEntity.id,
            name = activityEntity.name,
            emoji = activityEntity.emoji,
            iconKey = activityEntity.iconKey,
            category = activityEntity.category,
            isArchived = activityEntity.isArchived,
        )
        val tagEntities = tagDao.getTagsForBehaviorSync(behaviorId)
        val tags = tagEntities.map { entity ->
            Tag(
                id = entity.id,
                name = entity.name,
                color = entity.color,
                category = entity.category,
                priority = entity.priority,
                isArchived = entity.isArchived,
            )
        }
        return BehaviorWithDetails(
            behavior = behavior,
            activity = activity,
            tags = tags,
        )
    }

    override suspend fun insert(behavior: Behavior, tagIds: List<Long>): Long {
        val id = behaviorDao.insert(behavior.toEntity())
        if (tagIds.isNotEmpty()) {
            behaviorDao.insertTagCrossRefs(
                tagIds.map { tagId ->
                    BehaviorTagCrossRefEntity(
                        behaviorId = id,
                        tagId = tagId,
                    )
                }
            )
        }
        return id
    }

    override suspend fun setEndTime(id: Long, endTime: Long) =
        behaviorDao.setEndTime(id, endTime)

    override suspend fun setNote(id: Long, note: String?) =
        behaviorDao.setNote(id, note)

    override suspend fun endCurrentBehavior(endTime: Long) =
        behaviorDao.endCurrentBehavior(endTime)

    override suspend fun delete(id: Long) =
        behaviorDao.delete(id)

    private fun BehaviorEntity.toModel() = Behavior(
        id = id,
        activityId = activityId,
        startTime = startTime,
        endTime = endTime,
        nature = BehaviorNature.entries.firstOrNull { it.key == nature } ?: BehaviorNature.CURRENT,
        note = note,
        pomodoroCount = pomodoroCount,
    )

    private fun Behavior.toEntity() = BehaviorEntity(
        id = id,
        activityId = activityId,
        startTime = startTime,
        endTime = endTime,
        nature = nature.key,
        note = note,
        pomodoroCount = pomodoroCount,
    )
}
