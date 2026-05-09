package com.nltimer.core.behaviorui.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.nltimer.core.data.model.Behavior
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.model.DialogGridConfig
import com.nltimer.core.data.model.SecondsStrategy
import com.nltimer.core.data.util.hasTimeConflict
import com.nltimer.core.designsystem.theme.PathDrawMode
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@Suppress("LongParameterList")
@Composable
internal fun rememberAddBehaviorState(
    mode: BehaviorNature,
    initialStartTime: LocalTime?,
    initialEndTime: LocalTime?,
    initialActivityId: Long?,
    initialTagIds: List<Long>,
    initialNote: String?,
    editBehaviorId: Long?,
    existingBehaviors: List<Behavior>,
    dialogConfig: DialogGridConfig,
): AddBehaviorState {
    return remember {
        AddBehaviorState(
            mode = mode,
            initialStartTime = initialStartTime,
            initialEndTime = initialEndTime,
            initialActivityId = initialActivityId,
            initialTagIds = initialTagIds,
            initialNote = initialNote,
            editBehaviorId = editBehaviorId,
            existingBehaviors = existingBehaviors,
            dialogConfig = dialogConfig,
        )
    }.also { state ->
        LaunchedEffect(initialActivityId) {
            if (initialActivityId != null) state.selectedActivityId = initialActivityId
        }
    }
}

internal class AddBehaviorState(
    private val mode: BehaviorNature,
    initialStartTime: LocalTime?,
    initialEndTime: LocalTime?,
    initialActivityId: Long?,
    initialTagIds: List<Long>,
    initialNote: String?,
    private val editBehaviorId: Long?,
    private val existingBehaviors: List<Behavior>,
    dialogConfig: DialogGridConfig,
) {
    var selectedActivityId by mutableStateOf(initialActivityId)
    var selectedTagIds by mutableStateOf(initialTagIds.toSet())

    val sheetOpenTime: LocalDateTime = LocalDateTime.now()
    private val now = sheetOpenTime
    var userAdjustedTime by mutableStateOf(false)
    var startTime by mutableStateOf(
        initialStartTime?.let { now.withHour(it.hour).withMinute(it.minute).withSecond(it.second) } ?: now
    )
    var endTime by mutableStateOf(
        initialEndTime?.let { now.withHour(it.hour).withMinute(it.minute).withSecond(it.second) } ?: now
    )

    val duration: Duration by derivedStateOf {
        val d = Duration.between(startTime, endTime)
        if (d.isNegative) Duration.ZERO else d
    }

    var note by mutableStateOf(initialNote ?: "")

    val hasTimeConflict: Boolean by derivedStateOf {
        if (mode == BehaviorNature.PENDING) return@derivedStateOf false
        val today = LocalDateTime.now().toLocalDate()
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
            ignoreBehaviorId = editBehaviorId,
        )
    }

    var showAddActivityDialog by mutableStateOf(false)
    var showAddTagDialog by mutableStateOf(false)
    var showActivityPicker by mutableStateOf(false)
    var showTagPicker by mutableStateOf(false)
    var showTimeAdjustments by mutableStateOf(false)

    val effectiveMode: PathDrawMode = if (dialogConfig.pathDrawMode == PathDrawMode.Random) {
        val candidates = PathDrawMode.entries.filter {
            it != PathDrawMode.Random && it != PathDrawMode.None && it != PathDrawMode.WrigglingMaggot
        }
        candidates.random()
    } else {
        dialogConfig.pathDrawMode
    }

    var isDragging by mutableStateOf(false)
    var dragOffset by mutableStateOf(Offset.Zero)
    var hoveredOption by mutableStateOf<String?>(null)
    val optionsLayoutBounds = mutableStateMapOf<String, Rect>()
    var boxPositionInWindow by mutableStateOf(Offset.Zero)
    var innerBoxPositionInWindow by mutableStateOf(Offset.Zero)
    var buttonRowPositionInWindow by mutableStateOf(Offset.Zero)
    var optionsRowHeight by mutableFloatStateOf(0f)

    fun resolveStartTime(strategy: SecondsStrategy, confirmTime: LocalDateTime): LocalDateTime {
        return if (userAdjustedTime) {
            startTime.withSecond(0).withNano(0)
        } else {
            val sourceSeconds = when (strategy) {
                SecondsStrategy.OPEN_TIME -> sheetOpenTime.second
                SecondsStrategy.CONFIRM_TIME -> confirmTime.second
            }
            startTime.withSecond(sourceSeconds).withNano(0)
        }
    }
}
