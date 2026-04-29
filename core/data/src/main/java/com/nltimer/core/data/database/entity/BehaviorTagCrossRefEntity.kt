package com.nltimer.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * BehaviorTagCrossRefEntity 行为-标签交叉引用实体
 * 多对多关联表，连接 behaviors 与 tags
 * 行为删除时级联删除关联记录；标签删除时受限
 */
@Entity(
    tableName = "behavior_tag_cross_ref",
    primaryKeys = ["behaviorId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = BehaviorEntity::class,
            parentColumns = ["id"],
            childColumns = ["behaviorId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("behaviorId"), Index("tagId")],
)
data class BehaviorTagCrossRefEntity(
    val behaviorId: Long,
    val tagId: Long,
)
