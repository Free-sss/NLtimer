package com.nltimer.feature.home.ui.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
    // Mark-style-main
    if (tags.isEmpty()) return

    FlowRow(
        modifier = modifier,
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

            Text(
                text = tag.name,
                color = textColor,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .background(backgroundColor, RoundedCornerShape(14.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                    .clickable { onTagToggle(tag.id) }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}
