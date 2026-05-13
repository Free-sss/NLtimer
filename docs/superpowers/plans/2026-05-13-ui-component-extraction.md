# UI 组件分层提取复用 — 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development 或 superpowers:dispatching-parallel-agents 逐任务实现此计划。

**目标：** 将分散在各 feature 模块的重复 UI 代码提取到 `core/designsystem`，按原子→业务→模板三层组织。

**架构：** 在 `core/designsystem/component/atom/` 下放置原子组件，`core/designsystem/component/` 下放置业务组件。逐个提取后在各 feature 模块中替换调用。

**技术栈：** Kotlin, Jetpack Compose, Material3, Hilt

**工作树：** `.worktrees/ui-component-extraction`，分支 `feature/ui-component-extraction`

**构建验证：** 每个任务完成后运行 `./gradlew assembleDebug` 确认编译通过。

---

## 任务依赖图

```
任务1(SelectableOptionChip) ─┬→ 任务4(PickerPopupContainer重构)
                             ├→ 任务5(ChipFlowSelector迁移)
任务2(TextInputDialog) ──────── 任务6(对话框迁移)
任务3(SettingsEntryCard) ────── 任务7(Settings迁移)
任务8(EmptyStateView) ────────── 任务9(空状态迁移)
任务10(ExpandableCard) ──────── 任务11(可展开卡片迁移)
任务12(ColorExt) ────────────── 任务13(颜色转换迁移)
```

任务 1-3、8、10、12 可并行执行（无依赖）。
任务 4-7、9、11、13 依赖前面的提取结果。

---

### 任务 1：SelectableOptionChip 原子组件

**文件：**
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/atom/SelectableOptionChip.kt`

- [ ] **步骤 1：创建 SelectableOptionChip**

```kotlin
package com.nltimer.core.designsystem.component.atom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SelectableOptionChip(
    text: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(10.dp),
) {
    Surface(
        onClick = onSelect,
        shape = shape,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerLow,
        border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.size(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
```

- [ ] **步骤 2：编译验证**

运行：`./gradlew :core:designsystem:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add core/designsystem/src/main/java/com/nltimer/core/designsystem/component/atom/SelectableOptionChip.kt
git commit -m "feat(designsystem): 提取 SelectableOptionChip 原子组件"
```

---

### 任务 2：TextInputDialog 业务组件

**文件：**
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/TextInputDialog.kt`

- [ ] **步骤 1：创建 TextInputDialog**

```kotlin
package com.nltimer.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.nltimer.core.designsystem.theme.appOutlinedTextFieldColors

@Composable
fun TextInputDialog(
    title: String,
    label: String,
    initialValue: String = "",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    validate: (String) -> Boolean = { it.isNotBlank() },
    confirmText: String = "确定",
    dismissText: String = "取消",
    enableCondition: ((String, String) -> Boolean)? = null,
) {
    var value by remember(initialValue) { mutableStateOf(initialValue) }
    val trimmed = value.trim()
    val isValid = validate(trimmed)
    val extraCheck = enableCondition?.invoke(trimmed, initialValue) ?: true

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = appOutlinedTextFieldColors(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(trimmed) },
                enabled = isValid && extraCheck,
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        modifier = modifier,
    )
}
```

- [ ] **步骤 2：编译验证**

运行：`./gradlew :core:designsystem:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add core/designsystem/src/main/java/com/nltimer/core/designsystem/component/TextInputDialog.kt
git commit -m "feat(designsystem): 提取 TextInputDialog 通用文本输入对话框"
```

---

### 任务 3：SettingsEntryCard 业务组件

**文件：**
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/SettingsEntryCard.kt`

- [ ] **步骤 1：创建 SettingsEntryCard**

```kotlin
package com.nltimer.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun SettingsEntryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        onClick = { if (!loading) onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
```

- [ ] **步骤 2：编译验证**

运行：`./gradlew :core:designsystem:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add core/designsystem/src/main/java/com/nltimer/core/designsystem/component/SettingsEntryCard.kt
git commit -m "feat(designsystem): 提取 SettingsEntryCard 设置入口卡片组件"
```

---

### 任务 4：重构 PickerPopup 使用 SelectableOptionChip

**文件：**
- 修改：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/GroupPickerPopup.kt`
- 修改：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/SingleSelectPickerPopup.kt`
- 修改：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/MultiSelectPickerPopup.kt`
- 修改：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/CategoryPickerPopup.kt`

- [ ] **步骤 1：重构 GroupPickerPopup** — 将 items forEach 块中的 Surface+Row+Icon+Text 替换为 `SelectableOptionChip` 调用。

- [ ] **步骤 2：重构 SingleSelectPickerPopup** — 同上替换。

- [ ] **步骤 3：重构 MultiSelectPickerPopup** — 同上替换。

- [ ] **步骤 4：重构 CategoryPickerPopup** — 同上替换。

- [ ] **步骤 5：编译验证**

运行：`./gradlew assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 6：Commit**

```bash
git add core/designsystem/src/main/java/com/nltimer/core/designsystem/component/GroupPickerPopup.kt core/designsystem/src/main/java/com/nltimer/core/designsystem/component/SingleSelectPickerPopup.kt core/designsystem/src/main/java/com/nltimer/core/designsystem/component/MultiSelectPickerPopup.kt core/designsystem/src/main/java/com/nltimer/core/designsystem/component/CategoryPickerPopup.kt
git commit -m "refactor(picker): Picker 系列弹窗改用 SelectableOptionChip 统一渲染"
```

---

### 任务 5：迁移 DialogConfigScreen 的 ChipFlowSelector/InlineToggleRow 使用 SelectableOptionChip

**文件：**
- 修改：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/DialogConfigScreen.kt`

- [ ] **步骤 1：重构 ChipFlowSelector** — 将 `Surface(onClick) { Text(...) }` 替换为 `SelectableOptionChip`（shape = RoundedCornerShape(8.dp)，注意需要调整 padding 和 typography）。

- [ ] **步骤 2：重构 InlineToggleRow** — 同上替换。

- [ ] **步骤 3：重构 LayoutWithStepperRow** — 布局选项 Surface 替换为 `SelectableOptionChip`（shape = RoundedCornerShape(8.dp)）。

- [ ] **步骤 4：编译验证**

运行：`./gradlew assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 5：Commit**

```bash
git add feature/settings/src/main/java/com/nltimer/feature/settings/ui/DialogConfigScreen.kt
git commit -m "refactor(settings): DialogConfigScreen 选项芯片改用 SelectableOptionChip"
```

---

### 任务 6：迁移所有 TextInputDialog 调用点

**文件：**
- 修改：`feature/management_activities/src/main/java/com/nltimer/feature/management_activities/ui/components/dialogs/AddGroupDialog.kt` — 改为调用 `TextInputDialog`
- 修改：`feature/management_activities/src/main/java/com/nltimer/feature/management_activities/ui/RenameGroupDialog.kt` — 改为调用 `TextInputDialog`
- 修改：`feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/dialogs/RenameCategoryDialog.kt` — 改为调用 `TextInputDialog`
- 修改：`feature/categories/src/main/java/com/nltimer/feature/categories/ui/CategoriesScreen.kt` — 内联的新建/重命名对话框替换为 `TextInputDialog`
- 修改：`feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/dialogs/AddCategoryDialog.kt` — 这个使用自定义动画样式，保留原样不迁移

- [ ] **步骤 1：AddGroupDialog 改用 TextInputDialog**

替换整个函数体为：
```kotlin
@Composable
fun AddGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    TextInputDialog(
        title = "新建分组",
        label = "分组名称",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}
```

- [ ] **步骤 2：RenameGroupDialog 改用 TextInputDialog**

替换为：
```kotlin
@Composable
fun RenameGroupDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    TextInputDialog(
        title = "重命名分组",
        label = "新名称",
        initialValue = currentName,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        enableCondition = { newName, old -> newName != old },
    )
}
```

- [ ] **步骤 3：RenameCategoryDialog 改用 TextInputDialog**

替换为：
```kotlin
@Composable
fun RenameCategoryDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (newName: String) -> Unit,
) {
    TextInputDialog(
        title = "重命名分类",
        label = "新名称",
        initialValue = currentName,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        confirmText = "保存",
        enableCondition = { newName, old -> newName.isNotBlank() && newName != old },
    )
}
```

- [ ] **步骤 4：CategoriesScreen 内联对话框改用 TextInputDialog** — 将 `showAddDialog` 和 `showRenameDialog` 状态分支中的 AlertDialog 替换为 TextInputDialog 调用。

- [ ] **步骤 5：编译验证**

运行：`./gradlew assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 6：Commit**

```bash
git add feature/management_activities/ feature/tag_management/ feature/categories/
git commit -m "refactor: 文本输入对话框统一改用 TextInputDialog"
```

---

### 任务 7：迁移 SettingsScreen/DataManagementScreen 使用 SettingsEntryCard

**文件：**
- 修改：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/SettingsScreen.kt` — 删除 private `SettingsEntryCard`，改用 designsystem 的
- 修改：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/DataManagementScreen.kt` — 删除 private `ActionCard`，改用 `SettingsEntryCard`

- [ ] **步骤 1：SettingsScreen** — 删除私有 `SettingsEntryCard` 函数，添加 `import com.nltimer.core.designsystem.component.SettingsEntryCard`，更新调用点（去掉 icon 参数中缺少的 click modifier，因为 Card 的 onClick 已内置）。

- [ ] **步骤 2：DataManagementScreen** — 删除私有 `ActionCard` 函数，替换调用为 `SettingsEntryCard(icon = ..., title = ..., subtitle = ..., loading = ..., onClick = ...)`。

- [ ] **步骤 3：编译验证**

运行：`./gradlew assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add feature/settings/
git commit -m "refactor(settings): 设置入口卡片统一改用 SettingsEntryCard"
```

---

### 任务 8：EmptyStateView / LoadingScreen 业务组件

**文件：**
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/EmptyStateView.kt`
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/LoadingScreen.kt`

- [ ] **步骤 1：创建 LoadingScreen**

```kotlin
package com.nltimer.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}
```

- [ ] **步骤 2：创建 EmptyStateView**

```kotlin
package com.nltimer.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun EmptyStateView(
    message: String,
    subtitle: String? = null,
    icon: ImageVector = Icons.Outlined.Inbox,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}
```

- [ ] **步骤 3：编译验证**

运行：`./gradlew :core:designsystem:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add core/designsystem/src/main/java/com/nltimer/core/designsystem/component/LoadingScreen.kt core/designsystem/src/main/java/com/nltimer/core/designsystem/component/EmptyStateView.kt
git commit -m "feat(designsystem): 提取 LoadingScreen 和 EmptyStateView 组件"
```

---

### 任务 9：迁移空状态/加载状态调用点

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt`
- 修改：`feature/management_activities/src/main/java/com/nltimer/feature/management_activities/ui/ActivityManagementScreen.kt`
- 修改：`feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/TagManagementScreen.kt`
- 修改：`feature/behavior_management/` 中的空状态

- [ ] **步骤 1-4：逐文件替换**

每个文件中：将加载状态 `Box { CircularProgressIndicator }` 替换为 `LoadingScreen()`；将空状态 Column 替换为 `EmptyStateView(message = "xxx", subtitle = "xxx")`。

- [ ] **步骤 5：编译验证**

运行：`./gradlew assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 6：Commit**

```bash
git add feature/home/ feature/management_activities/ feature/tag_management/ feature/behavior_management/
git commit -m "refactor: 加载/空状态视图统一改用 LoadingScreen 和 EmptyStateView"
```

---

### 任务 10：ExpandableCard 业务组件

**文件：**
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/ExpandableCard.kt`

- [ ] **步骤 1：创建 ExpandableCard**

```kotlin
package com.nltimer.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableCard(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess
                        else Icons.Default.ExpandMore,
                        contentDescription = null,
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    content()
                }
            }
        }
    }
}
```

- [ ] **步骤 2：编译验证**

运行：`./gradlew :core:designsystem:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add core/designsystem/src/main/java/com/nltimer/core/designsystem/component/ExpandableCard.kt
git commit -m "feat(designsystem): 提取 ExpandableCard 可展开卡片组件"
```

---

### 任务 11：迁移可展开卡片调用点

**文件：**
- 修改：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/DialogConfigScreen.kt` — 替换 `ConfigExpandableSection` 为 `ExpandableCard`
- 修改：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/DataManagementScreen.kt` — 替换 `ExpandableSection` 为 `ExpandableCard`

- [ ] **步骤 1：DialogConfigScreen** — 删除私有 `ConfigExpandableSection`，改为 `ExpandableCard(title = ..., expanded = ..., onToggle = ...) { ... }`。注意 DialogConfigScreen 中内容区的 padding 可能不同，需调整。

- [ ] **步骤 2：DataManagementScreen** — 删除私有 `ExpandableSection`，改为 `ExpandableCard`。注意 ExpandableSection 的内容区（Row of TextButtons）需要适配。

- [ ] **步骤 3：编译验证**

运行：`./gradlew assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add feature/settings/
git commit -m "refactor(settings): 可展开区域卡片统一改用 ExpandableCard"
```

---

### 任务 12：ColorExt 颜色工具扩展

**文件：**
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/ColorExt.kt`

- [ ] **步骤 1：创建 ColorExt**

```kotlin
package com.nltimer.core.designsystem.theme

import androidx.compose.ui.graphics.Color

fun Long?.toComposeColor(default: () -> Color): Color =
    this?.let { Color(it) } ?: default()
```

- [ ] **步骤 2：编译验证**

运行：`./gradlew :core:designsystem:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/ColorExt.kt
git commit -m "feat(designsystem): 添加 Long?.toComposeColor() 颜色工具扩展"
```

---

### 任务 13：迁移颜色转换调用点

**文件：**
- 修改：`feature/behavior_management/src/main/java/com/nltimer/feature/behavior_management/ui/BehaviorListItem.kt`
- 修改：`feature/behavior_management/src/main/java/com/nltimer/feature/behavior_management/ui/BehaviorTimelineItem.kt`

- [ ] **步骤 1：BehaviorListItem** — 将 `activity.color?.let { android.graphics.Color.valueOf(c)... }` 替换为 `activity.color.toComposeColor { MaterialTheme.colorScheme.primary }`

- [ ] **步骤 2：BehaviorTimelineItem** — 同上替换

- [ ] **步骤 3：编译验证**

运行：`./gradlew assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add feature/behavior_management/
git commit -m "refactor(behavior_management): 颜色转换改用 toComposeColor 扩展"
```

---

### 任务 14：最终编译验证

- [ ] **步骤 1：全量编译**

运行：`./gradlew assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 2：代码审查** — 检查所有迁移点无遗漏

- [ ] **步骤 3：最终 Commit（如有修复）**

```bash
git add -A
git commit -m "chore: UI 组件分层提取最终修复"
```
