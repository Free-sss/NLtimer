package com.nltimer.core.data.repository.impl

import com.nltimer.core.data.database.dao.TagDao
import com.nltimer.core.data.model.Tag
import com.nltimer.core.data.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TagRepositoryImpl 标签仓库实现
 * 委托 TagDao 完成标签的增删改查及分类管理
 *
 * @param tagDao 标签数据访问对象
 */
@Singleton
class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao,
) : TagRepository {

    override fun getAllActive(): Flow<List<Tag>> =
        tagDao.getAllActive().map { list -> list.map { Tag.fromEntity(it) } }

    override fun getAll(): Flow<List<Tag>> =
        tagDao.getAll().map { list -> list.map { Tag.fromEntity(it) } }

    override fun getByCategory(category: String): Flow<List<Tag>> =
        tagDao.getByCategory(category).map { list -> list.map { Tag.fromEntity(it) } }

    override fun search(query: String): Flow<List<Tag>> =
        tagDao.search(query).map { list -> list.map { Tag.fromEntity(it) } }

    override fun getByActivityId(activityId: Long): Flow<List<Tag>> =
        tagDao.getByActivityId(activityId).map { list -> list.map { Tag.fromEntity(it) } }

    override suspend fun getById(id: Long): Tag? =
        tagDao.getById(id)?.let { Tag.fromEntity(it) }

    override suspend fun getByName(name: String): Tag? =
        tagDao.getByName(name)?.let { Tag.fromEntity(it) }

    override suspend fun insert(tag: Tag): Long =
        tagDao.insert(tag.toEntity())

    override suspend fun update(tag: Tag) =
        tagDao.update(tag.toEntity())

    override suspend fun setArchived(id: Long, archived: Boolean) =
        tagDao.setArchived(id, archived)

    override fun getDistinctCategories(): Flow<List<String>> =
        tagDao.getDistinctCategories()

    override suspend fun renameCategory(oldName: String, newName: String) =
        tagDao.renameCategory(oldName, newName)

    override suspend fun resetCategory(category: String) =
        tagDao.resetCategory(category)
}
