package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.designsystem.theme.appBorder
import com.nltimer.feature.home.model.GridCellUiState

@Composable
fun GridCell(
    cell: GridCellUiState,
    modifier: Modifier = Modifier,
) {
    val isPlatinum = cell.wasPlanned && cell.status == BehaviorNature.COMPLETED && cell.achievementLevel != null
    val platinumStrength = if (isPlatinum) cell.achievementLevel / 100f else 0f

    val backgroundColor = when {
        cell.isCurrent -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        isPlatinum -> MaterialTheme.colorScheme.surfaceContainerLow
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .heightIn(max = 140.dp)
            .clipToBounds()
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .appBorder(
                borderProducer = {
                    val borderColor = when {
                        isPlatinum -> {
                            val strength = platinumStrength
                            Color(
                                red = (0.78f + 0.22f * strength),
                                green = (0.69f + 0.31f * strength),
                                blue = 1.0f,
                                alpha = 0.5f + 0.5f * strength,
                            )
                        }
                        cell.isCurrent -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                    val borderWidth = if (cell.isCurrent || isPlatinum) 2.dp else 1.dp
                    BorderStroke(borderWidth, borderColor)
                },
                shape = RoundedCornerShape(16.dp)
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        cell.activityEmoji?.let { emoji ->
            Text(text = emoji, style = MaterialTheme.typography.headlineMedium)
        }
        cell.activityName?.let { name ->
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (cell.tags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                cell.tags.forEach { tag -> TagChip(tag = tag) }
            }
        }
    }
}
