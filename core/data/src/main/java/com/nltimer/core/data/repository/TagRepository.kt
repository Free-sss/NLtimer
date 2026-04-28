package com.nltimer.core.data.repository

import com.nltimer.core.data.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun getAllActive(): Flow<List<Tag>>
    fun getAll(): Flow<List<Tag>>
    fun getByCategory(category: String): Flow<List<Tag>>
    fun search(query: String): Flow<List<Tag>>
    fun getByActivityId(activityId: Long): Flow<List<Tag>>
    suspend fun getById(id: Long): Tag?
    suspend fun getByName(name: String): Tag?
    suspend fun insert(tag: Tag): Long
    suspend fun update(tag: Tag)
    suspend fun setArchived(id: Long, archived: Boolean)
    
    fun getDistinctCategories(): Flow<List<String>>
    suspend fun renameCategory(oldName: String, newName: String)
    suspend fun resetCategory(category: String)
}
