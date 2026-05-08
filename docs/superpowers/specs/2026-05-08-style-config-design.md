# 样式风格可配置化设计规格

## 背景

NLtimer 项目中存在大量硬编码的视觉参数：~30 处圆角、~18 处 Alpha 透明度、~10 处边框宽度散布于 18+ 个文件中。现有 Theme 系统已支持色彩/字体/暗色模式等配置，但圆角、边框、透明度不受用户控制。

本规格将这三类核心视觉参数从硬编码转为用户可配置，采用混合模式（预设档位 + 高级自定义滑块），全局统一生效。

## 配置范围

### 首批开放（本次实现）

- 全局圆角档位（比例缩放）
- 全局边框粗细档位（比例缩放）
- 全局透明度 Alpha 档位（比例缩放）

### 延后（二期迭代）

- 组件固定尺寸、卡片高度、侧边栏宽度
- 独立字号大小、阴影参数

## 数据模型

### 枚举类

```kotlin
// core/designsystem/.../StyleConfig.kt

enum class CornerPreset(val scale: Float) {
    COMPACT(0.25f),   // 4dp→1dp, 12dp→3dp, 16dp→4dp, 28dp→7dp, 32dp→8dp
    STANDARD(1.0f),   // 保持现有硬编码值
    ROUNDED(2.0f),    // 12dp→24dp, 16dp→32dp, 32dp→64dp(cap)
    SOFT(3.0f);       // 趋向全圆角
    companion object { val DEFAULT = STANDARD }
}

enum class BorderPreset(val scale: Float) {
    NONE(0.0f),       // 无边框
    THIN(0.5f),       // 1dp→0.5dp, 2dp→1dp
    STANDARD(1.0f),   // 保持现有值
    THICK(2.0f);      // 1dp→2dp, 2dp→4dp
    companion object { val DEFAULT = STANDARD }
}

enum class AlphaPreset(val scale: Float) {
    SUBTLE(0.5f),     // 所有alpha减半
    STANDARD(1.0f),   // 保持现有值
    VIVID(1.5f),      // alpha增强50%（cap 1.0）
    SOLID(2.0f);      // 趋向不透明
    companion object { val DEFAULT = STANDARD }
}
```

### StyleConfig data class

```kotlin
data class StyleConfig(
    val cornerPreset: CornerPreset = CornerPreset.DEFAULT,
    val borderPreset: BorderPreset = BorderPreset.DEFAULT,
    val alphaPreset: AlphaPreset = AlphaPreset.DEFAULT,
    // 高级自定义：null 时跟随 preset，非 null 时覆盖
    val cornerScale: Float? = null,
    val borderScale: Float? = null,
    val alphaScale: Float? = null,
)
```

### Theme 扩展

```kotlin
data class Theme(
    // ...existing fields unchanged...
    val showBorders: Boolean = true,      // 保留，与 borderPreset 联动
    val style: StyleConfig = StyleConfig(), // 新增
)
```

### 辅助计算属性

```kotlin
fun StyleConfig.effectiveCornerScale(): Float = cornerScale ?: cornerPreset.scale
fun StyleConfig.effectiveBorderScale(): Float = borderScale ?: borderPreset.scale
fun StyleConfig.effectiveAlphaScale(): Float = alphaScale ?: alphaPreset.scale
```

### 关键规则

- `cornerScale`/`borderScale`/`alphaScale` 为 null 时跟随预设档位，非 null 时用户自定义覆盖
- `showBorders` 保留作为兼容字段，关闭时等同于 `borderPreset = NONE`；选择非 NONE 预设时自动开启 showBorders
- 比例缩放上界保护：corner cap 50dp，border cap 8dp，alpha cap 1.0f
- 所有配置全局统一生效，不做单页面单独配置

## 持久化 & SettingsPrefs 扩展

### SettingsPrefs 接口新增

```kotlin
interface SettingsPrefs {
    // ...existing methods...
    fun getStyleConfigFlow(): Flow<StyleConfig>
    suspend fun updateStyleConfig(config: StyleConfig)
}
```

### SettingsPrefsImpl 新增偏好键

```kotlin
object StyleKeys {
    val CORNER_PRESET = stringPreferencesKey("corner_preset")
    val BORDER_PRESET = stringPreferencesKey("border_preset")
    val ALPHA_PRESET = stringPreferencesKey("alpha_preset")
    val CORNER_SCALE = floatPreferencesKey("corner_scale_custom")
    val BORDER_SCALE = floatPreferencesKey("border_scale_custom")
    val ALPHA_SCALE = floatPreferencesKey("alpha_scale_custom")
}
```

### 存储逻辑

- enum 存为 `name` 字符串，读取时 `CornerPreset.valueOf(name)` 回落默认值
- `cornerScale`/`borderScale`/`alphaScale` 存为 float，缺少时返回 null（跟随预设）
- `getThemeFlow()` 中合并读取：将 `style: StyleConfig` 组装进 `Theme` 返回，保持单一数据源

### ThemeSettingsViewModel 扩展

```kotlin
fun updateStyleConfig(config: StyleConfig) { ... }
fun setCornerPreset(preset: CornerPreset) { ... }
fun setBorderPreset(preset: BorderPreset) { ... }
fun setAlphaPreset(preset: AlphaPreset) { ... }
fun setCustomCornerScale(scale: Float?) { ... }
fun setCustomBorderScale(scale: Float?) { ... }
fun setCustomAlphaScale(scale: Float?) { ... }
```

## 硬编码替换策略

### Token 定义（新文件）

```kotlin
// core/designsystem/.../ShapeTokens.kt
object ShapeTokens {
    const val CORNER_EXTRA_SMALL = 4    // TimeFloatingLabel, DualTimePicker highlight
    const val CORNER_SMALL = 6          // DialogConfig selectors, ActivityChip small
    const val CORNER_MEDIUM = 12        // GridCell, TimeSideBar, Timeline items, RouteSettings
    const val CORNER_LARGE = 16         // GridCellEmpty/Locked, BehaviorCard, Settings card
    const val CORNER_EXTRA_LARGE = 28   // Dialog outer, ColorPicker
    const val CORNER_FULL = 32          // MomentFocusCard
    const val CORNER_PILL = 36          // SlideActionPill
}

object BorderTokens {
    const val THIN = 1       // locked cell, timeline idle, chips
    const val STANDARD = 2   // active cell, focus card, empty cell
}
```

Alpha 不抽 token — alpha 值与颜色语义强耦合，抽 token 增加理解成本，直接在组件处 `styledAlpha(baseAlpha)`。

### 辅助扩展函数（新文件）

```kotlin
// core/designsystem/.../StyleExt.kt
@Composable
fun styledCorner(baseDp: Float): Dp {
    val scale = LocalTheme.current.style.effectiveCornerScale()
    return (baseDp * scale).coerceIn(0.dp, 50.dp)
}

@Composable
fun styledBorder(baseDp: Float): Dp {
    val scale = LocalTheme.current.style.effectiveBorderScale()
    return (baseDp * scale).coerceIn(0.dp, 8.dp)
}

@Composable
fun styledAlpha(baseAlpha: Float): Float {
    val scale = LocalTheme.current.style.effectiveAlphaScale()
    return (baseAlpha * scale).coerceIn(0f, 1f)
}
```

### 替换示例

```kotlin
// GridCell.kt — 替换前
RoundedCornerShape(12.dp)
primaryContainer.copy(alpha = 0.3f)
BorderStroke(2.dp, ...)

// GridCell.kt — 替换后
RoundedCornerShape(styledCorner(ShapeTokens.CORNER_MEDIUM))
primaryContainer.copy(alpha = styledAlpha(0.3f))
BorderStroke(styledBorder(BorderTokens.STANDARD), ...)
```

### 替换范围

| 分类 | 涉及文件数 | 替换处数 |
|------|-----------|---------|
| 圆角 `RoundedCornerShape` | 14 | ~30 |
| Alpha `.copy(alpha=)` | 8 | ~18 |
| 边框 `BorderStroke`/`border` | 8 | ~10 |
| **合计** | **~18** | **~58** |

### 兼容性

数值本身不改动，只包裹 `styledXxx()` 调用。STANDARD 档 scale=1.0，行为 100% 向后兼容。

## 设置页面 UI

### 入口

ThemeSettingsScreen 现有 `showBorders` 开关之后新增"样式风格"分区。

### 布局

```
┌─────────────────────────────────┐
│  ← Theme Config                 │
├─────────────────────────────────┤
│  [现有] AppTheme / Material You │
│  [现有] Seed Color / Palette    │
│  [现有] Font / AMOLED           │
│  [现有] Show Borders            │
├─────────────────────────────────┤
│  样式风格                        │  ← 新分区
│                                 │
│  圆角   [紧凑][标准][圆润][超圆] │  ← SegmentedButton
│  边框   [无边][纤细][标准][粗厚] │
│  透明度 [淡雅][标准][鲜明][实心] │
│                                 │
│  ─── 高级自定义 ───              │  ← 可折叠区域
│  (展开后显示)                    │
│  圆角缩放   ━━━●━━━━━ 1.0x     │  ← Slider 0.0-3.0
│  边框缩放   ━━━●━━━━━ 1.0x     │  ← Slider 0.0-2.0
│  透明度缩放 ━━━●━━━━━ 1.0x     │  ← Slider 0.0-2.0
│                                 │
│  [重置为默认]                    │  ← 一键恢复 STANDARD
└─────────────────────────────────┘
```

### 交互逻辑

1. **预设档位** — 点击即生效，同时将对应 `customScale` 置为 null
2. **高级 Slider** — 拖动时自动选中"自定义"状态，`customScale` 设为 slider 值
3. **双向联动** — 选择预设时 slider 跟随移动到预设 scale 值；拖动 slider 时预设档位取消选中
4. **重置按钮** — 三个参数全部恢复 STANDARD + customScale = null
5. **实时预览** — 修改即刻反映到界面（Theme Flow 驱动重组）

### 组件选型

- 预设档位：`SingleChoiceSegmentedButtonRow`（M3 标准组件）
- 高级区折叠：`AnimatedVisibility` + 点击文字切换
- Slider：M3 `Slider`
- 分区标题：与现有 ThemeSettings 页面风格一致

### showBorders 联动

- 关闭 showBorders → 自动设 `borderPreset = NONE`
- 选择非 NONE 预设 → 自动开启 showBorders

## 开发阶段

1. **阶段1：结构改造** — 扩展 Theme/StyleConfig/SettingsPrefs/ViewModel
2. **阶段2：硬编码替换** — 58 处替换，新建 ShapeTokens/BorderTokens/StyleExt
3. **阶段3：设置页面 UI** — ThemeSettingsScreen 新增样式风格分区
4. **阶段4：自测收尾** — 全页面校验 + 机型适配 + 合并

## Git 工作树

- 分支名：`feature/theme-config-style`
- 所有改动在工作树完成，不干扰主工程
