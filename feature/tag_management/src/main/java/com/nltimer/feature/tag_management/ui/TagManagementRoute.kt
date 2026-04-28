package com.nltimer.feature.tag_management.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.nltimer.feature.tag_management.viewmodel.TagManagementViewModel

@Composable
fun TagManagementRoute(
    onNavigateBack: () -> Unit,
    viewModel: TagManagementViewModel = hiltViewModel(),
) {
    TagManagementScreen(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
    )
}
