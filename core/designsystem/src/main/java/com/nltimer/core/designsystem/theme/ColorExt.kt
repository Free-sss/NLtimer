package com.nltimer.core.designsystem.theme

import androidx.compose.ui.graphics.Color

fun Long?.toComposeColor(default: () -> Color): Color =
    this?.let { Color(it) } ?: default()
