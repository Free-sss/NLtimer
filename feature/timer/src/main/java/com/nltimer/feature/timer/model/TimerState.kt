package com.nltimer.feature.timer.model

data class TimerState(
    val elapsedSeconds: Long = 0,
    val isRunning: Boolean = false,
)
