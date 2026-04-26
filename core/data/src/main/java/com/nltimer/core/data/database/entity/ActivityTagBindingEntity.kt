package com.nltimer.core.data.database.entity

import androidx.room.Entity

@Entity(
    tableName = "activity_tag_binding",
    primaryKeys = ["activityId", "tagId"],
)
data class ActivityTagBindingEntity(
    val activityId: Long,
    val tagId: Long,
)
