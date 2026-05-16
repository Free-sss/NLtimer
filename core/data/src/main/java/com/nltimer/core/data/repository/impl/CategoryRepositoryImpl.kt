package com.nltimer.core.data.repository.impl

import com.nltimer.core.data.database.dao.ActivityGroupDao
import com.nltimer.core.data.database.dao.TagDao
import com.nltimer.core.data.database.entity.ActivityGroupEntity
import com.nltimer.core.data.database.NLtimerDatabase
import com.nltimer.core.data.repository.CategoryRepository
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.room.withTransaction
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CategoryRepositoryImpl 分类仓库实现
 * 分别管理活动分组和标签分类的增删改名操作
 *
 * @param groupDao 活动分组数据访问对象
 * @param tagDao 标签数据访问对象
 */
@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val groupDao: ActivityGroupDao,
    private val tagDao: TagDao,
    private val database: NLtimerDatabase,
) : CategoryRepository {

    companion object {
        private const val TAG = "DB_CategoryDao"
    }

    override fun getDistinctActivityCategories(parent: String?): Flow<List<String>> =
        // 获取所有分组的名称并按字母排序
        groupDao.getAll().map { groups ->
            groups.map { it.name }.sorted()
        }

    override suspend fun addActivityCategory(name: String) {
        val maxOrder = groupDao.getMaxSortOrder() ?: -1
        val id = groupDao.insert(ActivityGroupEntity(name = name, sortOrder = maxOrder + 1))
        Log.d(TAG, "✅ addActivityCategory id=$id name=$name")
    }

    override suspend fun renameActivityCategory(oldName: String, newName: String, parent: String?) {
        groupDao.renameByName(oldName, newName)
        Log.d(TAG, "✅ renameActivityCategory oldName=$oldName newName=$newName")
    }

    override suspend fun resetActivityCategory(category: String) {
        database.withTransaction {
            val group = groupDao.getByName(category) ?: return@withTransaction
            groupDao.ungroupAllActivities(group.id)
            groupDao.delete(group)
            Log.d(TAG, "✅ resetActivityCategory category=$category")
        }
    }

    override fun getDistinctTagCategories(parent: String?): Flow<List<String>> =
        tagDao.getDistinctCategories()

    override suspend fun renameTagCategory(oldName: String, newName: String, parent: String?) {
        tagDao.renameCategory(oldName, newName)
        Log.d(TAG, "✅ renameTagCategory oldName=$oldName newName=$newName")
    }

    override suspend fun resetTagCategory(category: String) {
        tagDao.resetCategory(category)
        Log.d(TAG, "✅ resetTagCategory category=$category")
    }
}
