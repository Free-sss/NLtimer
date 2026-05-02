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
        startDestination = "home",
        modifier = modifier,
    ) {
        // 注册主页、副页、统计页等常规模块路由
        composable("home") { HomeRoute() }
        composable("sub") { SubRoute() }
        composable("stats") { StatsRoute() }
        // 注册分类管理、活动管理、标签管理等后台管理路由
        composable("categories") { CategoriesRoute() }
        composable("management_activities") { ActivityManagementRoute() }
        composable("tag_management") {
            TagManagementRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        // 注册设置页与主题设置页路由，支持返回导航
        composable("settings") {
            SettingsRoute(
                onNavigateToThemeSettings = { navController.navigate("theme_settings") },
                onNavigateToDialogConfig = { navController.navigate("dialog_config") },
            )
        }
        composable("theme_settings") {
            ThemeSettingsRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable("dialog_config") {
            DialogConfigRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        // 注入 debug 模块的动态路由（release 构建中为 null，不执行）
        debugRoutes?.invoke(this)
    }
}

// debug 模块通过此变量动态注入路由，release 构建保持 null
internal var debugRoutes: (NavGraphBuilder.() -> Unit)? = null
