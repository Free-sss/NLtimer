# GridCellEmpty 弹出式长按菜单实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将 GridCellEmpty 的长按菜单改为弹出式 Popup，支持智能上下弹出方向和不抬手滑动选择。

**架构：** 在 GridCellEmpty 内部用 `Popup` composable 显示菜单，通过 `awaitPointerEventScope` 手动处理长按检测和手指位置追踪，实现不抬手滑动选择。

**技术栈：** Jetpack Compose, Material3

---

## 文件结构

| 文件 | 职责 |
|------|------|
| `feature/home/src/main/java/com/nltimer/feature/home/ui/components/GridCellEmpty.kt` | 唯一修改文件。替换原地展开为 Popup 菜单，实现手势追踪 |

---

## 任务 1：重写 GridCellEmpty 为弹出式菜单

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/components/GridCellEmpty.kt`

### 步骤 1：替换整个文件内容

```kotlin
package com.nltimer.feature.home.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.nltimer.core.designsystem.theme.appBorder
import kotlinx.coroutines.delay

/**
 * 表示可点击添加行为的空单元格 Composable。
 * 短按打开添加行为弹窗，长按弹出行为模式选择菜单。
 *
 * @param modifier 修饰符
 * @param onClick 点击回调（完成模式）
 */
@Composable
fun GridCellEmpty(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    var showMenu by remember { mutableStateOf(false) }
    var popupDirection by remember { mutableStateOf(PopupDirection.DOWN) }
    var hoveredIndex by remember { mutableIntStateOf(-1) }
    var cellOffset by remember { mutableStateOf(Offset.Zero) }
    var cellSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    // 菜单选项定义
    val options = remember {
        listOf(
            MenuOption("当前") {
                Toast.makeText(context, "当前模式开发中", Toast.LENGTH_SHORT).show()
            },
            MenuOption("完成") {
                onClick()
            },
            MenuOption("目标") {
                Toast.makeText(context, "目标模式开发中", Toast.LENGTH_SHORT).show()
            },
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                cellOffset = Offset(
                    coordinates.positionInWindow().x,
                    coordinates.positionInWindow().y,
                )
                cellSize = coordinates.size.toSize()
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown()
                        val downPosition = down.position

                        // 检测长按
                        var longPressTriggered = false
                        val longPressJob = kotlinx.coroutines.launch {
                            delay(400)
                            longPressTriggered = true

                            // 计算弹出方向
                            val screenHeight = size.height
                            val cellCenterY = cellOffset.y + cellSize.height / 2
                            popupDirection = if (cellCenterY < screenHeight / 2) {
                                PopupDirection.DOWN
                            } else {
                                PopupDirection.UP
                            }

                            showMenu = true
                            hoveredIndex = -1
                        }

                        // 等待手指抬起或移动
                        var moveToOptionIndex = -1
                        var upTriggered = false

                        while (!upTriggered) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Move -> {
                                    if (longPressTriggered && showMenu) {
                                        // 计算手指相对于菜单的位置
                                        val pointer = event.changes.first().position
                                        val menuY = if (popupDirection == PopupDirection.DOWN) {
                                            cellOffset.y + cellSize.height
                                        } else {
                                            cellOffset.y - (options.size * 48 + (options.size - 1)).dp.toPx()
                                        }
                                        val menuX = cellOffset.x + cellSize.width / 2 - 80.dp.toPx()

                                        val relativeY = pointer.y - menuY + downPosition.y
                                        val relativeX = pointer.x - menuX

                                        // 检测落在哪个选项上
                                        val optionHeight = 48.dp.toPx()
                                        val optionIndex = (relativeY / optionHeight).toInt()

                                        moveToOptionIndex = if (optionIndex in options.indices &&
                                            relativeX in 0f..(160.dp.toPx())
                                        ) {
                                            optionIndex
                                        } else {
                                            -1
                                        }
                                        hoveredIndex = moveToOptionIndex
                                    }
                                }

                                PointerEventType.Release -> {
                                    upTriggered = true
                                    longPressJob.cancel()

                                    if (longPressTriggered) {
                                        // 长按后抬起
                                        showMenu = false
                                        if (hoveredIndex in options.indices) {
                                            options[hoveredIndex].onSelect()
                                        }
                                    } else {
                                        // 短按
                                        onClick()
                                    }
                                }
                            }
                        }
                    }
                }
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(16.dp),
                )
                .appBorder(
                    borderProducer = {
                        BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "添加行为",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        // 弹出菜单
        if (showMenu) {
            val popupAlignment = if (popupDirection == PopupDirection.DOWN) {
                Alignment.TopCenter
            } else {
                Alignment.BottomCenter
            }

            Popup(
                alignment = popupAlignment,
                offset = with(density) {
                    androidx.compose.ui.unit.IntOffset(
                        x = 0,
                        y = if (popupDirection == PopupDirection.DOWN) {
                            cellSize.height.toInt() + 8.dp.roundToPx()
                        } else {
                            -(cellSize.height.toInt() + 8.dp.roundToPx())
                        },
                    )
                },
                properties = PopupProperties(
                    focusable = false,
                    dismissOnClickOutside = true,
                    dismissOnBackPress = true,
                ),
                onDismissRequest = { showMenu = false },
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    tonalElevation = 4.dp,
                    shadowElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.width(160.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        options.forEachIndexed { index, option ->
                            val isHovered = index == hoveredIndex
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .background(
                                        if (isHovered) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    )
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            if (index < options.size - 1) {
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class PopupDirection {
    UP, DOWN
}

private data class MenuOption(
    val label: String,
    val onSelect: () -> Unit,
)
```

### 步骤 2：构建验证

运行：
```bash
./gradlew.bat :feature:home:compileDebugKotlin
```

预期：BUILD SUCCESSFUL

### 步骤 3：Commit

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/components/GridCellEmpty.kt
git commit -m "feat: GridCellEmpty 弹出式长按菜单支持滑动选择

- 长按弹出 Android 样式选项菜单
- 智能判断弹出方向（上/下），避免超出屏幕
- 支持不抬手滑动选择，悬停高亮
- 【当前】【目标】占位 Toast 提示"
```

---

## 自检

1. **规格覆盖度：** 规格中所有需求均已覆盖：短按保持、长按弹出、智能方向、滑动高亮、不抬手触发、占位符 Toast
2. **占位符扫描：** 无 TODO/待定/后续实现
3. **类型一致性：** 使用现有 `onClick: () -> Unit` 签名，无需修改调用方
