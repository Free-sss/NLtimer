package com.nltimer.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.Behavior
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.model.Tag
import com.nltimer.core.data.repository.ActivityRepository
import com.nltimer.core.data.repository.BehaviorRepository
import com.nltimer.core.data.repository.TagRepository
import com.nltimer.feature.home.match.MatchStrategy
import com.nltimer.feature.home.model.GridCellUiState
import com.nltimer.feature.home.model.GridRowUiState
import com.nltimer.feature.home.model.HomeUiState
import com.nltimer.feature.home.model.TagUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val behaviorRepository: BehaviorRepository,
    private val activityRepository: ActivityRepository,
    private val tagRepository: TagRepository,
    private val matchStrategy: MatchStrategy,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities.asStateFlow()

    private val _tagsForSelectedActivity = MutableStateFlow<List<Tag>>(emptyList())
    val tagsForSelectedActivity: StateFlow<List<Tag>> = _tagsForSelectedActivity.asStateFlow()

    private val _allTags = MutableStateFlow<List<Tag>>(emptyList())
    val allTags: StateFlow<List<Tag>> = _allTags.asStateFlow()

    private var selectedActivityId: Long? = null

    private val today = LocalDate.now()

    init {
        loadHomeBehaviors()
        loadActivities()
        loadAllTags()
    }

    private fun loadActivities() {
        viewModelScope.launch {
            activityRepository.getAllActive().collect { list ->
                _activities.update { list }
            }
        }
    }

    private fun loadAllTags() {
        viewModelScope.launch {
            tagRepository.getAllActive().collect { list ->
                _allTags.update { list }
            }
        }
    }

    private fun loadHomeBehaviors() {
        viewModelScope.launch {
            val dayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val dayEnd = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            behaviorRepository.getHomeBehaviors(dayStart, dayEnd)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
                .collect { behaviors ->
                    val state = buildUiState(behaviors)
                    _uiState.update { state }
                }
        }
    }

    private suspend fun buildUiState(behaviors: List<Behavior>): HomeUiState {
        val now = LocalTime.now()
        val hasActive = behaviors.any { it.status == BehaviorNature.ACTIVE }

        if (behaviors.isEmpty()) {
            val addCell = GridCellUiState(
                behaviorId = null,
                activityEmoji = null,
                activityName = null,
                tags = emptyList(),
                status = null,
                isCurrent = false,
                isAddPlaceholder = true,
            )
            val row = GridRowUiState(
                rowId = "empty-row",
                startTime = now,
                isCurrentRow = true,
                isLocked = false,
                cells = listOf(addCell),
            )
            return HomeUiState(
                rows = listOf(row),
                currentRowId = row.rowId,
                isLoading = false,
                selectedTimeHour = now.hour,
                hasActiveBehavior = false,
            )
        }

        val rows = mutableListOf<GridRowUiState>()
        val cells = behaviors.map { behavior ->
            val activity = activityRepository.getById(behavior.activityId)
            val tags = try {
                behaviorRepository.getTagsForBehavior(behavior.id).firstOrNull() ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
            val isActive = behavior.status == BehaviorNature.ACTIVE

            GridCellUiState(
                behaviorId = behavior.id,
                activityEmoji = activity?.emoji,
                activityName = activity?.name,
                tags = tags.map { TagUiState(id = it.id, name = it.name, color = it.color, isActive = !it.isArchived) },
                status = behavior.status,
                isCurrent = isActive,
                wasPlanned = behavior.wasPlanned,
                achievementLevel = behavior.achievementLevel,
                estimatedDuration = behavior.estimatedDuration,
                actualDuration = behavior.actualDuration,
                durationMs = if (isActive && behavior.startTime > 0) {
                    System.currentTimeMillis() - behavior.startTime
                } else null,
            )
        }

        val addCell = GridCellUiState(
            behaviorId = null,
            activityEmoji = null,
            activityName = null,
            tags = emptyList(),
            status = null,
            isCurrent = false,
            isAddPlaceholder = true,
        )
        val allCells = cells + addCell

        var currentRowId: String? = null
        allCells.chunked(4).forEachIndexed { rowIndex, rowCells ->
            val rowId = "row-$rowIndex-${rowCells.firstOrNull()?.behaviorId ?: "add"}"
            val hasCurrentInRow = rowCells.any { it.isCurrent }
            if (hasCurrentInRow) currentRowId = rowId

            val timeForRow = if (rowIndex < allCells.size / 4) {
                val behavior = behaviors.getOrNull(rowIndex * 4)
                if (behavior != null) {
                    java.time.Instant.ofEpochMilli(behavior.startTime)
                        .atZone(ZoneId.systemDefault())
                        .toLocalTime()
                } else {
                    now
                }
            } else {
                now
            }

            val paddedCells = rowCells.toMutableList()
            while (paddedCells.size < 4) {
                paddedCells.add(
                    GridCellUiState(
                        behaviorId = null,
                        activityEmoji = null,
                        activityName = null,
                        tags = emptyList(),
                        status = null,
                        isCurrent = false,
                    )
                )
            }

            rows.add(
                GridRowUiState(
                    rowId = rowId,
                    startTime = timeForRow,
                    isCurrentRow = hasCurrentInRow,
                    isLocked = false,
                    cells = paddedCells,
                )
            )
        }

        return HomeUiState(
            rows = rows,
            currentRowId = currentRowId,
            isLoading = false,
            selectedTimeHour = now.hour,
            hasActiveBehavior = hasActive,
        )
    }

    fun addActivity(name: String, emoji: String) {
        viewModelScope.launch {
            activityRepository.insert(
                Activity(
                    id = 0,
                    name = name,
                    emoji = emoji.ifBlank { null },
                    iconKey = null,
//                    category = null,
                    isArchived = false,
                )
            )
        }
    }

    fun addTag(name: String) {
        viewModelScope.launch {
            tagRepository.insert(
                Tag(
                    id = 0,
                    name = name,
                    color = null,
                    textColor = null,
                    icon = null,
                    category = null,
                    priority = 0,
                    usageCount = 0,
                    sortOrder = 0,
                    isArchived = false,
                )
            )
        }
    }

    // feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/AddBehaviorSheet.kt
    fun showAddSheet() {
        _uiState.update { it.copy(isAddSheetVisible = true) }
    }

    fun hideAddSheet() {
        _uiState.update { it.copy(isAddSheetVisible = false) }
        selectedActivityId = null
        _tagsForSelectedActivity.update { emptyList() }
    }

    fun onActivitySelected(activityId: Long) {
        selectedActivityId = activityId
        viewModelScope.launch {
            tagRepository.getByActivityId(activityId).collect { tags ->
                _tagsForSelectedActivity.update { tags }
            }
        }
    }

    fun addBehavior(
        activityId: Long,
        tagIds: List<Long>,
        startTime: Long,
        status: BehaviorNature,
        note: String?,
    ) {
        viewModelScope.launch {
            if (status == BehaviorNature.ACTIVE) {
                behaviorRepository.endCurrentBehavior(startTime)
            }

            val maxSeq = behaviorRepository.getMaxSequence()
            val wasPlanned = status == BehaviorNature.PENDING

            behaviorRepository.insert(
                Behavior(
                    id = 0,
                    activityId = activityId,
                    startTime = if (status == BehaviorNature.PENDING) 0L else startTime,
                    endTime = if (status == BehaviorNature.COMPLETED) startTime else null,
                    status = status,
                    note = note,
                    pomodoroCount = 0,
                    sequence = maxSeq + 1,
                    estimatedDuration = null,
                    actualDuration = null,
                    achievementLevel = null,
                    wasPlanned = wasPlanned,
                ),
                tagIds = tagIds,
            )
            hideAddSheet()
        }
    }

    fun completeBehavior(behaviorId: Long) {
        viewModelScope.launch {
            val isIdle = _uiState.value.isIdleMode
            behaviorRepository.completeCurrentAndStartNext(behaviorId, isIdle)
        }
    }

    fun toggleIdleMode() {
        _uiState.update { it.copy(isIdleMode = !it.isIdleMode) }
    }

    fun startNextPending() {
        viewModelScope.launch {
            val next = behaviorRepository.getNextPending() ?: return@launch
            behaviorRepository.setStatus(next.id, "active")
            behaviorRepository.setStartTime(next.id, System.currentTimeMillis())
        }
    }

    fun reorderGoals(orderedIds: List<Long>) {
        viewModelScope.launch {
            behaviorRepository.reorderGoals(orderedIds)
        }
    }

    fun deleteBehavior(id: Long) {
        viewModelScope.launch {
            behaviorRepository.delete(id)
        }
    }

    fun scrollToTime(hour: Int) {
        _uiState.update { it.copy(selectedTimeHour = hour) }
    }
}
