# 统一 AddActivity/AddTag 表单 Spec 设计

## 背景

增加活动、增加标签的表单在多处调用，但 Spec 定义分散且不一致：

| 调用位置 | 容器 | Spec 来源 | 字段差异 |
|---------|------|----------|---------|
| home/AddBehaviorSheet 内 | GenericFormDialog | 本地 addActivitySpec | 缺 tags、category |
| home/AddBehaviorSheet 内 | GenericFormDialog | 本地 addTagSpec | 缺 priority、category |
| management_activities | GenericFormSheet | 本地 createSpec | 与 debug 一致 |
| tag_management | GenericFormDialog | 本地 createTagSpec | 有 priority/category，缺 tags |
| debug | GenericFormSheet | ActivityFormSpecs | 完整（基准） |

## 方案

将 debug 模块的 `ActivityFormSpecs` 提升到 `core/designsystem`，作为所有模块的 Spec 单一来源。

### 容器策略

- AddBehaviorSheet 内（已嵌套 BottomSheet）→ `GenericFormDialog`（AlertDialog）
- 活动管理/标签管理/Debug → `GenericFormSheet`（BottomSheet）

### 数据适配

各调用点的 `onSubmit` 回调中从 `formState` 提取字段的逻辑保留在各自模块，暂不统一。

## 文件变更

| 操作 | 文件 | 说明 |
|------|------|------|
| 新建 | `core/designsystem/.../form/ActivityFormSpecs.kt` | 从 debug 提升，作为单一来源 |
| 修改 | `home/.../sheet/AddActivityDialog.kt` | 引用 core Spec，删除本地 spec |
| 修改 | `home/.../sheet/AddTagDialog.kt` | 引用 core Spec，删除本地 spec |
| 修改 | `management_activities/.../ActivityFormSheets.kt` | 引用 core Spec，删除本地 createSpec/editSpec |
| 修改 | `tag_management/.../dialogs/AddTagDialog.kt` | 引用 core Spec，删除本地 createTagSpec |
| 修改 | `debug/.../preview/AddActivityPreview.kt` | 改为引用 core 的 ActivityFormSpecs |
| 修改 | `debug/.../preview/AddTagPreview.kt` | 改为引用 core 的 ActivityFormSpecs |
| 删除 | `debug/.../preview/ActivityFormSpecs.kt` | 已提升到 core |

## Spec 定义

```kotlin
object ActivityFormSpecs {
    val createActivity = FormSpec(
        title = "增加活动",
        submitLabel = "增加活动",
        sections = listOf(
            FormSection(rows = listOf(
                FormRow.IconColor(iconKey = "icon", colorKey = "color", initialEmoji = "📖"),
            )),
            FormSection(rows = listOf(
                FormRow.TextInput(key = "name", label = "名称", placeholder = "请输入"),
                FormRow.TextInput(key = "note", label = "备注", placeholder = "请输入"),
            )),
            FormSection(rows = listOf(
                FormRow.LabelAction(key = "tags", label = "关联标签", actionText = "+ 增加"),
            )),
            FormSection(rows = listOf(
                FormRow.LabelAction(key = "category", label = "所属分类", actionText = "未分类"),
            )),
        ),
    )

    fun editActivity() = FormSpec(
        title = "编辑活动",
        submitLabel = "保存",
        sections = createActivity.sections + FormSection(
            rows = listOf(FormRow.Switch(key = "isArchived", label = "归档")),
        ),
    )

    val createTag = FormSpec(
        title = "新增标签",
        submitLabel = "新增标签",
        sections = listOf(
            FormSection(rows = listOf(
                FormRow.IconColor(iconKey = "icon", colorKey = "color", initialEmoji = "🏷️"),
            )),
            FormSection(rows = listOf(
                FormRow.TextInput(key = "name", label = "名称", placeholder = "请输入标签名"),
                FormRow.NumberInput(key = "priority", label = "优先级", initialValue = 0, range = 0..99),
            )),
            FormSection(rows = listOf(
                FormRow.LabelAction(key = "category", label = "所属分类", actionText = "未分类"),
            )),
        ),
    )

    fun editTag() = FormSpec(
        title = "编辑标签",
        submitLabel = "保存",
        sections = createTag.sections + FormSection(
            rows = listOf(FormRow.Switch(key = "isArchived", label = "归档")),
        ),
    )
}
```

## 验证

1. AddBehaviorSheet → 点击"+ 活动" → 弹出 Dialog，字段含 icon/color/name/note/tags/category
2. AddBehaviorSheet → 点击"+ 标签" → 弹出 Dialog，字段含 icon/color/name/priority/category
3. 活动管理 → FAB 添加活动 → 弹出 Sheet，字段与 debug 一致
4. 标签管理 → 添加标签 → 弹出 Dialog，字段与 debug 一致
5. Debug 页面 → AddActivity/AddTag 预览 → 行为不变
