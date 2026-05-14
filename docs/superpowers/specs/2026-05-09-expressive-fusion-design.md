# Expressive Fusion 设计规格

> 日期：2026-05-09
> 来源：Momentum × Reef 样式风格提取报告
> 策略：方案 C——可切换表达力，精华适配 12 个借鉴点

---

## 1. 总览

NLtimer 已有高度可配置的 StyleConfig 体系（cornerPreset / borderPreset / alphaPreset）。本设计在此基础上新增 **ExpressivenessPreset（表达力预设）** 作为"主旋钮"——切换它时自动联动所有子维度，高级用户仍可单独微调。

三档表达力：

| 预设 | 定位 | 氛围关键词 |
|------|------|-----------|
| SUBDUED | 克制 | 灰底、紧凑、无装饰 |
| STANDARD | 标准 | 浅染、适度圆角、轻交互 |
| EXPRESSIVE | 表达 | 多色磁贴、大圆角、衬线、变形 |

---

## 2. 数据模型变更

### 2.1 新增枚举

```kotlin
enum class ExpressivenessPreset { SUBDUED, STANDARD, EXPRESSIVE }

enum class CardColorStrategy { SURFACE, TINTED_PRIMARY, MULTI_CONTAINER }

enum class IconContainerSize { NONE, CIRCLE_SMALL, CIRCLE_LARGE }
// NONE=不显示, CIRCLE_SMALL=36dp, CIRCLE_LARGE=44dp

enum class TimerTypography { HEADLINE, DISPLAY_SMALL, DISPLAY_LARGE_SERIF }
// HEADLINE=32sp sans, DISPLAY_SMALL=36sp sans, DISPLAY_LARGE_SERIF=57sp serif(DM Serif Text)

enum class PressedShapeLevel { OFF, MILD, FULL_MORPH }
// OFF=仅缩放0.95, MILD=RoundedCornerShape(24%), FULL_MORPH=CircleShape→8dp

enum class WavyProgressLevel { OFF, ON, PROMINENT }
// OFF=LinearProgressIndicator, ON/PROMINENT=LinearWavyProgressIndicator(不同amplitude)
```

### 2.2 StyleConfig 扩展

```kotlin
data class StyleConfig(
    val cornerPreset: CornerPreset = STANDARD,
    val borderPreset: BorderPreset = STANDARD,
    val alphaPreset: AlphaPreset = STANDARD,
    // === 新增 ===
    val expressiveness: ExpressivenessPreset = SUBDUED,
    val cardColorStrategy: CardColorStrategy = SURFACE,
    val iconContainerSize: IconContainerSize = NONE,
    val timerTypography: TimerTypography = HEADLINE,
    val pressedShape: PressedShapeLevel = OFF,
    val wavyProgress: WavyProgressLevel = OFF,
)
```

### 2.3 一键预设映射

```kotlin
fun ExpressivenessPreset.toStyleConfig(): StyleConfig = when (this) {
    SUBDUED -> StyleConfig(
        cornerPreset = COMPACT,      // 0.25x
        borderPreset = STANDARD,     // 1x
        alphaPreset = SUBTLE,        // 0.5x
        expressiveness = SUBDUED,
        cardColorStrategy = SURFACE,
        iconContainerSize = NONE,
        timerTypography = HEADLINE,
        pressedShape = OFF,
        wavyProgress = OFF,
    )
    STANDARD -> StyleConfig(
        cornerPreset = STANDARD,     // 1x
        borderPreset = THIN,         // 0.5x
        alphaPreset = STANDARD,      // 1x
        expressiveness = STANDARD,
        cardColorStrategy = TINTED_PRIMARY,
        iconContainerSize = CIRCLE_SMALL,
        timerTypography = DISPLAY_SMALL,
        pressedShape = MILD,
        wavyProgress = ON,
    )
    EXPRESSIVE -> StyleConfig(
        cornerPreset = SOFT,         // 3x
        borderPreset = NONE,         // 0x
        alphaPreset = VIVID,         // 1.5x
        expressiveness = EXPRESSIVE,
        cardColorStrategy = MULTI_CONTAINER,
        iconContainerSize = CIRCLE_LARGE,
        timerTypography = DISPLAY_LARGE_SERIF,
        pressedShape = FULL_MORPH,
        wavyProgress = PROMINENT,
    )
}
```

### 2.4 ShapeTokens 新增

```kotlin
const val CORNER_SUPER_LARGE = 36  // 用于 EXPRESSIVE 档的大圆角
```

`styledCorner()` 的 SOFT(3x) 与 CORNER_LARGE(16dp) 组合 = 48dp，clamp 到 CORNER_SUPER_LARGE(36dp)。

### 2.5 字体新增

- 本地打包 `dm_serif_text.ttf`（约 30KB）到 `core/designsystem/res/font/`
- `Fonts` 枚举新增 `DM_SERIF_TEXT`
- `provideTypography()` 中 `timerTypography == DISPLAY_LARGE_SERIF` 时对计时文字组件使用 DM Serif Text
- 注意：DM Serif Text 仅用作计时大字号点缀，不替换全局 fontFamily

### 2.6 持久化

SettingsPrefs / DialogGridConfig 中新增对应字段存储。expressiveness 切换时批量更新 StyleConfig 所有字段；用户手动覆盖子项后，expressiveness 显示"自定义"标签。

---

## 3. 12个借鉴点落地设计

### 3.1 大圆角 24~36dp（来源：Reef）

**零改动路径**：现有 `styledCorner()` + `cornerPreset` 已完全支持。
- SUBDUED: COMPACT(0.25x) → 16dp×0.25 = 4dp
- STANDARD: STANDARD(1x) → 16dp
- EXPRESSIVE: SOFT(3x) → min(16dp×3, 36dp) = 36dp

新增 `ShapeTokens.CORNER_SUPER_LARGE = 36` 作为 clamp 上限。

### 3.2 多色容器卡片 cardColorStrategy（来源：Reef）

| 策略 | 实现 |
|------|------|
| SURFACE | `MaterialTheme.colorScheme.surfaceContainer`（现状） |
| TINTED_PRIMARY | `primaryContainer.alpha(styledAlpha(0.3))` 浅染 |
| MULTI_CONTAINER | 首页功能磁贴按类型分配 primaryContainer / secondaryContainer / tertiaryContainer；行为卡片保留 activityColor 作为容器底色，避免颜色冲突 |

UI 层通过 `LocalStyleConfig.current.cardColorStrategy` 读取策略，在各卡片组件中条件分支。

### 3.3 圆形图标容器 iconContainerSize（来源：Reef）

```kotlin
fun Modifier.iconContainer(
    size: IconContainerSize,
    color: Color,
) = this.then(
    when (size) {
        NONE -> Modifier
        CIRCLE_SMALL -> Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
        CIRCLE_LARGE -> Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color)
    }
)
```

用于：卡片左上角图标、设置项 leadingIcon、底部导航图标背景。
容器颜色 = `primaryContainer.alpha(styledAlpha())` 或 `activityColor.alpha(styledAlpha())`。

### 3.4 计时大字号 + 衬线 timerTypography（来源：Reef）

| 档位 | 字号 | 字体 | 用途 |
|------|------|------|------|
| HEADLINE | 32sp | sans（Figtree/Default） | MomentFocusCard 计时、GridCell 时长 |
| DISPLAY_SMALL | 36sp | sans | 同上 |
| DISPLAY_LARGE_SERIF | 57sp | DM Serif Text | MomentFocusCard 计时读秒 |

DM Serif Text .ttf 本地打包到 `core/designsystem/res/font/dm_serif_text.ttf`。
新增 `LocalTimerTypography` CompositionLocal 提供计时专用 Typography token。

### 3.5 滑动解锁胶囊增强（来源：Reef FocusTogglePill）

基于现有 `SlideActionPill` 增强，不新建组件：

| 档位 | 行为 |
|------|------|
| SUBDUED | 无动画，点击直接触发 |
| STANDARD | 线性拖动，无弹簧 |
| EXPRESSIVE | 弹簧动画(DampingRatioMediumBouncy/StiffnessLow) + 进度色填充(0→70%) + thumb 图标切换(▶→✓) + 文案切换("滑动"→"松开") + thumb 阴影 |

新增参数传入 `SlideActionPill`，内部根据 `pressedShape` level 条件分支。

### 3.6 按下变形 pressedShape（来源：Reef）

通过 M3 Expressive Button API 的 shapes 参数实现，不使用 Modifier 扩展：

```kotlin
// 按钮：ButtonDefaults.shapes(pressedShape = ...)
fun buttonShapes(level: PressedShapeLevel): ButtonShapes = when (level) {
    OFF -> ButtonDefaults.shapes()  // 默认，无变形
    MILD -> ButtonDefaults.shapes(pressedShape = RoundedCornerShape(24))
    FULL_MORPH -> ButtonDefaults.shapes(pressedShape = ButtonDefaults.pressedShape)
}

// 图标按钮：IconButtonDefaults.shapes()
fun iconButtonShapes(level: PressedShapeLevel): IconButtonShapes = when (level) {
    OFF -> IconButtonDefaults.shapes()
    MILD -> IconButtonDefaults.shapes(pressedShape = RoundedCornerShape(16))
    FULL_MORPH -> IconButtonDefaults.shapes(
        pressedShape = IconButtonDefaults.largePressedShape
    )
}
```

应用范围：`Button`(shapes参数)、`FilledTonalIconButton`(64dp+shapes)、`ToggleButton`(connectedButtonShapes)。

### 3.7 快捷时长 AssistChip（来源：Reef）

新组件 `DurationAssistChipRow`：

```kotlin
@Composable
fun DurationAssistChipRow(
    durations: List<Duration>,       // 从 DialogGridConfig 读取
    selectedDuration: Duration?,
    onDurationSelect: (Duration) -> Unit,
    modifier: Modifier = Modifier,
)
```

内部用 `AssistChip` 渲染，选中态用 `FilterChip` 或 border 高亮。
时长列表（默认 5/15/25/45/60 分钟）存入 `DialogGridConfig`。

使用场景：AddBehaviorSheet 时长区域、计时配置页面。

### 3.8 波浪进度条（来源：Momentum）

直接使用 M3 Expressive 的 `LinearWavyProgressIndicator`：

| 档位 | 组件 | 参数 |
|------|------|------|
| OFF | `LinearProgressIndicator` | 标准 4dp 高 |
| ON | `LinearWavyProgressIndicator` | 小 amplitude，8dp 高 |
| PROMINENT | `LinearWavyProgressIndicator` | 大 amplitude，12dp 高 |

用途：行为计时进度、番茄循环进度。

### 3.9 GroupCard 自动圆角（来源：Reef SettingsCard）

新增 `GroupCard` 组件替代 `leadingItemShape/middleItemShape/endItemShape`：

```kotlin
@Composable
fun GroupCard(
    items: @Composable () -> Unit,   // 内部用 Column 排列
    modifier: Modifier = Modifier,
)
```

内部逻辑：
- `listSize == 1` → 全圆角 `styledCorner(CORNER_EXTRA_LARGE)`
- `index == 0` → 上圆下直
- `index == last` → 上直下圆
- else → `styledCorner(CORNER_EXTRA_SMALL)` (6dp)

保留 `detachedItemShape()` 用于单独项。
圆角值跟随 `styledCorner()` 三档联动。

### 3.10 AppBottomSheet 封装（来源：Momentum MomentumBottomSheet）

```kotlin
@Composable
fun AppBottomSheet(
    onDismiss: () -> Unit,
    title: String?,
    content: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
)
```

封装特性：
- `sheetMaxWidth = 500dp`
- content padding 联动 StyleConfig（默认 20dp，EXPRESSIVE 档 32dp）
- 子项 `spacedBy(8.dp)`
- `animateContentSize()` 内置
- dragHandle + title 标准化
- 圆角/border 跟随 StyleConfig

### 3.11 纯色全屏引导页 AppIntro（来源：Reef）

```kotlin
@Composable
fun AppIntroScreen(onFinish: () -> Unit)
```

实现：
- `HorizontalPager` 6 页
- 每页全屏纯色背景（从 seedColor 派生 6 色：深蓝/蓝/青/紫/橙/绿）
- 页面内容：大图标 + 标题(DM Serif Text) + 副文 + 页码指示器
- 底部"跳过"和"下一步/开始"按钮
- 首次启动检测：DataStore 存储 `hasSeenIntro: Boolean`
- 已看则跳过，直接进主页

### 3.12 选中态形状 VerySunny（来源：Momentum）

```kotlin
fun selectedShape(expressiveness: ExpressivenessPreset): Shape = when (expressiveness) {
    SUBDUED, STANDARD -> CircleShape
    EXPRESSIVE -> MaterialShapes.VerySunny.toShape()
}
```

依赖 `androidx.compose.material3.shapes.MaterialShapes`（M3 Expressive API）。
仅 EXPRESSIVE 档启用。
用途：选项卡选中态、调色板 Chip 选中、分类筛选 Chip。

---

## 4. 主题设置页 UI

在现有 ThemeSettingsScreen 中新增"表达力"区域：

```
[ 表达力预设 ]
  ┌─────────┬─────────┬───────────┐
  │  克制   │  标准   │  表达 🔥  │
  └─────────┴─────────┴───────────┘

[ 卡片色策略 ]  (子项，可独立切换)
  ○ 统一灰底  ○ 主色浅染  ○ 多色拼盘

[ 圆形图标容器 ]  (子项)
  ○ 无  ○ 小36dp  ○ 大44dp

[ 计时字号 ]  (子项)
  ○ 32sp  ○ 36sp  ○ 57sp衬线

... (其余子项类似)
```

选中预设 → 自动填充所有子项 → 用户手动覆盖子项 → 预设标签显示"自定义"。

---

## 5. 影响范围

| 模块 | 变更类型 |
|------|----------|
| `core/designsystem` | 新增枚举、StyleConfig 扩展、GroupCard/AppBottomSheet/DurationAssistChipRow 组件、iconContainer/pressedShapeMorph 扩展、DM Serif 字体、ShapeTokens、AppIntroScreen |
| `core/data` | SettingsPrefs 新增字段、DialogGridConfig 新增 durationPresets |
| `feature/home` | 卡片色策略应用、计时字号/衬线、波浪进度条、图标容器、SlideActionPill 增强 |
| `feature/settings` | 表达力预设 UI、子项微调 UI、预设联动逻辑 |
| `core/behaviorui` | DurationAssistChipRow 接入、AppBottomSheet 替代 |

---

## 6. 不做的事

- 不替换全局 fontFamily 为 DM Serif Text（仅计时场景点缀）
- 不引入 Google Fonts Provider 在线下载（全部本地打包）
- 不改变现有 MVVM 架构和模块依赖方向
- 不在行为卡片上强行使用多色容器（保留 activityColor）
- 不新增单独的"字体选择器"UI（仅枚举切换，不做 Momentum 级别的 8 字体可选）
