package com.nltimer.feature.home.ui.sheet

import android.graphics.PathMeasure as AndroidPathMeasure
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility as ComposeAnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.model.Behavior
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.model.DialogGridConfig
import com.nltimer.core.data.model.Tag
import com.nltimer.core.data.util.hasTimeConflict
import com.nltimer.core.designsystem.theme.ChipDisplayMode
import com.nltimer.core.designsystem.theme.GridLayoutMode
import com.nltimer.core.designsystem.theme.NLtimerTheme
import com.nltimer.core.designsystem.theme.PathDrawMode
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBehaviorSheet(
    modifier: Modifier = Modifier,
    activities: List<Activity>,
    activityGroups: List<ActivityGroup>,
    tagsForActivity: List<Tag>,
    allTags: List<Tag> = emptyList(),
    dialogConfig: DialogGridConfig = DialogGridConfig(),
    initialStartTime: LocalTime? = null,
    initialEndTime: LocalTime? = null,
    existingBehaviors: List<Behavior> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (activityId: Long, tagIds: List<Long>, startTime: LocalTime, endTime: LocalTime?, nature: BehaviorNature, note: String?) -> Unit,
    onAddActivity: (name: String, emoji: String) -> Unit = { _, _ -> },
    onAddTag: (name: String) -> Unit = {},
) {
    BehaviorSheetWrapper(
        modifier = modifier,
        mode = BehaviorNature.COMPLETED,
        activities = activities,
        activityGroups = activityGroups,
        allTags = allTags,
        dialogConfig = dialogConfig,
        initialStartTime = initialStartTime,
        initialEndTime = initialEndTime,
        existingBehaviors = existingBehaviors,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        onAddActivity = onAddActivity,
        onAddTag = onAddTag,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCurrentBehaviorSheet(
    modifier: Modifier = Modifier,
    activities: List<Activity>,
    activityGroups: List<ActivityGroup>,
    tagsForActivity: List<Tag>,
    allTags: List<Tag> = emptyList(),
    dialogConfig: DialogGridConfig = DialogGridConfig(),
    initialStartTime: LocalTime? = null,
    existingBehaviors: List<Behavior> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (activityId: Long, tagIds: List<Long>, startTime: LocalTime, endTime: LocalTime?, nature: BehaviorNature, note: String?) -> Unit,
    onAddActivity: (name: String, emoji: String) -> Unit = { _, _ -> },
    onAddTag: (name: String) -> Unit = {},
) {
    BehaviorSheetWrapper(
        modifier = modifier,
        mode = BehaviorNature.ACTIVE,
        activities = activities,
        activityGroups = activityGroups,
        allTags = allTags,
        dialogConfig = dialogConfig,
        initialStartTime = initialStartTime,
        existingBehaviors = existingBehaviors,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        onAddActivity = onAddActivity,
        onAddTag = onAddTag,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTargetBehaviorSheet(
    modifier: Modifier = Modifier,
    activities: List<Activity>,
    activityGroups: List<ActivityGroup>,
    tagsForActivity: List<Tag>,
    allTags: List<Tag> = emptyList(),
    dialogConfig: DialogGridConfig = DialogGridConfig(),
    existingBehaviors: List<Behavior> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (activityId: Long, tagIds: List<Long>, startTime: LocalTime, endTime: LocalTime?, nature: BehaviorNature, note: String?) -> Unit,
    onAddActivity: (name: String, emoji: String) -> Unit = { _, _ -> },
    onAddTag: (name: String) -> Unit = {},
) {
    BehaviorSheetWrapper(
        modifier = modifier,
        mode = BehaviorNature.PENDING,
        activities = activities,
        activityGroups = activityGroups,
        allTags = allTags,
        dialogConfig = dialogConfig,
        existingBehaviors = existingBehaviors,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        onAddActivity = onAddActivity,
        onAddTag = onAddTag,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BehaviorSheetWrapper(
    modifier: Modifier,
    mode: BehaviorNature,
    activities: List<Activity>,
    activityGroups: List<ActivityGroup>,
    allTags: List<Tag>,
    dialogConfig: DialogGridConfig,
    initialStartTime: LocalTime? = null,
    initialEndTime: LocalTime? = null,
    existingBehaviors: List<Behavior> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (activityId: Long, tagIds: List<Long>, startTime: LocalTime, endTime: LocalTime?, nature: BehaviorNature, note: String?) -> Unit,
    onAddActivity: (name: String, emoji: String) -> Unit,
    onAddTag: (name: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f),
    ) {
        AddBehaviorSheetContent(
            modifier = modifier.imePadding(),
            mode = mode,
            activities = activities,
            activityGroups = activityGroups,
            allTags = allTags,
            dialogConfig = dialogConfig,
            initialStartTime = initialStartTime,
            initialEndTime = initialEndTime,
            existingBehaviors = existingBehaviors,
            onConfirm = { activityId, tagIds, startTime, endTime, nature, note ->
                onConfirm(activityId, tagIds, startTime, endTime, nature, note)
                onDismiss()
            },
            onDismiss = onDismiss,
            onAddActivity = onAddActivity,
            onAddTag = onAddTag,
        )
    }
}

@Composable
internal fun AddBehaviorSheetContent(
    modifier: Modifier = Modifier,
    mode: BehaviorNature = BehaviorNature.COMPLETED,
    activities: List<Activity>,
    activityGroups: List<ActivityGroup>,
    allTags: List<Tag>,
    dialogConfig: DialogGridConfig,
    initialStartTime: LocalTime? = null,
    initialEndTime: LocalTime? = null,
    existingBehaviors: List<Behavior> = emptyList(),
    onConfirm: (activityId: Long, tagIds: List<Long>, startTime: LocalTime, endTime: LocalTime?, nature: BehaviorNature, note: String?) -> Unit,
    onDismiss: () -> Unit,
    onAddActivity: (name: String, emoji: String) -> Unit = { _, _ -> },
    onAddTag: (name: String) -> Unit = {},
) {
    var selectedActivityId by remember { mutableStateOf<Long?>(null) }
    var selectedTagIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    val now = LocalDateTime.now().withSecond(0).withNano(0)
    var startTime by remember {
        mutableStateOf(
            initialStartTime?.let { now.withHour(it.hour).withMinute(it.minute) } ?: now
        )
    }
    var endTime by remember {
        mutableStateOf(
            initialEndTime?.let { now.withHour(it.hour).withMinute(it.minute) } ?: now
        )
    }
    val duration: Duration by remember {
        derivedStateOf {
            val d = Duration.between(startTime, endTime)
            if (d.isNegative) Duration.ZERO else d
        }
    }
    val nature = mode
    var note by remember { mutableStateOf("") }

    val today = remember { LocalDateTime.now().toLocalDate() }
    val hasTimeConflict by remember(startTime, endTime, mode, existingBehaviors) {
        derivedStateOf {
            if (mode == BehaviorNature.PENDING) return@derivedStateOf false
            val nowEpoch = System.currentTimeMillis()
            val startEpoch = today.atTime(startTime.toLocalTime())
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            val endEpoch = if (mode == BehaviorNature.COMPLETED) {
                today.atTime(endTime.toLocalTime())
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            } else null
            hasTimeConflict(
                newStart = startEpoch,
                newEnd = endEpoch,
                newStatus = mode,
                existingBehaviors = existingBehaviors,
                currentTime = nowEpoch,
            )
        }
    }

    var showAddActivityDialog by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var showActivityPicker by remember { mutableStateOf(false) }
    var showTagPicker by remember { mutableStateOf(false) }
    var showTimeAdjustments by remember { mutableStateOf(false) }

    val activityChips = remember(activities) { activities.map { ChipItem(it) } }
    val tagChips = remember(allTags) { allTags.map { ChipItem(it) } }

    val emphasisColor = MaterialTheme.colorScheme.secondary
    var pathLength by remember { mutableFloatStateOf(0f) }
    var targetProgress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1500),
        label = "path_draw_progress"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "jumping_transition")
    val jumpProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavy_line_progress"
    )

    val effectiveMode = remember(dialogConfig.pathDrawMode) {
        if (dialogConfig.pathDrawMode == PathDrawMode.Random) {
            val candidates = PathDrawMode.entries.filter {
                it != PathDrawMode.Random && it != PathDrawMode.None && it != PathDrawMode.WrigglingMaggot
            }
            candidates.random()
        } else {
            dialogConfig.pathDrawMode
        }
    }

    val context = LocalContext.current
    var isDragging by remember { mutableStateOf(false) }

    val blurRadius by animateDpAsState(
        targetValue = if (showTimeAdjustments) 8.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "blur_animation"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (showTimeAdjustments) 0.7f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha_animation"
    )
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var hoveredOption by remember { mutableStateOf<String?>(null) }
    val optionsLayoutBounds = remember { mutableStateMapOf<String, Rect>() }
    var boxPositionInWindow by remember { mutableStateOf(Offset.Zero) }
    var innerBoxPositionInWindow by remember { mutableStateOf(Offset.Zero) }
    var buttonRowPositionInWindow by remember { mutableStateOf(Offset.Zero) }
    var optionsRowHeight by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(effectiveMode) {
        targetProgress = 0f
        targetProgress = 1f
    }

    val horizontalLinesForActivities = remember(dialogConfig.activityHorizontalLines) {
        if (dialogConfig.activityHorizontalLines == 0) Int.MAX_VALUE else dialogConfig.activityHorizontalLines
    }
    val horizontalLinesForTags = remember(dialogConfig.tagHorizontalLines) {
        if (dialogConfig.tagHorizontalLines == 0) Int.MAX_VALUE else dialogConfig.tagHorizontalLines
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { boxPositionInWindow = it.positionInWindow() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.dp)
                    .graphicsLayer { clip = false }
                    .drawWithContent {
                        drawContent()
                        val strokeWidthPx = 3.dp.toPx()
                        val halfStroke = strokeWidthPx / 2
                        val r = 28.dp.toPx()
                        val w = size.width
                        val h = size.height
                        val extendedH = h * 1.2f
                        val path = Path().apply {
                            moveTo(halfStroke, extendedH)
                            lineTo(halfStroke, r)
                            arcTo(
                                rect = Rect(
                                    halfStroke,
                                    halfStroke,
                                    r * 2 - halfStroke,
                                    r * 2 - halfStroke
                                ),
                                startAngleDegrees = 180f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(w - r, halfStroke)
                            arcTo(
                                rect = Rect(
                                    w - r * 2 + halfStroke,
                                    halfStroke,
                                    w - halfStroke,
                                    r * 2 - halfStroke
                                ),
                                startAngleDegrees = 270f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(w - halfStroke, extendedH)
                        }

                        val pathMeasure = PathMeasure()
                        pathMeasure.setPath(path, false)
                        val totalLength = pathMeasure.length
                        if (totalLength > 0 && pathLength == 0f) {
                            pathLength = totalLength
                        }

                        val animatedPath = Path()
                        if (effectiveMode != PathDrawMode.None && animatedProgress > 0f) {
                            when (effectiveMode) {
                                PathDrawMode.StartToEnd -> {
                                    val stopDistance = totalLength * animatedProgress
                                    pathMeasure.getSegment(0f, stopDistance, animatedPath)
                                    drawPath(
                                        path = animatedPath,
                                        color = emphasisColor,
                                        style = Stroke(width = strokeWidthPx)
                                    )
                                }

                                PathDrawMode.BothSidesToMiddle -> {
                                    val halfLength = totalLength / 2f
                                    val drawLength = halfLength * animatedProgress
                                    val startSegment = Path()
                                    pathMeasure.getSegment(0f, drawLength, startSegment)
                                    drawPath(
                                        path = startSegment,
                                        color = emphasisColor,
                                        style = Stroke(width = strokeWidthPx)
                                    )
                                    val endSegment = Path()
                                    pathMeasure.getSegment(
                                        totalLength - drawLength,
                                        totalLength,
                                        endSegment
                                    )
                                    drawPath(
                                        path = endSegment,
                                        color = emphasisColor,
                                        style = Stroke(width = strokeWidthPx)
                                    )
                                }

                                PathDrawMode.WrigglingMaggot -> {
                                    val segmentCount = 24
                                    val segmentLengthPx = 12f
                                    val waveCount = 2
                                    val amplitudeRatio = 0.03f

                                    val androidPathMeasure = AndroidPathMeasure().apply {
                                        setPath(path.asAndroidPath(), false)
                                    }
                                    val position = FloatArray(2)
                                    val tangent = FloatArray(2)

                                    for (i in 0 until segmentCount) {
                                        val baseRatio = i.toFloat() / segmentCount
                                        val offset = sin(
                                            (baseRatio * waveCount + jumpProgress) * 2 * PI.toFloat()
                                        ) * amplitudeRatio
                                        val sampleRatio = (baseRatio + offset).coerceIn(0f, 1f)
                                        val distance = sampleRatio * totalLength

                                        androidPathMeasure.getPosTan(distance, position, tangent)

                                        val angle = atan2(
                                            tangent[1].toDouble(),
                                            tangent[0].toDouble()
                                        ).toFloat()
                                        val cosA = cos(angle)
                                        val sinA = sin(angle)

                                        val halfLen = segmentLengthPx / 2f
                                        val startX = position[0] - halfLen * cosA
                                        val startY = position[1] - halfLen * sinA
                                        val endX = position[0] + halfLen * cosA
                                        val endY = position[1] + halfLen * sinA

                                        drawLine(
                                            color = emphasisColor,
                                            start = Offset(startX, startY),
                                            end = Offset(endX, endY),
                                            strokeWidth = 3.dp.toPx(),
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }

                                PathDrawMode.Random, PathDrawMode.None -> {}
                            }
                        }
                    }
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp)
                            .then(
                                if (showTimeAdjustments) {
                                    Modifier.pointerInput(showTimeAdjustments) {
                                        awaitPointerEventScope {
                                            while (true) {
                                                val event = awaitPointerEvent(PointerEventPass.Main)
                                                if (event.type == PointerEventType.Press) {
                                                    showTimeAdjustments = false
                                                }
                                            }
                                        }
                                    }
                                } else Modifier
                            )
                    ) {
                        if (mode == BehaviorNature.COMPLETED) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val durationText: String by remember {
                                    derivedStateOf {
                                        val totalMinutes = duration.toMinutes()
                                        val h = totalMinutes / 60
                                        val m = totalMinutes % 60
                                        "${h}时${m}分"
                                    }
                                }
                                Text(
                                    text = "用时：$durationText",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = emphasisColor,
                                    ),
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { innerBoxPositionInWindow = it.positionInWindow() }
                        ) {
                            Column {
                                when (mode) {
                                    BehaviorNature.COMPLETED -> {
                                        DualTimePicker(
                                            startTime = startTime,
                                            endTime = endTime,
                                            animate = !showTimeAdjustments,
                                            onTimesChanged = { start, end ->
                                                if (startTime != start) startTime = start
                                                if (endTime != end) endTime = end
                                            },
                                            onLeftCenterClick = {
                                                showTimeAdjustments = !showTimeAdjustments
                                            },
                                            onRightCenterClick = {
                                                showTimeAdjustments = !showTimeAdjustments
                                            },
                                        )
                                    }
                                    BehaviorNature.ACTIVE -> {
                                        SingleTimePicker(
                                            startTime = startTime,
                                            animate = !showTimeAdjustments,
                                            onTimeChanged = { startTime = it },
                                            onCenterClick = {
                                                showTimeAdjustments = !showTimeAdjustments
                                            },
                                        )
                                    }
                                    BehaviorNature.PENDING -> {}
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        // Blurred Content
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .blur(blurRadius)
                                .graphicsLayer {
                                    alpha = contentAlpha
                                }
                        ) {
                            Spacer(modifier = Modifier.height(1.dp))
                            ActivityGridComponent(

                                chips = activityChips,
                                onChipClick = { id ->
                                    selectedActivityId = id
                                },
                                selectedId = selectedActivityId,
                                displayMode = dialogConfig.activityDisplayMode,
                                layoutMode = dialogConfig.activityLayoutMode,
                                maxLinesPerColumn = dialogConfig.activityColumnLines,
                                maxLinesHorizontal = horizontalLinesForActivities,
                                useActivityColorForText = dialogConfig.activityUseColorForText,
                                functionChipLabel = "活动",
                                functionChipIcon = {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = "活动",
                                        modifier = Modifier.size(14.dp),
                                    )
                                },
                                functionChipOnClick = { showActivityPicker = true },
                                functionChipOnLongClick = { showAddActivityDialog = true },
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            ActivityGridComponent(
                                chips = tagChips,
                                onChipClick = { tagId ->
                                    selectedTagIds = if (tagId in selectedTagIds) {
                                        selectedTagIds - tagId
                                    } else {
                                        selectedTagIds + tagId
                                    }
                                },
                                selectedIds = selectedTagIds,
                                multiSelect = true,
                                displayMode = dialogConfig.tagDisplayMode,
                                layoutMode = dialogConfig.tagLayoutMode,
                                maxLinesPerColumn = dialogConfig.tagColumnLines,
                                maxLinesHorizontal = horizontalLinesForTags,
                                useActivityColorForText = dialogConfig.tagUseColorForText,
                                functionChipLabel = "标签",
                                functionChipIcon = {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Label,
                                        contentDescription = "标签",
                                        modifier = Modifier.size(14.dp),
                                    )
                                },
                                functionChipOnClick = { showTagPicker = true },
                                functionChipOnLongClick = { showAddTagDialog = true },
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            NoteInputComponent(
                                note = note,
                                onNoteChange = { note = it },
                                onTopButton = { },
                                onBottomButton = { },
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onGloballyPositioned {
                                        buttonRowPositionInWindow = it.positionInWindow()
                                    },
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                var buttonPositionInWindow by remember { mutableStateOf(Offset.Zero) }
                                TextButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .weight(2f)
                                        .height(40.dp)
                                        .onGloballyPositioned { layoutCoordinates ->
                                            buttonPositionInWindow =
                                                layoutCoordinates.positionInWindow()
                                        }
                                        .offset {
                                            IntOffset(
                                                dragOffset.x.roundToInt(),
                                                dragOffset.y.roundToInt()
                                            )
                                        }
                                        .pointerInput(Unit) {
                                            detectDragGestures(
                                                onDragStart = {
                                                    isDragging = true
                                                },
                                                onDragEnd = {
                                                    if (hoveredOption != null) {
                                                        Toast.makeText(
                                                            context,
                                                            "触发功能: $hoveredOption",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    isDragging = false
                                                    dragOffset = Offset.Zero
                                                    hoveredOption = null
                                                },
                                                onDragCancel = {
                                                    isDragging = false
                                                    dragOffset = Offset.Zero
                                                    hoveredOption = null
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragOffset += dragAmount

                                                    val currentPointerPosition =
                                                        buttonPositionInWindow + dragOffset + change.position
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
                                ) {
                                    Text(
                                        "Gesture",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 14.sp
                                    )
                                }
                                Button(
                                    onClick = {
                                        if (mode == BehaviorNature.COMPLETED
                                            && !startTime.toLocalTime().isBefore(endTime.toLocalTime())
                                        ) {
                                            Toast.makeText(
                                                context,
                                                "开始时间必须早于结束时间",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@Button
                                        }
                                        if (hasTimeConflict) {
                                            Toast.makeText(
                                                context,
                                                "该时间段与已有行为记录冲突",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@Button
                                        }
                                        selectedActivityId?.let { activityId ->
                                            onConfirm(
                                                activityId,
                                                selectedTagIds.toList(),
                                                startTime.toLocalTime(),
                                                if (mode == BehaviorNature.COMPLETED) endTime.toLocalTime() else null,
                                                nature,
                                                note.ifBlank { null }
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (hasTimeConflict)
                                            MaterialTheme.colorScheme.error
                                        else
                                            MaterialTheme.colorScheme.primaryContainer
                                    ),

                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    enabled = selectedActivityId != null,
                                ) {
                                    Text(
                                        text = if (hasTimeConflict) "时间冲突" else "确认",
                                        fontSize = 14.sp,
                                        color = if (hasTimeConflict)
                                            MaterialTheme.colorScheme.onError
                                        else
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }

        if (showTimeAdjustments && mode != BehaviorNature.PENDING) {
            when (mode) {
                BehaviorNature.COMPLETED -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .offset {
                                IntOffset(
                                    0,
                                    (innerBoxPositionInWindow.y - boxPositionInWindow.y + 90.dp.toPx()).roundToInt()
                                )
                            },
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TimeAdjustmentCard(
                            currentTime = startTime,
                            onTimeChanged = { startTime = it },
                            modifier = Modifier.weight(1f)
                        )
                        TimeAdjustmentCard(
                            currentTime = endTime,
                            onTimeChanged = { endTime = it },
                            modifier = Modifier.weight(1f),
                            maxTime = LocalDateTime.now(),
                        )
                    }
                }
                BehaviorNature.ACTIVE -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .offset {
                                IntOffset(
                                    0,
                                    (innerBoxPositionInWindow.y - boxPositionInWindow.y + 90.dp.toPx()).roundToInt()
                                )
                            },
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        TimeAdjustmentCard(
                            currentTime = startTime,
                            onTimeChanged = { startTime = it },
                            modifier = Modifier.weight(1f),
                            maxTime = LocalDateTime.now(),
                        )
                    }
                }
                BehaviorNature.PENDING -> {}
            }
        }

        if (isDragging) {
            val density = LocalDensity.current
            val gapPx = with(density) { 8.dp.toPx() }
            val optionsY =
                buttonRowPositionInWindow.y - boxPositionInWindow.y - optionsRowHeight - gapPx
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp)
                    .offset { IntOffset(0, optionsY.roundToInt()) }
                    .onGloballyPositioned { coords ->
                        optionsRowHeight = coords.size.height.toFloat()
                    },
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val options = listOf("测试1", "测试2", "测试3", "添加自定义功能")
                options.forEach { option ->
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
    }

    if (showAddActivityDialog) {
        // Todo 待完善增加活动弹窗 以及绑定数据
        AddActivityDialog(
            onDismiss = { showAddActivityDialog = false },
            onConfirm = { name, emoji ->
                onAddActivity(name, emoji)
                showAddActivityDialog = false
            },
        )
    }

    if (showAddTagDialog) {
        // Todo 待完善增加标签弹窗 以及绑定数据
        AddTagDialog(
            onDismiss = { showAddTagDialog = false },
            onConfirm = { name ->
                onAddTag(name)
                showAddTagDialog = false
            },
        )
    }

    // 活动分类选择弹窗
    if (showActivityPicker) {
        val activityGroupsMap = remember(activityGroups) {
            activityGroups.associateBy { it.id }
        }
        val groupedActivities = remember(activities, activityGroups) {
            val groups = activities.groupBy { it.groupId }
                .map { (groupId, items) ->
                    val group = if (groupId != null) activityGroupsMap[groupId] else null
                    CategoryGroup(
                        id = groupId ?: -1L,
                        name = group?.name ?: "未分类",
                        items = items.map { ActivityCategorizable(it) },
                    )
                }
                .sortedBy { if (it.id == -1L) Int.MAX_VALUE.toLong() else activityGroupsMap[it.id]?.sortOrder?.toLong() ?: Long.MAX_VALUE }
            groups
        }

        CategoryPickerDialog(
            title = "选择活动",
            items = activities.map { ActivityCategorizable(it) },
            categoryGroups = groupedActivities,
            selectedId = selectedActivityId,
            onItemSelected = { id ->
                selectedActivityId = id
                showActivityPicker = false
            },
            onDismiss = { showActivityPicker = false },
            onAddNew = {
                showActivityPicker = false
                showAddActivityDialog = true
            },
        )
    }

    // 标签分类选择弹窗
    if (showTagPicker) {
        val groupedTags = remember(allTags) {
            val groups = allTags.groupBy { it.category ?: "未分类" }
                .map { (category, items) ->
                    CategoryGroup(
                        id = category.hashCode().toLong(),
                        name = category,
                        items = items.map { TagCategorizable(it) },
                    )
                }
                .sortedBy { it.name }
            groups
        }

        CategoryPickerDialog(
            title = "选择标签",
            items = allTags.map { TagCategorizable(it) },
            categoryGroups = groupedTags,
            selectedIds = selectedTagIds,
            multiSelect = true,
            onItemsSelected = { ids ->
                selectedTagIds = ids
            },
            onDismiss = { showTagPicker = false },
            onAddNew = {
                showTagPicker = false
                showAddTagDialog = true
            },
        )
    }
}

@Composable
private fun TimeAdjustmentCard(
    currentTime: LocalDateTime,
    onTimeChanged: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
    maxTime: LocalDateTime? = null,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TimeAdjustmentComponent(
                currentTime = currentTime,
                onTimeChanged = onTimeChanged,
                maxTime = maxTime,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddBehaviorSheetPreview() {
    val sampleActivities = listOf(
        Activity(1, "Coding", "👨‍💻", null, null, false),
        Activity(2, "Reading", "📚", null, null, false),
        Activity(3, "Workout", "💪", null, null, false)
    )
    val sampleTags = listOf(
        Tag(1, "Work", null, null, null, null, 0, 0, 0, false),
        Tag(2, "Study", null, null, null, null, 0, 0, 0, false)
    )
    val sampleGroups = listOf(
        ActivityGroup(1, "工作", 0),
        ActivityGroup(2, "学习", 1),
    )

    NLtimerTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            AddBehaviorSheetContent(
                activities = sampleActivities,
                activityGroups = sampleGroups,
                allTags = sampleTags,
                dialogConfig = DialogGridConfig(),
                existingBehaviors = emptyList(),
                onConfirm = { _, _, _, _, _, _ -> },
                onDismiss = { },
            )
        }
    }
}

// 活动适配器，实现 CategorizableItem 接口
private data class ActivityCategorizable(
    val activity: Activity,
) : CategorizableItem {
    override val itemId: Long = activity.id
    override val itemName: String = activity.name
    override val category: String? = null // 分组由外部处理
    override val usageCount: Int = 0 // 需要从统计数据获取
    override val lastUsedTimestamp: Long? = null
    override val emoji: String? = activity.emoji
}

// 标签适配器，实现 CategorizableItem 接口
private data class TagCategorizable(
    val tag: Tag,
) : CategorizableItem {
    override val itemId: Long = tag.id
    override val itemName: String = tag.name
    override val category: String? = tag.category
    override val usageCount: Int = tag.usageCount
    override val lastUsedTimestamp: Long? = null
    override val emoji: String? = null
}
