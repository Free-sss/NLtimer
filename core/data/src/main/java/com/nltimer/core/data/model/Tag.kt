package com.nltimer.core.data.model

data class Tag(
    val id: Long,
    val name: String,
    val color: Long?,
    val textColor: Long?,
    val icon: String?,
    val category: String?,
    val priority: Int,
    val usageCount: Int,
    val sortOrder: Int,
    val isArchived: Boolean,
)
