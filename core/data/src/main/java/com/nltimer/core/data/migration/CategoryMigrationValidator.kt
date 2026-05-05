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

/**
 * CategoryMigrationValidator 分类迁移校验器
 * 将旧版 DataStore 中保存的活动分类迁移到 activity_groups 表，迁移完成后清理旧键
 *
 * @param dataStore DataStore 偏好存储
 * @param groupDao 活动分组 DAO
 */
@Singleton
class CategoryMigrationValidator @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val groupDao: ActivityGroupDao,
) {
    private val savedActivityCategoriesKey = stringPreferencesKey("saved_activity_categories")

    suspend fun migrateIfNeeded() {
        // 读取旧 DataStore 中保存的分类列表
        val prefs = dataStore.data.first()
        val raw = prefs[savedActivityCategoriesKey] ?: ""
        if (raw.isBlank()) return

        val savedCategories = raw.split(",").toSet()
        // 获取数据库中已有的分组名称
        val existingNames = groupDao.getAll().first().map { it.name }.toSet()
        // 过滤出尚未创建的分组，批量插入（避免 N+1 查询）
        var maxOrder = groupDao.getAll().first().maxOfOrNull { it.sortOrder } ?: -1
        savedCategories
            .filter { it !in existingNames }
            .forEach { name ->
                maxOrder++
                groupDao.insert(ActivityGroupEntity(name = name, sortOrder = maxOrder))
            }

        // 迁移完成后删除旧 DataStore 键
        dataStore.edit { it.remove(savedActivityCategoriesKey) }
    }
}
