package com.nltimer.core.data.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys = OFF")
        runCatching {
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

            db.execSQL("ALTER TABLE activity_groups ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE activity_groups ADD COLUMN archivedAt INTEGER")

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
        }.also {
            db.execSQL("PRAGMA foreign_keys = ON")
        }.getOrThrow()
    }
}
