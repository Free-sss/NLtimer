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

object DebugInitializer {

    @JvmStatic
    fun init() {
        debugRoutes = {
            composable("debug") { DebugRoute() }
        }

        drawerMenuItems.add(
            DrawerMenuItem("debug", "🐛 调试", Icons.Default.Build)
        )

        HomeDebugComponents.registerAll()
        SubDebugComponents.registerAll()
        StatsDebugComponents.registerAll()
        SettingsDebugComponents.registerAll()
        CategoriesDebugComponents.registerAll()
        ManagementDebugComponents.registerAll()
        TagManagementDebugComponents.registerAll()
    }
}
