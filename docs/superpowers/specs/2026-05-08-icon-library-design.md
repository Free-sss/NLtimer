# 图标库集成设计规格

> 日期：2026-05-08
> 状态：已批准（v2 — 根据审查反馈修订）

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

### 编码安全与校验

**Style 校验**：并非所有 Material Icon 都有五种样式变体。若用户传入 `mi:TwoTone:Search` 但不存在该变体，解析成功但 `resolveImageVector` 返回 `null`，此时走 fallback 逻辑（见第 2 节）。

**Name 白名单**：`IconKeyResolver.parseMaterialIcon` 在解析时对 `Name` 做白名单校验（仅允许 `[A-Za-z0-9_]`），防止注入意外前缀或特殊字符。

**Emoji 输入限制**：手动输入框限制为 **最多 4 个 Unicode 码点**（而非 4 个 char），使用 `BreakIterator.getCharacterInstance()` 计数，确保 ZWJ 序列等复合 emoji 不会被截断。超出限制时截断到最近的码点边界。

### 解析工具

位置：`core/designsystem/icon/IconKeyResolver.kt`

```kotlin
object IconKeyResolver {
    private const val PREFIX = "mi:"
    private val NAME_PATTERN = Regex("^[A-Za-z0-9_]+$")

    fun isMaterialIcon(iconKey: String): Boolean = iconKey.startsWith(PREFIX)

    fun parseMaterialIcon(iconKey: String): Pair<String, String>? {
        if (!isMaterialIcon(iconKey)) return null
        val parts = iconKey.removePrefix(PREFIX).split(":", limit = 2)
        if (parts.size != 2) return null
        val (style, name) = parts
        if (!NAME_PATTERN.matches(name)) return null
        return style to name
    }

    fun resolveImageVector(iconKey: String): ImageVector? {
        val (style, name) = parseMaterialIcon(iconKey) ?: return null
        return MaterialIconCatalog.resolve(style, name)
    }

    fun iconKeyToDisplayText(iconKey: String?): String {
        if (iconKey == null) return ""
        if (isMaterialIcon(iconKey)) {
            val (_, name) = parseMaterialIcon(iconKey) ?: return iconKey
            return name
        }
        return iconKey
    }
}
```

`resolveImageVector` 通过 `MaterialIconCatalog` 内部维护的 `Map<Pair<String, String>, () -> ImageVector>` 懒加载映射查找 `ImageVector`，不使用反射。每个 `ImageVector` 通过 `lazy` 延迟实例化，仅在实际渲染时才创建，避免启动时 400+ 个 `ImageVector` 全部常驻内存。

### 文本化降级

`iconKeyToDisplayText(iconKey)` 工具函数供所有非 Compose 渲染场景使用（如数据导出、日志、无 Compose 上下文的文本拼接）：
- 纯 emoji → 原样返回
- `mi:Filled:Search` → 返回 `"Search"`（图标名称作为可读文本）
- 无效 iconKey → 原样返回

### 向后兼容

现有纯 emoji iconKey 无需任何迁移。新增的 `mi:` 前缀只在用户选择图标库图标时产生。数据库字段类型不变（`String?`），无需 Room 迁移。

目前项目无小部件、通知或数据导出功能使用 iconKey，后续如需新增这些功能，应使用 `iconKeyToDisplayText` 进行文本化降级。

## 2. IconRenderer 渲染组件

位置：`core/designsystem/icon/IconRenderer.kt`

```kotlin
@Composable
fun IconRenderer(
    iconKey: String?,
    modifier: Modifier = Modifier,
    defaultEmoji: String = "📌",
    tint: Color = LocalContentColor.current,
    iconSize: Dp = 24.dp,
    emojiFontSize: TextUnit = TextUnit.Unspecified
)
```

参数说明：
- `iconSize`：统一控制 Material Icon 的 `Modifier.size()` 和 emoji 的默认字号（当 `emojiFontSize` 为 `Unspecified` 时，emoji 也使用 `iconSize` 对应的 sp 值）
- `emojiFontSize`：单独覆盖 emoji 字号，为 `Unspecified` 时跟随 `iconSize`
- `tint`：仅对 Material Icon 生效，emoji 使用自身颜色

行为逻辑：
- `iconKey` 为 `null` → 显示 `defaultEmoji`（Text 组件）
- `iconKey` 以 `mi:` 开头且 `resolveImageVector` 成功 → 用 `Icon(imageVector, tint = tint)` 渲染
- `iconKey` 以 `mi:` 开头但 `resolveImageVector` 返回 `null` → **回退显示 `defaultEmoji`**，并在 Debug 构建中输出 `Log.w` 告警（`BuildConfig.DEBUG` 守卫）
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
enum class IconCategory {
    ACTION, COMMUNICATION, CONTENT,
    DEVICE, IMAGE, MAPS,
    NAVIGATION, SOCIAL, EDITOR,
    AV, PLACES, HARDWARE
}

data class IconEntry(
    val name: String,
    val style: String,
    val imageVectorProvider: () -> ImageVector,
    val category: IconCategory,
    val keywords: List<String>
)

object MaterialIconCatalog {
    private val _icons: MutableList<IconEntry> = mutableListOf()
    private val _resolveMap: Map<Pair<String, String>, () -> ImageVector> by lazy {
        _icons.associateBy({ it.style to it.name }, { it.imageVectorProvider })
    }
    val icons: List<IconEntry> get() = _icons
    val categories: List<IconCategory> = IconCategory.entries
    fun search(query: String): List<IconEntry>
    fun byCategory(category: IconCategory): List<IconEntry>
    fun resolve(style: String, name: String): ImageVector? = _resolveMap[style to name]?.invoke()
}
```

### 内存策略

- `IconEntry.imageVectorProvider` 存储 `() -> ImageVector` 闭包（如 `{ Icons.Filled.Search }`），而非直接引用 `ImageVector` 实例
- Material Icons 的属性（`Icons.Filled.Home` 等）本身通过 `val` + `lazy` 实现，首次访问时才创建实例
- `_resolveMap` 使用 `by lazy` 延迟构建，仅在首次 `resolve` 调用时初始化
- 结果：启动时零内存开销，图标按需加载后缓存

### 目录策略

- 从 ~2000 个 Material Icons 中精选约 400 个常用图标
- 按 12 个分类组织
- 每个图标附带中英文搜索关键词（中文使用常用昵称如"笑哭"、"搜索"，而非仅官方 CLDR 短名）
- 默认 style 为 `Filled`，部分图标提供 `Outlined` 变体
- 编译期确定（静态列表），无需反射
- 分类标签使用 `stringResource(R.string.icon_category_action)` 等，不硬编码中文（见第 7 节国际化）

## 4. IconPickerSheet 选择器 UI

位置：`core/designsystem/icon/IconPickerSheet.kt`

### 布局结构

```
┌─────────────────────────────┐
│  编辑图标       [🗑️] [✕]   │  ← 标题栏，🗑️ 为"无图标"重置按钮
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
│  手动输入: [📖_________]    │  ← 仅 Emoji Tab，回车即选定
└─────────────────────────────┘
```

### Emoji Tab

- 数据源：基于 Unicode 标准分类，精选常用 emoji（约 800 个）
- 位置：`core/designsystem/icon/EmojiCatalog.kt`
- 分类：常用/表情/手势/动物/食物/交通/活动/物品/自然/符号
- 支持搜索（中文常用名 + 英文 CLDR 名称双覆盖）
- 底部保留手动输入框（最多 4 个 Unicode 码点，见第 1 节编码安全），回车即选定并关闭 Sheet

### 图标库 Tab

- 数据源：`MaterialIconCatalog`
- 分类 chips 横向滚动
- 网格点击选择，自动生成 `mi:Filled:Search` 格式 iconKey

### 交互流程

1. 用户点击表单中的图标圆圈 → 打开 `IconPickerSheet`
2. **网格点击选择** → 立即回填到表单，关闭 Sheet
3. **手动输入框** → 回车键选定，回填并关闭 Sheet
4. **重置按钮（🗑️）** → 回填 `null`（无图标），关闭 Sheet
5. **✕ 按钮** → 取消，不做任何更改

### Tab 状态保持

双 Tab 切换时保持各自的浏览状态（搜索词、滚动位置、选中分类），使用 `rememberSaveable` 或独立 `State` 管理，避免切换 Tab 后丢失浏览进度。

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
| `core/designsystem/icon/IconKeyResolver.kt` | iconKey 解析工具 + 文本化降级 |
| `core/designsystem/icon/IconRenderer.kt` | 统一渲染 Composable |
| `core/designsystem/icon/MaterialIconCatalog.kt` | 精选图标目录数据（懒加载） |
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
- 精选图标数量约 400 个，避免选择器信息过载
- 搜索关键词需覆盖中英文常用名，方便中文用户
- 深色模式下 Material Icon 通过 `tint` 自动适配内容色，emoji 为彩色无需特殊处理，两者视觉差异可接受

## 7. 国际化

所有用户可见的文本必须使用 `stringResource`，不硬编码中文：

| 字符串 Key | 默认值（中文） | 用途 |
|-----------|--------------|------|
| `icon_picker_title` | 编辑图标 | 选择器标题 |
| `icon_picker_tab_emoji` | Emoji | Emoji Tab 标签 |
| `icon_picker_tab_icons` | 图标库 | 图标库 Tab 标签 |
| `icon_picker_search` | 搜索... | 搜索框 placeholder |
| `icon_picker_manual_input` | 手动输入 | 输入框标签 |
| `icon_picker_reset` | 无图标 | 重置按钮 contentDescription |
| `icon_category_action` | 操作 | 分类名称 |
| `icon_category_communication` | 沟通 | 分类名称 |
| ... | ... | 其余分类同模式 |
| `emoji_category_frequent` | 常用 | Emoji 分类名 |
| `emoji_category_expressions` | 表情 | Emoji 分类名 |
| ... | ... | 其余分类同模式 |

字符串资源文件位置：`core/designsystem/src/main/res/values/strings.xml`（与现有 `done`/`cancel` 同文件追加）

## 8. 边界情况与测试要点

实现完成后需针对以下极端情况进行专项测试：

- 很长前缀的非法 iconKey（如 `mi:aaaa:bbbb:cccc`）— `parseMaterialIcon` 的 `split(":", limit = 2)` 应正确处理
- 不存在的 style 组合（如 `mi:TwoTone:NonExistent`）— `resolveImageVector` 返回 null，回退为 defaultEmoji
- Name 包含特殊字符（如 `mi:Filled:<script>`）— 白名单校验拒绝
- 超长 emoji ZWJ 序列 — 手动输入框按 Unicode 码点截断
- 空白 iconKey（`"  "`）— 表单提交时 `trim().ifBlank { null }` 应清空为 null
