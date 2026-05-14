package com.nltimer.core.data.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
        runCatching {
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
        }.also {
            db.execSQL("PRAGMA foreign_keys = ON")
        }.getOrThrow()
    }
}
