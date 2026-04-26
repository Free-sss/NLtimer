package com.nltimer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nltimer.core.data.database.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: TagEntity): Long

    @Update
    suspend fun update(tag: TagEntity)

    @Delete
    suspend fun delete(tag: TagEntity)

    @Query("SELECT * FROM tags WHERE isArchived = 0 ORDER BY priority DESC, name")
    fun getAllActive(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags ORDER BY name")
    fun getAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getById(id: Long): TagEntity?

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): TagEntity?

    @Query("SELECT * FROM tags WHERE category = :category AND isArchived = 0 ORDER BY priority DESC, name")
    fun getByCategory(category: String): Flow<List<TagEntity>>

    @Query("UPDATE tags SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: Long, archived: Boolean)

    @Query("SELECT * FROM tags WHERE name LIKE '%' || :query || '%' AND isArchived = 0")
    fun search(query: String): Flow<List<TagEntity>>

    @Query(
        """
        SELECT t.* FROM tags t
        INNER JOIN activity_tag_binding atb ON t.id = atb.tagId
        WHERE atb.activityId = :activityId AND t.isArchived = 0
        ORDER BY t.priority DESC, t.name
        """
    )
    fun getByActivityId(activityId: Long): Flow<List<TagEntity>>

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
