---
name: m3-expressive-design-patterns
description: Material 3 Expressive 设计模式集——从 Momentum 与 Reef 两个生产级安卓 Compose 项目中提取的色彩、形状、组件、交互、动效设计优点。在为 Android Jetpack Compose 项目设计或实现界面、卡片、按钮、设置项、计时屏、引导页、底部弹窗、分组列表、滑动确认、按下反馈等 UI 元素时使用此 skill；适用于追求"现代、有表达力、Material You 友好"风格的项目。
version: 1.0.0
source: 提取自 docs/reference/Momentum 与 docs/reference/Reef 静态分析
analyzed_projects: 2
analyzed_files: 24
scope: 不包含字体/排版（typography 由项目自行决定）
---

# M3 Expressive Design Patterns（来自 Momentum × Reef）

> 把 Momentum「极简灰底 · 高度可调」与 Reef「彩色磁贴 · 大圆角软糖 · 仪式感」两套生产级 Compose 设计的优点提炼为可复用模式。
> **本 skill 不涉及字体/排版**——这部分由调用方自行决定。

## 何时使用此 skill

当你正在做以下任一工作时，**必须**遵循这里的模式：

- 为 Android Compose App 搭建 `MaterialTheme` / `MaterialExpressiveTheme`
- 设计主页磁贴/卡片网格、设置页分组列表
- 实现"启动专注 / 启动计时 / 启动任务"等需要仪式感的入口按钮
- 实现底部弹窗 `ModalBottomSheet`、引导页 AppIntro、Dialog
- 选择圆角尺寸、阴影策略、容器层级
- 决定按钮按下反馈、切屏动画、分组动画

## 核心原则（按重要性降序）

### ① 色彩永远走 `MaterialTheme.colorScheme`，绝不写死色值

**Why**：Momentum 与 Reef 主体 UI 几乎零硬编码色值。两者都允许"动态色（Material You）/ 系统暗色 / AMOLED"在不改一行业务代码的前提下生效。
**How to apply**：

- ✅ 使用：`MaterialTheme.colorScheme.{surface, surfaceContainer, surfaceContainerHigh, primary, primaryContainer, secondary, secondaryContainer, tertiary, tertiaryContainer, error, errorContainer, onSurface, onSurfaceVariant, ...}`
- ❌ 禁止：`Color(0xFF...)` 直接写在业务 Composable 里
- 仅 3 处例外允许硬编码：①启动器图标 colors.xml；②AppIntro 全屏背景色；③临时 Preview 背景

**仅有的硬编码场景示例**（来自 Reef）：

```xml
<!-- res/values/colors.xml：只放 launcher icon 用色 -->
<color name="ic_launcher_background">#021123</color>
<color name="ic_launcher_1">#BA2E5C</color>
<color name="ic_launcher_2">#325ED5</color>
```

```xml
<!-- res/values-v31/colors.xml：Android 12+ 改走系统色 -->
<color name="ic_launcher_background">@android:color/system_accent1_50</color>
<color name="ic_launcher_1">@android:color/system_accent3_400</color>
<color name="ic_launcher_2">@android:color/system_accent1_500</color>
```

### ② 卡片色策略 = "彩色磁贴拼盘"（来自 Reef）

**Why**：在主页/功能入口区，**用不同的 `*Container` 色区分卡片功能** > 一律灰底。Reef 主页 4 个卡片分别用 `primaryContainer`（主功能 FocusMode）、`secondaryContainer`（数据统计 AppUsage）、`tertiaryContainer`（白名单 TimeLimits）、`surfaceContainerHigh`（次要入口 Routines/Pomodoro），信息层次一目了然。
**How to apply**：

| 卡片角色 | 容器色 |
|---|---|
| 主操作 / 行动召唤 | `primaryContainer` |
| 数据 / 统计 | `secondaryContainer` |
| 配置 / 白名单 | `tertiaryContainer` |
| 危险 / 捐赠 / 警示 | `errorContainer.copy(alpha = 0.5f)` |
| 次要列表项 | `surfaceContainer` / `surfaceContainerHigh` |

文字色对应 `on{X}Container`，副文字加 `.copy(alpha = 0.7f)` 弱化。

### ③ 圆角偏大、形状有层级（来自 Reef）

**Why**：Reef 圆角全在 24~36dp 区间，"软糖"质感强；Momentum 用 16dp 偏中庸。若追求现代表达力，**优先用大圆角**。
**How to apply**：

```kotlin
// 全局 shape 定制（在 MaterialExpressiveTheme 里覆盖）
val shapes = Shapes(largeIncreased = RoundedCornerShape(36.0.dp))

// 卡片圆角分级
RoundedCornerShape(32.dp)  // 旗舰主卡（FocusMode 类）
RoundedCornerShape(28.dp)  // 二级强调卡（AppInfoCard）
RoundedCornerShape(24.dp)  // 标准卡片
RoundedCornerShape(16.dp)  // 链接/小卡
RoundedCornerShape(6.dp)   // 设置项分组中间项（紧贴）
CircleShape                // 图标容器（必备）
```

### ④ 圆形图标容器 = 卡片的视觉锚点（来自 Reef）

**Why**：Reef 的每张卡片左/右都有一个 `CircleShape` 的色块装着图标，**让卡片结构标准化、扫读速度极快**。常用尺寸：36/40/44/48dp。
**How to apply** — 通用模板：

```kotlin
Surface(
    modifier = Modifier.size(48.dp),
    shape = CircleShape,
    color = MaterialTheme.colorScheme.primaryContainer
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Icon(
            imageVector = Icons.Rounded.XYZ,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
```

每个卡片右侧再放一个 36dp 的 `CircleShape` 装 `Icons.AutoMirrored.Rounded.ArrowForward`，形成"左 logo · 中文字 · 右箭头"的统一节奏。

### ⑤ 按下变形（pressedShape）—— Expressive 核心触感

**Why**：M3 Expressive 的标志特性。Reef 全局对所有主按钮、IconButton 启用了"按下时形状切换"，触感反馈明显，远胜默认涟漪。
**How to apply**：

```kotlin
// Button：默认形状 → 按下变椭圆
Button(
    onClick = { ... },
    shapes = ButtonDefaults.shapes(pressedShape = ButtonDefaults.pressedShape),
)

// FilledTonalIconButton：超大方形 → 按下小方形
FilledTonalIconButton(
    onClick = { ... },
    modifier = Modifier.size(64.dp),
    shapes = IconButtonDefaults.shapes(
        shape = IconButtonDefaults.extraLargeSquareShape,
        pressedShape = IconButtonDefaults.largePressedShape,
    ),
) { Icon(Icons.Rounded.Add, null) }

// IconToggleButton：未选 ⇄ 选中两种 shape
IconToggleButton(
    checked = isPaused,
    shapes = IconButtonDefaults.toggleableShapes(
        shape = if (isPaused) IconButtonDefaults.largeSquareShape
                else IconButtonDefaults.extraLargeSquareShape
    ),
)
```

### ⑥ 设置项分组 = 智能圆角（来自 Reef，强烈推荐）

**Why**：Reef 用一个 `SettingsCard(index, listSize)` 组件就解决了所有"分组列表上下连块"圆角问题，比 Momentum 三个 `leading/middle/endItemShape()` 函数手写更简洁。
**How to apply** — 直接复用此模板：

```kotlin
@Composable
fun SettingsCard(index: Int, listSize: Int, content: @Composable () -> Unit) {
    val shape = when {
        listSize == 1 -> RoundedCornerShape(24.dp)
        index == 0 -> RoundedCornerShape(
            topStart = 24.dp, topEnd = 24.dp,
            bottomStart = 6.dp, bottomEnd = 6.dp
        )
        index == listSize - 1 -> RoundedCornerShape(
            topStart = 6.dp, topEnd = 6.dp,
            bottomStart = 24.dp, bottomEnd = 24.dp
        )
        else -> RoundedCornerShape(6.dp)
    }
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + scaleIn(0.95f, spring(dampingRatio = DampingRatioMediumBouncy)),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = shape
        ) { content() }
    }
}
```

内部用 `ListItem` + `Color.Transparent` 容器即可。

### ⑦ 滑动解锁胶囊 = 给重要操作加仪式感（来自 Reef）

**Why**：Reef 的"开始专注"用 `FocusTogglePill` 替代普通点击——drag 阈值 70% 才触发，含弹簧动画 + 进度色变 + thumb 阴影增强。**强化用户决心、防止误触、显著提升仪式感**。
**How to apply** — 适用于：启动计时 / 启动专注 / 确认删除 / 解锁危险操作。

```kotlin
@Composable
fun SlideToConfirm(label: String, onConfirm: () -> Unit) {
    val pillWidth = 200.dp
    val thumbSize = 60.dp
    val padding = 6.dp
    val maxOffset = with(LocalDensity.current) { (pillWidth - thumbSize - padding * 2).toPx() }

    var offsetX by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val animatedOffset by animateFloatAsState(
        targetValue = if (isDragging) offsetX else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    val progress = (animatedOffset / maxOffset).coerceIn(0f, 1f)

    Surface(
        modifier = Modifier
            .width(pillWidth).height(72.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        if (offsetX >= maxOffset * 0.7f) onConfirm()
                        offsetX = 0f; isDragging = false
                    },
                    onDragCancel = { offsetX = 0f; isDragging = false },
                    onHorizontalDrag = { _, d ->
                        offsetX = (offsetX + d).coerceIn(0f, maxOffset)
                    }
                )
            },
        shape = RoundedCornerShape(36.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f + progress * 0.4f)
    ) {
        // ... thumb 用 Surface(CircleShape, shadowElevation = 4.dp + 4.dp * progress)
    }
}
```

### ⑧ 顶栏永远滚动响应（来自双方）

**Why**：两个项目都用 `MediumTopAppBar` + `exitUntilCollapsedScrollBehavior`，滚动时 `surface → surfaceContainer` 渐变，比静态 `TopAppBar` 现代。
**How to apply**：

```kotlin
val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    contentWindowInsets = WindowInsets(0),
    topBar = {
        MediumTopAppBar(
            title = { /* 主页可加 44dp CircleShape logo + 文字 */ },
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        )
    }
) { ... }
```

### ⑨ AssistChip 快捷预设（来自 Reef）

**Why**：Reef 计时屏给出 5/15/30/60/90/180 分钟一键预设，极大降低输入成本。
**How to apply**：用于"快速选择常用值"——时长、金额、数量等。

```kotlin
FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
    AssistChip(onClick = { setMinutes(5) }, label = { Text("5 分钟") })
    AssistChip(onClick = { setMinutes(15) }, label = { Text("15 分钟") })
    AssistChip(onClick = { setMinutes(30) }, label = { Text("30 分钟") })
    AssistChip(onClick = { setMinutes(60) }, label = { Text("1 小时") })
    AssistChip(onClick = { setMinutes(120) }, label = { Text("2 小时") })
}
```

### ⑩ ToggleButton 连块组（来自双方）

**Why**：分段控件比 RadioGroup 现代得多。Momentum 与 Reef 都使用 `ButtonGroupDefaults.connectedLeading/Middle/TrailingButtonShapes()` 形成连块视觉。
**How to apply**：

```kotlin
val modes = listOf("Timer", "Pomodoro")
FlowRow(
    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
) {
    modes.forEachIndexed { index, label ->
        ToggleButton(
            checked = index == selected,
            onCheckedChange = { onSelect(index) },
            shapes = when (index) {
                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                modes.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
            },
            modifier = Modifier.weight(1f).semantics { role = Role.RadioButton },
        ) { Text(label) }
    }
}
```

### ⑪ 底部弹窗封装（来自 Momentum）

**Why**：Momentum 的 `MomentumBottomSheet` 设置了平板友好的最大宽度、统一 padding、自动尺寸动画——可直接复用。
**How to apply**：

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    padding: Dp = 32.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetMaxWidth = 500.dp,           // 平板/折叠屏友好
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier.padding(padding).animateContentSize().fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}
```

### ⑫ 切屏动画 = 横向滑入 + 淡入（来自 Reef）

**Why**：设置主页 → 子页用 `AnimatedContent + slideInHorizontally + fadeIn` 100ms，比 Navigation 默认更轻快。
**How to apply**：

```kotlin
AnimatedContent(
    targetState = currentScreen,
    transitionSpec = {
        if (targetState != Main) {
            slideInHorizontally({ it }, tween(100)) + fadeIn(tween(100)) togetherWith
                slideOutHorizontally({ -it / 3 }, tween(100)) + fadeOut(tween(100))
        } else {
            slideInHorizontally({ -it / 3 }, tween(100)) + fadeIn(tween(100)) togetherWith
                slideOutHorizontally({ it }, tween(100)) + fadeOut(tween(100))
        }
    }
) { screen -> /* render screen */ }
```

### ⑬ 波浪进度条 = 计时/番茄循环视觉锚点（来自 Momentum）

**Why**：M3 Expressive 自带 `LinearWavyProgressIndicator`，比直线进度条更生动，特别适合"专注、阅读、冥想"类场景。
**How to apply**：

```kotlin
LinearWavyProgressIndicator(
    progress = { 0.65f },
    modifier = Modifier.fillMaxWidth(),
)
```

### ⑭ AppIntro 用纯色全屏背景（来自 Reef）

**Why**：每屏一个鲜明色——深蓝 / 警示橙红 / 紫蓝 / 暖橙 / 薄荷青 / 紫罗兰——立刻区分步骤、提升记忆点。
**How to apply**：6 屏典型权限引导：欢迎 / 无障碍 / 用量统计 / 通知 / 电池优化 / 勿扰。每屏 `IntroPage(backgroundColor = Color(0xFFXXXXXX), contentColor = Color.White)`。允许在 AppIntro 这种"非主流程、纯展示"场景例外硬编码色值。

### ⑮ 选中态用特殊形状提升视觉锚点（来自 Momentum）

**Why**：选中调色板 chip 时把 `CircleShape` 切换成 `MaterialShapes.VerySunny.toShape()`，"光芒四射"形状立刻吸睛。
**How to apply**：用于"已选/激活"的 chip、tab、icon 容器。

```kotlin
Box(
    modifier = Modifier
        .size(50.dp)
        .clip(if (selected) MaterialShapes.VerySunny.toShape() else CircleShape)
)
```

### ⑯ 多容器卡片的 alpha 副文字模式

**Why**：在 `*Container` 彩色卡里，副文字用 `on{X}Container.copy(alpha = 0.7f~0.8f)` 弱化，而不是切换到不同色——保持色相协调。
**How to apply**：

```kotlin
Text(text = title, color = MaterialTheme.colorScheme.onPrimaryContainer)
Text(
    text = subtitle,
    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
)
```

### ⑰ 专注/计时屏的"超大数字"沉浸感（来自 Reef）

**Why**：Reef 计时屏用 88~92sp Bold 显示读秒——把屏幕变成一块"巨型时钟"，强化沉浸感。**字号尺度可参考，字体选择留给调用方**。
**How to apply**：

```kotlin
Text(
    text = timeLeft,
    fontSize = 88.sp,
    fontWeight = FontWeight.Bold,
    textAlign = TextAlign.Center,
    color = if (isBreak) MaterialTheme.colorScheme.tertiary
            else MaterialTheme.colorScheme.onSurface
)
```

> ⚠️ 字号是数值，不是字体；本 skill 范围。字体由项目自定。

### ⑱ 卡片高度建议固定（来自 Reef）

**Why**：Reef 用 `Modifier.height(180.dp / 260.dp)` 固定主磁贴高度，**网格视觉整齐**，避免 wrap_content 导致行高错落。
**How to apply**：双卡并排用 180dp，旗舰单卡用 260dp，列表型卡用 wrap_content。

### ⑲ 关键操作 Dialog 标准结构（来自 Reef）

**How to apply**：

```kotlin
AlertDialog(
    onDismissRequest = onDismiss,
    icon = { Icon(Icons.Rounded.Groups, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary) },
    title = { Text(stringResource(R.string.title), style = MaterialTheme.typography.headlineSmall) },
    text = { Text(stringResource(R.string.body), style = MaterialTheme.typography.bodyMedium) },
    confirmButton = { Button(onClick = onConfirm) { /* icon + spacer + text */ } },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
)
```

### ⑳ 高自由度主题选项（来自 Momentum，进阶可选）

**Why**：Momentum 让用户调 Seed / PaletteStyle / AMOLED / Material You 四维度，**差异化竞品**。
**How to apply**：用 `com.materialkolor:material-kolor` 的 `DynamicMaterialTheme` 包装 `MaterialTheme`。需要时再启用，不要默认就堆叠（多数 App 简单"跟随系统"已足够）。

```kotlin
DynamicMaterialTheme(
    seedColor = if (isMaterialYou && SDK_INT >= S) colorResource(android.R.color.system_accent1_200)
                else userSeedColor,
    isDark = isDark,
    isAmoled = isAmoled,
    style = userPaletteStyle.toMPaletteStyle(),
    content = content,
)
```

---

## 反模式（**不要做**）

- ❌ 在业务 Composable 里 `Color(0xFFxxxxxx)`（除 launcher icon、AppIntro 全屏背景外）
- ❌ 圆角小于 12dp（除分组中间项）
- ❌ 卡片不留外部 padding 直接贴边（最少 16dp 水平 padding）
- ❌ 用 `RadioGroup` / 原生单选——优先 `ToggleButton` 连块组
- ❌ 主操作单击触发不可逆动作——用 `SlideToConfirm` 或确认 Dialog
- ❌ 在彩色 Container 卡里副文字用纯黑或灰——必须 `on{X}Container.copy(alpha = ...)`
- ❌ 顶栏写死 `containerColor`、不接 `scrollBehavior`
- ❌ 不为图标加 `CircleShape` 容器——卡片缺少视觉锚点

---

## 可直接复用清单（按推荐度排序）

| # | 模式 | 来源 | 落点 |
|---|---|---|---|
| 1 | `SettingsCard(index, listSize)` 智能圆角 | Reef | 设置页/任意分组列表 |
| 2 | 圆形图标容器 36~48dp | Reef | 所有卡片视觉锚点 |
| 3 | 多容器色卡片拼盘 | Reef | 主页磁贴 |
| 4 | `pressedShape` 按下变形 | Reef | 全局按钮 |
| 5 | `MediumTopAppBar` + 滚动渐变 | 双方 | 全局顶栏 |
| 6 | `SlideToConfirm` 滑动解锁胶囊 | Reef | 启动专注/确认危险操作 |
| 7 | `AssistChip` 快捷预设 | Reef | 时长/数量预设 |
| 8 | `ToggleButton` 连块组 | 双方 | 分段切换 |
| 9 | `AppBottomSheet`（500dp + 32dp + animateContentSize）| Momentum | 通用底部弹窗 |
| 10 | `AnimatedContent` 横向切屏 | Reef | 设置主→子页 |
| 11 | `LinearWavyProgressIndicator` 波浪进度 | Momentum | 计时/番茄进度 |
| 12 | 6 屏纯色 AppIntro | Reef | 首启权限引导 |
| 13 | `MaterialShapes.VerySunny` 选中态 | Momentum | 选中 chip 视觉强调 |
| 14 | 卡片固定高度 180/260dp | Reef | 主页磁贴对齐 |
| 15 | Material You + Seed + AMOLED 四维主题（进阶） | Momentum | 高级设置项 |

---

## 检查清单（提交前自检）

- [ ] 没有任何业务色值用 `Color(0xFF...)` 写死（豁免：launcher icon / AppIntro 全屏背景）
- [ ] 所有卡片有圆角 ≥ 16dp
- [ ] 所有卡片图标用 `CircleShape` 容器包裹
- [ ] 主操作按钮启用 `pressedShape`
- [ ] 顶栏接入 `scrollBehavior`，配 `surface → surfaceContainer` 渐变
- [ ] 设置项分组复用 `SettingsCard(index, listSize)`
- [ ] 彩色 Container 卡片副文字用 `on{X}Container.copy(alpha = 0.7f)` 弱化
- [ ] 危险/不可逆操作用 `SlideToConfirm` 或 `AlertDialog` 二次确认
- [ ] 切屏用 `AnimatedContent` + 横向滑入 + 淡入
- [ ] 平板友好：`ModalBottomSheet` 设 `sheetMaxWidth = 500.dp`

---

## 与其他 skill 的关系

- **不替代** `chinese-documentation` / 字体相关 skill——本 skill 专攻视觉/交互层
- **可叠加** `brainstorming`（需求阶段）→ 本 skill（设计阶段）→ `test-driven-development`（实现阶段）
- **进阶配套**：`com.materialkolor` 库提供 `DynamicMaterialTheme`，配合 ⑳ 实现完整 Material You Plus

---

> 来源：static analysis of Momentum (`shub39.momentum`) and Reef (`dev.pranav.reef`)
> 已分析关键文件 24 个：`MomentumTheme.kt`、`ReefTheme.kt`、`MomentumBottomSheet.kt`、`zigZagBackground.kt`、`ProjectListItem.kt`、`AlarmCard.kt`、`LookAndFeel.kt`、`Onboarding.kt`、`MainScreen.kt`、`TimerScreen.kt`、`SettingsComponents.kt`、`MainSettingsScreen.kt`、`SettingsScreen.kt`、`AboutScreen.kt`、`AppIntroScreen.kt` 及 themes/colors XML 等。
> 适用：Android Jetpack Compose + Material 3（含 Expressive 实验 API）项目。
