package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TimeSideBar(
    activeHours: Set<Int>,
    currentHour: Int,
    onHourClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Mark-style-main
    Column(
        modifier = modifier
            .width(20.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(
            text = "时",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        for (hour in 0..23) {
            val isActive = hour in activeHours
            val isCurrent = hour == currentHour

            val backgroundColor = when {
                isCurrent -> MaterialTheme.colorScheme.tertiary
                isActive -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.surfaceContainerHigh
            }
            val contentColor = when {
                isCurrent -> MaterialTheme.colorScheme.onTertiary
                isActive -> MaterialTheme.colorScheme.onPrimary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Box(
                modifier = Modifier
                    .size(if (isCurrent) 24.dp else 22.dp)
                    .background(backgroundColor, CircleShape)
                    .clickable { onHourClick(hour) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = hour.toString(),
                    color = contentColor,
                    fontSize = 9.sp,
                )
            }
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(16.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            )
        }
    }
}
