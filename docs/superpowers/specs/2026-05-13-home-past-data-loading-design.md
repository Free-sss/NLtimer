# 主页 4 布局加载历史数据 — 设计规格

**日期**: 2026-05-13
**类型**: 功能扩展
**状态**: 待实现
**Supersedes**: [2026-05-04-home-cross-day-lazy-load-design.md](2026-05-04-home-cross-day-lazy-load-design.md)

## 背景

主页 4 种布局（GRID / TIMELINE_REVERSE / LOG / MOMENT）当前只展示今日数据。`HomeViewModel.loadHomeBehaviors` 在 [HomeViewModel.kt:130-144](../../../feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeViewModel.kt#L130-L144) 把查询区间硬编码为 `today.startOfDayMillis()` 到 `today.endOfDayMillis()`。

用户场景：在主页内连续向过去滚动浏览历史数据，作为复盘 / 回看的入口。

## 与旧设计的差异（Supersedes 2026-05-04）

| 维度 | 2026-05-04（旧） | 2026-05-13（本设计） |
|---|---|---|
| 数据模型 | `daySections: List<DaySection>` | `HomeListItem` sealed + GRID row 加 `dayDivider` |
| 加载粒度 | 1 天 / 次 | 7 天 / 次 |
| MOMENT 处理 | 未提及 | 明确不参与，filter 掉非今日 |
| 空白日处理 | 未提及 | 整天无记录的日期不渲染 |
| 最远边界 | 未提及 | 数据库最早一条记录 |
| FAB 约束 | 未提及 | 明确不切换语义（来自 memory 约束）|

## 核心范式

**沿用现有 4 布局，在主页内部向过去无限滚动浏览。**

明确不做：
- 日期 navigator / 日历选择器
- 独立"历史回看"页面
- 任何形式的上下文 / 页面切换

## 关键约束

### 来自项目记忆（feedback_home_drag_feature）

- FAB 拖拽菜单的语义不能在不同浏览区域切换
- 拖拽功能是产品特色，不能弱化或移除

### 设计响应

- FAB 拖拽菜单始终作用于"今日动作"，与滚动位置无关
- 历史区编辑入口走 cell 长按，与 FAB 拖拽菜单语义完全解耦

## 数据层设计

### HomeViewModel 新增状态

```kotlin
private val _loadedEarliest = MutableStateFlow(today)
private val _earliestRecord = MutableStateFlow<LocalDate?>(null)
private val _isLoadingMore = MutableStateFlow(false)
```

- `_loadedEarliest`：当前已加载窗口的下界（含），初始 = today
- `_earliestRecord`：数据库中最早一条 behavior 的日期，作为加载下界
- `_isLoadingMore`：防抖标志，防止 Flow re-subscribe 期间重复触发

### loadHomeBehaviors 改造

原实现一次性传入 `today` 的时间区间。新实现改为：

```kotlin
private fun loadHomeBehaviors() {
    viewModelScope.launch {
        combine(
            _loadedEarliest.flatMapLatest { earliest ->
                behaviorRepository.getHomeBehaviors(
                    earliest.startOfDayMillis(),
                    today.endOfDayMillis()
                )
            },
            homeLayoutConfig
        ) { behaviors, _ -> behaviors }
            .collect { behaviors ->
                val state = buildUiState(behaviors)
                _uiState.update { state }
                _isLoadingMore.value = false
            }
    }
}
```

### loadMore 方法

```kotlin
fun loadMore() {
    if (_isLoadingMore.value) return
    val candidate = _loadedEarliest.value.minusDays(7)
    val cap = _earliestRecord.value ?: return
    val target = maxOf(candidate, cap)
    if (!target.isBefore(_loadedEarliest.value)) return  // 已到边界
    _isLoadingMore.value = true
    _loadedEarliest.value = target
}
```

### Repository 新增方法

```kotlin
// BehaviorRepository.kt
suspend fun getEarliestBehaviorDate(): Long?

// BehaviorDao.kt
@Query("SELECT MIN(start_time) FROM behaviors")
suspend fun getEarliestStartTime(): Long?
```

`HomeViewModel.init` 内调用一次，把结果转为 `LocalDate` 缓存到 `_earliestRecord`。无任何记录时为 null，loadMore 直接退化为无操作。

### 加载策略参数

- **加载粒度**：7 天 / 次
- **下界**：`_earliestRecord`
- **防抖**：`_isLoadingMore` 标志位

## UI 层数据模型

### HomeListItem sealed interface（新增，给 LOG / TIMELINE_REVERSE 用）

```kotlin
sealed interface HomeListItem {
    data class CellItem(val cell: GridCellUiState) : HomeListItem
    data class DayDivider(val date: LocalDate, val label: String) : HomeListItem
}
```

### GridDaySection（新增，给 GRID 用）

```kotlin
data class GridDaySection(
    val date: LocalDate,
    val label: String,
    val rows: List<GridRowUiState>,
)
```

GRID 按 section 渲染：每个 section 顶部是 DayDivider 的 stickyHeader，下面是该日的 24 行小时网格。`GridRowUiState` 自身**不变**。

### HomeUiState 字段调整

保留现有 `rows: List<GridRowUiState>` 字段（兼容当前 GRID 实现的最简过渡，第一阶段可先用；最终目标是用 `gridSections` 替换）。本设计的最终态：

| 字段 | 说明 |
|---|---|
| `items: List<HomeListItem>` | **新增**，给 LOG / TIMELINE_REVERSE 使用 |
| `gridSections: List<GridDaySection>` | **新增**，给 GRID 使用 |
| `momentCells: List<GridCellUiState>` | **新增**，给 MOMENT 使用（只含今日 cells） |
| `rows: List<GridRowUiState>` | **删除**（被 gridSections 取代） |
| `isLoadingMore: Boolean = false` | **新增**，控制加载指示器 |
| `hasReachedEarliest: Boolean = false` | **新增**，控制"已到最早"提示 |
| 其余字段 | 不变 |

### HomeUiStateBuilder 改造

- 输入：多日 behaviors
- 输出：同时构造 `items`（按时间排序、跨日处插入 DayDivider）和 `gridSections`（按日分组，每个 section 内部仍是 24 行网格）
- 整天没有 behavior 的日期：不出现在 `items` 和 `gridSections` 中

### 空白日处理

- 整天没有 behavior 的日期：不渲染（不出现孤立的 DayDivider、不出现空白 row 区）
- 实现位置：UiStateBuilder 按日分组后，整天无 cell 的日期直接跳过

## 各布局处理

### LOG 布局

- [BehaviorLogView.kt](../../../feature/home/src/main/java/com/nltimer/feature/home/ui/components/BehaviorLogView.kt) 渲染 `HomeListItem` 列表，DayDivider 显示为分隔条样式
- `LazyListState` 监听：当 `lastVisibleItemIndex >= totalItems - 5` 时回调 `onLoadMore()`
- 编辑能力（cell 长按 → EditSheet）保留

### TIMELINE_REVERSE 布局

- [TimelineReverseView.kt](../../../feature/home/src/main/java/com/nltimer/feature/home/ui/components/TimelineReverseView.kt) 同 LOG 处理
- 时间线方向不变：倒序，新在上、旧在下

### GRID 布局

- [TimeAxisGrid.kt](../../../feature/home/src/main/java/com/nltimer/feature/home/ui/components/TimeAxisGrid.kt) 改造为多日网格拼接
- **方向**：每日内部时间轴保持现状（早晨在上、夜晚在下），日与日之间按时间顺序拼接——今天在最下方，往上是昨天、前天
- **加载触发**：`firstVisibleItemIndex <= 5` 时回调 `onLoadMore()`
- **TimeSideBar**：默认按"当前可见日期"的活跃 hour 显示；如果实现阶段发现 UX 复杂或滚动跳变明显，降级为"滚到历史区时折叠 TimeSideBar"
- `isCurrentRow / selectedTimeHour` 仅对今日 row 生效（历史 row 一律 false）

### MOMENT 布局

- [MomentView.kt](../../../feature/home/src/main/java/com/nltimer/feature/home/ui/components/MomentView.kt) 不改
- **数据源**：在 HomeUiState 新增 `momentCells: List<GridCellUiState>`（由 UiStateBuilder 从今日 behaviors 单独构造），MOMENT 直接消费它，不依赖 `items` / `gridSections`
- 历史数据加载只影响 `items` / `gridSections`，不影响 `momentCells`，MOMENT 完全无感

## 交互细节

| 交互 | 行为 |
|---|---|
| FAB 拖拽菜单 | 始终作用于"今日"，不因滚动位置改变 |
| 长按任意 cell（含历史） | 调起 EditSheet，可编辑 |
| 点击空白 cell | 与现状保持一致（弹添加 sheet） |
| 加载中 | 在 list 底部 / grid 顶部边缘显示轻量 spinner |
| 到达最早记录 | 边界显示"已到最早一条记录"提示，不再触发 |

## 受影响的关键文件

| 文件 | 改动类型 |
|---|---|
| feature/home/.../HomeViewModel.kt | 新增 `_loadedEarliest / _earliestRecord / _isLoadingMore`、`loadMore()`，改造 `loadHomeBehaviors` |
| feature/home/.../HomeUiStateBuilder.kt | 支持多日 behaviors，输出 `items` 和 `gridSections` |
| feature/home/.../model/HomeListItem.kt | **新增**：sealed interface |
| feature/home/.../model/GridDaySection.kt | **新增**：GRID 按日分组的 section |
| feature/home/.../model/HomeUiState.kt | 删除 `rows`；新增 `items / gridSections / isLoadingMore / hasReachedEarliest` |
| feature/home/.../model/GridRowUiState.kt | 不变 |
| feature/home/.../ui/components/BehaviorLogView.kt | 消费 `items`，渲染 DayDivider + LazyListState 边界监听 + onLoadMore 回调 |
| feature/home/.../ui/components/TimelineReverseView.kt | 同上 |
| feature/home/.../ui/components/TimeAxisGrid.kt | 消费 `gridSections`，按 section 渲染（stickyHeader + 24 行）+ 顶部边界监听 |
| feature/home/.../ui/components/MomentView.kt | 不变（消费新字段 `momentCells`） |
| feature/home/.../ui/HomeScreen.kt | 把 `viewModel::loadMore` 透传给 3 个布局；把 `items / gridSections / momentCells` 分发给对应布局 |
| core/data/.../repository/BehaviorRepository.kt + Impl | 新增 `getEarliestBehaviorDate()` |
| core/data/.../database/dao/BehaviorDao.kt | 新增 `getEarliestStartTime()` 查询 |

## 测试要点

- **加载触发**：滚到边界时调用 `loadMore`、防抖生效、相同窗口不重复加载
- **跨日列表项**：相邻 cell 跨日时正确插入 DayDivider；同一日内不插入
- **`isCurrentRow` 隔离**：历史日的 row 一律为 false，selectedTimeHour 只影响今日
- **空白日跳过**：连续无记录的日期不渲染、不出现死区
- **最早记录边界**：滚到 `_earliestRecord` 后 `loadMore` 无效、显示提示
- **MOMENT 隔离**：历史数据 collect 后 MOMENT 仍只显示今日 cells
- **编辑历史 cell**：长按 → EditSheet 正确加载该 cell 的 activity / tags / note
- **FAB 不变**：拖拽菜单语义与滚动位置无关

## YAGNI 排除项（明确不做）

- 日期 navigator / 日历选择器
- 独立"历史回看"页面
- MOMENT 的历史变通方案
- FAB 拖拽菜单语义切换
- 历史区限编辑 / 只读模式
- 跨日"周/月汇总"展示
- 数据可视化（趋势图）

## 后续可能扩展（不在本次范围）

- 历史区域的统计 / 汇总入口
- 跨日搜索 / 过滤
- 历史数据的可视化
