# GridCellEmpty 弹出式长按菜单设计文档

## 背景

在首页网格时间轴中，`GridCellEmpty` 是空白单元格。用户希望长按后弹出 Android 样式的选项菜单，支持不抬手滑动选择。

## 需求

- **短按**：保持现有行为，直接打开添加行为弹窗（完成模式）
- **长按**：弹出 Android 样式的纵向选项菜单
- **不抬手滑动选择**：长按后手指不抬起，滑动到选项上时高亮，松开触发
- **智能弹出方向**：根据单元格在屏幕中的位置，自动决定向上或向下弹出，避免超出屏幕
- 三个选项：【当前】【完成】【目标】
- 【完成】走现有逻辑，【当前】【目标】暂时 Toast 提示"功能开发中"
- 抬手时不在任何选项上 → 取消菜单

## 方案：自定义 Popup + 手势追踪

### 交互流程

1. 默认状态：显示 "+" 和 "添加行为"
2. 短按：调用 `onClick()` 打开 AddBehaviorSheet
3. 长按（>400ms）：
   - 计算单元格在屏幕中的位置
   - 如果在屏幕上半部分 → 菜单向下弹出（`Alignment.TopCenter`）
   - 如果在屏幕下半部分 → 菜单向上弹出（`Alignment.BottomCenter`）
   - 显示 Popup 菜单覆盖层
4. 长按后不抬手滑动：
   - 手指移动到某个选项上时，该选项背景高亮
   - 离开选项时取消高亮
5. 松手（ACTION_UP）：
   - 如果在某个选项上 → 触发该选项并关闭菜单
   - 如果不在任何选项上 → 仅关闭菜单

### 技术实现

**手势处理：**
使用 `Modifier.pointerInput` 的 `awaitPointerEventScope` 手动处理完整手势流程：
- `awaitFirstDown()` 获取按下位置
- 启动一个延迟协程（400ms）检测长按
- 如果在延迟期间手指移动超过阈值 → 取消长按，视为普通滑动
- 如果长按触发 → 显示 Popup，进入追踪模式
- 追踪模式下持续读取 `pointerEvent` 获取手指位置
- `UP` 时计算落在哪个选项上并触发

**Popup 实现：**
- 使用 Compose 的 `Popup` composable
- `alignment` 根据弹出方向动态设置
- `offset` 微调位置使其紧贴单元格
- `onDismissRequest` 处理外部点击关闭

**菜单 UI：**
- 背景：`MaterialTheme.colorScheme.surface`
- 圆角：`8.dp`
- 阴影：`4.dp`
- 每个选项：高度 `48.dp`，横向居中
- 文字：`MaterialTheme.typography.bodyMedium`
- 高亮背景：`MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)`
- 选项间分隔线：`1.dp`，`outlineVariant` 色

**选项数据：**
```kotlin
data class MenuOption(
    val label: String,
    val onSelect: () -> Unit,
)
```

### 状态管理

```kotlin
var showMenu by remember { mutableStateOf(false) }
var popupDirection by remember { mutableStateOf(PopupDirection.DOWN) }
var hoveredIndex by remember { mutableStateOf(-1) }
```

### 坐标计算

- 使用 `onGloballyPositioned` 获取单元格在窗口中的坐标
- 屏幕中心线 = 屏幕高度 / 2
- 单元格中心 Y < 屏幕中心线 → 向下弹出
- 单元格中心 Y >= 屏幕中心线 → 向上弹出

### 碰撞检测

- 菜单显示后，记录每个选项在屏幕中的绝对坐标
- 手指位置与选项区域做矩形碰撞检测
- 确定当前悬停的选项索引

## 影响范围

仅修改 `GridCellEmpty.kt` 文件，不改动：
- `GridRow.kt` 的调用方式
- `HomeScreen.kt` 或 `HomeRoute.kt`
- `HomeViewModel.kt`

## 测试要点

1. 短按仍然正常打开 AddBehaviorSheet
2. 长按正确弹出菜单
3. 菜单弹出方向正确（上/下）
4. 滑动到选项上时高亮
5. 松手时正确触发选项
6. 松手时不在选项上仅关闭菜单
7. 菜单显示期间点击外部区域关闭菜单
