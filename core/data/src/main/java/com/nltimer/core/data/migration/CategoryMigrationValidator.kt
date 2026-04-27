package com.nltimer.core.data.migration

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nltimer.core.data.database.dao.ActivityGroupDao
import com.nltimer.core.data.database.entity.ActivityGroupEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryMigrationValidator @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val groupDao: ActivityGroupDao,
) {
    private val savedActivityCategoriesKey = stringPreferencesKey("saved_activity_categories")

    suspend fun migrateIfNeeded() {
        val prefs = dataStore.data.first()
        val raw = prefs[savedActivityCategoriesKey] ?: ""
        if (raw.isBlank()) return

        val savedCategories = raw.split(",").toSet()
        val existingNames = groupDao.getAll().first().map { it.name }.toSet()
        savedCategories
            .filter { it !in existingNames }
            .forEach { name ->
                val maxOrder = groupDao.getAll().first().maxOfOrNull { it.sortOrder } ?: -1
                groupDao.insert(ActivityGroupEntity(name = name, sortOrder = maxOrder + 1))
            }

        dataStore.edit { it.remove(savedActivityCategoriesKey) }
    }
}
