package com.nltimer.feature.settings.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.core.data.SettingsPrefs
import com.nltimer.core.designsystem.theme.AppTheme
import com.nltimer.core.designsystem.theme.Fonts
import com.nltimer.core.designsystem.theme.PaletteStyle
import com.nltimer.core.designsystem.theme.Theme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(
    private val settingsPrefs: SettingsPrefs,
) : ViewModel() {

    val theme: StateFlow<Theme> = settingsPrefs.getThemeFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Theme())

    fun onSeedColorChange(color: Color) {
        viewModelScope.launch {
            settingsPrefs.updateTheme(theme.value.copy(seedColor = color))
        }
    }

    fun onThemeSwitch(appTheme: AppTheme) {
        viewModelScope.launch {
            settingsPrefs.updateTheme(theme.value.copy(appTheme = appTheme))
        }
    }

    fun onAmoledSwitch(isAmoled: Boolean) {
        viewModelScope.launch {
            settingsPrefs.updateTheme(theme.value.copy(isAmoled = isAmoled))
        }
    }

    fun onPaletteChange(style: PaletteStyle) {
        viewModelScope.launch {
            settingsPrefs.updateTheme(theme.value.copy(paletteStyle = style))
        }
    }

    fun onMaterialYouToggle(enabled: Boolean) {
        viewModelScope.launch {
            settingsPrefs.updateTheme(theme.value.copy(isMaterialYou = enabled))
        }
    }

    fun onFontChange(font: Fonts) {
        viewModelScope.launch {
            settingsPrefs.updateTheme(theme.value.copy(font = font))
        }
    }

    fun onShowBordersToggle(enabled: Boolean) {
        viewModelScope.launch {
            settingsPrefs.updateTheme(theme.value.copy(showBorders = enabled))
        }
    }
}
