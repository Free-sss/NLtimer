package com.nltimer.app.navigation

import androidx.compose.ui.graphics.vector.ImageVector

object NLtimerRoutes {
    const val HOME = "home"
    const val SUB = "sub"
    const val STATS = "stats"
    const val CATEGORIES = "categories"
    const val MANAGEMENT_ACTIVITIES = "management_activities"
    const val TAG_MANAGEMENT = "tag_management"
    const val SETTINGS = "settings"
    const val THEME_SETTINGS = "theme_settings"
    const val DIALOG_CONFIG = "dialog_config"

    val PRIMARY_ROUTES = setOf(HOME, SUB, STATS, CATEGORIES, MANAGEMENT_ACTIVITIES, SETTINGS)
    val SETTINGS_FULLSCREEN_ROUTES = setOf(THEME_SETTINGS, DIALOG_CONFIG)
}

data class RouteConfig(
    val route: String,
    val label: String,
    val icon: ImageVector? = null,
    val showBottomNav: Boolean = true,
    val isFullscreen: Boolean = false,
    val topBarTitle: String? = null,
)
