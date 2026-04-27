package com.nltimer.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nltimer.feature.home.ui.HomeRoute
import com.nltimer.feature.settings.ui.SettingsRoute
import com.nltimer.feature.settings.ui.ThemeSettingsRoute
import com.nltimer.feature.stats.ui.StatsRoute
import com.nltimer.feature.sub.ui.SubRoute

@Composable
fun NLtimerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier,
    ) {
        composable("home") { HomeRoute() }
        composable("sub") { SubRoute() }
        composable("stats") { StatsRoute() }
        composable("settings") { SettingsRoute() }
        composable("theme_settings") {
            ThemeSettingsRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
