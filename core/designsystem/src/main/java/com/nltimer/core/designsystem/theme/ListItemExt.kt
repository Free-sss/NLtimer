package com.nltimer.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

private const val CONNECTED_CORNER_RADIUS = 4
private const val END_CORNER_RADIUS = 16

@Composable
fun listItemColors(): ListItemColors =
    ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)

fun leadingItemShape(): Shape =
    RoundedCornerShape(
        topStart = END_CORNER_RADIUS.dp,
        topEnd = END_CORNER_RADIUS.dp,
        bottomEnd = CONNECTED_CORNER_RADIUS.dp,
        bottomStart = CONNECTED_CORNER_RADIUS.dp,
    )

fun middleItemShape(): Shape =
    RoundedCornerShape(
        topStart = CONNECTED_CORNER_RADIUS.dp,
        topEnd = CONNECTED_CORNER_RADIUS.dp,
        bottomStart = CONNECTED_CORNER_RADIUS.dp,
        bottomEnd = CONNECTED_CORNER_RADIUS.dp,
    )

fun endItemShape(): Shape =
    RoundedCornerShape(
        topStart = CONNECTED_CORNER_RADIUS.dp,
        topEnd = CONNECTED_CORNER_RADIUS.dp,
        bottomEnd = END_CORNER_RADIUS.dp,
        bottomStart = END_CORNER_RADIUS.dp,
    )

fun detachedItemShape(): Shape = RoundedCornerShape(END_CORNER_RADIUS.dp)
