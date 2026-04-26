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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val behaviorRepository: BehaviorRepository,
    private val activityRepository: ActivityRepository,
    private val tagRepository: TagRepository,
    private val matchStrategy: MatchStrategy,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities

    private val _tagsForSelectedActivity = MutableStateFlow<List<Tag>>(emptyList())
    val tagsForSelectedActivity: StateFlow<List<Tag>> = _tagsForSelectedActivity

    private var selectedActivityId: Long? = null

    private val today = LocalDate.now()

    init {
        loadBehaviors()
        loadActivities()
    }

    private fun loadActivities() {
        viewModelScope.launch {
            activityRepository.getAllActive().collect { list ->
                _activities.update { list }
            }
        }
    }

    private fun loadBehaviors() {
        viewModelScope.launch {
            val dayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val dayEnd = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            combine(
                behaviorRepository.getByDayRange(dayStart, dayEnd),
                behaviorRepository.getCurrentBehavior(),
            ) { behaviors, currentBehavior ->
                buildUiState(behaviors, currentBehavior)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
                .collect { state -> _uiState.update { state } }
        }
    }

    private suspend fun buildUiState(
        behaviors: List<Behavior>,
        currentBehavior: Behavior?,
    ): HomeUiState {
        if (behaviors.isEmpty()) {
            val now = LocalTime.now()
            val currentRow = GridRowUiState(
                rowId = "current-empty",
                startTime = now,
                isCurrentRow = true,
                isLocked = false,
                cells = buildEmptyCells(),
            )
            return HomeUiState(
                rows = listOf(currentRow),
                currentRowId = currentRow.rowId,
                isLoading = false,
                selectedTimeHour = now.hour,
            )
        }

        val rows = mutableListOf<GridRowUiState>()
        val grouped = behaviors.groupBy { behavior ->
            val startTime = java.time.Instant.ofEpochMilli(behavior.startTime)
                .atZone(ZoneId.systemDefault()).toLocalTime()
            startTime.truncatedTo(ChronoUnit.HOURS)
        }

        val sortedKeys = grouped.keys.sorted()
        var currentRowId: String? = null

        for (timeSlot in sortedKeys) {
            val slotBehaviors = grouped[timeSlot] ?: continue
            val cells = mutableListOf<GridCellUiState>()

            for (behavior in slotBehaviors) {
                val activity = activityRepository.getById(behavior.activityId)
                val tags = try {
                    tagRepository.getByActivityId(behavior.activityId).firstOrNull() ?: emptyList()
                } catch (_: Exception) {
                    emptyList()
                }

                val isCurrent = currentBehavior?.id == behavior.id
                if (isCurrent) currentRowId = timeSlot.toString()

                cells.add(
                    GridCellUiState(
                        behaviorId = behavior.id,
                        activityEmoji = activity?.emoji,
                        activityName = activity?.name,
                        tags = tags.map { TagUiState(id = it.id, name = it.name, color = it.color) },
                        nature = behavior.nature,
                        isCurrent = isCurrent,
                    )
                )
            }

            while (cells.size < 4) {
                cells.add(
                    GridCellUiState(
                        behaviorId = null,
                        activityEmoji = null,
                        activityName = null,
                        tags = emptyList(),
                        nature = null,
                        isCurrent = false,
                    )
                )
            }

            val isCurrentRow = currentBehavior?.let { cb ->
                val cbStart = java.time.Instant.ofEpochMilli(cb.startTime)
                    .atZone(ZoneId.systemDefault()).toLocalTime()
                    .truncatedTo(ChronoUnit.HOURS)
                cbStart == timeSlot
            } ?: false

            rows.add(
                GridRowUiState(
                    rowId = timeSlot.toString(),
                    startTime = timeSlot,
                    isCurrentRow = isCurrentRow,
                    isLocked = false,
                    cells = cells.take(4),
                )
            )
        }

        val lastRow = rows.lastOrNull()
        val hasEmptySlot = lastRow?.cells?.any { it.behaviorId == null } == true
        val hasCurrent = lastRow?.cells?.any { it.isCurrent } == true

        if (lastRow != null && hasEmptySlot && !hasCurrent) {
            val now = LocalTime.now().truncatedTo(ChronoUnit.HOURS)
            if (now !in sortedKeys) {
                rows.add(
                    GridRowUiState(
                        rowId = "next-$now",
                        startTime = now,
                        isCurrentRow = true,
                        isLocked = false,
                        cells = buildEmptyCells(),
                    )
                )
                currentRowId = "next-$now"
            }
        }

        return HomeUiState(
            rows = rows,
            currentRowId = currentRowId,
            isLoading = false,
            selectedTimeHour = LocalTime.now().hour,
        )
    }

    private fun buildEmptyCells(): List<GridCellUiState> = List(4) {
        GridCellUiState(
            behaviorId = null,
            activityEmoji = null,
            activityName = null,
            tags = emptyList(),
            nature = null,
            isCurrent = false,
        )
    }

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
        nature: BehaviorNature,
        note: String?,
    ) {
        viewModelScope.launch {
            if (nature == BehaviorNature.CURRENT) {
                behaviorRepository.endCurrentBehavior(startTime)
            }
            behaviorRepository.insert(
                Behavior(
                    id = 0,
                    activityId = activityId,
                    startTime = startTime,
                    endTime = if (nature != BehaviorNature.CURRENT) startTime else null,
                    nature = nature,
                    note = note,
                    pomodoroCount = 0,
                ),
                tagIds = tagIds,
            )
            hideAddSheet()
        }
    }

    fun scrollToTime(hour: Int) {
        _uiState.update { it.copy(selectedTimeHour = hour) }
    }
}
