package com.nltimer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nltimer.core.data.database.entity.ActivityGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityGroupDao {
    @Query("SELECT * FROM activity_groups ORDER BY sortOrder ASC, id ASC")
    fun getAll(): Flow<List<ActivityGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: ActivityGroupEntity): Long

    @Update
    suspend fun update(group: ActivityGroupEntity)

    @Delete
    suspend fun delete(group: ActivityGroupEntity)

    @Query("UPDATE activities SET groupId = NULL WHERE groupId = :groupId")
    suspend fun ungroupAllActivities(groupId: Long)

    @Query("SELECT * FROM activity_groups WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): ActivityGroupEntity?

    @Query("UPDATE activity_groups SET name = :newName WHERE name = :oldName")
    suspend fun renameByName(oldName: String, newName: String)

    @Query("DELETE FROM activity_groups WHERE name = :name")
    suspend fun deleteByName(name: String)
}
