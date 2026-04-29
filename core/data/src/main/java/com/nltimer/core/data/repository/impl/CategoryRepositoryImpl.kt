package com.nltimer.core.data.repository.impl

import com.nltimer.core.data.database.dao.ActivityGroupDao
import com.nltimer.core.data.database.dao.TagDao
import com.nltimer.core.data.database.entity.ActivityGroupEntity
import com.nltimer.core.data.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
) : CategoryRepository {

    override fun getDistinctActivityCategories(parent: String?): Flow<List<String>> =
        // 获取所有分组的名称并按字母排序
        groupDao.getAll().map { groups ->
            groups.map { it.name }.sorted()
        }

    override suspend fun addActivityCategory(name: String) {
        // 计算当前最大排序值，追加新分组到最后
        val existing = groupDao.getAll().first()
        val maxOrder = existing.maxOfOrNull { it.sortOrder } ?: -1
        groupDao.insert(ActivityGroupEntity(name = name, sortOrder = maxOrder + 1))
    }

    override suspend fun renameActivityCategory(oldName: String, newName: String, parent: String?) {
        groupDao.renameByName(oldName, newName)
    }

    override suspend fun resetActivityCategory(category: String) {
        // 先解绑分组下的所有活动，再删除分组本身
        val group = groupDao.getByName(category) ?: return
        groupDao.ungroupAllActivities(group.id)
        groupDao.delete(group)
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
