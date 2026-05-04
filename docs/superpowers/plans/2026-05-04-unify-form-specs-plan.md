# 统一 AddActivity/AddTag 表单 Spec 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将 `ActivityFormSpecs` 从 debug 模块提升到 `core/designsystem`，所有模块引用同一 Spec 定义，统一增加活动/标签的 UI 字段。

**架构：** `core/designsystem` 提供 `ActivityFormSpecs` 单一来源 + `GenericFormDialog`/`GenericFormSheet` 容器；各模块引用共享 Spec，按上下文选择容器（AddBehaviorSheet 内用 Dialog，其他用 Sheet）。

**技术栈：** Kotlin, Jetpack Compose, FormSpec/GenericFormDialog/GenericFormSheet

---

## 文件结构

| 操作 | 文件 | 职责 |
|------|------|------|
| 新建 | `core/designsystem/src/main/java/com/nltimer/core/designsystem/form/ActivityFormSpecs.kt` | 活动/标签 FormSpec 单一来源 |
| 修改 | `feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/AddActivityDialog.kt` | 引用 core Spec，删除本地 spec |
| 修改 | `feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/AddTagDialog.kt` | 引用 core Spec，删除本地 spec |
| 修改 | `feature/management_activities/src/main/java/.../dialogs/ActivityFormSheets.kt` | 引用 core Spec，删除本地 createSpec/editSpec |
| 修改 | `feature/tag_management/src/main/java/.../dialogs/AddTagDialog.kt` | 引用 core Spec，删除本地 createTagSpec |
| 修改 | `feature/debug/src/main/java/.../preview/AddActivityPreview.kt` | 改引用 core ActivityFormSpecs |
| 修改 | `feature/debug/src/main/java/.../preview/AddTagPreview.kt` | 改引用 core ActivityFormSpecs |
| 修改 | `feature/debug/src/main/java/.../preview/EditActivityPreview.kt` | 改引用 core ActivityFormSpecs |
| 修改 | `feature/debug/src/main/java/.../preview/EditTagPreview.kt` | 改引用 core ActivityFormSpecs |
| 删除 | `feature/debug/src/main/java/.../preview/ActivityFormSpecs.kt` | 已提升到 core |

---

### 任务 1：在 core/designsystem 创建 ActivityFormSpecs

**文件：**
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/form/ActivityFormSpecs.kt`

- [ ] **步骤 1：创建 ActivityFormSpecs.kt**

```kotlin
package com.nltimer.core.designsystem.form

object ActivityFormSpecs {
    val createActivity = FormSpec(
        title = "增加活动",
        submitLabel = "增加活动",
        sections = listOf(
            FormSection(
                rows = listOf(
                    FormRow.IconColor(iconKey = "icon", colorKey = "color", initialEmoji = "📖"),
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
                    FormRow.LabelAction(key = "tags", label = "关联标签", actionText = "+ 增加"),
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
        sections = createActivity.sections + FormSection(
            rows = listOf(
                FormRow.Switch(key = "isArchived", label = "归档"),
            ),
        ),
    )

    val createTag = FormSpec(
        title = "新增标签",
        submitLabel = "新增标签",
        sections = listOf(
            FormSection(
                rows = listOf(
                    FormRow.IconColor(iconKey = "icon", colorKey = "color", initialEmoji = "🏷️"),
                ),
            ),
            FormSection(
                rows = listOf(
                    FormRow.TextInput(key = "name", label = "名称", placeholder = "请输入标签名"),
                    FormRow.NumberInput(key = "priority", label = "优先级", initialValue = 0, range = 0..99),
                ),
            ),
            FormSection(
                rows = listOf(
                    FormRow.LabelAction(key = "category", label = "所属分类", actionText = "未分类"),
                ),
            ),
        ),
    )

    fun editTag() = FormSpec(
        title = "编辑标签",
        submitLabel = "保存",
        sections = createTag.sections + FormSection(
            rows = listOf(
                FormRow.Switch(key = "isArchived", label = "归档"),
            ),
        ),
    )
}
```

- [ ] **步骤 2：验证编译**

运行：`.\gradlew.bat :core:designsystem:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add core/designsystem/src/main/java/com/nltimer/core/designsystem/form/ActivityFormSpecs.kt
git commit -m "♻️ refactor: extract ActivityFormSpecs to core/designsystem as single source of truth"
```

---

### 任务 2：更新 home 模块 AddActivityDialog

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/AddActivityDialog.kt`

- [ ] **步骤 1：替换本地 spec 为 core ActivityFormSpecs**

将整个文件替换为：

```kotlin
package com.nltimer.feature.home.ui.sheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.nltimer.core.designsystem.form.ActivityFormSpecs
import com.nltimer.core.designsystem.form.GenericFormDialog
import com.nltimer.core.designsystem.theme.NLtimerTheme

@Composable
fun AddActivityDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, emoji: String) -> Unit,
) {
    GenericFormDialog(
        spec = ActivityFormSpecs.createActivity,
        initialData = null,
        onDismiss = onDismiss,
        onSubmit = { formState ->
            val name = formState["name"]?.trim() ?: ""
            val emoji = formState["icon"]?.trim() ?: ""
            if (name.isNotBlank()) {
                onConfirm(name, emoji)
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun AddActivityDialogPreview() {
    NLtimerTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            AddActivityDialog(
                onDismiss = {},
                onConfirm = { _, _ -> }
            )
        }
    }
}
```

- [ ] **步骤 2：验证编译**

运行：`.\gradlew.bat :feature:home:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/AddActivityDialog.kt
git commit -m "♻️ refactor(home): AddActivityDialog uses core ActivityFormSpecs"
```

---

### 任务 3：更新 home 模块 AddTagDialog

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/AddTagDialog.kt`

- [ ] **步骤 1：替换本地 spec 为 core ActivityFormSpecs**

将整个文件替换为：

```kotlin
package com.nltimer.feature.home.ui.sheet

import androidx.compose.runtime.Composable
import com.nltimer.core.designsystem.form.ActivityFormSpecs
import com.nltimer.core.designsystem.form.GenericFormDialog

@Composable
fun AddTagDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit,
) {
    GenericFormDialog(
        spec = ActivityFormSpecs.createTag,
        initialData = null,
        onDismiss = onDismiss,
        onSubmit = { formState ->
            val name = formState["name"]?.trim() ?: ""
            if (name.isNotBlank()) {
                onConfirm(name)
            }
        },
    )
}
```

- [ ] **步骤 2：验证编译**

运行：`.\gradlew.bat :feature:home:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/AddTagDialog.kt
git commit -m "♻️ refactor(home): AddTagDialog uses core ActivityFormSpecs"
```

---

### 任务 4：更新 management_activities 模块 ActivityFormSheets

**文件：**
- 修改：`feature/management_activities/src/main/java/com/nltimer/feature/management_activities/ui/components/dialogs/ActivityFormSheets.kt`

- [ ] **步骤 1：替换本地 spec 为 core ActivityFormSpecs**

将文件中顶部的 `createSpec` 和 `editSpec()` 删除，改为引用 `ActivityFormSpecs`。关键变更点：

1. 删除 `private val createSpec = FormSpec(...)` 整个定义
2. 删除 `private fun editSpec() = FormSpec(...)` 整个定义
3. 添加 `import com.nltimer.core.designsystem.form.ActivityFormSpecs`
4. `AddActivityFormSheet` 中 `createSpec` → `ActivityFormSpecs.createActivity`
5. `EditActivityFormSheet` 中 `editSpec()` → `ActivityFormSpecs.editActivity()`

具体替换：

`specWithCategory` 中的 `createSpec.copy(` → `ActivityFormSpecs.createActivity.copy(`
`specWithCategory` 中的 `createSpec.sections.map` → `ActivityFormSpecs.createActivity.sections.map`

`specWithCategory` 中的 `editSpec().copy(` → `ActivityFormSpecs.editActivity().copy(`
`specWithCategory` 中的 `editSpec().sections.map` → `ActivityFormSpecs.editActivity().sections.map`

- [ ] **步骤 2：验证编译**

运行：`.\gradlew.bat :feature:management_activities:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add feature/management_activities/src/main/java/com/nltimer/feature/management_activities/ui/components/dialogs/ActivityFormSheets.kt
git commit -m "♻️ refactor(management_activities): ActivityFormSheets uses core ActivityFormSpecs"
```

---

### 任务 5：更新 tag_management 模块 AddTagDialog

**文件：**
- 修改：`feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/dialogs/AddTagDialog.kt`

- [ ] **步骤 1：替换本地 spec 为 core ActivityFormSpecs**

将整个文件替换为：

```kotlin
package com.nltimer.feature.tag_management.ui.components.dialogs

import androidx.compose.runtime.Composable
import com.nltimer.core.designsystem.form.ActivityFormSpecs
import com.nltimer.core.designsystem.form.GenericFormDialog

@Composable
fun AddTagDialog(
    initialCategory: String?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, color: Long?, icon: String?, priority: Int, category: String?) -> Unit,
) {
    val categoryName = initialCategory ?: "未分类"

    val specWithCategory = ActivityFormSpecs.createTag.copy(
        sections = ActivityFormSpecs.createTag.sections.map { section ->
            section.copy(
                rows = section.rows.map { row ->
                    if (row is com.nltimer.core.designsystem.form.FormRow.LabelAction && row.key == "category") {
                        row.copy(actionText = categoryName)
                    } else row
                },
            )
        },
    )

    GenericFormDialog(
        spec = specWithCategory,
        initialData = null,
        onDismiss = onDismiss,
        onSubmit = { formState ->
            val name = formState["name"]?.trim() ?: ""
            val icon = formState["icon"]?.trim()?.ifBlank { null }
            val colorHex = formState["color"]?.trim()?.ifBlank { null }
            val priority = formState["priority"]?.toIntOrNull() ?: 0
            val color = colorHex?.let {
                try { it.toLong(16).or(0xFF000000.toLong()) } catch (_: Exception) { null }
            }
            onConfirm(name, color, icon, priority, initialCategory)
        },
    )
}
```

- [ ] **步骤 2：验证编译**

运行：`.\gradlew.bat :feature:tag_management:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/dialogs/AddTagDialog.kt
git commit -m "♻️ refactor(tag_management): AddTagDialog uses core ActivityFormSpecs"
```

---

### 任务 6：更新 debug 模块 Preview 文件引用

**文件：**
- 修改：`feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/AddActivityPreview.kt`
- 修改：`feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/AddTagPreview.kt`
- 修改：`feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/EditActivityPreview.kt`
- 修改：`feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/EditTagPreview.kt`

- [ ] **步骤 1：更新 AddActivityPreview.kt**

将 `ActivityFormSpecs.create` 替换为 `ActivityFormSpecs.createActivity`（2 处）：

- 第 49 行：`ActivityFormSpecs.create.copy(` → `ActivityFormSpecs.createActivity.copy(`
- 第 50 行：`ActivityFormSpecs.create.sections.map` → `ActivityFormSpecs.createActivity.sections.map`

添加 import：`import com.nltimer.core.designsystem.form.ActivityFormSpecs`

- [ ] **步骤 2：更新 AddTagPreview.kt**

`ActivityFormSpecs.createTag` 名称不变，只需添加 import：

添加 import：`import com.nltimer.core.designsystem.form.ActivityFormSpecs`

- [ ] **步骤 3：更新 EditActivityPreview.kt**

`ActivityFormSpecs.editActivity()` 名称不变，只需添加 import：

添加 import：`import com.nltimer.core.designsystem.form.ActivityFormSpecs`

- [ ] **步骤 4：更新 EditTagPreview.kt**

`ActivityFormSpecs.editTag()` 名称不变，只需添加 import：

添加 import：`import com.nltimer.core.designsystem.form.ActivityFormSpecs`

- [ ] **步骤 5：验证编译**

运行：`.\gradlew.bat :feature:debug:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 6：Commit**

```bash
git add feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/AddActivityPreview.kt feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/AddTagPreview.kt feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/EditActivityPreview.kt feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/EditTagPreview.kt
git commit -m "♻️ refactor(debug): preview files use core ActivityFormSpecs"
```

---

### 任务 7：删除 debug 模块旧 ActivityFormSpecs

**文件：**
- 删除：`feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/ActivityFormSpecs.kt`

- [ ] **步骤 1：删除文件**

删除 `feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/ActivityFormSpecs.kt`

- [ ] **步骤 2：全量编译验证**

运行：`.\gradlew.bat :app:assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add -u feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/ActivityFormSpecs.kt
git commit -m "🔥 refactor(debug): remove local ActivityFormSpecs (moved to core)"
```

---

### 任务 8：最终验证

- [ ] **步骤 1：全量编译 + 安装**

运行：`.\gradlew.bat :app:assembleDebug; if ($LASTEXITCODE -eq 0) { adb -s ebc3de22 install -r "D:\2026Code\Group_android\NLtimer\app\build\outputs\apk\debug\app-debug.apk"; adb -s ebc3de22 shell am start -n "com.nltimer.app/com.nltimer.feature.debug.ui.DebugActivity" }`
预期：BUILD SUCCESSFUL，应用安装并启动

- [ ] **步骤 2：手动验证**

1. Debug 页 → Forms → "增加活动" → 字段含 icon/color/name/note/tags/category
2. Debug 页 → Forms → "新增标签" → 字段含 icon/color/name/priority/category
3. 首页 → AddBehaviorSheet → 点击"+ 活动" → 弹出 Dialog，字段同上
4. 首页 → AddBehaviorSheet → 点击"+ 标签" → 弹出 Dialog，字段同上
5. 侧边栏 → 活动管理 → FAB 添加 → 弹出 Sheet，字段同上
6. 侧边栏 → 标签管理 → 添加标签 → 弹出 Dialog，字段同上
