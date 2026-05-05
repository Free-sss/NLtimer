package com.nltimer.app.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/**
 * 底部导航栏菜单项数据模型
 *
 * @param route 导航路由名
 * @param label 显示的文本标签
 * @param icon 菜单项图标
 */
private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

// 底部导航栏的菜单项列表，按显示顺序排列
private val navItems = listOf(
    NavItem("home", "主页", Icons.Default.Home),
    NavItem("sub", "副页", Icons.Default.Apps),
    NavItem("stats", "统计", Icons.Default.BarChart),
    NavItem("settings", "设置", Icons.Default.Settings),
)

/**
 * 底部导航栏 Composable
 * 根据当前目标路由高亮选中项，点击后导航到对应页面并管理回退栈
 *
 * @param navController 导航控制器
 * @param modifier Modifier 修饰符
 */
@Composable
fun AppBottomNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    // 通过 currentBackStackEntryFlow 收集当前导航回退栈入口
    val currentBackStackEntry by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(
        initialValue = navController.currentBackStackEntry,
    )
    val currentDestination = currentBackStackEntry?.destination

    NavigationBar(modifier = modifier) {
        // 遍历底部菜单项，生成对应的 NavigationBarItem
        navItems.forEach { item ->
            // 判断当前项是否在目标层级中，以决定高亮状态
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                    )
                },
                label = { Text(item.label) },
                selected = selected,
                onClick = {
                    // 导航到目标路由，清除回退栈至起始目的地并保存/恢复状态
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
    }
}
