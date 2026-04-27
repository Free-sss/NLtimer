package com.nltimer.core.data.repository

import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import kotlinx.coroutines.flow.Flow

interface ActivityManagementRepository {
    fun getAllActivities(): Flow<List<Activity>>
    fun getUncategorizedActivities(): Flow<List<Activity>>
    fun getActivitiesByGroup(groupId: Long): Flow<List<Activity>>
    fun getAllGroups(): Flow<List<ActivityGroup>>

    suspend fun addActivity(activity: Activity): Long
    suspend fun updateActivity(activity: Activity)
    suspend fun deleteActivity(id: Long)
    suspend fun moveActivityToGroup(activityId: Long, groupId: Long?)

    suspend fun addGroup(name: String): Long
    suspend fun renameGroup(id: Long, newName: String)
    suspend fun deleteGroup(id: Long)

    suspend fun initializePresets()
}
