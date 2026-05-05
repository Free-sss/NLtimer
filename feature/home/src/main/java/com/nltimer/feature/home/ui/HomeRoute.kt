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
    val timeLabelConfig by viewModel.timeLabelConfig.collectAsState()

    HomeScreen(
        uiState = uiState,
        activities = activities,
        activityGroups = activityGroups,
        tagsForSelectedActivity = tagsForSelectedActivity,
        allTags = allTags,
        dialogConfig = dialogConfig,
        onEmptyCellClick = { idleStart, idleEnd -> viewModel.showAddSheet(AddSheetMode.COMPLETED, idleStart, idleEnd) },
        onShowAddSheet = { viewModel.showAddSheet(it) },
        onCellLongClick = { cell -> viewModel.showEditSheet(cell) },
        onAddBehavior = { activityId, tagIds, startTime, endTime, nature, note ->
            val startEpochMillis = LocalDate.now()
                .atTime(startTime)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            val endEpochMillis = endTime?.let {
                LocalDate.now()
                    .atTime(it)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }
            viewModel.addBehavior(activityId, tagIds, startEpochMillis, endEpochMillis, nature, note)
        },
        onDismissSheet = viewModel::hideAddSheet,
        onCompleteBehavior = { viewModel.completeBehavior(it) },
        onToggleIdleMode = viewModel::toggleIdleMode,
        onStartNextPending = viewModel::startNextPending,
        onAddActivity = { name, emoji -> viewModel.addActivity(name, emoji) },
        onAddTag = { name -> viewModel.addTag(name) },
        onHourClick = viewModel::scrollToTime,
        onLayoutChange = viewModel::onHomeLayoutChange,
        timeLabelConfig = timeLabelConfig,
        onTimeLabelConfigChange = viewModel::onTimeLabelConfigChange,
    )
}
