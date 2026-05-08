# 字段详细调试工具实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 在 debug 包中为活动管理和标签管理添加字段详细调试弹窗，帮助开发者识别缺失字段

**架构：** 创建通用的 `FieldDetailDialog` 组件，在 `ActivityDetailSheet` 和 `EditTagFormSheet` 的右上角添加按钮，仅在 `BuildConfig.DEBUG` 为 true 时显示

**技术栈：** Jetpack Compose, Material3, kotlinx.serialization, Android BuildConfig

---

## 文件结构

| 文件路径 | 职责 |
|---------|------|
| `feature/debug/src/main/java/com/nltimer/feature/debug/ui/components/FieldDetailDialog.kt` | 通用字段详细弹窗组件 |
| `feature/debug/src/main/java/com/nltimer/feature/debug/ui/components/FieldInfo.kt` | 字段信息数据类和辅助函数 |
| `feature/management_activities/src/main/java/com/nltimer/feature/management_activities/ui/components/ActivityDetailSheet.kt` | 修改：添加字段详细按钮 |
| `feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/dialogs/EditTagFormSheet.kt` | 修改：添加字段详细按钮 |

---

## 任务 1：创建 FieldInfo 数据类和辅助函数

**文件：**
- 创建：`feature/debug/src/main/java/com/nltimer/feature/debug/ui/components/FieldInfo.kt`

- [ ] **步骤 1：创建 FieldInfo 数据类**

```kotlin
package com.nltimer.feature.debug.ui.components

/**
 * 字段信息数据类，用于描述单个字段的调试信息
 *
 * @param name 字段名，如 "name", "iconKey"
 * @param displayName 显示名，如 "名称", "图标"
 * @param value 字段值
 * @param isDisplayed 是否在 UI 上展示
 * @param isMissing 是否缺失（null/空/默认值）
 */
data class FieldInfo(
    val name: String,
    val displayName: String,
    val value: Any?,
    val isDisplayed: Boolean,
    val isMissing: Boolean,
)
```

- [ ] **步骤 2：创建缺失判断扩展函数**

在同一个文件中添加：

```kotlin
/**
 * 判断字段值是否为缺失状态
 * - null 值视为缺失
 * - 空字符串视为缺失
 * - Int 类型的 0 视为缺失（排除 id 字段）
 * - Boolean 类型的 false 视为缺失
 */
fun Any?.isFieldMissing(fieldName: String): Boolean = when (this) {
    null -> true
    is String -> isBlank()
    is Int -> this == 0 && !fieldName.endsWith("Id")
    is Boolean -> !this
    else -> false
}
```

- [ ] **步骤 3：创建 Activity 字段映射函数**

在同一个文件中添加：

```kotlin
import com.nltimer.core.data.model.Activity

/**
 * 将 Activity 对象转换为 FieldInfo 列表
 */
fun Activity.toFieldInfoList(): List<FieldInfo> = listOf(
    FieldInfo("id", "ID", id, isDisplayed = false, isMissing = id == 0L),
    FieldInfo("name", "名称", name, isDisplayed = true, isMissing = name.isFieldMissing("name")),
    FieldInfo("iconKey", "图标", iconKey, isDisplayed = true, isMissing = iconKey.isFieldMissing("iconKey")),
    FieldInfo("color", "颜色", color, isDisplayed = true, isMissing = color.isFieldMissing("color")),
    FieldInfo("keywords", "关键词", keywords, isDisplayed = false, isMissing = keywords.isFieldMissing("keywords")),
    FieldInfo("groupId", "分组", groupId, isDisplayed = false, isMissing = groupId.isFieldMissing("groupId")),
    FieldInfo("isPreset", "预设", isPreset, isDisplayed = false, isMissing = false),
    FieldInfo("isArchived", "归档", isArchived, isDisplayed = false, isMissing = false),
    FieldInfo("archivedAt", "归档时间", archivedAt, isDisplayed = false, isMissing = archivedAt.isFieldMissing("archivedAt")),
    FieldInfo("usageCount", "使用次数", usageCount, isDisplayed = true, isMissing = usageCount.isFieldMissing("usageCount")),
)
```

- [ ] **步骤 4：创建 Tag 字段映射函数**

在同一个文件中添加：

```kotlin
import com.nltimer.core.data.model.Tag

/**
 * 将 Tag 对象转换为 FieldInfo 列表
 */
fun Tag.toFieldInfoList(): List<FieldInfo> = listOf(
    FieldInfo("id", "ID", id, isDisplayed = false, isMissing = id == 0L),
    FieldInfo("name", "名称", name, isDisplayed = true, isMissing = name.isFieldMissing("name")),
    FieldInfo("color", "颜色", color, isDisplayed = true, isMissing = color.isFieldMissing("color")),
    FieldInfo("iconKey", "图标", iconKey, isDisplayed = true, isMissing = iconKey.isFieldMissing("iconKey")),
    FieldInfo("category", "分类", category, isDisplayed = false, isMissing = category.isFieldMissing("category")),
    FieldInfo("priority", "优先级", priority, isDisplayed = false, isMissing = priority.isFieldMissing("priority")),
    FieldInfo("usageCount", "使用次数", usageCount, isDisplayed = true, isMissing = usageCount.isFieldMissing("usageCount")),
    FieldInfo("sortOrder", "排序", sortOrder, isDisplayed = false, isMissing = false),
    FieldInfo("keywords", "关键词", keywords, isDisplayed = false, isMissing = keywords.isFieldMissing("keywords")),
    FieldInfo("isArchived", "归档", isArchived, isDisplayed = false, isMissing = false),
    FieldInfo("archivedAt", "归档时间", archivedAt, isDisplayed = false, isMissing = archivedAt.isFieldMissing("archivedAt")),
)
```

- [ ] **步骤 5：Commit**

```bash
git add feature/debug/src/main/java/com/nltimer/feature/debug/ui/components/FieldInfo.kt
git commit -m "feat(debug): add FieldInfo data class and field mapping functions"
```

---

## 任务 2：创建 FieldDetailDialog 组件

**文件：**
- 创建：`feature/debug/src/main/java/com/nltimer/feature/debug/ui/components/FieldDetailDialog.kt`

- [ ] **步骤 1：创建 FieldDetailDialog 基本结构**

```kotlin
package com.nltimer.feature.debug.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 字段详细调试弹窗
 *
 * @param title 弹窗标题，如 "活动字段详情"
 * @param fields 字段信息列表
 * @param rawJson 完整的 JSON 字符串
 * @param onDismiss 关闭弹窗的回调
 */
@Composable
fun FieldDetailDialog(
    title: String,
    fields: List<FieldInfo>,
    rawJson: String,
    onDismiss: () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("渲染", "原生")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, tabTitle ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(tabTitle) },
                        )
                    }
                }

                when (selectedTab) {
                    0 -> RenderTab(fields = fields)
                    1 -> NativeTab(rawJson = rawJson)
                }
            }
        },
        confirmButton = {},
    )
}
```

- [ ] **步骤 2：创建 RenderTab 组件**

在同一个文件中添加：

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.font.FontWeight

/**
 * 渲染 Tab 内容
 *
 * 显示当前 UI 上展示的字段，以及缺失字段的汇总
 */
@Composable
private fun RenderTab(fields: List<FieldInfo>) {
    val displayedFields = fields.filter { it.isDisplayed }
    val missingFields = fields.filter { it.isMissing }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 显示 UI 展示的字段
        items(displayedFields) { field ->
            FieldRow(field = field)
        }

        // 缺失字段汇总区域
        if (missingFields.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                MissingFieldsSummary(missingFields = missingFields)
            }
        }
    }
}
```

- [ ] **步骤 3：创建 FieldRow 组件**

在同一个文件中添加：

```kotlin
/**
 * 单个字段行
 */
@Composable
private fun FieldRow(field: FieldInfo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = field.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = field.value?.toString() ?: "—",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (field.isMissing) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
    )
}
```

- [ ] **步骤 4：创建 MissingFieldsSummary 组件**

在同一个文件中添加：

```kotlin
/**
 * 缺失字段汇总区域
 */
@Composable
private fun MissingFieldsSummary(missingFields: List<FieldInfo>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp),
            )
            .padding(12.dp),
    ) {
        Text(
            text = "缺失字段",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = missingFields.joinToString(", ") { it.displayName },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}
```

- [ ] **步骤 5：创建 NativeTab 组件**

在同一个文件中添加：

```kotlin
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily

/**
 * 原生 Tab 内容
 *
 * 显示完整的 JSON 字符串，支持复制到剪贴板
 */
@Composable
private fun NativeTab(rawJson: String) {
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
    ) {
        Button(
            onClick = {
                clipboardManager.setText(AnnotatedString(rawJson))
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("复制全部")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = rawJson,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(12.dp),
        )
    }
}
```

- [ ] **步骤 6：Commit**

```bash
git add feature/debug/src/main/java/com/nltimer/feature/debug/ui/components/FieldDetailDialog.kt
git commit -m "feat(debug): add FieldDetailDialog component with render and native tabs"
```

---

## 任务 3：修改 ActivityDetailSheet 添加字段详细按钮

**文件：**
- 修改：`feature/management_activities/src/main/java/com/nltimer/feature/management_activities/ui/components/ActivityDetailSheet.kt`

- [ ] **步骤 1：添加必要的 import**

在文件顶部添加：

```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import com.nltimer.core.data.model.Activity
import com.nltimer.feature.debug.ui.components.FieldDetailDialog
import com.nltimer.feature.debug.ui.components.toFieldInfoList
```

- [ ] **步骤 2：添加 showFieldDetail 状态**

在 `ActivityDetailSheet` 函数内部，`val sheetState = ...` 之后添加：

```kotlin
var showFieldDetail by remember { mutableStateOf(false) }
```

- [ ] **步骤 3：添加字段详细按钮**

找到现有的 `Row` 中的 `IconButton(onClick = { onEdit(activity) })`，在其前面添加：

```kotlin
if (BuildConfig.DEBUG) {
    IconButton(onClick = { showFieldDetail = true }) {
        Icon(
            Icons.Default.Info,
            contentDescription = "字段详细",
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}
```

- [ ] **步骤 4：添加 FieldDetailDialog 调用**

在 `ModalBottomSheet` 的闭合大括号之后，添加：

```kotlin
if (showFieldDetail) {
    val fields = remember(activity) { activity.toFieldInfoList() }
    val rawJson = remember(activity) {
        buildString {
            append("{\n")
            append("  \"id\": ${activity.id},\n")
            append("  \"name\": \"${activity.name}\",\n")
            append("  \"iconKey\": ${activity.iconKey?.let { "\"$it\"" } ?: "null"},\n")
            append("  \"keywords\": ${activity.keywords?.let { "\"$it\"" } ?: "null"},\n")
            append("  \"groupId\": ${activity.groupId ?: "null"},\n")
            append("  \"isPreset\": ${activity.isPreset},\n")
            append("  \"isArchived\": ${activity.isArchived},\n")
            append("  \"archivedAt\": ${activity.archivedAt ?: "null"},\n")
            append("  \"color\": ${activity.color ?: "null"},\n")
            append("  \"usageCount\": ${activity.usageCount}\n")
            append("}")
        }
    }

    FieldDetailDialog(
        title = "活动字段详情",
        fields = fields,
        rawJson = rawJson,
        onDismiss = { showFieldDetail = false },
    )
}
```

- [ ] **步骤 5：添加 BuildConfig import**

在文件顶部添加：

```kotlin
import com.nltimer.app.BuildConfig
```

- [ ] **步骤 6：Commit**

```bash
git add feature/management_activities/src/main/java/com/nltimer/feature/management_activities/ui/components/ActivityDetailSheet.kt
git commit -m "feat(activities): add field detail button to ActivityDetailSheet in debug mode"
```

---

## 任务 4：修改 EditTagFormSheet 添加字段详细按钮

**文件：**
- 修改：`feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/dialogs/EditTagFormSheet.kt`

- [ ] **步骤 1：添加必要的 import**

在文件顶部添加：

```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import com.nltimer.app.BuildConfig
import com.nltimer.core.data.model.Tag
import com.nltimer.feature.debug.ui.components.FieldDetailDialog
import com.nltimer.feature.debug.ui.components.toFieldInfoList
```

- [ ] **步骤 2：添加 showFieldDetail 状态**

在 `EditTagFormSheet` 函数内部适当位置添加：

```kotlin
var showFieldDetail by remember { mutableStateOf(false) }
```

- [ ] **步骤 3：添加字段详细按钮**

在 Sheet 的标题栏或操作栏区域添加按钮：

```kotlin
if (BuildConfig.DEBUG) {
    IconButton(onClick = { showFieldDetail = true }) {
        Icon(
            Icons.Default.Info,
            contentDescription = "字段详细",
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}
```

- [ ] **步骤 4：添加 FieldDetailDialog 调用**

在 Sheet 组件的闭合大括号之后，添加：

```kotlin
if (showFieldDetail) {
    val fields = remember(tag) { tag.toFieldInfoList() }
    val rawJson = remember(tag) {
        buildString {
            append("{\n")
            append("  \"id\": ${tag.id},\n")
            append("  \"name\": \"${tag.name}\",\n")
            append("  \"color\": ${tag.color ?: "null"},\n")
            append("  \"iconKey\": ${tag.iconKey?.let { "\"$it\"" } ?: "null"},\n")
            append("  \"category\": ${tag.category?.let { "\"$it\"" } ?: "null"},\n")
            append("  \"priority\": ${tag.priority},\n")
            append("  \"usageCount\": ${tag.usageCount},\n")
            append("  \"sortOrder\": ${tag.sortOrder},\n")
            append("  \"keywords\": ${tag.keywords?.let { "\"$it\"" } ?: "null"},\n")
            append("  \"isArchived\": ${tag.isArchived},\n")
            append("  \"archivedAt\": ${tag.archivedAt ?: "null"}\n")
            append("}")
        }
    }

    FieldDetailDialog(
        title = "标签字段详情",
        fields = fields,
        rawJson = rawJson,
        onDismiss = { showFieldDetail = false },
    )
}
```

- [ ] **步骤 5：Commit**

```bash
git add feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/dialogs/EditTagFormSheet.kt
git commit -m "feat(tags): add field detail button to EditTagFormSheet in debug mode"
```

---

## 任务 5：验证和测试

**文件：**
- 无新文件，仅验证

- [ ] **步骤 1：编译检查**

```bash
cd .worktrees/field-detail-debug
./gradlew assembleDebug
```

预期：BUILD SUCCESSFUL

- [ ] **步骤 2：运行 lint 检查**

```bash
./gradlew lintDebug
```

预期：无新增 error 级别问题

- [ ] **步骤 3：运行单元测试**

```bash
./gradlew testDebugUnitTest
```

预期：所有测试通过

- [ ] **步骤 4：Commit 最终版本**

```bash
git add -A
git commit -m "chore: finalize field detail debug feature"
```

---

## 自检清单

✅ **规格覆盖度：**
- 字段详细按钮在右上角 ✅
- 仅在 debug 模式显示 ✅
- 渲染 tab 显示 UI 字段和缺失汇总 ✅
- 原生 tab 显示完整 JSON ✅
- 复制全部功能 ✅
- 支持 Activity 和 Tag ✅

✅ **占位符扫描：** 无 TODO、待定或未完成的章节

✅ **类型一致性：**
- `FieldInfo` 在所有任务中保持一致
- `toFieldInfoList()` 扩展函数签名一致
- `FieldDetailDialog` 参数一致

---

## 执行方式

**计划已完成并保存到 `docs/superpowers/plans/2026-05-08-field-detail-debug-plan.md`。两种执行方式：**

**1. 子代理驱动（推荐）** - 每个任务调度一个新的子代理，任务间进行审查，快速迭代

**2. 内联执行** - 在当前会话中使用 executing-plans 执行任务，批量执行并设有检查点

**选哪种方式？**
