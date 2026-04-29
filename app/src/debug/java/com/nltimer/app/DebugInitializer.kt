package com.nltimer.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.navigation.compose.composable
import com.nltimer.app.component.DrawerMenuItem
import com.nltimer.app.component.drawerMenuItems
import com.nltimer.app.navigation.debugRoutes
import com.nltimer.feature.categories.debug.CategoriesDebugComponents
import com.nltimer.feature.debug.DebugRoute
import com.nltimer.feature.home.debug.HomeDebugComponents
import com.nltimer.feature.management_activities.debug.ManagementDebugComponents
import com.nltimer.feature.settings.debug.SettingsDebugComponents
import com.nltimer.feature.stats.debug.StatsDebugComponents
import com.nltimer.feature.sub.debug.SubDebugComponents
import com.nltimer.feature.tag_management.debug.TagManagementDebugComponents

/**
 * app 模块的调试初始化入口
 * 在 debug 构建中注册调试路由和侧边栏菜单项，并初始化各模块的调试组件
 */
object DebugInitializer {

    @JvmStatic
    fun init() {
        // 注册调试页面路由，可通过导航跳转到调试主页
        debugRoutes = {
            composable("debug") { DebugRoute() }
        }

        // 在侧边栏菜单中追加调试入口项
        drawerMenuItems.add(
            DrawerMenuItem("debug", "调试", Icons.Default.Build)
        )

        // 依次注册各 feature 模块的调试组件（预览数据、假路由等）
        HomeDebugComponents.registerAll()
        SubDebugComponents.registerAll()
        StatsDebugComponents.registerAll()
        SettingsDebugComponents.registerAll()
        CategoriesDebugComponents.registerAll()
        ManagementDebugComponents.registerAll()
        TagManagementDebugComponents.registerAll()
    }
}
