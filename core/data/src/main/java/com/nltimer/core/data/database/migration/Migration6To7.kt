package com.nltimer.core.data.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys = OFF")
        runCatching {
            db.execSQL(
                """
                CREATE TABLE activity_tag_binding_new (
                    activityId INTEGER NOT NULL,
                    tagId INTEGER NOT NULL,
                    FOREIGN KEY (activityId) REFERENCES activities(id) ON DELETE CASCADE,
                    FOREIGN KEY (tagId) REFERENCES tags(id) ON DELETE CASCADE,
                    PRIMARY KEY (activityId, tagId)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO activity_tag_binding_new (activityId, tagId)
                SELECT activityId, tagId FROM activity_tag_binding
                """.trimIndent()
            )
            db.execSQL("DROP TABLE activity_tag_binding")
            db.execSQL("ALTER TABLE activity_tag_binding_new RENAME TO activity_tag_binding")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_activity_tag_binding_activityId ON activity_tag_binding(activityId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_activity_tag_binding_tagId ON activity_tag_binding(tagId)")
        }.also {
            db.execSQL("PRAGMA foreign_keys = ON")
        }.getOrThrow()
    }
}
