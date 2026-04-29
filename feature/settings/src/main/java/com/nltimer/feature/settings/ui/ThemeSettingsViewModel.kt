package com.nltimer.feature.settings.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.core.data.SettingsPrefs
import com.nltimer.core.designsystem.theme.AppTheme
import com.nltimer.core.designsystem.theme.Fonts
import com.nltimer.core.designsystem.theme.HomeLayout
import com.nltimer.core.designsystem.theme.PaletteStyle
import com.nltimer.core.designsystem.theme.Theme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 主题设置页的 ViewModel
 * 管理主题配置的读取与持久化，暴露可观察的 [theme] 状态流供 UI 层订阅
 */
@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(
    private val settingsPrefs: SettingsPrefs,
) : ViewModel() {

    /** 当前主题配置的状态流，通过 DataStore 持久化并实时响应变更 */
    val theme: StateFlow<Theme> = settingsPrefs.getThemeFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Theme())

    /** 更新种子颜色 */
    fun onSeedColorChange(color: Color) {
        viewModelScope.launch {
            settingsPrefs.updateTheme(theme.value.copy(seedColor = color))
        }
    }

    /** 切换亮暗模式 */
    fun onThemeSwitch(appTheme: AppTheme) {
        viewModelScope.launch {
            settingsPrefs.updateTheme(theme.value.copy(appTheme = appTheme))
        }
    }

    /** 切换 AMOLED 纯黑模式 */
    fun onAmoledSwitch(isAmoled: Boolean) {
        viewModelScope.launch {
            settingsPrefs.updateTheme(theme.value.copy(isAmoled = isAmoled))
        }
    }

    /** 更改调色板风格 */
    fun onPaletteChange(style: PaletteStyle) {
        viewModelScope.launch {
            settingsPrefs.updateTheme(theme.value.copy(paletteStyle = style))
        }
    }

    /** 切换 Material You 动态取色 */
    fun onMaterialYouToggle(enabled: Boolean) {
        viewModelScope.launch {
            settingsPrefs.updateTheme(theme.value.copy(isMaterialYou = enabled))
        }
    }

    /** 切换全局字体 */
    fun onFontChange(font: Fonts) {
        viewModelScope.launch {
            settingsPrefs.updateTheme(theme.value.copy(font = font))
        }
    }

    /** 切换组件边框显示 */
    fun onShowBordersToggle(enabled: Boolean) {
        viewModelScope.launch {
            settingsPrefs.updateTheme(theme.value.copy(showBorders = enabled))
        }
    }

    /** 切换主页布局（网格/时间轴） */
    fun onHomeLayoutChange(layout: HomeLayout) {
        viewModelScope.launch {
            settingsPrefs.updateTheme(theme.value.copy(homeLayout = layout))
        }
    }
}
