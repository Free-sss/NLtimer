package com.nltimer.feature.home.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import com.nltimer.core.data.util.formatDuration

@Composable
fun LiveDurationText(
    startEpochMs: Long,
    isCurrent: Boolean,
    fallbackDurationMs: Long,
): String {
    val duration = if (isCurrent) {
        val elapsed by produceState(initialValue = System.currentTimeMillis() - startEpochMs) {
            while (true) {
                kotlinx.coroutines.delay(1000)
                value = System.currentTimeMillis() - startEpochMs
            }
        }
        elapsed
    } else {
        fallbackDurationMs
    }
    return if (duration > 0) formatDuration(duration) else ""
}

@Composable
fun LiveElapsedDuration(
    startEpochMs: Long,
    isCurrent: Boolean,
    fallbackDurationMs: Long,
): Long {
    return if (isCurrent) {
        val elapsed by produceState(initialValue = System.currentTimeMillis() - startEpochMs) {
            while (true) {
                kotlinx.coroutines.delay(1000)
                value = System.currentTimeMillis() - startEpochMs
            }
        }
        elapsed
    } else {
        fallbackDurationMs
    }
}
