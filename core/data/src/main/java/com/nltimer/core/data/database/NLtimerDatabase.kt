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
    version = 10,
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
                try {
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
                } finally {
                    db.execSQL("PRAGMA foreign_keys = ON")
                }
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE activities ADD COLUMN color INTEGER DEFAULT NULL")
            }
        }

        // 数据库从版本 5 到 6 的迁移：为 activities、activity_groups、tags 的 name 字段添加唯一索引
        // 先处理重复数据（保留 id 最小的记录），再创建唯一索引
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 关闭外键约束，避免删除重复记录时触发级联限制
                db.execSQL("PRAGMA foreign_keys = OFF")
                try {

                // 将 behaviors 中引用了重复 activity 的记录指向保留的 activity
                db.execSQL(
                    """
                    UPDATE behaviors SET activityId = (
                        SELECT MIN(a2.id) FROM activities a2
                        WHERE a2.name = (SELECT a1.name FROM activities a1 WHERE a1.id = behaviors.activityId)
                    )
                    """.trimIndent()
                )
                // 将 activity_tag_binding 中引用了重复 activity 的记录指向保留的 activity
                db.execSQL(
                    """
                    UPDATE activity_tag_binding SET activityId = (
                        SELECT MIN(a2.id) FROM activities a2
                        WHERE a2.name = (SELECT a1.name FROM activities a1 WHERE a1.id = activity_tag_binding.activityId)
                    )
                    """.trimIndent()
                )
                // 删除 activities 中的重复 name 记录，保留 id 最小的
                db.execSQL(
                    """
                    DELETE FROM activities WHERE id NOT IN (
                        SELECT MIN(id) FROM activities GROUP BY name
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_activities_name ON activities(name)")

                // 将 activities 中引用了重复 group 的记录指向保留的 group
                db.execSQL(
                    """
                    UPDATE activities SET groupId = (
                        SELECT MIN(g2.id) FROM activity_groups g2
                        WHERE g2.name = (SELECT g1.name FROM activity_groups g1 WHERE g1.id = activities.groupId)
                    ) WHERE groupId IS NOT NULL
                    """.trimIndent()
                )
                // 删除 activity_groups 中的重复 name 记录，保留 id 最小的
                db.execSQL(
                    """
                    DELETE FROM activity_groups WHERE id NOT IN (
                        SELECT MIN(id) FROM activity_groups GROUP BY name
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_activity_groups_name ON activity_groups(name)")

                // 将 activity_tag_binding 中引用了重复 tag 的记录指向保留的 tag
                db.execSQL(
                    """
                    UPDATE activity_tag_binding SET tagId = (
                        SELECT MIN(t2.id) FROM tags t2
                        WHERE t2.name = (SELECT t1.name FROM tags t1 WHERE t1.id = activity_tag_binding.tagId)
                    )
                    """.trimIndent()
                )
                // 将 behavior_tag_cross_ref 中引用了重复 tag 的记录指向保留的 tag
                db.execSQL(
                    """
                    UPDATE behavior_tag_cross_ref SET tagId = (
                        SELECT MIN(t2.id) FROM tags t2
                        WHERE t2.name = (SELECT t1.name FROM tags t1 WHERE t1.id = behavior_tag_cross_ref.tagId)
                    )
                    """.trimIndent()
                )
                // 删除 tags 中的重复 name 记录，保留 id 最小的
                db.execSQL(
                    """
                    DELETE FROM tags WHERE id NOT IN (
                        SELECT MIN(id) FROM tags GROUP BY name
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_tags_name ON tags(name)")

                } finally {
                    // 重新开启外键约束
                    db.execSQL("PRAGMA foreign_keys = ON")
                }
            }
        }

        // 数据库从版本 7 到 8 的迁移：重建 behaviors 表索引
        // 将单列索引 index_behaviors_startTime 替换为复合索引 (startTime, sequence)
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 删除旧索引
                db.execSQL("DROP INDEX IF EXISTS index_behaviors_startTime")
                // 创建新的复合索引
                db.execSQL("CREATE INDEX IF NOT EXISTS index_behaviors_startTime_sequence ON behaviors(startTime, sequence)")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("PRAGMA foreign_keys = OFF")
                try {
                    // 1. 重建 activities 表：移除 emoji，新增 keywords/archivedAt/usageCount/color
                    db.execSQL(
                        """
                        CREATE TABLE activities_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            name TEXT NOT NULL,
                            iconKey TEXT,
                            keywords TEXT,
                            groupId INTEGER,
                            isPreset INTEGER NOT NULL DEFAULT 0,
                            isArchived INTEGER NOT NULL DEFAULT 0,
                            archivedAt INTEGER,
                            color INTEGER,
                            usageCount INTEGER NOT NULL DEFAULT 0,
                            createdAt INTEGER NOT NULL,
                            updatedAt INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        INSERT INTO activities_new (id, name, iconKey, keywords, groupId, isPreset, isArchived, archivedAt, color, usageCount, createdAt, updatedAt)
                        SELECT id, name, iconKey, NULL, groupId, isPreset, isArchived, NULL, color, 0, createdAt, updatedAt
                        FROM activities
                        """.trimIndent()
                    )
                    db.execSQL("DROP TABLE activities")
                    db.execSQL("ALTER TABLE activities_new RENAME TO activities")
                    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_activities_name ON activities(name)")

                    // 2. activity_groups 表：新增 isArchived 和 archivedAt 列
                    db.execSQL("ALTER TABLE activity_groups ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE activity_groups ADD COLUMN archivedAt INTEGER")

                    // 3. 重建 tags 表：移除 textColor，icon 重命名为 iconKey，新增 archivedAt
                    db.execSQL(
                        """
                        CREATE TABLE tags_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            name TEXT NOT NULL,
                            color INTEGER,
                            iconKey TEXT,
                            category TEXT,
                            priority INTEGER NOT NULL DEFAULT 0,
                            usageCount INTEGER NOT NULL DEFAULT 0,
                            sortOrder INTEGER NOT NULL DEFAULT 0,
                            isArchived INTEGER NOT NULL DEFAULT 0,
                            archivedAt INTEGER
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        INSERT INTO tags_new (id, name, color, iconKey, category, priority, usageCount, sortOrder, isArchived, archivedAt)
                        SELECT id, name, color, icon, category, priority, usageCount, sortOrder, isArchived, NULL
                        FROM tags
                        """.trimIndent()
                    )
                    db.execSQL("DROP TABLE tags")
                    db.execSQL("ALTER TABLE tags_new RENAME TO tags")
                    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_tags_name ON tags(name)")
                } finally {
                    db.execSQL("PRAGMA foreign_keys = ON")
                }
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_activities_isArchived` ON `activities` (`isArchived`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_activities_groupId` ON `activities` (`groupId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tags_isArchived` ON `tags` (`isArchived`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tags_category` ON `tags` (`category`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_behaviors_startTime_status` ON `behaviors` (`startTime`, `status`)")
            }
        }
    }
}
