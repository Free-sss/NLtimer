package com.nltimer.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NLtimerApp() {
    val navController = rememberNavController()
    NLtimerScaffold(navController = navController)
}
