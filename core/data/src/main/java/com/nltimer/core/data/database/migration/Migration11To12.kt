package com.nltimer.core.data.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys = OFF")
        runCatching {
            db.execSQL(
                """
                CREATE TABLE behaviors_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    activityId INTEGER NOT NULL,
                    startTime INTEGER NOT NULL,
                    endTime INTEGER,
                    status TEXT NOT NULL DEFAULT 'pending',
                    note TEXT,
                    pomodoroCount INTEGER NOT NULL DEFAULT 0,
                    sequence INTEGER NOT NULL DEFAULT 0,
                    estimatedDuration INTEGER,
                    actualDuration INTEGER,
                    achievementLevel INTEGER,
                    wasPlanned INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (activityId) REFERENCES activities(id) ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO behaviors_new (id, activityId, startTime, endTime, status, note, pomodoroCount, sequence, estimatedDuration, actualDuration, achievementLevel, wasPlanned)
                SELECT id, activityId, startTime, endTime, status, note, pomodoroCount, sequence, estimatedDuration, actualDuration, achievementLevel, wasPlanned
                FROM behaviors
                """.trimIndent()
            )
            db.execSQL("DROP TABLE behaviors")
            db.execSQL("ALTER TABLE behaviors_new RENAME TO behaviors")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_behaviors_activityId ON behaviors(activityId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_behaviors_startTime_sequence ON behaviors(startTime, sequence)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_behaviors_status ON behaviors(status)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_behaviors_startTime_status ON behaviors(startTime, status)")
        }.also {
            db.execSQL("PRAGMA foreign_keys = ON")
        }.getOrThrow()
    }
}
