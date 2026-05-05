package com.nltimer.core.data.repository

import com.nltimer.core.data.database.dao.ActivityGroupDao
import com.nltimer.core.data.database.dao.TagDao
import com.nltimer.core.data.database.entity.ActivityGroupEntity
import com.nltimer.core.data.database.entity.TagEntity
import com.nltimer.core.data.repository.impl.CategoryRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryRepositoryTest {

    private val groupEntities = mutableListOf<ActivityGroupEntity>()
    private val tagEntities = mutableListOf<TagEntity>()
    private val groupFlow = MutableStateFlow<List<ActivityGroupEntity>>(emptyList())
    private val tagFlow = MutableStateFlow<List<TagEntity>>(emptyList())

    private val fakeGroupDao = object : ActivityGroupDao {
        override fun getAll(): Flow<List<ActivityGroupEntity>> = groupFlow

        override suspend fun insert(group: ActivityGroupEntity): Long {
            val id = groupEntities.size.toLong() + 1
            groupEntities.add(group.copy(id = id))
            groupFlow.value = groupEntities.toList()
            return id
        }

        override suspend fun update(group: ActivityGroupEntity) {
            groupEntities.replaceAll { if (it.id == group.id) group else it }
            groupFlow.value = groupEntities.toList()
        }

        override suspend fun delete(group: ActivityGroupEntity) {
            groupEntities.removeAll { it.id == group.id }
            groupFlow.value = groupEntities.toList()
        }

        override suspend fun ungroupAllActivities(groupId: Long) {}

        override suspend fun getByName(name: String): ActivityGroupEntity? =
            groupEntities.find { it.name == name }

        override suspend fun renameByName(oldName: String, newName: String) {
            groupEntities.replaceAll {
                if (it.name == oldName) it.copy(name = newName) else it
            }
            groupFlow.value = groupEntities.toList()
        }

        override suspend fun deleteByName(name: String) {
            groupEntities.removeAll { it.name == name }
            groupFlow.value = groupEntities.toList()
        }

        override suspend fun deleteAll() {
            groupEntities.clear()
            groupFlow.value = emptyList()
        }
    }

    private val fakeTagDao = object : TagDao {
        override suspend fun insert(tag: TagEntity): Long {
            val id = tagEntities.size.toLong() + 1
            tagEntities.add(tag.copy(id = id))
            tagFlow.value = tagEntities.toList()
            return id
        }
        override suspend fun update(tag: TagEntity) {
            tagEntities.replaceAll { if (it.id == tag.id) tag else it }
            tagFlow.value = tagEntities.toList()
        }
        override suspend fun delete(tag: TagEntity) {
            tagEntities.removeAll { it.id == tag.id }
            tagFlow.value = tagEntities.toList()
        }
        override fun getAllActive(): Flow<List<TagEntity>> = flowOf(emptyList())
        override fun getAll(): Flow<List<TagEntity>> = tagFlow
        override suspend fun getById(id: Long): TagEntity? = tagEntities.find { it.id == id }
        override suspend fun getByName(name: String): TagEntity? = tagEntities.find { it.name == name }
        override fun getByCategory(category: String): Flow<List<TagEntity>> = flowOf(emptyList())
        override suspend fun setArchived(id: Long, archived: Boolean) {
            tagEntities.replaceAll { if (it.id == id) it.copy(isArchived = archived) else it }
            tagFlow.value = tagEntities.toList()
        }
        override fun search(query: String): Flow<List<TagEntity>> = flowOf(emptyList())
        override fun getByActivityId(activityId: Long): Flow<List<TagEntity>> = flowOf(emptyList())
        override suspend fun getTagsForBehaviorSync(behaviorId: Long): List<TagEntity> = emptyList()

        override fun getDistinctCategories(): Flow<List<String>> =
            tagFlow.map { entities ->
                entities.mapNotNull { it.category }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
            }

        override suspend fun renameCategory(oldName: String, newName: String) {
            tagEntities.replaceAll {
                if (it.category == oldName) it.copy(category = newName) else it
            }
            tagFlow.value = tagEntities.toList()
        }

        override suspend fun resetCategory(category: String) {
            tagEntities.replaceAll {
                if (it.category == category) it.copy(category = null) else it
            }
            tagFlow.value = tagEntities.toList()
        }

        override suspend fun deleteAll() {
            tagEntities.clear()
            tagFlow.value = emptyList()
        }
    }

    private val repository = CategoryRepositoryImpl(fakeGroupDao, fakeTagDao)

    @Test
    fun getDistinctActivityCategories_returnsSortedNames() = runTest {
        fakeGroupDao.insert(ActivityGroupEntity(name = "运动"))
        fakeGroupDao.insert(ActivityGroupEntity(name = "学习"))

        val categories = repository.getDistinctActivityCategories().first()

        assertEquals(listOf("学习", "运动"), categories)
    }

    @Test
    fun addActivityCategory_createsNewGroup() = runTest {
        repository.addActivityCategory("工作")

        val categories = repository.getDistinctActivityCategories().first()

        assertEquals(listOf("工作"), categories)
    }

    @Test
    fun renameActivityCategory_updatesName() = runTest {
        fakeGroupDao.insert(ActivityGroupEntity(name = "运动"))

        repository.renameActivityCategory("运动", "体育")

        val categories = repository.getDistinctActivityCategories().first()
        assertEquals(listOf("体育"), categories)
    }

    @Test
    fun resetActivityCategory_deletesGroup() = runTest {
        fakeGroupDao.insert(ActivityGroupEntity(name = "运动"))

        repository.resetActivityCategory("运动")

        val categories = repository.getDistinctActivityCategories().first()
        assertTrue(categories.isEmpty())
    }

    @Test
    fun getDistinctTagCategories_filtersNullAndEmpty() = runTest {
        fakeTagDao.insert(TagEntity(name = "重要", category = "优先级"))
        fakeTagDao.insert(TagEntity(name = "次要", category = null))
        fakeTagDao.insert(TagEntity(name = "紧急", category = "优先级"))

        val categories = repository.getDistinctTagCategories().first()

        assertEquals(listOf("优先级"), categories)
    }

    @Test
    fun renameTagCategory_updatesAllMatching() = runTest {
        fakeTagDao.insert(TagEntity(name = "重要", category = "优先级"))
        fakeTagDao.insert(TagEntity(name = "已读", category = "状态"))

        val beforeRename = tagFlow.value.map { it.category }.filterNotNull().distinct().sorted()
        assertEquals(listOf("优先级", "状态"), beforeRename)

        repository.renameTagCategory("优先级", "重要程度")

        val afterRename = tagFlow.value.map { it.category }.filterNotNull().distinct().sorted()
        assertTrue(afterRename.contains("重要程度"))
        assertTrue(afterRename.contains("状态"))
    }

    @Test
    fun resetTagCategory_setsCategoryToNull() = runTest {
        fakeTagDao.insert(TagEntity(name = "重要", category = "优先级"))

        repository.resetTagCategory("优先级")

        val categories = repository.getDistinctTagCategories().first()
        assertTrue(categories.isEmpty())
    }
}
