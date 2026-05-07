package com.nltimer.feature.settings.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun SettingsSubpageScaffold(
    content: @Composable (PaddingValues) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    content(
        PaddingValues(
            top = 8.dp,
            bottom = 24.dp,
            start = 16.dp,
            end = 16.dp,
        )
    )
}
