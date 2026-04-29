package com.nltimer.core.data.database.entity

import androidx.room.Entity

/**
 * ActivityTagBindingEntity 活动-标签关联实体
 * 多对多关联表，连接 activities 与 tags
 */
@Entity(
    tableName = "activity_tag_binding",
    primaryKeys = ["activityId", "tagId"],
)
data class ActivityTagBindingEntity(
    val activityId: Long,
    val tagId: Long,
)
