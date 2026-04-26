package com.nltimer.core.data.repository

import com.nltimer.core.data.model.Behavior
import com.nltimer.core.data.model.BehaviorWithDetails
import com.nltimer.core.data.model.Tag
import kotlinx.coroutines.flow.Flow

interface BehaviorRepository {
    fun getByDayRange(dayStart: Long, dayEnd: Long): Flow<List<Behavior>>
    fun getCurrentBehavior(): Flow<Behavior?>
    fun getTagsForBehavior(behaviorId: Long): Flow<List<Tag>>
    suspend fun getBehaviorWithDetails(behaviorId: Long): BehaviorWithDetails?
    suspend fun insert(behavior: Behavior, tagIds: List<Long> = emptyList()): Long
    suspend fun setEndTime(id: Long, endTime: Long)
    suspend fun setNote(id: Long, note: String?)
    suspend fun endCurrentBehavior(endTime: Long)
    suspend fun delete(id: Long)
}
