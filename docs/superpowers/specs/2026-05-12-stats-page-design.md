# NLtimer 统计页面设计规格

日期：2026-05-12

## 概述

将 `feature:stats` 模块从占位符升级为完整统计页面，采用分段滚动布局，展示多时间段统计数据，包含今日摘要、历史趋势图表、活动排行和标签分布。

## 设计决策记录

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 页面布局 | A 分段滚动页 | 与项目主页滚动体验一致 |
| 时间维度 | 多时间段全展示（今日/周/月/累计） | 用户选择 |
| 额外维度 | 活动 + 标签维度 | 用户选择 |
| 色块配色 | M3 Dynamic Color | 与项目 MaterialKolor 主题一致 |
| 图表库 | Vico (compose-m3) | Kotlin-first，M3 适配，参考 Tomato 已使用 |
| 环形图 | Compose Canvas 自绘 | 避免引入额外库 |

## 页面结构（自上而下）

### 1. 今日摘要 — 2x2 色块卡片

```
┌─────────────┐ ┌─────────────┐
│ 专注时长     │ │ 完成次数     │
│ PrimaryCont │ │ TertiaryCont│
│ 2h 35m      │ │ 8 次        │
└─────────────┘ └─────────────┘
┌─────────────┐ ┌─────────────┐
│ 番茄钟数     │ │ 计划完成率   │
│ SecondaryC  │ │ SurfaceVar  │
│ 12 个       │ │ 75%         │
└─────────────┘ └─────────────┘
```

- 4 张卡片使用 M3 dynamic color token：`PrimaryContainer`、`TertiaryContainer`、`SecondaryContainer`、`SurfaceVariant`（高对比）
- 数字带计数动画（`animateIntAsState` / `animateFloatAsState`）
- 圆角 `shapes.large`（约 16-28dp），与项目现有组件一致
- 卡片入场：`AnimatedVisibility` + `slideInVertically`，依次延迟 80ms

### 2. 近 7 天 — 柱状图卡片

- Vico `ColumnChart`，每日一根柱子
- 今日柱子用浅色/虚线区分（未完成）
- 标题行右侧显示日均时长
- X 轴：周一~周日（或日期）
- Y 轴：时长（小时/分钟自适应）
- 卡片背景 `Surface` 色块

### 3. 近 30 天 — 柱状图卡片

- 同 Vico `ColumnChart`，柱子更细
- 支持横向滚动（Vico 自带 `VicoScrollState`）
- 标题行右侧显示日均时长
- X 轴：日期
- Y 轴：时长

### 4. 活动排行 — 横向条形卡片

- 按总时长降序排列
- 每行包含：左侧色条（使用活动自身 `color` 字段或 M3 色阶） + 活动名 + 时长 + 次数 + 占比%
- 条形宽度按占比计算
- 最多显示前 10 个活动，其余折叠

### 5. 标签分布 — 环形图卡片

- Compose Canvas 自绘环形图
- 按标签聚合总时长
- 中心显示总时长数字
- 每段弧线使用标签自身 `color` 字段
- 右侧或下方显示图例列表（标签名 + 占比）

### 6. 累计总览 — 底部卡片

- 总专注时长
- 总完成次数
- 总番茄钟数
- 单行或 3 列布局

## 动画规格

| 动画 | 实现 | 参数 |
|------|------|------|
| 卡片入场 | `AnimatedVisibility` + `slideInVertically` | 依次延迟 80ms，duration 300ms |
| 数字计数 | `animateIntAsState` / `animateFloatAsState` | duration 800ms，`FastOutSlowInEasing` |
| 图表淡入 | `AnimatedVisibility` + `fadeIn` | 延迟 300ms（避免加载闪烁） |
| 页面滚动 | 标准 `LazyColumn` | 无特殊处理 |

## 数据层改动

### 模块依赖

- `feature:stats` 新增依赖 `core:data`
- `feature:stats` 新增 Hilt 注入支持（`nltimer.android.hilt` 插件）

### 新增 DAO 查询（BehaviorDao）

```kotlin
// 按日分组统计时长（以 startTime 整除 86400000 得到日索引）
@Query("""
    SELECT 
        (startTime / 86400000) as day,
        SUM(COALESCE(actualDuration, (endTime - startTime))) as totalDuration
    FROM behaviors 
    WHERE startTime >= :rangeStart AND startTime < :rangeEnd AND status = 'completed'
    GROUP BY day ORDER BY day ASC
""")
fun getDailyDurations(rangeStart: Long, rangeEnd: Long): Flow<List<DailyDurationRow>>

// 按活动分组统计时长
@Query("""
    SELECT a.id, a.name, a.color, 
           SUM(COALESCE(b.actualDuration, (b.endTime - b.startTime))) as totalDuration,
           COUNT(*) as count
    FROM behaviors b 
    JOIN activities a ON b.activityId = a.id
    WHERE b.startTime >= :rangeStart AND b.startTime < :rangeEnd AND b.status = 'completed'
    GROUP BY b.activityId ORDER BY totalDuration DESC
""")
fun getStatsByActivity(rangeStart: Long, rangeEnd: Long): Flow<List<ActivityStatRow>>

// 按标签分组统计时长
@Query("""
    SELECT t.id, t.name, t.color,
           SUM(COALESCE(b.actualDuration, (b.endTime - b.startTime))) as totalDuration
    FROM behaviors b
    JOIN behavior_tag_cross_ref btc ON b.id = btc.behaviorId
    JOIN tags t ON btc.tagId = t.id
    WHERE b.startTime >= :rangeStart AND b.startTime < :rangeEnd AND b.status = 'completed'
    GROUP BY t.id ORDER BY totalDuration DESC
""")
fun getStatsByTag(rangeStart: Long, rangeEnd: Long): Flow<List<TagStatRow>>
```

### 新增数据类

```kotlin
data class DailyDurationRow(val day: Long, val totalDuration: Long)
data class ActivityStatRow(val id: Long, val name: String, val color: Long?, val totalDuration: Long, val count: Int)
data class TagStatRow(val id: Long, val name: String, val color: Long?, val totalDuration: Long)
```

### 新增 StatsRepository

```kotlin
class StatsRepository @Inject constructor(
    private val behaviorDao: BehaviorDao,
    private val clockService: ClockService,
) {
    fun getTodayStats(): Flow<TodayStats>
    fun getDailyDurations(days: Int): Flow<List<DailyDurationRow>>
    fun getActivityRanking(days: Int): Flow<List<ActivityStatRow>>
    fun getTagDistribution(days: Int): Flow<List<TagStatRow>>
    fun getAllTimeStats(): Flow<AllTimeStats>
}
```

### 新增 StatsViewModel

```kotlin
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository
) : ViewModel() {
    val uiState: StateFlow<StatsUiState>
}
```

### 新增 UiState

```kotlin
data class StatsUiState(
    val todayStats: TodayStats,
    val weeklyChart: List<DailyDurationRow>,
    val monthlyChart: List<DailyDurationRow>,
    val activityRanking: List<ActivityStatRow>,
    val tagDistribution: List<TagStatRow>,
    val allTimeStats: AllTimeStats,
)

data class TodayStats(
    val focusDuration: Long,
    val completedCount: Int,
    val pomodoroCount: Int,
    val plannedRate: Float,
)

data class AllTimeStats(
    val totalDuration: Long,
    val totalCompleted: Int,
    val totalPomodoro: Int,
)
```

## 第三方依赖

### Vico 图表库

在 `gradle/libs.versions.toml` 新增：

```toml
[versions]
vico = "2.1.3"

[libraries]
vico-compose-m3 = { module = "com.patrykandpatrick.vico:compose-m3", version.ref = "vico" }
```

`feature:stats/build.gradle.kts` 新增：

```kotlin
implementation(libs.vico.compose.m3)
```

### 不引入的库

- 环形图：Compose Canvas 自绘，不引入额外库
- 动画：Compose 内置动画 API，不需要额外库

## 组件文件结构

```
feature/stats/src/main/java/com/nltimer/feature/stats/
├── ui/
│   ├── StatsRoute.kt          # 路由入口，注入 ViewModel
│   ├── StatsScreen.kt         # 主页面 LazyColumn
│   └── components/
│       ├── TodaySummaryCards.kt   # 2x2 色块卡片
│       ├── WeeklyChartCard.kt     # 近7天柱状图
│       ├── MonthlyChartCard.kt    # 近30天柱状图
│       ├── ActivityRankingCard.kt # 活动排行
│       ├── TagDonutCard.kt        # 标签环形图
│       ├── AllTimeCard.kt         # 累计总览
│       └── AnimatedCounter.kt     # 数字计数动画组件
├── viewmodel/
│   └── StatsViewModel.kt
└── model/
    └── StatsUiState.kt
```

## 边界情况

| 情况 | 处理 |
|------|------|
| 无数据 | 显示空状态占位（图标 + "暂无统计数据"） |
| 今日无记录 | 色块显示 0，不隐藏 |
| 活动无 color 字段 | 使用 M3 Primary 色阶自动分配 |
| 标签无关联 | 不显示标签分布卡片 |
| 横屏/大屏 | LazyColumn 自适应，卡片宽度 fillMaxWidth |

## 不在范围内

- 年度热力图（可后续迭代）
- 数据导出（已有独立功能）
- 统计数据缓存优化（首次实现不做）
- 共享元素转场动画（需要 Navigation3 支持，后续迭代）
