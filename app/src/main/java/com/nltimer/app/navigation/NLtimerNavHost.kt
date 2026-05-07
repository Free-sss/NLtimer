package com.nltimer.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nltimer.feature.categories.ui.CategoriesRoute
import com.nltimer.feature.home.ui.HomeRoute
import com.nltimer.feature.management_activities.ui.ActivityManagementRoute
import com.nltimer.feature.settings.ui.DialogConfigRoute
import com.nltimer.feature.settings.ui.SettingsRoute
import com.nltimer.feature.settings.ui.ThemeSettingsRoute
import com.nltimer.feature.stats.ui.StatsRoute
import com.nltimer.feature.sub.ui.SubRoute
import com.nltimer.feature.tag_management.ui.TagManagementRoute

const val HOME_ROUTE = "home"
const val SUB_ROUTE = "sub"
const val STATS_ROUTE = "stats"
const val CATEGORIES_ROUTE = "categories"
const val MANAGEMENT_ACTIVITIES_ROUTE = "management_activities"
const val TAG_MANAGEMENT_ROUTE = "tag_management"
const val SETTINGS_ROUTE = "settings"
const val THEME_SETTINGS_ROUTE = "theme_settings"
const val DIALOG_CONFIG_ROUTE = "dialog_config"

/**
 * 导航宿主 Composable
 * 注册所有应用页面的路由，包括 feature 模块的页面路由和 debug 模块的动态路由
 *
 * @param navController 导航控制器
 * @param modifier Modifier 修饰符
 */
@Composable
fun NLtimerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE,
        modifier = modifier,
    ) {
        composable(HOME_ROUTE) { HomeRoute() }
        composable(SUB_ROUTE) { SubRoute() }
        composable(STATS_ROUTE) { StatsRoute() }
        composable(CATEGORIES_ROUTE) { CategoriesRoute() }
        composable(MANAGEMENT_ACTIVITIES_ROUTE) { ActivityManagementRoute() }
        composable(TAG_MANAGEMENT_ROUTE) {
            TagManagementRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(SETTINGS_ROUTE) {
            SettingsRoute(
                onNavigateToThemeSettings = { navController.navigate(THEME_SETTINGS_ROUTE) },
                onNavigateToDialogConfig = { navController.navigate(DIALOG_CONFIG_ROUTE) },
            )
        }
        composable(THEME_SETTINGS_ROUTE) {
            ThemeSettingsRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(DIALOG_CONFIG_ROUTE) {
            DialogConfigRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        debugRoutes?.invoke(this)
    }
}

// debug 模块通过此变量动态注入路由，release 构建保持 null
internal var debugRoutes: (NavGraphBuilder.() -> Unit)? = null
