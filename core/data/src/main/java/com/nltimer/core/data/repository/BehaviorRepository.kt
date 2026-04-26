package com.nltimer.core.data.repository

import com.nltimer.core.data.model.Behavior
import com.nltimer.core.data.model.BehaviorWithDetails
import com.nltimer.core.data.model.Tag
import kotlinx.coroutines.flow.Flow

interface BehaviorRepository {
    fun getByDayRange(dayStart: Long, dayEnd: Long): Flow<List<Behavior>>
    fun getCurrentBehavior(): Flow<Behavior?>
    fun getHomeBehaviors(dayStart: Long, dayEnd: Long): Flow<List<Behavior>>
    fun getTagsForBehavior(behaviorId: Long): Flow<List<Tag>>
    fun getPendingBehaviors(): Flow<List<Behavior>>
    suspend fun getBehaviorWithDetails(behaviorId: Long): BehaviorWithDetails?
    suspend fun getNextPending(): Behavior?
    suspend fun getMaxSequence(): Int
    suspend fun insert(behavior: Behavior, tagIds: List<Long> = emptyList()): Long
    suspend fun setEndTime(id: Long, endTime: Long)
    suspend fun setStatus(id: Long, status: String)
    suspend fun setStartTime(id: Long, startTime: Long)
    suspend fun setActualDuration(id: Long, duration: Long)
    suspend fun setAchievementLevel(id: Long, level: Int)
    suspend fun setSequence(id: Long, sequence: Int)
    suspend fun setNote(id: Long, note: String?)
    suspend fun endCurrentBehavior(endTime: Long)
    suspend fun completeCurrentAndStartNext(currentId: Long, idleMode: Boolean): Behavior?
    suspend fun reorderGoals(orderedIds: List<Long>)
    suspend fun delete(id: Long)
    suspend fun settleDay(dayStart: Long, dayEnd: Long)
}
