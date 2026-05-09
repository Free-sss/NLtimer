package com.nltimer.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.nltimer.core.designsystem.R

data class TimerTextStyle(
    val timeStyle: TextStyle,
    val useSerif: Boolean,
)

val LocalTimerTypography = compositionLocalOf { TimerTextStyle(Typography().headlineLarge, false) }

@Composable
fun resolveTimerTextStyle(): TimerTextStyle {
    val style = LocalTheme.current.style
    val typography = MaterialTheme.typography
    return when (style.timerTypography) {
        TimerTypography.HEADLINE -> TimerTextStyle(typography.headlineLarge, false)
        TimerTypography.DISPLAY_SMALL -> TimerTextStyle(typography.displaySmall, false)
        TimerTypography.DISPLAY_LARGE_SERIF -> TimerTextStyle(
            typography.displayLarge.copy(fontFamily = FontFamily(Font(R.font.dm_serif_text))),
            true,
        )
    }
}
