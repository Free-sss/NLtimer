package com.nltimer.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private val OptionsRowPaddingHorizontal = 16.dp
private val OptionsGap = 8.dp
private val OptionsGapFromFab = 68.dp
private val OptionCornerRadius = 8.dp
private val OptionPaddingVertical = 12.dp
private val OptionTonalElevation = 4.dp
private val OptionShadowElevation = 4.dp

@Composable
fun FabDragOptions(
    state: DragFabState,
    options: List<String>,
    modifier: Modifier = Modifier,
) {
    if (!state.isDragging) return

    val density = LocalDensity.current
    val gapPx = with(density) { OptionsGapFromFab.toPx() }
    val fabBottomPx = state.fabLayoutPosition.y + state.fabSize.height
    val optionsY = fabBottomPx - state.optionsRowHeight - gapPx - state.boxPositionInWindow.y

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = OptionsRowPaddingHorizontal)
            .offset { IntOffset(0, optionsY.roundToInt()) }
            .onGloballyPositioned { coords ->
                state.optionsRowHeight = coords.size.height.toFloat()
            },
        horizontalArrangement = Arrangement.spacedBy(OptionsGap)
    ) {
        options.forEach { option ->
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .onGloballyPositioned { layoutCoordinates ->
                        val position = layoutCoordinates.positionInWindow()
                        val size = layoutCoordinates.size
                        state.optionsLayoutBounds[option] = Rect(
                            position.x,
                            position.y,
                            position.x + size.width,
                            position.y + size.height
                        )
                    },
                shape = RoundedCornerShape(OptionCornerRadius),
                color = if (state.hoveredOption == option)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = OptionTonalElevation,
                shadowElevation = OptionShadowElevation
            ) {
                Box(
                    modifier = Modifier.padding(vertical = OptionPaddingVertical),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (state.hoveredOption == option)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
