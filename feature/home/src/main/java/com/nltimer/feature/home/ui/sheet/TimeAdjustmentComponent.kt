package com.nltimer.feature.home.ui.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime

@Composable
fun TimeAdjustmentComponent(
    currentTime: LocalDateTime,
    onTimeChanged: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TimeButton(text = "占位", onClick = { onTimeChanged(LocalDateTime.now()) })
            TimeButton(text = "-15") { onTimeChanged(currentTime.plusMinutes(-15)) }
            TimeButton(text = "-5") { onTimeChanged(currentTime.plusMinutes(-5)) }
            TimeButton(text = "-1") { onTimeChanged(currentTime.plusMinutes(-1)) }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TimeButton(text = "现在", onClick = { onTimeChanged(LocalDateTime.now()) })
            TimeButton(text = "+15") { onTimeChanged(currentTime.plusMinutes(15)) }
            TimeButton(text = "+5") { onTimeChanged(currentTime.plusMinutes(5)) }
            TimeButton(text = "+1") { onTimeChanged(currentTime.plusMinutes(1)) }
        }
    }
}

@Composable
private fun TimeButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(horizontal = 3.dp, vertical = 0.dp),
        modifier = Modifier.width(36.dp).height(20.dp),
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 0.dp),
        colors = ButtonDefaults.buttonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
            containerColor = Color.Transparent,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}
