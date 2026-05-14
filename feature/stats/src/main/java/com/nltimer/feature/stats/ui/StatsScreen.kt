package com.nltimer.feature.stats.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nltimer.core.designsystem.component.PlaceholderScreen

@Composable
fun StatsRoute() {
    StatsScreen()
}

@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        icon = Icons.Default.BarChart,
        title = "统计",
        modifier = modifier,
    )
}
