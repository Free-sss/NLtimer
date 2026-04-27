package com.nltimer.core.data.repository

import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getDistinctActivityCategories(parent: String? = null): Flow<List<String>>
    suspend fun addActivityCategory(name: String)
    suspend fun renameActivityCategory(oldName: String, newName: String, parent: String? = null)
    suspend fun resetActivityCategory(category: String)

    fun getDistinctTagCategories(parent: String? = null): Flow<List<String>>
    suspend fun renameTagCategory(oldName: String, newName: String, parent: String? = null)
    suspend fun resetTagCategory(category: String)
}
