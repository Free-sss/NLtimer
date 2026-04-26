package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TimeFloatingLabel(
    time: LocalTime,
    isCurrentRow: Boolean,
    modifier: Modifier = Modifier,
) {
    // Mark-style-main
    val backgroundColor = if (isCurrentRow) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }
    val contentColor = if (isCurrentRow) {
        MaterialTheme.colorScheme.onTertiary
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    Text(
        text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
        color = contentColor,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 2.dp),
    )
}
