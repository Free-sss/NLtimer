package com.nltimer.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nltimer.core.data.database.dao.ActivityDao
import com.nltimer.core.data.database.dao.ActivityGroupDao
import com.nltimer.core.data.database.dao.BehaviorDao
import com.nltimer.core.data.database.dao.TagDao
import com.nltimer.core.data.database.entity.ActivityEntity
import com.nltimer.core.data.database.entity.ActivityGroupEntity
import com.nltimer.core.data.database.entity.ActivityTagBindingEntity
import com.nltimer.core.data.database.entity.BehaviorEntity
import com.nltimer.core.data.database.entity.BehaviorTagCrossRefEntity
import com.nltimer.core.data.database.entity.TagEntity

/**
 * NLtimerDatabase 主数据库类
 * 使用 Room 管理所有实体（活动、分组、标签、行为）及其 DAO
 */
@Database(
    entities = [
        ActivityEntity::class,
        ActivityGroupEntity::class,
        TagEntity::class,
        BehaviorEntity::class,
        ActivityTagBindingEntity::class,
        BehaviorTagCrossRefEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class NLtimerDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun activityGroupDao(): ActivityGroupDao
    abstract fun tagDao(): TagDao
    abstract fun behaviorDao(): BehaviorDao

    companion object {
        // 数据库从版本 3 到 4 的迁移：将 category 字段迁移到 activity_groups 表
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val now = System.currentTimeMillis()
                // 将 activities 中的分类去重写入 activity_groups
                db.execSQL(
                    """
                    INSERT INTO activity_groups (name, sortOrder, createdAt)
                    SELECT DISTINCT category, 0, $now
                    FROM activities
                    WHERE category IS NOT NULL AND category != ''
                      AND category NOT IN (SELECT name FROM activity_groups)
                    """.trimIndent()
                )
                // 用 groupId 外键替换原有的 category 字段
                db.execSQL(
                    """
                    UPDATE activities SET groupId = (
                        SELECT ag.id FROM activity_groups ag
                        WHERE ag.name = activities.category
                    ) WHERE category IS NOT NULL AND category != ''
                    """.trimIndent()
                )
                // 关闭外键约束后重建表，移除 category 列
                db.execSQL("PRAGMA foreign_keys = OFF")
                db.execSQL(
                    """
                    CREATE TABLE activities_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        emoji TEXT,
                        iconKey TEXT,
                        groupId INTEGER,
                        isPreset INTEGER NOT NULL DEFAULT 0,
                        isArchived INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                // 迁移所有数据到新表
                db.execSQL(
                    """
                    INSERT INTO activities_new (id, name, emoji, iconKey, groupId, isPreset, isArchived, createdAt, updatedAt)
                    SELECT id, name, emoji, iconKey, groupId, isPreset, isArchived, createdAt, updatedAt
                    FROM activities
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE activities")
                db.execSQL("ALTER TABLE activities_new RENAME TO activities")
                db.execSQL("PRAGMA foreign_keys = ON")
            }
        }
    }
}
