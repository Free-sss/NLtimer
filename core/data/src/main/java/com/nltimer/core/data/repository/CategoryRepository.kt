package com.nltimer.core.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * CategoryRepository 分类管理仓库接口
 * 分别管理活动分类与标签分类的增删改名操作
 */
interface CategoryRepository {
    fun getDistinctActivityCategories(parent: String? = null): Flow<List<String>>
    suspend fun addActivityCategory(name: String)
    suspend fun renameActivityCategory(oldName: String, newName: String, parent: String? = null)
    suspend fun resetActivityCategory(category: String)

    fun getDistinctTagCategories(parent: String? = null): Flow<List<String>>
    suspend fun renameTagCategory(oldName: String, newName: String, parent: String? = null)
    suspend fun resetTagCategory(category: String)
}
