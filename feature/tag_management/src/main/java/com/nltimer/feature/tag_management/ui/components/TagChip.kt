package com.nltimer.feature.tag_management.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.nltimer.core.data.model.Tag

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TagChip(
    tag: Tag,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = tag.color?.let { Color(it) }
        ?: MaterialTheme.colorScheme.surfaceVariant

    val contentColor = tag.textColor?.let { Color(it) }
        ?: MaterialTheme.colorScheme.onSurfaceVariant

    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = "#${tag.name}",
                style = MaterialTheme.typography.bodySmall,
                color = contentColor,
            )
        },
        modifier = modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
        ),
        colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
            containerColor = backgroundColor,
            labelColor = contentColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.5f),
            disabledLabelColor = contentColor.copy(alpha = 0.5f),
        ),
    )
}
