package com.nltimer.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DurationAssistChipRow(
    durations: List<Long>,
    selectedDuration: Duration?,
    onDurationSelect: (Duration) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        durations.forEach { minutes ->
            val duration = minutes.minutes
            val isSelected = duration == selectedDuration
            if (isSelected) {
                FilterChip(
                    selected = true,
                    onClick = { onDurationSelect(duration) },
                    label = { Text("${minutes}分") },
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            } else {
                AssistChip(
                    onClick = { onDurationSelect(duration) },
                    label = { Text("${minutes}分") },
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
        }
    }
}
