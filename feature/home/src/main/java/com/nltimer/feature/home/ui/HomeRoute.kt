package com.nltimer.feature.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.nltimer.feature.home.model.AddSheetMode
import com.nltimer.feature.home.viewmodel.HomeViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val activityGroups by viewModel.activityGroups.collectAsState()
    val tagsForSelectedActivity by viewModel.tagsForSelectedActivity.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    val dialogConfig by viewModel.dialogConfig.collectAsState()

    HomeScreen(
        uiState = uiState,
        activities = activities,
        activityGroups = activityGroups,
        tagsForSelectedActivity = tagsForSelectedActivity,
        allTags = allTags,
        dialogConfig = dialogConfig,
        onEmptyCellClick = { viewModel.showAddSheet(AddSheetMode.COMPLETED) },
        onShowAddSheet = { viewModel.showAddSheet(it) },
        onAddBehavior = { activityId, tagIds, startTime, nature, note ->
            val epochMillis = LocalDate.now()
                .atTime(startTime)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            viewModel.addBehavior(activityId, tagIds, epochMillis, nature, note)
        },
        onDismissSheet = viewModel::hideAddSheet,
        onCompleteBehavior = { viewModel.completeBehavior(it) },
        onToggleIdleMode = viewModel::toggleIdleMode,
        onStartNextPending = viewModel::startNextPending,
        onAddActivity = { name, emoji -> viewModel.addActivity(name, emoji) },
        onAddTag = { name -> viewModel.addTag(name) },
        onHourClick = viewModel::scrollToTime,
        onLayoutChange = viewModel::onHomeLayoutChange,
    )
}
