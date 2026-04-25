package com.nltimer.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.nltimer.app.component.AppBottomNavigation
import com.nltimer.app.component.AppDrawer
import com.nltimer.app.component.AppTopAppBar
import com.nltimer.app.navigation.NLtimerNavHost
import kotlinx.coroutines.launch

@Composable
fun NLtimerScaffold(
    navController: NavHostController,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
) {
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                onClose = {
                    coroutineScope.launch { drawerState.close() }
                },
            )
        },
    ) {
        Scaffold(
            topBar = {
                AppTopAppBar(
                    onMenuClick = {
                        coroutineScope.launch { drawerState.open() }
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
    }
}
