package com.nltimer.feature.home.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.model.AddActivityCallback
import com.nltimer.core.data.model.AddTagCallback
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.model.DialogGridConfig
import com.nltimer.core.data.model.Tag
import com.nltimer.core.designsystem.component.BottomBarDragFab
import com.nltimer.core.designsystem.component.rememberDragFabState
import com.nltimer.core.designsystem.theme.BottomBarMode
import com.nltimer.core.designsystem.theme.HomeLayout
import com.nltimer.core.designsystem.theme.LocalTheme
import com.nltimer.core.designsystem.theme.NLtimerTheme
import com.nltimer.core.designsystem.theme.TimeLabelConfig
import com.nltimer.core.tools.match.NoteScanResult
import com.nltimer.feature.home.model.AddSheetMode
import com.nltimer.feature.home.model.GridCellUiState
import com.nltimer.feature.home.model.GridRowUiState
import com.nltimer.feature.home.model.HomeUiState
import com.nltimer.feature.home.model.TagUiState
import com.nltimer.feature.home.ui.components.BehaviorLogView
import com.nltimer.feature.home.ui.components.MomentView
import com.nltimer.feature.home.ui.components.TimeAxisGrid
import com.nltimer.feature.home.ui.components.TimeLabelSettingsDialog
import com.nltimer.feature.home.ui.components.TimeSideBar
import com.nltimer.feature.home.ui.components.TimelineReverseView
import java.time.LocalTime

private val DragOptionsWithActive = listOf("完成", "放弃", "特记", "+自定义")
private val DragOptionsWithoutActive = listOf("完成", "目标", "当前", "+自定义")

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    activities: List<Activity>,
    activityGroups: List<ActivityGroup>,
    allTags: List<Tag>,
    dialogConfig: DialogGridConfig = DialogGridConfig(),
    onEmptyCellClick: (idleStart: LocalTime?, idleEnd: LocalTime?) -> Unit,
    onShowAddSheet: (AddSheetMode) -> Unit,
    onCellLongClick: (GridCellUiState) -> Unit,
    onAddBehavior: (activityId: Long, tagIds: List<Long>, startTime: LocalTime, endTime: LocalTime?, nature: BehaviorNature, note: String?) -> Unit,
    onDismissSheet: () -> Unit,
    onCompleteBehavior: (Long) -> Unit,
    onToggleIdleMode: () -> Unit,
    onStartNextPending: () -> Unit,
    onStartBehavior: (Long) -> Unit,
    onAddActivity: AddActivityCallback,
    onAddTag: AddTagCallback,
    onHourClick: (Int) -> Unit,
    timeLabelConfig: TimeLabelConfig = TimeLabelConfig(),
    onTimeLabelConfigChange: (TimeLabelConfig) -> Unit = {},
    onMatchNote: (String) -> NoteScanResult = { NoteScanResult(null, emptySet()) },
    modifier: Modifier = Modifier,
) {
    val theme = LocalTheme.current
    val layout = theme.homeLayout
    val isFloatingBottomBar = theme.bottomBarMode == BottomBarMode.FLOATING
    var showTimeLabelSettings by remember { mutableStateOf(false) }

    val activeBehaviorId by remember(uiState.rows) {
        derivedStateOf {
            uiState.rows
                .flatMap { it.cells }
                .firstOrNull { it.isCurrent && it.behaviorId != null }
                ?.behaviorId
        }
    }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short,
            )
        }
    }

    val dragFabState = rememberDragFabState()
    val dragOptions = if (uiState.hasActiveBehavior) DragOptionsWithActive else DragOptionsWithoutActive

    Box(
        modifier = modifier.onGloballyPositioned { dragFabState.boxPositionInWindow = it.positionInWindow() }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    HomeLayoutContent(
                        layout = layout,
                        uiState = uiState,
                        activeBehaviorId = activeBehaviorId,
                        onEmptyCellClick = onEmptyCellClick,
                        onCellLongClick = onCellLongClick,
                        onHourClick = onHourClick,
                        onCompleteBehavior = onCompleteBehavior,
                        onStartNextPending = onStartNextPending,
                        onStartBehavior = onStartBehavior,
                        timeLabelConfig = timeLabelConfig,
                        onTimeLabelSettingsClick = { showTimeLabelSettings = true },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            HomeSheetRouter(
                uiState = uiState,
                activities = activities,
                activityGroups = activityGroups,
                allTags = allTags,
                dialogConfig = dialogConfig,
                onDismissSheet = onDismissSheet,
                onAddBehavior = onAddBehavior,
                onAddActivity = onAddActivity,
                onAddTag = onAddTag,
                onMatchNote = onMatchNote,
            )
        }

        BottomBarDragFab(
            state = dragFabState,
            icon = if (uiState.hasActiveBehavior) Icons.Default.Check else Icons.Default.Add,
            dragOptions = dragOptions,
            label = if (uiState.hasActiveBehavior) "完成行为" else null,
            containerColor = if (uiState.hasActiveBehavior) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (uiState.hasActiveBehavior) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
            cornerRadius = if (uiState.hasActiveBehavior) 16.dp else 28.dp,
            onClick = if (uiState.hasActiveBehavior) {
                { activeBehaviorId?.let { onCompleteBehavior(it) } }
            } else {
                { onEmptyCellClick(null, null) }
            },
            onOptionSelected = { option ->
                when (option) {
                    "完成" -> {
                        if (uiState.hasActiveBehavior) {
                            activeBehaviorId?.let { onCompleteBehavior(it) }
                        } else {
                            onShowAddSheet(AddSheetMode.COMPLETED)
                        }
                    }
                    "当前" -> onShowAddSheet(AddSheetMode.CURRENT)
                    "目标" -> onShowAddSheet(AddSheetMode.TARGET)
                    else -> Toast.makeText(context, "触发功能: $option", Toast.LENGTH_SHORT).show()
                }
            },
        )

        if (showTimeLabelSettings) {
            TimeLabelSettingsDialog(
                config = timeLabelConfig,
                onConfigChange = onTimeLabelConfigChange,
                onDismiss = { showTimeLabelSettings = false },
            )
        }
    }
}

@Composable
private fun HomeLayoutContent(
    layout: HomeLayout,
    uiState: HomeUiState,
    activeBehaviorId: Long?,
    onEmptyCellClick: (idleStart: LocalTime?, idleEnd: LocalTime?) -> Unit,
    onCellLongClick: (GridCellUiState) -> Unit,
    onHourClick: (Int) -> Unit,
    onCompleteBehavior: (Long) -> Unit,
    onStartNextPending: () -> Unit,
    onStartBehavior: (Long) -> Unit,
    timeLabelConfig: TimeLabelConfig,
    onTimeLabelSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (layout) {
        HomeLayout.GRID -> GridContent(
            uiState = uiState,
            onEmptyCellClick = onEmptyCellClick,
            onCellLongClick = onCellLongClick,
            onHourClick = onHourClick,
            timeLabelConfig = timeLabelConfig,
            onTimeLabelSettingsClick = onTimeLabelSettingsClick,
            modifier = modifier,
        )
        HomeLayout.TIMELINE_REVERSE -> TimelineReverseContent(
            uiState = uiState,
            onEmptyCellClick = onEmptyCellClick,
            onCellLongClick = onCellLongClick,
            modifier = modifier,
        )
        HomeLayout.LOG -> LogContent(
            uiState = uiState,
            onCellLongClick = onCellLongClick,
            modifier = modifier,
        )
        HomeLayout.MOMENT -> MomentContent(
            uiState = uiState,
            activeBehaviorId = activeBehaviorId,
            onEmptyCellClick = onEmptyCellClick,
            onCellLongClick = onCellLongClick,
            onCompleteBehavior = onCompleteBehavior,
            onStartNextPending = onStartNextPending,
            onStartBehavior = onStartBehavior,
            modifier = modifier,
        )
    }
}

@Composable
private fun GridContent(
    uiState: HomeUiState,
    onEmptyCellClick: (idleStart: LocalTime?, idleEnd: LocalTime?) -> Unit,
    onCellLongClick: (GridCellUiState) -> Unit,
    onHourClick: (Int) -> Unit,
    timeLabelConfig: TimeLabelConfig,
    onTimeLabelSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showSideBar = LocalTheme.current.showTimeSideBar
    Row(modifier = modifier) {
        TimeAxisGrid(
            rows = uiState.rows,
            onEmptyCellClick = onEmptyCellClick,
            onCellLongClick = onCellLongClick,
            currentHour = uiState.selectedTimeHour,
            showTimeSideBar = showSideBar,
            timeLabelConfig = timeLabelConfig,
            onTimeLabelSettingsClick = onTimeLabelSettingsClick,
            modifier = Modifier.weight(1f),
        )
        if (showSideBar) {
            val activeHours by remember {
                derivedStateOf {
                    uiState.rows
                        .filter { it.cells.any { cell -> cell.behaviorId != null } || it.isCurrentRow }
                        .map { it.startTime.hour }
                        .toSet()
                }
            }
            TimeSideBar(
                activeHours = activeHours,
                currentHour = uiState.selectedTimeHour,
                onHourClick = onHourClick,
            )
        }
    }
}

@Composable
private fun TimelineReverseContent(
    uiState: HomeUiState,
    onEmptyCellClick: (idleStart: LocalTime?, idleEnd: LocalTime?) -> Unit,
    onCellLongClick: (GridCellUiState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val allCells = remember(uiState.rows) { uiState.rows.flatMap { it.cells } }
    TimelineReverseView(
        cells = allCells,
        onAddClick = onEmptyCellClick,
        onCellLongClick = onCellLongClick,
        modifier = modifier
    )
}

@Composable
private fun LogContent(
    uiState: HomeUiState,
    onCellLongClick: (GridCellUiState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val allCells = remember(uiState.rows) { uiState.rows.flatMap { it.cells } }
    BehaviorLogView(
        cells = allCells,
        onCellLongClick = onCellLongClick,
        modifier = modifier
    )
}

@Composable
private fun MomentContent(
    uiState: HomeUiState,
    activeBehaviorId: Long?,
    onEmptyCellClick: (idleStart: LocalTime?, idleEnd: LocalTime?) -> Unit,
    onCellLongClick: (GridCellUiState) -> Unit,
    onCompleteBehavior: (Long) -> Unit,
    onStartNextPending: () -> Unit,
    onStartBehavior: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val allCells = remember(uiState.rows) { uiState.rows.flatMap { it.cells } }
    MomentView(
        cells = allCells,
        hasActiveBehavior = uiState.hasActiveBehavior,
        activeBehaviorId = activeBehaviorId,
        onCompleteBehavior = onCompleteBehavior,
        onStartNextPending = onStartNextPending,
        onStartBehavior = onStartBehavior,
        onEmptyCellClick = onEmptyCellClick,
        onCellLongClick = onCellLongClick,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    val sampleTags = listOf(
        Tag(1, "Tag 1", null, null, null, 0, 0, 0, null, false),
        Tag(2, "Tag 2", null, null, null, 0, 0, 0, null, false)
    )
    val sampleActivities = listOf(
        Activity(1, "Activity 1", "😊"),
        Activity(2, "Activity 2", "🚀")
    )
    val sampleUiState = HomeUiState(
        isLoading = false,
        rows = listOf(
            GridRowUiState(
                rowId = "1",
                startTime = LocalTime.of(9, 0),
                isCurrentRow = true,
                isLocked = false,
                cells = listOf(
                    GridCellUiState(
                        behaviorId = 1L,
                        activityIconKey = "😊",
                        activityName = "Activity 1",
                        tags = listOf(TagUiState(1, "Tag 1", null)),
                        status = BehaviorNature.ACTIVE,
                        isCurrent = true
                    )
                )
            )
        ),
        selectedTimeHour = 9
    )

    NLtimerTheme {
        HomeScreen(
            uiState = sampleUiState,
            activities = sampleActivities,
            activityGroups = emptyList(),
            allTags = sampleTags,
            onEmptyCellClick = { _, _ -> },
            onShowAddSheet = {},
            onCellLongClick = {},
            onAddBehavior = { _, _, _, _, _, _ -> },
            onDismissSheet = {},
            onCompleteBehavior = {},
            onToggleIdleMode = {},
            onStartNextPending = {},
            onStartBehavior = {},
            onAddActivity = { _, _, _, _, _, _ -> },
            onAddTag = { _, _, _, _, _, _, _ -> },
            onHourClick = {},
        )
    }
}