package com.nltimer.core.data.repository.impl

import com.nltimer.core.data.database.dao.ActivityDao
import com.nltimer.core.data.database.dao.TagDao
import com.nltimer.core.data.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val activityDao: ActivityDao,
    private val tagDao: TagDao,
) : CategoryRepository {

    override fun getDistinctActivityCategories(parent: String?): Flow<List<String>> =
        activityDao.getDistinctCategories()

    override suspend fun renameActivityCategory(oldName: String, newName: String, parent: String?) {
        activityDao.renameCategory(oldName, newName)
    }

    override suspend fun resetActivityCategory(category: String) {
        activityDao.resetCategory(category)
    }

    override fun getDistinctTagCategories(parent: String?): Flow<List<String>> =
        tagDao.getDistinctCategories()

    override suspend fun renameTagCategory(oldName: String, newName: String, parent: String?) {
        tagDao.renameCategory(oldName, newName)
    }

    override suspend fun resetTagCategory(category: String) {
        tagDao.resetCategory(category)
    }
}
