package com.nltimer.core.data.model

import com.nltimer.core.data.database.entity.ActivityGroupEntity

data class ActivityGroup(
    val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0,
) {
    fun toEntity() = ActivityGroupEntity(
        id = id,
        name = name,
        sortOrder = sortOrder,
    )

    companion object {
        fun fromEntity(entity: ActivityGroupEntity) = ActivityGroup(
            id = entity.id,
            name = entity.name,
            sortOrder = entity.sortOrder,
        )
    }
}
