package com.nltimer.core.designsystem.theme

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import com.materialkolor.DynamicMaterialTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NLtimerTheme(
    theme: Theme = Theme(),
    content: @Composable () -> Unit,
) {
    val isDark = when (theme.appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    val typography = provideTypography(font = theme.font.toFontRes())

    DynamicMaterialTheme(
        seedColor = theme.seedColor,
        isDark = isDark,
        isAmoled = theme.isAmoled,
        style = theme.paletteStyle.toMPaletteStyle(),
        typography = typography,
    ) {
        AnimatedContent(
            targetState = theme.appTheme to theme.isAmoled,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
            },
            label = "theme-transition",
        ) {
            content()
        }
    }
}
