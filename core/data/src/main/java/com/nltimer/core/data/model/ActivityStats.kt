package com.nltimer.core.data.model

data class ActivityStats(
    val usageCount: Int = 0,
    val totalDurationMinutes: Long = 0,
    val lastUsedTimestamp: Long? = null,
)
