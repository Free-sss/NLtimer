package com.nltimer.core.data.repository

import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun getAllActive(): Flow<List<Activity>>
    fun getAll(): Flow<List<Activity>>
    fun getAllGroups(): Flow<List<ActivityGroup>>
    fun search(query: String): Flow<List<Activity>>
    suspend fun getById(id: Long): Activity?
    suspend fun getByName(name: String): Activity?
    suspend fun insert(activity: Activity): Long
    suspend fun update(activity: Activity)
    suspend fun setArchived(id: Long, archived: Boolean)
}
