package com.nltimer.core.data.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_activities_isArchived` ON `activities` (`isArchived`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_activities_groupId` ON `activities` (`groupId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_tags_isArchived` ON `tags` (`isArchived`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_tags_category` ON `tags` (`category`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_behaviors_startTime_status` ON `behaviors` (`startTime`, `status`)")
    }
}
