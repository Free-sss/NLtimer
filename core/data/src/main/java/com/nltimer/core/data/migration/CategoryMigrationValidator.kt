package com.nltimer.core.data.migration

import com.nltimer.core.data.SettingsPrefs
import com.nltimer.core.data.database.dao.ActivityGroupDao
import com.nltimer.core.data.database.entity.ActivityGroupEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryMigrationValidator @Inject constructor(
    private val settingsPrefs: SettingsPrefs,
    private val groupDao: ActivityGroupDao,
) {
    suspend fun migrateIfNeeded() {
        val savedCategories = settingsPrefs.getSavedActivityCategories().first()
        if (savedCategories.isEmpty()) return

        val existingNames = groupDao.getAll().first().map { it.name }.toSet()
        savedCategories
            .filter { it !in existingNames }
            .forEach { name ->
                val maxOrder = groupDao.getAll().first().maxOfOrNull { it.sortOrder } ?: -1
                groupDao.insert(ActivityGroupEntity(name = name, sortOrder = maxOrder + 1))
            }

        settingsPrefs.saveActivityCategories(emptySet())
    }
}
