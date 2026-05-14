package com.nltimer.feature.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.model.Behavior
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.model.DialogGridConfig
import com.nltimer.core.data.model.Tag
import com.nltimer.feature.home.model.AddSheetMode
import com.nltimer.feature.home.model.HomeUiState
import com.nltimer.core.behaviorui.sheet.AddBehaviorSheet
import com.nltimer.core.behaviorui.sheet.AddCurrentBehaviorSheet
import com.nltimer.core.behaviorui.sheet.AddTargetBehaviorSheet
import com.nltimer.core.tools.match.NoteScanResult
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@Composable
internal fun HomeSheetRouter(
    uiState: HomeUiState,
    activities: List<Activity>,
    activityGroups: List<ActivityGroup>,
    allTags: List<Tag>,
    dialogConfig: DialogGridConfig,
    onDismissSheet: () -> Unit,
    onAddBehavior: (activityId: Long, tagIds: List<Long>, startTime: LocalTime, endTime: LocalTime?, nature: BehaviorNature, note: String?) -> Unit,
    onAddActivity: (name: String, iconKey: String?, color: Long?, groupId: Long?, keywords: String?, tagIds: List<Long>) -> Unit,
    onAddTag: (name: String, color: Long?, icon: String?, priority: Int, category: String?, keywords: String?, activityId: Long?) -> Unit,
    onMatchNote: (String) -> NoteScanResult,
) {
    if (uiState.addSheetMode == null) return

    val existingBehaviors by remember(uiState.momentCells) {
        derivedStateOf {
            uiState.momentCells
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
        AddSheetMode.COMPLETED -> AddBehaviorSheet(
            activities = activities,
            activityGroups = activityGroups,
            allTags = allTags,
            dialogConfig = dialogConfig,
            initialStartTime = uiState.idleStartTime ?: uiState.lastBehaviorEndTime,
            initialEndTime = uiState.idleEndTime,
            initialActivityId = uiState.editInitialActivityId,
            initialTagIds = uiState.editInitialTagIds,
            initialNote = uiState.editInitialNote,
            editBehaviorId = uiState.editBehaviorId,
            existingBehaviors = existingBehaviors,
            onDismiss = onDismissSheet,
            onConfirm = onAddBehavior,
            onAddActivity = onAddActivity,
            onAddTag = onAddTag,
            onMatchNote = onMatchNote,
        )

        AddSheetMode.CURRENT -> AddCurrentBehaviorSheet(
            activities = activities,
            activityGroups = activityGroups,
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
            onMatchNote = onMatchNote,
        )

        AddSheetMode.TARGET -> AddTargetBehaviorSheet(
            activities = activities,
            activityGroups = activityGroups,
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
            onMatchNote = onMatchNote,
        )

        null -> {}
    }
}
