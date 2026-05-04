# FAB 长按拖拽菜单 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将 AddBehaviorSheet 中的 gesture 长按拖拽逻辑复制到主页右下角 MorphingFab，使 FAB 支持长按拖拽弹出选项菜单，两个 FAB 状态使用不同选项集。

**架构：** 改造现有 `MorphingFab` composable，添加 `detectDragGestures` 手势检测和选项网格渲染。短按保留原有 onClick 行为，长按进入拖拽模式并弹出选项行。选项行仅在拖拽时渲染，使用 `onGloballyPositioned` 追踪碰撞检测。

**技术栈：** Kotlin, Jetpack Compose, `detectDragGestures`, `onGloballyPositioned`, `mutableStateMapOf`

---

## 文件结构

| 文件 | 职责 | 操作 |
|------|------|------|
| `feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt` | MorphingFab 改造：添加拖拽手势+选项网格 | 修改 |

---

### 任务 1：添加拖拽状态变量和导入

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt`

- [ ] **步骤 1：添加必要的导入**

在 HomeScreen.kt 顶部的 import 区域添加：

```kotlin
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Rect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
```

- [ ] **步骤 2：在 MorphingFab 函数中添加拖拽状态变量**

在 `MorphingFab` 函数体开头（`val cornerRadius = ...` 之前）添加：

```kotlin
val context = LocalContext.current
var isDragging by remember { mutableStateOf(false) }
var dragOffset by remember { mutableStateOf(Offset.Zero) }
var hoveredOption by remember { mutableStateOf<String?>(null) }
val optionsLayoutBounds = remember { mutableStateMapOf<String, Rect>() }
var buttonPositionInWindow by remember { mutableStateOf(Offset.Zero) }
var optionsRowHeight by remember { mutableFloatStateOf(0f) }
```

- [ ] **步骤 3：验证编译通过**

运行：`.\gradlew :feature:home:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt
git commit -m "feat(home): add drag state variables to MorphingFab"
```

---

### 任务 2：定义选项配置和替换 Surface onClick 为拖拽手势

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt`

- [ ] **步骤 1：在 MorphingFab 中添加选项配置**

在拖拽状态变量之后、`val cornerRadius = ...` 之前添加：

```kotlin
val options = if (hasActiveBehavior) {
    listOf("完成", "放弃", "特记", "+自定义")
} else {
    listOf("完成", "目标", "当前", "+自定义")
}
```

- [ ] **步骤 2：保留 Surface onClick 并添加 pointerInput 拖拽手势**

保留 Surface 的 `onClick`（短按仍触发原有行为），在 modifier 上添加 `.onGloballyPositioned`、`.offset` 和 `.pointerInput`。`onClick` 和 `detectDragGestures` 可以共存——tap 触发 onClick，拖拽触发 detectDragGestures（参考 AddBehaviorSheet 中 Gesture 按钮的实现）。

替换前：
```kotlin
Surface(
    modifier = Modifier,
    shape = RoundedCornerShape(cornerRadius),
    color = containerColor,
    contentColor = contentColor,
    shadowElevation = 4.dp,
    onClick = if (hasActiveBehavior) onCompleteClick else onAddClick,
)
```

替换后：
```kotlin
Surface(
    modifier = Modifier
        .onGloballyPositioned { layoutCoordinates ->
            buttonPositionInWindow = layoutCoordinates.positionInWindow()
        }
        .offset {
            IntOffset(
                dragOffset.x.roundToInt(),
                dragOffset.y.roundToInt()
            )
        }
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = {
                    isDragging = true
                },
                onDragEnd = {
                    if (hoveredOption != null) {
                        when (hoveredOption) {
                            "完成" -> {
                                if (hasActiveBehavior) {
                                    onCompleteClick()
                                } else {
                                    onAddClick()
                                }
                            }
                            else -> {
                                Toast.makeText(
                                    context,
                                    "触发功能: $hoveredOption",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    isDragging = false
                    dragOffset = Offset.Zero
                    hoveredOption = null
                },
                onDragCancel = {
                    isDragging = false
                    dragOffset = Offset.Zero
                    hoveredOption = null
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    dragOffset += dragAmount
                    val currentPointerPosition =
                        buttonPositionInWindow + dragOffset + change.position
                    val hit =
                        optionsLayoutBounds.entries.find { entry ->
                            entry.value.contains(currentPointerPosition)
                        }?.key
                    if (hit != hoveredOption) {
                        hoveredOption = hit
                    }
                }
            )
        },
    shape = RoundedCornerShape(cornerRadius),
    color = containerColor,
    contentColor = contentColor,
    shadowElevation = 4.dp,
    onClick = if (hasActiveBehavior) onCompleteClick else onAddClick,
)
```

注意：需要添加 `import androidx.compose.ui.unit.IntOffset` 和 `import kotlin.math.roundToInt`（如果尚未导入）。

- [ ] **步骤 3：验证编译通过**

运行：`.\gradlew :feature:home:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt
git commit -m "feat(home): add drag gesture detection to MorphingFab keeping onClick"
```

---

### 任务 3：添加拖拽时的选项网格渲染

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt`

- [ ] **步骤 1：将 MorphingFab 返回类型从 Surface 改为 Box 包裹**

当前 MorphingFab 直接返回 Surface。需要改为用 Box 包裹，以便在 FAB 上方渲染选项网格。

替换整个 MorphingFab 函数体结构：

替换前（Surface 直接作为返回值）：
```kotlin
Surface(
    modifier = Modifier
        ...
) {
    Row(...) {
        Icon(...)
        if (hasActiveBehavior) {
            Spacer(...)
            Text(...)
        }
    }
}
```

替换后（Box 包裹 Surface + 选项行）：
```kotlin
Box {
    Surface(
        modifier = Modifier
            .onGloballyPositioned { ... }
            .offset { ... }
            .pointerInput(Unit) { ... },
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = if (hasActiveBehavior) Icons.Default.Check else Icons.Default.Add,
                contentDescription = null,
            )
            if (hasActiveBehavior) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "完成行为",
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }

    if (isDragging) {
        val density = LocalDensity.current
        val gapPx = with(density) { 8.dp.toPx() }
        val optionsY = -optionsRowHeight - gapPx
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, optionsY.roundToInt()) }
                .onGloballyPositioned { coords ->
                    optionsRowHeight = coords.size.height.toFloat()
                },
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .onGloballyPositioned { layoutCoordinates ->
                            val position = layoutCoordinates.positionInWindow()
                            val size = layoutCoordinates.size
                            optionsLayoutBounds[option] = Rect(
                                position.x,
                                position.y,
                                position.x + size.width,
                                position.y + size.height
                            )
                        },
                    shape = RoundedCornerShape(8.dp),
                    color = if (hoveredOption == option)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 4.dp,
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hoveredOption == option)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
```

注意：需要添加 `import androidx.compose.ui.platform.LocalDensity`（如果尚未导入）。

- [ ] **步骤 2：验证编译通过**

运行：`.\gradlew :feature:home:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt
git commit -m "feat(home): add drag option grid overlay to MorphingFab"
```

---

### 任务 4：构建并验证完整功能

**文件：**
- 无新增修改

- [ ] **步骤 1：完整构建**

运行：`.\gradlew :feature:home:assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 2：检查 lint 警告**

运行：`.\gradlew :feature:home:lintDebug`
预期：无新增 error 级别问题

- [ ] **步骤 3：Commit（如有自动修复）**

```bash
git add -A
git commit -m "chore: fix lint issues from FAB drag menu implementation"
```
