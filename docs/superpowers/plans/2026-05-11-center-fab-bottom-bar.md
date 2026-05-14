# Center FAB Bottom Bar 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 新增 `BottomBarMode.CENTER_FAB` 悬浮居中模式，将设置移至左侧、导航紧邻、FAB 独立居中。

**架构：** 在 `BottomBarMode` 枚举中新增值，创建新的底栏组合函数 `AppCenterFabBottomBar`，`BottomBarDragFab` 根据 `LocalTheme` 自动切换对齐位置，`NLtimerScaffold` 路由新底栏。

**技术栈：** Kotlin, Jetpack Compose, Material 3

---

## 文件结构

| 文件 | 操作 | 职责 |
|------|------|------|
| `core/designsystem/.../BottomBarMode.kt` | 修改 | 新增枚举值 |
| `core/designsystem/.../EnumExt.kt` | 修改 | 新增显示文本 |
| `core/designsystem/.../BottomBarDragFab.kt` | 修改 | FAB 位置自适应 |
| `app/.../AppBottomNavigation.kt` | 修改 | 新增 AppCenterFabBottomBar |
| `app/.../NLtimerScaffold.kt` | 修改 | 处理新模式路由 |

---

### 任务 1：枚举变更

**文件：**
- 修改：`core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/BottomBarMode.kt`
- 修改：`core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/EnumExt.kt:114-117`

- [ ] **步骤 1：BottomBarMode.kt 新增枚举值**

将 `BottomBarMode` 改为：

```kotlin
package com.nltimer.core.designsystem.theme

enum class BottomBarMode {
    STANDARD,
    FLOATING,
    CENTER_FAB,
}
```

- [ ] **步骤 2：EnumExt.kt 新增显示文本**

将 `BottomBarMode.toDisplayString()` 改为：

```kotlin
fun BottomBarMode.toDisplayString(): String = when (this) {
    BottomBarMode.STANDARD -> "标准"
    BottomBarMode.FLOATING -> "悬浮"
    BottomBarMode.CENTER_FAB -> "悬浮居中"
}
```

- [ ] **步骤 3：编译验证**

运行：`./gradlew :core:designsystem:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/BottomBarMode.kt core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/EnumExt.kt
git commit -m "feat: add CENTER_FAB to BottomBarMode enum"
```

---

### 任务 2：BottomBarDragFab 位置自适应

**文件：**
- 修改：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/BottomBarDragFab.kt`

- [ ] **步骤 1：添加 LocalTheme 导入并切换对齐方式**

添加导入：

```kotlin
import com.nltimer.core.designsystem.theme.BottomBarMode
import com.nltimer.core.designsystem.theme.LocalTheme
```

将 `BottomBarDragFab` 中的 `DragActionFab` 调用改为：

```kotlin
@Composable
fun BoxScope.BottomBarDragFab(
    state: DragFabState,
    icon: ImageVector,
    dragOptions: List<String>,
    modifier: Modifier = Modifier,
    label: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    cornerRadius: Dp = 28.dp,
    onClick: () -> Unit,
    onOptionSelected: (String) -> Unit,
) {
    val isCenterFab = LocalTheme.current.bottomBarMode == BottomBarMode.CENTER_FAB

    DragActionFab(
        state = state,
        icon = icon,
        label = label,
        containerColor = containerColor,
        contentColor = contentColor,
        cornerRadius = cornerRadius,
        onClick = onClick,
        onOptionSelected = onOptionSelected,
        modifier = modifier
            .align(if (isCenterFab) Alignment.BottomCenter else Alignment.BottomStart)
            .navigationBarsPadding()
            .then(
                if (isCenterFab) Modifier.padding(bottom = 8.dp)
                else Modifier.padding(start = 12.dp, bottom = 8.dp)
            ),
    )

    FabDragOptions(
        state = state,
        options = dragOptions,
    )
}
```

- [ ] **步骤 2：编译验证**

运行：`./gradlew :core:designsystem:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add core/designsystem/src/main/java/com/nltimer/core/designsystem/component/BottomBarDragFab.kt
git commit -m "feat: BottomBarDragFab adapt position for CENTER_FAB mode"
```

---

### 任务 3：新增 AppCenterFabBottomBar

**文件：**
- 修改：`app/src/main/java/com/nltimer/app/component/AppBottomNavigation.kt`

- [ ] **步骤 1：在文件末尾（`FloatingToolbarTab` 之后）添加 `AppCenterFabBottomBar` 组合函数**

```kotlin
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun AppCenterFabBottomBar(
    navController: NavHostController,
    onSettingsClick: () -> Unit,
    onSettingsLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentBackStackEntry by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(
        initialValue = navController.currentBackStackEntry,
    )
    val currentDestination = currentBackStackEntry?.destination

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(
            modifier = Modifier.align(Alignment.BottomStart)
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .combinedClickable(
                        onLongClick = onSettingsClick,
                        onClick = onSettingsLongClick,
                    ),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = CircleShape,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "菜单",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            HorizontalFloatingToolbar(
                expanded = true,
                colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
                    toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
            ) {
                navItems.filter { it.route != NLtimerRoutes.SETTINGS }.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    FloatingToolbarTab(
                        selected = selected,
                        icon = item.icon,
                        label = item.label,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        }
    }
}
```

需要额外导入（文件已有大部分）：

```kotlin
import androidx.compose.foundation.layout.Row
```

- [ ] **步骤 2：编译验证**

运行：`./gradlew :app:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add app/src/main/java/com/nltimer/app/component/AppBottomNavigation.kt
git commit -m "feat: add AppCenterFabBottomBar composable"
```

---

### 任务 4：NLtimerScaffold 集成新模式

**文件：**
- 修改：`app/src/main/java/com/nltimer/app/NLtimerScaffold.kt`

- [ ] **步骤 1：添加 AppCenterFabBottomBar 导入和 LocalTheme 判断**

在 import 区添加：

```kotlin
import com.nltimer.app.component.AppCenterFabBottomBar
```

- [ ] **步骤 2：修改 isFloating 判断逻辑，增加 isCenterFab**

将：

```kotlin
val isFloating = theme.bottomBarMode == BottomBarMode.FLOATING && !isSecondaryPage
```

改为：

```kotlin
val isFloating = theme.bottomBarMode == BottomBarMode.FLOATING && !isSecondaryPage
val isCenterFab = theme.bottomBarMode == BottomBarMode.CENTER_FAB && !isSecondaryPage
val isAnyFloating = isFloating || isCenterFab
```

- [ ] **步骤 3：更新 Scaffold 的 modifier 判断**

将：

```kotlin
if (!isSecondaryPage && !isFloating) Modifier.padding(bottom = 80.dp) else Modifier
```

改为：

```kotlin
if (!isSecondaryPage && !isAnyFloating) Modifier.padding(bottom = 80.dp) else Modifier
```

- [ ] **步骤 4：更新 NavHost 的 bottom padding**

将：

```kotlin
bottom = if (isFloating) 0.dp else if (!isSecondaryPage) padding.calculateBottomPadding() else 0.dp,
```

改为：

```kotlin
bottom = if (isAnyFloating) 0.dp else if (!isSecondaryPage) padding.calculateBottomPadding() else 0.dp,
```

- [ ] **步骤 5：更新底部导航栏条件判断**

将：

```kotlin
if (!isFloating && !isSecondaryPage) {
```

改为：

```kotlin
if (!isAnyFloating && !isSecondaryPage) {
```

- [ ] **步骤 6：在浮动底栏判断之后添加 isCenterFab 分支**

在现有 `if (isFloating) { ... }` 块之后添加：

```kotlin
if (isCenterFab) {
    AppCenterFabBottomBar(
        navController = navController,
        onSettingsClick = { showSettingsPopup = true },
        onSettingsLongClick = {
            navController.navigate(NLtimerRoutes.SETTINGS) {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
        modifier = Modifier.align(Alignment.BottomCenter),
    )
}
```

- [ ] **步骤 7：更新设置弹窗偏移量**

将：

```kotlin
popupOffsetY = if (isFloating) -160 else -100,
```

改为：

```kotlin
popupOffsetY = if (isAnyFloating) -160 else -100,
```

- [ ] **步骤 8：编译验证**

运行：`./gradlew :app:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 9：Commit**

```bash
git add app/src/main/java/com/nltimer/app/NLtimerScaffold.kt
git commit -m "feat: integrate CENTER_FAB mode in NLtimerScaffold"
```

---

### 任务 5：全量构建与验证

- [ ] **步骤 1：全量 debug 构建**

运行：`./gradlew assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 2：运行现有测试**

运行：`./gradlew test`
预期：所有测试通过

- [ ] **步骤 3：最终 Commit（如有格式修正）**

```bash
git add -A
git commit -m "chore: formatting and lint fixes for CENTER_FAB feature"
```
