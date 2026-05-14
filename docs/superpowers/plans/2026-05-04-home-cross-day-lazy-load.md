# 主页跨天懒加载 实现计划

> **[SUPERSEDED]** 本计划已被 [2026-05-13-home-past-data-loading-plan.md](2026-05-13-home-past-data-loading-plan.md) 取代。配套 spec [2026-05-04-home-cross-day-lazy-load-design.md](../specs/2026-05-04-home-cross-day-lazy-load-design.md) 也已 SUPERSEDED。本计划保留作为推理过程的历史参考。

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 主页支持向上滚动懒加载历史天数的行为数据，按天分组显示吸顶日期头

**架构：** ViewModel 维护 `List<DaySection>` 替代原 `rows`，滚动到顶部时追加前一天数据。三种布局模式（Grid / Timeline / Log）统一改为按 DaySection 分组渲染，使用 `stickyHeader` 吸顶。

**技术栈：** Kotlin, Jetpack Compose, Room, Flow

---

## 文件结构

| 操作 | 文件 | 职责 |
|------|------|------|
| 修改 | `feature/home/.../model/HomeUiState.kt` | 新增 DaySection，替换 rows 为 daySections |
| 修改 | `feature/home/.../viewmodel/HomeViewModel.kt` | 按天加载逻辑、loadPreviousDay、buildDaySection |
| 修改 | `feature/home/.../ui/HomeScreen.kt` | 传递 daySections、onLoadMore 回调 |
| 修改 | `feature/home/.../ui/components/TimeAxisGrid.kt` | 按 DaySection 分组渲染 + stickyHeader + 滚动检测 |
| 修改 | `feature/home/.../ui/components/TimelineReverseView.kt` | 按 DaySection 分组渲染 + stickyHeader + 滚动检测 |
| 修改 | `feature/home/.../ui/components/BehaviorLogView.kt` | 按 DaySection 分组渲染 + stickyHeader + 滚动检测 |
| 新建 | `feature/home/.../ui/components/DateHeader.kt` | 吸顶日期头组件（三种布局共用） |

---

### 任务 1：数据模型 — 新增 DaySection，修改 HomeUiState

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/model/HomeUiState.kt`

- [ ] **步骤 1：在 HomeUiState.kt 中新增 DaySection data class 并修改 HomeUiState**

在 `HomeUiState.kt` 文件中，在 `HomeUiState` 之前添加 `DaySection`，然后修改 `HomeUiState`：

```kotlin
package com.nltimer.feature.home.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate

@Immutable
data class DaySection(
    val date: LocalDate,
    val rows: List<GridRowUiState>,
)

@Immutable
data class HomeUiState(
    val daySections: List<DaySection> = emptyList(),
    val earliestLoadedDate: LocalDate? = null,
    val isLoadingMore: Boolean = false,
    val currentRowId: String? = null,
    val isAddSheetVisible: Boolean = false,
    val selectedTimeHour: Int = 0,
    val isLoading: Boolean = true,
    val isIdleMode: Boolean = false,
    val hasActiveBehavior: Boolean = false,
    val isDetailSheetVisible: Boolean = false,
    val detailBehavior: BehaviorDetailUiState? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)
```

- [ ] **步骤 2：构建验证**

运行：`.\gradlew.bat :feature:home:compileDebugKotlin`
预期：编译失败（因为 ViewModel 和 UI 层仍在引用 `rows`），这是预期的，后续任务修复

---

### 任务 2：ViewModel — 按天加载逻辑

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeViewModel.kt`

- [ ] **步骤 1：修改 loadHomeBehaviors 为按天构建 DaySection**

将 `loadHomeBehaviors()` 中的 `buildUiState(behaviors)` 调用改为 `buildDaySection(today, behaviors)`，结果放入 `daySections`：

```kotlin
private fun loadHomeBehaviors() {
    viewModelScope.launch {
        val dayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val dayEnd = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        behaviorRepository.getHomeBehaviors(dayStart, dayEnd)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
            .collect { behaviors ->
                val section = buildDaySection(today, behaviors)
                _uiState.update { state ->
                    val existingOtherDays = state.daySections.filter { it.date != today }
                    state.copy(
                        daySections = (existingOtherDays + section).sortedByDescending { it.date },
                        earliestLoadedDate = state.earliestLoadedDate ?: today,
                        isLoading = false,
                        hasActiveBehavior = behaviors.any { it.status == BehaviorNature.ACTIVE },
                    )
                }
            }
    }
}
```

- [ ] **步骤 2：将 buildUiState 重构为 buildDaySection**

将原 `buildUiState` 方法签名改为 `buildDaySection(date: LocalDate, behaviors: List<Behavior>): DaySection`，内部逻辑不变，只是返回 `DaySection(date = date, rows = rows)` 而非 `HomeUiState`。方法末尾改为：

```kotlin
return DaySection(
    date = date,
    rows = rows,
)
```

删除原方法中构建 `HomeUiState` 的部分（`currentRowId`、`isLoading`、`selectedTimeHour`、`hasActiveBehavior`），这些字段已在 `loadHomeBehaviors` 的 collect 块中处理。

- [ ] **步骤 3：新增 loadPreviousDay 方法**

```kotlin
fun loadPreviousDay() {
    val currentEarliest = _uiState.value.earliestLoadedDate ?: return
    if (_uiState.value.isLoadingMore) return

    val previousDate = currentEarliest.minusDays(1)
    _uiState.update { it.copy(isLoadingMore = true) }

    viewModelScope.launch {
        val dayStart = previousDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val dayEnd = previousDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val behaviors = behaviorRepository.getByDayRange(dayStart, dayEnd).firstOrNull() ?: emptyList()
        val section = buildDaySection(previousDate, behaviors)

        _uiState.update { state ->
            state.copy(
                daySections = (state.daySections + section).sortedByDescending { it.date },
                earliestLoadedDate = previousDate,
                isLoadingMore = false,
            )
        }
    }
}
```

- [ ] **步骤 4：构建验证**

运行：`.\gradlew.bat :feature:home:compileDebugKotlin`
预期：编译失败（UI 层仍引用旧 `rows`），后续任务修复

---

### 任务 3：日期头组件

**文件：**
- 创建：`feature/home/src/main/java/com/nltimer/feature/home/ui/components/DateHeader.kt`

- [ ] **步骤 1：创建 DateHeader Composable**

```kotlin
package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val monthDayFormatter = DateTimeFormatter.ofPattern("M月d日")
private val weekDayFormatter = DateTimeFormatter.ofPattern("E")

@Composable
fun DateHeader(
    date: LocalDate,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    val label = when (date) {
        today -> "今天"
        today.minusDays(1) -> "昨天"
        today.minusDays(2) -> "前天"
        else -> date.format(monthDayFormatter)
    }
    val weekDay = date.format(weekDayFormatter)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "  $weekDay",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
```

- [ ] **步骤 2：构建验证**

运行：`.\gradlew.bat :feature:home:compileDebugKotlin`
预期：编译通过（新文件无外部依赖）

---

### 任务 4：TimeAxisGrid — 按 DaySection 分组渲染

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/components/TimeAxisGrid.kt`

- [ ] **步骤 1：修改 TimeAxisGrid 签名和内部逻辑**

将 `rows: List<GridRowUiState>` 参数替换为 `daySections: List<DaySection>`，新增 `onLoadMore: () -> Unit` 参数。使用 `stickyHeader` 渲染日期头，滚动到顶部触发加载：

```kotlin
@Composable
fun TimeAxisGrid(
    modifier: Modifier = Modifier,
    daySections: List<DaySection>,
    onEmptyCellClick: () -> Unit,
    onLayoutChange: (HomeLayout) -> Unit,
    currentHour: Int = 0,
    onLoadMore: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    var showLayoutMenu by remember { mutableStateOf(false) }

    LaunchedEffect(currentHour) {
        val todaySection = daySections.firstOrNull()
        if (todaySection != null) {
            val headerOffset = 1
            val targetIndex = todaySection.rows.indexOfFirst { it.startTime.hour >= currentHour }
            if (targetIndex >= 0) {
                listState.animateScrollToItem(headerOffset + targetIndex)
            }
        }
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            layoutInfo.visibleItemsInfo.firstOrNull()?.index?.let { it <= 2 } == true
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    LazyColumn(
        state = listState,
        modifier = modifier.padding(start = 10.dp, end = 0.dp, top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showLayoutMenu = true }
                ) {
                    Text(
                        text = "网格时间",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showLayoutMenu,
                    onDismissRequest = { showLayoutMenu = false }
                ) {
                    HomeLayout.values().forEach { layout ->
                        DropdownMenuItem(
                            text = { Text(layout.toDisplayString()) },
                            onClick = {
                                onLayoutChange(layout)
                                showLayoutMenu = false
                            }
                        )
                    }
                }
            }
        }

        daySections.forEach { section ->
            stickyHeader(key = "header-${section.date}") {
                DateHeader(date = section.date)
            }
            items(items = section.rows, key = { it.rowId }) { row ->
                GridRow(
                    row = row,
                    onEmptyCellClick = onEmptyCellClick,
                )
            }
        }
    }
}
```

需要新增 import：
```kotlin
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import com.nltimer.feature.home.model.DaySection
```

- [ ] **步骤 2：构建验证**

运行：`.\gradlew.bat :feature:home:compileDebugKotlin`
预期：编译失败（HomeScreen 仍传 rows），后续任务修复

---

### 任务 5：TimelineReverseView — 按 DaySection 分组渲染

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/components/TimelineReverseView.kt`

- [ ] **步骤 1：修改 TimelineReverseView 签名和内部逻辑**

将 `cells: List<GridCellUiState>` 替换为 `daySections: List<DaySection>`，新增 `onLoadMore`：

```kotlin
@Composable
fun TimelineReverseView(
    daySections: List<DaySection>,
    onAddClick: () -> Unit,
    onLayoutChange: (HomeLayout) -> Unit,
    modifier: Modifier = Modifier,
    onLoadMore: () -> Unit = {},
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    var showLayoutMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        val listState = rememberLazyListState()

        val shouldLoadMore by remember {
            derivedStateOf {
                listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index?.let { it <= 2 } == true
            }
        }
        LaunchedEffect(shouldLoadMore) {
            if (shouldLoadMore) onLoadMore()
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showLayoutMenu = true }
                    ) {
                        Text(
                            text = "时间轴",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showLayoutMenu,
                        onDismissRequest = { showLayoutMenu = false }
                    ) {
                        HomeLayout.entries.forEach { layout ->
                            DropdownMenuItem(
                                text = { Text(layout.toDisplayString()) },
                                onClick = {
                                    onLayoutChange(layout)
                                    showLayoutMenu = false
                                }
                            )
                        }
                    }
                }
            }

            daySections.forEach { section ->
                stickyHeader(key = "header-${section.date}") {
                    DateHeader(date = section.date)
                }

                val behaviors = remember(section.rows) {
                    section.rows.flatMap { it.cells }
                        .filter { it.behaviorId != null }
                        .sortedBy { it.startTime }
                }

                val timelineItems = remember(behaviors) {
                    val items = mutableListOf<TimelineItemData>()

                    if (behaviors.isNotEmpty()) {
                        val latest = behaviors.last()
                        if (latest.status != com.nltimer.core.data.model.BehaviorNature.ACTIVE && latest.endTime != null) {
                            val now = LocalTime.now()
                            if (now.isAfter(latest.endTime)) {
                                items.add(TimelineItemData.Idle(latest.endTime, now))
                            }
                        }

                        for (i in behaviors.indices.reversed()) {
                            val behavior = behaviors[i]
                            items.add(TimelineItemData.Behavior(behavior))

                            if (i > 0) {
                                val prevEnd = behaviors[i - 1].endTime
                                val currentStart = behavior.startTime
                                if (prevEnd != null && currentStart != null && currentStart.isAfter(prevEnd)) {
                                    items.add(TimelineItemData.Idle(prevEnd, currentStart))
                                }
                            }
                        }
                    }
                    items
                }

                items(timelineItems) { item ->
                    when (item) {
                        is TimelineItemData.Behavior -> {
                            TimelineBehaviorItem(
                                behavior = item.behavior,
                                timeFormatter = timeFormatter
                            )
                        }
                        is TimelineItemData.Idle -> {
                            TimelineIdleItem(
                                start = item.start,
                                end = item.end,
                                timeFormatter = timeFormatter,
                                onAddClick = onAddClick
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        FloatingActionButton(
            onClick = onAddClick,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(64.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(32.dp))
        }
    }
}
```

需要新增 import：
```kotlin
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import com.nltimer.feature.home.model.DaySection
```

- [ ] **步骤 2：构建验证**

运行：`.\gradlew.bat :feature:home:compileDebugKotlin`

---

### 任务 6：BehaviorLogView — 按 DaySection 分组渲染

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/components/BehaviorLogView.kt`

- [ ] **步骤 1：修改 BehaviorLogView 签名和内部逻辑**

将 `cells: List<GridCellUiState>` 替换为 `daySections: List<DaySection>`，新增 `onLoadMore`：

```kotlin
@Composable
fun BehaviorLogView(
    daySections: List<DaySection>,
    onLayoutChange: (HomeLayout) -> Unit,
    modifier: Modifier = Modifier,
    onLoadMore: () -> Unit = {},
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    var showLayoutMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        val listState = rememberLazyListState()

        val shouldLoadMore by remember {
            derivedStateOf {
                listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index?.let { it <= 2 } == true
            }
        }
        LaunchedEffect(shouldLoadMore) {
            if (shouldLoadMore) onLoadMore()
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showLayoutMenu = true }
                    ) {
                        Text(
                            text = "行为日志",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showLayoutMenu,
                        onDismissRequest = { showLayoutMenu = false }
                    ) {
                        HomeLayout.entries.forEach { layout ->
                            DropdownMenuItem(
                                text = { Text(layout.toDisplayString()) },
                                onClick = {
                                    onLayoutChange(layout)
                                    showLayoutMenu = false
                                }
                            )
                        }
                    }
                }
            }

            daySections.forEach { section ->
                stickyHeader(key = "header-${section.date}") {
                    DateHeader(date = section.date)
                }

                val behaviors = remember(section.rows) {
                    section.rows.flatMap { it.cells }
                        .filter { it.behaviorId != null && it.startTime != null }
                        .sortedByDescending { it.startTime }
                }

                if (behaviors.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暂无行为记录",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(items = behaviors, key = { it.behaviorId!! }) { behavior ->
                        BehaviorLogCard(
                            behavior = behavior,
                            timeFormatter = timeFormatter
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
```

需要新增 import：
```kotlin
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import com.nltimer.feature.home.model.DaySection
```

- [ ] **步骤 2：构建验证**

运行：`.\gradlew.bat :feature:home:compileDebugKotlin`

---

### 任务 7：HomeScreen — 适配 DaySection

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt`

- [ ] **步骤 1：修改 HomeScreen 参数和内部调用**

1. 新增 `onLoadMore: () -> Unit` 参数
2. 将 `uiState.rows` 的引用改为 `uiState.daySections`
3. 更新三种布局组件的调用签名

关键变更点：

```kotlin
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    activities: List<Activity>,
    activityGroups: List<ActivityGroup>,
    tagsForSelectedActivity: List<Tag>,
    allTags: List<Tag>,
    dialogConfig: DialogGridConfig = DialogGridConfig(),
    onEmptyCellClick: () -> Unit,
    onAddBehavior: (activityId: Long, tagIds: List<Long>, startTime: LocalTime, nature: BehaviorNature, note: String?) -> Unit,
    onDismissSheet: () -> Unit,
    onCompleteBehavior: (Long) -> Unit,
    onToggleIdleMode: () -> Unit,
    onStartNextPending: () -> Unit,
    onAddActivity: (name: String, emoji: String) -> Unit,
    onAddTag: (name: String) -> Unit,
    onHourClick: (Int) -> Unit,
    onLayoutChange: (HomeLayout) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val layout = LocalTheme.current.homeLayout

    val activeBehaviorId by remember(uiState.daySections) {
        derivedStateOf {
            uiState.daySections
                .flatMap { it.rows }
                .flatMap { it.cells }
                .firstOrNull { it.isCurrent && it.behaviorId != null }
                ?.behaviorId
        }
    }

    // ... Scaffold 不变 ...

    // Grid 模式
    HomeLayout.GRID -> {
        Row(modifier = Modifier.weight(1f)) {
            TimeAxisGrid(
                daySections = uiState.daySections,
                onEmptyCellClick = onEmptyCellClick,
                currentHour = uiState.selectedTimeHour,
                onLayoutChange = onLayoutChange,
                onLoadMore = onLoadMore,
                modifier = Modifier.weight(1f),
            )
            TimeSideBar(
                activeHours = uiState.daySections
                    .flatMap { it.rows }
                    .filter { it.cells.any { cell -> cell.behaviorId != null } || it.isCurrentRow }
                    .map { it.startTime.hour }
                    .toSet(),
                currentHour = uiState.selectedTimeHour,
                onHourClick = onHourClick,
            )
        }
    }
    // Timeline 模式
    HomeLayout.TIMELINE_REVERSE -> {
        TimelineReverseView(
            daySections = uiState.daySections,
            onAddClick = onEmptyCellClick,
            onLayoutChange = onLayoutChange,
            onLoadMore = onLoadMore,
            modifier = Modifier.weight(1f)
        )
    }
    // Log 模式
    HomeLayout.LOG -> {
        BehaviorLogView(
            daySections = uiState.daySections,
            onLayoutChange = onLayoutChange,
            onLoadMore = onLoadMore,
            modifier = Modifier.weight(1f)
        )
    }
```

- [ ] **步骤 2：修改 Preview 中的示例数据**

将 Preview 中的 `rows = listOf(...)` 改为 `daySections = listOf(DaySection(date = LocalDate.now(), rows = listOf(...)))`，并添加 `onLoadMore = {}`。

- [ ] **步骤 3：构建验证**

运行：`.\gradlew.bat :feature:home:compileDebugKotlin`

---

### 任务 8：HomeScreen 调用方 — 传入 onLoadMore

**文件：**
- 搜索 HomeScreen 的调用方，传入 `onLoadMore = viewModel::loadPreviousDay`

- [ ] **步骤 1：找到 HomeScreen 的调用处并添加 onLoadMore 参数**

搜索项目中调用 `HomeScreen(` 的位置，添加 `onLoadMore = viewModel::loadPreviousDay`。

- [ ] **步骤 2：全量构建验证**

运行：`.\gradlew.bat :app:assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：安装到设备验证**

运行构建安装命令，在设备上验证：
1. 主页正常显示今天数据
2. 向上滚动时日期头吸顶
3. 滚到顶部时自动加载前一天数据
4. 三种布局模式均正常工作

- [ ] **步骤 4：Commit**

```bash
git add -A
git commit -m "feat: 主页支持跨天懒加载，按天分组显示吸顶日期头"
```
