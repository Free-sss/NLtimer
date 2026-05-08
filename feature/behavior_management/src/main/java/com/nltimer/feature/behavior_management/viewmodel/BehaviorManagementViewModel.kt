package com.nltimer.feature.behavior_management.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.model.BehaviorWithDetails
import com.nltimer.core.data.repository.ActivityRepository
import com.nltimer.core.data.repository.BehaviorRepository
import com.nltimer.core.data.repository.TagRepository
import com.nltimer.feature.behavior_management.model.BehaviorManagementUiState
import com.nltimer.feature.behavior_management.model.TimeRangePreset
import com.nltimer.feature.behavior_management.model.ViewMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BehaviorManagementViewModel @Inject constructor(
    private val behaviorRepository: BehaviorRepository,
    private val activityRepository: ActivityRepository,
    private val tagRepository: TagRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BehaviorManagementUiState())
    val uiState: StateFlow<BehaviorManagementUiState> = _uiState.asStateFlow()

    val activityGroups = activityRepository.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val tagCategories = tagRepository.getDistinctCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allActivities = activityRepository.getAllActive()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allTags = tagRepository.getAllActive()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        observeBehaviors()
    }

    private fun observeBehaviors() {
        viewModelScope.launch {
            _uiState
                .map { Pair(it.rangeStartDate, it.timeRange) }
                .distinctUntilChanged()
                .flatMapLatest { (date, preset) ->
                    val startEpoch = date
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                    val endEpoch = startEpoch + preset.hours * 3600_000L
                    behaviorRepository.getBehaviorsWithDetailsByTimeRange(startEpoch, endEpoch)
                        .combine(activityGroups) { behaviors, groups ->
                            Pair(behaviors, groups)
                        }
                }
                .combine(_uiState) { (behaviors, groups), state ->
                    applyFilters(behaviors, state, groups)
                }
                .collect { filtered ->
                    _uiState.update { it.copy(behaviors = filtered.toImmutableList()) }
                }
        }
    }

    private fun applyFilters(
        behaviors: List<BehaviorWithDetails>,
        state: BehaviorManagementUiState,
        groups: List<ActivityGroup>,
    ): List<BehaviorWithDetails> {
        var result = behaviors

        state.selectedActivityGroup?.let { groupName ->
            val groupIds = groups
                .filter { it.name == groupName }
                .map { it.id }
                .toSet()
            result = result.filter { it.activity.groupId in groupIds }
        }

        state.selectedTagCategory?.let { category ->
            result = result.filter { bwd ->
                bwd.tags.any { it.category == category }
            }
        }

        state.selectedStatus?.let { nature ->
            result = result.filter { it.behavior.status == nature }
        }

        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.lowercase()
            result = result.filter { bwd ->
                bwd.activity.name.lowercase().contains(query) ||
                    bwd.tags.any { it.name.lowercase().contains(query) } ||
                    bwd.behavior.note?.lowercase()?.contains(query) == true
            }
        }

        return result
    }

    fun setTimeRange(preset: TimeRangePreset) {
        _uiState.update { it.copy(timeRange = preset) }
    }

    fun setRangeStartDate(date: LocalDate) {
        _uiState.update { it.copy(rangeStartDate = date) }
    }

    fun navigateRange(direction: Int) {
        _uiState.update { state ->
            val daysToAdd = when (state.timeRange) {
                TimeRangePreset.FOUR_HOURS, TimeRangePreset.EIGHT_HOURS -> 1L
                TimeRangePreset.ONE_DAY -> 1L
                TimeRangePreset.THREE_DAYS -> 3L
                TimeRangePreset.SEVEN_DAYS -> 7L
                TimeRangePreset.ONE_MONTH -> 30L
                TimeRangePreset.ONE_YEAR -> 365L
            }
            state.copy(rangeStartDate = state.rangeStartDate.plusDays(daysToAdd * direction))
        }
    }

    fun setActivityGroupFilter(groupName: String?) {
        _uiState.update { it.copy(selectedActivityGroup = groupName) }
    }

    fun setTagCategoryFilter(category: String?) {
        _uiState.update { it.copy(selectedTagCategory = category) }
    }

    fun setStatusFilter(nature: BehaviorNature?) {
        _uiState.update { it.copy(selectedStatus = nature) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setViewMode(mode: ViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun startEditBehavior(behaviorId: Long) {
        _uiState.update { it.copy(editBehaviorId = behaviorId) }
    }

    fun finishEditBehavior() {
        _uiState.update { it.copy(editBehaviorId = null) }
    }

    fun toggleMultiSelect(behaviorId: Long) {
        _uiState.update { state ->
            val newIds = if (behaviorId in state.selectedBehaviorIds) {
                state.selectedBehaviorIds - behaviorId
            } else {
                state.selectedBehaviorIds + behaviorId
            }
            state.copy(
                selectedBehaviorIds = newIds,
                isMultiSelectMode = newIds.isNotEmpty(),
            )
        }
    }

    fun exitMultiSelect() {
        _uiState.update { it.copy(selectedBehaviorIds = emptySet(), isMultiSelectMode = false) }
    }

    fun deleteSelectedBehaviors() {
        viewModelScope.launch {
            _uiState.value.selectedBehaviorIds.forEach { id ->
                behaviorRepository.delete(id)
            }
            exitMultiSelect()
        }
    }
}
