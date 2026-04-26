package com.nltimer.core.data.model

data class Activity(
    val id: Long,
    val name: String,
    val emoji: String?,
    val iconKey: String?,
    val category: String?,
    val isArchived: Boolean,
)
