package com.nltimer.feature.behavior_management.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.model.BehaviorWithDetails
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BehaviorListItem(
    behaviorWithDetails: BehaviorWithDetails,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isEvenItem: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val bwd = behaviorWithDetails
    val behavior = bwd.behavior
    val activity = bwd.activity
    val tags = bwd.tags

    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        isEvenItem -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val dotColor = activity.color?.let { c ->
            android.graphics.Color.valueOf(c).let { cc ->
                androidx.compose.ui.graphics.Color(cc.red(), cc.green(), cc.blue(), cc.alpha())
            }
        } ?: MaterialTheme.colorScheme.primary

        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(dotColor),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (tags.isNotEmpty()) {
                    Text(
                        text = tags.joinToString("·") { it.name },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
                val statusText = when (behavior.status) {
                    BehaviorNature.COMPLETED -> "✓"
                    BehaviorNature.ACTIVE -> "▶"
                    BehaviorNature.PENDING -> "○"
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
                behavior.note?.let { note ->
                    if (note.isNotBlank()) {
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                    }
                }
            }
        }

        val timeText = formatTimeRange(behavior.startTime, behavior.endTime)
        Text(
            text = timeText,
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
    }
}

private fun formatTimeRange(start: Long, end: Long?): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val startTime = Instant.ofEpochMilli(start)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(formatter)
    val endTime = end?.let {
        Instant.ofEpochMilli(it)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(formatter)
    }
    return if (endTime != null) "$startTime - $endTime" else startTime
}
