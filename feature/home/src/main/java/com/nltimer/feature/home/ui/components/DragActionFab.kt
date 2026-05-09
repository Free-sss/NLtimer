package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.nltimer.feature.home.model.AddSheetMode
import kotlin.math.roundToInt

private val FabCornerRadiusActive = 16.dp
private val FabCornerRadiusIdle = 28.dp
private val FabShadowElevation = 4.dp
private val FabContentPaddingHorizontal = 16.dp
private val FabContentPaddingVertical = 16.dp
private val FabIconSpacerWidth = 8.dp

internal class DragFabState {
    var isDragging by mutableStateOf(false)
    var dragOffset by mutableStateOf(Offset.Zero)
    var dragStartOffset by mutableStateOf(Offset.Zero)
    var hoveredOption by mutableStateOf<String?>(null)
    val optionsLayoutBounds: SnapshotStateMap<String, Rect> = SnapshotStateMap()
    var fabLayoutPosition by mutableStateOf(Offset.Zero)
    var fabSize by mutableStateOf(IntSize.Zero)
    var optionsRowHeight by mutableFloatStateOf(0f)
    var boxPositionInWindow by mutableStateOf(Offset.Zero)
}

@Composable
internal fun rememberDragFabState(): DragFabState = remember { DragFabState() }

@Composable
internal fun DragActionFab(
    state: DragFabState,
    hasActiveBehavior: Boolean,
    activeBehaviorId: Long?,
    onCompleteActive: () -> Unit,
    onShowAddSheet: (AddSheetMode) -> Unit,
    onFabClick: () -> Unit,
    onUnhandledOption: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }

    val cornerRadius = if (hasActiveBehavior) FabCornerRadiusActive else FabCornerRadiusIdle
    val containerColor = if (hasActiveBehavior) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = if (hasActiveBehavior) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Surface(
        modifier = modifier
            .onGloballyPositioned { layoutCoordinates ->
                state.fabLayoutPosition = layoutCoordinates.positionInWindow()
                state.fabSize = layoutCoordinates.size
            }
            .offset {
                IntOffset(
                    state.dragOffset.x.roundToInt(),
                    state.dragOffset.y.roundToInt()
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        state.isDragging = true
                        state.dragStartOffset = startOffset
                        state.optionsLayoutBounds.clear()
                    },
                    onDragEnd = {
                        state.hoveredOption?.let { option ->
                            when (option) {
                                "完成" -> {
                                    if (hasActiveBehavior) {
                                        onCompleteActive()
                                    } else {
                                        onShowAddSheet(AddSheetMode.COMPLETED)
                                    }
                                }
                                "当前" -> onShowAddSheet(AddSheetMode.CURRENT)
                                "目标" -> onShowAddSheet(AddSheetMode.TARGET)
                                else -> onUnhandledOption(option)
                            }
                        }
                        resetDragState(state)
                    },
                    onDragCancel = { resetDragState(state) },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        state.dragOffset += dragAmount
                        val fabW = state.fabSize.width.toFloat()
                        val fabH = state.fabSize.height.toFloat()
                        state.dragOffset = Offset(
                            x = state.dragOffset.x.coerceIn(
                                -state.fabLayoutPosition.x,
                                screenWidthPx - state.fabLayoutPosition.x - fabW
                            ),
                            y = state.dragOffset.y.coerceIn(
                                -state.fabLayoutPosition.y,
                                screenHeightPx - state.fabLayoutPosition.y - fabH
                            )
                        )
                        val currentPointerPosition =
                            state.fabLayoutPosition + state.dragOffset + state.dragStartOffset
                        val hit = state.optionsLayoutBounds.entries
                            .firstOrNull { it.value.contains(currentPointerPosition) }
                            ?.key
                        if (hit != state.hoveredOption) {
                            state.hoveredOption = hit
                        }
                    }
                )
            },
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = FabShadowElevation,
        onClick = if (hasActiveBehavior) {
            { onCompleteActive(); Unit }
        } else {
            { onFabClick(); Unit }
        },
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = FabContentPaddingHorizontal,
                vertical = FabContentPaddingVertical
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = if (hasActiveBehavior) Icons.Default.Check else Icons.Default.Add,
                contentDescription = null,
            )
            if (hasActiveBehavior) {
                Spacer(modifier = Modifier.width(FabIconSpacerWidth))
                Text(
                    text = "完成行为",
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

private fun resetDragState(state: DragFabState) {
    state.isDragging = false
    state.dragOffset = Offset.Zero
    state.dragStartOffset = Offset.Zero
    state.hoveredOption = null
}
