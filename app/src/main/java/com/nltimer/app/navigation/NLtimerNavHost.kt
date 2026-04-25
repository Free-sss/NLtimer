package com.nltimer.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nltimer.feature.timer.ui.TimerRoute

@Composable
fun NLtimerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = "timer",
        modifier = modifier,
    ) {
        composable("timer") {
            TimerRoute()
        }
    }
}
