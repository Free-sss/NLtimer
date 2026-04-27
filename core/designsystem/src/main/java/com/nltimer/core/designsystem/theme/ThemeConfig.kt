package com.nltimer.core.designsystem.theme

import androidx.compose.ui.graphics.Color

data class Theme(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val isAmoled: Boolean = false,
    val paletteStyle: PaletteStyle = PaletteStyle.TONALSPOT,
    val isMaterialYou: Boolean = false,
    val seedColor: Color = Color(0xFF1565C0),
    val font: Fonts = Fonts.FIGTREE,
)
