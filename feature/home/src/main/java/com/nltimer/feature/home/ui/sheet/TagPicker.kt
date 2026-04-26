package com.nltimer.feature.home.ui.sheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.Tag

@Composable
fun TagPicker(
    tags: List<Tag>,
    selectedTagIds: Set<Long>,
    onTagToggle: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (tags.isEmpty()) return

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        tags.forEach { tag ->
            val isSelected = tag.id in selectedTagIds
            val tagColor = tag.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
            val backgroundColor = if (isSelected) {
                tagColor.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
            val borderColor = if (isSelected) tagColor else Color.Transparent
            val textColor = if (isSelected) tagColor else MaterialTheme.colorScheme.onSurfaceVariant

            Surface(
                onClick = { onTagToggle(tag.id) },
                shape = RoundedCornerShape(14.dp),
                color = backgroundColor,
                border = if (isSelected) BorderStroke(1.dp, borderColor) else null,
            ) {
                Text(
                    text = tag.name,
                    color = textColor,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}
