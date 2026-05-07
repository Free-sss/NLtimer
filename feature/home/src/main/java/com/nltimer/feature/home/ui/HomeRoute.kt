package com.nltimer.feature.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

    val onEmptyCellClick = remember(viewModel) {
        { idleStart: LocalTime?, idleEnd: LocalTime? ->
            viewModel.showAddSheet(AddSheetMode.COMPLETED, idleStart, idleEnd)
        }
    }
    val onShowAddSheet = remember(viewModel) {
        { mode: AddSheetMode -> viewModel.showAddSheet(mode) }
    }
    val onCellLongClick = remember(viewModel) {
        { cell: com.nltimer.feature.home.model.GridCellUiState -> viewModel.showEditSheet(cell) }
    }
    val onAddBehavior = remember(viewModel) {
        { activityId: Long, tagIds: List<Long>, startTime: LocalTime, endTime: LocalTime?, nature: com.nltimer.core.data.model.BehaviorNature, note: String? ->
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
        }
    }
    val onDismissSheet = remember(viewModel) { { viewModel.hideAddSheet() } }
    val onCompleteBehavior = remember(viewModel) {
        { id: Long -> viewModel.completeBehavior(id) }
    }
    val onToggleIdleMode = remember(viewModel) { { viewModel.toggleIdleMode() } }
    val onStartNextPending = remember(viewModel) { { viewModel.startNextPending() } }
    val onAddActivity = remember(viewModel) {
        { name: String, iconKey: String -> viewModel.addActivity(name, iconKey) }
    }
    val onAddTag = remember(viewModel) {
        { name: String -> viewModel.addTag(name) }
    }
    val onHourClick = remember(viewModel) { { hour: Int -> viewModel.scrollToTime(hour) } }
    val onLayoutChange = remember(viewModel) { { layout: com.nltimer.core.designsystem.theme.HomeLayout -> viewModel.onHomeLayoutChange(layout) } }
    val onTimeLabelConfigChange = remember(viewModel) {
        { config: com.nltimer.core.designsystem.theme.TimeLabelConfig -> viewModel.onTimeLabelConfigChange(config) }
    }

    HomeScreen(
        uiState = uiState,
        activities = activities,
        activityGroups = activityGroups,
        tagsForSelectedActivity = tagsForSelectedActivity,
        allTags = allTags,
        dialogConfig = dialogConfig,
        onEmptyCellClick = onEmptyCellClick,
        onShowAddSheet = onShowAddSheet,
        onCellLongClick = onCellLongClick,
        onAddBehavior = onAddBehavior,
        onDismissSheet = onDismissSheet,
        onCompleteBehavior = onCompleteBehavior,
        onToggleIdleMode = onToggleIdleMode,
        onStartNextPending = onStartNextPending,
        onAddActivity = onAddActivity,
        onAddTag = onAddTag,
        onHourClick = onHourClick,
        onLayoutChange = onLayoutChange,
        timeLabelConfig = timeLabelConfig,
        onTimeLabelConfigChange = onTimeLabelConfigChange,
    )
}
