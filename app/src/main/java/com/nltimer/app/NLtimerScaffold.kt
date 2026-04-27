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
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.nltimer.app.component.AppBottomNavigation
import com.nltimer.app.component.AppDrawer
import com.nltimer.app.component.AppTopAppBar
import com.nltimer.app.component.RouteSettingsPopup
import com.nltimer.app.navigation.NLtimerNavHost
import kotlinx.coroutines.launch

@Composable
fun NLtimerScaffold(
    navController: NavHostController,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
) {
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
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
                NLtimerNavHost(
                    navController = navController,
                    modifier = Modifier.padding(padding),
                )
            }

            if (showSettingsPopup) {
                RouteSettingsPopup(
                    currentRoute = currentRoute,
                    onDismiss = { showSettingsPopup = false }
                )
            }
        }
    }
}
