package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TimeSideBar(
    activeHours: Set<Int>,
    currentHour: Int,
    onHourClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayedHours = remember(activeHours, currentHour) {
        (activeHours + currentHour).sorted()
    }

    var showBubble by remember { mutableStateOf(false) }
    var bubbleHour by remember { mutableIntStateOf(0) }
    var bubbleY by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current

    Box(modifier = modifier.width(40.dp)) {
        if (showBubble) {
            Box(
                modifier = Modifier
                    .offset(x = (-52).dp, y = with(density) { bubbleY.toDp() } - 24.dp)
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(12.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${bubbleHour}:00",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    if (showBubble) {
                        val index = (bubbleY / size.height * displayedHours.size)
                            .toInt()
                            .coerceIn(0, displayedHours.lastIndex)
                        val hour = displayedHours[index]
                        if (hour != bubbleHour) {
                            bubbleHour = hour
                            onHourClick(hour)
                        }
                    }
                }
                .pointerInput(displayedHours) {
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            bubbleY = offset.y
                            val index = (offset.y / size.height * displayedHours.size)
                                .toInt()
                                .coerceIn(0, displayedHours.lastIndex)
                            bubbleHour = displayedHours[index]
                            showBubble = true
                            onHourClick(bubbleHour)
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            bubbleY = (bubbleY + dragAmount).coerceIn(0f, size.height.toFloat())
                            val index = (bubbleY / size.height * displayedHours.size)
                                .toInt()
                                .coerceIn(0, displayedHours.lastIndex)
                            val hour = displayedHours[index]
                            if (hour != bubbleHour) {
                                bubbleHour = hour
                                onHourClick(hour)
                            }
                        },
                        onDragEnd = { showBubble = false },
                        onDragCancel = { showBubble = false },
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            displayedHours.forEach { hour ->
                val isActive = hour in activeHours
                val isCurrent = hour == currentHour

                Text(
                    modifier = modifier.clickable{onHourClick(hour)},
                    text = hour.toString(),
                    color = when {
                        isCurrent -> MaterialTheme.colorScheme.tertiary
                        isActive -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                    fontSize = 11.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,


                )
            }
        }
    }
}
