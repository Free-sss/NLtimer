# 字段详细调试工具设计规格

**日期**：2026-05-08
**状态**：已批准
**范围**：活动管理、标签管理的字段详细调试功能

---

## 1. 背景与目标

### 问题
开发者在调试活动（Activity）和标签（Tag）数据时，难以快速识别哪些字段是缺失的，以及完整的数据结构是什么样的。

### 解决方案
在 debug 包中，当用户点击活动或标签元素时，在详情弹窗的右上角显示一个"字段详细"按钮。点击后弹出一个包含两个 tab 的对话框：
- **渲染**：显示当前 UI 上实际展示的字段，以及缺失字段的汇总
- **原生**：显示完整的数据对象 JSON，支持复制到剪贴板

---

## 2. 架构设计

### 组件结构
```
ActivityDetailSheet / EditTagFormSheet
    └── [字段详细] 按钮 (右上角，仅 debug 模式)
            └── FieldDetailDialog (AlertDialog)
                ├── Tab: 渲染
                │   ├── 已填充字段列表
                │   └── 缺失字段汇总区域
                └── Tab: 原生
                    ├── JSON 完整字段
                    └── [复制全部] 按钮
```

### 文件改动
1. **新建** `feature/debug/src/main/java/com/nltimer/feature/debug/ui/components/FieldDetailDialog.kt`
   - 通用字段详细弹窗组件
2. **修改** `feature/management_activities/src/main/java/com/nltimer/feature/management_activities/ui/components/ActivityDetailSheet.kt`
   - 添加"字段详细"按钮和状态管理
3. **修改** `feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/dialogs/EditTagFormSheet.kt`
   - 添加"字段详细"按钮和状态管理

---

## 3. 数据模型

### FieldInfo 数据类

```kotlin
data class FieldInfo(
    val name: String,        // 字段名，如 "name", "iconKey"
    val displayName: String, // 显示名，如 "名称", "图标"
    val value: Any?,         // 字段值
    val isDisplayed: Boolean,// 是否在 UI 上展示
    val isMissing: Boolean,  // 是否缺失（null/空/默认值）
)
```

### 字段缺失判断逻辑

```kotlin
fun Any?.isMissing(): Boolean = when (this) {
    null -> true
    is String -> isBlank()
    is Int -> this == 0 && name 不是 "id" 类型
    is Boolean -> !this  // false 视为缺失（如 isArchived）
    else -> false
}
```

---

## 4. 字段映射

### Activity 字段映射

| 字段名 | 显示名 | UI 展示 | 缺失判断 |
|--------|--------|---------|----------|
| name | 名称 | ✅ | null/空 |
| iconKey | 图标 | ✅ | null |
| color | 颜色 | ✅ | null |
| keywords | 关键词 | ❌ | null/空 |
| groupId | 分组 | ❌ | null |
| isPreset | 预设 | ❌ | - |
| isArchived | 归档 | ❌ | - |
| usageCount | 使用次数 | ✅ | 0 |

### Tag 字段映射

| 字段名 | 显示名 | UI 展示 | 缺失判断 |
|--------|--------|---------|----------|
| name | 名称 | ✅ | null/空 |
| color | 颜色 | ✅ | null |
| iconKey | 图标 | ✅ | null |
| category | 分类 | ❌ | null/空 |
| priority | 优先级 | ❌ | 0 |
| keywords | 关键词 | ❌ | null/空 |
| usageCount | 使用次数 | ✅ | 0 |

---

## 5. UI 设计

### 按钮样式
- 图标：`Icons.Default.Info`（ℹ️ 信息图标）
- 位置：`ActivityDetailSheet` 右上角，在"编辑"和"删除"按钮左侧
- 颜色：`MaterialTheme.colorScheme.onSurface`（与其他按钮一致）
- 显示条件：`BuildConfig.DEBUG` 为 true

### Dialog 样式
- 标题：居中显示，如"活动字段详情"
- Tab 栏：使用 `TabRow`，两个 tab 平分宽度
- 宽度：`fillMaxWidth(0.9f)`，留出边距
- 高度：自适应内容，最大 `fillMaxHeight(0.7f)`

### 渲染 Tab 样式
- **已填充字段**：
  - 左侧：字段显示名（灰色）
  - 右侧：字段值（黑色）
  - 分隔线：`HorizontalDivider`
- **缺失字段汇总**：
  - 背景：`surfaceVariant` 浅灰色
  - 标题："缺失字段"（红色）
  - 内容：字段名列表，用逗号分隔

### 原生 Tab 样式
- **复制按钮**：
  - 位置：右上角
  - 文案："复制全部"
  - 图标：`Icons.Default.ContentCopy`
- **JSON 内容**：
  - 字体：`FontFamily.Monospace`
  - 背景：`surfaceVariant`
  - 圆角：`RoundedCornerShape(8.dp)`
  - 内边距：`12.dp`
  - 可滚动（如果内容过长）

---

## 6. 数据流

```
ViewModel (Activity/Tag 数据)
    ↓
Sheet (接收数据，添加按钮)
    ↓ 点击按钮
Sheet 内部状态 (showFieldDetail = true)
    ↓
FieldDetailDialog (接收 fields + rawJson)
```

---

## 7. 实现细节

### Debug 模式判断

```kotlin
// 在 BuildConfig.DEBUG 为 true 时才显示按钮
if (BuildConfig.DEBUG) {
    IconButton(onClick = { showFieldDetail = true }) {
        Icon(Icons.Default.Info, contentDescription = "字段详细")
    }
}
```

### 复制功能实现

```kotlin
val clipboardManager = LocalClipboardManager.current
Button(onClick = {
    clipboardManager.setText(AnnotatedString(rawJson))
}) {
    Icon(Icons.Default.ContentCopy, contentDescription = null)
    Spacer(modifier = Modifier.width(4.dp))
    Text("复制全部")
}
```

### JSON 序列化

使用 `kotlinx.serialization` 将数据对象转换为格式化的 JSON 字符串：

```kotlin
val rawJson = Json.encodeToString(activity)
// 或者手动构建 JSON 字符串以确保格式可控
```

---

## 8. 测试策略

### 单元测试
- `FieldInfo` 数据类的 `isMissing` 扩展函数
- 字段映射函数

### UI 测试
- `FieldDetailDialog` 的渲染测试
- Tab 切换测试
- 复制功能测试

---

## 9. 范围限制

- 仅在 debug 包中显示按钮
- 不影响 release 包的 UI
- 不修改现有的 Activity/Tag 数据模型
- 不添加新的依赖库

---

## 10. 未来扩展

- 支持编辑字段值（调试用）
- 支持导出字段数据到文件
- 支持字段变更历史追踪
