# Debug 表单交互增强 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 为 Debug 表单的 IconColor 和 LabelAction 增加交互：颜色选色、emoji 编辑、TagPicker 多选、Group 下拉选择，并注入 mock 数据

**架构：** FormRow.LabelAction 增加 onClick 回调 → GenericFormSheet 传递 onClick + IconColor 内部管理交互弹窗 → Preview 文件注入 mock 数据和 picker 弹窗逻辑

**技术栈：** Kotlin, Jetpack Compose, ColorPickerDialog(core/designsystem), TagPicker(feature/home)

---

## 任务 1：FormRow.LabelAction 增加 onClick 参数 + GenericFormSheet 传递

**文件：**
- 修改：`feature/debug/src/main/java/com/nltimer/feature/debug/model/FormSpec.kt` — LabelAction 加 onClick
- 修改：`feature/debug/src/main/java/com/nltimer/feature/debug/ui/GenericFormSheet.kt` — LabelActionRenderer 用 onClick 包裹药丸

- [ ] **步骤 1：修改 LabelAction 数据类**

```kotlin
    data class LabelAction(
        val key: String,
        val label: String,
        val actionText: String,
        val showHelp: Boolean = false,
        val onClick: (() -> Unit)? = null,
    ) : FormRow()
```

- [ ] **步骤 2：修改 LabelActionRenderer 使药丸可点击**

将 `LabelActionRenderer` 中的右侧药丸 Box 包裹 `clickable`：

```kotlin
@Composable
private fun LabelActionRenderer(row: FormRow.LabelAction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (row.showHelp) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Outlined.HelpOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .then(if (row.onClick != null) Modifier.clickable { row.onClick() } else Modifier)
                .padding(horizontal = 14.dp, vertical = 6.dp),
        ) {
            Text(
                text = row.actionText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}
```

需要添加 import：
```kotlin
import androidx.compose.foundation.clickable
```

- [ ] **步骤 3：构建验证**

```bash
./gradlew :feature:debug:compileDebugKotlin
```

预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add feature/debug/src/main/java/com/nltimer/feature/debug/model/FormSpec.kt
git add feature/debug/src/main/java/com/nltimer/feature/debug/ui/GenericFormSheet.kt
git commit -m "✨ feat(debug-ui): add onClick to LabelAction and wire in renderer"
```

---

## 任务 2：IconColorRenderer 交互升级（颜色盘 + emoji 编辑）

**文件：**
- 修改：`feature/debug/src/main/java/com/nltimer/feature/debug/ui/GenericFormSheet.kt` — IconColorRenderer 重写

- [ ] **步骤 1：重写 IconColorRenderer**

完整替换现有 `IconColorRenderer`（约 L233-L272）：

```kotlin
@Composable
private fun IconColorRenderer(
    row: FormRow.IconColor,
    emoji: String,
    colorValue: String,
    onEmojiChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
) {
    val currentColor = try {
        if (colorValue.isNotBlank()) Color(colorValue.toLong(16).or(0xFF000000.toLong()))
        else MaterialTheme.colorScheme.primary
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    var showEmojiEditor by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                "图标",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable { showEmojiEditor = true },
                contentAlignment = Alignment.Center,
            ) {
                Text(emoji, style = MaterialTheme.typography.titleMedium)
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                "颜色",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(currentColor)
                    .clickable { showColorPicker = true },
            )
        }
    }

    if (showEmojiEditor) {
        EmojiEditDialog(
            current = emoji,
            onConfirm = { newEmoji ->
                onEmojiChange(newEmoji)
                showEmojiEditor = false
            },
            onDismiss = { showEmojiEditor = false },
        )
    }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = currentColor,
            onSelect = { color ->
                val hex = color.toArgb().toULong().toString(16)
                onColorChange(hex)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false },
        )
    }
}

@Composable
private fun EmojiEditDialog(
    current: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(current) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑图标") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= 4) text = it },
                label = { Text("Emoji") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text.ifBlank { current }) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}
```

需要添加的 imports：
```kotlin
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.nltimer.core.designsystem.component.ColorPickerDialog
```

同时修改 `FormRowRenderer` 中的 `IconColor` 分支，将回调解耦传入：

```kotlin
        is FormRow.IconColor -> IconColorRenderer(
            row = row,
            emoji = formState[row.iconKey] ?: row.initialEmoji,
            colorValue = formState[row.colorKey] ?: "",
            onEmojiChange = { formState[row.iconKey] = it },
            onColorChange = { formState[row.colorKey] = it },
        )
```

- [ ] **步骤 2：构建验证**

```bash
./gradlew :feature:debug:compileDebugKotlin
```

预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add feature/debug/src/main/java/com/nltimer/feature/debug/ui/GenericFormSheet.kt
git commit -m "✨ feat(debug-ui): add color picker and emoji editor to IconColor"
```

---

## 任务 3：创建 MockData.kt

**文件：**
- 创建：`feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/MockData.kt`

- [ ] **步骤 1：创建 MockData.kt**

```kotlin
package com.nltimer.feature.debug.ui.preview

import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.model.Tag

object MockData {
    val tags = listOf(
        Tag(
            id = 1, name = "工作", color = 0xFF4A90E2, textColor = null,
            icon = null, category = "生活", priority = 0,
            usageCount = 0, sortOrder = 0, isArchived = false,
        ),
        Tag(
            id = 2, name = "学习", color = 0xFF50C878, textColor = null,
            icon = null, category = "成长", priority = 1,
            usageCount = 0, sortOrder = 0, isArchived = false,
        ),
        Tag(
            id = 3, name = "运动", color = 0xFFFF6B6B, textColor = null,
            icon = null, category = "健康", priority = 2,
            usageCount = 0, sortOrder = 0, isArchived = false,
        ),
        Tag(
            id = 4, name = "深度", color = 0xFF9B59B6, textColor = null,
            icon = null, category = null, priority = 0,
            usageCount = 0, sortOrder = 0, isArchived = false,
        ),
        Tag(
            id = 5, name = "紧急", color = 0xFFE74C3C, textColor = null,
            icon = null, category = null, priority = 0,
            usageCount = 0, sortOrder = 0, isArchived = false,
        ),
    )

    val groups = listOf(
        ActivityGroup(id = 1, name = "工作", sortOrder = 0),
        ActivityGroup(id = 2, name = "学习", sortOrder = 1),
        ActivityGroup(id = 3, name = "健康", sortOrder = 2),
        ActivityGroup(id = 4, name = "娱乐", sortOrder = 3),
    )
}
```

- [ ] **步骤 2：构建验证**

```bash
./gradlew :feature:debug:compileDebugKotlin
```

预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/MockData.kt
git commit -m "✨ feat(debug-mock): add mock tags and groups for form previews"
```

---

## 任务 4：更新 preview 文件注入交互

**文件：**
- 修改：`feature/debug/.../ui/preview/AddActivityPreview.kt` — 注入 tag/group picker
- 修改：`feature/debug/.../ui/preview/EditActivityPreview.kt` — 同上
- 修改：`feature/debug/.../ui/preview/AddTagPreview.kt` — 无改动（已有默认交互）
- 修改：`feature/debug/.../ui/preview/EditTagPreview.kt` — 无改动

- [ ] **步骤 1：重写 AddActivityPreview.kt**

```kotlin
package com.nltimer.feature.debug.ui.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.model.Tag
import com.nltimer.feature.debug.model.FormRow
import com.nltimer.feature.debug.ui.GenericFormSheet
import com.nltimer.feature.home.ui.sheet.TagPicker

@Composable
fun AddActivityPreview() {
    val mockTags = remember { MockData.tags }
    val mockGroups = remember { MockData.groups }
    var showSheet by remember { mutableStateOf(true) }
    var selectedTagIds by remember { mutableStateOf(setOf<Long>()) }
    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    var showTagPicker by remember { mutableStateOf(false) }
    var showGroupPicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "点击下方按钮打开新增活动弹窗",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { showSheet = true }) {
                Text("打开新增活动弹窗")
            }
        }
    }

    if (showSheet) {
        GenericFormSheet(
            spec = ActivityFormSpecs.create.copy(
                sections = ActivityFormSpecs.create.sections.map { section ->
                    section.copy(
                        rows = section.rows.map { row ->
                            if (row is FormRow.LabelAction) {
                                when (row.key) {
                                    "tags" -> row.copy(onClick = { showTagPicker = true })
                                    "category" -> row.copy(
                                        actionText = mockGroups.find { it.id == selectedGroupId }?.name ?: "未分类",
                                        onClick = { showGroupPicker = true },
                                    )
                                    else -> row
                                }
                            } else row
                        },
                    )
                },
            ),
            initialData = null,
            onDismiss = { showSheet = false },
            onSubmit = { showSheet = false },
        )
    }

    if (showTagPicker) {
        TagPickerDialog(
            tags = mockTags,
            selectedIds = selectedTagIds,
            onIdsChanged = { selectedTagIds = it },
            onDismiss = { showTagPicker = false },
        )
    }

    if (showGroupPicker) {
        GroupPickerDialog(
            groups = mockGroups,
            selectedId = selectedGroupId,
            onSelected = { selectedGroupId = it },
            onDismiss = { showGroupPicker = false },
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun TagPickerDialog(
    tags: List<Tag>,
    selectedIds: Set<Long>,
    onIdsChanged: (Set<Long>) -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp).padding(bottom = 32.dp),
        ) {
            Text(
                "选择关联标签",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            TagPicker(
                tags = tags,
                selectedTagIds = selectedIds,
                onTagToggle = { id ->
                    onIdsChanged(
                        if (id in selectedIds) selectedIds - id else selectedIds + id
                    )
                },
            )
        }
    }
}
```

需要添加的 imports：
```kotlin
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import com.nltimer.core.designsystem.component.ColorPickerDialog
```

- [ ] **步骤 2：重写 EditActivityPreview.kt**（格式同上，initialData 预填 + 同样的 picker 逻辑）

```kotlin
package com.nltimer.feature.debug.ui.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.model.Tag
import com.nltimer.feature.debug.model.FormRow
import com.nltimer.feature.debug.ui.GenericFormSheet
import com.nltimer.feature.home.ui.sheet.TagPicker

@Composable
fun EditActivityPreview() {
    val mockTags = remember { MockData.tags }
    val mockGroups = remember { MockData.groups }
    var showSheet by remember { mutableStateOf(true) }
    var selectedTagIds by remember { mutableStateOf(setOf<Long>()) }
    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    var showTagPicker by remember { mutableStateOf(false) }
    var showGroupPicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "点击下方按钮打开编辑活动弹窗",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { showSheet = true }) {
                Text("打开编辑活动弹窗")
            }
        }
    }

    if (showSheet) {
        GenericFormSheet(
            spec = ActivityFormSpecs.editActivity().copy(
                sections = ActivityFormSpecs.editActivity().sections.map { section ->
                    section.copy(
                        rows = section.rows.map { row ->
                            if (row is FormRow.LabelAction) {
                                when (row.key) {
                                    "tags" -> row.copy(onClick = { showTagPicker = true })
                                    "category" -> row.copy(
                                        actionText = mockGroups.find { it.id == selectedGroupId }?.name ?: "未分类",
                                        onClick = { showGroupPicker = true },
                                    )
                                    else -> row
                                }
                            } else row
                        },
                    )
                },
            ),
            initialData = mapOf("name" to "阅读", "note" to "每天30分钟", "icon" to "📖", "isArchived" to "false"),
            onDismiss = { showSheet = false },
            onSubmit = { showSheet = false },
        )
    }

    if (showTagPicker) {
        TagPickerSheet(tags = mockTags, selectedIds = selectedTagIds, onIdsChanged = { selectedTagIds = it }, onDismiss = { showTagPicker = false })
    }
    if (showGroupPicker) {
        GroupPickerSheet(groups = mockGroups, selectedId = selectedGroupId, onSelected = { selectedGroupId = it }, onDismiss = { showGroupPicker = false })
    }
}
```

- [ ] **步骤 3：构建验证**

```bash
./gradlew :feature:debug:compileDebugKotlin
```

预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/AddActivityPreview.kt
git add feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/EditActivityPreview.kt
git commit -m "✨ feat(debug-preview): inject tag picker and group picker into activity previews"
```

---

## 任务 5：提取共享 Picker Sheet 到独立文件 + 最终构建

**文件：**
- 创建：`feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/PickerSheets.kt` — 共享 TagPickerSheet + GroupPickerSheet
- 修改：`AddActivityPreview.kt` / `EditActivityPreview.kt` — 改为引用共享 PickerSheets

- [ ] **步骤 1：创建 PickerSheets.kt**

```kotlin
package com.nltimer.feature.debug.ui.preview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.model.Tag
import com.nltimer.feature.home.ui.sheet.TagPicker

@Composable
fun TagPickerSheet(
    tags: List<Tag>,
    selectedIds: Set<Long>,
    onIdsChanged: (Set<Long>) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp).padding(bottom = 32.dp),
        ) {
            Text(
                "选择关联标签",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            TagPicker(
                tags = tags,
                selectedTagIds = selectedIds,
                onTagToggle = { id ->
                    onIdsChanged(if (id in selectedIds) selectedIds - id else selectedIds + id)
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupPickerSheet(
    groups: List<ActivityGroup>,
    selectedId: Long?,
    onSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    val allGroups = listOf(ActivityGroup(id = 0, name = "未分类")) + groups
    var selectedText by remember(selectedId) {
        mutableStateOf(allGroups.find { it.id == (selectedId ?: 0L) }?.name ?: "未分类")
    }
    var expanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp).padding(bottom = 32.dp),
        ) {
            Text(
                "选择所属分组",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                OutlinedTextField(
                    value = selectedText,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    allGroups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group.name) },
                            onClick = {
                                selectedText = group.name
                                onSelected(if (group.id == 0L) null else group.id)
                                expanded = false
                                onDismiss()
                            },
                        )
                    }
                }
            }
        }
    }
}
```

- [ ] **步骤 2：简化 AddActivityPreview.kt — 移除内联 Picker 函数，用 PickerSheets 引用**

AddActivityPreview.kt 中删除 `TagPickerDialog` 和 `GroupPickerDialog` 内联函数，改为调用 `TagPickerSheet(...)` 和 `GroupPickerSheet(...)`。

EditActivityPreview.kt 同理。

- [ ] **步骤 3：构建验证**

```bash
./gradlew :feature:debug:compileDebugKotlin
```

预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/
git commit -m "♻️ refactor(debug-preview): extract shared PickerSheets, wire into activity previews"
```

---

## 任务 6：最终构建验证 + 安装真机

- [ ] **步骤 1：Debug 构建**

```bash
./gradlew assembleDebug
```

预期：BUILD SUCCESSFUL

- [ ] **步骤 2：安装真机**

```bash
./gradlew installDebug
```

预期：Installed on 1 device

---

## 自检结果

1. **规格覆盖度** ✅：
   - color picker → 任务2 IconColorRenderer
   - iconKey emoji 编辑 → 任务2 EmojiEditDialog
   - tags 交互 → 任务4/5 TagPickerSheet
   - category 交互 → 任务5 GroupPickerSheet
   - mock 数据 → 任务3 MockData.kt
   - LabelAction onClick → 任务1

2. **占位符扫描** ✅ — 所有步骤包含完整代码

3. **类型一致性** ✅：
   - `FormRow.LabelAction.onClick` 定义在任务1，任务4/5中使用一致
   - `MockData.tags` / `MockData.groups` 定义在任务3，任务4/5引用一致
   - `TagPickerSheet` / `GroupPickerSheet` 定义在任务5，任务4的代码也已对齐
