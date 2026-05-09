package com.nltimer.app.component

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import java.util.concurrent.CopyOnWriteArrayList
import com.nltimer.app.navigation.NLtimerRoutes

private val MIN_DRAWER_WIDTH = 280.dp

/**
 * 抽屉侧边栏菜单项数据模型
 *
 * @param route 点击后导航的目标路由
 * @param label 菜单项显示文本
 * @param icon 菜单项图标
 */
internal data class DrawerMenuItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

// 抽屉侧边栏菜单项列表，debug 模块可在初始化时动态追加
internal val drawerMenuItems = CopyOnWriteArrayList(listOf(
    DrawerMenuItem(NLtimerRoutes.HOME, "主页", Icons.Default.Home),
    DrawerMenuItem(NLtimerRoutes.THEME_SETTINGS, "主题配置", Icons.Default.Brightness5),
    DrawerMenuItem(NLtimerRoutes.CATEGORIES, "分类管理", Icons.Default.Category),
    DrawerMenuItem(NLtimerRoutes.MANAGEMENT_ACTIVITIES, "活动管理", Icons.Default.List),
    DrawerMenuItem(NLtimerRoutes.TAG_MANAGEMENT, "标签管理", Icons.Default.Label),
    DrawerMenuItem(NLtimerRoutes.SETTINGS, "设置", Icons.Default.Settings),
))

/**
 * 抽屉侧边栏 Composable
 * 显示应用标题和应用菜单项列表，支持导航跳转和关闭抽屉
 *
 * @param navController 导航控制器
 * @param onClose 关闭抽屉的回调
 * @param modifier Modifier 修饰符
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun AppDrawer(
    navController: NavHostController,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 获取当前屏幕宽度，计算抽屉最大宽度为屏幕宽度一半，最低 280dp
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val maxDrawerWidth = (screenWidthDp * 0.5f).coerceAtLeast(MIN_DRAWER_WIDTH)

    // 获取当前路由，用于菜单项的高亮判断
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalDrawerSheet(
        modifier = modifier.widthIn(
            min = MIN_DRAWER_WIDTH,
            max = maxDrawerWidth,
        ),
    ) {
        // 应用标题区域
        Text(
            text = "NLtimer",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 遍历菜单项，生成可点击的导航项
        drawerMenuItems.forEach { item ->
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                    )
                },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    // 仅在非当前路由时执行导航，避免重复跳转
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    onClose()
                },
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
    }
}
