package com.nltimer.feature.behavior_management.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.nltimer.core.data.util.formatEpochTimeRange
import com.nltimer.core.designsystem.component.cardColorForStrategy
import com.nltimer.core.designsystem.component.toComposeColor
import com.nltimer.core.designsystem.theme.BorderTokens
import com.nltimer.core.designsystem.theme.LocalTheme
import com.nltimer.core.designsystem.theme.ShapeTokens
import com.nltimer.core.designsystem.theme.appBorder
import com.nltimer.core.designsystem.theme.styledAlpha
import com.nltimer.core.designsystem.theme.styledBorder
import com.nltimer.core.designsystem.theme.styledCorner

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
    val strategy = LocalTheme.current.style.cardColorStrategy

    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = styledAlpha(0.3f))
        isEvenItem -> cardColorForStrategy(strategy, behaviorWithDetails.behavior.id.hashCode())
            .copy(alpha = styledAlpha(0.15f))
        else -> cardColorForStrategy(strategy, behaviorWithDetails.behavior.id.hashCode())
    }
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = styledAlpha(0.5f))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(styledCorner(ShapeTokens.CORNER_MEDIUM)))
            .background(bgColor)
            .appBorder(
                borderProducer = { BorderStroke(styledBorder(BorderTokens.THIN), borderColor) },
                shape = RoundedCornerShape(styledCorner(ShapeTokens.CORNER_MEDIUM)),
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val dotColor = activity.color.toComposeColor()

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
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
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
                val statusText = behavior.status.displaySymbol
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = styledAlpha(0.6f)),
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                    }
                }
            }
        }

        val timeText = formatEpochTimeRange(behavior.startTime, behavior.endTime)
        Text(
            text = timeText,
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
    }
}

