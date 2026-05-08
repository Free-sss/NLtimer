# 当前时刻布局模式设计规格

## 概述

在 NLtimer 主页新增第四种布局模式「当前时刻」（MOMENT）。上方为 Reef 风格聚焦卡片，根据当前行为状态动态切换内容和交互；下方为今日全部行为的时间轴列表。聚焦卡片的核心交互是滑动操作——有当前行为时滑动完成，有目标行为时滑动开启。

## 架构决策

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 布局枚举 | `HomeLayout.MOMENT` | 与 GRID/TIMELINE_REVERSE/LOG 并列，通过下拉菜单切换 |
| 卡片交互 | 滑动拉条（SlideActionPill） | 借鉴 Reef FocusTogglePill，触感反馈强，防误触 |
| 数据源 | 复用 `GridCellUiState` | 无需新增数据查询，直接从 `uiState.rows.cells` 提取 |
| 行为列表 | 精简卡片列表 | 复用 `GridCellUiState` 数据，独立轻量渲染 |
| 实时计时 | ViewModel 中 ` kotlinx.coroutines.flow` 每秒 emit | ACTIVE 行为的实时用时更新 |

## 组件结构

### 文件清单

| 文件 | 操作 | 职责 |
|------|------|------|
| `core/designsystem/.../theme/HomeLayout.kt` | 修改 | 新增 `MOMENT` 枚举值 |
| `core/designsystem/.../theme/EnumExt.kt` | 修改 | 新增 `MOMENT.toDisplayString() → "当前时刻"` |
| `feature/home/.../ui/components/MomentView.kt` | 新建 | 状态判断 + 卡片 + 列表的主布局 |
| `feature/home/.../ui/components/MomentFocusCard.kt` | 新建 | 聚焦卡片组件（Reef 风格） |
| `feature/home/.../ui/components/SlideActionPill.kt` | 新建 | 滑动操作拉条组件 |
| `feature/home/.../ui/HomeScreen.kt` | 修改 | `when(layout)` 分支新增 MOMENT → MomentView |

### 页面布局

```
┌──────────────────────────────────┐
│  [LayoutMenuHeader: 当前时刻 ▾]   │  布局切换菜单
├──────────────────────────────────┤
│                                  │
│  ┌────────────────────────┐      │
│  │   MomentFocusCard       │      │  聚焦卡片（~260dp）
│  │                        │      │
│  │  [SlideActionPill      ]│      │  滑动拉条
│  │   活动名 / 用时 / 描述  │      │  动态内容
│  └────────────────────────┘      │
│                                  │
├──────────────────────────────────┤
│  今日行为                        │  列表标题
│  ┌────────────────────────┐      │
│  │ 📖 阅读  08:00-09:30   │      │  行为卡片
│  │ 🏃 运动  10:00-...     │      │  ACTIVE 带脉冲高亮
│  │ 📝 写作  (目标)        │      │  PENDING 带标识
│  └────────────────────────┘      │
│  ...更多行为                     │
└──────────────────────────────────┘
```

## MomentFocusCard 三态交互

### 状态 1：有 ACTIVE 行为

卡片内容：
- 滑动拉条（滑到底 → 完成）
- 活动名称 + emoji（粗体，headlineLarge）
- 实时计时器（从 startTime 开始，每秒更新，格式 "HH:mm:ss"）
- 标签 chips
- 描述提示："正在专注..."

滑动触发：`onCompleteBehavior(activeBehaviorId)`

### 状态 2：无 ACTIVE 但有 PENDING

卡片内容：
- 滑动拉条（滑到底 → 开启第一个目标）
- 第一个 PENDING 活动名称 + emoji
- 预计时长或开始时间
- 标签 chips
- 描述提示："滑动开启目标"

滑动触发：`onStartNextPending()`

### 状态 3：无 ACTIVE 也无 PENDING

卡片内容：
- 无滑动拉条
- 居中显示图标 + "添加行为"
- 描述提示："点击开始记录你的行为"

点击触发：`onEmptyCellClick(null, null)`

## SlideActionPill 组件

借鉴 Reef 的 `FocusTogglePill`，适配 NLtimer 场景：

| 参数 | 类型 | 说明 |
|------|------|------|
| `onSlideProgress` | `(Float) -> Unit` | 滑动进度回调（0f~1f） |
| `onActivate` | `() -> Unit` | 滑到底部触发 |
| `activeLabel` | `String` | 滑动条右侧提示文字（如"滑动完成"/"滑动开启"） |
| `activatedLabel` | `String` | 滑过 50% 后提示文字（如"释放"/"松手即可"） |
| `leadingIcon` | `ImageVector` | 拖拽圆形中图标（默认） |
| `activatedIcon` | `ImageVector` | 拖拽圆形中图标（过半后） |

样式：
- 宽 200dp，高 72dp，圆角 36dp
- 容器颜色 `primary.copy(alpha = 0.3f + progress * 0.4f)`
- 拖拽圆形 60dp，颜色 `primary`，带阴影
- 过半后图标从默认图标切换为 ✓
- 松手时如未达到 70% 最大偏移则弹回（带弹簧动画）

## 行为列表

直接使用 `uiState.rows.flatMap { it.cells }` 中 `behaviorId != null` 的单元格。

每个行为卡片显示：
- 左侧：活动 emoji + 活动名
- 中间：标签 chips
- 右侧：时间范围（HH:mm - HH:mm），ACTIVE 行为显示"进行中"
- ACTIVE 行为：脉冲动画边框（`primary` 色）
- PENDING 行为：虚线边框
- 长按：触发 `onCellLongClick`

列表按 `startTime` 排序（现有数据已按时间排列）。

## 数据流

无新增 ViewModel/Repository 方法，全部复用现有：

| 卡片状态 | 数据来源 | 触发操作 |
|---------|---------|---------|
| ACTIVE | `hasActiveBehavior` + `activeBehaviorId` | `onCompleteBehavior(id)` |
| PENDING | cells 中 `status == PENDING` 取第一个 | `onStartNextPending()` |
| 空 | `hasActiveBehavior == false` 且无 PENDING cells | `onEmptyCellClick(null, null)` |

实时计时实现：MomentFocusCard 内使用 `produceState` 或 `LaunchedEffect` + `delay(1000)` 每秒计算 `(System.currentTimeMillis() - startEpochMs)` 并格式化显示。

## HomeScreen 集成

`HomeScreen` 的 `when(layout)` 分支新增：

```kotlin
HomeLayout.MOMENT -> {
    MomentView(
        cells = allCells,
        hasActiveBehavior = uiState.hasActiveBehavior,
        activeBehaviorId = activeBehaviorId,
        onCompleteBehavior = onCompleteBehavior,
        onStartNextPending = onStartNextPending,
        onEmptyCellClick = onEmptyCellClick,
        onCellLongClick = onCellLongClick,
        onLayoutChange = onLayoutChange,
        modifier = Modifier.weight(1f)
    )
}
```
