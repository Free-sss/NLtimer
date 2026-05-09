package com.nltimer.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.nltimer.app.component.AppBottomNavigation
import com.nltimer.app.component.AppDrawer
import com.nltimer.app.component.AppTopAppBar
import com.nltimer.app.component.RouteSettingsPopup
import com.nltimer.app.navigation.NLtimerNavHost
import com.nltimer.app.navigation.NLtimerRoutes
import com.nltimer.feature.settings.ui.ThemeSettingsViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NLtimerScaffold(
    navController: NavHostController,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
) {
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isSecondaryPage = currentRoute in NLtimerRoutes.SETTINGS_FULLSCREEN_ROUTES
    val topBarTitle = when (currentRoute) {
        NLtimerRoutes.SETTINGS -> "设置"
        NLtimerRoutes.THEME_SETTINGS -> "主题配置"
        NLtimerRoutes.DIALOG_CONFIG -> "弹窗配置"
        NLtimerRoutes.BEHAVIOR_MANAGEMENT -> "行为管理"
        else -> "NLtimer"
    }
    var showSettingsPopup by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                navController = navController,
                onClose = {
                    coroutineScope.launch { drawerState.close() }
                },
            )
        },
    ) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            // 底栏（在内容层之下）
            AppBottomNavigation(
                navController = navController,
                modifier = Modifier.align(Alignment.BottomCenter),
            )

            // 顶栏 + 页面内容（在底栏之上）
            Scaffold(
                modifier = Modifier.then(
                    if (!isSecondaryPage) Modifier.padding(bottom = 80.dp) else Modifier
                ),
                containerColor = Color.Transparent,
                topBar = {
                    if (isSecondaryPage) {
                        TopAppBar(
                            title = { Text(topBarTitle) },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "返回",
                                    )
                                }
                            },
                        )
                    } else {
                        AppTopAppBar(
                            title = topBarTitle,
                            onMenuClick = {
                                coroutineScope.launch { drawerState.open() }
                            },
                            onSettingClick = {
                                showSettingsPopup = true
                            },
                        )
                    }
                },
            ) { padding ->
                NLtimerNavHost(
                    navController = navController,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = padding.calculateTopPadding(),
                            bottom = if (!isSecondaryPage) padding.calculateBottomPadding() else 0.dp,
                        ),
                )
            }

            if (showSettingsPopup) {
                val themeViewModel: ThemeSettingsViewModel = hiltViewModel()
                RouteSettingsPopup(
                    currentRoute = currentRoute,
                    onDismiss = { showSettingsPopup = false },
                    onHomeLayoutChange = { themeViewModel.onHomeLayoutChange(it) },
                    onShowTimeSideBarChange = { themeViewModel.onShowTimeSideBarToggle(it) }
                )
            }
        }
    }
}
