package com.nltimer.core.designsystem.theme

import com.materialkolor.PaletteStyle as MPaletteStyle
import com.nltimer.core.designsystem.R

/**
 * 将本地字体枚举映射为资源 ID，系统默认字体返回 null
 */
fun Fonts.toFontRes(): Int? = when (this) {
    Fonts.FIGTREE -> R.font.figtree
    Fonts.SYSTEM_DEFAULT -> null
}

/**
 * 返回字体的可读显示名称
 */
fun Fonts.toDisplayString(): String = when (this) {
    Fonts.FIGTREE -> "Figtree"
    Fonts.SYSTEM_DEFAULT -> "System Default"
}

/**
 * 将本地 PaletteStyle 枚举映射为 materialkolor 库的 PaletteStyle
 */
fun PaletteStyle.toMPaletteStyle(): MPaletteStyle = when (this) {
    PaletteStyle.TONALSPOT -> MPaletteStyle.TonalSpot
    PaletteStyle.NEUTRAL -> MPaletteStyle.Neutral
    PaletteStyle.VIBRANT -> MPaletteStyle.Vibrant
    PaletteStyle.EXPRESSIVE -> MPaletteStyle.Expressive
    PaletteStyle.RAINBOW -> MPaletteStyle.Rainbow
    PaletteStyle.FRUITSALAD -> MPaletteStyle.FruitSalad
    PaletteStyle.MONOCHROME -> MPaletteStyle.Monochrome
    PaletteStyle.FIDELITY -> MPaletteStyle.Fidelity
    PaletteStyle.CONTENT -> MPaletteStyle.Content
}

/**
 * 返回主题模式的可读显示文本
 */
fun AppTheme.toDisplayString(): String = when (this) {
    AppTheme.LIGHT -> "Light"
    AppTheme.DARK -> "Dark"
    AppTheme.SYSTEM -> "Follow System"
}

/**
 * 返回首页布局的可读显示文本
 */
fun HomeLayout.toDisplayString(): String = when (this) {
    HomeLayout.GRID -> "网格时间"
    HomeLayout.TIMELINE_REVERSE -> "时间轴(反)"
}
