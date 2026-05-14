# 主页布局独立样式配置 — 设计规格

## 目标

为主页的每种布局模式（GRID/LOG/TIMELINE/MOMENT）提供独立的样式配置，通过 DataStore 持久化，在设置页提供配置入口。

## 数据结构

```kotlin
data class HomeLayoutConfig(
    val grid: GridLayoutStyle = GridLayoutStyle(),
    val log: LogLayoutStyle = LogLayoutStyle(),
    val timeline: TimelineLayoutStyle = TimelineLayoutStyle(),
    val moment: MomentLayoutStyle = MomentLayoutStyle(),
)

data class GridLayoutStyle(
    val columns: Int = 4,
    val minRowHeight: Int = 100,
    val maxCellHeight: Int = 140,
    val columnSpacing: Int = 5,
    val cellPadding: Int = 4,
    val iconSize: Int = 14,
    val tagScale: Float = 0.8f,
    val tagSpacing: Int = 2,
    val activeBgAlpha: Float = 0.3f,
)

data class LogLayoutStyle(
    val cardPadding: Int = 12,
    val iconSize: Int = 18,
    val iconSpacing: Int = 6,
    val tagRowSpacing: Int = 6,
    val statusBadgePaddingH: Int = 8,
    val statusBadgePaddingV: Int = 2,
)

data class TimelineLayoutStyle(
    // 预留扩展
)

data class MomentLayoutStyle(
    // 预留扩展
)
```

## DataStore 键

| 键名 | 类型 | 映射字段 |
|------|------|---------|
| `home_grid_columns` | Int | grid.columns |
| `home_grid_min_row_height` | Int | grid.minRowHeight |
| `home_grid_max_cell_height` | Int | grid.maxCellHeight |
| `home_grid_column_spacing` | Int | grid.columnSpacing |
| `home_grid_cell_padding` | Int | grid.cellPadding |
| `home_grid_icon_size` | Int | grid.iconSize |
| `home_grid_tag_scale` | Float | grid.tagScale |
| `home_grid_tag_spacing` | Int | grid.tagSpacing |
| `home_grid_active_bg_alpha` | Float | grid.activeBgAlpha |
| `home_log_card_padding` | Int | log.cardPadding |
| `home_log_icon_size` | Int | log.iconSize |
| `home_log_icon_spacing` | Int | log.iconSpacing |
| `home_log_tag_row_spacing` | Int | log.tagRowSpacing |
| `home_log_badge_padding_h` | Int | log.statusBadgePaddingH |
| `home_log_badge_padding_v` | Int | log.statusBadgePaddingV |

## 数据流

```
SettingsPrefs.getHomeLayoutConfigFlow()
  → HomeViewModel.homeLayoutConfig: StateFlow<HomeLayoutConfig>
  → HomeScreen(config: HomeLayoutConfig)
  → HomeLayoutContent 根据 homeLayout 选择对应 style
    → GridContent(grid: GridLayoutStyle) → GridRow/GridCell 读取参数
    → LogContent(log: LogLayoutStyle) → BehaviorLogCard 读取参数
```

## 配置 UI

设置页新增"主页布局配置"入口卡片（SettingsEntryCard），点击进入 HomeLayoutConfigScreen。

HomeLayoutConfigScreen 内部按布局分节：
- ExpandableCard "网格布局" — 列数 Stepper(2-6)、最小行高 Stepper(60-200)、最大行高 Stepper(80-300)、列间距 Stepper(0-20)、内边距 Stepper(0-16)、图标大小 Stepper(8-24)、标签缩放 Stepper(0.5-1.0 x0.1)、标签间距 Stepper(0-8)、激活背景透明度 Stepper(0.1-0.8 x0.05)
- ExpandableCard "日志布局" — 卡片内边距 Stepper(4-24)、图标大小 Stepper(8-32)、图标间距 Stepper(2-16)、标签行间距 Stepper(2-16)
- ExpandableCard "其他布局" — 预留空

## 影响文件

- 新建：`core/data/.../model/HomeLayoutConfig.kt`
- 修改：`core/data/.../SettingsPrefs.kt` — 新增接口方法
- 修改：`core/data/.../SettingsPrefsImpl.kt` — 实现 DataStore 读写
- 修改：`feature/home/.../viewmodel/HomeViewModel.kt` — 读取 config
- 修改：`feature/home/.../ui/HomeScreen.kt` — 传递 config
- 修改：`feature/home/.../ui/components/GridRow.kt` — 使用 columns/spacing
- 修改：`feature/home/.../ui/components/GridCell.kt` — 使用 padding/iconSize/tagScale/tagSpacing/activeBgAlpha
- 修改：`feature/home/.../ui/components/BehaviorLogCard.kt` — 使用 log style
- 修改：`feature/home/.../ui/components/BehaviorCardContainer.kt` — 使用 cardPadding/tagRowSpacing
- 新建：`feature/settings/.../ui/HomeLayoutConfigScreen.kt`
- 修改：`feature/settings/.../ui/SettingsScreen.kt` — 新增入口
- 修改：导航图 — 新增路由
