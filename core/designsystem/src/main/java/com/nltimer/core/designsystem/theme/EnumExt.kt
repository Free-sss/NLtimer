package com.nltimer.core.designsystem.theme

import com.materialkolor.PaletteStyle as MPaletteStyle
import com.nltimer.core.designsystem.R

fun Fonts.toFontRes(): Int? = when (this) {
    Fonts.FIGTREE -> R.font.figtree
    Fonts.SYSTEM_DEFAULT -> null
}

fun Fonts.toDisplayString(): String = when (this) {
    Fonts.FIGTREE -> "Figtree"
    Fonts.SYSTEM_DEFAULT -> "System Default"
}

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

fun AppTheme.toDisplayString(): String = when (this) {
    AppTheme.LIGHT -> "Light"
    AppTheme.DARK -> "Dark"
    AppTheme.SYSTEM -> "Follow System"
}
