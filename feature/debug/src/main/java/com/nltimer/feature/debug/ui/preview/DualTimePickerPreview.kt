package com.nltimer.feature.debug.ui.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.nltimer.feature.home.ui.sheet.DualTimePicker
import com.nltimer.feature.home.ui.sheet.SingleTimePicker
import java.time.LocalDateTime

@Preview
@Composable
fun DualTimePickerDebugPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        val now = LocalDateTime.now()
        DualTimePicker(
            startTime = now,
            endTime = now.plusHours(1),
            onDurationChanged = {},
        )
    }
}

@Preview
@Composable
fun SingleTimePickerDebugPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        SingleTimePicker(
            startTime = LocalDateTime.now(),
            onTimeChanged = {},
        )
    }
}
