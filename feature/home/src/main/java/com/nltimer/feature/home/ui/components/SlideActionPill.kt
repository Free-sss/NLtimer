package com.nltimer.feature.home.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.theme.ShapeTokens
import com.nltimer.core.designsystem.theme.styledAlpha
import com.nltimer.core.designsystem.theme.styledCorner
import kotlin.math.roundToInt

@Composable
fun SlideActionPill(
    onActivate: () -> Unit,
    modifier: Modifier = Modifier,
    onSlideProgress: (Float) -> Unit = {},
    activeLabel: String = "滑动",
    activatedLabel: String = "释放",
    leadingIcon: ImageVector = Icons.Filled.Check,
    activatedIcon: ImageVector = Icons.Filled.Check,
    pillWidth: Dp = 200.dp,
) {
    val thumbSize = 60.dp
    val padding = 6.dp
    val maxOffset = with(LocalDensity.current) { (pillWidth - thumbSize - padding * 2).toPx() }

    var offsetXState = remember { mutableStateOf(0f) }
    var isDraggingState = remember { mutableStateOf(false) }

    val animatedOffset = animateFloatAsState(
        targetValue = if (isDraggingState.value) offsetXState.value else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "thumb_offset"
    )

    val progress = (animatedOffset.value / maxOffset).coerceIn(0f, 1f)

    LaunchedEffect(progress) {
        onSlideProgress(progress)
    }

    Surface(
        modifier = modifier
            .width(pillWidth)
            .height(72.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { isDraggingState.value = true },
                    onDragEnd = {
                        if (offsetXState.value >= maxOffset * 0.7f) {
                            onActivate()
                        }
                        offsetXState.value = 0f
                        isDraggingState.value = false
                    },
                    onDragCancel = {
                        offsetXState.value = 0f
                        isDraggingState.value = false
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetXState.value = (offsetXState.value + dragAmount).coerceIn(0f, maxOffset)
                    }
                )
            },
        shape = RoundedCornerShape(styledCorner(ShapeTokens.CORNER_PILL)),
        color = MaterialTheme.colorScheme.primary.copy(alpha = styledAlpha(0.3f) + progress * 0.4f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.CenterStart
        ) {
            if (progress > 0.5f) {
                Row(
                    modifier = Modifier.width(pillWidth - padding * 2),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activatedLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                            alpha = (1f - progress).coerceIn(0.3f, 0.7f)
                        ),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.width(pillWidth - padding * 2),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activeLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                            alpha = (1f - progress).coerceIn(0.3f, 0.7f)
                        ),
                        modifier = Modifier.padding(end = 10.dp),
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .size(thumbSize)
                    .offset { IntOffset(animatedOffset.value.roundToInt(), 0) },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp + (4.dp * progress)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        if (progress > 0.5f) activatedIcon else leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
