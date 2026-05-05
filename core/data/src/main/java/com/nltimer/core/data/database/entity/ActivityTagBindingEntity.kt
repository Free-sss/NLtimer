package com.nltimer.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * ActivityTagBindingEntity 活动-标签关联实体
 * 多对多关联表，连接 activities 与 tags
 */
@Entity(
    tableName = "activity_tag_binding",
    primaryKeys = ["activityId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = ActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("activityId"), Index("tagId")],
)
data class ActivityTagBindingEntity(
    val activityId: Long,
    val tagId: Long,
)
