package com.nltimer.app.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.nltimer.app.navigation.NLtimerRoutes

/**
 * 底部导航栏菜单项数据模型
 *
 * @param route 导航路由名
 * @param label 显示的文本标签
 * @param icon 菜单项图标
 */
internal data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

// 底部导航栏的菜单项列表，按显示顺序排列
internal val navItems = listOf(
    NavItem(NLtimerRoutes.HOME, "主页", Icons.Default.Home),
    NavItem(NLtimerRoutes.SUB, "副页", Icons.Default.Apps),
    NavItem(NLtimerRoutes.STATS, "统计", Icons.Default.BarChart),
    NavItem(NLtimerRoutes.SETTINGS, "设置", Icons.Default.Settings),
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppFloatingBottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val currentBackStackEntry by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(
        initialValue = navController.currentBackStackEntry,
    )
    val currentDestination = currentBackStackEntry?.destination

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        HorizontalFloatingToolbar(
            expanded = true,
            colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
                toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
        ) {
            navItems.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                FloatingToolbarTab(
                    selected = selected,
                    icon = item.icon,
                    label = item.label,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun FloatingToolbarTab(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val backgroundColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        color = backgroundColor,
        shape = CircleShape,
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .size(52.dp, 40.dp)
            .clip(CircleShape),
        onClick = onClick,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
