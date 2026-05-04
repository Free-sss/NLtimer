# FAB 长按拖拽菜单设计

## 概述

将 AddBehaviorSheet 中的 gesture 长按拖拽逻辑复制到主页右下角 MorphingFab，使 FAB 支持长按拖拽弹出选项菜单。两个 FAB 状态使用不同的选项集。

## 交互流程

```
FAB 长按 → 进入拖拽模式 → 上方弹出1行4个选项
  → 拖拽到选项上松手 → 触发对应操作
  → 短按（无拖拽）→ 保留原有行为（完成行为 / 打开添加弹窗）
  → 拖拽取消/松手在空白处 → FAB 回原位
```

## 选项配置

### 【+】FAB（无活跃行为）

| 选项1 | 选项2 | 选项3 | 选项4 |
|-------|-------|-------|-------|
| 完成 | 目标 | 当前 | +自定义 |

### 【完成行为】FAB（有活跃行为）

| 选项1 | 选项2 | 选项3 | 选项4 |
|-------|-------|-------|-------|
| 完成 | 放弃 | 特记 | +自定义 |

### 功能实现状态

| 选项 | 行为 | 状态 |
|------|------|------|
| 完成 | 完成当前活跃行为 / 创建已完成行为 | ✅ 已实现 |
| 目标 | 打开添加行为弹窗(nature=PENDING) | 🔲 Toast 占位 |
| 当前 | 打开添加行为弹窗(nature=ACTIVE) | 🔲 Toast 占位 |
| 放弃 | 放弃当前活跃行为 | 🔲 Toast 占位 |
| 特记 | 为当前行为添加特殊标记 | 🔲 Toast 占位 |
| +自定义 | 自定义操作 | 🔲 Toast 占位 |

## 布局规格

- 选项使用一行4个的 Row 布局，每个选项 `weight(1f)` 等宽
- 选项行渲染在 FAB 正上方，间距 8dp
- 选项仅在 `isDragging` 时渲染
- 未来扩展：新行加在上方，向下兼容

## 技术实现

### 改造 MorphingFab

从 AddBehaviorSheet 复制以下逻辑到 MorphingFab：

1. `dragOffset: Offset` — 拖拽偏移量
2. `hoveredOption: String?` — 当前悬停的选项
3. `optionsLayoutBounds: mutableStateMapOf<String, Rect>` — 选项位置追踪
4. `buttonPositionInWindow: Offset` — 按钮在窗口中的位置
5. `optionsRowHeight: Float` — 选项行高度
6. `detectDragGestures` — 手势检测

### 手势处理

- `pointerInput` + `detectDragGestures` 替代当前 `onClick`
- 长按触发拖拽，短按保留原有 onClick 行为
- 拖拽时 FAB 跟随手指偏移（`offset { IntOffset(dragOffset.x, dragOffset.y) }`）
- 拖拽时实时检测手指位置与选项的碰撞（`optionsLayoutBounds.contains()`）
- 悬停选项高亮（`hoveredOption` 控制颜色）
- 松手时若有悬停选项则触发操作，否则取消

### 短按与长按区分

使用 `detectDragGestures` 的 `onDragStart` 作为长按判定。如果未进入拖拽就松手，视为短按，执行原有 onClick 逻辑。

### 选项渲染

```kotlin
if (isDragging) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(0, optionsY.roundToInt()) }
            .onGloballyPositioned { ... },
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            Surface(
                modifier = Modifier.weight(1f).onGloballyPositioned { ... },
                shape = RoundedCornerShape(8.dp),
                color = if (hoveredOption == option) primary else surfaceVariant
            ) {
                Box(modifier = Modifier.padding(vertical = 12.dp)) {
                    Text(option)
                }
            }
        }
    }
}
```

## 涉及文件

| 文件 | 改动 |
|------|------|
| `HomeScreen.kt` (MorphingFab) | 主要改动：添加拖拽手势和选项网格 |
| `AddBehaviorSheet.kt` | 参考源：复制拖拽逻辑，不修改 |

## 不做的事

- 不修改 AddBehaviorSheet 中现有的 Gesture 按钮
- 不新增文件
- 不实现占位选项的实际功能（仅 Toast）
