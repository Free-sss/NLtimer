package com.nltimer.core.data.migration

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nltimer.core.data.database.dao.ActivityGroupDao
import com.nltimer.core.data.database.entity.ActivityGroupEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryMigrationValidatorTest {

    private lateinit var fakeGroupDao: FakeGroupDao
    private lateinit var fakeDataStore: FakeDataStore
    private lateinit var validator: CategoryMigrationValidator
    private val savedKey = stringPreferencesKey("saved_activity_categories")

    @Before
    fun setup() {
        fakeGroupDao = FakeGroupDao()
        fakeDataStore = FakeDataStore()
        validator = CategoryMigrationValidator(fakeDataStore, fakeGroupDao)
    }

    @Test
    fun `migrateIfNeeded does nothing when no saved categories`() = runTest {
        validator.migrateIfNeeded()
        assertTrue(fakeGroupDao.insertedGroups.isEmpty())
    }

    @Test
    fun `migrateIfNeeded migrates categories to groups`() = runTest {
        fakeDataStore.setValue(savedKey, "运动,学习,工作")
        validator.migrateIfNeeded()
        assertEquals(3, fakeGroupDao.insertedGroups.size)
        val names = fakeGroupDao.insertedGroups.map { it.name }.toSet()
        assertTrue(names.contains("运动"))
        assertTrue(names.contains("学习"))
        assertTrue(names.contains("工作"))
    }

    @Test
    fun `migrateIfNeeded skips already existing groups`() = runTest {
        fakeGroupDao.groups.add(ActivityGroupEntity(id = 1, name = "运动", sortOrder = 0))
        fakeDataStore.setValue(savedKey, "运动,学习")
        validator.migrateIfNeeded()
        assertEquals(1, fakeGroupDao.insertedGroups.size)
        assertEquals("学习", fakeGroupDao.insertedGroups[0].name)
    }

    @Test
    fun `migrateIfNeeded calculates correct sort order`() = runTest {
        fakeGroupDao.groups.add(ActivityGroupEntity(id = 1, name = "已有", sortOrder = 5))
        fakeDataStore.setValue(savedKey, "新A,新B")
        validator.migrateIfNeeded()
        assertEquals(2, fakeGroupDao.insertedGroups.size)
        assertEquals(6, fakeGroupDao.insertedGroups[0].sortOrder)
        assertEquals(7, fakeGroupDao.insertedGroups[1].sortOrder)
    }

    @Test
    fun `migrateIfNeeded cleans up DataStore key after migration`() = runTest {
        fakeDataStore.setValue(savedKey, "分类A")
        validator.migrateIfNeeded()
        assertTrue(fakeDataStore.removedKeys.contains(savedKey))
    }

    @Test
    fun `migrateIfNeeded with empty existing groups starts sortOrder at 0`() = runTest {
        fakeDataStore.setValue(savedKey, "新分类")
        validator.migrateIfNeeded()
        assertEquals(1, fakeGroupDao.insertedGroups.size)
        assertEquals(0, fakeGroupDao.insertedGroups[0].sortOrder)
    }

    @Test
    fun `migrateIfNeeded with all existing groups still cleans up DataStore`() = runTest {
        fakeGroupDao.groups.add(ActivityGroupEntity(id = 1, name = "运动", sortOrder = 0))
        fakeDataStore.setValue(savedKey, "运动")
        validator.migrateIfNeeded()
        assertTrue(fakeGroupDao.insertedGroups.isEmpty())
        assertTrue(fakeDataStore.removedKeys.contains(savedKey))
    }

    private class FakeDataStore : DataStore<Preferences> {
        private val prefs = mutablePreferencesOf()
        val removedKeys = mutableListOf<androidx.datastore.preferences.core.Preferences.Key<*>>()
        override val data: Flow<Preferences> = MutableStateFlow(prefs as Preferences)

        fun setValue(key: androidx.datastore.preferences.core.Preferences.Key<String>, value: String) {
            prefs[key] = value
        }

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            val result = transform(prefs)
            // Track removed keys
            for (key in prefs.asMap().keys) {
                if (key !in result.asMap()) removedKeys.add(key)
            }
            return result
        }
    }

    private class FakeGroupDao : ActivityGroupDao {
        val groups = mutableListOf<ActivityGroupEntity>()
        val insertedGroups = mutableListOf<ActivityGroupEntity>()
        private val groupFlow = MutableStateFlow<List<ActivityGroupEntity>>(emptyList())

        override fun getAll(): Flow<List<ActivityGroupEntity>> {
            groupFlow.value = groups.toList()
            return groupFlow
        }
        override suspend fun insert(group: ActivityGroupEntity): Long {
            val id = (groups.size + insertedGroups.size + 1).toLong()
            val inserted = group.copy(id = id)
            insertedGroups.add(inserted)
            return id
        }
        override suspend fun update(group: ActivityGroupEntity) {}
        override suspend fun delete(group: ActivityGroupEntity) {}
        override suspend fun ungroupAllActivities(groupId: Long) {}
        override suspend fun getByName(name: String): ActivityGroupEntity? = null
        override suspend fun renameByName(oldName: String, newName: String) {}
        override suspend fun deleteByName(name: String) {}
        override suspend fun deleteAll() {}
        override suspend fun getMaxSortOrder(): Int? = groups.maxOfOrNull { it.sortOrder }
        override suspend fun getById(id: Long): ActivityGroupEntity? = groups.find { it.id == id }
        override suspend fun getAllSync(): List<ActivityGroupEntity> = emptyList()
    }
}
