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

    override fun getHomeBehaviors(dayStart: Long, dayEnd: Long): Flow<List<Behavior>> =
        behaviorDao.getHomeBehaviors(dayStart, dayEnd).map { list -> list.map { it.toModel() } }

    override fun getTagsForBehavior(behaviorId: Long): Flow<List<Tag>> =
        behaviorDao.getTagsForBehavior(behaviorId).map { list ->
            list.map { entity ->
                Tag(
                    id = entity.id,
                    name = entity.name,
                    color = entity.color,
                    textColor = entity.textColor,
                    icon = entity.icon,
                    category = entity.category,
                    priority = entity.priority,
                    usageCount = entity.usageCount,
                    sortOrder = entity.sortOrder,
                    isArchived = entity.isArchived,
                )
            }
        }

    override fun getPendingBehaviors(): Flow<List<Behavior>> =
        behaviorDao.getPendingBehaviors().map { list -> list.map { it.toModel() } }

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
                textColor = entity.textColor,
                icon = entity.icon,
                category = entity.category,
                priority = entity.priority,
                usageCount = entity.usageCount,
                sortOrder = entity.sortOrder,
                isArchived = entity.isArchived,
            )
        }
        return BehaviorWithDetails(
            behavior = behavior,
            activity = activity,
            tags = tags,
        )
    }

    override suspend fun getNextPending(): Behavior? =
        behaviorDao.getNextPending()?.toModel()

    override suspend fun getMaxSequence(): Int =
        behaviorDao.getMaxSequence()

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

    override suspend fun setStatus(id: Long, status: String) =
        behaviorDao.setStatus(id, status)

    override suspend fun setStartTime(id: Long, startTime: Long) =
        behaviorDao.setStartTime(id, startTime)

    override suspend fun setActualDuration(id: Long, duration: Long) =
        behaviorDao.setActualDuration(id, duration)

    override suspend fun setAchievementLevel(id: Long, level: Int) =
        behaviorDao.setAchievementLevel(id, level)

    override suspend fun setSequence(id: Long, sequence: Int) =
        behaviorDao.setSequence(id, sequence)

    override suspend fun setNote(id: Long, note: String?) =
        behaviorDao.setNote(id, note)

    override suspend fun endCurrentBehavior(endTime: Long) =
        behaviorDao.endCurrentBehavior(endTime)

    override suspend fun completeCurrentAndStartNext(currentId: Long, idleMode: Boolean): Behavior? {
        val now = System.currentTimeMillis()
        val clampedEndTime = if (now < now) now else now
        behaviorDao.setEndTime(currentId, clampedEndTime)

        val currentEntity = behaviorDao.getById(currentId)
        if (currentEntity != null && currentEntity.startTime > 0) {
            val duration = clampedEndTime - currentEntity.startTime
            behaviorDao.setActualDuration(currentId, duration)

            val estimated = currentEntity.estimatedDuration?.times(60_000)
            if (currentEntity.wasPlanned && estimated != null && estimated > 0) {
                val diff = kotlin.math.abs(duration - estimated)
                val ratio = (diff.toDouble() / estimated).coerceAtMost(1.0)
                val level = ((1.0 - ratio) * 100).toInt().coerceIn(0, 100)
                behaviorDao.setAchievementLevel(currentId, level)
            }
        }

        behaviorDao.setStatus(currentId, "completed")

        if (idleMode) return null

        val nextPending = behaviorDao.getNextPending() ?: return null
        val nextNow = System.currentTimeMillis()
        behaviorDao.setStatus(nextPending.id, "active")
        behaviorDao.setStartTime(nextPending.id, nextNow)
        return nextPending.toModel()
    }

    override suspend fun reorderGoals(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id ->
            behaviorDao.setSequence(id, index)
        }
    }

    override suspend fun delete(id: Long) {
        val toDelete = behaviorDao.getById(id) ?: return
        behaviorDao.delete(id)

        val pendingList = behaviorDao.getPendingBehaviors()
        val pendingEntities = mutableListOf<BehaviorEntity>()
        pendingList.collect { pendingEntities.addAll(it) }
    }

    override suspend fun settleDay(dayStart: Long, dayEnd: Long) {
    }

    private fun BehaviorEntity.toModel() = Behavior(
        id = id,
        activityId = activityId,
        startTime = startTime,
        endTime = endTime,
        status = BehaviorNature.entries.firstOrNull { it.key == status } ?: BehaviorNature.PENDING,
        note = note,
        pomodoroCount = pomodoroCount,
        sequence = sequence,
        estimatedDuration = estimatedDuration,
        actualDuration = actualDuration,
        achievementLevel = achievementLevel,
        wasPlanned = wasPlanned,
    )

    private fun Behavior.toEntity() = BehaviorEntity(
        id = id,
        activityId = activityId,
        startTime = startTime,
        endTime = endTime,
        status = status.key,
        note = note,
        pomodoroCount = pomodoroCount,
        sequence = sequence,
        estimatedDuration = estimatedDuration,
        actualDuration = actualDuration,
        achievementLevel = achievementLevel,
        wasPlanned = wasPlanned,
    )

    override suspend fun updateBehavior(
        id: Long,
        activityId: Long,
        startTime: Long,
        endTime: Long?,
        status: String,
        note: String?,
    ) {
        behaviorDao.update(id, activityId, startTime, endTime, status, note)
    }

    override suspend fun updateTagsForBehavior(behaviorId: Long, tagIds: List<Long>) {
        behaviorDao.deleteTagsForBehavior(behaviorId)
        behaviorDao.insertTagCrossRefs(tagIds.map { BehaviorTagCrossRefEntity(behaviorId = behaviorId, tagId = it) })
    }
}
