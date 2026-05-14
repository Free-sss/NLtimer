# UI 组件分层提取复用 — 设计规格

## 目标

将 NLtimer 项目中分散在各 feature 模块的重复 UI 代码，按三层粒度统一提取到 `core/designsystem` 模块，全部支持配置化入参调用。

## 现状分析

项目已有良好的组件共享基础（`core/designsystem`），但存在以下问题：

- **4 个 Picker Popup**（~400 行）结构 80% 相同但无共享基类
- **6 个文本输入对话框**几乎完全相同，仅标题/标签不同
- **Settings 入口卡片**在 2 个文件中重复
- **可展开区域卡片**有 3 个变体未统一
- **可选芯片**在 picker、设置页中重复出现
- **空状态/加载状态**在 6 处重复
- **活动颜色转换**工具函数在 2 处完全复制

预计可整合约 **760 行**为 **10+ 个共享组件。

---

## 一、细粒度基础组件（原子层）

提取到 `core/designsystem/component/atom/`：

### 1.1 `SelectableOptionChip`

可选选项芯片，统一当前在 PickerPopup 和设置页中重复的 Surface + Text 模式。

```kotlin
@Composable
fun SelectableOptionChip(
    text: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
)
```

**替代位置：** 4 个 Picker 的 item 渲染、DialogConfigScreen 的 ChipFlowSelector/InlineToggleRow。

### 1.2 `KeyValueRow`

键值对行，统一 StatRow/DetailRow。

```kotlin
@Composable
fun KeyValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    labelStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    valueStyle: TextStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
    labelWeight: Float = 0f,
    valueWeight: Float = 0f,
)
```

### 1.3 `AppDivider`

分割线组件，带主题感知颜色。

```kotlin
@Composable
fun AppDivider(modifier: Modifier = Modifier)
```

### 1.4 `SpacingBox` / `HorizontalSpacing`

间距容器。

```kotlin
@Composable fun VerticalSpacing(height: Dp = 8.dp)
@Composable fun HorizontalSpacing(width: Dp = 8.dp)
```

### 1.5 颜色工具扩展

```kotlin
// ColorExt.kt
fun Long?.toComposeColor(default: () -> Color): Color =
    this?.let { Color(it) } ?: default()
```

**替代：** `BehaviorListItem` 和 `BehaviorTimelineItem` 中的重复转换代码。

### 1.6 卡片样式工具

```kotlin
fun appCardColors(): CardColors = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
)
val appCardShape: Shape = RoundedCornerShape(16.dp)
```

---

## 二、中粒度业务组件（组件层）

提取到 `core/designsystem/component/`：

### 2.1 `PickerPopupContainer`

统一 4 个 Picker Popup 的外层容器结构。

```kotlin
@Composable
fun <T> PickerPopupContainer(
    title: String,
    items: List<T>,
    isSelected: (T) -> Boolean,
    onSelect: (T) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    headerAction: @Composable (() -> Unit)? = null,
    itemName: (T) -> String = { it.toString() },
)
```

**替代：** `GroupPickerPopup`、`SingleSelectPickerPopup`、`MultiSelectPickerPopup`、`CategoryPickerPopup` 的外层结构。各 Picker 保留为薄封装，调用 `PickerPopupContainer`。

### 2.2 `TextInputDialog`

统一 6 个文本输入对话框。

```kotlin
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
    conflictCheck: ((String) -> String?)? = null,
)
```

**替代：** `AddGroupDialog`、`RenameGroupDialog`、`RenameCategoryDialog`、`CategoriesScreen` 内联的新建/重命名对话框、`AddCategoryDialog`。

### 2.3 `SettingsEntryCard`

统一设置页入口卡片。

```kotlin
@Composable
fun SettingsEntryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
)
```

**替代：** `SettingsScreen.SettingsEntryCard`、`DataManagementScreen.ActionCard`。

### 2.4 `ExpandableCard`

统一可展开卡片。基于现有 `ExpandableGroupCard` 扩展，支持标题、副标题、自定义内容。

```kotlin
@Composable
fun ExpandableCard(
    title: String,
    subtitle: String? = null,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    headerTrailing: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
)
```

**替代：** `DialogConfigScreen.ConfigExpandableSection`、`DataManagementScreen.ExpandableSection`、统一 `ExpandableGroupCard`。

### 2.5 `GroupedItemsCard<T>`

统一分组卡片（GroupCard / CategoryCard 的共享结构）。

```kotlin
@Composable
fun <T> GroupedItemsCard(
    title: String,
    items: List<T>,
    onItemClick: (T) -> Unit,
    onAddClick: (() -> Unit)? = null,
    menuItems: List<MenuItem>,
    itemContent: @Composable (T) -> Unit,
)
```

### 2.6 `LoadingScreen` / `EmptyStateView`

```kotlin
@Composable
fun LoadingScreen(modifier: Modifier = Modifier)

@Composable
fun EmptyStateView(
    message: String,
    subtitle: String? = null,
    icon: ImageVector = Icons.Outlined.Inbox,
    modifier: Modifier = Modifier,
)
```

### 2.7 `DebugInfoButton`

```kotlin
@Composable
fun DebugInfoButton(
    fieldName: String,
    fieldValue: Any?,
    modifier: Modifier = Modifier,
)
```

### 2.8 Form Spec 扩展工具

```kotlin
fun FormSpec.withLabelActions(
    vararg updates: Pair<String, Pair<String, () -> Unit>>
): FormSpec
```

**替代：** 4 个 FormSheet 中的重复 spec mutation 代码。

---

## 三、粗粒度页面模板（模板层）

提取到 `core/designsystem/template/`：

### 3.1 `ManagementScaffold`

管理页面的通用脚手架，统一各管理页面的 Scaffold + TopBar + FAB 结构。

```kotlin
@Composable
fun ManagementScaffold(
    title: String,
    onBack: () -> Unit,
    fabContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
)
```

### 3.2 `CrudListPage<T>`

CRUD 列表页通用模板，统一 ActivityManagement、TagManagement、Categories 等页面的 Loading → Empty → Content 三态切换。

```kotlin
@Composable
fun <T> CrudListPage(
    items: List<T>,
    isLoading: Boolean,
    emptyMessage: String,
    emptySubtitle: String? = null,
    listItem: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
)
```

---

## 影响范围

| 组件 | 替代文件数 | 预计减少行数 |
|------|-----------|-------------|
| PickerPopupContainer | 4 | ~250 |
| TextInputDialog | 6 | ~120 |
| SettingsEntryCard | 2 | ~60 |
| ExpandableCard | 3 | ~60 |
| SelectableOptionChip | 6+ | ~50 |
| GroupedItemsCard | 2 | ~80 |
| LoadingScreen / EmptyStateView | 6 | ~40 |
| ColorExt / CardStyles | 4+ | ~20 |
| Form Spec 工具 | 4 | ~60 |
| DebugInfoButton | 2 | ~15 |
| 页面模板 | 3 | ~30 |
| **合计** | **~42 文件** | **~760 行** |

## 实施原则

1. **向后兼容**：新组件不破坏现有 API，旧调用点逐步迁移
2. **单一职责**：每个组件只做一件事，通过参数配置行为
3. **渐进迁移**：先提取原子组件，再构建业务组件，最后组装模板
4. **测试覆盖**：每个新组件配合 screenshot 测试验证视觉一致性
