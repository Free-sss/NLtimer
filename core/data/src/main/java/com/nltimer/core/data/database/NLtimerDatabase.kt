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
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val now = System.currentTimeMillis()
                db.execSQL(
                    """
                    INSERT INTO activity_groups (name, sortOrder, createdAt)
                    SELECT DISTINCT category, 0, $now
                    FROM activities
                    WHERE category IS NOT NULL AND category != ''
                      AND category NOT IN (SELECT name FROM activity_groups)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    UPDATE activities SET groupId = (
                        SELECT ag.id FROM activity_groups ag
                        WHERE ag.name = activities.category
                    ) WHERE category IS NOT NULL AND category != ''
                    """.trimIndent()
                )
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
