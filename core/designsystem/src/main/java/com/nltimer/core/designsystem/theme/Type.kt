package com.nltimer.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

/** Material3 默认排版基准 */
private val DEFAULT_TYPOGRAPHY = Typography()

/**
 * 生成应用排版配置
 * 使用系统默认排版作为基准，统一替换字体系列
 *
 * @param font 自定义字体资源 ID，为 null 时使用系统默认字体
 */
fun provideTypography(font: Int? = null): Typography {
    val selectedFont = font?.let { FontFamily(Font(it)) } ?: FontFamily.Default

    return Typography(
        displayLarge = DEFAULT_TYPOGRAPHY.displayLarge.copy(fontFamily = selectedFont),
        displayMedium = DEFAULT_TYPOGRAPHY.displayMedium.copy(fontFamily = selectedFont),
        displaySmall = DEFAULT_TYPOGRAPHY.displaySmall.copy(fontFamily = selectedFont),
        headlineLarge = DEFAULT_TYPOGRAPHY.headlineLarge.copy(fontFamily = selectedFont),
        headlineMedium = DEFAULT_TYPOGRAPHY.headlineMedium.copy(fontFamily = selectedFont),
        headlineSmall = DEFAULT_TYPOGRAPHY.headlineSmall.copy(fontFamily = selectedFont),
        titleLarge = DEFAULT_TYPOGRAPHY.titleLarge.copy(fontFamily = selectedFont),
        titleMedium = DEFAULT_TYPOGRAPHY.titleMedium.copy(fontFamily = selectedFont),
        titleSmall = DEFAULT_TYPOGRAPHY.titleSmall.copy(fontFamily = selectedFont),
        bodyLarge = DEFAULT_TYPOGRAPHY.bodyLarge.copy(fontFamily = selectedFont),
        bodyMedium = DEFAULT_TYPOGRAPHY.bodyMedium.copy(fontFamily = selectedFont),
        bodySmall = DEFAULT_TYPOGRAPHY.bodySmall.copy(fontFamily = selectedFont),
        labelLarge = DEFAULT_TYPOGRAPHY.labelLarge.copy(fontFamily = selectedFont),
        labelMedium = DEFAULT_TYPOGRAPHY.labelMedium.copy(fontFamily = selectedFont),
        labelSmall = DEFAULT_TYPOGRAPHY.labelSmall.copy(fontFamily = selectedFont),
    )
}
