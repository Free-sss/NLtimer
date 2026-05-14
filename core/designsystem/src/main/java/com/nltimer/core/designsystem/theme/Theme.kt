package com.nltimer.core.designsystem.theme

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.materialkolor.DynamicMaterialTheme

/**
 * 当前主题配置的 CompositionLocal
 * 子组件可通过 [LocalTheme.current] 读取当前生效的 [Theme] 配置
 */
val LocalTheme = staticCompositionLocalOf { Theme() }

val LocalImmersiveTopPadding = staticCompositionLocalOf { 0.dp }

/**
 * NLtimer 全局主题入口
 * 根据 [theme] 配置决定亮暗模式、调色板风格、字体，并通过 [DynamicMaterialTheme] 注入 Material3 主题，
 * 同时通过 [LocalTheme] 将完整配置下发给子组件
 *
 * @param theme 完整的主题配置对象
 * @param content 子组件内容
 */
@Composable
fun NLtimerTheme(
    theme: Theme = Theme(),
    content: @Composable () -> Unit,
) {
    // 根据用户选择的主题模式映射为亮/暗布尔值
    val isDark = when (theme.appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    val typography = provideTypography(font = theme.font.toFontRes())

    // 通过 DynamicMaterialTheme 生成动态调色板并注入 Material3 主题
    DynamicMaterialTheme(
        seedColor = theme.seedColor,
        isDark = isDark,
        isAmoled = theme.isAmoled,
        style = theme.paletteStyle.toMPaletteStyle(),
        typography = typography,
    ) {
        // 将完整主题配置注入 CompostionLocal，供子组件按需读取
        CompositionLocalProvider(
            LocalTheme provides theme,
            LocalTimerTypography provides resolveTimerTextStyle(),
        ) {
            @Suppress("UnusedCrossfadeTargetStateParameter")
            Crossfade(targetState = theme.appTheme to theme.isAmoled, label = "theme") {
                content()
            }
        }
    }
}
