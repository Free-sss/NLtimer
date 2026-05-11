package com.nltimer.app.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
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

internal data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

internal val navItems = listOf(
    NavItem(NLtimerRoutes.HOME, "主页", Icons.Default.Home),
    NavItem(NLtimerRoutes.STATS, "统计", Icons.Default.BarChart),
    NavItem(NLtimerRoutes.SETTINGS, "设置", Icons.Default.Settings),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppBottomNavigation(
    navController: NavHostController,
    onSettingsClick: () -> Unit,
    onSettingsLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentBackStackEntry by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(
        initialValue = navController.currentBackStackEntry,
    )
    val currentDestination = currentBackStackEntry?.destination

    NavigationBar(modifier = modifier) {
        navItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            if (item.route == NLtimerRoutes.SETTINGS) {
                NavigationBarItem(
                    icon = {
                        Box(
                            modifier = Modifier.combinedClickable(
                                onLongClick = onSettingsClick,
                                onClick = onSettingsLongClick,
                            ),
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                            )
                        }
                    },
                    label = { Text(item.label) },
                    selected = selected,
                    onClick = onSettingsClick,
                )
            } else {
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
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun AppFloatingBottomBar(
    navController: NavHostController,
    onSettingsClick: () -> Unit,
    onSettingsLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentBackStackEntry by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(
        initialValue = navController.currentBackStackEntry,
    )
    val currentDestination = currentBackStackEntry?.destination

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        HorizontalFloatingToolbar(
            expanded = true,
            colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
                toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
        ) {
            navItems.filter { it.route != NLtimerRoutes.SETTINGS }.forEach { item ->
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

        Surface(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(bottom = 4.dp)
                .size(48.dp)
                .clip(CircleShape)
                .combinedClickable(
                    onLongClick = onSettingsClick,
                    onClick = onSettingsLongClick,
                ),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = CircleShape,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "菜单",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
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
