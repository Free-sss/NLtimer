package com.nltimer.feature.management_activities.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.nltimer.feature.management_activities.viewmodel.ActivityManagementViewModel

@Composable
fun ActivityManagementRoute(
    viewModel: ActivityManagementViewModel = hiltViewModel(),
) {
    ActivityManagementScreen(viewModel = viewModel)
}
