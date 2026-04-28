package com.nltimer.core.data.model

import com.nltimer.core.data.database.entity.ActivityEntity

data class Activity(
    val id: Long = 0,
    val name: String,
    val emoji: String? = null,
    val iconKey: String? = null,
    val groupId: Long? = null,
    val isPreset: Boolean = false,
    val isArchived: Boolean = false,
) {
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
