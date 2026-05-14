package com.nltimer.core.designsystem.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

enum class AppTagChipStyle {
    Compact,
    Assist,
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppTagChip(
    label: String,
    color: Long?,
    modifier: Modifier = Modifier,
    prefixed: Boolean = false,
    style: AppTagChipStyle = AppTagChipStyle.Compact,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val chipColor = color?.let { Color(it) } ?: defaultTagContainerColor(style)
    val textColor = color?.let { readableContentColor(Color(it)) } ?: defaultTagContentColor(style)
    val text = if (prefixed) "#$label" else label

    when (style) {
        AppTagChipStyle.Compact -> Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier
                .then(clickableModifier(onClick, onLongClick))
                .background(chipColor, RoundedCornerShape(10.dp))
                .padding(horizontal = 6.dp, vertical = 1.dp),
        )
        AppTagChipStyle.Assist -> AssistChip(
            onClick = onClick ?: {},
            label = {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor,
                )
            },
            modifier = modifier.then(clickableModifier(onClick, onLongClick)),
            colors = AssistChipDefaults.assistChipColors(
                containerColor = chipColor,
                labelColor = textColor,
                disabledContainerColor = chipColor.copy(alpha = 0.5f),
                disabledLabelColor = textColor.copy(alpha = 0.5f),
            ),
        )
    }
}

private fun readableContentColor(background: Color): Color =
    if (background.luminance() > 0.5f) Color.Black else Color.White

@Composable
private fun defaultTagContainerColor(style: AppTagChipStyle): Color = when (style) {
    AppTagChipStyle.Compact -> MaterialTheme.colorScheme.primaryContainer
    AppTagChipStyle.Assist -> MaterialTheme.colorScheme.surfaceVariant
}

@Composable
private fun defaultTagContentColor(style: AppTagChipStyle): Color = when (style) {
    AppTagChipStyle.Compact -> MaterialTheme.colorScheme.onPrimaryContainer
    AppTagChipStyle.Assist -> MaterialTheme.colorScheme.onSurfaceVariant
}

@OptIn(ExperimentalFoundationApi::class)
private fun clickableModifier(
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
): Modifier = if (onClick != null || onLongClick != null) {
    Modifier.combinedClickable(
        onClick = onClick ?: {},
        onLongClick = onLongClick,
    )
} else {
    Modifier
}
