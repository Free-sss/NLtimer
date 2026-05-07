package com.nltimer.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SettingsSubpageScaffold(
    content: @Composable (PaddingValues) -> Unit,
) {
    content(
        PaddingValues(
            top = 8.dp,
            bottom = 24.dp,
            start = 16.dp,
            end = 16.dp,
        )
    )
}

@Composable
fun SettingsSubpageContainer(
    containerColor: Color = MaterialTheme.colorScheme.background,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    SettingsSubpageScaffold { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(containerColor),
            contentPadding = padding,
        ) {
            content()
        }
    }
}
