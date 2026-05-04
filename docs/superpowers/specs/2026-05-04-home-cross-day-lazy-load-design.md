# 主页跨天懒加载设计

## 背景

当前主页只显示今天的行为数据，`HomeViewModel.loadHomeBehaviors()` 查询 `today → tomorrow` 一天范围。用户希望向上滚动时能懒加载历史数据（昨天、前天…），实现跨天浏览。

## 方案

手动按天追加（方案 B）。ViewModel 维护按天分组的状态，滚动到顶部时追加前一天数据。DAO 层已有 `getByDayRange()`，无需引入 Paging 3。

## 数据模型变更

### 新增 DaySection

```kotlin
data class DaySection(
    val date: LocalDate,
    val rows: List<GridRowUiState>,
)
```

### HomeUiState 变更

| 字段 | 变更 |
|------|------|
| `rows` | → 删除，替换为 `daySections: List<DaySection>` |
| 新增 | `earliestLoadedDate: LocalDate?` — 最早已加载日期 |
| 新增 | `isLoadingMore: Boolean = false` — 正在加载历史 |

## ViewModel 变更

1. `loadHomeBehaviors()` — 加载今天，结果放入 `daySections[0]`，设置 `earliestLoadedDate = today`
2. 新增 `loadPreviousDay()` — 取 `earliestLoadedDate - 1`，调用 `behaviorRepository.getHomeBehaviors()`，prepend 到 `daySections`
3. `buildUiState()` 改为 `buildDaySection(date, behaviors)` — 接收日期和行为列表，返回单个 `DaySection`

## UI 变更

### 三种布局模式统一改造

`TimeAxisGrid`、`TimelineReverseView`、`BehaviorLogView` 的 `LazyColumn` 改为按 `DaySection` 分组渲染：

```
LazyColumn {
    daySections.forEach { section ->
        stickyHeader(key = "header-${section.date}") {
            DateHeader(section.date)
        }
        items(section.rows, key = { it.rowId }) { row ->
            GridRow(row, ...)
        }
    }
}
```

### 日期头样式

- 今天 → "今天 5月4日 周日"
- 昨天 → "昨天 5月3日 周六"
- 更早 → "5月2日 周五"

使用 `stickyHeader` 实现吸顶。

### 滚动检测加载

在 `LazyColumn` 中检测首个可见项索引接近 0 时，回调 `onLoadMore()` 触发 `loadPreviousDay()`。

## 滚动方向

仅向上加载历史，向下为今天及以后（暂不加载未来）。

## 加载策略

滚动到顶部自动触发，`isLoadingMore` 防重复加载，每次加载 1 天。
