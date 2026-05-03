# GridCellEmpty 长按展开菜单实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 为 GridCellEmpty 添加长按展开三种行为模式（当前/完成/目标）选择菜单的功能。

**架构：** 在 GridCellEmpty 内部用状态管理展开/收起，通过 pointerInput 检测长按手势，使用 AnimatedVisibility 切换默认内容和菜单内容。

**技术栈：** Jetpack Compose, Material3

---

## 文件结构

| 文件 | 职责 |
|------|------|
| `feature/home/src/main/java/com/nltimer/feature/home/ui/components/GridCellEmpty.kt` | 唯一修改文件。添加长按检测、展开状态、菜单 UI |

---

## 任务 1：修改 GridCellEmpty 添加长按展开菜单

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/components/GridCellEmpty.kt`

### 步骤 1：添加必要导入

在文件顶部现有 import 后添加：

```kotlin
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
```

### 步骤 2：修改 GridCellEmpty 实现

将 `GridCellEmpty` 函数体替换为以下实现：

```kotlin
@Composable
fun GridCellEmpty(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .animateContentSize()
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
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (isExpanded) {
                            isExpanded = false
                        } else {
                            onClick()
                        }
                    },
                    onLongPress = {
                        isExpanded = true
                    },
                )
            }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TextButton(
                    onClick = {
                        Toast.makeText(context, "当前模式开发中", Toast.LENGTH_SHORT).show()
                        isExpanded = false
                    },
                ) {
                    Text(
                        text = "当前",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                TextButton(
                    onClick = {
                        onClick()
                        isExpanded = false
                    },
                ) {
                    Text(
                        text = "完成",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                TextButton(
                    onClick = {
                        Toast.makeText(context, "目标模式开发中", Toast.LENGTH_SHORT).show()
                        isExpanded = false
                    },
                ) {
                    Text(
                        text = "目标",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !isExpanded,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Column(
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
        }
    }
}
```

### 步骤 3：构建验证

运行：
```bash
./gradlew.bat :feature:home:compileDebugKotlin
```

预期：BUILD SUCCESSFUL

### 步骤 4：Commit

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/components/GridCellEmpty.kt
git commit -m "feat: GridCellEmpty 长按展开行为模式选择菜单

- 长按展开【当前】【完成】【目标】三个选项
- 短按保持原有添加行为逻辑
- 【当前】【目标】暂时 Toast 提示开发中"
```

---

## 自检

1. **规格覆盖度：** 规格中所有需求均已覆盖：短按保持、长按展开、三个选项、占位符 Toast、点击外部收起
2. **占位符扫描：** 无 TODO/待定/后续实现
3. **类型一致性：** 使用现有 `onClick: () -> Unit` 签名，无需修改调用方
