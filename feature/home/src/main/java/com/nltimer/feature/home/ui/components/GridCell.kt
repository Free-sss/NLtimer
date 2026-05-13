package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.model.GridLayoutStyle
import com.nltimer.core.designsystem.icon.IconRenderer
import com.nltimer.core.designsystem.theme.BorderTokens
import com.nltimer.core.designsystem.theme.ShapeTokens
import com.nltimer.core.designsystem.theme.appBorder
import com.nltimer.core.designsystem.theme.styledAlpha
import com.nltimer.core.designsystem.theme.styledBorder
import com.nltimer.core.designsystem.theme.styledCorner
import com.nltimer.feature.home.model.GridCellUiState

private const val PLATINUM_RED_BASE = 0.78f
private const val PLATINUM_GREEN_BASE = 0.69f
private const val PLATINUM_RED_STRENGTH = 0.22f
private const val PLATINUM_GREEN_STRENGTH = 0.31f

@Composable
fun GridCell(
    cell: GridCellUiState,
    modifier: Modifier = Modifier,
    gridStyle: GridLayoutStyle = GridLayoutStyle(),
) {
    val isPlatinum = cell.wasPlanned && cell.status == BehaviorNature.COMPLETED
    val platinumStrength = cell.achievementLevel?.let { it / 100f } ?: 0f

    val backgroundColor = when {
        cell.isCurrent -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = styledAlpha(gridStyle.activeBgAlpha))
        isPlatinum -> MaterialTheme.colorScheme.surfaceContainerLow
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .heightIn(max = gridStyle.maxCellHeight.dp)
            .clipToBounds()
            .background(backgroundColor, RoundedCornerShape(styledCorner(ShapeTokens.CORNER_MEDIUM)))
            .appBorder(
                borderProducer = {
                    val borderColor = when {
                        isPlatinum -> {
                            val strength = platinumStrength
                            Color(
                                red = (PLATINUM_RED_BASE + PLATINUM_RED_STRENGTH * strength),
                                green = (PLATINUM_GREEN_BASE + PLATINUM_GREEN_STRENGTH * strength),
                                blue = 1.0f,
                                alpha = styledAlpha(0.5f) + 0.5f * strength,
                            )
                        }
                        cell.isCurrent -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                    val borderWidth = if (cell.isCurrent || isPlatinum) styledBorder(BorderTokens.STANDARD) else styledBorder(BorderTokens.THIN)
                    BorderStroke(borderWidth, borderColor)
                },
                shape = RoundedCornerShape(styledCorner(ShapeTokens.CORNER_MEDIUM))
            )
            .padding(gridStyle.cellPadding.dp),
    ) {
        cell.activityIconKey?.let { iconKey ->
            Box(
                modifier = Modifier.matchParentSize(),
                contentAlignment = Alignment.BottomStart,
            ) {
                IconRenderer(
                    iconKey = iconKey,
                    defaultEmoji = "❓",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                    iconSize = (gridStyle.maxCellHeight * 0.2f).dp,
                    modifier = Modifier.padding(start = 2.dp, bottom = 2.dp),
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
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
                    modifier = Modifier.scale(gridStyle.tagScale),
                    horizontalArrangement = Arrangement.spacedBy(gridStyle.tagSpacing.dp),
                    verticalArrangement = Arrangement.spacedBy(gridStyle.tagSpacing.dp),
                    maxLines = 2,
                ) {
                    cell.tags.forEach { tag -> TagChip(tag = tag) }
                }
            }
            cell.note?.let { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = styledAlpha(0.6f))
                )
            }
        }
    }
}
