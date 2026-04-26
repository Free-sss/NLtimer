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

    @Query("UPDATE behaviors SET note = :note WHERE id = :id")
    suspend fun setNote(id: Long, note: String?)

    @Query("DELETE FROM behaviors WHERE id = :id")
    suspend fun delete(id: Long)

    @Query(
        """
        SELECT * FROM behaviors
        WHERE startTime >= :dayStart AND startTime < :dayEnd
        ORDER BY startTime ASC
        """
    )
    fun getByDayRange(dayStart: Long, dayEnd: Long): Flow<List<BehaviorEntity>>

    @Query("SELECT * FROM behaviors WHERE nature = 'CURRENT' AND endTime IS NULL LIMIT 1")
    fun getCurrentBehavior(): Flow<BehaviorEntity?>

    @Query("UPDATE behaviors SET endTime = :endTime WHERE nature = 'CURRENT' AND endTime IS NULL")
    suspend fun endCurrentBehavior(endTime: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTagCrossRef(crossRef: BehaviorTagCrossRefEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTagCrossRefs(crossRefs: List<BehaviorTagCrossRefEntity>)

    @Query("SELECT * FROM behaviors WHERE id = :id")
    suspend fun getById(id: Long): BehaviorEntity?

    @Query(
        """
        SELECT t.* FROM tags t
        INNER JOIN behavior_tag_cross_ref btc ON t.id = btc.tagId
        WHERE btc.behaviorId = :behaviorId
        ORDER BY t.priority DESC, t.name
        """
    )
    fun getTagsForBehavior(behaviorId: Long): Flow<List<TagEntity>>
}
