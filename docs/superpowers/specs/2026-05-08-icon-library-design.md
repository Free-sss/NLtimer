# 图标库集成设计规格

> 日期：2026-05-08
> 状态：已批准

## 背景

NLtimer 项目的 Activity 和 Tag 都有 `iconKey` 字段（`String?`），当前存储纯 emoji 字符。用户编辑 emoji 的方式是手动输入/粘贴到 `OutlinedTextField`，没有选择器 UI。需要引入图标库，让用户既能输入 emoji，也能通过图标库 UI 选择矢量图标。

## 决策记录

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 模式 | 混合模式（emoji + 矢量图标） | 满足两种需求 |
| 图标库 | Material Icons（现有 material-icons-extended） | 零新依赖，Compose 原生渲染着色 |
| 选择器 UI | BottomSheet 双 Tab | 屏幕空间充裕，体验统一 |
| Emoji 编辑 | 网格选择 + 手动输入框 | 兼顾便捷和灵活性 |
| iconKey 格式 | 前缀区分 | 零迁移成本，向后兼容 |

## 1. iconKey 格式与解析

### 格式约定

| 类型 | iconKey 值 | 示例 |
|------|-----------|------|
| Emoji | 纯 emoji 字符 | `📖`、`📺` |
| Material Icon | `mi:{Style}:{Name}` | `mi:Filled:Search`、`mi:Outlined:Home` |

Style 可选值：`Filled`、`Outlined`、`Rounded`、`Sharp`、`TwoTone`

### 解析工具

位置：`core/designsystem/icon/IconKeyResolver.kt`

```kotlin
object IconKeyResolver {
    private const val PREFIX = "mi:"

    fun isMaterialIcon(iconKey: String): Boolean = iconKey.startsWith(PREFIX)

    fun parseMaterialIcon(iconKey: String): Pair<String, String>? {
        if (!isMaterialIcon(iconKey)) return null
        val parts = iconKey.removePrefix(PREFIX).split(":", limit = 2)
        if (parts.size != 2) return null
        return parts[0] to parts[1]
    }

    fun resolveImageVector(iconKey: String): ImageVector?
}
```

`resolveImageVector` 通过 `MaterialIconCatalog` 的预编译映射查找 `ImageVector`，不使用反射。

### 向后兼容

现有纯 emoji iconKey 无需任何迁移。新增的 `mi:` 前缀只在用户选择图标库图标时产生。数据库字段类型不变（`String?`），无需 Room 迁移。

## 2. IconRenderer 渲染组件

位置：`core/designsystem/icon/IconRenderer.kt`

```kotlin
@Composable
fun IconRenderer(
    iconKey: String?,
    modifier: Modifier = Modifier,
    defaultEmoji: String = "📌",
    tint: Color = LocalContentColor.current,
    emojiFontSize: TextUnit = 24.sp
)
```

行为逻辑：
- `iconKey` 为 `null` → 显示 `defaultEmoji`（Text 组件）
- `iconKey` 以 `mi:` 开头 → 解析为 `ImageVector`，用 `Icon()` 渲染，支持着色
- `iconKey` 为纯 emoji → 用 `Text()` 渲染

### 需要替换的现有渲染点

| 文件 | 行号 | 当前写法 | 替换为 |
|------|------|---------|--------|
| `ActivityChip.kt` | 32-33 | emoji string concat | `IconRenderer` + name Text |
| `ActivityDetailSheet.kt` | 67 | `Text(activity.iconKey ?: "📌")` | `IconRenderer(activity.iconKey)` |
| `GridCell.kt` | 84 | `cell.activityIconKey?.let { ... }` | `IconRenderer` |
| `MomentView.kt` | 237 | emoji string concat | `IconRenderer` + name |
| `MomentFocusCard.kt` | 149, 212 | emoji string concat | `IconRenderer` + name |
| `TimelineReverseView.kt` | 279 | emoji string concat | `IconRenderer` + name |
| `BehaviorLogView.kt` | 138 | emoji string concat | `IconRenderer` + name |
| `CategoryPickerDialog.kt` | 396-400 | `item.iconKey` text | `IconRenderer` |
| `ActivityPicker.kt` | 101 | emoji string concat | `IconRenderer` + name |

## 3. Material Icon 图标目录

位置：`core/designsystem/icon/MaterialIconCatalog.kt`

### 数据结构

```kotlin
enum class IconCategory(val label: String) {
    ACTION("操作"), COMMUNICATION("沟通"), CONTENT("内容"),
    DEVICE("设备"), IMAGE("图像"), MAPS("地图"),
    NAVIGATION("导航"), SOCIAL("社交"), EDITOR("编辑"),
    AV("影音"), PLACES("地点"), HARDWARE("硬件")
}

data class IconEntry(
    val name: String,
    val style: String,
    val imageVector: ImageVector,
    val category: IconCategory,
    val keywords: List<String>
)

object MaterialIconCatalog {
    val icons: List<IconEntry>
    val categories: List<IconCategory>
    fun search(query: String): List<IconEntry>
    fun byCategory(category: IconCategory): List<IconEntry>
    fun resolve(style: String, name: String): ImageVector?
}
```

### 目录策略

- 从 ~2000 个 Material Icons 中精选 300-500 个常用图标
- 按 12 个分类组织
- 每个图标附带中英文搜索关键词
- 默认 style 为 `Filled`，部分图标提供 `Outlined` 变体
- 编译期确定（静态列表），无需反射

## 4. IconPickerSheet 选择器 UI

位置：`core/designsystem/icon/IconPickerSheet.kt`

### 布局结构

```
┌─────────────────────────────┐
│  编辑图标              [✕]  │
├─────────────────────────────┤
│  [Emoji]  [图标库]          │
├─────────────────────────────┤
│  🔍 搜索...                 │
├─────────────────────────────┤
│  ┌常用┐┌表情┐┌动物┐...      │  ← 分类 Chips（横向滚动）
├─────────────────────────────┤
│  ┌──┐┌──┐┌──┐┌──┐┌──┐┌──┐  │
│  │😀││😎││🤔││😅││🥰││😡│  │  ← 图标网格 (4-6 列)
│  └──┘└──┘└──┘└──┘└──┘└──┘  │
├─────────────────────────────┤
│  手动输入: [📖_________]    │  ← 仅 Emoji Tab 显示
│                    [确定]    │
└─────────────────────────────┘
```

### Emoji Tab

- 数据源：基于 Unicode 标准分类，精选常用 emoji（约 800 个）
- 位置：`core/designsystem/icon/EmojiCatalog.kt`
- 分类：常用/表情/手势/动物/食物/交通/活动/物品/自然/符号
- 支持搜索（emoji 名称 / Unicode 名称）
- 底部保留手动输入框（最大 4 字符），带确定按钮

### 图标库 Tab

- 数据源：`MaterialIconCatalog`
- 分类 chips 横向滚动
- 网格点击选择，自动生成 `mi:Filled:Search` 格式 iconKey

### 交互流程

1. 用户点击表单中的图标圆圈 → 打开 `IconPickerSheet`
2. 选择 emoji 或图标 → 立即回填到表单，关闭 Sheet
3. 或在手动输入框输入 → 按确定回填

### API

```kotlin
@Composable
fun IconPickerSheet(
    currentIconKey: String?,
    onIconSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    defaultEmoji: String = "📖"
)
```

## 5. 表单系统集成

### 改造目标

将 `emojiEditDialog()` 替换为 `IconPickerSheet`。

### 需要修改的文件

| 文件 | 改动 |
|------|------|
| `core/designsystem/form/GenericFormSheet.kt` | `emojiEditDialog()` → 调用 `IconPickerSheet` |
| `core/designsystem/form/GenericFormDialog.kt` | `dialogEmojiEditDialog()` → 调用 `IconPickerSheet` |
| `feature/debug/.../GenericFormSheet.kt` | 同上（同步修改） |
| `feature/management_activities/.../EditActivityDialog.kt` | 消除重复的 emoji 编辑逻辑，统一用 `IconPickerSheet` |

### 新增文件

| 文件 | 职责 |
|------|------|
| `core/designsystem/icon/IconKeyResolver.kt` | iconKey 解析工具 |
| `core/designsystem/icon/IconRenderer.kt` | 统一渲染 Composable |
| `core/designsystem/icon/MaterialIconCatalog.kt` | 精选图标目录数据 |
| `core/designsystem/icon/IconPickerSheet.kt` | 图标选择器 BottomSheet |
| `core/designsystem/icon/EmojiCatalog.kt` | Emoji 分类数据 |

### 不需要改动

- 数据库 / Entity / DAO — iconKey 字段类型不变（`String?`）
- ViewModel 层 — 仍是传递字符串
- Room 迁移 — 无需

## 6. 实现方案

使用现有 `material-icons-extended` 依赖，零新依赖引入。

### 优势

- 无新增依赖
- Compose 原生 `Icon()` 渲染，支持着色
- 图标质量由 Google 保证
- 与项目现有设计语言统一

### 注意事项

- `material-icons-extended` 包体积较大（~6MB），但项目已引入
- 精选图标数量控制在 300-500，避免选择器信息过载
- 搜索关键词需覆盖中英文，方便中文用户
