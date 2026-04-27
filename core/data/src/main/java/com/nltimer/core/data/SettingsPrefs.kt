package com.nltimer.core.data

import com.nltimer.core.designsystem.theme.Theme
import kotlinx.coroutines.flow.Flow

interface SettingsPrefs {
    fun getThemeFlow(): Flow<Theme>
    suspend fun updateTheme(theme: Theme)

    fun getSavedTagCategories(): Flow<Set<String>>
    suspend fun saveTagCategories(categories: Set<String>)
}
