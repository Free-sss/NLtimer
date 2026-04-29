# Debug 通用表单弹窗 + 目录重构 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将 AddActivityDialogPreview 重构为 FormSpec 数据驱动的 GenericFormSheet，新增四种模式预览，重构 debug 模块目录为 model/ui/preview 分层

**架构：** FormSpec sealed class 定义表单行类型 → GenericFormSheet 动态渲染 → ActivityFormSpecs 工厂提供四种模式预设 → 各 Preview 文件组合触发按钮 + GenericFormSheet

**技术栈：** Kotlin, Jetpack Compose, Material3 ModalBottomSheet

---

## 任务 1：创建 FormSpec 数据模型

**文件：**
- 创建：`feature/debug/src/main/java/com/nltimer/feature/debug/model/FormSpec.kt`

- [ ] **步骤 1：创建 FormSpec 数据类和 sealed class 行类型**

```kotlin
package com.nltimer.feature.debug.model

import androidx.compose.runtime.Composable

data class FormSpec(
    val title: String,
    val submitLabel: String,
    val sections: List<FormSection>,
)

data class FormSection(
    val rows: List<FormRow>,
)

sealed class FormRow {
    data class TextInput(
        val key: String,
        val label: String,
        val placeholder: String,
        val initialValue: String = "",
    ) : FormRow()

    data class IconColor(
        val iconKey: String,
        val colorKey: String,
        val initialEmoji: String = "📖",
    ) : FormRow()

    data class LabelAction(
        val key: String,
        val label: String,
        val actionText: String,
        val showHelp: Boolean = false,
    ) : FormRow()
}
```

- [ ] **步骤 2：构建验证**

```bash
./gradlew :feature:debug:compileDebugKotlin
```

预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add feature/debug/src/main/java/com/nltimer/feature/debug/model/
git commit -m "✨ feat(debug-model): add FormSpec data model with sealed class row types"
```

---

## 任务 2：创建 GenericFormSheet 通用表单弹窗

**文件：**
- 创建：`feature/debug/src/main/java/com/nltimer/feature/debug/ui/GenericFormSheet.kt`

- [ ] **步骤 1：创建 GenericFormSheet.kt**

```kotlin
package com.nltimer.feature.debug.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nltimer.feature.debug.model.FormRow
import com.nltimer.feature.debug.model.FormSection
import com.nltimer.feature.debug.model.FormSpec

/**
 * 通用表单底部弹窗
 * 根据 [spec] 描述的表单结构动态渲染表单行，维护内部 [formState] Map，
 * 通过 [initialData] 区分新增/编辑模式，提交时通过 [onSubmit] 传出所有字段值。
 *
 * @param spec 表单结构描述
 * @param initialData 编辑模式的初始数据，新增模式传 null
 * @param onDismiss 弹窗关闭回调
 * @param onSubmit 提交回调，参数为字段 key→value 映射
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericFormSheet(
    spec: FormSpec,
    initialData: Map<String, String>?,
    onDismiss: () -> Unit,
    onSubmit: (Map<String, String>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 从 spec 提取所有 key 并建立默认值，编辑模式用 initialData 覆盖
    val formState = remember {
        val defaults = spec.defaultValues()
        if (initialData != null) {
            defaults.putAll(initialData)
        }
        mutableStateMapOf<String, String>().apply { putAll(defaults) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = spec.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // 遍历渲染每个分组
            spec.sections.forEachIndexed { index, section ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column {
                        section.rows.forEach { row ->
                            FormRowRenderer(
                                row = row,
                                formState = formState,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onSubmit(formState.toMap()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = spec.submitLabel,
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * 表单行渲染器
 * 根据 [row] 的具体类型分发到对应的渲染函数
 */
@Composable
private fun FormRowRenderer(
    row: FormRow,
    formState: MutableMap<String, String>,
) {
    when (row) {
        is FormRow.TextInput -> TextInputRenderer(
            row = row,
            value = formState[row.key] ?: row.initialValue,
            onValueChange = { formState[row.key] = it },
        )
        is FormRow.IconColor -> IconColorRenderer(
            row = row,
            emoji = formState[row.iconKey] ?: row.initialEmoji,
            colorValue = formState[row.colorKey] ?: "",
        )
        is FormRow.LabelAction -> LabelActionRenderer(row = row)
    }
}

@Composable
private fun TextInputRenderer(
    row: FormRow.TextInput,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(52.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = row.placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun IconColorRenderer(
    row: FormRow.IconColor,
    emoji: String,
    colorValue: String,
) {
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
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
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
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

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

/**
 * 从 FormSpec 提取所有行 key 的默认值
 */
private fun FormSpec.defaultValues(): Map<String, String> = buildMap {
    sections.forEach { section ->
        section.rows.forEach { row ->
            when (row) {
                is FormRow.TextInput -> put(row.key, row.initialValue)
                is FormRow.IconColor -> {
                    put(row.iconKey, row.initialEmoji)
                    put(row.colorKey, "")
                }
                is FormRow.LabelAction -> {} // LabelAction 不存储数据
            }
        }
    }
}
```

- [ ] **步骤 2：构建验证**

```bash
./gradlew :feature:debug:compileDebugKotlin
```

预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add feature/debug/src/main/java/com/nltimer/feature/debug/ui/GenericFormSheet.kt
git commit -m "✨ feat(debug-ui): add GenericFormSheet driven by FormSpec"
```

---

## 任务 3：移动现有 preview 文件 + 创建 ActivityFormSpecs 工厂

**文件：**
- 🚚 移动：`ui/DualTimePickerPreview.kt` → `ui/preview/DualTimePickerPreview.kt`
- 🚚 移动：`ui/TimeAdjustmentPreview.kt` → `ui/preview/TimeAdjustmentPreview.kt`
- ➕ 创建：`feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/ActivityFormSpecs.kt`

- [ ] **步骤 1：创建目标目录 + 移动 DualTimePickerPreview.kt**

工具操作（不能使用 bash mv）：
- 创建目录 `feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/`
- 读取 `ui/DualTimePickerPreview.kt` 完整内容
- 将 package 改为 `com.nltimer.feature.debug.ui.preview`
- 写入 `ui/preview/DualTimePickerPreview.kt`
- 删除 `ui/DualTimePickerPreview.kt`

```kotlin
// package 改为：
package com.nltimer.feature.debug.ui.preview
```

内容其余不变。

- [ ] **步骤 2：移动 TimeAdjustmentPreview.kt**

读取 `ui/TimeAdjustmentPreview.kt` 完整内容，package 改为 `com.nltimer.feature.debug.ui.preview`，写入 `ui/preview/TimeAdjustmentPreview.kt`，删除 `ui/TimeAdjustmentPreview.kt`。

- [ ] **步骤 3：创建 ActivityFormSpecs.kt**

```kotlin
package com.nltimer.feature.debug.ui.preview

import com.nltimer.feature.debug.model.FormRow
import com.nltimer.feature.debug.model.FormSection
import com.nltimer.feature.debug.model.FormSpec

object ActivityFormSpecs {
    val create = FormSpec(
        title = "增加活动",
        submitLabel = "增加活动",
        sections = listOf(
            FormSection(
                rows = listOf(
                    FormRow.IconColor(iconKey = "emoji", colorKey = "color", initialEmoji = "📖"),
                ),
            ),
            FormSection(
                rows = listOf(
                    FormRow.TextInput(key = "name", label = "名称", placeholder = "请输入"),
                    FormRow.TextInput(key = "note", label = "备注", placeholder = "请输入"),
                ),
            ),
            FormSection(
                rows = listOf(
                    FormRow.LabelAction(key = "tags", label = "关联标签", actionText = "+ 增加", showHelp = true),
                    FormRow.LabelAction(key = "keywords", label = "关键词", actionText = "+ 增加", showHelp = true),
                ),
            ),
            FormSection(
                rows = listOf(
                    FormRow.LabelAction(key = "category", label = "所属分类", actionText = "未分类"),
                ),
            ),
        ),
    )

    val createTag = FormSpec(
        title = "新增标签",
        submitLabel = "新增标签",
        sections = listOf(
            FormSection(
                rows = listOf(
                    FormRow.IconColor(iconKey = "emoji", colorKey = "color", initialEmoji = "🏷️"),
                ),
            ),
            FormSection(
                rows = listOf(
                    FormRow.TextInput(key = "name", label = "名称", placeholder = "请输入标签名"),
                ),
            ),
            FormSection(
                rows = listOf(
                    FormRow.LabelAction(key = "category", label = "所属分类", actionText = "未分类"),
                ),
            ),
        ),
    )

    fun editActivity() = FormSpec(
        title = "编辑活动",
        submitLabel = "保存",
        sections = create.sections,
    )

    fun editTag() = FormSpec(
        title = "编辑标签",
        submitLabel = "保存",
        sections = createTag.sections,
    )
}
```

- [ ] **步骤 4：构建验证**

```bash
./gradlew :feature:debug:compileDebugKotlin
```

预期：BUILD SUCCESSFUL

- [ ] **步骤 5：Commit**

```bash
git add feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/
git add feature/debug/src/main/java/com/nltimer/feature/debug/ui/DualTimePickerPreview.kt
git add feature/debug/src/main/java/com/nltimer/feature/debug/ui/TimeAdjustmentPreview.kt
git commit -m "✨ feat(debug): move previews to ui/preview + add ActivityFormSpecs factory"
```

---

## 任务 4：创建四种模式预览文件

**文件：**
- 创建：`feature/debug/.../ui/preview/AddActivityPreview.kt`
- 创建：`feature/debug/.../ui/preview/EditActivityPreview.kt`
- 创建：`feature/debug/.../ui/preview/AddTagPreview.kt`
- 创建：`feature/debug/.../ui/preview/EditTagPreview.kt`

- [ ] **步骤 1：创建 AddActivityPreview.kt**

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
import com.nltimer.feature.debug.ui.GenericFormSheet

@Composable
fun AddActivityPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        var showSheet by remember { mutableStateOf(true) }

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
                spec = ActivityFormSpecs.create,
                initialData = null,
                onDismiss = { showSheet = false },
                onSubmit = { showSheet = false },
            )
        }
    }
}
```

- [ ] **步骤 2：创建 EditActivityPreview.kt**

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
import com.nltimer.feature.debug.ui.GenericFormSheet

@Composable
fun EditActivityPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        var showSheet by remember { mutableStateOf(true) }

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
                spec = ActivityFormSpecs.editActivity(),
                initialData = mapOf("name" to "阅读", "note" to "每天30分钟", "emoji" to "📖"),
                onDismiss = { showSheet = false },
                onSubmit = { showSheet = false },
            )
        }
    }
}
```

- [ ] **步骤 3：创建 AddTagPreview.kt**

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
import com.nltimer.feature.debug.ui.GenericFormSheet

@Composable
fun AddTagPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        var showSheet by remember { mutableStateOf(true) }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "点击下方按钮打开新增标签弹窗",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { showSheet = true }) {
                    Text("打开新增标签弹窗")
                }
            }
        }

        if (showSheet) {
            GenericFormSheet(
                spec = ActivityFormSpecs.createTag,
                initialData = null,
                onDismiss = { showSheet = false },
                onSubmit = { showSheet = false },
            )
        }
    }
}
```

- [ ] **步骤 4：创建 EditTagPreview.kt**

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
import com.nltimer.feature.debug.ui.GenericFormSheet

@Composable
fun EditTagPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        var showSheet by remember { mutableStateOf(true) }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "点击下方按钮打开编辑标签弹窗",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { showSheet = true }) {
                    Text("打开编辑标签弹窗")
                }
            }
        }

        if (showSheet) {
            GenericFormSheet(
                spec = ActivityFormSpecs.editTag(),
                initialData = mapOf("name" to "工作", "emoji" to "💼"),
                onDismiss = { showSheet = false },
                onSubmit = { showSheet = false },
            )
        }
    }
}
```

- [ ] **步骤 5：构建验证**

```bash
./gradlew :feature:debug:compileDebugKotlin
```

预期：BUILD SUCCESSFUL

- [ ] **步骤 6：Commit**

```bash
git add feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/
git commit -m "✨ feat(debug): add four form-mode previews using GenericFormSheet"
```

---

## 任务 5：更新 FeatureDebugComponents 注册 + 删除旧文件

**文件：**
- 修改：`feature/debug/.../FeatureDebugComponents.kt` — 更新 import 和注册
- 删除：`feature/debug/.../ui/AddActivityDialogPreview.kt`

- [ ] **步骤 1：更新 FeatureDebugComponents.kt**

```kotlin
package com.nltimer.feature.debug

import com.nltimer.core.designsystem.debug.DebugComponent
import com.nltimer.core.designsystem.debug.DebugComponentRegistry
import com.nltimer.feature.debug.ui.preview.AddActivityPreview
import com.nltimer.feature.debug.ui.preview.AddTagPreview
import com.nltimer.feature.debug.ui.preview.DualTimePickerDebugPreview
import com.nltimer.feature.debug.ui.preview.EditActivityPreview
import com.nltimer.feature.debug.ui.preview.EditTagPreview
import com.nltimer.feature.debug.ui.preview.TimeAdjustmentDebugPreview

object FeatureDebugComponents {
    fun registerAll() {
        DebugComponentRegistry.register(
            DebugComponent(
                id = "DualTimePicker",
                name = "双列时间选择器",
                group = "Pickers",
                description = "左右双列日期+时分滚轮选择器",
            ) {
                DualTimePickerDebugPreview()
            }
        )
        DebugComponentRegistry.register(
            DebugComponent(
                id = "TimeAdjustment",
                name = "时间步进调节器",
                group = "Inputs",
                description = "水平步进式时间增减按钮组",
            ) {
                TimeAdjustmentDebugPreview()
            }
        )
        DebugComponentRegistry.register(
            DebugComponent(
                id = "AddActivity",
                name = "新增活动",
                group = "Forms",
                description = "新增活动表单，含图标/名称/备注/标签",
            ) {
                AddActivityPreview()
            }
        )
        DebugComponentRegistry.register(
            DebugComponent(
                id = "EditActivity",
                name = "编辑活动",
                group = "Forms",
                description = "编辑活动表单，预填模拟数据",
            ) {
                EditActivityPreview()
            }
        )
        DebugComponentRegistry.register(
            DebugComponent(
                id = "AddTag",
                name = "新增标签",
                group = "Forms",
                description = "新增标签表单，含图标/名称/分类",
            ) {
                AddTagPreview()
            }
        )
        DebugComponentRegistry.register(
            DebugComponent(
                id = "EditTag",
                name = "编辑标签",
                group = "Forms",
                description = "编辑标签表单，预填模拟数据",
            ) {
                EditTagPreview()
            }
        )
    }
}
```

- [ ] **步骤 2：删除 AddActivityDialogPreview.kt**

工具操作：删除 `feature/debug/src/main/java/com/nltimer/feature/debug/ui/AddActivityDialogPreview.kt`

- [ ] **步骤 3：构建验证**

```bash
./gradlew :feature:debug:compileDebugKotlin
```

预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add feature/debug/src/main/java/com/nltimer/feature/debug/FeatureDebugComponents.kt
git rm feature/debug/src/main/java/com/nltimer/feature/debug/ui/AddActivityDialogPreview.kt
git commit -m "✨ feat(debug): update registry for form previews + remove old dialog file"
```

---

## 任务 6：完整构建验证

- [ ] **步骤 1：Debug 构建**

```bash
./gradlew assembleDebug
```

预期：BUILD SUCCESSFUL

- [ ] **步骤 2：Release 构建**

```bash
./gradlew :app:compileReleaseKotlin
```

预期：BUILD SUCCESSFUL（zero residual）

---

## 自检结果

1. **规格覆盖度** ✅：
   - FormSpec 数据模型 → 任务1
   - GenericFormSheet → 任务2
   - 四种模式预览 → 任务4（AddActivity/EditActivity/AddTag/EditTag）
   - ActivityFormSpecs 工厂 → 任务3
   - 旧文件移动（DualTimePicker/TimeAdjustment）→ 任务3
   - 删除 AddActivityDialogPreview → 任务5
   - 更新 FeatureDebugComponents → 任务5
   - 构建验证 → 任务6

2. **占位符扫描** ✅ — 所有步骤包含完整代码

3. **类型一致性** ✅：
   - `FormSpec`/`FormSection`/`FormRow` 定义在任务1，后续所有引用一致
   - `GenericFormSheet` 签名在任务2定义，任务4中调用一致
   - `ActivityFormSpecs` 定义在任务3，任务4中引用一致
   - 所有 preview 函数签名 `@Composable fun XxxPreview()` 与注册调用一致
