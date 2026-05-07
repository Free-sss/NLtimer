# 时间轴/日志模式点击长按交互设计规格

## 概述

为主页时间轴模式（TIMELINE_REVERSE）和行为日志模式（LOG）的行为卡片添加与网格模式（GRID）一致的点击和长按交互行为，并修复时间轴模式中 0 秒空闲段意外显示的 bug。

## 交互行为（三种模式统一）

| 手势 | 网格模式（现有） | 时间轴模式（新增） | 日志模式（新增） |
|------|-----------------|-------------------|-----------------|
| 点击行为卡片 | 弹出 BehaviorDetailDialog | 弹出 BehaviorDetailDialog | 弹出 BehaviorDetailDialog |
| 长按行为卡片 | 触发 onCellLongClick → 编辑详情 Sheet | 触发 onCellLongClick → 编辑详情 Sheet | 触发 onCellLongClick → 编辑详情 Sheet |
| 点击空闲段 | N/A（保持现有 "+" 按钮） | N/A（保持现有 "+" 按钮） | N/A（无空闲段概念） |

## 变更范围

### 1. TimelineReverseView.kt

- 新增参数 `onCellLongClick: (GridCellUiState) -> Unit`
- 内部引入 `detailCell` 状态管理
- `TimelineBehaviorItem` 改用 `combinedClickable`（onClick → 详情Dialog, onLongClick → onCellLongClick）
- 复制 `BehaviorDetailDialog` 组件到文件内（与 GridRow 同源逻辑）
- **Bug 修复**：空闲间隔过滤——时长 < 1 分钟的空闲段不插入列表项

### 2. BehaviorLogView.kt

- 新增参数 `onCellLongClick: (GridCellUiState) -> Unit`
- 内部引入 `detailCell` 状态管理
- `BehaviorLogCard` 改用 `combinedClickable`（onClick → 详情Dialog, onLongClick → onCellLongClick）
- 复制 `BehaviorDetailDialog` 组件到文件内（与 GridRow 同源逻辑）

### 3. HomeScreen.kt

- `TimelineReverseView` 调用处传入 `onCellLongClick`
- `BehaviorLogView` 调用处传入 `onCellLongClick`

## 0 秒空闲段 Bug 修复

**根因**：两个相邻行为之间存在毫秒级间隙（如前一个行为 endTime 与后一个行为 startTime 之间有几毫秒差异），`Duration.between()` 计算出极短间隔，转换为显示文本时被格式化为 "0秒"。

**修复**：在 `TimelineReverseView` 计算 timelineItems 时，对空闲间隔增加最小阈值过滤：

```kotlin
if (prevEnd != null && currentStart != null && currentStart.isAfter(prevEnd)) {
    val gap = Duration.between(prevEnd, currentStart)
    if (gap.toMinutes() >= 1) {
        items.add(TimelineItemData.Idle(prevEnd, currentStart))
    }
}
```

## 不变更内容

- 空闲段的交互保持现状（时间轴保留 "+" 按钮，日志模式无空闲段）
- 网格模式的逻辑不做任何改动
- BehaviorDetailDialog 暂不提取为共享组件（代码量约100行，后续可优化）
