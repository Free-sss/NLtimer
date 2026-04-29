# Debug 通用表单弹窗 + 模块目录重构 设计文档

**日期：** 2026-04-29
**状态：** ✅ 已批准
**架构方案：** FormSpec 数据驱动 + GenericFormSheet 复用 + debug 模块目录化

---

## 1. 目标与范围

### 目标
将 `AddActivityDialogPreview` 重构为通用表单弹窗组件，通过 `FormSpec` 数据结构驱动表单渲染，支持"新增活动 / 编辑活动 / 新增标签 / 编辑标签"四种模式，同时将 debug 模块从扁平目录改为分层目录。

### 范围
- ✅ **FormSpec 数据结构**：定义表单分组的 sealed class 行类型
- ✅ **GenericFormSheet**：读取 FormSpec 动态渲染，维护 `Map<String, String>` 状态
- ✅ **四种模式预览**：`AddActivityPreview`、`EditActivityPreview`、`AddTagPreview`、`EditTagPreview`
- ✅ **复用现有 UI 组件**：`TextInputRow`、`ActionRow` 直接迁移为通用渲染器
- ✅ **debug 模块目录重构**：`model/` + `ui/preview/` 分层
- ❌ **不在范围内**：真实数据交互、icon/color picker 交互（后续迭代）
- ❌ **不在范围内**：Activity/Tag 真实字段映射（后续从项目提取）

---

## 2. 架构设计

### 2.1 FormSpec 数据结构

```kotlin
// feature/debug/.../model/FormSpec.kt

data class FormSpec(
    val title: String,                     // "增加活动" / "编辑活动" / "新增标签"
    val submitLabel: String,               // 按钮文案
    val sections: List<FormSection>,
)

data class FormSection(
    val rows: List<FormRow>,
)

sealed class FormRow {
    data class TextInput(
        val key: String,                   // 数据字段 key
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

### 2.2 GenericFormSheet 核心逻辑

```
入参: spec, initialData: Map<String, String>?, onDismiss, onSubmit
       │
       ├── 从 spec 提取所有 FormRow 的 key → 初始化 Map<String, String>
       │     └── 编辑模式：填入 initialData
       │     └── 新增模式：填入初始默认值
       │
       ├── ModalBottomSheet 渲染
       │     ├── dragHandle: 标题
       │     └── Column:
       │           ├── forEach(spec.sections) { section ->
       │           │     Surface(card) {
       │           │         forEach(section.rows) { row ->
       │           │             when (row) {
       │           │                 TextInput    → TextInputRenderer
       │           │                 IconColor    → IconColorRenderer
       │           │                 LabelAction  → LabelActionRenderer
       │           │             }
       │           │         }
       │           │     }
       │           │ }
       │           └── Button(submitLabel, onSubmit(data))
       │
       └── onSubmit 回调：传出 Map<String, String>
```

### 2.3 四种模式预设的 FormSpec

| 模式 | title | submitLabel | sections 差异 |
|---|---|---|---|
| 新增活动 | "增加活动" | "增加活动" | IconColor + TextInput(name,note) + LabelAction(tags,keywords) + LabelAction(category) |
| 编辑活动 | "编辑活动" | "保存" | 同上，但 initialData 预填 |
| 新增标签 | "新增标签" | "新增标签" | IconColor + TextInput(name) + LabelAction(category) |
| 编辑标签 | "编辑标签" | "保存" | 同上，但 initialData 预填 |

### 2.4 Debug 模块目录重构

```
feature/debug/src/main/java/com/nltimer/feature/debug/
├── model/
│   └── FormSpec.kt                          ★ 新建
├── ui/
│   ├── GenericFormSheet.kt                  ★ 新建（核心复用组件）
│   ├── DebugPage.kt                         ☆ 不变
│   ├── DebugRoute.kt                        ☆ 不变
│   └── preview/
│       ├── DualTimePickerPreview.kt         → 移入
│       ├── TimeAdjustmentPreview.kt         → 移入
│       ├── AddActivityPreview.kt            ★ 新建（用 FormSpec 定义）
│       ├── EditActivityPreview.kt           ★ 新建
│       ├── AddTagPreview.kt                 ★ 新建
│       └── EditTagPreview.kt                ★ 新建
└── FeatureDebugComponents.kt               ☆ 更新注册
```

### 2.5 组件调用示例

```kotlin
// AddActivityPreview.kt
@Composable
fun AddActivityPreview() {
    var showSheet by remember { mutableStateOf(true) }
    // ... trigger button ...

    if (showSheet) {
        GenericFormSheet(
            spec = ActivityFormSpecs.create,
            initialData = null,     // null = 新增模式
            onDismiss = { showSheet = false },
            onSubmit = { data -> showSheet = false },
        )
    }
}

// ActivityFormSpecs.kt — 集中定义所有模式
object ActivityFormSpecs {
    val create = FormSpec(
        title = "增加活动",
        submitLabel = "增加活动",
        sections = listOf(...)
    )

    fun edit(initial: Map<String, String>) = FormSpec(
        title = "编辑活动",
        submitLabel = "保存",
        sections = listOf(...)
    )
}
```

---

## 3. 数据流

```
[FormSpec 定义] ──→ [GenericFormSheet 渲染]
                          │
                          ├── 状态: MutableMap<String, String>
                          │     ├── TextInput value ←→ state[key]
                          │     ├── IconColor emoji ←→ state[iconKey]
                          │     └── LabelAction ← 仅展示，不存储
                          │
                          └── 提交: { onSubmit(state.toMap()) }
```

无 ViewModel、无 Repository，纯 UI 状态在 `GenericFormSheet` 内部 `remember`。

---

## 4. 文件变更清单

| 操作 | 文件 | 说明 |
|---|---|---|
| ➕ 新建 | `model/FormSpec.kt` | 表单数据结构 |
| ➕ 新建 | `ui/GenericFormSheet.kt` | 通用表单弹窗 |
| ➕ 新建 | `ui/preview/AddActivityPreview.kt` | 新增活动预览 |
| ➕ 新建 | `ui/preview/EditActivityPreview.kt` | 编辑活动预览 |
| ➕ 新建 | `ui/preview/AddTagPreview.kt` | 新增标签预览 |
| ➕ 新建 | `ui/preview/EditTagPreview.kt` | 编辑标签预览 |
| ➕ 新建 | `ui/preview/ActivityFormSpecs.kt` | 活动/标签 FormSpec 工厂 |
| 🚚 移动 | `DualTimePickerPreview.kt` → `ui/preview/` | 目录归类 |
| 🚚 移动 | `TimeAdjustmentPreview.kt` → `ui/preview/` | 目录归类 |
| 🔥 删除 | `AddActivityDialogPreview.kt` | 被 GenericFormSheet 替代 |
| ♻️ 更新 | `FeatureDebugComponents.kt` | 更新注册和 import |

---

## 5. 边界情况

- **initialData 为 null**：全部字段使用默认空值 → 新增模式
- **initialData 部分键缺失**：使用默认值兜底
- **onSubmit 空**：按钮正常渲染，点击无回调副作用
- **FormSpec 为空 sections**：仅显示标题栏 + 提交按钮
