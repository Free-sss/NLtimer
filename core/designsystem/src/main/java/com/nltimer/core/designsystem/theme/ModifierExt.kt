package com.nltimer.core.designsystem.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape

/**
 * A global border modifier that respects the [Theme.showBorders] setting.
 * When borders are disabled, it skips the [borderProducer] calculation entirely.
 */
@Composable
fun Modifier.appBorder(
    borderProducer: @Composable () -> BorderStroke,
    shape: Shape = RectangleShape
): Modifier {
    val showBorders = LocalTheme.current.showBorders
    return if (showBorders) {
        this.border(borderProducer(), shape)
    } else {
        this
    }
}
