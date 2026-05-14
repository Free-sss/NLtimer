# 主页 4 布局加载历史数据 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**配套 spec**：[../specs/2026-05-13-home-past-data-loading-design.md](../specs/2026-05-13-home-past-data-loading-design.md)
**Supersedes plan**：[2026-05-04-home-cross-day-lazy-load.md](2026-05-04-home-cross-day-lazy-load.md)

**目标：** 主页 GRID / TIMELINE_REVERSE / LOG 三种布局支持向过去无限滚动浏览历史行为数据（按 7 天粒度自动加载，到最早一条记录为止）；MOMENT 保持只看今天。

**架构：** HomeViewModel 维护 `_loadedEarliest` 状态控制查询窗口下界，向上滚到边界时 `loadMore()` 减 7 天并通过 Flow 重订阅刷新数据。UI 层引入 `HomeListItem` sealed interface（给 LOG/TIMELINE_REVERSE）和 `GridDaySection`（给 GRID）承载跨日 + 日分隔条；MOMENT 单独消费 `momentCells`（仅今日）。FAB 拖拽菜单与历史浏览完全解耦。

**技术栈：** Kotlin, Jetpack Compose, Room, Hilt, Kotlin Coroutines / Flow, JUnit + kotlinx-coroutines-test

---

## 文件结构

### 新增

| 文件 | 职责 |
|------|------|
| `feature/home/src/main/java/com/nltimer/feature/home/model/HomeListItem.kt` | sealed interface：CellItem / DayDivider，LOG 和 TIMELINE_REVERSE 用 |
| `feature/home/src/main/java/com/nltimer/feature/home/model/GridDaySection.kt` | data class：按日分组（date + label + rows），GRID 用 |

### 修改

| 文件 | 改动职责 |
|------|---------|
| `core/data/src/main/java/com/nltimer/core/data/database/dao/BehaviorDao.kt` | 新增 `SELECT MIN(startTime)` 查询 |
| `core/data/src/main/java/com/nltimer/core/data/repository/BehaviorRepository.kt` | 接口新增 `getEarliestBehaviorDate()` |
| `core/data/src/main/java/com/nltimer/core/data/repository/impl/BehaviorRepositoryImpl.kt` | 实现 `getEarliestBehaviorDate()` |
| `feature/home/src/main/java/com/nltimer/feature/home/model/HomeUiState.kt` | 新增 `items / gridSections / momentCells / isLoadingMore / hasReachedEarliest` |
| `feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeUiStateBuilder.kt` | 支持多日 behaviors，输出 items / gridSections / momentCells |
| `feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeViewModel.kt` | 新增 `_loadedEarliest / _earliestRecord / _isLoadingMore`、`loadMore()`，改造 loadHomeBehaviors |
| `feature/home/src/main/java/com/nltimer/feature/home/ui/components/BehaviorLogView.kt` | 接收 items + 排序按 startEpochMs（修复跨日 bug）+ 渲染 DayDivider + 边界监听触发 loadMore |
| `feature/home/src/main/java/com/nltimer/feature/home/ui/components/TimelineReverseView.kt` | 同上 + Idle 间隙跨日处理 |
| `feature/home/src/main/java/com/nltimer/feature/home/ui/components/TimeAxisGrid.kt` | 接收 gridSections + section 渲染 + 顶部边界监听触发 loadMore |
| `feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt` | 透传 `viewModel::loadMore`；按布局分发 items / gridSections / momentCells |
| `feature/home/src/main/java/com/nltimer/feature/home/ui/HomeRoute.kt` | HomeScreen 调用处补 `onLoadMore = viewModel::loadMore` |

### 不动

- `feature/home/src/main/java/com/nltimer/feature/home/model/GridRowUiState.kt`（GRID 行结构不变）
- `feature/home/src/main/java/com/nltimer/feature/home/model/GridCellUiState.kt`
- `feature/home/src/main/java/com/nltimer/feature/home/ui/components/MomentView.kt`（签名不变，HomeScreen 切换数据源即可）

---

## 命令速查

构建模块：
```
.\gradlew.bat :core:data:compileDebugKotlin
.\gradlew.bat :feature:home:compileDebugKotlin
```

跑模块测试：
```
.\gradlew.bat :core:data:testDebugUnitTest --tests "ClassName.testName"
.\gradlew.bat :feature:home:testDebugUnitTest --tests "ClassName.testName"
```

---

## 任务 1：数据层新增 `getEarliestBehaviorDate()`

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/database/dao/BehaviorDao.kt`
- 修改：`core/data/src/main/java/com/nltimer/core/data/repository/BehaviorRepository.kt`
- 修改：`core/data/src/main/java/com/nltimer/core/data/repository/impl/BehaviorRepositoryImpl.kt`
- 修改测试 fakes：`core/data/src/test/java/com/nltimer/core/data/repository/BehaviorRepositoryImplTest.kt`、`core/data/src/test/java/com/nltimer/core/data/repository/ActivityManagementRepositoryImplTest.kt`、`feature/home/src/test/java/com/nltimer/feature/home/viewmodel/HomeViewModelTest.kt`（FakeBehaviorDao / FakeBehaviorRepository 补 stub）

- [ ] **步骤 1：在 `BehaviorDao.kt` 末尾（class 体内最后一个 `@Query` 之后）新增方法**

```kotlin
/** 查询数据库中最早一条 behavior 的 startTime（毫秒），无数据返回 null */
@Query("SELECT MIN(startTime) FROM behaviors WHERE startTime > 0")
suspend fun getEarliestStartTime(): Long?
```

- [ ] **步骤 2：在 `BehaviorRepository` 接口里新增方法（紧跟 `getMaxSequence()` 之后）**

```kotlin
suspend fun getEarliestBehaviorDate(): java.time.LocalDate?
```

- [ ] **步骤 3：在 `BehaviorRepositoryImpl` 实现该方法（紧跟 `getMaxSequence` 实现之后）**

```kotlin
override suspend fun getEarliestBehaviorDate(): java.time.LocalDate? {
    val earliestMs = behaviorDao.getEarliestStartTime() ?: return null
    return java.time.Instant.ofEpochMilli(earliestMs)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
}
```

- [ ] **步骤 4：补全所有 FakeBehaviorDao / FakeBehaviorRepository stub**

```kotlin
// FakeBehaviorDao（BehaviorRepositoryImplTest.kt、ActivityManagementRepositoryImplTest.kt）
override suspend fun getEarliestStartTime(): Long? =
    behaviors.filter { it.startTime > 0 }.minOfOrNull { it.startTime }

// FakeBehaviorRepository（HomeViewModelTest.kt）
override suspend fun getEarliestBehaviorDate(): java.time.LocalDate? = null
```

- [ ] **步骤 5：在 `BehaviorRepositoryImplTest.kt` 新增测试**

```kotlin
@Test
fun `getEarliestBehaviorDate returns earliest date among valid behaviors`() = runTest {
    val zone = java.time.ZoneId.systemDefault()
    val day1 = java.time.LocalDate.of(2026, 5, 10).atStartOfDay(zone).toInstant().toEpochMilli()
    val day2 = java.time.LocalDate.of(2026, 5, 12).atStartOfDay(zone).toInstant().toEpochMilli()
    fakeDao.behaviors.add(makeEntity(id = 1L, startTime = day2))
    fakeDao.behaviors.add(makeEntity(id = 2L, startTime = day1))

    val result = repository.getEarliestBehaviorDate()

    assertEquals(java.time.LocalDate.of(2026, 5, 10), result)
}

@Test
fun `getEarliestBehaviorDate returns null when no valid behavior exists`() = runTest {
    val result = repository.getEarliestBehaviorDate()
    assertNull(result)
}
```

如果该测试文件没有 `makeEntity` 辅助函数，按现有测试同款的 `BehaviorEntity(...)` 直接构造。

- [ ] **步骤 6：运行测试验证**

```
.\gradlew.bat :core:data:testDebugUnitTest --tests "com.nltimer.core.data.repository.BehaviorRepositoryImplTest.getEarliestBehaviorDate*"
```

预期：两个测试 PASS。

- [ ] **步骤 7：Commit**

```
git add core/data/src/main/java/com/nltimer/core/data/database/dao/BehaviorDao.kt core/data/src/main/java/com/nltimer/core/data/repository/BehaviorRepository.kt core/data/src/main/java/com/nltimer/core/data/repository/impl/BehaviorRepositoryImpl.kt core/data/src/test/java/com/nltimer/core/data/repository/BehaviorRepositoryImplTest.kt core/data/src/test/java/com/nltimer/core/data/repository/ActivityManagementRepositoryImplTest.kt feature/home/src/test/java/com/nltimer/feature/home/viewmodel/HomeViewModelTest.kt
git commit -m "feat(data): 新增 getEarliestBehaviorDate 查询最早行为日期"
```

---

## 任务 2：新建 HomeListItem 和 GridDaySection

**文件：**
- 创建：`feature/home/src/main/java/com/nltimer/feature/home/model/HomeListItem.kt`
- 创建：`feature/home/src/main/java/com/nltimer/feature/home/model/GridDaySection.kt`

- [ ] **步骤 1：创建 `HomeListItem.kt`**

```kotlin
package com.nltimer.feature.home.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate

/**
 * LOG / TIMELINE_REVERSE 布局的列表项 sealed interface。
 * CellItem 渲染单个行为，DayDivider 在跨日处展示日期分隔条。
 */
@Immutable
sealed interface HomeListItem {
    val key: String

    @Immutable
    data class CellItem(val cell: GridCellUiState) : HomeListItem {
        override val key: String = "cell-${cell.behaviorId ?: "null"}"
    }

    @Immutable
    data class DayDivider(val date: LocalDate, val label: String) : HomeListItem {
        override val key: String = "divider-${date}"
    }
}
```

- [ ] **步骤 2：创建 `GridDaySection.kt`**

```kotlin
package com.nltimer.feature.home.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate

/**
 * GRID 布局按日分组的 section。
 * 每个 section 内部仍是 24 小时网格的 rows，section 头部渲染日分隔条。
 */
@Immutable
data class GridDaySection(
    val date: LocalDate,
    val label: String,
    val rows: List<GridRowUiState>,
)
```

- [ ] **步骤 3：编译验证**

```
.\gradlew.bat :feature:home:compileDebugKotlin
```

预期：BUILD SUCCESSFUL。

- [ ] **步骤 4：Commit**

```
git add feature/home/src/main/java/com/nltimer/feature/home/model/HomeListItem.kt feature/home/src/main/java/com/nltimer/feature/home/model/GridDaySection.kt
git commit -m "feat(home): 新增 HomeListItem 和 GridDaySection 数据模型"
```

---

## 任务 3：HomeUiState 加新字段（保留 rows 用于渐进迁移）

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/model/HomeUiState.kt`

- [ ] **步骤 1：在 HomeUiState data class 字段列表中追加新字段**

把 `val rows: List<GridRowUiState> = emptyList(),` 这一行之后追加：

```kotlin
    val items: List<HomeListItem> = emptyList(),
    val gridSections: List<GridDaySection> = emptyList(),
    val momentCells: List<GridCellUiState> = emptyList(),
    val isLoadingMore: Boolean = false,
    val hasReachedEarliest: Boolean = false,
```

保留 `rows` 字段不动（任务 10 才删）。

- [ ] **步骤 2：编译验证**

```
.\gradlew.bat :feature:home:compileDebugKotlin
```

预期：BUILD SUCCESSFUL（新增字段都有默认值，不破坏现有调用）。

- [ ] **步骤 3：Commit**

```
git add feature/home/src/main/java/com/nltimer/feature/home/model/HomeUiState.kt
git commit -m "feat(home): HomeUiState 新增 items/gridSections/momentCells 等字段"
```

---

## 任务 4：HomeUiStateBuilder 支持多日并输出新字段

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeUiStateBuilder.kt`
- 创建/修改测试：`feature/home/src/test/java/com/nltimer/feature/home/viewmodel/HomeUiStateBuilderTest.kt`

- [ ] **步骤 1：编写失败的测试 `HomeUiStateBuilderTest.kt`**

```kotlin
package com.nltimer.feature.home.viewmodel

import com.nltimer.core.data.model.Behavior
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.feature.home.model.HomeListItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class HomeUiStateBuilderTest {

    private val builder = HomeUiStateBuilder()
    private val zone = ZoneId.systemDefault()
    private val today: LocalDate = LocalDate.of(2026, 5, 13)

    private fun epochMs(date: LocalDate, hour: Int) =
        date.atTime(hour, 0).atZone(zone).toInstant().toEpochMilli()

    private fun behavior(
        id: Long,
        date: LocalDate,
        hour: Int,
        status: BehaviorNature = BehaviorNature.COMPLETED,
    ) = Behavior(
        id = id,
        activityId = 1L,
        startTime = epochMs(date, hour),
        endTime = epochMs(date, hour + 1),
        status = status,
        sequence = 0,
        wasPlanned = false,
        achievementLevel = null,
        estimatedDuration = null,
        actualDuration = 3_600_000L,
        note = null,
        pomodoroCount = 0,
    )

    @Test
    fun `items contain DayDivider at every cross-day boundary`() {
        val yesterday = today.minusDays(1)
        val behaviors = listOf(
            behavior(1L, yesterday, 10),
            behavior(2L, yesterday, 14),
            behavior(3L, today, 9),
        )

        val state = builder.buildUiState(
            behaviors = behaviors,
            activities = emptyList(),
            tagsByBehaviorId = emptyMap(),
            now = LocalTime.of(10, 0),
            currentTimeMs = epochMs(today, 10),
            today = today,
        )

        val dividerDates = state.items.filterIsInstance<HomeListItem.DayDivider>().map { it.date }
        assertEquals(listOf(yesterday, today), dividerDates)
    }

    @Test
    fun `gridSections one per day with rows for that day`() {
        val yesterday = today.minusDays(1)
        val behaviors = listOf(
            behavior(1L, yesterday, 10),
            behavior(2L, today, 9),
        )

        val state = builder.buildUiState(
            behaviors = behaviors,
            activities = emptyList(),
            tagsByBehaviorId = emptyMap(),
            now = LocalTime.of(10, 0),
            currentTimeMs = epochMs(today, 10),
            today = today,
        )

        assertEquals(listOf(yesterday, today), state.gridSections.map { it.date })
        assertTrue(state.gridSections.all { it.rows.isNotEmpty() })
    }

    @Test
    fun `empty day is skipped from items and gridSections`() {
        val twoDaysAgo = today.minusDays(2)
        val behaviors = listOf(
            behavior(1L, twoDaysAgo, 10),
            behavior(2L, today, 9),
        )

        val state = builder.buildUiState(
            behaviors = behaviors,
            activities = emptyList(),
            tagsByBehaviorId = emptyMap(),
            now = LocalTime.of(10, 0),
            currentTimeMs = epochMs(today, 10),
            today = today,
        )

        val dividerDates = state.items.filterIsInstance<HomeListItem.DayDivider>().map { it.date }
        assertEquals(listOf(twoDaysAgo, today), dividerDates)
        assertEquals(listOf(twoDaysAgo, today), state.gridSections.map { it.date })
    }

    @Test
    fun `momentCells contain only today behaviors`() {
        val yesterday = today.minusDays(1)
        val behaviors = listOf(
            behavior(1L, yesterday, 10),
            behavior(2L, today, 9),
        )

        val state = builder.buildUiState(
            behaviors = behaviors,
            activities = emptyList(),
            tagsByBehaviorId = emptyMap(),
            now = LocalTime.of(10, 0),
            currentTimeMs = epochMs(today, 10),
            today = today,
        )

        val ids = state.momentCells.mapNotNull { it.behaviorId }
        assertEquals(listOf(2L), ids)
    }
}
```

- [ ] **步骤 2：运行测试验证失败**

```
.\gradlew.bat :feature:home:testDebugUnitTest --tests "com.nltimer.feature.home.viewmodel.HomeUiStateBuilderTest"
```

预期：FAIL（编译失败，`buildUiState` 不接受 `today` 参数）。

- [ ] **步骤 3：改造 `HomeUiStateBuilder.buildUiState`**

替换整个 `buildUiState` 方法 + 修改 `buildGridRows` 加 `isCurrentDay` 参数 + 新增 helper 方法。完整 patch：

```kotlin
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
    val days = java.time.temporal.ChronoUnit.DAYS.between(date, today)
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
```

修改 `buildGridRows` 签名为：

```kotlin
private fun buildGridRows(
    allCells: List<GridCellUiState>,
    sortedBehaviors: List<Behavior>,
    now: LocalTime,
    gridColumns: Int = DEFAULT_GRID_COLUMNS,
    isCurrentDay: Boolean = true,
): Pair<List<GridRowUiState>, String?> {
```

把方法体中 `val hasCurrentInRow = rowCells.any { it.isCurrent }` 改为：

```kotlin
val hasCurrentInRow = isCurrentDay && rowCells.any { it.isCurrent }
```

文件顶部 imports 追加：

```kotlin
import com.nltimer.feature.home.model.GridDaySection
import com.nltimer.feature.home.model.HomeListItem
import java.time.LocalDate
import java.time.temporal.ChronoUnit
```

- [ ] **步骤 4：更新 HomeViewModel 调用点传入 today**

打开 `HomeViewModel.kt`，找到 `buildUiState` 调用（约第 155-162 行），加 `today = today` 参数：

```kotlin
return uiStateBuilder.buildUiState(
    behaviors = behaviors,
    activities = _activities.value,
    tagsByBehaviorId = tagsByBehaviorId,
    now = now,
    currentTimeMs = clockService.currentTimeMillis(),
    today = today,
    gridColumns = homeLayoutConfig.value.grid.columns,
)
```

`today` 已经在 [HomeViewModel.kt:100](feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeViewModel.kt#L100) 定义为 `private val today = LocalDate.now()`。

- [ ] **步骤 5：运行测试验证通过**

```
.\gradlew.bat :feature:home:testDebugUnitTest --tests "com.nltimer.feature.home.viewmodel.HomeUiStateBuilderTest"
```

预期：4 个测试 PASS。

- [ ] **步骤 6：跑原 HomeViewModelTest 验证未破坏现有行为**

```
.\gradlew.bat :feature:home:testDebugUnitTest --tests "com.nltimer.feature.home.viewmodel.HomeViewModelTest"
```

预期：原有测试全部 PASS。

- [ ] **步骤 7：Commit**

```
git add feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeUiStateBuilder.kt feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeViewModel.kt feature/home/src/test/java/com/nltimer/feature/home/viewmodel/HomeUiStateBuilderTest.kt
git commit -m "feat(home): UiStateBuilder 支持多日 behaviors，输出 items/gridSections/momentCells"
```

---

## 任务 5：HomeViewModel 加 loadMore 与无限滚动数据流

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeViewModel.kt`
- 修改测试：`feature/home/src/test/java/com/nltimer/feature/home/viewmodel/HomeViewModelTest.kt`

- [ ] **步骤 1：在 HomeViewModelTest 内现有 FakeBehaviorRepository 上加字段 + override**

```kotlin
var earliestDate: java.time.LocalDate? = null
override suspend fun getEarliestBehaviorDate(): java.time.LocalDate? = earliestDate
```

- [ ] **步骤 2：编写失败的测试**

```kotlin
@Test
fun `loadMore is no-op when earliestRecord is null`() = runTest {
    val viewModel = createViewModel()
    advanceUntilIdle()
    val before = viewModel.uiState.value.items.size

    viewModel.loadMore()
    advanceUntilIdle()

    assertEquals(before, viewModel.uiState.value.items.size)
}

@Test
fun `loadMore stops at earliestRecord boundary`() = runTest {
    val earliest = java.time.LocalDate.now().minusDays(3)
    fakeBehaviorRepository.earliestDate = earliest
    val viewModel = createViewModel()
    advanceUntilIdle()

    repeat(5) {
        viewModel.loadMore()
        advanceUntilIdle()
    }

    assertTrue(viewModel.uiState.value.hasReachedEarliest)
}
```

- [ ] **步骤 3：运行测试验证失败**

```
.\gradlew.bat :feature:home:testDebugUnitTest --tests "com.nltimer.feature.home.viewmodel.HomeViewModelTest.loadMore*"
```

预期：FAIL（`loadMore` 方法不存在）。

- [ ] **步骤 4：改造 `HomeViewModel.kt`**

文件顶部 imports 追加：

```kotlin
import java.time.LocalDate
```

类体内紧跟 `private val today = LocalDate.now()` 之后新增：

```kotlin
private val _loadedEarliest = MutableStateFlow(today)
private val _earliestRecord = MutableStateFlow<LocalDate?>(null)
private val _isLoadingMore = MutableStateFlow(false)
```

替换 `loadHomeBehaviors` 整个方法：

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
private fun loadHomeBehaviors() {
    viewModelScope.launch {
        _earliestRecord.value = try {
            behaviorRepository.getEarliestBehaviorDate()
        } catch (_: Exception) {
            null
        }
    }
    viewModelScope.launch {
        combine(
            _loadedEarliest.flatMapLatest { earliest ->
                behaviorRepository.getHomeBehaviors(
                    earliest.startOfDayMillis(),
                    today.endOfDayMillis()
                )
            },
            homeLayoutConfig,
            _isLoadingMore,
            _loadedEarliest,
            _earliestRecord,
        ) { behaviors, _, loadingMore, loadedEarliest, earliestRecord ->
            BehaviorsSnapshot(behaviors, loadingMore, loadedEarliest, earliestRecord)
        }.collect { snapshot ->
            val state = buildUiState(snapshot.behaviors)
            val reached = snapshot.earliestRecord?.let { !snapshot.loadedEarliest.isAfter(it) } ?: false
            _uiState.update {
                state.copy(
                    isLoadingMore = snapshot.isLoadingMore,
                    hasReachedEarliest = reached,
                )
            }
            _isLoadingMore.value = false
        }
    }
}

private data class BehaviorsSnapshot(
    val behaviors: List<com.nltimer.core.data.model.Behavior>,
    val isLoadingMore: Boolean,
    val loadedEarliest: LocalDate,
    val earliestRecord: LocalDate?,
)
```

类体末尾新增 `loadMore`（放在 `onHomeLayoutConfigChange` 之后）：

```kotlin
fun loadMore() {
    if (_isLoadingMore.value) return
    val current = _loadedEarliest.value
    val candidate = current.minusDays(7)
    val cap = _earliestRecord.value ?: return
    val target = if (candidate.isBefore(cap)) cap else candidate
    if (!target.isBefore(current)) return
    _isLoadingMore.value = true
    _loadedEarliest.value = target
}
```

- [ ] **步骤 5：运行测试验证通过**

```
.\gradlew.bat :feature:home:testDebugUnitTest --tests "com.nltimer.feature.home.viewmodel.HomeViewModelTest"
```

预期：新增 + 原有测试全部 PASS。

- [ ] **步骤 6：Commit**

```
git add feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeViewModel.kt feature/home/src/test/java/com/nltimer/feature/home/viewmodel/HomeViewModelTest.kt
git commit -m "feat(home): HomeViewModel 支持向过去无限滚动加载（7 天/次）"
```

---

## 任务 6：BehaviorLogView 改造（消费 items + 修正跨日排序 + 边界监听）

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/components/BehaviorLogView.kt`
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt`

- [ ] **步骤 1：替换 `BehaviorLogView.kt` 整个文件**

```kotlin
package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.LogLayoutStyle
import com.nltimer.core.data.util.hhmmFormatter
import com.nltimer.feature.home.model.GridCellUiState
import com.nltimer.feature.home.model.HomeListItem
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun BehaviorLogView(
    items: List<HomeListItem>,
    onCellLongClick: (GridCellUiState) -> Unit = {},
    onLoadMore: () -> Unit = {},
    isLoadingMore: Boolean = false,
    hasReachedEarliest: Boolean = false,
    modifier: Modifier = Modifier,
    logStyle: LogLayoutStyle = LogLayoutStyle(),
) {
    val timeFormatter = hhmmFormatter
    val listState = rememberLazyListState()
    var detailCell by remember { mutableStateOf<GridCellUiState?>(null) }

    val displayItems = remember(items) { reverseGroupedItems(items) }

    LaunchedEffect(displayItems, hasReachedEarliest) {
        if (hasReachedEarliest) return@LaunchedEffect
        snapshotFlow {
            val total = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total to lastVisible
        }.distinctUntilChanged()
            .filter { (total, last) -> total > 0 && last >= total - 5 }
            .collect { onLoadMore() }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 180.dp),
        ) {
            if (displayItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "暂无行为记录",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(items = displayItems, key = { it.key }) { item ->
                    when (item) {
                        is HomeListItem.DayDivider -> DayDividerRow(label = item.label)
                        is HomeListItem.CellItem -> BehaviorLogCard(
                            behavior = item.cell,
                            timeFormatter = timeFormatter,
                            onClick = { detailCell = item.cell },
                            onLongClick = { onCellLongClick(item.cell) },
                            logStyle = logStyle,
                        )
                    }
                }
                if (isLoadingMore) item { LoadingMoreIndicator() }
                if (hasReachedEarliest) item { ReachedEarliestIndicator() }
            }
        }
    }

    detailCell?.let { cell ->
        BehaviorDetailDialog(cell = cell, onDismiss = { detailCell = null })
    }
}

private fun reverseGroupedItems(items: List<HomeListItem>): List<HomeListItem> {
    val groups = mutableListOf<Pair<HomeListItem.DayDivider, MutableList<HomeListItem.CellItem>>>()
    items.forEach { item ->
        when (item) {
            is HomeListItem.DayDivider -> groups.add(item to mutableListOf())
            is HomeListItem.CellItem -> groups.lastOrNull()?.second?.add(item)
        }
    }
    return groups.asReversed().flatMap { (divider, cells) ->
        listOf<HomeListItem>(divider) + cells.asReversed()
    }
}

@Composable
private fun DayDividerRow(label: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LoadingMoreIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
        )
    }
}

@Composable
private fun ReachedEarliestIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "已到最早一条记录",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
```

- [ ] **步骤 2：在 `HomeScreen.kt` 修改 `LogContent`**

```kotlin
@Composable
private fun LogContent(
    uiState: HomeUiState,
    onCellLongClick: (GridCellUiState) -> Unit,
    onLoadMore: () -> Unit,
    logStyle: LogLayoutStyle = LogLayoutStyle(),
    modifier: Modifier = Modifier,
) {
    BehaviorLogView(
        items = uiState.items,
        onCellLongClick = onCellLongClick,
        onLoadMore = onLoadMore,
        isLoadingMore = uiState.isLoadingMore,
        hasReachedEarliest = uiState.hasReachedEarliest,
        logStyle = logStyle,
        modifier = modifier,
    )
}
```

`HomeLayoutContent` 函数签名加 `onLoadMore: () -> Unit` 参数；其内对 `LogContent(...)` 调用处加 `onLoadMore = onLoadMore`；其它两个布局调用先用 `onLoadMore = {}` 占位（任务 7/8 接）。`HomeScreen` 函数签名同样加 `onLoadMore: () -> Unit = {}` 参数，并把 `HomeLayoutContent(...)` 调用处加 `onLoadMore = onLoadMore`。

- [ ] **步骤 3：编译验证**

```
.\gradlew.bat :feature:home:compileDebugKotlin
```

预期：BUILD SUCCESSFUL。

- [ ] **步骤 4：Commit**

```
git add feature/home/src/main/java/com/nltimer/feature/home/ui/components/BehaviorLogView.kt feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt
git commit -m "feat(home): LOG 布局支持加载历史数据，修正跨日 LocalTime 排序 bug"
```

---

## 任务 7：TimelineReverseView 改造（消费 items + Idle 同日处理 + 边界监听）

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/components/TimelineReverseView.kt`
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt`

- [ ] **步骤 1：替换 `TimelineReverseView` 中 `@Composable fun TimelineReverseView(...)` 函数体**

签名替换为：

```kotlin
@Composable
fun TimelineReverseView(
    items: List<HomeListItem>,
    onAddClick: (idleStart: LocalTime?, idleEnd: LocalTime?) -> Unit,
    onCellLongClick: (GridCellUiState) -> Unit = {},
    onLoadMore: () -> Unit = {},
    isLoadingMore: Boolean = false,
    hasReachedEarliest: Boolean = false,
    modifier: Modifier = Modifier,
)
```

函数体替换为：

```kotlin
{
    val timeFormatter = hhmmFormatter
    val listState = rememberLazyListState()
    var detailCell by remember { mutableStateOf<GridCellUiState?>(null) }

    val timelineItems = remember(items) { buildTimelineItemsReversed(items) }

    LaunchedEffect(timelineItems, hasReachedEarliest) {
        if (hasReachedEarliest) return@LaunchedEffect
        snapshotFlow {
            val total = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total to lastVisible
        }.distinctUntilChanged()
            .filter { (total, last) -> total > 0 && last >= total - 5 }
            .collect { onLoadMore() }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 180.dp),
        ) {
            items(items = timelineItems, key = { it.key }) { item ->
                when (item) {
                    is TimelineDisplayItem.Divider -> DayDividerRow(label = item.label)
                    is TimelineDisplayItem.BehaviorRow -> TimelineBehaviorItem(
                        behavior = item.cell,
                        timeFormatter = timeFormatter,
                        onClick = { detailCell = item.cell },
                        onLongClick = { onCellLongClick(item.cell) },
                    )
                    is TimelineDisplayItem.Idle -> TimelineIdleItem(
                        start = item.start,
                        end = item.end,
                        timeFormatter = timeFormatter,
                        onAddClick = { onAddClick(item.start, item.end) },
                    )
                }
            }
            if (isLoadingMore) item { LoadingMoreIndicator() }
            if (hasReachedEarliest) item { ReachedEarliestIndicator() }
        }
    }

    detailCell?.let { cell ->
        BehaviorDetailDialog(cell = cell, onDismiss = { detailCell = null })
    }
}
```

文件顶部 imports 替换 `TimelineItemData` 相关引用，追加：

```kotlin
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.size
import com.nltimer.feature.home.model.HomeListItem
import java.time.LocalDate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
```

文件底部替换 `sealed class TimelineItemData ...` 整段为：

```kotlin
private sealed class TimelineDisplayItem(val key: String) {
    class Divider(val date: LocalDate, val label: String) : TimelineDisplayItem("divider-$date")
    class BehaviorRow(val cell: GridCellUiState) : TimelineDisplayItem("behavior-${cell.behaviorId}")
    class Idle(val start: LocalTime, val end: LocalTime) : TimelineDisplayItem("idle-$start-$end")
}

private fun buildTimelineItemsReversed(items: List<HomeListItem>): List<TimelineDisplayItem> {
    data class DayBucket(val divider: HomeListItem.DayDivider, val cells: MutableList<GridCellUiState>)
    val buckets = mutableListOf<DayBucket>()
    items.forEach { item ->
        when (item) {
            is HomeListItem.DayDivider -> buckets.add(DayBucket(item, mutableListOf()))
            is HomeListItem.CellItem -> buckets.lastOrNull()?.cells?.add(item.cell)
        }
    }

    val result = mutableListOf<TimelineDisplayItem>()
    buckets.asReversed().forEach { bucket ->
        val sortedAsc = bucket.cells.sortedBy { it.startTime?.toSecondOfDay() ?: 0 }
        if (sortedAsc.isEmpty()) return@forEach
        result.add(TimelineDisplayItem.Divider(bucket.divider.date, bucket.divider.label))
        for (i in sortedAsc.indices.reversed()) {
            val cell = sortedAsc[i]
            result.add(TimelineDisplayItem.BehaviorRow(cell))
            if (i > 0) {
                val prevEnd = sortedAsc[i - 1].endTime
                val currentStart = cell.startTime
                if (prevEnd != null && currentStart != null && currentStart.isAfter(prevEnd)) {
                    val gap = java.time.Duration.between(prevEnd, currentStart)
                    if (gap.toMinutes() >= 1) {
                        result.add(TimelineDisplayItem.Idle(prevEnd, currentStart))
                    }
                }
            }
        }
    }
    return result
}

@Composable
private fun DayDividerRow(label: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LoadingMoreIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
    }
}

@Composable
private fun ReachedEarliestIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "已到最早一条记录",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
```

**YAGNI**：原代码 lines 74-84 的"今日最后一条结束 → 现在"自动 Idle 间隙暂时移除（属于今日特定 UX 优化，本任务先做最小可工作版本；如后续手动测试发现 UX 问题，再单独 PR 加回）。

- [ ] **步骤 2：在 `HomeScreen.kt` 修改 `TimelineReverseContent`**

```kotlin
@Composable
private fun TimelineReverseContent(
    uiState: HomeUiState,
    onEmptyCellClick: (idleStart: LocalTime?, idleEnd: LocalTime?) -> Unit,
    onCellLongClick: (GridCellUiState) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TimelineReverseView(
        items = uiState.items,
        onAddClick = onEmptyCellClick,
        onCellLongClick = onCellLongClick,
        onLoadMore = onLoadMore,
        isLoadingMore = uiState.isLoadingMore,
        hasReachedEarliest = uiState.hasReachedEarliest,
        modifier = modifier,
    )
}
```

`HomeLayoutContent` 内 `TimelineReverseContent(...)` 调用处把占位的 `onLoadMore = {}` 改为 `onLoadMore = onLoadMore`。

- [ ] **步骤 3：编译验证**

```
.\gradlew.bat :feature:home:compileDebugKotlin
```

预期：BUILD SUCCESSFUL。

- [ ] **步骤 4：Commit**

```
git add feature/home/src/main/java/com/nltimer/feature/home/ui/components/TimelineReverseView.kt feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt
git commit -m "feat(home): TIMELINE_REVERSE 支持加载历史，Idle 间隙仅在同日内计算"
```

---

## 任务 8：TimeAxisGrid 改造（按 section 渲染 + 顶部边界监听）

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/components/TimeAxisGrid.kt`
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt`

- [ ] **步骤 1：替换 `TimeAxisGrid.kt` 整个文件**

```kotlin
package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.GridLayoutStyle
import com.nltimer.core.designsystem.theme.TimeLabelConfig
import com.nltimer.feature.home.model.GridCellUiState
import com.nltimer.feature.home.model.GridDaySection
import java.time.LocalTime
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimeAxisGrid(
    modifier: Modifier = Modifier,
    sections: List<GridDaySection>,
    onEmptyCellClick: (idleStart: LocalTime?, idleEnd: LocalTime?) -> Unit,
    onCellLongClick: (GridCellUiState) -> Unit = {},
    onLoadMore: () -> Unit = {},
    isLoadingMore: Boolean = false,
    hasReachedEarliest: Boolean = false,
    currentHour: Int = 0,
    showTimeSideBar: Boolean = false,
    timeLabelConfig: TimeLabelConfig = TimeLabelConfig(),
    onTimeLabelSettingsClick: () -> Unit = {},
    gridStyle: GridLayoutStyle = GridLayoutStyle(),
) {
    val listState = rememberLazyListState()

    LaunchedEffect(currentHour, sections) {
        val todaySection = sections.lastOrNull() ?: return@LaunchedEffect
        val targetIndex = todaySection.rows.indexOfFirst { it.startTime.hour >= currentHour }
        if (targetIndex >= 0) {
            val precedingItems = sections.dropLast(1).sumOf { 1 + it.rows.size }
            val absoluteIndex = precedingItems + 1 + targetIndex
            listState.animateScrollToItem(absoluteIndex)
        }
    }

    LaunchedEffect(sections, hasReachedEarliest) {
        if (hasReachedEarliest) return@LaunchedEffect
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .filter { it <= 5 }
            .collect { onLoadMore() }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.padding(start = 10.dp, end = if (showTimeSideBar) 0.dp else 10.dp, top = 0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(bottom = 180.dp),
    ) {
        if (isLoadingMore) item("loading-top") { LoadingMoreIndicator() }
        if (hasReachedEarliest) item("reached-earliest") { ReachedEarliestIndicator() }
        sections.forEach { section ->
            stickyHeader(key = "header-${section.date}") {
                DayDividerRow(label = section.label)
            }
            items(items = section.rows, key = { it.rowId }) { row ->
                GridRow(
                    row = row,
                    onEmptyCellClick = onEmptyCellClick,
                    onCellLongClick = onCellLongClick,
                    timeLabelConfig = timeLabelConfig,
                    gridStyle = gridStyle,
                )
            }
        }
    }
}

@Composable
private fun DayDividerRow(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LoadingMoreIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
    }
}

@Composable
private fun ReachedEarliestIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "已到最早一条记录",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
```

- [ ] **步骤 2：在 `HomeScreen.kt` 修改 `GridContent`**

```kotlin
@Composable
private fun GridContent(
    uiState: HomeUiState,
    onEmptyCellClick: (idleStart: LocalTime?, idleEnd: LocalTime?) -> Unit,
    onCellLongClick: (GridCellUiState) -> Unit,
    onHourClick: (Int) -> Unit,
    onLoadMore: () -> Unit,
    timeLabelConfig: TimeLabelConfig,
    onTimeLabelSettingsClick: () -> Unit,
    gridStyle: GridLayoutStyle = GridLayoutStyle(),
    modifier: Modifier = Modifier,
) {
    val showSideBar = LocalTheme.current.showTimeSideBar
    Row(modifier = modifier) {
        TimeAxisGrid(
            sections = uiState.gridSections,
            onEmptyCellClick = onEmptyCellClick,
            onCellLongClick = onCellLongClick,
            onLoadMore = onLoadMore,
            isLoadingMore = uiState.isLoadingMore,
            hasReachedEarliest = uiState.hasReachedEarliest,
            currentHour = uiState.selectedTimeHour,
            showTimeSideBar = showSideBar,
            timeLabelConfig = timeLabelConfig,
            onTimeLabelSettingsClick = onTimeLabelSettingsClick,
            gridStyle = gridStyle,
            modifier = Modifier.weight(1f),
        )
        if (showSideBar) {
            val activeHours by remember {
                derivedStateOf {
                    uiState.gridSections.lastOrNull()?.rows.orEmpty()
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
```

`HomeLayoutContent` 内 `GridContent(...)` 调用处把 `onLoadMore = {}` 改为 `onLoadMore = onLoadMore`。

- [ ] **步骤 3：编译验证**

```
.\gradlew.bat :feature:home:compileDebugKotlin
```

预期：BUILD SUCCESSFUL。

- [ ] **步骤 4：Commit**

```
git add feature/home/src/main/java/com/nltimer/feature/home/ui/components/TimeAxisGrid.kt feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt
git commit -m "feat(home): GRID 按 section 渲染，向上滚动到顶部加载历史"
```

---

## 任务 9：MOMENT 切换为 momentCells + HomeScreen 整体接线

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt`
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeRoute.kt`

- [ ] **步骤 1：修改 `MomentContent`**

```kotlin
@Composable
private fun MomentContent(
    uiState: HomeUiState,
    activeBehaviorId: Long?,
    onEmptyCellClick: (idleStart: LocalTime?, idleEnd: LocalTime?) -> Unit,
    onCellLongClick: (GridCellUiState) -> Unit,
    onCompleteBehavior: (Long) -> Unit,
    onStartNextPending: () -> Unit,
    onStartBehavior: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    MomentView(
        cells = uiState.momentCells,
        hasActiveBehavior = uiState.hasActiveBehavior,
        activeBehaviorId = activeBehaviorId,
        onCompleteBehavior = onCompleteBehavior,
        onStartNextPending = onStartNextPending,
        onStartBehavior = onStartBehavior,
        onEmptyCellClick = onEmptyCellClick,
        onCellLongClick = onCellLongClick,
        modifier = modifier,
    )
}
```

- [ ] **步骤 2：HomeRoute 接线**

打开 `feature/home/src/main/java/com/nltimer/feature/home/ui/HomeRoute.kt`，找到 `HomeScreen(...)` 调用，在最后一个命名参数后追加：

```kotlin
onLoadMore = viewModel::loadMore,
```

- [ ] **步骤 3：编译 + 模块测试**

```
.\gradlew.bat :feature:home:compileDebugKotlin
.\gradlew.bat :feature:home:testDebugUnitTest
```

预期：BUILD SUCCESSFUL，测试全 PASS。

- [ ] **步骤 4：安装到设备进行手动验证**

```
.\gradlew.bat installDebug
```

人工测试场景：
1. 启动 App，主页正常显示今日数据
2. LOG 布局：滚到底，看到"昨天 5/12"分隔条 + 昨天数据；继续滚加载更早 7 天
3. TIMELINE_REVERSE：滚到底，跨日分隔正常出现；Idle 间隙仅在同日内显示
4. GRID：从底部滚到顶部，到顶后再上滑，加载昨日网格 + sticky 日分隔条
5. MOMENT：切到布局，只显示今日 cells；不论上下滚都不触发加载
6. 滚到最早一条记录后显示"已到最早一条记录"，且 loadMore 无效
7. FAB 拖拽菜单：在任何滚动位置都是今日动作语义
8. 长按昨日 cell：EditSheet 弹出，可编辑

- [ ] **步骤 5：Commit**

```
git add feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt feature/home/src/main/java/com/nltimer/feature/home/ui/HomeRoute.kt
git commit -m "feat(home): 整体接线 onLoadMore，MOMENT 改用 momentCells 数据源"
```

---

## 任务 10：清理 — 删除 HomeUiState.rows 冗余字段

**前提：** 任务 6/7/8/9 完成后，`rows` 字段已无生产代码消费者。

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/model/HomeUiState.kt`
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeUiStateBuilder.kt`
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt`（Preview）

- [ ] **步骤 1：Grep 确认 rows 字段无生产引用**

用 Grep 工具搜索 `uiState.rows` 在 `feature/home/src/main/**`。预期：除 Preview 之外无匹配。

- [ ] **步骤 2：从 HomeUiState 删除 `rows` 字段（及 currentRowId 如无消费者）**

把 `val rows: List<GridRowUiState> = emptyList(),` 这一行删除。Grep 验证 `currentRowId` 是否还有消费者；若无也删除。

- [ ] **步骤 3：从 HomeUiStateBuilder 删除 rows 构造**

`buildUiState` 中：
- 删除 `val (todayRows, currentRowId) = buildGridRows(...)` 行
- 返回 `HomeUiState(...)` 时去掉 `rows = todayRows`、`currentRowId = currentRowId`

`buildEmptyState` 同步：
- 删除 `rows = listOf(row)`、`currentRowId = row.rowId`

- [ ] **步骤 4：修复 HomeScreen.kt 的 Preview**

把 `HomeScreenPreview` 里 `rows = listOf(...)` 这一段替换为：

```kotlin
val sampleSection = GridDaySection(
    date = java.time.LocalDate.of(2026, 5, 13),
    label = "今天 5/13",
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
                    isCurrent = true,
                )
            )
        )
    ),
)
val sampleUiState = HomeUiState(
    isLoading = false,
    gridSections = listOf(sampleSection),
    selectedTimeHour = 9,
)
```

并在文件顶部 imports 追加：

```kotlin
import com.nltimer.feature.home.model.GridDaySection
```

- [ ] **步骤 5：编译 + 测试**

```
.\gradlew.bat :feature:home:compileDebugKotlin
.\gradlew.bat :feature:home:testDebugUnitTest
```

预期：BUILD SUCCESSFUL，测试全 PASS。

- [ ] **步骤 6：Commit**

```
git add feature/home/src/main/java/com/nltimer/feature/home/model/HomeUiState.kt feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeUiStateBuilder.kt feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt
git commit -m "refactor(home): 删除 HomeUiState.rows 冗余字段，gridSections 完全取代"
```

---

## 验收清单

实施完毕后，应满足下列每项：

- [ ] LOG 滚到底自动加载昨日及之前 7 天数据，DayDivider 正确出现
- [ ] TIMELINE_REVERSE 同上，Idle 间隙不跨日
- [ ] GRID 向上滚到顶自动加载，日分隔条 sticky
- [ ] MOMENT 切到布局后只显示今日数据，向下滚不会触发加载
- [ ] 滚到最早一条记录的日期后，显示"已到最早一条记录"，再触发 loadMore 不会有动作
- [ ] FAB 拖拽菜单语义在任何滚动位置都保持"今日动作"
- [ ] 长按历史 cell → EditSheet 弹出，可编辑该历史行为
- [ ] `gradlew.bat :feature:home:testDebugUnitTest` 全部 PASS
- [ ] `gradlew.bat :core:data:testDebugUnitTest` 全部 PASS
