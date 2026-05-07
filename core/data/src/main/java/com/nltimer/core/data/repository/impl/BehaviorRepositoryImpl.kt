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
import com.nltimer.core.data.util.BehaviorCalculator
import com.nltimer.core.data.util.ClockService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BehaviorRepositoryImpl 行为记录仓库实现
 * 处理行为记录的完整生命周期：创建、计时、状态变更、完成度评估、标签关联
 *
 * @param behaviorDao 行为数据访问对象
 * @param activityDao 活动数据访问对象（用于构建详情）
 * @param tagDao 标签数据访问对象（用于构建标签关联）
 */
@Singleton
class BehaviorRepositoryImpl @Inject constructor(
    private val behaviorDao: BehaviorDao,
    private val activityDao: ActivityDao,
    private val tagDao: TagDao,
    private val clockService: ClockService,
) : BehaviorRepository {

    override fun getByDayRange(dayStart: Long, dayEnd: Long): Flow<List<Behavior>> =
        behaviorDao.getByDayRange(dayStart, dayEnd).map { list -> list.map { it.toModel() } }

    override fun getCurrentBehavior(): Flow<Behavior?> =
        behaviorDao.getCurrentBehavior().map { it?.toModel() }

    override fun getHomeBehaviors(dayStart: Long, dayEnd: Long): Flow<List<Behavior>> =
        behaviorDao.getHomeBehaviors(dayStart, dayEnd).map { list -> list.map { it.toModel() } }

    override fun getTagsForBehavior(behaviorId: Long): Flow<List<Tag>> =
        behaviorDao.getTagsForBehavior(behaviorId).map { list ->
            list.map { Tag.fromEntity(it) }
        }

    override fun getPendingBehaviors(): Flow<List<Behavior>> =
        behaviorDao.getPendingBehaviors().map { list -> list.map { it.toModel() } }

    override suspend fun getBehaviorWithDetails(behaviorId: Long): BehaviorWithDetails? {
        // 查询行为、活动、标签三层数据组装完整详情
        val behaviorEntity = behaviorDao.getById(behaviorId) ?: return null
        val behavior = behaviorEntity.toModel()
        val activityEntity = activityDao.getById(behavior.activityId) ?: return null
        val activity = Activity.fromEntity(activityEntity)
        val tagEntities = tagDao.getTagsForBehaviorSync(behaviorId)
        val tags = tagEntities.map { Tag.fromEntity(it) }
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
        // 先插入行为记录，再批量插入标签关联
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
        val now = clockService.currentTimeMillis()
        val currentEntity = behaviorDao.getById(currentId) ?: return null
        val clampedEndTime = now.coerceAtLeast(currentEntity.startTime)

        // 计算实际耗时并评估完成度
        if (currentEntity.startTime > 0) {
            val result = BehaviorCalculator.calculateCompletion(
                startTime = currentEntity.startTime,
                endTime = clampedEndTime,
                wasPlanned = currentEntity.wasPlanned,
                estimatedDurationMinutes = currentEntity.estimatedDuration,
            )
            behaviorDao.setActualDuration(currentId, result.durationMs)
            if (result.achievementLevel != null) {
                behaviorDao.setAchievementLevel(currentId, result.achievementLevel)
            }
        }

        // 结束当前行为：设置结束时间和状态
        behaviorDao.setEndTime(currentId, clampedEndTime)
        behaviorDao.setStatus(currentId, BehaviorNature.COMPLETED.key)

        // 空闲模式不启动下一个
        if (idleMode) return null

        // 自动启动下一个待办行为
        val nextPending = behaviorDao.getNextPending() ?: return null
        val nextNow = clockService.currentTimeMillis()
        behaviorDao.setStatus(nextPending.id, BehaviorNature.ACTIVE.key)
        behaviorDao.setStartTime(nextPending.id, nextNow)
        return nextPending.toModel()
    }

    override suspend fun reorderGoals(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id ->
            behaviorDao.setSequence(id, index)
        }
    }

    override suspend fun delete(id: Long) = behaviorDao.delete(id)

    override suspend fun settleDay(dayStart: Long, dayEnd: Long) {
    }

    // 数据库实体与领域模型互转
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
        
        // 重新计算实际耗时
        if (status == "completed" && endTime != null && startTime > 0) {
            val duration = endTime - startTime
            behaviorDao.setActualDuration(id, duration)
        } else if (status == "active" && startTime > 0) {
            val duration = clockService.currentTimeMillis() - startTime
            behaviorDao.setActualDuration(id, duration)
        } else {
            behaviorDao.setActualDuration(id, 0L)
        }
    }

    override suspend fun updateTagsForBehavior(behaviorId: Long, tagIds: List<Long>) {
        // 先删除原有关联，再重新插入
        behaviorDao.deleteTagsForBehavior(behaviorId)
        behaviorDao.insertTagCrossRefs(tagIds.map { BehaviorTagCrossRefEntity(behaviorId = behaviorId, tagId = it) })
    }

    override fun getBehaviorsOverlappingRange(rangeStart: Long, rangeEnd: Long): Flow<List<Behavior>> =
        behaviorDao.getBehaviorsOverlappingRange(rangeStart, rangeEnd).map { list ->
            list.map { it.toModel() }
        }
}
