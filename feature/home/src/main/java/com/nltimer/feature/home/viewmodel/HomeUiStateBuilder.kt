package com.nltimer.feature.home.viewmodel

import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.Behavior
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.model.Tag
import com.nltimer.feature.home.model.GridCellUiState
import com.nltimer.feature.home.model.GridDaySection
import com.nltimer.feature.home.model.GridRowUiState
import com.nltimer.feature.home.model.HomeListItem
import com.nltimer.feature.home.model.HomeUiState
import com.nltimer.feature.home.model.TagUiState
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class HomeUiStateBuilder {

    companion object {
        const val STATE_TIMEOUT_MS = 5_000L
        private const val ROW_ID_FORMAT = "row-%d-%s"
        const val DEFAULT_GRID_COLUMNS = 4
    }

    fun buildUiState(
        behaviors: List<Behavior>,
        activities: List<Activity>,
        tagsByBehaviorId: Map<Long, List<Tag>>,
        now: LocalTime,
        currentTimeMs: Long,
        today: LocalDate,
        gridColumns: Int = DEFAULT_GRID_COLUMNS,
    ): HomeUiState {
        if (behaviors.isEmpty()) {
            return buildEmptyState(now)
        }

        val hasActive = calculateCurrentBehavior(behaviors)
        val activityMap = activities.associateBy { it.id }
        val sortedBehaviors = buildTimelineBehaviors(behaviors)
        val allCellsRaw = buildMomentBehaviors(sortedBehaviors, activityMap, tagsByBehaviorId, currentTimeMs)

        val todayBehaviorIds = sortedBehaviors.filter { isToday(it, today) }.map { it.id }.toSet()
        val todayCells = allCellsRaw.filter { it.behaviorId != null && it.behaviorId in todayBehaviorIds }
        val pendingCells = allCellsRaw.filter { it.status == BehaviorNature.PENDING }
        val momentCells = todayCells + pendingCells

        val addCell = buildAddCell(todayCells, now)
        val gridSections = buildGridSections(allCellsRaw, sortedBehaviors, today, addCell, now, gridColumns)
        val items = buildListItems(allCellsRaw, today)
        val (todayRows, currentRowId) = buildGridRows(
            allCells = todayCells + addCell,
            sortedBehaviors = sortedBehaviors.filter { isToday(it, today) },
            now = now,
            gridColumns = gridColumns,
            isCurrentDay = true,
        )

        val lastBehaviorEndTime = calculateLastBehaviorEndTime(behaviors)

        return HomeUiState(
            rows = todayRows,
            items = items,
            gridSections = gridSections,
            momentCells = momentCells,
            currentRowId = currentRowId,
            isLoading = false,
            selectedTimeHour = now.hour,
            hasActiveBehavior = hasActive,
            lastBehaviorEndTime = lastBehaviorEndTime,
        )
    }

    private fun isToday(behavior: Behavior, today: LocalDate): Boolean {
        if (behavior.status == BehaviorNature.PENDING) return false
        if (behavior.startTime <= 0L) return false
        return Instant.ofEpochMilli(behavior.startTime).atZone(ZoneId.systemDefault()).toLocalDate() == today
    }

    private fun cellDate(cell: GridCellUiState): LocalDate? {
        val epoch = cell.startEpochMs ?: return null
        return Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    private fun dayLabel(date: LocalDate, today: LocalDate): String {
        val days = ChronoUnit.DAYS.between(date, today)
        return when (days) {
            0L -> "今天 ${date.monthValue}/${date.dayOfMonth}"
            1L -> "昨天 ${date.monthValue}/${date.dayOfMonth}"
            else -> "${date.monthValue}/${date.dayOfMonth}"
        }
    }

    private fun buildListItems(
        allCells: List<GridCellUiState>,
        today: LocalDate,
    ): List<HomeListItem> {
        val datedCells = allCells
            .filter { it.behaviorId != null && cellDate(it) != null }
            .sortedBy { it.startEpochMs ?: Long.MAX_VALUE }
        val byDate: Map<LocalDate, List<GridCellUiState>> = datedCells
            .groupBy { cellDate(it)!! }
            .filterValues { it.isNotEmpty() }

        val result = mutableListOf<HomeListItem>()
        byDate.keys.sorted().forEach { date ->
            result.add(HomeListItem.DayDivider(date = date, label = dayLabel(date, today)))
            byDate[date]!!.forEach { cell -> result.add(HomeListItem.CellItem(cell)) }
        }
        return result
    }

    private fun buildGridSections(
        allCells: List<GridCellUiState>,
        sortedBehaviors: List<Behavior>,
        today: LocalDate,
        todayAddCell: GridCellUiState,
        now: LocalTime,
        gridColumns: Int,
    ): List<GridDaySection> {
        val datedCells = allCells.filter { it.behaviorId != null && cellDate(it) != null }
        val byDate: Map<LocalDate, List<GridCellUiState>> = datedCells
            .groupBy { cellDate(it)!! }
            .filterValues { it.isNotEmpty() }

        val sections = mutableListOf<GridDaySection>()
        byDate.keys.sorted().forEach { date ->
            val cells = byDate[date]!!.sortedBy { it.startEpochMs ?: 0L }
            val cellsForSection = if (date == today) cells + todayAddCell else cells
            val dateBehaviors = sortedBehaviors.filter { b ->
                b.startTime > 0L &&
                    Instant.ofEpochMilli(b.startTime).atZone(ZoneId.systemDefault()).toLocalDate() == date
            }
            val isTodaySection = date == today
            val rowsTime = if (isTodaySection) now else dateBehaviors.firstOrNull()?.let {
                Instant.ofEpochMilli(it.startTime).atZone(ZoneId.systemDefault()).toLocalTime()
            } ?: LocalTime.MIDNIGHT
            val (rows, _) = buildGridRows(
                allCells = cellsForSection,
                sortedBehaviors = dateBehaviors,
                now = rowsTime,
                gridColumns = gridColumns,
                isCurrentDay = isTodaySection,
            )
            sections.add(GridDaySection(date = date, label = dayLabel(date, today), rows = rows))
        }
        return sections
    }

    private fun buildEmptyState(now: LocalTime): HomeUiState {
        val addCell = GridCellUiState(
            behaviorId = null,
            activityIconKey = null,
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

    private fun buildTimelineBehaviors(behaviors: List<Behavior>): List<Behavior> {
        val nonPending = behaviors
            .filter { it.status != BehaviorNature.PENDING }
            .sortedBy { it.startTime }
        val pending = behaviors
            .filter { it.status == BehaviorNature.PENDING }
            .sortedBy { it.sequence }
        return nonPending + pending
    }

    private fun buildMomentBehaviors(
        sortedBehaviors: List<Behavior>,
        activityMap: Map<Long, Activity>,
        tagsByBehaviorId: Map<Long, List<Tag>>,
        currentTimeMs: Long,
    ): List<GridCellUiState> {
        return sortedBehaviors.map { behavior ->
            val activity = activityMap[behavior.activityId]
            val tags = tagsByBehaviorId[behavior.id].orEmpty()
            val isActive = behavior.status == BehaviorNature.ACTIVE
            val isPending = behavior.status == BehaviorNature.PENDING
            // PENDING 的 startTime 在数据库里是 0L，用 Instant.ofEpochMilli(0) 在 UTC+N 时区下
            // 会被解析成 08:00 之类的幽灵值（中国时区 UTC+8 → 1970-01-01 08:00）。
            // 因此这里直接置 null，避免详情弹窗里出现伪造的开始时间。
            val startLocal = if (isPending || behavior.startTime <= 0L) {
                null
            } else {
                Instant.ofEpochMilli(behavior.startTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime()
            }
            val endLocal = behavior.endTime?.let {
                Instant.ofEpochMilli(it)
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime()
            }

            GridCellUiState(
                behaviorId = behavior.id,
                activityIconKey = activity?.iconKey,
                activityName = activity?.name,
                tags = tags.map { TagUiState(id = it.id, name = it.name, color = it.color, isActive = !it.isArchived) },
                status = behavior.status,
                isCurrent = isActive,
                wasPlanned = behavior.wasPlanned,
                achievementLevel = behavior.achievementLevel,
                estimatedDuration = behavior.estimatedDuration,
                actualDuration = behavior.actualDuration,
                durationMs = if (isActive && behavior.startTime > 0) {
                    currentTimeMs - behavior.startTime
                } else null,
                startTime = startLocal,
                endTime = endLocal,
                startEpochMs = if (isPending || behavior.startTime <= 0L) null else behavior.startTime,
                endEpochMs = behavior.endTime,
                note = behavior.note,
                pomodoroCount = behavior.pomodoroCount,
            )
        }
    }

    private fun buildAddCell(cells: List<GridCellUiState>, now: LocalTime): GridCellUiState {
        val lastEnd = cells.lastOrNull()?.endTime
        val idleStart = lastEnd ?: now
        val idleEnd = now

        return GridCellUiState(
            behaviorId = null,
            activityIconKey = null,
            activityName = null,
            tags = emptyList(),
            status = null,
            isCurrent = false,
            isAddPlaceholder = true,
            startTime = idleStart,
            endTime = idleEnd,
        )
    }

    private fun buildGridRows(
        allCells: List<GridCellUiState>,
        sortedBehaviors: List<Behavior>,
        now: LocalTime,
        gridColumns: Int = DEFAULT_GRID_COLUMNS,
        isCurrentDay: Boolean = true,
    ): Pair<List<GridRowUiState>, String?> {
        val rows = mutableListOf<GridRowUiState>()
        var currentRowId: String? = null

        allCells.chunked(gridColumns).forEachIndexed { rowIndex, rowCells ->
            val rowId = ROW_ID_FORMAT.format(rowIndex, rowCells.firstOrNull()?.behaviorId ?: "add")
            val hasCurrentInRow = isCurrentDay && rowCells.any { it.isCurrent }
            if (hasCurrentInRow) currentRowId = rowId

            val timeForRow = if (rowIndex < allCells.size / gridColumns) {
                val behavior = sortedBehaviors.getOrNull(rowIndex * gridColumns)
                if (behavior != null
                    && behavior.status != BehaviorNature.PENDING
                    && behavior.startTime > 0L
                ) {
                    Instant.ofEpochMilli(behavior.startTime)
                        .atZone(ZoneId.systemDefault())
                        .toLocalTime()
                } else {
                    now
                }
            } else {
                now
            }

            val paddedCells = rowCells.toMutableList()
            while (paddedCells.size < gridColumns) {
                paddedCells.add(
                    GridCellUiState(
                        behaviorId = null,
                        activityIconKey = null,
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

        return rows to currentRowId
    }

    private fun calculateCurrentBehavior(behaviors: List<Behavior>): Boolean {
        return behaviors.any { it.status == BehaviorNature.ACTIVE }
    }

    private fun calculateLastBehaviorEndTime(behaviors: List<Behavior>): LocalTime? {
        return behaviors
            .filter { it.endTime != null }
            .maxByOrNull { it.endTime ?: 0 }
            ?.let {
                Instant.ofEpochMilli(it.endTime!!)
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime()
            }
    }
}
