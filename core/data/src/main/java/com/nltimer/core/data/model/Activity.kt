package com.nltimer.core.data.model

import com.nltimer.core.data.database.entity.ActivityEntity

/**
 * Activity 活动领域模型
 * 表示一个可计时、可归档的活动条目，对应数据库中的 activities 表
 */
data class Activity(
    val id: Long = 0,
    val name: String,
    val emoji: String? = null,
    val iconKey: String? = null,
    val groupId: Long? = null,
    val isPreset: Boolean = false,
    val isArchived: Boolean = false,
) {
    /** 转换为数据库实体 */
    fun toEntity() = ActivityEntity(
        id = id,
        name = name,
        emoji = emoji,
        iconKey = iconKey,
        groupId = groupId,
        isPreset = isPreset,
        isArchived = isArchived,
    )

    companion object {
        /** 从数据库实体构建领域模型 */
        fun fromEntity(entity: ActivityEntity) = Activity(
            id = entity.id,
            name = entity.name,
            emoji = entity.emoji,
            iconKey = entity.iconKey,
            groupId = entity.groupId,
            isPreset = entity.isPreset,
            isArchived = entity.isArchived,
        )
    }
}
