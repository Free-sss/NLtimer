# Momentum vs Reef 样式风格提取报告

> 范围：`docs/reference/Momentum/`、`docs/reference/Reef/`
> 方式：扫描 XML 资源（themes/colors/values）+ Kotlin Compose 主题与组件源码
> 备注：两个项目均为纯 Jetpack Compose 应用，传统 XML 主题极简，主要样式定义集中在 Kotlin 侧（`MomentumTheme.kt` / `ReefTheme.kt`）

---

## 目录

1. [总览](#1-总览)
2. [主题与暗色模式](#2-主题与暗色模式)
3. [色彩体系](#3-色彩体系)
4. [字体排版](#4-字体排版)
5. [间距与圆角](#5-间距与圆角)
6. [典型组件样式对比](#6-典型组件样式对比)
7. [视觉装饰与动效](#7-视觉装饰与动效)
8. [显著差异点专标](#8-显著差异点专标)
9. [整体风格关键词总结](#9-整体风格关键词总结)
10. [对 NLtimer 设计的可借鉴点](#10-对-nltimer-设计的可借鉴点)

---

## 1. 总览

| 项目 | 包名 | 定位 | 技术栈 |
|---|---|---|---|
| **Momentum** | `shub39.momentum` | 「日常拍照 → 自动剪成蒙太奇视频」记录类 App | Compose + **Material3 Expressive** + `com.materialkolor` 动态调色 + Room + DataStore |
| **Reef** | `dev.pranav.reef` | 「专注 / 番茄钟 / 应用使用统计 / 网站屏蔽」专注类 App | Compose + **Material3 Expressive**（实验 API）+ Material You 动态色 |

> 共同点：两者都全面拥抱 **Material 3 Expressive**（M3 表达式风格），都使用 `MediumTopAppBar`、`Card` 圆角容器、`ListItem` 设置项、`Switch` 开关；都重度依赖 `MaterialTheme.colorScheme`，几乎不写死色值。

---

## 2. 主题与暗色模式

### Momentum — 高自由度的"调色板"主题

`Momentum/app/src/main/java/shub39/momentum/presentation/shared/MomentumTheme.kt`

```kotlin
DynamicMaterialTheme(
    seedColor = if (theme.isMaterialYou && SDK_INT >= S) system_accent1_200 else theme.seedColor,
    isDark    = AppTheme.{SYSTEM, LIGHT, DARK},
    isAmoled  = theme.isAmoled,
    style     = theme.paletteStyle.toMPaletteStyle(),  // 多种 PaletteStyle
    typography = provideTypography(font = theme.font.toFontRes()),
)
```

- 用户可自定义：**Light / Dark / 跟随系统**、**Material You 开关**、**AMOLED 纯黑**、**自选 Seed 颜色**、**Palette Style**（EXPRESSIVE 等）、**字体**。
- XML `themes.xml` 只声明：`<style name="Theme.Momentum" parent="android:Theme.Material.Light.NoActionBar" />`，所有视觉都交给 Compose 控制。
- 默认 `seedColor = Color.Blue`、`paletteStyle = EXPRESSIVE`、`font = FIGTREE`。

### Reef — 极简的动态色 + Expressive 主题

`Reef/Reef/src/main/java/dev/pranav/reef/ui/Theme.kt`

```kotlin
val colorScheme = when {
    SDK_INT >= S -> if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
    darkTheme    -> darkColorScheme()
    else         -> expressiveLightColorScheme()
}
val shapes = Shapes(largeIncreased = RoundedCornerShape(36.0.dp))
MaterialExpressiveTheme(colorScheme = colorScheme, shapes = shapes, content = content)
```

- 只跟随系统暗色（`isSystemInDarkTheme()`），无主题选择 UI。
- XML `themes.xml`：`parent="Theme.Material3Expressive.DynamicColors.DayNight.NoActionBar"`，`values-v31` 还把 Splash 背景挂到 `?colorSurface`。
- 唯一自定义 Shape：`largeIncreased = 36dp`（用于大圆角胶囊）。

---

## 3. 色彩体系

### 3.1 Momentum

| 项目 | 取值 / 来源 |
|---|---|
| 默认 Seed | `Color.Blue`（`Theme.kt`） |
| 主色板来源 | `materialkolor` 根据 Seed 动态生成完整 M3 色板 |
| Material You | 启用时 Seed 替换为 `android.R.color.system_accent1_200` |
| AMOLED | 开启后整个色板基于纯黑底 |
| 卡片底色 | `MaterialTheme.colorScheme.surfaceContainer` / `surfaceContainerLow` |
| 文字色 | `onSurface` / `onSurfaceVariant` |
| Splash | 单独 `splash.xml`，区分 `values/` 与 `values-night/` |
| **写死的具体色值** | 几乎没有；仅在 Preview 中出现 `0xFFFFFF` |

### 3.2 Reef

`Reef/Reef/src/main/res/values/colors.xml`（仅启动图标用，正文 UI 全部走动态色）

| 名称 | 色值 | 用途 |
|---|---|---|
| `ic_launcher_background` | `#021123` | 启动器图标深蓝底 |
| `ic_launcher_1` | `#BA2E5C` | 启动器图标洋红 |
| `ic_launcher_2` | `#325ED5` | 启动器图标蓝 |

`values-v31/colors.xml`：以上三色在 Android 12+ 改用 `@android:color/system_accent1_50/system_accent3_400/system_accent1_500`，跟随系统主题。

**AppIntro 引导页固定色（Reef 唯一手写色彩集合）**：
`Reef/.../intro/AppIntroScreen.kt`

| 步骤 | 背景色 | 含义 |
|---|---|---|
| 欢迎 | `#093A8F` | 深蓝 |
| 无障碍服务 | `#FF3D00` | 警示橙红 |
| 用量统计 | `#536DFE` | 紫蓝 |
| 通知权限 | `#F19C32` | 暖橙 |
| 电池优化 | `#00BFA5` | 薄荷青 |
| 勿扰权限 | `#8968D5` | 紫罗兰 |

正文 UI 中颜色全部通过 `MaterialTheme.colorScheme.{primary, primaryContainer, secondaryContainer, tertiaryContainer, errorContainer, surfaceContainer, surfaceContainerHigh, …}` 引用。

### 3.3 色彩体系对比

| 维度 | Momentum | Reef |
|---|---|---|
| 主色配置 | **用户自选 Seed + 多 PaletteStyle** | 仅"跟随壁纸"或 Expressive 兜底 |
| AMOLED | ✅ 单独开关 | ❌ 无 |
| 写死色值 | 几乎无 | 仅 Launcher 图标 + AppIntro 6 色 |
| 容器层级 | `surfaceContainer` / `surfaceContainerLow` 双层 | `surface` / `surfaceContainer` / `surfaceContainerHigh` 三层 + `primary/secondary/tertiaryContainer` 大量做色卡 |

> ⚠️ **差异显著**：Reef 在主页直接用 `primaryContainer / secondaryContainer / tertiaryContainer / errorContainer` 作为不同卡片的底色，**视觉上是"彩色磁贴拼盘"**；Momentum 几乎只用中性灰 `surfaceContainer*`，**视觉上是"统一灰底 + 内容图片自带色"**。

---

## 4. 字体排版

### 4.1 Momentum — 8 字体可选 + 可变字体特技

`Momentum/common/.../enums/Fonts.kt` + `provideTypography.kt`

| 可选字体 | 资源 |
|---|---|
| INTER | `R.font.inter` |
| POPPINS | `R.font.poppins` |
| MANROPE | `R.font.manrope` |
| MONTSERRAT | `R.font.montserrat` |
| **FIGTREE**（默认） | `R.font.figtree` |
| QUICKSAND | `R.font.quicksand` |
| GOOGLE_SANS | `R.font.google_sans_flex`（**可变字体 .ttf**） |
| SYSTEM_DEFAULT | `FontFamily.Default` |

**`google_sans_flex` 可变字体特殊用法**：

```kotlin
flexFontEmphasis(slant = 0f) → weight=1000, slant, width=120
flexFontRounded()            → weight=800, "ROND"=100f   // ROND 轴 = 圆润度
```

`flexFontRounded()` 用在 Onboarding 标题，营造"圆乎乎、可爱、欢迎"的感觉；`flexFontEmphasis()` 用在设置页 TopAppBar 标题，做超粗超宽强调。

字号体系：完全沿用 Material3 默认 `Typography()`，只替换 fontFamily：

| 角色 | 默认字号（M3 规范） |
|---|---|
| displayLarge | 57sp |
| displayMedium | 45sp |
| displaySmall | 36sp |
| headlineLarge | 32sp |
| headlineMedium | 28sp |
| headlineSmall | 24sp |
| titleLarge | 22sp |
| titleMedium | 16sp / 500w |
| titleSmall | 14sp / 500w |
| bodyLarge | 16sp |
| bodyMedium | 14sp |
| bodySmall | 12sp |
| labelLarge | 14sp / 500w |
| labelMedium | 12sp / 500w |
| labelSmall | 11sp / 500w |

### 4.2 Reef — 「无衬线 + 一种衬线点缀」

`Reef/Reef/src/main/java/dev/pranav/reef/ui/Typography.kt`

```kotlin
object Typography {
    val DMSerif = FontFamily(Font(googleFont = GoogleFont("DM Serif Text"), fontProvider = provider))
}
```

- 整体字体：M3 默认（系统无衬线）。
- **唯一手动声明字体**：`DM Serif Text`（**衬线**），通过 Google Fonts Provider 异步下载。
- 用途：仅用在 **大标题、卡片标题、计时器读秒**（`HomeContent.kt` 的 `FocusModeCard`、`AppUsageCard`、`TimeLimitsCard` 标题，`AboutScreen.kt` 的 App 名，`TimerScreen.kt` 88sp 的剩余时间）。

特殊字号（直接 `.sp` 写死）：

| 场景 | 字号 | 字重 | 字体 |
|---|---|---|---|
| 简单计时配置 时分大数字 | **92.sp** | Bold | M3 displayLarge |
| 番茄钟运行中 剩余时间 | **88.sp** | Bold | **DMSerif** |
| FocusMode 副文字行高 | 22.sp lineHeight | — | bodyLarge |

### 4.3 字体对比

| 维度 | Momentum | Reef |
|---|---|---|
| 字体可选数 | 8 | 1（且仅作点缀） |
| 字体获取 | 本地 .ttf / .otf 打包 | Google Fonts Provider 在线下载 |
| 衬线 vs 无衬线 | 全部无衬线 | 无衬线为底，**衬线 DM Serif 点睛** |
| 可变字体 | ✅ Google Sans Flex（weight/slant/ROND/width 4 轴） | ❌ |
| 自定义大字号 | 无（沿用 M3 规范） | 有（92sp / 88sp 极大字号） |

---

## 5. 间距与圆角

### 5.1 Momentum

通用间距：`8dp / 16dp / 32dp` 三档（Compose `dp` 直写，未集中到 dimens.xml）。

| 元素 | 数值 |
|---|---|
| `MomentumBottomSheet` 内边距 | **32dp** |
| `MomentumBottomSheet` 子项垂直间距 | 8dp |
| `MomentumBottomSheet` 最大宽度 | 500dp |
| `Card` 默认 | M3 默认（约 12dp 圆角） |
| `AlarmCard` 圆角 | **16dp** |
| `ProjectListItem` 内图片裁剪 | 16dp |
| Carousel 高度 | 200dp，preferredItemWidth 200dp，itemSpacing 8dp |
| Onboarding 横向 padding | 32dp，pageSpacing 32dp |
| 设置项「分组圆角」 | 自定义 `leadingItemShape() / middleItemShape() / endItemShape()` 形成 **"上圆下方→中部连续→下圆上方"** 的连块视觉 |
| `MaterialShapes.VerySunny` | 用作"已选中"调色板 chip 的特殊形状 |

### 5.2 Reef

| 元素 | 数值 |
|---|---|
| 全局 `largeIncreased` Shape | **36dp** |
| `FocusModeCard` 圆角 | **32dp**，高度固定 260dp |
| `FocusTogglePill` 圆角 | 36dp，宽 200dp、高 72dp，thumb 60dp 圆 |
| `AppUsageCard / TimeLimitsCard` | 24dp 圆角，高度 180dp |
| `RoutinesCard / WebsiteBlocklistCard / PomodoroTimerCard` | 24dp 圆角 |
| `LinkCard` | 16dp 圆角 |
| `AppInfoCard` | 28dp 圆角 |
| `DeveloperCard / SupportCard` | 24dp 圆角 |
| **设置项 SettingsCard 圆角策略** | 单项=24dp 全圆；首项=上 24 下 6；尾项=上 6 下 24；中间=6dp。**形成"叠加分块"视觉** |
| 图标圆形容器 | `CircleShape`，常见 36 / 40 / 44 / 48 dp |
| 卡片之间 Spacer | 12dp / 16dp |
| 屏幕水平 padding | 16dp（主页）/ 24dp（计时器） |

### 5.3 圆角与间距对比

| 维度 | Momentum | Reef |
|---|---|---|
| 主流圆角 | 16dp | **24dp ~ 36dp，明显更大更圆** |
| 圆形图标容器 | 较少 | **大量使用 CircleShape**（每个卡片左上角都是圆色块 + Icon） |
| 分组列表圆角策略 | 自定义 leading/middle/end shape | `SettingsCard` index 智能切换四角 |
| 间距风格 | 偏紧凑（8/16/32） | 偏舒展（12/16/24，大卡片高度固定 180/260） |

---

## 6. 典型组件样式对比

### 6.1 顶栏 TopAppBar

| | Momentum | Reef |
|---|---|---|
| 类型 | `TopAppBar` + `enterAlwaysScrollBehavior` | `MediumTopAppBar` + `exitUntilCollapsedScrollBehavior`（**主页/计时器/关于全用大顶栏**） |
| 标题字体 | `flexFontEmphasis()`（极粗扁体） | `headlineLarge / headlineMedium`，必要时叠 `DMSerif` |
| 颜色 | M3 默认 | `containerColor=surface, scrolledContainerColor=surfaceContainer / surfaceColorAtElevation(3.dp)` |
| 图标 | 自带 `R.drawable.arrow_back` | Material Icons (`Icons.AutoMirrored.Filled.ArrowBack`) |
| 主页前缀 | — | **44dp 圆形 primaryContainer + Waves 图标** 作为 logo |

### 6.2 卡片 Card

| | Momentum | Reef |
|---|---|---|
| 默认底色 | `surfaceContainer`（中性灰） | **彩色容器**：`primaryContainer / secondaryContainer / tertiaryContainer / errorContainer / surfaceContainerHigh` 多色拼盘 |
| 圆角 | 16dp | 24~32dp |
| 内 padding | 16dp | 16~28dp |
| 内嵌结构 | 图片 Carousel + 文字 Row + 箭头图标 | 圆形图标 + 标题（DMSerif）+ 副文 + 圆形箭头按钮 |
| 阴影 | 默认 | `Surface` + `shadowElevation = 4dp + 4dp * progress` 动态阴影 |

### 6.3 按钮 Button

| | Momentum | Reef |
|---|---|---|
| 主按钮 | M3 `Button` | M3 `Button` + `ButtonDefaults.shapes(pressedShape = ButtonDefaults.pressedShape)`（按下变形） |
| 图标按钮 | 标准 `IconButton` | **`FilledTonalIconButton` 64dp 大尺寸 + `IconButtonDefaults.extraLargeSquareShape`/`largePressedShape`**（按下圆变方） |
| 切换组 | `ToggleButton` 加 `ButtonGroupDefaults.ConnectedSpaceBetween`（连块） | `ToggleButton` 加 `connectedLeading/Middle/TrailingButtonShapes()`（连块） |
| 危险按钮 | — | `containerColor = colorScheme.error`（Donate） |
| 滑动确认 | — | **`FocusTogglePill` 自实现的"滑动解锁"胶囊**：drag 阈值 70% 触发，含弹簧动画 + 进度色 + thumb 阴影 |

### 6.4 输入与表单

| | Momentum | Reef |
|---|---|---|
| 时间输入 | `TimeInput` + `TimePickerDialog` | 自定义 `ExpressiveCounter`（Label + 大数字 + ± 圆按钮） |
| 开关 | `Switch` | `Switch` |
| Slider | `LinearWavyProgressIndicator`（**波浪进度条**） | — |
| 颜色选择 | 自定义 `ColorPickerDialog` | — |
| Chip | — | `AssistChip` 大量用于快捷时长（5/15/30/60/90 分钟） |

### 6.5 列表 ListItem

| | Momentum | Reef |
|---|---|---|
| 容器 | `ListItem` + `colors = listItemColors()`（自定义） | `ListItem` + `containerColor = Color.Transparent` 套在 `SettingsCard` 内 |
| 分组 | 多 `ListItem` 共享 leading/middle/end 圆角 | 多 `ListItem` 由父 `SettingsCard(index, listSize)` 决定圆角 |
| 触发器 | 内嵌 `Switch` / `IconButton` / `Button` | 内嵌 `Switch` / `IconButton`（"-/+"文本按钮） |

### 6.6 BottomSheet / Dialog

| | Momentum | Reef |
|---|---|---|
| BottomSheet | `MomentumBottomSheet` 封装：`sheetMaxWidth=500dp`、padding 32dp、子项 spacedBy 8dp、`animateContentSize` | 未见自定义封装，直接用 `AlertDialog` |
| Dialog | 自封装 `MomentumDialog` | 标准 `AlertDialog`（`CommunityDialog / DonateDialog`），icon + 标题 + 文本 + 主辅按钮 |

### 6.7 列表分组形状（重要差异）

```kotlin
// Momentum: 自定义形状函数
Modifier.clip(leadingItemShape())   // 顶部分组首项
Modifier.clip(middleItemShape())    // 中间项
Modifier.clip(endItemShape())       // 底部尾项

// Reef: SettingsCard 通过 index/listSize 智能切换
listSize == 1 → RoundedCornerShape(24.dp)
index == 0    → 上 24 / 下 6
index == last → 上 6 / 下 24
else          → 6dp
```

> 两者都营造了"分组卡片之间留 1~2dp 缝、首尾大圆角、中间小圆角"的现代设置项视觉，但 **Reef 用一个组件就解决，更简洁**。

---

## 7. 视觉装饰与动效

### 7.1 Momentum 的特色装饰

- **`zigZagBackground`**（`shared/zigZagBackground.kt`）：`Modifier` 扩展，绘制 Z 字折线背景，`steps=20`、`zigZagHeight=20f`、`Color.Gray.alpha=0.3`、`stroke=4f`。用于空状态或装饰性分隔。
- **`MaterialShapes.VerySunny`**：选中态调色板 chip 的"光芒"形状。
- **`LinearWavyProgressIndicator`**：M3 Expressive 的波浪进度条。
- **`HorizontalMultiBrowseCarousel`**：项目列表里展示最近 10 天照片的多浏览轮播（高度 200dp，preferredItemWidth 200dp）。
- **Onboarding 配 `CameraAnimation / AutoAnimation / PrivateAnimation`**（推测为 Lottie/Compose 动画）。

### 7.2 Reef 的特色装饰

- **`FocusTogglePill` 滑动解锁**：手势 `detectHorizontalDragGestures` + `animateFloatAsState`（`Spring.DampingRatioMediumBouncy / StiffnessLow`）+ 进度驱动 `alpha + 阴影`。文案在 progress > 0.5 时从 "Slide" 切到 "Release"，thumb 图标从 `Waves` 切到 `Check`。
- **`AnimatedContent + AnimatedVisibility` 横向滑屏**：设置主→子页用 `slideInHorizontally + fadeIn` 100ms 切换。
- **`SettingsCard` 出现动画**：`fadeIn() + scaleIn(0.95f, DampingRatioMediumBouncy)`。
- **`ButtonDefaults.shapes(pressedShape)`**：所有主按钮按下时形状变化，触感反馈强。
- **`FilledTonalIconButton` 按下方块化**：`extraLargeSquareShape → largePressedShape`（圆 → 方），是 M3 Expressive 的标志性动效。
- **TopAppBar `scrollBehavior`**：滚动时 `surface → surfaceContainer` 渐变。

### 7.3 动效对比

| 维度 | Momentum | Reef |
|---|---|---|
| 切屏动效 | Pager 横向 | `AnimatedContent` 横向 + 淡入 |
| 进度条 | 波浪 `LinearWavyProgressIndicator` | 标准（无） |
| 按下变形 | 默认 | **大量 pressedShape**（M3 表达式核心） |
| 手势组件 | Carousel | **滑动解锁胶囊** |
| 大数字动画 | — | `animateContentSize` + `AnimatedContent` 计时器读秒切换 |

---

## 8. 显著差异点专标

⚠️ **以下为视觉/体验层面差异最大的点，值得在做选型时重点权衡：**

| # | 维度 | Momentum | Reef | 体感差异 |
|---|---|---|---|---|
| ① | 主题可定制度 | 极高（Seed/Palette/AMOLED/Font/Material You 五维） | 极低（仅跟随系统） | Momentum 适合"重度个性化"用户；Reef 适合"开箱即用"用户 |
| ② | 卡片色策略 | **统一中性 surfaceContainer 灰** | **彩色 primaryContainer / secondaryContainer / tertiaryContainer 拼盘** | Reef 视觉更"花"更有信息层次；Momentum 更"性冷淡" |
| ③ | 字体策略 | 8 字体可选，全无衬线 | 1 字体 DM Serif **衬线点缀**大标题 | Reef 标题有"杂志感"；Momentum 全程统一 |
| ④ | 圆角尺度 | 16dp 为主 | **24~36dp，更大更圆** | Reef 整体更"软" |
| ⑤ | 圆形图标容器 | 较少 | **几乎每个卡片都有圆形色块 + 图标作 logo** | Reef 视觉锚点强、辨识度高 |
| ⑥ | 计时大字号 | — | **88sp / 92sp Bold** | Reef 计时屏「沉浸式专注」氛围 |
| ⑦ | 手势动效 | Pager + Carousel | **滑动解锁胶囊**（启动专注模式） | Reef 有"仪式感"，强化用户决心 |
| ⑧ | 设置项圆角 | 三个 shape 函数手写 | 一个 `SettingsCard(index, size)` 自动算 | Reef 实现更优雅 |
| ⑨ | 按下变形 | 默认 | **`pressedShape` 圆 → 方 / 椭圆 → 矩形** | Reef 触感反馈更明显 |
| ⑩ | AppIntro 引导页 | 三屏，纯文字 + Lottie 动画 | 六屏，**每屏全屏纯色背景**（深蓝/橙红/紫蓝/橙/青/紫） | Reef 引导页更"App Store 大厂感" |
| ⑪ | 衬线字体 | ❌ | ✅ DM Serif 点缀 | Reef 有内容质感 |
| ⑫ | AMOLED | ✅ 单独开关 | ❌ | 仅 Momentum 适配 OLED 省电党 |
| ⑬ | 装饰性背景 | `zigZagBackground` Z 字线 | 无 | Momentum 有"手绘感" |

---

## 9. 整体风格关键词总结

### Momentum

> **「极简灰底 · 高度可调 · 精致克制 · 可变字体玩家」**

- 关键词：**Material You Plus、灰底卡片、字体收藏家、AMOLED 友好、波浪进度、Z 字背景、圆角中庸（16dp）**
- 一句话：把"个性化"做成了第一公民——**用户调出怎样，App 就长成怎样**。骨子里仍是 M3 默认的克制美学。

### Reef

> **「彩色磁贴 · 大圆角软糖 · 衬线点睛 · 仪式感专注」**

- 关键词：**多容器拼盘、24~36dp 大圆角、CircleShape 图标位、DM Serif 衬线、滑动解锁胶囊、Pomodoro 88sp 巨型读秒、Expressive pressedShape 触感**
- 一句话：**Material 3 Expressive 教科书式实现**——颜色、形状、动效、字体四者协同放大表达力，氛围鲜明。

---

## 10. 对 NLtimer 设计的可借鉴点

> 基于本次扫描，给出 NLtimer（计时类应用）可参考的具体方案：

| 来源 | 可借鉴点 | 落点 |
|---|---|---|
| **Reef** | `FocusTogglePill` 滑动解锁胶囊 | 计时启动按钮——比单击 Tap 更有"开始专注"的仪式感 |
| **Reef** | 计时大字号 88sp + DMSerif 衬线 | 主计时屏读秒——视觉震撼且具阅读质感 |
| **Reef** | `SettingsCard(index, listSize)` 自动圆角 | 设置页分组——一个组件解决全部圆角 |
| **Reef** | `primaryContainer / secondaryContainer / tertiaryContainer` 多色卡片 | 主页"功能磁贴"分区——比纯灰更易区分 |
| **Reef** | `pressedShape` 按下变形 | 全局按钮触感强化 |
| **Reef** | `AssistChip` 快捷时长（5/15/30/60/90 分钟） | 计时配置预设 |
| **Reef** | 6 屏纯色 AppIntro | 首次启动引导 |
| **Momentum** | `flexFontRounded()` 圆润可变字体 | Onboarding/欢迎页标题 |
| **Momentum** | 用户可选 Seed + PaletteStyle + AMOLED | 高级设置项（差异化竞品） |
| **Momentum** | `LinearWavyProgressIndicator` 波浪进度条 | 计时倒计/番茄循环进度 |
| **Momentum** | `MomentumBottomSheet`（max 500dp + 32dp padding + animateContentSize） | 通用底部弹窗封装 |
| **Momentum** | `MaterialShapes.VerySunny` 选中态形状 | 选项卡选中态视觉锚点 |

---

> 报告完毕。已分析的关键文件：
> - **Momentum**：`MomentumTheme.kt`、`provideTypography.kt`、`themes.xml`、`Theme.kt`(data class)、`AppTheme/Fonts` enums、`enumExt.kt`、`MomentumBottomSheet.kt`、`zigZagBackground.kt`、`ProjectListItem.kt`、`AlarmCard.kt`、`LookAndFeel.kt`、`Onboarding.kt`
> - **Reef**：`Theme.kt`、`Typography.kt`、`themes.xml`、`values-v31/themes.xml`、`colors.xml`、`values-v31/colors.xml`、`MainScreen.kt`、`TimerScreen.kt`、`SettingsComponents.kt`、`MainSettingsScreen.kt`、`SettingsScreen.kt`、`AboutScreen.kt`、`AppIntroScreen.kt`
