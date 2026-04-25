package com.nltimer.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.nltimer.app.navigation.NLtimerNavHost

@Composable
fun NLtimerApp() {
    val navController = rememberNavController()
    Scaffold { padding ->
        NLtimerNavHost(
            navController = navController,
            modifier = Modifier.padding(padding),
        )
    }
}
