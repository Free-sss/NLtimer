# 主页日志模式设计规格

## 概述

在 NLtimer 主页现有两种布局模式（网格时间、时间轴倒序）基础上，新增第三种布局模式：**行为日志（LOG）**。该模式以卡片列表形式详细展示所有行为记录的完整字段信息，面向开发者调试和有详细觉察需求的用户。

## 架构决策

- **模式定位**：`HomeLayout` 枚举新增 `LOG` 值，与 `GRID`、`TIMELINE_REVERSE` 并列，通过顶部下拉菜单切换
- **数据复用**：直接复用现有 `GridCellUiState` 数据，无需新增数据模型或 Repository 查询
- **UI 层**：在 `feature:home` 模块新增 `BehaviorLogView.kt` 组件
- **样式风格**：遵循现有 Material Design 3 暗色主题，与 `TimelineReverseView` 卡片风格保持一致

## 数据模型

### 复用现有模型

日志模式不引入新数据模型，直接使用：

- `GridCellUiState` — 单元格 UI 状态（已包含所有需要展示的字段）
- `TagUiState` — 标签 UI 状态
- `BehaviorNature` — 行为状态枚举（PENDING / ACTIVE / COMPLETED）

### GridCellUiState 已有字段映射

| 字段 | 类型 | 日志卡片展示位置 |
|------|------|----------------|
| `behaviorId` | Long? | 开发者信息区（小字灰色） |
| `activityEmoji` | String? | 头部 — 活动 emoji |
| `activityName` | String? | 头部 — 活动名称 |
| `tags` | List<TagUiState> | 标签区 — chip 列表 |
| `status` | BehaviorNature? | 头部 — 状态标签（带颜色） |
| `isCurrent` | Boolean | 卡片背景高亮（ACTIVE 状态） |
| `wasPlanned` | Boolean | 详细字段区 — "计划内: 是/否" |
| `achievementLevel` | Int? | 详细字段区 — "完成度: X"（如有） |
| `estimatedDuration` | Long? | 详细字段区 — "预估: X"（毫秒转可读格式） |
| `actualDuration` | Long? | 详细字段区 — "实际: X"（毫秒转可读格式） |
| `durationMs` | Long? | 时间区 — 计算并显示用时 |
| `startTime` | LocalTime? | 时间区 — 起始时间 HH:mm |
| `endTime` | LocalTime? | 时间区 — 结束时间 HH:mm（空则显示"进行中"） |
| `note` | String? | 详细字段区 — 备注（如有则显示，无则隐藏） |
| `pomodoroCount` | Int | 详细字段区 — "番茄钟: X"（从 Behavior 模型，需确认 UIState 是否已包含） |

> **注意**：`pomodoroCount` 当前在 `GridCellUiState` 中不存在，需要从 `Behavior` 模型透传。若 UIState 未包含，需在 `buildUiState` 中补充。

## 模块结构

### 变更文件清单

```
feature/home/
├── model/
│   └── GridCellUiState.kt          # 新增 pomodoroCount 字段
├── ui/
│   ├── HomeScreen.kt               # 新增 LOG 分支渲染 BehaviorLogView
│   └── components/
│       └── BehaviorLogView.kt      # 新增：日志模式主组件
├── viewmodel/
│   └── HomeViewModel.kt            # 补充 pomodoroCount 到 GridCellUiState

core/designsystem/
└── theme/
    ├── HomeLayout.kt               # 新增 LOG 枚举值
    └── EnumExt.kt                  # 新增 LOG 的 toDisplayString()
```

## UI 设计

### 布局切换菜单

下拉菜单选项从 2 个扩展为 3 个：
- 网格时间
- 时间轴(反)
- 行为日志 ← 新增

### 日志列表结构

使用 `LazyColumn`，顶部为布局切换标题行，下方为行为卡片列表（按 `startTime` 倒序）。

### 行为日志卡片结构

```
┌─────────────────────────────────────────┐
│ [emoji] 活动名称              [状态标签] │  ← 头部信息行
├─────────────────────────────────────────┤
│ 起始: 09:15 → 结束: 10:30   用时: 1h15m │  ← 时间信息行
├─────────────────────────────────────────┤
│ #标签1  #标签2  #标签3                   │  ← 标签区
├─────────────────────────────────────────┤
│ 备注: 专注编码，效率较高                  │  ← 备注（可选，无则不显示）
│ 番茄钟: 2    预估: 1h    实际: 1h15m     │  ← 详细字段区（小字灰色）
│ 完成度: 4    计划内: 是                  │
├─────────────────────────────────────────┤
│ behaviorId: 42 | activityId: 7          │  ← 开发者字段区（更小字号、更浅颜色）
└─────────────────────────────────────────┘
```

### 状态标签颜色

| 状态 | 背景色 | 文字色 |
|------|--------|--------|
| ACTIVE | `primaryContainer` | `onPrimaryContainer` |
| COMPLETED | `tertiaryContainer` | `onTertiaryContainer` |
| PENDING | `secondaryContainer` | `onSecondaryContainer` |

### 空状态

当天无行为记录时，列表中央显示：
- 图标：待确认（可用 `Icons.Default.List` 或类似）
- 文字："暂无行为记录"

## 交互逻辑

1. **布局切换**：点击顶部标题行下拉菜单，选择"行为日志"切换到此模式
2. **卡片点击**：可选，点击卡片可打开现有行为详情底部弹窗（复用 `isDetailSheetVisible` 逻辑）
3. **滚动**：`LazyColumn` 支持自然滚动，无额外滚动定位逻辑
4. **数据排序**：按 `startTime` 倒序排列（最新的行为在最上方）

## 实现要点

### HomeLayout 枚举扩展

```kotlin
enum class HomeLayout {
    GRID,
    TIMELINE_REVERSE,
    LOG,
}
```

### toDisplayString() 扩展

```kotlin
fun HomeLayout.toDisplayString(): String = when (this) {
    HomeLayout.GRID -> "网格时间"
    HomeLayout.TIMELINE_REVERSE -> "时间轴(反)"
    HomeLayout.LOG -> "行为日志"
}
```

### HomeScreen 分支新增

```kotlin
when (layout) {
    HomeLayout.GRID -> { /* 现有逻辑 */ }
    HomeLayout.TIMELINE_REVERSE -> { /* 现有逻辑 */ }
    HomeLayout.LOG -> {
        BehaviorLogView(
            cells = uiState.rows.flatMap { it.cells },
            onLayoutChange = onLayoutChange,
            modifier = Modifier.weight(1f)
        )
    }
}
```

### 时长格式化工具

复用 `TimelineReverseView.kt` 中已有的 `formatDuration(ms: Long): String` 函数，或提取为公共工具函数。

## 依赖变更

无新增依赖。仅修改现有模块内文件。

## 测试要点

1. 布局切换菜单正确显示 3 个选项
2. 切换到 LOG 模式后正确渲染行为卡片列表
3. 卡片字段完整显示，无 NPE 或空指针异常
4. 空状态正确显示"暂无行为记录"
5. 状态标签颜色正确对应 BehaviorNature
6. 时长计算正确（durationMs > 0 时显示，否则用 actualDuration）
