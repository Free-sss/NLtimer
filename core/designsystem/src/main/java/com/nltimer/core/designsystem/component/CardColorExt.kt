package com.nltimer.core.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.nltimer.core.designsystem.theme.CardColorStrategy
import com.nltimer.core.designsystem.theme.styledAlpha

@Composable
fun cardColorForStrategy(strategy: CardColorStrategy, containerIndex: Int = 0): Color {
    return when (strategy) {
        CardColorStrategy.SURFACE -> MaterialTheme.colorScheme.surfaceContainer
        CardColorStrategy.TINTED_PRIMARY -> MaterialTheme.colorScheme.primaryContainer.copy(
            alpha = styledAlpha(0.3f),
        )
        CardColorStrategy.MULTI_CONTAINER -> when (containerIndex % 3) {
            0 -> MaterialTheme.colorScheme.primaryContainer
            1 -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.tertiaryContainer
        }
    }
}
