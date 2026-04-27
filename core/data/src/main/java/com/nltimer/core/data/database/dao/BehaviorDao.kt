package com.nltimer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nltimer.core.data.database.entity.BehaviorEntity
import com.nltimer.core.data.database.entity.BehaviorTagCrossRefEntity
import com.nltimer.core.data.database.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BehaviorDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(behavior: BehaviorEntity): Long

    @Query("UPDATE behaviors SET endTime = :endTime WHERE id = :id")
    suspend fun setEndTime(id: Long, endTime: Long)

    @Query("UPDATE behaviors SET status = :status WHERE id = :id")
    suspend fun setStatus(id: Long, status: String)

    @Query("UPDATE behaviors SET startTime = :startTime WHERE id = :id")
    suspend fun setStartTime(id: Long, startTime: Long)

    @Query("UPDATE behaviors SET actualDuration = :duration WHERE id = :id")
    suspend fun setActualDuration(id: Long, duration: Long)

    @Query("UPDATE behaviors SET achievementLevel = :level WHERE id = :id")
    suspend fun setAchievementLevel(id: Long, level: Int)

    @Query("UPDATE behaviors SET sequence = :sequence WHERE id = :id")
    suspend fun setSequence(id: Long, sequence: Int)

    @Query("UPDATE behaviors SET note = :note WHERE id = :id")
    suspend fun setNote(id: Long, note: String?)

    @Query("DELETE FROM behaviors WHERE id = :id")
    suspend fun delete(id: Long)

    @Query(
        """
        SELECT * FROM behaviors
        WHERE startTime >= :dayStart AND startTime < :dayEnd
        ORDER BY sequence ASC, startTime ASC
        """
    )
    fun getByDayRange(dayStart: Long, dayEnd: Long): Flow<List<BehaviorEntity>>

    @Query("SELECT * FROM behaviors WHERE status = 'active' AND endTime IS NULL LIMIT 1")
    fun getCurrentBehavior(): Flow<BehaviorEntity?>

    @Query("UPDATE behaviors SET endTime = :endTime, status = 'completed' WHERE status = 'active' AND endTime IS NULL")
    suspend fun endCurrentBehavior(endTime: Long)

    @Query(
        """
        SELECT * FROM behaviors
        WHERE (startTime >= :dayStart AND startTime < :dayEnd)
           OR status = 'pending'
        ORDER BY sequence ASC
        """
    )
    fun getHomeBehaviors(dayStart: Long, dayEnd: Long): Flow<List<BehaviorEntity>>

    @Query("SELECT * FROM behaviors WHERE status = 'pending' ORDER BY sequence ASC LIMIT 1")
    suspend fun getNextPending(): BehaviorEntity?

    @Query("SELECT COALESCE(MAX(sequence), -1) FROM behaviors")
    suspend fun getMaxSequence(): Int

    @Query("SELECT * FROM behaviors WHERE id = :id")
    suspend fun getById(id: Long): BehaviorEntity?

    @Query("SELECT * FROM behaviors WHERE status = 'pending' ORDER BY sequence ASC")
    fun getPendingBehaviors(): Flow<List<BehaviorEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTagCrossRef(crossRef: BehaviorTagCrossRefEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTagCrossRefs(crossRefs: List<BehaviorTagCrossRefEntity>)

    @Query(
        """
        DELETE FROM behavior_tag_cross_ref
        WHERE behaviorId = :behaviorId AND tagId IN (:tagIds)
        """
    )
    suspend fun removeTagCrossRefs(behaviorId: Long, tagIds: List<Long>)

    @Query(
        """
        SELECT t.* FROM tags t
        INNER JOIN behavior_tag_cross_ref btc ON t.id = btc.tagId
        WHERE btc.behaviorId = :behaviorId
        ORDER BY t.priority DESC, t.name
        """
    )
    fun getTagsForBehavior(behaviorId: Long): Flow<List<TagEntity>>

    @Query(
        """
        UPDATE behaviors
        SET activityId = :activityId,
            startTime = :startTime,
            endTime = :endTime,
            status = :status,
            note = :note
        WHERE id = :id
        """
    )
    suspend fun update(
        id: Long,
        activityId: Long,
        startTime: Long,
        endTime: Long?,
        status: String,
        note: String?,
    )

    @Query("DELETE FROM behavior_tag_cross_ref WHERE behaviorId = :behaviorId")
    suspend fun deleteTagsForBehavior(behaviorId: Long)

    @Query(
        """
        SELECT t.* FROM tags t
        INNER JOIN behavior_tag_cross_ref btc ON t.id = btc.tagId
        WHERE btc.behaviorId = :behaviorId
        ORDER BY t.priority DESC, t.name
        """
    )
    suspend fun getTagsForBehaviorSync(behaviorId: Long): List<TagEntity>
}
