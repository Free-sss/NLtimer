package com.nltimer.core.designsystem.theme

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.materialkolor.DynamicMaterialTheme

/**
 * 当前主题配置的 CompositionLocal
 * 子组件可通过 [LocalTheme.current] 读取当前生效的 [Theme] 配置
 */
val LocalTheme = staticCompositionLocalOf { Theme() }

/**
 * NLtimer 全局主题入口
 * 根据 [theme] 配置决定亮暗模式、调色板风格、字体，并通过 [DynamicMaterialTheme] 注入 Material3 主题，
 * 同时通过 [LocalTheme] 将完整配置下发给子组件
 *
 * @param theme 完整的主题配置对象
 * @param content 子组件内容
 */
@SuppressLint("UnusedContentLambdaTargetStateParameter")
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
        CompositionLocalProvider(LocalTheme provides theme) {
            // 主题切换时以淡入淡出动画过渡，避免颜色突变
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
}
