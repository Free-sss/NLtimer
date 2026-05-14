package com.nltimer.feature.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nltimer.feature.home.model.AddSheetMode
import com.nltimer.feature.home.viewmodel.HomeViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activities by viewModel.activities.collectAsStateWithLifecycle()
    val activityGroups by viewModel.activityGroups.collectAsStateWithLifecycle()
    val allTags by viewModel.allTags.collectAsStateWithLifecycle()
    val activityLastUsedMap by viewModel.activityLastUsedMap.collectAsStateWithLifecycle()
    val tagLastUsedMap by viewModel.tagLastUsedMap.collectAsStateWithLifecycle()
    val dialogConfig by viewModel.dialogConfig.collectAsStateWithLifecycle()
    val timeLabelConfig by viewModel.timeLabelConfig.collectAsStateWithLifecycle()
    val homeLayoutConfig by viewModel.homeLayoutConfig.collectAsStateWithLifecycle()

    val onEmptyCellClick = remember(viewModel) {
        { idleStart: LocalTime?, idleEnd: LocalTime? ->
            viewModel.showAddSheet(AddSheetMode.CURRENT, idleStart, idleEnd)
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
    val onStartBehavior = remember(viewModel) { { id: Long -> viewModel.startBehavior(id) } }
    val onAddActivity = remember(viewModel) {
        { name: String, iconKey: String?, color: Long?, groupId: Long?, keywords: String?, tagIds: List<Long> ->
            viewModel.addActivity(name, iconKey, color, groupId, keywords, tagIds)
        }
    }
    val onAddTag = remember(viewModel) {
        { name: String, color: Long?, icon: String?, priority: Int, category: String?, keywords: String?, activityId: Long? ->
            viewModel.addTag(name, color, icon, priority, category, keywords, activityId)
        }
    }
    val onHourClick = remember(viewModel) { { hour: Int -> viewModel.scrollToTime(hour) } }
    val onTimeLabelConfigChange = remember(viewModel) {
        { config: com.nltimer.core.designsystem.theme.TimeLabelConfig -> viewModel.onTimeLabelConfigChange(config) }
    }
    val onHomeLayoutConfigChange = remember(viewModel) {
        { config: com.nltimer.core.data.model.HomeLayoutConfig -> viewModel.onHomeLayoutConfigChange(config) }
    }
    val onMatchNote = remember(viewModel) {
        { note: String -> viewModel.matchNoteFromText(note) }
    }

    HomeScreen(
        uiState = uiState,
        activities = activities,
        activityGroups = activityGroups,
        allTags = allTags,
        dialogConfig = dialogConfig,
        homeLayoutConfig = homeLayoutConfig,
        activityLastUsedMap = activityLastUsedMap,
        tagLastUsedMap = tagLastUsedMap,
        onEmptyCellClick = onEmptyCellClick,
        onShowAddSheet = onShowAddSheet,
        onCellLongClick = onCellLongClick,
        onAddBehavior = onAddBehavior,
        onDismissSheet = onDismissSheet,
        onCompleteBehavior = onCompleteBehavior,
        onToggleIdleMode = onToggleIdleMode,
        onStartNextPending = onStartNextPending,
        onStartBehavior = onStartBehavior,
        onAddActivity = onAddActivity,
        onAddTag = onAddTag,
        onHourClick = onHourClick,
        onLoadMore = viewModel::loadMore,
        timeLabelConfig = timeLabelConfig,
        onTimeLabelConfigChange = onTimeLabelConfigChange,
        onHomeLayoutConfigChange = onHomeLayoutConfigChange,
        onMatchNote = onMatchNote,
    )
}
