package com.nltimer.core.behaviorui.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.nltimer.core.data.model.Behavior
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.model.DialogGridConfig
import com.nltimer.core.data.model.SecondsStrategy
import com.nltimer.core.data.util.hasTimeConflict
import com.nltimer.core.designsystem.component.DragMenuState
import com.nltimer.core.designsystem.theme.PathDrawMode
import com.nltimer.core.tools.match.NoteScanResult
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
        private set
    var startTime by mutableStateOf(
        initialStartTime?.let { now.withHour(it.hour).withMinute(it.minute).withSecond(it.second) } ?: now
    )
    var endTime by mutableStateOf(
        initialEndTime?.let { now.withHour(it.hour).withMinute(it.minute).withSecond(it.second) } ?: now
    )

    /**
     * endTime 是否随系统时钟自动推进。
     *
     * 启用条件：当前为完成（COMPLETED）弹窗、调用方未指定结束时间、也不在编辑既有行为
     * —— 这种"现在刚做完"的新建场景下，"用时"应像秒表一样实时增长。
     * 一旦用户手动调整时间，立即关闭以保留用户选择。
     */
    var endTimeAutoTracking by mutableStateOf(
        mode == BehaviorNature.COMPLETED && initialEndTime == null && editBehaviorId == null
    )
        private set

    fun markUserAdjustedTime() {
        userAdjustedTime = true
        endTimeAutoTracking = false
    }

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
    val dragMenuState = DragMenuState()

    val effectiveMode: PathDrawMode = if (dialogConfig.pathDrawMode == PathDrawMode.Random) {
        val candidates = PathDrawMode.entries.filter {
            it != PathDrawMode.Random && it != PathDrawMode.None && it != PathDrawMode.WrigglingMaggot
        }
        candidates.random()
    } else {
        dialogConfig.pathDrawMode
    }

    var boxPositionInWindow by mutableStateOf(Offset.Zero)
    var innerBoxPositionInWindow by mutableStateOf(Offset.Zero)

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

    /**
     * 把备注扫描结果合并到当前选中状态（不破坏用户已手动选中的项）。
     *
     * 合并规则：
     * - 活动（单选）：仅当 [selectedActivityId] 为 null 且扫描结果有命中时写入；已选活动**不被覆盖**
     * - 标签（多选）：把命中标签 ID 并入现有集合，已有选中保留
     *
     * @return 实际产生的变更摘要，调用方可据此显示反馈（例如 Toast）
     */
    fun applyNoteScan(result: NoteScanResult): NoteScanApplyOutcome {
        val activityAdded = if (selectedActivityId == null && result.activityId != null) {
            selectedActivityId = result.activityId
            true
        } else false

        val newTagIds = result.tagIds - selectedTagIds
        if (newTagIds.isNotEmpty()) {
            selectedTagIds = selectedTagIds + newTagIds
        }

        return NoteScanApplyOutcome(
            activityAdded = activityAdded,
            activityHeld = !activityAdded && result.activityId != null && selectedActivityId != result.activityId,
            tagsAdded = newTagIds.size,
        )
    }
}

/**
 * `applyNoteScan` 的副作用摘要，用于上层反馈。
 *
 * @property activityAdded 本次扫描结果是否实际写入了 [AddBehaviorState.selectedActivityId]
 * @property activityHeld 扫描命中活动但因用户已选而保持原状（用于"已保留你的活动选择"提示）
 * @property tagsAdded 本次新增到 [AddBehaviorState.selectedTagIds] 的标签数量（去重后）
 */
internal data class NoteScanApplyOutcome(
    val activityAdded: Boolean,
    val activityHeld: Boolean,
    val tagsAdded: Int,
) {
    val hasAnyChange: Boolean get() = activityAdded || tagsAdded > 0
}
