package com.nltimer.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.model.Behavior
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.model.DialogGridConfig
import com.nltimer.core.data.model.Tag
import com.nltimer.core.data.repository.ActivityRepository
import com.nltimer.core.data.repository.BehaviorRepository
import com.nltimer.core.data.repository.TagRepository
import com.nltimer.core.data.SettingsPrefs
import com.nltimer.core.designsystem.theme.HomeLayout
import com.nltimer.core.designsystem.theme.TimeLabelConfig
import com.nltimer.feature.home.match.MatchStrategy
import com.nltimer.feature.home.model.AddSheetMode
import com.nltimer.feature.home.model.GridCellUiState
import com.nltimer.feature.home.model.GridRowUiState
import com.nltimer.feature.home.model.HomeUiState
import com.nltimer.feature.home.model.TagUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

/**
 * 首页 ViewModel。
 * 负责加载当天行为、活动和标签数据，管理添加/完成行为、布局切换等交互逻辑。
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val behaviorRepository: BehaviorRepository,
    private val activityRepository: ActivityRepository,
    private val tagRepository: TagRepository,
    private val settingsPrefs: SettingsPrefs,
    private val matchStrategy: MatchStrategy,
) : ViewModel() {

    // --- 暴露给 UI 的状态流 ---
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // 活动列表流
    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities.asStateFlow()

    // 活动分组流
    private val _activityGroups = MutableStateFlow<List<ActivityGroup>>(emptyList())
    val activityGroups: StateFlow<List<ActivityGroup>> = _activityGroups.asStateFlow()

    // 当前选中活动关联的标签流
    private val _tagsForSelectedActivity = MutableStateFlow<List<Tag>>(emptyList())
    val tagsForSelectedActivity: StateFlow<List<Tag>> = _tagsForSelectedActivity.asStateFlow()

    // 全部标签流
    private val _allTags = MutableStateFlow<List<Tag>>(emptyList())
    val allTags: StateFlow<List<Tag>> = _allTags.asStateFlow()

    // 弹窗配置流
    val dialogConfig: StateFlow<DialogGridConfig> = settingsPrefs.getDialogConfigFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DialogGridConfig())

    val timeLabelConfig: StateFlow<TimeLabelConfig> = settingsPrefs.getTimeLabelConfigFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimeLabelConfig())

    // 当前选中的活动 ID（用于标签过滤）
    private var selectedActivityId: Long? = null

    private val today = LocalDate.now()

    // 初始化时加载所有数据
    init {
        loadHomeBehaviors()
        loadActivitiesAndGroups()
        loadAllTags()
    }

    // 从仓库加载活动列表和分组
    private fun loadActivitiesAndGroups() {
        viewModelScope.launch {
            combine(
                activityRepository.getAllActive(),
                activityRepository.getAllGroups()
            ) { activities, groups ->
                activities to groups
            }.collect { (activities, groups) ->
                _activities.update { activities }
                _activityGroups.update { groups }
            }
        }
    }

    // 从仓库加载全部标签
    private fun loadAllTags() {
        viewModelScope.launch {
            tagRepository.getAllActive().collect { list ->
                _allTags.update { list }
            }
        }
    }

    // 从仓库加载当天行为数据，并构建 UI 状态
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

    // 将领域行为数据转换为 UI 状态，包括分页、标签填充和占位单元格
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

        val allActivities = activityRepository.getAll().firstOrNull().orEmpty()
        val activityMap = allActivities.associateBy { it.id }

        val rows = mutableListOf<GridRowUiState>()
        val cells = behaviors.map { behavior ->
            val activity = activityMap[behavior.activityId]
            val tags = try {
                behaviorRepository.getTagsForBehavior(behavior.id).firstOrNull() ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
            val isActive = behavior.status == BehaviorNature.ACTIVE
            val startLocal = java.time.Instant.ofEpochMilli(behavior.startTime)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
            val endLocal = behavior.endTime?.let {
                java.time.Instant.ofEpochMilli(it)
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime()
            }

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
                startTime = startLocal,
                endTime = endLocal,
                note = behavior.note,
                pomodoroCount = behavior.pomodoroCount,
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

        // 获取最后一个已完成行为的结束时间
        val lastBehaviorEndTime = behaviors
            .filter { it.endTime != null }
            .maxByOrNull { it.endTime ?: 0 }
            ?.let {
                java.time.Instant.ofEpochMilli(it.endTime!!)
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime()
            }

        return HomeUiState(
            rows = rows,
            currentRowId = currentRowId,
            isLoading = false,
            selectedTimeHour = now.hour,
            hasActiveBehavior = hasActive,
            lastBehaviorEndTime = lastBehaviorEndTime,
        )
    }

    // 添加新活动到仓库
    fun addActivity(name: String, emoji: String) {
        viewModelScope.launch {
            activityRepository.insert(
                Activity(
                    id = 0,
                    name = name,
                    emoji = emoji.ifBlank { null },
                    iconKey = null,
                    isArchived = false,
                )
            )
        }
    }

    // 添加新标签到仓库
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

    fun showAddSheet(mode: AddSheetMode = AddSheetMode.COMPLETED) {
        _uiState.update { it.copy(addSheetMode = mode) }
    }

    fun hideAddSheet() {
        _uiState.update { it.copy(addSheetMode = null) }
        selectedActivityId = null
        _tagsForSelectedActivity.update { emptyList() }
    }

    // 选中活动时加载其关联标签
    fun onActivitySelected(activityId: Long) {
        selectedActivityId = activityId
        viewModelScope.launch {
            tagRepository.getByActivityId(activityId).collect { tags ->
                _tagsForSelectedActivity.update { tags }
            }
        }
    }

    // 添加新行为：ACTIVE 时先结束当前行为，然后插入新记录
    fun addBehavior(
        activityId: Long,
        tagIds: List<Long>,
        startTime: Long,
        endTime: Long?,
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
                    endTime = if (status == BehaviorNature.COMPLETED) endTime ?: startTime else null,
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

    // 完成指定行为（根据空闲模式决定后续行为处理）
    fun completeBehavior(behaviorId: Long) {
        viewModelScope.launch {
            val isIdle = _uiState.value.isIdleMode
            behaviorRepository.completeCurrentAndStartNext(behaviorId, isIdle)
        }
    }

    // 切换空闲模式开关
    fun toggleIdleMode() {
        _uiState.update { it.copy(isIdleMode = !it.isIdleMode) }
    }

    // 启动下一个待办行为
    fun startNextPending() {
        viewModelScope.launch {
            val next = behaviorRepository.getNextPending() ?: return@launch
            behaviorRepository.setStatus(next.id, BehaviorNature.ACTIVE.key)
            behaviorRepository.setStartTime(next.id, System.currentTimeMillis())
        }
    }

    // 重新排序目标列表
    fun reorderGoals(orderedIds: List<Long>) {
        viewModelScope.launch {
            behaviorRepository.reorderGoals(orderedIds)
        }
    }

    // 删除指定行为
    fun deleteBehavior(id: Long) {
        viewModelScope.launch {
            behaviorRepository.delete(id)
        }
    }

    // 滚动到指定小时
    fun scrollToTime(hour: Int) {
        _uiState.update { it.copy(selectedTimeHour = hour) }
    }

    // 更新主题中保存的首页布局模式
    fun onHomeLayoutChange(layout: HomeLayout) {
        viewModelScope.launch {
            settingsPrefs.getThemeFlow().firstOrNull()?.let { theme ->
                settingsPrefs.updateTheme(theme.copy(homeLayout = layout))
            }
        }
    }

    fun onTimeLabelConfigChange(config: TimeLabelConfig) {
        viewModelScope.launch {
            settingsPrefs.updateTimeLabelConfig(config)
        }
    }
}
