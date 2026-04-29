# 重要：Debug 表单字段映射决策文档

**日期：** 2026-04-29
**状态：** ✅ 已实施
**关联：** `feature/debug/model/FormSpec.kt` + `feature/debug/ui/preview/ActivityFormSpecs.kt`

---

## 1. 背景

将 Debug 模块中的表单从"假数据占位"升级为"与真实数据模型对齐"，对照 `core/data/model/Activity.kt` 和 `core/data/model/Tag.kt` 的真实字段，逐字段做出保留/删除/新增决策。

---

## 2. Activity 表单字段映射

| 表单字段 key | 真实 Activity 模型 | 决策 | FormRow 类型 | 新增/编辑可见 |
|---|---|---|---|---|
| `icon` (原名 emoji) | `Activity.emoji: String?` | 保留，改名以适配 iconKey 体系 | `IconColor` | 两者 |
| `color` | **不存在** | ❌ 删除，Activity 无 color 字段 | — | — |
| `name` | `Activity.name: String` | ✅ 保留 | `TextInput` | 两者 |
| `note` | **不存在**（属于 Behavior 表） | 保留为便签字段 | `TextInput` | 两者 |
| `tags` | 间接通过 `ActivityTagBindingEntity` | 保留为占位 LabelAction，后续接真实交互 | `LabelAction` | 两者 |
| `keywords` | **不存在** | ❌ 删除 | — | — |
| `category` | `Activity.groupId: Long?` | 保留，真实为 groupId 外键 | `LabelAction` | 两者 |
| `id` | `Activity.id: Long` | ➕ 新增，表单内部持有 | 表单内部 | 编辑 |
| `isArchived` | `Activity.isArchived: Boolean` | ➕ 新增，仅编辑模式底部 Switch | `Switch` | 仅编辑 |

### 表单结构（最终）

```
[卡片1] icon: IconColor
[卡片2] name: TextInput  +  note: TextInput
[卡片3] tags: LabelAction(占位)
[卡片4] category: LabelAction(占位)
[编辑]  + isArchived: Switch
```

---

## 3. Tag 表单字段映射

| 表单字段 key | 真实 Tag 模型 | 决策 | FormRow 类型 | 新增/编辑可见 |
|---|---|---|---|---|
| `icon` (原名 emoji) | `Tag.icon: String?` | 改名 | `IconColor` | 两者 |
| `color` | `Tag.color: Long?` | ✅ 保留（ARGB int） | `IconColor` 内颜色圆圈 | 两者 |
| `name` | `Tag.name: String` | ✅ 保留 | `TextInput` | 两者 |
| `category` | `Tag.category: String?` | ✅ 保留 | `LabelAction` | 两者 |
| `id` | `Tag.id: Long` | ➕ 新增，表单内部持有 | 表单内部 | 编辑 |
| `textColor` | `Tag.textColor: Long?` | ❌ 不新增，由 color 覆盖 | — | — |
| `priority` | `Tag.priority: Int` | ➕ 新增，展示在新增/编辑 | `NumberInput(0..99)` | 两者 |
| `usageCount` | `Tag.usageCount: Int` | ✅ 表单持有，不展示 | 表单内部 | 不可见 |
| `sortOrder` | `Tag.sortOrder: Int` | ✅ 表单持有，不展示 | 表单内部 | 不可见 |
| `isArchived` | `Tag.isArchived: Boolean` | ➕ 新增，仅编辑模式底部 Switch | `Switch` | 仅编辑 |

### 表单结构（最终）

```
[卡片1] icon: IconColor
[卡片2] name: TextInput  +  priority: NumberInput(0..99)
[卡片3] category: LabelAction(占位)
[编辑]  + isArchived: Switch
```

---

## 4. FormRow 类型清单

| 类型 | 用途 | 存储方式 |
|---|---|---|
| `TextInput` | 文本输入（name, note） | Map String→String |
| `IconColor` | 图标 emoji + 颜色圆圈 | Map (iconKey→emoji, colorKey→"") |
| `LabelAction` | 占位操作行（tags, category） | 不存储数据 |
| `Switch` | 布尔开关（isArchived） | Map key→"true"/"false" |
| `NumberInput` | 两位数字输入（priority） | Map key→数字字符串 |

---

## 5. 编辑模式 initialData 键名

```kotlin
// EditActivityPreview
mapOf("name" to "阅读", "note" to "每天30分钟", "icon" to "📖", "isArchived" to "false")

// EditTagPreview
mapOf("name" to "工作", "icon" to "💼", "priority" to "5", "isArchived" to "false")
```

---

## 6. 未实现 / 后续迭代

| 项 | 说明 |
|---|---|
| tags 真实交互 | 当前为 LabelAction 占位，后续接入标签选择器 |
| category 真实交互 | 当前为 LabelAction 占位，后续接入 ActivityGroup 选择器 |
| color picker | IconColor 颜色圆圈当前仅展示主题色，后续接真实选色 |
| iconKey | 当前由 emoji 覆盖，后续支持自定义 Material Icon |
