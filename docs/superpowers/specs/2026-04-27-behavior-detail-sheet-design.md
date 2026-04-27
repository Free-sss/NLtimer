# 行为详情页（BehaviorDetailSheet）功能设计文档

> 版本：v1.0 | 日期：2026-04-27 | 状态：已确认，待实现

---

## 1. 概述

### 1.1 背景

NLtimer 当前已实现行为的添加（`AddBehaviorSheet`）和完成操作，但缺少对已有行为的查看与编辑能力。用户长按主页网格中的行为卡片后，需弹出详情页，支持修改活动、标签、时间、备注，以及删除和时间填充操作。

### 1.2 目标功能清单

| # | 功能 | 说明 |
|---|------|------|
| 1 | 可交互式数据展示 | 活动选择、标签切换、时间滚轮、备注编辑，均支持点击/滑动操作及视觉反馈 |
| 2 | 开始时间选择 | 日期+时分滚轮 + [上尾]/[当前] 快捷按钮 |
| 3 | 结束时间选择 | 日期+时分滚轮 + [当前] 快捷按钮 |
| 4 | 活动及标签修改 | 点击切换活动、标签多选/取消，保存时实时落库 |
| 5 | 删除功能 | 删除按钮 → 确认弹窗 → 级联删除关联数据 |
| 6 | 时间填充功能 | 填满左侧/右侧空闲时间，自动计算并调整时间边界 |
| 7 | 可复用性 | 所有新增组件可被任意弹窗（AddSheet / DetailSheet / 未来弹窗）复用 |

### 1.3 技术栈

| 维度 | 选型 |
|------|------|
| UI | Jetpack Compose + Material3 |
| 弹窗 | `ModalBottomSheet`（与 AddBehaviorSheet 一致）|
| 架构 | MVVM + Repository，Hilt 注入 |
| 数据 | Room Database |
| Min SDK | API 31（Android 12）|

---

## 2. 组件架构与可复用性

### 2.1 拆分策略

将现有 `AddBehaviorSheet` 中隐式耦合的 UI 组件拆分为独立 Composable，所有新组件遵循：**输入通过参数传入，输出通过回调传出**，不依赖 ViewModel。

### 2.2 最终文件结构

```
feature/home/ui/sheet/
├── [新增] TimeSelectionSection.kt      带快捷按钮的起止时间选择器（可复用）
├── [新增] DateScrollPicker.kt          日期滚轮选择器（TimeSelectionSection 内部使用）
├── [新增] TimeScrollPicker.kt          时间滚轮选择器（TimeSelectionSection 内部使用）
├── [新增] TimeFillActions.kt           填满左/右侧空闲时间按钮组（可复用）
├── [新增] DeleteConfirmDialog.kt       删除确认弹窗（可复用）
├── [新增] BehaviorDetailSheet.kt       行为详情弹窗外壳（ModalBottomSheet）
├── [新增] BehaviorDetailSheetContent.kt 行为详情内容体（可被 Dialog 等容器复用）
├── [保持] ActivityPicker.kt            活动选择器
├── [保持] TagPicker.kt                 标签选择器
├── [保持] NoteInput.kt                 备注输入框
├── [保持] BehaviorNatureSelector.kt    行为类型选择器
├── [保持] AddActivityDialog.kt         添加活动弹窗
├── [保持] AddTagDialog.kt             添加标签弹窗
├── [调整] AddBehaviorSheet.kt          改为组合可复用组件
└── [调整] AddBehaviorSheetContent.kt   同上
```

### 2.3 组件依赖树

```
BehaviorDetailSheet (ModalBottomSheet)
└── BehaviorDetailSheetContent (可复用内容体)
    ├── 活动名 + 状态标签行
    ├── ActivityPicker (复用)
    ├── TagPicker ×2 (复用：关联标签 + 所有标签)
    ├── TimeSelectionSection (新增)
    │   ├── DateScrollPicker (新增)
    │   ├── TimeScrollPicker (新增)
    │   ├── [上尾] Chip 按钮
    │   └── [当前] Chip 按钮
    ├── TimeFillActions (新增)
    │   ├── [← 填满左侧空闲] OutlinedButton
    │   └── [填满右侧空闲 →] OutlinedButton
    ├── NoteInput (复用)
    └── 保存/删除按钮行

DeleteConfirmDialog (新增，删除时独立弹出)
```

### 2.4 关键组件接口

**TimeSelectionSection**（可复用时间选择区域）：
- 参数：`startDateTime`, `endDateTime?`, `onStartDateTimeChange`, `onEndDateTimeChange`, `showEndTime`, `onQuickFillHead?`, `onQuickFillCurrentStart?`, `onQuickFillCurrentEnd?`
- 复用场景：AddBehaviorSheet（`showEndTime` 按 nature 动态控制，快捷按钮全部为 null）、BehaviorDetailSheet（全部启用）

**TimeFillActions**（可复用填充按钮组）：
- 参数：`onFillLeft`, `onFillRight`, `leftFillEnabled`, `rightFillEnabled`

**DeleteConfirmDialog**（可复用删除确认）：
- 参数：`behaviorName`, `onDismiss`, `onConfirm`

**BehaviorDetailSheetContent**（可复用内容体）：
- 参数：`detail`, `allActivities`, `allTags`, `onConfirm`, `onDelete`
- 可被 `ModalBottomSheet` 或 `Dialog` 复用

---

## 3. 数据流与状态管理

### 3.1 整体数据流

```
GridCell.onLongClick(behaviorId)
  → HomeScreen → viewModel.showDetailSheet(behaviorId)
  → Repository.getBehaviorWithDetails(id) → BehaviorWithDetails
  → HomeUiState.detailBehavior = BehaviorDetailUiState(...)
  → BehaviorDetailSheet 渲染

用户编辑 → 收集变更到 local state
  → 保存 → viewModel.updateBehaviorDetail(...)
    → Repository: setStartTime/setEndTime/setStatus/setNote + updateTags
  → refresh → 主页即时刷新
```

### 3.2 HomeUiState 扩展

现有字段保持不变，新增：
- `isDetailSheetVisible: Boolean` — 控制详情弹窗显隐
- `detailBehavior: BehaviorDetailUiState?` — 详情数据快照
- `isSaving: Boolean` — 保存中状态
- `errorMessage: String?` — 错误信息（一次性消费）

### 3.3 BehaviorDetailUiState

```kotlin
data class BehaviorDetailUiState(
    val behaviorId: Long,
    val activityId: Long,
    val activityEmoji: String?,
    val activityName: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime?,
    val status: BehaviorNature,
    val note: String?,
    val tags: List<TagUiState>,
    val allAvailableTags: List<TagUiState>,
    val allActivities: List<Activity>,
    val achievementLevel: Int?,
    val estimatedDuration: Long?,
    val actualDuration: Long?,
)
```

### 3.4 ViewModel 新增方法

| 方法 | 职责 |
|------|------|
| `showDetailSheet(behaviorId)` | 加载详情，构建 UiState，设置 visible=true |
| `hideDetailSheet()` | 关闭弹窗，清空 detailBehavior |
| `updateBehaviorDetail(...)` | 批量更新行为字段并落库 |
| `deleteBehaviorDetail(behaviorId)` | 删除行为及关联标签，刷新主页 |
| `fillLeftGap(behaviorId)` | 执行填满左侧空闲时间算法 |
| `fillRightGap(behaviorId)` | 执行填满右侧空闲时间算法 |

### 3.5 Repository 层需新增的方法

| 方法 | 用途 |
|------|------|
| `suspend fun updateBehavior(id, activityId, startTime, endTime, status, note)` | 批量更新行为字段 |
| `suspend fun updateTagsForBehavior(behaviorId, tagIds)` | 全量替换行为关联标签 |

### 3.6 长按触发链路

```
GridCell.kt → modifier = Modifier.combinedClickable(
    onClick = { ... },
    onLongClick = { onLongPress(behaviorId) }
) → TimeAxisGrid → GridRow → HomeScreen.onCellLongPress → viewModel.showDetailSheet()
```

---

## 4. 时间选择器与快捷按钮

### 4.1 滚轮选择器规格

使用 `LazyColumn` + 手动吸附实现自定义滚轮。

| 属性 | 日期滚轮 (DateScrollPicker) | 时间滚轮 (TimeScrollPicker) |
|------|-----|-----|
| 数据源 | `today ± 7天`（15项） | `00:00~23:55`，步长5分钟（288项） |
| 格式 | `MM月dd日` | `HH:mm` |
| 尺寸 | 宽96dp × 高200dp | 宽64dp × 高200dp |
| 每项高度 | 40dp | 40dp |
| 可见项数 | 5（中间选中） | 5 |
| 选中样式 | `primaryContainer` 背景 + `bodyLarge SemiBold` | 同左 |
| 未选中 | `bodySmall`，alpha 0.7→0.4→0.2（距离递增） | 同左 |
| 吸附 | `animateScrollToItem()` | 同左 |

### 4.2 快捷按钮

| 按钮 | 位置 | 行为 | 视觉反馈 |
|------|------|------|---------|
| [上尾] | 仅开始时间行 | 开始时间 = 上一个行为的 endTime | Ripple → 滚轮动画滚动 → 高亮闪烁300ms |
| [当前] | 开始/结束各一个 | 时间 = `LocalDateTime.now()` | 同上 |

快捷按钮 Chip 样式：高度28dp，圆角20dp，`labelSmall` 字号。[上尾] 使用 `surfaceContainerHighest` 背景；[当前] 使用 `primaryContainer` 背景。

---

## 5. 时间填充功能算法

### 5.1 概念模型

```
0:00 ─── 前一个行为 ───┤ 空闲 ├─── 当前行为 ───┤ 空闲 ├─── 后一个行为 ─── 24:00
                    endTime_A    startTime_B  endTime_B    startTime_C

填满左侧: startTime_B = endTime_A      填满右侧: endTime_B = startTime_C
```

### 5.2 "填满左侧空闲时间" 算法

1. 获取当天所有 ACTIVE/COMPLETED 行为，按 startTime 排序
2. 找到当前行为的索引 currentIndex
3. 若 currentIndex > 0：newStartTime = 前一个行为的 endTime（endTime 为 null 则用 startTime 兜底）
4. 若 currentIndex == 0：newStartTime = dayStart（当天00:00）
5. 更新 startTime，若行为是 ACTIVE 则自动切换为 COMPLETED + 设置 endTime
6. 更新 uiState，触发滚轮动画

### 5.3 "填满右侧空闲时间" 算法

1. 同上获取排序列表和当前索引
2. 若 currentIndex < size-1：newEndTime = 后一个行为的 startTime
3. 若 currentIndex == size-1：newEndTime = dayEnd（当天24:00）
4. 若 endTime 为 null（ACTIVE），先用当前时间兜底 + 切换为 COMPLETED
5. 更新 endTime，触发滚轮动画

### 5.4 最小持续时间保护

填充后若 `newStartTime >= newEndTime`（持续时间为0），强制设置最小持续时间 5 分钟：`newEndTime = newStartTime + 5分钟`。

---

## 6. 界面布局与视觉规范

### 6.1 整体布局

```
╭──────────────────────────────────────────╮
│              ═══ 拖拽手柄 ═══             │ ← ModalBottomSheet 自带
│  行为详情                       [✕]      │ ← 标题栏
│  🔥 深度工作                COMPLETED     │ ← 活动名+状态标签
│ ──────────── 活动 ────────────           │
│  [😊健身] [📚学习] [🎮游戏] ... +添加     │ ← ActivityPicker
│ ──────────── 关联标签 ────────────       │
│  [#深度] [#专注] ... +添加               │ ← TagPicker(关联)
│ ──────────── 所有标签 ────────────       │
│  [#紧急] [#日常] [#娱乐] ...             │ ← TagPicker(所有)
│ ──────────── 时间设置 ────────────       │
│  开始 [4月27日]:[14:30] [上尾][当前]     │ ← TimeSelectionSection
│  结束 [4月27日]:[16:45] [当前]           │
│  [← 填满左侧空闲] [填满右侧空闲 →]       │ ← TimeFillActions
│ ──────────── 备注 ────────────           │
│  [补充描述...________________]           │ ← NoteInput
│ ──────────── 操作 ────────────           │
│  [保存修改]              [🗑 删除]       │ ← 保存(filled) / 删除(outlined error)
╰──────────────────────────────────────────╯
```

### 6.2 各区域规范要点

**标题栏**："行为详情"左对齐，`titleMedium`，颜色 `onSurface`。

**Section 标题**：`labelMedium`，颜色 `primary`，上方间距16dp。点击可折叠/展开（v2）。

**ActivityPicker**：`FlowRow` 流式布局。选中态 `primaryContainer` + `primary` 边框，未选中 `surfaceContainerHigh` + `outlineVariant` 边框。

**TagPicker**：`FlowRow` 流式布局。选中态颜色20%alpha背景+对应色边框，未选中 `surfaceContainerHigh`。

**滚轮容器**：`RoundedCornerShape(12dp)`，`surfaceContainerHigh` 背景。

**操作按钮行**：无修改时"保存修改"变为"关闭"（直接关闭无保存）。保存中按钮显示 `CircularProgressIndicator` 并 disabled。

**状态视觉反馈**：加载中 → `LinearProgressIndicator`；保存失败 → Snackbar；保存成功 → Sheet关闭 + 主页即时刷新。

### 6.3 DeleteConfirmDialog

```
╭─────────────────────────╮
│    确认删除              │
│  确定要删除行为          │
│  "🔥 深度工作" 吗？      │
│  此操作不可撤销。         │
│       [取消] [确认删除]   │
╰─────────────────────────╯
```
样式：`AlertDialog`，圆角24dp。确认按钮 `error` 色。

---

## 7. 异常处理与边界情况

### 7.1 状态锁定矩阵

| 功能 | PENDING | ACTIVE | COMPLETED |
|------|:---:|:---:|:---:|
| 修改活动/标签/备注 | ✅ | ✅ | ✅ |
| 修改开始时间 | ✅ | ⚠️ ≥前一个行为endTime | ✅ |
| 修改结束时间 | ❌ | ⚠️ 自动切换COMPLETED | ✅ |
| [上尾] 快捷按钮 | ❌ 隐藏 | ✅ | ✅ |
| [当前](开始) | ✅ | ✅ | ✅ |
| [当前](结束) | ❌ 隐藏 | ✅ + 自动完成 | ✅ |
| 填满左/右空闲 | ❌ 禁用 | ⚠️ 填充后自动完成 | ✅ |
| 删除 | ✅ | ✅ | ✅ |
| 修改行为类型 | ✅ | ✅ | ✅ |

### 7.2 关键边界处理

**时间校验**：开始时间 > 结束时间 → 实时限制滚轮范围 + 保存时二次校验 + Toast提示。

**跨天时间**：允许，日期滚轮范围 ±7 天。

**时间重叠**：保存时检测与其他行为重叠 → 弹确认提示"此时间段与「xxx」行为重叠，仍要保存吗？"

**填充边界**：当天唯一行为 → 填满左侧=dayStart，填满右侧=dayEnd。前一个endTime为null → 用startTime兜底。

**并发保护**：详情页打开期间行为被外部删除 → 检测无效后关闭Sheet+提示。快速连续点击 → 防抖500ms。

**空状态**：无活动/标签时显示"暂无数据，点击+添加"提示。

### 7.3 错误处理统一策略

所有 Repository 操作包裹 try-catch，异常通过 `errorMessage` 字段传递到 UI 层（Snackbar 一次性消费后清除）。

### 7.4 无障碍（Accessibility）

| 元素 | contentDescription |
|------|-------------------|
| [上尾] | "将开始时间设置为上一个行为的结束时间" |
| [当前] | "将时间设置为当前时刻" |
| 滚轮选中项 | "4月27日，当前选中" |
| 填满左侧 | "填满左侧空闲时间，将开始时间提前" |
| 填满右侧 | "填满右侧空闲时间，将结束时间延后" |
| 删除按钮 | "删除当前行为" |

---

## 8. 实现难点分析

| 难点 | 说明 | 对策 |
|------|------|------|
| 自定义时间滚轮 | Material3 无内置日期+时分一体滚轮组件 | 基于 LazyColumn 自建，参考 Picker 组件实现模式，控制可见项数5项 + 手动吸附 |
| 滚轮吸附动画 | 手指抬起后需平滑吸附到最近项 | `animateScrollToItem()` + 计算最近项索引，动画时长 200ms |
| 快捷按钮触发滚轮滚动 | 点击后两个滚轮需并行滚动到目标位置 | `kotlinx.coroutines.launch` 内两个 `animateScrollToItem` 并行调用 |
| 时间填充算法 | 需查询相邻行为，计算空闲间隙 | Repository 层新增 `getHomeBehaviors` 已存在，可直接复用查询 + 内存排序计算 |
| 状态驱动的时间选择器联动 | 开始时间变化时结束时间滚轮下限需同步更新 | 通过 `derivedStateOf` 实时计算有效范围 |
| 行为类型切换的时间影响 | ACTIVE↔COMPLETED 切换时时间边界状态变化 | 在切换 nature 时同步更新 `showEndTime` 和快捷按钮可见性 |
| 详情页与主页数据一致性 | 保存/删除后主页需即时刷新 | HomeViewModel 中 `loadHomeBehaviors()` 已在 init 中通过 Flow 订阅，保存后自动触发 |
| 组件复用与 AddSheet 重构 | 需从 AddSheet 中提取通用组件但不影响其功能 | 组件只拆不删，AddSheet 改为组合调用，变更最小化 |
| 跨天时间处理 | 昨日/明日行为的空闲间隙计算 | 查询时扩大 dayStart/dayEnd 范围至 ±1 天 |

---

## 9. 文件变更清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `feature/home/ui/sheet/TimeSelectionSection.kt` | 新增 | 可复用时间选择区域 |
| `feature/home/ui/sheet/DateScrollPicker.kt` | 新增 | 日期滚轮组件 |
| `feature/home/ui/sheet/TimeScrollPicker.kt` | 新增 | 时间滚轮组件 |
| `feature/home/ui/sheet/TimeFillActions.kt` | 新增 | 时间填充按钮组 |
| `feature/home/ui/sheet/DeleteConfirmDialog.kt` | 新增 | 删除确认弹窗 |
| `feature/home/ui/sheet/BehaviorDetailSheet.kt` | 新增 | 详情弹窗外壳 |
| `feature/home/ui/sheet/BehaviorDetailSheetContent.kt` | 新增 | 详情弹窗内容体 |
| `feature/home/model/HomeUiState.kt` | 修改 | 新增 `isDetailSheetVisible`、`detailBehavior` 等字段 |
| `feature/home/model/BehaviorDetailUiState.kt` | 新增 | 详情 UI 状态数据类 |
| `feature/home/viewmodel/HomeViewModel.kt` | 修改 | 新增 6 个方法 |
| `feature/home/ui/HomeScreen.kt` | 修改 | 新增长按回调 & DetailSheet 显示 |
| `feature/home/ui/HomeRoute.kt` | 修改 | 透传回调 |
| `feature/home/ui/components/GridCell.kt` | 修改 | 添加 `combinedClickable` 长按手势 |
| `feature/home/ui/components/TimeAxisGrid.kt` | 修改 | 透传 `onCellLongPress` |
| `core/data/repository/BehaviorRepository.kt` | 修改 | 新增 `updateBehavior`、`updateTagsForBehavior` 接口 |
| `core/data/repository/impl/BehaviorRepositoryImpl.kt` | 修改 | 实现新增方法 |
| `core/data/database/dao/BehaviorDao.kt` | 修改 | 新增 `update`、`updateTags` SQL 操作 |
