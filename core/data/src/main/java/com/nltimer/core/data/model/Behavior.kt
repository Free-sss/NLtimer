package com.nltimer.core.data.model

data class Behavior(
    val id: Long,
    val activityId: Long,
    val startTime: Long,
    val endTime: Long?,
    val nature: BehaviorNature,
    val note: String?,
    val pomodoroCount: Int,
)
