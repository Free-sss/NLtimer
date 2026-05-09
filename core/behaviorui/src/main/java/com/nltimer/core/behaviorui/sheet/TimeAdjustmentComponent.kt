package com.nltimer.core.behaviorui.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime

@Composable
fun TimeAdjustmentComponent(
    currentTime: LocalDateTime,
    onTimeChanged: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
    maxTime: LocalDateTime? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val row1 = listOf(
            "重置" to { onTimeChanged(LocalDateTime.now().withSecond(0).withNano(0)) },
            "-1" to { onTimeChanged(currentTime.plusMinutes(-1)) },
            "-5" to { onTimeChanged(currentTime.plusMinutes(-5)) },
            "-15" to { onTimeChanged(currentTime.plusMinutes(-15)) }
        )
        val row2 = listOf(
            "现在" to { onTimeChanged(LocalDateTime.now().withSecond(0).withNano(0)) },
            "+1" to {
                val newTime = currentTime.plusMinutes(1)
                onTimeChanged(if (maxTime != null && newTime > maxTime) maxTime else newTime)
            },
            "+5" to {
                val newTime = currentTime.plusMinutes(5)
                onTimeChanged(if (maxTime != null && newTime > maxTime) maxTime else newTime)
            },
            "+15" to {
                val newTime = currentTime.plusMinutes(15)
                onTimeChanged(if (maxTime != null && newTime > maxTime) maxTime else newTime)
            }
        )

        listOf(row1, row2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                rowItems.forEach { (text, onClick) ->
                    TimeButton(
                        text = text,
                        onClick = onClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            maxLines = 1
        )
    }
}
