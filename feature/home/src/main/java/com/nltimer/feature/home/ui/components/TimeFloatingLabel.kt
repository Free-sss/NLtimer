package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.theme.ShapeTokens
import com.nltimer.core.designsystem.theme.TimeLabelConfig
import com.nltimer.core.designsystem.theme.TimeLabelFormat
import com.nltimer.core.designsystem.theme.TimeLabelStyle
import com.nltimer.core.data.util.hhmmFormatter
import com.nltimer.core.designsystem.theme.styledCorner
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TimeFloatingLabel(
    time: LocalTime,
    isCurrentRow: Boolean,
    modifier: Modifier = Modifier,
    config: TimeLabelConfig = TimeLabelConfig(),
) {
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

    val formattedTime = formatTime(time, config.format)

    when (config.style) {
        TimeLabelStyle.PILL -> PillLabel(
            text = formattedTime,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            modifier = modifier,
        )
        TimeLabelStyle.PLAIN -> PlainLabel(
            text = formattedTime,
            contentColor = if (isCurrentRow) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.primary
            },
            modifier = modifier,
        )
        TimeLabelStyle.UNDERLINE -> UnderlineLabel(
            text = formattedTime,
            lineColor = backgroundColor,
            contentColor = if (isCurrentRow) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.primary
            },
            modifier = modifier,
        )
        TimeLabelStyle.DOT -> DotLabel(
            text = formattedTime,
            dotColor = backgroundColor,
            contentColor = if (isCurrentRow) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.primary
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun PillLabel(
    text: String,
    backgroundColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = contentColor,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(styledCorner(ShapeTokens.CORNER_EXTRA_SMALL)))
            .padding(horizontal = 10.dp, vertical = 2.dp),
    )
}

@Composable
private fun PlainLabel(
    text: String,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = contentColor,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 2.dp),
    )
}

@Composable
private fun UnderlineLabel(
    text: String,
    lineColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    val strokeWidth = 2.dp
    Text(
        text = text,
        color = contentColor,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .drawBehind {
                val strokeWidthPx = strokeWidth.toPx()
                drawLine(
                    color = lineColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeWidthPx,
                )
            },
    )
}

@Composable
private fun DotLabel(
    text: String,
    dotColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(dotColor, CircleShape),
            )
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

private fun formatTime(time: LocalTime, format: TimeLabelFormat): String {
    val formatter = when (format) {
        TimeLabelFormat.HH_MM -> hhmmFormatter
        TimeLabelFormat.H_MM -> DateTimeFormatter.ofPattern("H:mm")
        TimeLabelFormat.H_MM_A -> DateTimeFormatter.ofPattern("h:mm a")
    }
    return time.format(formatter)
}
