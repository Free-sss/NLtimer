package com.nltimer.core.data.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys = OFF")
        runCatching {
            db.execSQL(
                """
                UPDATE behaviors SET activityId = (
                    SELECT MIN(a2.id) FROM activities a2
                    WHERE a2.name = (SELECT a1.name FROM activities a1 WHERE a1.id = behaviors.activityId)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                UPDATE activity_tag_binding SET activityId = (
                    SELECT MIN(a2.id) FROM activities a2
                    WHERE a2.name = (SELECT a1.name FROM activities a1 WHERE a1.id = activity_tag_binding.activityId)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                DELETE FROM activities WHERE id NOT IN (
                    SELECT MIN(id) FROM activities GROUP BY name
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_activities_name ON activities(name)")

            db.execSQL(
                """
                UPDATE activities SET groupId = (
                    SELECT MIN(g2.id) FROM activity_groups g2
                    WHERE g2.name = (SELECT g1.name FROM activity_groups g1 WHERE g1.id = activities.groupId)
                ) WHERE groupId IS NOT NULL
                """.trimIndent()
            )
            db.execSQL(
                """
                DELETE FROM activity_groups WHERE id NOT IN (
                    SELECT MIN(id) FROM activity_groups GROUP BY name
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_activity_groups_name ON activity_groups(name)")

            db.execSQL(
                """
                UPDATE activity_tag_binding SET tagId = (
                    SELECT MIN(t2.id) FROM tags t2
                    WHERE t2.name = (SELECT t1.name FROM tags t1 WHERE t1.id = activity_tag_binding.tagId)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                UPDATE behavior_tag_cross_ref SET tagId = (
                    SELECT MIN(t2.id) FROM tags t2
                    WHERE t2.name = (SELECT t1.name FROM tags t1 WHERE t1.id = behavior_tag_cross_ref.tagId)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                DELETE FROM tags WHERE id NOT IN (
                    SELECT MIN(id) FROM tags GROUP BY name
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_tags_name ON tags(name)")
        }.also {
            db.execSQL("PRAGMA foreign_keys = ON")
        }.getOrThrow()
    }
}
