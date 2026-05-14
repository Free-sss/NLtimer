package com.nltimer.feature.sub.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nltimer.core.designsystem.component.PlaceholderScreen

@Composable
fun SubRoute() {
    SubScreen()
}

@Composable
fun SubScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        icon = Icons.Default.Apps,
        title = "副页",
        modifier = modifier,
    )
}
