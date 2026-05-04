package com.nltimer.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.nltimer.app.component.AppBottomNavigation
import com.nltimer.app.component.AppDrawer
import com.nltimer.app.component.AppTopAppBar
import com.nltimer.app.component.RouteSettingsPopup
import com.nltimer.app.navigation.NLtimerNavHost
import com.nltimer.feature.settings.ui.ThemeSettingsViewModel
import kotlinx.coroutines.launch

/**
 * 应用主框架 Composable
 * 使用 ModalNavigationDrawer 包裹 Scaffold，整合顶栏、底栏、抽屉和路由导航
 *
 * @param navController 导航控制器，用于管理页面跳转
 * @param drawerState 抽屉状态，控制侧边栏的打开与关闭
 * @param themeViewModel 主题设置 ViewModel，用于响应主页布局变更
 */
@Composable
fun NLtimerScaffold(
    navController: NavHostController,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    themeViewModel: ThemeSettingsViewModel = hiltViewModel(),
) {
    // 获取协程作用域，用于在回调中启动协程操作抽屉
    val coroutineScope = rememberCoroutineScope()
    // 监听当前导航回退栈，获取当前路由名
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    // 控制路由设置弹窗的显示状态
    var showSettingsPopup by remember { mutableStateOf(false) }

    // 模态抽屉布局，包裹整个 Scaffold 内容
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
        Box {
            Scaffold(
                topBar = {
                    AppTopAppBar(
                        onMenuClick = {
                            coroutineScope.launch { drawerState.open() }
                        },
                        onSettingClick = {
                            showSettingsPopup = true
                        },
                    )
                },
                bottomBar = { AppBottomNavigation(navController) },
            ) { padding ->
                // 导航宿主容器，传入 Scaffold 的 padding 避免被顶栏底栏遮挡
                NLtimerNavHost(
                    navController = navController,
                    modifier = Modifier.padding(padding),
                )
            }

            // 设置弹窗以覆盖层形式展示在当前页面之上
            if (showSettingsPopup) {
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
