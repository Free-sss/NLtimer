package com.nltimer.feature.debug.ui.preview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nltimer.feature.home.ui.sheet.TimeAdjustmentComponent
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Preview(showBackground = true)
@Composable
fun TimeAdjustmentDebugPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        var currentTime by remember { mutableStateOf(LocalDateTime.now()) }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = currentTime.format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                ),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(24.dp))

            TimeAdjustmentComponent(
                currentTime = currentTime,
                onTimeChanged = { currentTime = it },
            )
        }
    }
}
