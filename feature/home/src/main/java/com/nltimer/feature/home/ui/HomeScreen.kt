package com.nltimer.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import android.widget.Toast
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.model.Behavior
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.model.DialogGridConfig
import com.nltimer.core.data.model.Tag
import com.nltimer.feature.home.model.HomeUiState
import androidx.compose.ui.tooling.preview.Preview
import com.nltimer.core.designsystem.theme.HomeLayout
import com.nltimer.core.designsystem.theme.LocalTheme
import com.nltimer.core.designsystem.theme.NLtimerTheme
import com.nltimer.core.designsystem.theme.TimeLabelConfig
import com.nltimer.feature.home.ui.components.BehaviorLogView
import com.nltimer.feature.home.model.GridCellUiState
import com.nltimer.feature.home.model.GridRowUiState
import com.nltimer.feature.home.model.AddSheetMode
import com.nltimer.feature.home.model.TagUiState
import com.nltimer.feature.home.ui.components.TimeAxisGrid
import com.nltimer.feature.home.ui.components.TimeLabelSettingsDialog
import com.nltimer.feature.home.ui.components.TimeSideBar
import com.nltimer.feature.home.ui.components.TimelineReverseView
import com.nltimer.feature.home.ui.sheet.AddBehaviorSheet
import com.nltimer.feature.home.ui.sheet.AddCurrentBehaviorSheet
import com.nltimer.feature.home.ui.sheet.AddTargetBehaviorSheet
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    activities: List<Activity>,
    activityGroups: List<ActivityGroup>,
    tagsForSelectedActivity: List<Tag>,
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
    onAddActivity: (name: String, iconKey: String) -> Unit,
    onAddTag: (name: String) -> Unit,
    onHourClick: (Int) -> Unit,
    onLayoutChange: (HomeLayout) -> Unit,
    timeLabelConfig: TimeLabelConfig = TimeLabelConfig(),
    onTimeLabelConfigChange: (TimeLabelConfig) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val layout = LocalTheme.current.homeLayout

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

    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var dragStartOffset by remember { mutableStateOf(Offset.Zero) }
    var hoveredOption by remember { mutableStateOf<String?>(null) }
    val optionsLayoutBounds = remember { mutableStateMapOf<String, Rect>() }
    var fabLayoutPosition by remember { mutableStateOf(Offset.Zero) }
    var fabSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    var optionsRowHeight by remember { mutableFloatStateOf(0f) }
    var boxPositionInWindow by remember { mutableStateOf(Offset.Zero) }

    val dragOptions = remember(uiState.hasActiveBehavior) {
        if (uiState.hasActiveBehavior) {
            listOf("完成", "放弃", "特记", "+自定义")
        } else {
            listOf("完成", "目标", "当前", "+自定义")
        }
    }

    val density = LocalDensity.current
    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }

    Box(
        modifier = modifier.onGloballyPositioned { boxPositionInWindow = it.positionInWindow() }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                val cornerRadius = if (uiState.hasActiveBehavior) 16.dp else 28.dp
                val containerColor = if (uiState.hasActiveBehavior) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
                val contentColor = if (uiState.hasActiveBehavior) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }

                Surface(
                    modifier = Modifier
                        .onGloballyPositioned { layoutCoordinates ->
                            fabLayoutPosition = layoutCoordinates.positionInWindow()
                            fabSize = layoutCoordinates.size
                        }
                        .offset {
                            IntOffset(
                                dragOffset.x.roundToInt(),
                                dragOffset.y.roundToInt()
                            )
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { startOffset ->
                                    isDragging = true
                                    dragStartOffset = startOffset
                                    optionsLayoutBounds.clear()
                                },
                                onDragEnd = {
                                    if (hoveredOption != null) {
                                        when (hoveredOption) {
                                            "完成" -> {
                                                if (uiState.hasActiveBehavior) {
                                                    activeBehaviorId?.let { onCompleteBehavior(it) }
                                                } else {
                                                    onShowAddSheet(AddSheetMode.COMPLETED)
                                                }
                                            }
                                            "当前" -> {
                                                onShowAddSheet(AddSheetMode.CURRENT)
                                            }
                                            "目标" -> {
                                                onShowAddSheet(AddSheetMode.TARGET)
                                            }
                                            else -> {
                                                Toast.makeText(
                                                    context,
                                                    "触发功能: $hoveredOption",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                    isDragging = false
                                    dragOffset = Offset.Zero
                                    dragStartOffset = Offset.Zero
                                    hoveredOption = null
                                },
                                onDragCancel = {
                                    isDragging = false
                                    dragOffset = Offset.Zero
                                    dragStartOffset = Offset.Zero
                                    hoveredOption = null
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffset += dragAmount
                                    val fabW = fabSize.width.toFloat()
                                    val fabH = fabSize.height.toFloat()
                                    dragOffset = Offset(
                                        x = dragOffset.x.coerceIn(-fabLayoutPosition.x, screenWidthPx - fabLayoutPosition.x - fabW),
                                        y = dragOffset.y.coerceIn(-fabLayoutPosition.y, screenHeightPx - fabLayoutPosition.y - fabH)
                                    )
                                    val currentPointerPosition =
                                        fabLayoutPosition + dragOffset + dragStartOffset
                                    val hit =
                                        optionsLayoutBounds.entries.find { entry ->
                                            entry.value.contains(currentPointerPosition)
                                        }?.key
                                    if (hit != hoveredOption) {
                                        hoveredOption = hit
                                    }
                                }
                            )
                        },
                    shape = RoundedCornerShape(cornerRadius),
                    color = containerColor,
                    contentColor = contentColor,
                    shadowElevation = 4.dp,
                    onClick = if (uiState.hasActiveBehavior) {
                        { activeBehaviorId?.let { onCompleteBehavior(it) }; Unit }
                    } else {
                        { onEmptyCellClick(null, null) }
                    },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = if (uiState.hasActiveBehavior) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = null,
                        )

                        if (uiState.hasActiveBehavior) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "完成行为",
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }
                }
            }
        ) { padding ->
        // 加载中显示转圈指示器，否则渲染主内容
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                ,
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 0.dp, bottom = 0.dp)
                ,
            ) {
                // 网格模式：左侧时间轴网格 + 右侧小时侧边栏
                when (layout) {
                    HomeLayout.GRID -> {
                        val showSideBar = LocalTheme.current.showTimeSideBar
                        Row(modifier = Modifier.weight(1f)) {
                            TimeAxisGrid(
                                rows = uiState.rows,
                                onEmptyCellClick = onEmptyCellClick,
                                onCellLongClick = onCellLongClick,
                                currentHour = uiState.selectedTimeHour,
                                onLayoutChange = onLayoutChange,
                                showTimeSideBar = showSideBar,
                                timeLabelConfig = timeLabelConfig,
                                onTimeLabelSettingsClick = { showTimeLabelSettings = true },
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
                    HomeLayout.TIMELINE_REVERSE -> {
                        val allCells = remember(uiState.rows) { uiState.rows.flatMap { it.cells } }
                        TimelineReverseView(
                            cells = allCells,
                            onAddClick = onEmptyCellClick,
                            onCellLongClick = onCellLongClick,
                            onLayoutChange = onLayoutChange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    HomeLayout.LOG -> {
                        val allCells = remember(uiState.rows) { uiState.rows.flatMap { it.cells } }
                        BehaviorLogView(
                            cells = allCells,
                            onCellLongClick = onCellLongClick,
                            onLayoutChange = onLayoutChange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        val existingBehaviors by remember(uiState.rows) {
            derivedStateOf {
                uiState.rows.flatMap { it.cells }
                    .filter { it.behaviorId != null && it.status != null }
                    .map { cell ->
                        Behavior(
                            id = cell.behaviorId!!,
                            activityId = 0,
                            startTime = cell.startTime
                                ?.atDate(LocalDate.now())
                                ?.atZone(ZoneId.systemDefault())
                                ?.toInstant()
                                ?.toEpochMilli() ?: 0,
                            endTime = cell.endTime
                                ?.atDate(LocalDate.now())
                                ?.atZone(ZoneId.systemDefault())
                                ?.toInstant()
                                ?.toEpochMilli(),
                            status = cell.status!!,
                            note = cell.note,
                            pomodoroCount = cell.pomodoroCount,
                            sequence = 0,
                            estimatedDuration = cell.estimatedDuration,
                            actualDuration = cell.actualDuration,
                            achievementLevel = cell.achievementLevel,
                            wasPlanned = cell.wasPlanned,
                        )
                    }
            }
        }

        when (uiState.addSheetMode) {
            AddSheetMode.COMPLETED -> {
                AddBehaviorSheet(
                    activities = activities,
                    activityGroups = activityGroups,
                    tagsForActivity = tagsForSelectedActivity,
                    allTags = allTags,
                    dialogConfig = dialogConfig,
                    initialStartTime = uiState.idleStartTime ?: uiState.lastBehaviorEndTime,
                    initialEndTime = uiState.idleEndTime ?: LocalTime.now(),
                    initialActivityId = uiState.editInitialActivityId,
                    initialTagIds = uiState.editInitialTagIds,
                    initialNote = uiState.editInitialNote,
                    editBehaviorId = uiState.editBehaviorId,
                    existingBehaviors = existingBehaviors,
                    onDismiss = onDismissSheet,
                    onConfirm = onAddBehavior,
                    onAddActivity = onAddActivity,
                    onAddTag = onAddTag,
                )
            }
            AddSheetMode.CURRENT -> {
                AddCurrentBehaviorSheet(
                    activities = activities,
                    activityGroups = activityGroups,
                    tagsForActivity = tagsForSelectedActivity,
                    allTags = allTags,
                    dialogConfig = dialogConfig,
                    initialStartTime = uiState.idleStartTime ?: LocalTime.now(),
                    initialActivityId = uiState.editInitialActivityId,
                    initialTagIds = uiState.editInitialTagIds,
                    initialNote = uiState.editInitialNote,
                    editBehaviorId = uiState.editBehaviorId,
                    existingBehaviors = existingBehaviors.filter { it.status != BehaviorNature.ACTIVE },
                    onDismiss = onDismissSheet,
                    onConfirm = onAddBehavior,
                    onAddActivity = onAddActivity,
                    onAddTag = onAddTag,
                )
            }
            AddSheetMode.TARGET -> {
                AddTargetBehaviorSheet(
                    activities = activities,
                    activityGroups = activityGroups,
                    tagsForActivity = tagsForSelectedActivity,
                    allTags = allTags,
                    dialogConfig = dialogConfig,
                    initialActivityId = uiState.editInitialActivityId,
                    initialTagIds = uiState.editInitialTagIds,
                    initialNote = uiState.editInitialNote,
                    editBehaviorId = uiState.editBehaviorId,
                    existingBehaviors = existingBehaviors,
                    onDismiss = onDismissSheet,
                    onConfirm = onAddBehavior,
                    onAddActivity = onAddActivity,
                    onAddTag = onAddTag,
                )
            }
            null -> {}
        }
    }

        if (isDragging) {
            val gapPx = with(density) { 8.dp.toPx() }
            val fabBottomPx = fabLayoutPosition.y + fabSize.height
            val optionsY = fabBottomPx - optionsRowHeight - gapPx - boxPositionInWindow.y
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset { IntOffset(0, optionsY.roundToInt()) }
                    .onGloballyPositioned { coords ->
                        optionsRowHeight = coords.size.height.toFloat()
                    },
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dragOptions.forEach { option ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { layoutCoordinates ->
                                val position = layoutCoordinates.positionInWindow()
                                val size = layoutCoordinates.size
                                optionsLayoutBounds[option] = Rect(
                                    position.x,
                                    position.y,
                                    position.x + size.width,
                                    position.y + size.height
                                )
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = if (hoveredOption == option)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 4.dp,
                        shadowElevation = 4.dp
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (hoveredOption == option)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        if (showTimeLabelSettings) {
            TimeLabelSettingsDialog(
                config = timeLabelConfig,
                onConfigChange = onTimeLabelConfigChange,
                onDismiss = { showTimeLabelSettings = false },
            )
        }
    }
}

// 预览用示例数据
@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    val sampleTags = listOf(
        Tag(1, "Tag 1", null, null, null, 0, 0, 0, false),
        Tag(2, "Tag 2", null, null, null, 0, 0, 0, false)
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
            tagsForSelectedActivity = sampleTags,
            allTags = sampleTags,
            onEmptyCellClick = { _, _ -> },
            onShowAddSheet = {},
            onCellLongClick = {},
            onAddBehavior = { _, _, _, _, _, _ -> },
            onDismissSheet = {},
            onCompleteBehavior = {},
            onToggleIdleMode = {},
            onStartNextPending = {},
            onAddActivity = { _, _ -> },
            onAddTag = {},
            onHourClick = {},
            onLayoutChange = {}
        )
    }
}
