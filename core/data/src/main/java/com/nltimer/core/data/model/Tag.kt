package com.nltimer.core.data.model

data class Tag(
    val id: Long,
    val name: String,
    val color: Long?,
    val category: String?,
    val priority: Int,
    val isArchived: Boolean,
)
