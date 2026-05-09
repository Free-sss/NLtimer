package com.nltimer.core.data.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP INDEX IF EXISTS index_behaviors_startTime")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_behaviors_startTime_sequence ON behaviors(startTime, sequence)")
    }
}
