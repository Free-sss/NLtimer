package com.nltimer.core.data.repository

import com.nltimer.core.data.database.dao.ActivityDao
import com.nltimer.core.data.database.dao.TagDao
import com.nltimer.core.data.database.entity.ActivityEntity
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

    private val activityEntities = mutableListOf<ActivityEntity>()
    private val tagEntities = mutableListOf<TagEntity>()
    private val activityFlow = MutableStateFlow<List<ActivityEntity>>(emptyList())
    private val tagFlow = MutableStateFlow<List<TagEntity>>(emptyList())

    private val fakeActivityDao = object : ActivityDao {
        override suspend fun insert(activity: ActivityEntity): Long {
            val id = activityEntities.size.toLong() + 1
            activityEntities.add(activity.copy(id = id))
            activityFlow.value = activityEntities.toList()
            return id
        }

        override suspend fun update(activity: ActivityEntity) {}
        override suspend fun delete(activity: ActivityEntity) {}
        override fun getAllActive(): Flow<List<ActivityEntity>> = flowOf(emptyList())
        override fun getAll(): Flow<List<ActivityEntity>> = activityFlow
        override suspend fun getById(id: Long): ActivityEntity? = null
        override suspend fun getByName(name: String): ActivityEntity? = null
        override fun getByCategory(category: String): Flow<List<ActivityEntity>> = flowOf(emptyList())
        override suspend fun setArchived(id: Long, archived: Boolean) {}
        override fun search(query: String): Flow<List<ActivityEntity>> = flowOf(emptyList())

        override fun getDistinctCategories(): Flow<List<String>> =
            activityFlow.map { entities ->
                entities.mapNotNull { it.category }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
            }

        override suspend fun renameCategory(oldName: String, newName: String) {
            activityEntities.replaceAll {
                if (it.category == oldName) it.copy(category = newName) else it
            }
            activityFlow.value = activityEntities.toList()
        }

        override suspend fun resetCategory(category: String) {
            activityEntities.replaceAll {
                if (it.category == category) it.copy(category = null) else it
            }
            activityFlow.value = activityEntities.toList()
        }
    }

    private val fakeTagDao = object : TagDao {
        override suspend fun insert(tag: TagEntity): Long {
            val id = tagEntities.size.toLong() + 1
            tagEntities.add(tag.copy(id = id))
            tagFlow.value = tagEntities.toList()
            return id
        }
        override suspend fun update(tag: TagEntity) {}
        override suspend fun delete(tag: TagEntity) {}
        override fun getAllActive(): Flow<List<TagEntity>> = flowOf(emptyList())
        override fun getAll(): Flow<List<TagEntity>> = tagFlow
        override suspend fun getById(id: Long): TagEntity? = null
        override suspend fun getByName(name: String): TagEntity? = null
        override fun getByCategory(category: String): Flow<List<TagEntity>> = flowOf(emptyList())
        override suspend fun setArchived(id: Long, archived: Boolean) {}
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
    }

    private val repository = CategoryRepositoryImpl(fakeActivityDao, fakeTagDao)

    @Test
    fun getDistinctActivityCategories_filtersNullAndEmpty() = runTest {
        fakeActivityDao.insert(ActivityEntity(name = "跑步", category = "运动"))
        fakeActivityDao.insert(ActivityEntity(name = "阅读", category = null))
        fakeActivityDao.insert(ActivityEntity(name = "冥想", category = ""))
        fakeActivityDao.insert(ActivityEntity(name = "游泳", category = "运动"))

        val categories = repository.getDistinctActivityCategories().first()

        assertEquals(listOf("运动"), categories)
    }

    @Test
    fun getDistinctActivityCategories_returnsDistinctSorted() = runTest {
        fakeActivityDao.insert(ActivityEntity(name = "A", category = "工作"))
        fakeActivityDao.insert(ActivityEntity(name = "B", category = "学习"))
        fakeActivityDao.insert(ActivityEntity(name = "C", category = "工作"))

        val categories = repository.getDistinctActivityCategories().first()

        assertEquals(listOf("学习", "工作"), categories)
    }

    @Test
    fun renameActivityCategory_updatesAllMatching() = runTest {
        fakeActivityDao.insert(ActivityEntity(name = "跑步", category = "运动"))
        fakeActivityDao.insert(ActivityEntity(name = "游泳", category = "运动"))
        fakeActivityDao.insert(ActivityEntity(name = "阅读", category = "学习"))

        val beforeRename = activityFlow.value.map { it.category }.filterNotNull().distinct().sorted()
        assertEquals(listOf("学习", "运动"), beforeRename)

        repository.renameActivityCategory("运动", "体育")

        val afterRename = activityFlow.value.map { it.category }.filterNotNull().distinct().sorted()
        assertTrue(afterRename.contains("体育"))
        assertTrue(afterRename.contains("学习"))
    }

    @Test
    fun resetActivityCategory_setsCategoryToNull() = runTest {
        fakeActivityDao.insert(ActivityEntity(name = "跑步", category = "运动"))
        fakeActivityDao.insert(ActivityEntity(name = "阅读", category = "学习"))

        repository.resetActivityCategory("运动")

        val categories = repository.getDistinctActivityCategories().first()
        assertEquals(listOf("学习"), categories)
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
