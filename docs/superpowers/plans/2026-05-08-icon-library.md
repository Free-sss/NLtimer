# 图标库集成实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 为 Activity/Tag 的 iconKey 字段引入 Material Icons 图标库，实现混合模式选择器，替代当前手动输入 emoji 的方式。

**架构：** 在 core:designsystem 模块新增 icon/ 包，包含解析层（IconKeyResolver）、渲染层（IconRenderer）、数据层（MaterialIconCatalog + EmojiCatalog）和 UI 层（IconPickerSheet）。表单系统通过替换 emojiEditDialog() 集成。所有 iconKey 渲染点统一迁移到 IconRenderer。

**技术栈：** Kotlin, Jetpack Compose, Material3, Material Icons Extended（已有依赖）

**工作树：** D:\2026Code\Group_android\NLtimer\.worktrees\icon-library（分支 feature/icon-library）

---

## 文件结构

### 新增文件

| 文件 | 职责 |
|------|------|
| core/designsystem/src/main/java/com/nltimer/core/designsystem/icon/IconKeyResolver.kt | iconKey 格式解析、白名单校验、文本化降级 |
| core/designsystem/src/main/java/com/nltimer/core/designsystem/icon/IconRenderer.kt | 统一渲染 Composable（emoji / Material Icon / fallback） |
| core/designsystem/src/main/java/com/nltimer/core/designsystem/icon/MaterialIconCatalog.kt | ~400 精选图标目录数据（懒加载）+ 搜索 + 分类 |
| core/designsystem/src/main/java/com/nltimer/core/designsystem/icon/EmojiCatalog.kt | ~800 精选 emoji 分类数据 + 搜索关键词 |
| core/designsystem/src/main/java/com/nltimer/core/designsystem/icon/IconPickerSheet.kt | BottomSheet 双 Tab 图标选择器 UI |

### 修改文件

| 文件 | 改动 |
|------|------|
| core/designsystem/src/main/res/values/strings.xml | 追加国际化字符串 |
| core/designsystem/.../form/GenericFormSheet.kt | 删除 emojiEditDialog()，改用 IconPickerSheet |
| core/designsystem/.../form/GenericFormDialog.kt | 删除 dialogEmojiEditDialog()，改用 IconPickerSheet |
| feature/debug/.../GenericFormSheet.kt | 同步 form/GenericFormSheet.kt 改动 |
| feature/management_activities/.../EditActivityDialog.kt | 统一用 IconPickerSheet |
| feature/management_activities/.../ActivityChip.kt | 用 IconRenderer 替换 emoji 拼接 |
| feature/management_activities/.../ActivityDetailSheet.kt | 用 IconRenderer 替换 |
| feature/home/.../GridCell.kt | 用 IconRenderer 替换 |
| feature/home/.../MomentView.kt | 用 IconRenderer 替换 |
| feature/home/.../MomentFocusCard.kt | 用 IconRenderer 替换 |
| feature/home/.../TimelineReverseView.kt | 用 IconRenderer 替换 |
| feature/home/.../BehaviorLogView.kt | 用 IconRenderer 替换 |
| feature/home/.../CategoryPickerDialog.kt | 用 IconRenderer 替换 |
| feature/home/.../ActivityPicker.kt | 用 IconRenderer 替换 |

---

## 任务 1：IconKeyResolver — 解析与校验工具

**文件：**
- 创建：core/designsystem/src/main/java/com/nltimer/core/designsystem/icon/IconKeyResolver.kt

- [ ] **步骤 1：创建 IconKeyResolver**

在 icon/ 目录下创建 IconKeyResolver.kt：

```kotlin
package com.nltimer.core.designsystem.icon

object IconKeyResolver {
    private const val PREFIX = "mi:"
    private val NAME_PATTERN = Regex("^[A-Za-z0-9_]+$")

    fun isMaterialIcon(iconKey: String): Boolean = iconKey.startsWith(PREFIX)

    fun isMaterialIcon(iconKey: String?): Boolean =
        iconKey != null && isMaterialIcon(iconKey)

    fun parseMaterialIcon(iconKey: String): Pair<String, String>? {
        if (!isMaterialIcon(iconKey)) return null
        val parts = iconKey.removePrefix(PREFIX).split(":", limit = 2)
        if (parts.size != 2) return null
        val (style, name) = parts
        if (!NAME_PATTERN.matches(name)) return null
        return style to name
    }

    fun resolveImageVector(iconKey: String): androidx.compose.ui.graphics.vector.ImageVector? {
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

- [ ] **步骤 2：Commit**

```bash
git add core/designsystem/src/main/java/com/nltimer/core/designsystem/icon/IconKeyResolver.kt
git commit -m "feat(icon): add IconKeyResolver for iconKey parsing and validation"
```

---

## 任务 2：IconRenderer — 统一渲染组件

**文件：**
- 创建：core/designsystem/src/main/java/com/nltimer/core/designsystem/icon/IconRenderer.kt

- [ ] **步骤 1：创建 IconRenderer**

```kotlin
package com.nltimer.core.designsystem.icon

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IconRenderer(
    iconKey: String?,
    modifier: Modifier = Modifier,
    defaultEmoji: String = "📌",
    tint: Color = MaterialTheme.colorScheme.onSurface,
    iconSize: Dp = 24.dp,
    emojiFontSize: TextUnit = TextUnit.Unspecified,
) {
    val resolvedFontSize = if (emojiFontSize == TextUnit.Unspecified) {
        iconSize.value.sp
    } else {
        emojiFontSize
    }

    when {
        iconKey == null -> {
            Text(
                text = defaultEmoji,
                fontSize = resolvedFontSize,
                modifier = modifier,
            )
        }
        IconKeyResolver.isMaterialIcon(iconKey) -> {
            val imageVector = IconKeyResolver.resolveImageVector(iconKey)
            if (imageVector != null) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = IconKeyResolver.iconKeyToDisplayText(iconKey),
                    tint = tint,
                    modifier = modifier,
                )
            } else {
                if (android.util.Log.isLoggable("IconRenderer", android.util.Log.WARN)) {
                    android.util.Log.w("IconRenderer", "Failed to resolve: $iconKey")
                }
                Text(
                    text = defaultEmoji,
                    fontSize = resolvedFontSize,
                    modifier = modifier,
                )
            }
        }
        else -> {
            Text(
                text = iconKey,
                fontSize = resolvedFontSize,
                modifier = modifier,
            )
        }
    }
}
```

- [ ] **步骤 2：Commit**

```bash
git add core/designsystem/src/main/java/com/nltimer/core/designsystem/icon/IconRenderer.kt
git commit -m "feat(icon): add IconRenderer composable for unified icon rendering"
```

---

## 任务 3：MaterialIconCatalog — 图标目录数据

**文件：**
- 创建：core/designsystem/src/main/java/com/nltimer/core/designsystem/icon/MaterialIconCatalog.kt

约 2000 行，包含 ~400 个精选图标条目。此处展示骨架和前 20 个范例，实际实现时需补全。

- [ ] **步骤 1：创建 MaterialIconCatalog**

```kotlin
package com.nltimer.core.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.graphics.vector.ImageVector

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
    private val _icons: List<IconEntry> = buildCatalog()
    private val _resolveMap: Map<Pair<String, String>, () -> ImageVector> by lazy {
        _icons.associateBy({ it.style to it.name }, { it.imageVectorProvider })
    }
    val icons: List<IconEntry> get() = _icons
    val categories: List<IconCategory> = IconCategory.entries

    fun search(query: String): List<IconEntry> {
        if (query.isBlank()) return _icons
        val q = query.lowercase()
        return _icons.filter { entry ->
            entry.name.lowercase().contains(q) ||
                entry.keywords.any { it.lowercase().contains(q) }
        }
    }

    fun byCategory(category: IconCategory): List<IconEntry> =
        _icons.filter { it.category == category }

    fun resolve(style: String, name: String): ImageVector? =
        _resolveMap[style to name]?.invoke()

    private fun buildCatalog(): List<IconEntry> = listOf(
        // ACTION
        IconEntry("Search", "Filled", { Icons.Filled.Search }, IconCategory.ACTION, listOf("搜索", "查找", "search", "find", "magnify")),
        IconEntry("Home", "Filled", { Icons.Filled.Home }, IconCategory.ACTION, listOf("首页", "主页", "home", "house")),
        IconEntry("Home", "Outlined", { Icons.Outlined.Home }, IconCategory.ACTION, listOf("首页", "home")),
        IconEntry("Settings", "Filled", { Icons.Filled.Settings }, IconCategory.ACTION, listOf("设置", "settings", "gear", "配置")),
        IconEntry("Settings", "Outlined", { Icons.Outlined.Settings }, IconCategory.ACTION, listOf("设置", "settings")),
        IconEntry("Add", "Filled", { Icons.Filled.Add }, IconCategory.ACTION, listOf("添加", "新增", "add", "plus")),
        IconEntry("Delete", "Filled", { Icons.Filled.Delete }, IconCategory.ACTION, listOf("删除", "delete", "remove", "移除")),
        IconEntry("Edit", "Filled", { Icons.Filled.Edit }, IconCategory.ACTION, listOf("编辑", "修改", "edit", "pencil")),
        IconEntry("Done", "Filled", { Icons.Filled.Done }, IconCategory.ACTION, listOf("完成", "确认", "done", "check")),
        IconEntry("Close", "Filled", { Icons.Filled.Close }, IconCategory.ACTION, listOf("关闭", "close", "x")),
        IconEntry("Favorite", "Filled", { Icons.Filled.Favorite }, IconCategory.ACTION, listOf("收藏", "喜欢", "favorite", "heart")),
        IconEntry("Favorite", "Outlined", { Icons.Outlined.Favorite }, IconCategory.ACTION, listOf("收藏", "favorite")),
        IconEntry("Star", "Filled", { Icons.Filled.Star }, IconCategory.ACTION, listOf("星标", "star")),
        IconEntry("Star", "Outlined", { Icons.Outlined.Star }, IconCategory.ACTION, listOf("星标", "star")),
        IconEntry("Share", "Filled", { Icons.Filled.Share }, IconCategory.ACTION, listOf("分享", "share")),
        IconEntry("Refresh", "Filled", { Icons.Filled.Refresh }, IconCategory.ACTION, listOf("刷新", "refresh")),
        IconEntry("FilterList", "Filled", { Icons.Filled.FilterList }, IconCategory.ACTION, listOf("筛选", "filter")),
        IconEntry("Flag", "Filled", { Icons.Filled.Flag }, IconCategory.ACTION, listOf("旗帜", "flag", "标记")),
        IconEntry("PushPin", "Filled", { Icons.Filled.PushPin }, IconCategory.ACTION, listOf("图钉", "pin", "固定")),
        // COMMUNICATION
        IconEntry("Call", "Filled", { Icons.Filled.Call }, IconCategory.COMMUNICATION, listOf("电话", "call")),
        IconEntry("Email", "Filled", { Icons.Filled.Email }, IconCategory.COMMUNICATION, listOf("邮件", "email", "mail")),
        IconEntry("Message", "Filled", { Icons.Filled.Message }, IconCategory.COMMUNICATION, listOf("消息", "message", "chat")),
        IconEntry("Notifications", "Filled", { Icons.Filled.Notifications }, IconCategory.COMMUNICATION, listOf("通知", "notification")),
        IconEntry("Notifications", "Outlined", { Icons.Outlined.Notifications }, IconCategory.COMMUNICATION, listOf("通知", "notification")),
        // CONTENT
        IconEntry("Create", "Filled", { Icons.Filled.Create }, IconCategory.CONTENT, listOf("创建", "create", "draw")),
        IconEntry("Folder", "Filled", { Icons.Filled.Folder }, IconCategory.CONTENT, listOf("文件夹", "folder")),
        IconEntry("Link", "Filled", { Icons.Filled.Link }, IconCategory.CONTENT, listOf("链接", "link")),
        IconEntry("List", "Filled", { Icons.Filled.List }, IconCategory.CONTENT, listOf("列表", "list")),
        // DEVICE
        IconEntry("Phone", "Filled", { Icons.Filled.Phone }, IconCategory.DEVICE, listOf("手机", "phone")),
        IconEntry("Lock", "Filled", { Icons.Filled.Lock }, IconCategory.DEVICE, listOf("锁", "lock", "密码")),
        IconEntry("Timer", "Filled", { Icons.Filled.Timer }, IconCategory.DEVICE, listOf("计时器", "timer", "秒表")),
        // IMAGE
        IconEntry("Photo", "Filled", { Icons.Filled.Photo }, IconCategory.IMAGE, listOf("照片", "photo", "image")),
        IconEntry("Palette", "Filled", { Icons.Filled.Palette }, IconCategory.IMAGE, listOf("调色板", "palette", "color")),
        IconEntry("LightMode", "Filled", { Icons.Filled.LightMode }, IconCategory.IMAGE, listOf("亮模式", "light", "sun")),
        // MAPS
        IconEntry("LocationOn", "Filled", { Icons.Filled.LocationOn }, IconCategory.MAPS, listOf("位置", "location")),
        IconEntry("Map", "Filled", { Icons.Filled.Map }, IconCategory.MAPS, listOf("地图", "map")),
        IconEntry("Place", "Filled", { Icons.Filled.Place }, IconCategory.MAPS, listOf("地点", "place")),
        IconEntry("Flight", "Filled", { Icons.Filled.Flight }, IconCategory.MAPS, listOf("飞行", "flight")),
        IconEntry("Public", "Filled", { Icons.Filled.Public }, IconCategory.MAPS, listOf("地球", "public", "globe")),
        // NAVIGATION
        IconEntry("ArrowBack", "Filled", { Icons.Filled.ArrowBack }, IconCategory.NAVIGATION, listOf("返回", "back", "arrow")),
        IconEntry("Menu", "Filled", { Icons.Filled.Menu }, IconCategory.NAVIGATION, listOf("菜单", "menu")),
        // SOCIAL
        IconEntry("Person", "Filled", { Icons.Filled.Person }, IconCategory.SOCIAL, listOf("人", "person", "user")),
        IconEntry("Person", "Outlined", { Icons.Outlined.Person }, IconCategory.SOCIAL, listOf("人", "person")),
        IconEntry("Group", "Filled", { Icons.Filled.Group }, IconCategory.SOCIAL, listOf("群组", "group", "people")),
        // EDITOR
        IconEntry("Info", "Filled", { Icons.Filled.Info }, IconCategory.EDITOR, listOf("信息", "info")),
        IconEntry("Warning", "Filled", { Icons.Filled.Warning }, IconCategory.EDITOR, listOf("警告", "warning")),
        IconEntry("Visibility", "Filled", { Icons.Filled.Visibility }, IconCategory.EDITOR, listOf("可见", "visibility")),
        // AV
        IconEntry("PlayArrow", "Filled", { Icons.Filled.PlayArrow }, IconCategory.AV, listOf("播放", "play")),
        IconEntry("Stop", "Filled", { Icons.Filled.Stop }, IconCategory.AV, listOf("停止", "stop")),
        IconEntry("MusicNote", "Filled", { Icons.Filled.MusicNote }, IconCategory.AV, listOf("音乐", "music")),
        // PLACES
        IconEntry("Store", "Filled", { Icons.Filled.Store }, IconCategory.PLACES, listOf("商店", "store")),
        IconEntry("Work", "Filled", { Icons.Filled.Work }, IconCategory.PLACES, listOf("工作", "work")),
        // HARDWARE
        IconEntry("Build", "Filled", { Icons.Filled.Build }, IconCategory.HARDWARE, listOf("构建", "build", "wrench")),
        IconEntry("Key", "Filled", { Icons.Filled.Key }, IconCategory.HARDWARE, listOf("钥匙", "key")),
        // MISC (mapped to closest categories)
        IconEntry("Search", "Outlined", { Icons.Outlined.Search }, IconCategory.ACTION, listOf("搜索", "search")),
        IconEntry("CalendarMonth", "Filled", { Icons.Filled.CalendarMonth }, IconCategory.ACTION, listOf("日历", "calendar")),
        IconEntry("ShoppingCart", "Filled", { Icons.Filled.ShoppingCart }, IconCategory.ACTION, listOf("购物车", "cart")),
        IconEntry("Check", "Filled", { Icons.Filled.Check }, IconCategory.ACTION, listOf("勾选", "check")),
        IconEntry("Language", "Filled", { Icons.Filled.Language }, IconCategory.ACTION, listOf("语言", "language")),
        IconEntry("Send", "Filled", { Icons.Filled.Send }, IconCategory.ACTION, listOf("发送", "send")),
        // ... 实际实现时扩展至约 400 个
    )
}
```

实际实现时需：
1. 参考 Material Icons 官方目录按分类挑选约 400 个常用图标
2. 每个图标验证 Icons.Filled.XXX 在 material-icons-extended 中存在
3. 补充 Outlined 变体

- [ ] **步骤 2：编译验证**

运行：`./gradlew :core:designsystem:compileDebugKotlin`
预期：PASS

- [ ] **步骤 3：Commit**

```bash
git add core/designsystem/src/main/java/com/nltimer/core/designsystem/icon/MaterialIconCatalog.kt
git commit -m "feat(icon): add MaterialIconCatalog with curated icons"
```

---

## 任务 4：EmojiCatalog — Emoji 分类数据

**文件：**
- 创建：core/designsystem/src/main/java/com/nltimer/core/designsystem/icon/EmojiCatalog.kt

- [ ] **步骤 1：创建 EmojiCatalog**

```kotlin
package com.nltimer.core.designsystem.icon

enum class EmojiCategory {
    FREQUENT, EXPRESSIONS, GESTURES, ANIMALS,
    FOOD, TRAVEL, ACTIVITIES, OBJECTS, NATURE, SYMBOLS
}

data class EmojiEntry(
    val emoji: String,
    val name: String,
    val category: EmojiCategory,
    val keywords: List<String>
)

object EmojiCatalog {
    private val _emojis: List<EmojiEntry> = buildCatalog()
    val emojis: List<EmojiEntry> get() = _emojis
    val categories: List<EmojiCategory> = EmojiCategory.entries

    fun search(query: String): List<EmojiEntry> {
        if (query.isBlank()) return _emojis
        val q = query.lowercase()
        return _emojis.filter { entry ->
            entry.name.lowercase().contains(q) ||
                entry.keywords.any { it.lowercase().contains(q) }
        }
    }

    fun byCategory(category: EmojiCategory): List<EmojiEntry> =
        _emojis.filter { it.category == category }

    private fun buildCatalog(): List<EmojiEntry> = listOf(
        // FREQUENT
        EmojiEntry("😀", "grinning", EmojiCategory.FREQUENT, listOf("笑", "开心", "grin", "smile")),
        EmojiEntry("👍", "thumbs_up", EmojiCategory.FREQUENT, listOf("赞", "好", "thumbs", "like")),
        EmojiEntry("❤️", "heart", EmojiCategory.FREQUENT, listOf("心", "爱", "heart", "love")),
        EmojiEntry("🎉", "party", EmojiCategory.FREQUENT, listOf("庆祝", "派对", "party")),
        EmojiEntry("🔥", "fire", EmojiCategory.FREQUENT, listOf("火", "热门", "fire", "hot")),
        EmojiEntry("⭐", "star", EmojiCategory.FREQUENT, listOf("星", "收藏", "star")),
        EmojiEntry("✅", "check", EmojiCategory.FREQUENT, listOf("完成", "勾", "check", "done")),
        EmojiEntry("💡", "bulb", EmojiCategory.FREQUENT, listOf("灯泡", "想法", "idea", "bulb")),
        // EXPRESSIONS
        EmojiEntry("😊", "smile", EmojiCategory.EXPRESSIONS, listOf("微笑", "smile")),
        EmojiEntry("😂", "joy", EmojiCategory.EXPRESSIONS, listOf("笑哭", "joy", "laugh")),
        EmojiEntry("🤔", "thinking", EmojiCategory.EXPRESSIONS, listOf("思考", "thinking")),
        EmojiEntry("😅", "sweat_smile", EmojiCategory.EXPRESSIONS, listOf("尴尬", "sweat")),
        EmojiEntry("😎", "cool", EmojiCategory.EXPRESSIONS, listOf("酷", "cool")),
        EmojiEntry("🥰", "smiling_hearts", EmojiCategory.EXPRESSIONS, listOf("喜欢", "love")),
        EmojiEntry("😡", "angry", EmojiCategory.EXPRESSIONS, listOf("生气", "angry")),
        EmojiEntry("😢", "cry", EmojiCategory.EXPRESSIONS, listOf("哭", "cry")),
        EmojiEntry("😭", "sob", EmojiCategory.EXPRESSIONS, listOf("大哭", "sob")),
        EmojiEntry("🤣", "rofl", EmojiCategory.EXPRESSIONS, listOf("笑死", "rofl")),
        EmojiEntry("😌", "relieved", EmojiCategory.EXPRESSIONS, listOf("放松", "relieved")),
        EmojiEntry("😱", "scream", EmojiCategory.EXPRESSIONS, listOf("惊恐", "scream")),
        EmojiEntry("🥺", "pleading", EmojiCategory.EXPRESSIONS, listOf("请求", "pleading")),
        EmojiEntry("😴", "sleeping", EmojiCategory.EXPRESSIONS, listOf("睡觉", "sleeping")),
        EmojiEntry("🤗", "hugging", EmojiCategory.EXPRESSIONS, listOf("拥抱", "hug")),
        EmojiEntry("😏", "smirk", EmojiCategory.EXPRESSIONS, listOf("得意", "smirk")),
        EmojiEntry("🤩", "star_struck", EmojiCategory.EXPRESSIONS, listOf("眼冒星", "amazed")),
        // GESTURES
        EmojiEntry("👋", "wave", EmojiCategory.GESTURES, listOf("挥手", "hello", "wave")),
        EmojiEntry("👏", "clap", EmojiCategory.GESTURES, listOf("鼓掌", "clap")),
        EmojiEntry("🤝", "handshake", EmojiCategory.GESTURES, listOf("握手", "handshake")),
        EmojiEntry("✌️", "peace", EmojiCategory.GESTURES, listOf("和平", "peace")),
        EmojiEntry("🤞", "crossed_fingers", EmojiCategory.GESTURES, listOf("祈祷", "luck")),
        EmojiEntry("👊", "fist", EmojiCategory.GESTURES, listOf("拳头", "fist")),
        EmojiEntry("✊", "raised_fist", EmojiCategory.GESTURES, listOf("加油", "solidarity")),
        EmojiEntry("🙌", "raised_hands", EmojiCategory.GESTURES, listOf("举手", "celebration")),
        EmojiEntry("💪", "muscle", EmojiCategory.GESTURES, listOf("力量", "muscle", "strong")),
        // ANIMALS
        EmojiEntry("🐱", "cat", EmojiCategory.ANIMALS, listOf("猫", "cat")),
        EmojiEntry("🐶", "dog", EmojiCategory.ANIMALS, listOf("狗", "dog")),
        EmojiEntry("🦊", "fox", EmojiCategory.ANIMALS, listOf("狐狸", "fox")),
        EmojiEntry("🐼", "panda", EmojiCategory.ANIMALS, listOf("熊猫", "panda")),
        EmojiEntry("🦋", "butterfly", EmojiCategory.ANIMALS, listOf("蝴蝶", "butterfly")),
        EmojiEntry("🐙", "octopus", EmojiCategory.ANIMALS, listOf("章鱼", "octopus")),
        EmojiEntry("🦄", "unicorn", EmojiCategory.ANIMALS, listOf("独角兽", "unicorn")),
        EmojiEntry("🐝", "bee", EmojiCategory.ANIMALS, listOf("蜜蜂", "bee")),
        EmojiEntry("🦉", "owl", EmojiCategory.ANIMALS, listOf("猫头鹰", "owl")),
        EmojiEntry("🐬", "dolphin", EmojiCategory.ANIMALS, listOf("海豚", "dolphin")),
        // FOOD
        EmojiEntry("🍎", "apple", EmojiCategory.FOOD, listOf("苹果", "apple")),
        EmojiEntry("🍕", "pizza", EmojiCategory.FOOD, listOf("披萨", "pizza")),
        EmojiEntry("☕", "coffee", EmojiCategory.FOOD, listOf("咖啡", "coffee")),
        EmojiEntry("🍜", "ramen", EmojiCategory.FOOD, listOf("面", "ramen")),
        EmojiEntry("🍰", "cake", EmojiCategory.FOOD, listOf("蛋糕", "cake")),
        EmojiEntry("🍺", "beer", EmojiCategory.FOOD, listOf("啤酒", "beer")),
        EmojiEntry("🍣", "sushi", EmojiCategory.FOOD, listOf("寿司", "sushi")),
        // TRAVEL
        EmojiEntry("🚗", "car", EmojiCategory.TRAVEL, listOf("汽车", "car")),
        EmojiEntry("✈️", "airplane", EmojiCategory.TRAVEL, listOf("飞机", "airplane")),
        EmojiEntry("🚀", "rocket", EmojiCategory.TRAVEL, listOf("火箭", "rocket")),
        EmojiEntry("🚲", "bike", EmojiCategory.TRAVEL, listOf("自行车", "bike")),
        EmojiEntry("🏠", "house", EmojiCategory.TRAVEL, listOf("家", "house")),
        // ACTIVITIES
        EmojiEntry("⚽", "soccer", EmojiCategory.ACTIVITIES, listOf("足球", "soccer")),
        EmojiEntry("🎮", "game", EmojiCategory.ACTIVITIES, listOf("游戏", "game")),
        EmojiEntry("📖", "book", EmojiCategory.ACTIVITIES, listOf("书", "阅读", "book")),
        EmojiEntry("🎵", "music", EmojiCategory.ACTIVITIES, listOf("音乐", "music")),
        EmojiEntry("🎬", "movie", EmojiCategory.ACTIVITIES, listOf("电影", "movie")),
        EmojiEntry("🏋️", "weightlift", EmojiCategory.ACTIVITIES, listOf("健身", "exercise")),
        EmojiEntry("🏃", "runner", EmojiCategory.ACTIVITIES, listOf("跑步", "run")),
        EmojiEntry("🎨", "art", EmojiCategory.ACTIVITIES, listOf("画画", "art")),
        EmojiEntry("🎸", "guitar", EmojiCategory.ACTIVITIES, listOf("吉他", "guitar")),
        EmojiEntry("🏊", "swim", EmojiCategory.ACTIVITIES, listOf("游泳", "swim")),
        EmojiEntry("📸", "camera", EmojiCategory.ACTIVITIES, listOf("拍照", "camera")),
        // OBJECTS
        EmojiEntry("📱", "phone", EmojiCategory.OBJECTS, listOf("手机", "phone")),
        EmojiEntry("💻", "laptop", EmojiCategory.OBJECTS, listOf("电脑", "computer")),
        EmojiEntry("💰", "money", EmojiCategory.OBJECTS, listOf("钱", "money")),
        EmojiEntry("🔑", "key", EmojiCategory.OBJECTS, listOf("钥匙", "key")),
        EmojiEntry("📦", "package", EmojiCategory.OBJECTS, listOf("包裹", "package")),
        EmojiEntry("🔖", "bookmark", EmojiCategory.OBJECTS, listOf("书签", "bookmark")),
        EmojiEntry("📌", "pin", EmojiCategory.OBJECTS, listOf("图钉", "pin")),
        EmojiEntry("🏷️", "label", EmojiCategory.OBJECTS, listOf("标签", "label")),
        EmojiEntry("🔧", "wrench", EmojiCategory.OBJECTS, listOf("扳手", "wrench")),
        EmojiEntry("📺", "tv", EmojiCategory.OBJECTS, listOf("电视", "tv")),
        EmojiEntry("💼", "briefcase", EmojiCategory.OBJECTS, listOf("公文包", "work")),
        // NATURE
        EmojiEntry("🌸", "cherry_blossom", EmojiCategory.NATURE, listOf("花", "樱花", "blossom")),
        EmojiEntry("🌈", "rainbow", EmojiCategory.NATURE, listOf("彩虹", "rainbow")),
        EmojiEntry("☀️", "sun", EmojiCategory.NATURE, listOf("太阳", "sun")),
        EmojiEntry("🌙", "moon", EmojiCategory.NATURE, listOf("月亮", "moon")),
        EmojiEntry("🌊", "ocean", EmojiCategory.NATURE, listOf("海浪", "ocean")),
        EmojiEntry("🍂", "leaf", EmojiCategory.NATURE, listOf("叶子", "leaf")),
        EmojiEntry("❄️", "snowflake", EmojiCategory.NATURE, listOf("雪花", "snow")),
        // SYMBOLS
        EmojiEntry("💯", "100", EmojiCategory.SYMBOLS, listOf("满分", "100")),
        EmojiEntry("⏰", "alarm", EmojiCategory.SYMBOLS, listOf("闹钟", "alarm")),
        EmojiEntry("🔔", "bell", EmojiCategory.SYMBOLS, listOf("铃铛", "bell")),
        EmojiEntry("♻️", "recycle", EmojiCategory.SYMBOLS, listOf("回收", "recycle")),
        EmojiEntry("➕", "plus", EmojiCategory.SYMBOLS, listOf("加", "plus")),
        EmojiEntry("➖", "minus", EmojiCategory.SYMBOLS, listOf("减", "minus")),
        EmojiEntry("❓", "question", EmojiCategory.SYMBOLS, listOf("问号", "question")),
        EmojiEntry("❗", "exclamation", EmojiCategory.SYMBOLS, listOf("感叹", "exclamation")),
        EmojiEntry("🏷️", "tag", EmojiCategory.SYMBOLS, listOf("标签", "tag")),
        // ... 实际实现时扩展至约 800 个
    )
}
```

- [ ] **步骤 2：Commit**

```bash
git add core/designsystem/src/main/java/com/nltimer/core/designsystem/icon/EmojiCatalog.kt
git commit -m "feat(icon): add EmojiCatalog with curated emojis"
```

---

## 任务 5：国际化字符串资源

**文件：**
- 修改：core/designsystem/src/main/res/values/strings.xml

- [ ] **步骤 1：追加字符串资源**

将 strings.xml 内容替换为：

```xml
<resources>
    <string name="done">完成</string>
    <string name="cancel">取消</string>

    <string name="icon_picker_title">编辑图标</string>
    <string name="icon_picker_tab_emoji">Emoji</string>
    <string name="icon_picker_tab_icons">图标库</string>
    <string name="icon_picker_search">搜索…</string>
    <string name="icon_picker_manual_input">手动输入</string>
    <string name="icon_picker_reset">无图标</string>
    <string name="icon_picker_confirm">确定</string>

    <string name="icon_category_action">操作</string>
    <string name="icon_category_communication">沟通</string>
    <string name="icon_category_content">内容</string>
    <string name="icon_category_device">设备</string>
    <string name="icon_category_image">图像</string>
    <string name="icon_category_maps">地图</string>
    <string name="icon_category_navigation">导航</string>
    <string name="icon_category_social">社交</string>
    <string name="icon_category_editor">编辑</string>
    <string name="icon_category_av">影音</string>
    <string name="icon_category_places">地点</string>
    <string name="icon_category_hardware">硬件</string>

    <string name="emoji_category_frequent">常用</string>
    <string name="emoji_category_expressions">表情</string>
    <string name="emoji_category_gestures">手势</string>
    <string name="emoji_category_animals">动物</string>
    <string name="emoji_category_food">食物</string>
    <string name="emoji_category_travel">交通</string>
    <string name="emoji_category_activities">活动</string>
    <string name="emoji_category_objects">物品</string>
    <string name="emoji_category_nature">自然</string>
    <string name="emoji_category_symbols">符号</string>
</resources>
```

- [ ] **步骤 2：Commit**

```bash
git add core/designsystem/src/main/res/values/strings.xml
git commit -m "feat(icon): add i18n string resources for icon picker"
```

---

## 任务 6：IconPickerSheet — 图标选择器 UI

**文件：**
- 创建：core/designsystem/src/main/java/com/nltimer/core/designsystem/icon/IconPickerSheet.kt

- [ ] **步骤 1：创建 IconPickerSheet**

创建完整的 IconPickerSheet 组件，包含：
1. `IconPickerSheet` — 主 Composable，ModalBottomSheet + Tab 切换
2. `EmojiTab` — Emoji 网格 + 手动输入框
3. `MaterialIconTab` — Material Icon 网格
4. `truncateToCodePoints` — Unicode 码点截断工具

组件结构：
- 标题栏：标题 + 重置按钮（Icons.Outlined.DeleteOutline）
- PrimaryTabRow：Emoji / 图标库 两个 Tab
- 搜索框（两 Tab 共享逻辑，各自内部维护 searchQuery）
- 分类 FilterChip（FlowRow）
- LazyVerticalGrid 图标网格
- Emoji Tab 底部：OutlinedTextField + 确定按钮（回车即选定）

关键实现细节：
- 使用 `rememberSaveable` 保持各 Tab 的搜索和分类状态
- Emoji Tab 手动输入：`KeyboardOptions(imeAction = ImeAction.Done)`，`KeyboardActions(onDone = { onIconSelected(manualInput) })`
- 图标网格点击 → 立即回填 `mi:Filled:Search` 格式 iconKey
- 分类字符串通过 `stringResource(when(category))` 映射
- `truncateToCodePoints` 使用 `BreakIterator.getCharacterInstance()` 计数

- [ ] **步骤 2：编译验证**

运行：`./gradlew :core:designsystem:compileDebugKotlin`
预期：PASS

- [ ] **步骤 3：Commit**

```bash
git add core/designsystem/src/main/java/com/nltimer/core/designsystem/icon/IconPickerSheet.kt
git commit -m "feat(icon): add IconPickerSheet with dual-tab picker"
```

---

## 任务 7：替换表单系统中的 emojiEditDialog

**文件：**
- 修改：core/designsystem/src/main/java/com/nltimer/core/designsystem/form/GenericFormSheet.kt
- 修改：core/designsystem/src/main/java/com/nltimer/core/designsystem/form/GenericFormDialog.kt

- [ ] **步骤 1：修改 GenericFormSheet.kt**

1. 添加 import：
```kotlin
import com.nltimer.core.designsystem.icon.IconPickerSheet
import com.nltimer.core.designsystem.icon.IconRenderer
```

2. 在 `iconColorRenderer()` 中：
   - 将 `showEmojiEditor` 重命名为 `showIconPicker`
   - 将 `showEmojiEditor = true` 改为 `showIconPicker = true`
   - 将图标圆圈中的 `Text(emoji)` 替换为 `IconRenderer(iconKey = emoji.ifBlank { null }, iconSize = 20.dp)`
   - 将 `emojiEditDialog(...)` 调用替换为 `IconPickerSheet(currentIconKey = emoji, onIconSelected = { newKey -> onEmojiChange(newKey ?: ""); showIconPicker = false }, onDismiss = { showIconPicker = false })`

3. 删除整个 `emojiEditDialog` 函数（约 333-362 行）

- [ ] **步骤 2：修改 GenericFormDialog.kt**

同理：
1. 添加 import
2. 在 `dialogIconColorRenderer()` 中替换 `dialogEmojiEditDialog` 为 `IconPickerSheet`
3. `Text(emoji)` → `IconRenderer`
4. 删除整个 `dialogEmojiEditDialog` 函数（约 279-308 行）

- [ ] **步骤 3：编译验证**

运行：`./gradlew :core:designsystem:compileDebugKotlin`
预期：PASS

- [ ] **步骤 4：Commit**

```bash
git add core/designsystem/src/main/java/com/nltimer/core/designsystem/form/GenericFormSheet.kt core/designsystem/src/main/java/com/nltimer/core/designsystem/form/GenericFormDialog.kt
git commit -m "refactor(form): replace emojiEditDialog with IconPickerSheet"
```

---

## 任务 8：同步修改 debug 和 legacy 表单

**文件：**
- 修改：feature/debug/src/main/java/com/nltimer/feature/debug/ui/GenericFormSheet.kt
- 修改：feature/management_activities/.../dialogs/EditActivityDialog.kt

- [ ] **步骤 1：修改 debug GenericFormSheet**

与任务 7 相同模式：删除 EmojiEditDialog，替换为 IconPickerSheet。

- [ ] **步骤 2：修改 EditActivityDialog**

将直接 OutlinedTextField（max 2 chars）替换为 IconRenderer + IconPickerSheet：
- 添加 `var showIconPicker by remember { mutableStateOf(false) }`
- 将 OutlinedTextField 替换为可点击的 IconRenderer
- 添加 IconPickerSheet 弹窗

- [ ] **步骤 3：编译验证**

运行：`./gradlew :feature:debug:compileDebugKotlin :feature:management_activities:compileDebugKotlin`
预期：PASS

- [ ] **步骤 4：Commit**

```bash
git add feature/debug/ feature/management_activities/
git commit -m "refactor: sync debug and legacy forms to IconPickerSheet"
```

---

## 任务 9：替换所有渲染点为 IconRenderer

**文件：**
- 修改：feature/management_activities/.../ActivityChip.kt
- 修改：feature/management_activities/.../ActivityDetailSheet.kt
- 修改：feature/home/.../GridCell.kt
- 修改：feature/home/.../MomentView.kt
- 修改：feature/home/.../MomentFocusCard.kt
- 修改：feature/home/.../TimelineReverseView.kt
- 修改：feature/home/.../BehaviorLogView.kt
- 修改：feature/home/.../CategoryPickerDialog.kt
- 修改：feature/home/.../ActivityPicker.kt

每个文件通用改法：
1. 添加 `import com.nltimer.core.designsystem.icon.IconRenderer`
2. 将 emoji 字符串拼接（如 `"${iconKey} ${name}"`）替换为 `Row { IconRenderer(...); Spacer; Text(name) }`
3. 将 `Text(iconKey ?: "📌")` 替换为 `IconRenderer(iconKey = iconKey, defaultEmoji = "📌", iconSize = ...)`

iconSize 建议：
- ActivityChip: 16.dp
- ActivityDetailSheet: 48.dp
- GridCell: 14.dp
- MomentView/MomentFocusCard/TimelineReverseView/BehaviorLogView: 16.dp
- CategoryPickerDialog/ActivityPicker: 20.dp

- [ ] **步骤 1：逐文件替换**

按上述列表逐个修改 9 个文件。

- [ ] **步骤 2：编译验证**

运行：`./gradlew :feature:home:compileDebugKotlin :feature:management_activities:compileDebugKotlin`
预期：PASS

- [ ] **步骤 3：Commit**

```bash
git add feature/home/ feature/management_activities/
git commit -m "refactor: replace all emoji rendering with IconRenderer"
```

---

## 任务 10：完整构建与验证

- [ ] **步骤 1：完整项目构建**

运行：`./gradlew assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 2：视觉验证清单**

在设备/模拟器上手动验证：
1. Activity 编辑表单 → 点击图标圆圈 → IconPickerSheet 弹出
2. Emoji Tab：搜索、分类切换、点击选择、手动输入回车
3. 图标库 Tab：搜索、分类切换、点击选择 → iconKey 为 mi:Filled:Search
4. 重置按钮 → iconKey 清空
5. 矢量图标在各屏幕正确渲染（着色、尺寸）
6. 现有 emoji iconKey 数据无异常

- [ ] **步骤 3：最终 Commit**

```bash
git add -A
git commit -m "feat(icon): complete icon library integration"
```
