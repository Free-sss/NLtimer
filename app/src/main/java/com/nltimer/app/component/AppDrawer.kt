package com.nltimer.app.component

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.nltimer.app.navigation.NLtimerRoutes
import java.util.concurrent.CopyOnWriteArrayList

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
    val icon: ImageVector,
)

// 抽屉常规菜单项（线性排列）。debug 模块可在初始化时动态追加调试入口
internal val drawerMenuItems = CopyOnWriteArrayList(listOf(
    DrawerMenuItem(NLtimerRoutes.THEME_SETTINGS, "主题配置", Icons.Default.Brightness5),
    DrawerMenuItem(NLtimerRoutes.SETTINGS, "设置", Icons.Default.Settings),
))

// 管理类入口（FlowRow 紧凑展示，单行均分；标签去掉"管理"二字以适配紧凑布局）
private val drawerManagementItems = listOf(
    DrawerMenuItem(NLtimerRoutes.CATEGORIES, "分类", Icons.Default.Category),
    DrawerMenuItem(NLtimerRoutes.MANAGEMENT_ACTIVITIES, "活动", Icons.AutoMirrored.Filled.List),
    DrawerMenuItem(NLtimerRoutes.TAG_MANAGEMENT, "标签", Icons.AutoMirrored.Filled.Label),
    DrawerMenuItem(NLtimerRoutes.BEHAVIOR_MANAGEMENT, "行为", Icons.AutoMirrored.Filled.EventNote),
)

/**
 * 抽屉侧边栏 Composable
 * 显示应用标题和应用菜单项列表，支持导航跳转和关闭抽屉
 *
 * @param navController 导航控制器
 * @param onClose 关闭抽屉的回调
 * @param modifier Modifier 修饰符
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppDrawer(
    navController: NavHostController,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val maxDrawerWidth = (screenWidthDp * 0.5f).coerceAtLeast(MIN_DRAWER_WIDTH)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navigateTo: (String) -> Unit = { route ->
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
        onClose()
    }

    ModalDrawerSheet(
        modifier = modifier.widthIn(
            min = MIN_DRAWER_WIDTH,
            max = maxDrawerWidth,
        ),
    ) {
        Text(
            text = "NLtimer",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
        )

        

        // 2. 管理分组小标题
        Text(
            text = "管理",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        )

        // 3. 管理入口 FlowRow：四项均分，单行内显示
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = drawerManagementItems.size,
        ) {
            drawerManagementItems.forEach { item ->
                ManagementChip(
                    item = item,
                    selected = currentRoute == item.route,
                    onClick = { navigateTo(item.route) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

                // 1. 主题配置 — 常规线性条目
        val themeItem = drawerMenuItems.firstOrNull { it.route == NLtimerRoutes.THEME_SETTINGS }
        themeItem?.let { item ->
            NavigationDrawerItem(
                icon = { Icon(imageVector = item.icon, contentDescription = null) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = { navigateTo(item.route) },
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. 其余常规条目（设置 + debug 动态追加项），保持可扩展顺序
        drawerMenuItems
            .filter { it.route != NLtimerRoutes.THEME_SETTINGS }
            .forEach { item ->
                NavigationDrawerItem(
                    icon = { Icon(imageVector = item.icon, contentDescription = null) },
                    label = { Text(item.label) },
                    selected = currentRoute == item.route,
                    onClick = { navigateTo(item.route) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
    }
}

@Composable
private fun ManagementChip(
    item: DrawerMenuItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        contentColor = contentColor,
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
