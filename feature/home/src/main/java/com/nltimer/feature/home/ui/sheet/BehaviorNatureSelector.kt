package com.nltimer.feature.home.ui.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.BehaviorNature

@Composable
fun BehaviorNatureSelector(
    selected: BehaviorNature,
    onSelect: (BehaviorNature) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        BehaviorNature.entries.forEach { nature ->
            val isSelected = nature == selected
            val label = when (nature) {
                BehaviorNature.CURRENT -> "当前 ●"
                BehaviorNature.COMPLETED -> "完成 ○"
                BehaviorNature.TARGET -> "目标 ○"
            }
            Text(
                text = label,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clickable { onSelect(nature) },
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
    }
}
