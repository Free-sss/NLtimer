package com.nltimer.core.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nltimer.core.designsystem.theme.AppTheme
import com.nltimer.core.designsystem.theme.Fonts
import com.nltimer.core.designsystem.theme.HomeLayout
import com.nltimer.core.designsystem.theme.PaletteStyle
import com.nltimer.core.designsystem.theme.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsPrefsImpl(private val dataStore: DataStore<Preferences>) : SettingsPrefs {

    override fun getThemeFlow(): Flow<Theme> = dataStore.data.map { prefs ->
        val seed = prefs[seedColorKey] ?: Color(0xFF1565C0).toArgb()
        val appThemeName = prefs[appThemeKey] ?: AppTheme.SYSTEM.name
        val paletteStyleName = prefs[paletteStyleKey] ?: PaletteStyle.TONALSPOT.name
        val fontName = prefs[fontKey] ?: Fonts.FIGTREE.name
        val homeLayoutName = prefs[homeLayoutKey] ?: HomeLayout.GRID.name

        Theme(
            seedColor = Color(seed),
            appTheme = try { AppTheme.valueOf(appThemeName) } catch (_: Exception) { AppTheme.SYSTEM },
            isAmoled = prefs[isAmoledKey] == true,
            paletteStyle = try { PaletteStyle.valueOf(paletteStyleName) } catch (_: Exception) { PaletteStyle.TONALSPOT },
            isMaterialYou = prefs[isMaterialYouKey] == true,
            font = try { Fonts.valueOf(fontName) } catch (_: Exception) { Fonts.FIGTREE },
            showBorders = prefs[showBordersKey] != false,
            homeLayout = try { HomeLayout.valueOf(homeLayoutName) } catch (_: Exception) { HomeLayout.GRID },
        )
    }

    override suspend fun updateTheme(theme: Theme) {
        dataStore.edit { prefs ->
            prefs[seedColorKey] = theme.seedColor.toArgb()
            prefs[appThemeKey] = theme.appTheme.name
            prefs[isAmoledKey] = theme.isAmoled
            prefs[paletteStyleKey] = theme.paletteStyle.name
            prefs[isMaterialYouKey] = theme.isMaterialYou
            prefs[fontKey] = theme.font.name
            prefs[showBordersKey] = theme.showBorders
            prefs[homeLayoutKey] = theme.homeLayout.name
        }
    }

    override fun getSavedTagCategories(): Flow<Set<String>> = dataStore.data.map { prefs ->
        val raw = prefs[savedTagCategoriesKey] ?: ""
        if (raw.isBlank()) emptySet() else raw.split(",").toSet()
    }

    override suspend fun saveTagCategories(categories: Set<String>) {
        dataStore.edit { prefs ->
            prefs[savedTagCategoriesKey] = categories.joinToString(",")
        }
    }

    companion object {
        private val seedColorKey = intPreferencesKey("seed_color")
        private val appThemeKey = stringPreferencesKey("app_theme")
        private val isAmoledKey = booleanPreferencesKey("is_amoled")
        private val paletteStyleKey = stringPreferencesKey("palette_style")
        private val isMaterialYouKey = booleanPreferencesKey("is_material_you")
        private val fontKey = stringPreferencesKey("font")
        private val showBordersKey = booleanPreferencesKey("show_borders")
        private val homeLayoutKey = stringPreferencesKey("home_layout")
        private val savedTagCategoriesKey = stringPreferencesKey("saved_tag_categories")
    }
}
