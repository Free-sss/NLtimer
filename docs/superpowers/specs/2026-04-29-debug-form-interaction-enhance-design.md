# Debug 表单交互增强设计文档

**日期：** 2026-04-29
**状态：** ✅ 已批准
**架构方案：** IconColor 接入 ColorPickerDialog + emoji 编辑；LabelAction 升级为可交互弹窗；preview 文件注入 mock 数据

---

## 1. 目标与范围

实现 `重要-2026-04-29-debug-form-field-mapping.md` 中的 4 个"未实现"项：
- ✅ color picker：IconColor 颜色圆圈点击弹出 `ColorPickerDialog`
- ✅ iconKey：IconColor 图标圆圈点击弹出 emoji 编辑
- ✅ tags 真实交互：LabelAction 点击弹出 TagPicker 多选
- ✅ category 真实交互：LabelAction 点击弹出 Group 下拉选择

---

## 2. FormRow 模型变更

### 2.1 LabelAction 增加点击回调

```kotlin
data class LabelAction(
    val key: String,
    val label: String,
    val actionText: String,
    val showHelp: Boolean = false,
    val onClick: (() -> Unit)? = null,  // ★ 新增：回调由外部注入
) : FormRow()
```

### 2.2 IconColor 不变

颜色和图标交互在 `GenericFormSheet` 内部通过 State 直接处理，无需改模型。

---

## 3. GenericFormSheet 渲染器变更

### 3.1 IconColorRenderer 升级

```
──────────── 之前（静态）────────────
[📖]   [●]     ← 两个圆圈仅展示

──────────── 之后（可交互）────────────
[📖]  ← 点击弹出 emoji 编辑 AlertDialog
     [●]  ← 点击弹出 ColorPickerDialog（复用 core/designsystem）
```

实现方式：
- 内部 hold `showEmojiEditor` 和 `showColorPicker` 状态
- 颜色值用 `Color(compose)` 类型在 renderer 内部转换
- 将 `formState[colorKey]` 存为 ARGB 字符串（如 "0xFF4A90E2"），ColorPicker 选完后 parseColor 写回

### 3.2 LabelActionRenderer 升级

药丸按钮 `onClick = row.onClick`，点击后由外部 preview 文件弹出的弹窗接管。

---

## 4. Mock 数据设计

### 4.1 Mock Tags

```kotlin
val mockTags = listOf(
    Tag(id = 1, name = "工作", color = 0xFF4A90E2, textColor = null, icon = null,
        category = "生活", priority = 0, usageCount = 0, sortOrder = 0, isArchived = false),
    Tag(id = 2, name = "学习", color = 0xFF50C878, textColor = null, icon = null,
        category = "成长", priority = 1, usageCount = 0, sortOrder = 0, isArchived = false),
    Tag(id = 3, name = "运动", color = 0xFFFF6B6B, textColor = null, icon = null,
        category = "健康", priority = 2, usageCount = 0, sortOrder = 0, isArchived = false),
    Tag(id = 4, name = "深度", color = 0xFF9B59B6, textColor = null, icon = null,
        category = null, priority = 0, usageCount = 0, sortOrder = 0, isArchived = false),
    Tag(id = 5, name = "紧急", color = 0xFFE74C3C, textColor = null, icon = null,
        category = null, priority = 0, usageCount = 0, sortOrder = 0, isArchived = false),
)
```

### 4.2 Mock Groups

```kotlin
val mockGroups = listOf(
    ActivityGroup(id = 1, name = "工作", sortOrder = 0),
    ActivityGroup(id = 2, name = "学习", sortOrder = 1),
    ActivityGroup(id = 3, name = "健康", sortOrder = 2),
    ActivityGroup(id = 4, name = "娱乐", sortOrder = 3),
)
```

---

## 5. Preview 文件数据注入

### 5.1 AddActivityPreview / EditActivityPreview

```kotlin
@Composable
fun AddActivityPreview() {
    // mock 数据 + 交互状态
    val mockTags = remember { MockData.tags }
    val mockGroups = remember { MockData.groups }
    var selectedTagIds by remember { mutableStateOf(setOf<Long>()) }
    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    var showTagPicker by remember { mutableStateOf(false) }
    var showGroupPicker by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(Color(0xFF007AFF)) }

    // ... trigger button ...

    if (showSheet) {
        GenericFormSheet(
            spec = ActivityFormSpecs.create,
            initialData = null,
            labelActions = mapOf(    // ★ 注入交互
                "tags" to { showTagPicker = true },
                "category" to { showGroupPicker = true },
            ),
            onDismiss = { showSheet = false },
            onSubmit = { showSheet = false },
        )
    }

    // TagPicker ModalBottomSheet
    if (showTagPicker) {
        TagPickerSheet(...)
    }

    // GroupPicker ModalBottomSheet  
    if (showGroupPicker) {
        GroupPickerSheet(...)
    }
}
```

### 5.2 AddTagPreview / EditTagPreview

同上，注入 color picker 交互（Tag 表单无需 tag/group picker）。

---

## 6. 文件变更清单

| 操作 | 文件 | 说明 |
|---|---|---|
| ♻️ 修改 | `model/FormSpec.kt` | LabelAction 加 `onClick` 参数 |
| ♻️ 修改 | `ui/GenericFormSheet.kt` | IconColorRenderer 加交互；LabelAction 传递 onClick |
| ➕ 新建 | `ui/preview/MockData.kt` | 集中定义 mock tags/groups |
| ♻️ 修改 | `ui/preview/AddActivityPreview.kt` | 注入 tag/group picker + mock 数据 |
| ♻️ 修改 | `ui/preview/EditActivityPreview.kt` | 同上 |
| ♻️ 修改 | `ui/preview/AddTagPreview.kt` | 注入 color picker |
| ♻️ 修改 | `ui/preview/EditTagPreview.kt` | 注入 color picker |

---

## 7. 边界情况

- **ColorPicker 选色后未确认直接关弹窗**：颜色保持之前的值不更新
- **TagPicker 无 Tag 选中**：selectedTagIds 为空集合，Label 显示 "+ 增加"
- **Group 选择"未分类"**：selectedGroupId = null，Label 显示 "未分类"
- **FormRow.IconColor 渲染时 colorKey 值为空字符串**：用 MaterialTheme.colorScheme.primary 兜底
