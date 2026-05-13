package com.nltimer.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun Long?.toComposeColor(default: @Composable () -> Color): Color =
    this?.let { Color(it) } ?: default()
