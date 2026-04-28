package com.nltimer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nltimer.core.data.database.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(activity: ActivityEntity): Long

    @Update
    suspend fun update(activity: ActivityEntity)

    @Delete
    suspend fun delete(activity: ActivityEntity)

    @Query("SELECT * FROM activities WHERE isArchived = 0 ORDER BY name")
    fun getAllActive(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities ORDER BY name")
    fun getAll(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getById(id: Long): ActivityEntity?

    @Query("SELECT * FROM activities WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): ActivityEntity?

    @Query("UPDATE activities SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: Long, archived: Boolean)

    @Query("SELECT * FROM activities WHERE name LIKE '%' || :query || '%' AND isArchived = 0")
    fun search(query: String): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE groupId IS NULL AND isArchived = 0 ORDER BY name")
    fun getUncategorized(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE groupId = :groupId AND isArchived = 0 ORDER BY name")
    fun getByGroup(groupId: Long): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE isPreset = 1 AND isArchived = 0 ORDER BY name")
    fun getAllPresets(): Flow<List<ActivityEntity>>

    @Query("UPDATE activities SET groupId = :groupId WHERE id = :activityId")
    suspend fun moveToGroup(activityId: Long, groupId: Long?)

    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun deleteById(id: Long)
}
