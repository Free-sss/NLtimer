package com.nltimer.core.data.model

data class Behavior(
    val id: Long,
    val activityId: Long,
    val startTime: Long,
    val endTime: Long?,
    val status: BehaviorNature,
    val note: String?,
    val pomodoroCount: Int,
    val sequence: Int,
    val estimatedDuration: Long?,
    val actualDuration: Long?,
    val achievementLevel: Int?,
    val wasPlanned: Boolean,
)
