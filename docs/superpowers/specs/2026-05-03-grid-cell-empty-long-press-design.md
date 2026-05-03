# GridCellEmpty 长按展开菜单设计文档

## 背景

在首页网格时间轴中，`GridCellEmpty` 是空白单元格，当前短按会打开添加行为的底部弹窗（AddBehaviorSheet）。用户希望支持长按展开三种行为添加模式的选择菜单。

## 需求

- **短按**：保持现有行为，直接打开添加行为弹窗（完成模式）
- **长按（>500ms）**：单元格在原地展开为三个选项按钮
- 三个选项：【当前】【完成】【目标】
- 【完成】走现有逻辑，【当前】【目标】暂时 Toast 提示"功能开发中"
- 展开状态下点击单元格外区域 → 收起恢复为"+"状态

## 方案：原地展开三按钮

### 交互流程

1. 默认状态：显示 "+" 和 "添加行为"
2. 长按触发：内容切换为三个垂直排列的 TextButton
3. 点击选项后执行对应逻辑并自动收起
4. 点击外部收起：通过监听父级点击或定时自动收起

### 技术实现

- `GridCellEmpty` 内部管理展开状态：`var isExpanded by remember { mutableStateOf(false) }`
- 使用 `Modifier.pointerInput` 检测长按手势（detectTapGestures onLongPress）
- 使用 `AnimatedVisibility` + `animateContentSize` 实现平滑展开/收起动画
- 三个选项用 `Column` 垂直排列
- 收起逻辑：展开状态下监听点击事件，点击非按钮区域则收起

### 状态管理

```kotlin
var isExpanded by remember { mutableStateOf(false) }
```

- 展开时显示三个按钮
- 收起时显示默认 "+" 内容
- 点击按钮后自动收起

### UI 结构

```
GridCellEmpty (Column)
├── AnimatedVisibility (isExpanded)
│   └── Column (三个选项)
│       ├── TextButton("当前")
│       ├── TextButton("完成")
│       └── TextButton("目标")
└── AnimatedVisibility (!isExpanded)
    └── Column (默认内容)
        ├── Text("+")
        └── Text("添加行为")
```

### 回调签名

保持现有 `onClick: () -> Unit` 不变，内部根据选择的模式处理：
- 【完成】→ 调用 `onClick()`
- 【当前】→ Toast("当前模式开发中")
- 【目标】→ Toast("目标模式开发中")

### 视觉设计

- 展开后背景保持 `surfaceVariant.copy(alpha = 0.3f)`
- 三个按钮使用 `MaterialTheme.typography.labelSmall`
- 按钮文字颜色使用 `MaterialTheme.colorScheme.primary`
- 按钮间距 4.dp

## 影响范围

仅修改 `GridCellEmpty.kt` 文件，不改动：
- `GridRow.kt` 的调用方式
- `HomeScreen.kt` 或 `HomeRoute.kt`
- `HomeViewModel.kt`

## 测试要点

1. 短按仍然正常打开 AddBehaviorSheet
2. 长按正确展开三个选项
3. 点击【完成】正常打开 AddBehaviorSheet
4. 点击【当前】【目标】显示 Toast
5. 展开后点击外部区域能收起
