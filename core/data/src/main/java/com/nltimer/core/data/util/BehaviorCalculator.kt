package com.nltimer.core.data.util

object BehaviorCalculator {

    data class CompletionResult(
        val durationMs: Long,
        val achievementLevel: Int?,
    )

    fun calculateCompletion(
        startTime: Long,
        endTime: Long,
        wasPlanned: Boolean,
        estimatedDurationMinutes: Long?,
    ): CompletionResult {
        val duration = (endTime - startTime).coerceAtLeast(0L)
        val achievementLevel = if (wasPlanned && estimatedDurationMinutes != null && estimatedDurationMinutes > 0) {
            val estimatedMs = estimatedDurationMinutes * MILLIS_PER_MINUTE
            val diff = kotlin.math.abs(duration - estimatedMs)
            val ratio = (diff.toDouble() / estimatedMs).coerceAtMost(1.0)
            ((1.0 - ratio) * 100).toInt().coerceIn(0, 100)
        } else null
        return CompletionResult(durationMs = duration, achievementLevel = achievementLevel)
    }
}
