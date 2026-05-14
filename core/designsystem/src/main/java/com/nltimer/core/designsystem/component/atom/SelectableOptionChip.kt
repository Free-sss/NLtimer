package com.nltimer.core.designsystem.component.atom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SelectableOptionChip(
    text: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(10.dp),
    showCheckIcon: Boolean = true,
    showBorder: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    contentPaddingHorizontal: Dp = 10.dp,
    contentPaddingVertical: Dp = 6.dp,
) {
    Surface(
        onClick = onSelect,
        shape = shape,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerLow,
        border = if (selected && showBorder) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = contentPaddingHorizontal, vertical = contentPaddingVertical),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected && showCheckIcon) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.size(4.dp))
            }
            Text(
                text = text,
                style = textStyle,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
