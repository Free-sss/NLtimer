# Expressive Fusion 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将 Momentum × Reef 的 12 个视觉/交互优点以"可切换表达力"方式融入 NLtimer

**架构：** 在现有 StyleConfig 体系上新增 ExpressivenessPreset 主旋钮 + 5 个子维度枚举，三档预设联动所有视觉维度。新增 GroupCard/AppBottomSheet/DurationAssistChipRow 组件，增强 SlideActionPill，新增 AppIntroScreen。

**技术栈：** Jetpack Compose + Material3 Expressive + MaterialKolor + Room + Hilt + DataStore

---

## 文件结构

### 新建文件

| 文件 | 职责 |
|------|------|
| `core/designsystem/src/main/res/font/dm_serif_text.ttf` | DM Serif Text 衬线字体 |
| `core/designsystem/.../theme/Expressiveness.kt` | ExpressivenessPreset + 5个子维度枚举 + toStyleConfig() |
| `core/designsystem/.../theme/TimerTypography.kt` | LocalTimerTypography + 计时字号/字体解析 |
| `core/designsystem/.../component/GroupCard.kt` | GroupCard 自动圆角组件 |
| `core/designsystem/.../component/AppBottomSheet.kt` | AppBottomSheet 统一封装 |
| `core/designsystem/.../component/DurationAssistChipRow.kt` | 快捷时长 Chip 行 |
| `core/designsystem/.../component/AppIntroScreen.kt` | 6屏引导页 |
| `core/designsystem/.../component/PressedShapeExt.kt` | buttonShapes/iconButtonShapes 工具函数 |
| `core/designsystem/.../component/WavyProgressExt.kt` | 波浪进度条封装 |
| `core/designsystem/.../component/IconContainerExt.kt` | Modifier.iconContainer 扩展 |
| `core/designsystem/.../component/CardColorExt.kt` | cardColorStrategy 解析扩展 |

### 修改文件

| 文件 | 变更 |
|------|------|
| `core/designsystem/.../theme/StyleConfig.kt` | StyleConfig 新增 6 个字段 |
| `core/designsystem/.../theme/ShapeTokens.kt` | 新增 CORNER_SUPER_LARGE=36 |
| `core/designsystem/.../theme/Fonts.kt` | 新增 DM_SERIF_TEXT |
| `core/designsystem/.../theme/EnumExt.kt` | 新增 DM_SERIF_TEXT.toFontRes() |
| `core/designsystem/.../theme/StyleExt.kt` | styledCorner clamp 用 CORNER_SUPER_LARGE |
| `core/data/.../SettingsPrefsImpl.kt` | 新增 6 个 DataStore key + 读写逻辑 |
| `core/data/.../model/DialogGridConfig.kt` | 新增 durationPresets 字段 |
| `feature/settings/.../ThemeSettingsViewModel.kt` | 新增表达力相关方法 |
| `feature/settings/.../ThemeSettingsScreen.kt` | 新增表达力预设 UI |
| `feature/home/.../components/moment/ActiveCard.kt` | 计时字号/衬线适配 |
| `feature/home/.../components/SlideActionPill.kt` | 增强三档弹簧/进度/图标 |
| `core/behaviorui/.../sheet/AddBehaviorSheet.kt` | DurationAssistChipRow 接入 |
| `app/.../MainActivity.kt` | AppIntro 首次启动检查 |

---

## 任务 1：Expressiveness 枚举与 StyleConfig 扩展

**文件：**
- 修改：`core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/StyleConfig.kt`
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/Expressiveness.kt`
- 修改：`core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/ShapeTokens.kt`

- [ ] **步骤 1：创建 Expressiveness.kt** — 包含 ExpressivenessPreset / CardColorStrategy / IconContainerSize / TimerTypography / PressedShapeLevel / WavyProgressLevel 六个枚举 + toStyleConfig() 映射函数。三个预设的参数映射见设计规格 §2.3。

- [ ] **步骤 2：扩展 StyleConfig** — 在 data class 新增 6 个字段：expressiveness / cardColorStrategy / iconContainerSize / timerTypography / pressedShape / wavyProgress，均使用 SUBDUED/OFF 默认值保持向后兼容。

- [ ] **步骤 3：新增 ShapeTokens.CORNER_SUPER_LARGE = 36**

- [ ] **步骤 4：Commit**
```bash
git commit -m "feat(designsystem): 新增 ExpressivenessPreset 及子维度枚举，扩展 StyleConfig"
```

---

## 任务 2：styledCorner clamp 适配 + DM Serif Text 字体

**文件：**
- 修改：`core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/StyleExt.kt`
- 修改：`core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/Fonts.kt`
- 修改：`core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/EnumExt.kt`

- [ ] **步骤 1：修改 styledCorner clamp** — 将 `coerceIn(0f, 50f)` 改为 `coerceIn(0f, ShapeTokens.CORNER_SUPER_LARGE.toFloat())`

- [ ] **步骤 2：Fonts 枚举新增 DM_SERIF_TEXT** — EnumExt 中 toFontRes() 返回 R.font.dm_serif_text，toDisplayString() 返回 "DM Serif Text"

- [ ] **步骤 3：放置 dm_serif_text.ttf** — 从 Google Fonts 下载 Apache 2.0 版本放到 `core/designsystem/src/main/res/font/dm_serif_text.ttf`

- [ ] **步骤 4：Commit**
```bash
git commit -m "feat(designsystem): styledCorner clamp 36dp + DM Serif Text 字体"
```

---

## 任务 3：LocalTimerTypography + 计时字号解析

**文件：**
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/TimerTypography.kt`
- 修改：`core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/Theme.kt`

- [ ] **步骤 1：创建 TimerTypography.kt** — TimerTextStyle data class（timeStyle: TextStyle, useSerif: Boolean）+ LocalTimerTypography compositionLocalOf + resolveTimerTextStyle() 函数，三档映射：HEADLINE=headlineLarge sans / DISPLAY_SMALL=displaySmall sans / DISPLAY_LARGE_SERIF=displayLarge + dm_serif_text fontFamily

- [ ] **步骤 2：在 NLtimerTheme 的 CompositionLocalProvider 中新增 LocalTimerTypography provides resolveTimerTextStyle()**

- [ ] **步骤 3：Commit**
```bash
git commit -m "feat(designsystem): LocalTimerTypography 计时字号/衬线解析"
```

---

## 任务 4：IconContainer 扩展

**文件：**
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/IconContainerExt.kt`

- [ ] **步骤 1：创建 Modifier.iconContainer(size: IconContainerSize, color: Color)** — NONE=空 / CIRCLE_SMALL=36dp CircleShape / CIRCLE_LARGE=44dp CircleShape

- [ ] **步骤 2：Commit**
```bash
git commit -m "feat(designsystem): Modifier.iconContainer 圆形图标容器扩展"
```

---

## 任务 5：CardColorStrategy 解析

**文件：**
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/CardColorExt.kt`

- [ ] **步骤 1：创建 cardColorForStrategy(strategy, containerIndex)** — SURFACE=surfaceContainer / TINTED_PRIMARY=primaryContainer.alpha(styledAlpha(0.3f)) / MULTI_CONTAINER=按 containerIndex%3 轮转 primary/secondary/tertiaryContainer

- [ ] **步骤 2：Commit**
```bash
git commit -m "feat(designsystem): cardColorForStrategy 卡片色策略解析"
```

---

## 任务 6：PressedShape 扩展

**文件：**
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/PressedShapeExt.kt`

- [ ] **步骤 1：创建 buttonShapesForLevel(level)** 和 **iconButtonShapesForLevel(level)** — OFF=shapes() / MILD=RoundedCornerShape(24) / FULL_MORPH=pressedShape 或 RoundedCornerShape(8) 降级

- [ ] **步骤 2：Commit**
```bash
git commit -m "feat(designsystem): buttonShapesForLevel / iconButtonShapesForLevel"
```

---

## 任务 7：WavyProgress 封装

**文件：**
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/WavyProgressExt.kt`

- [ ] **步骤 1：创建 AppProgressIndicator(progress, level)** — OFF=LinearProgressIndicator 4dp / ON=LinearWavyProgressIndicator 8dp / PROMINENT=LinearWavyProgressIndicator 12dp。如 API 不可用则降级为不同高度的 LinearProgressIndicator

- [ ] **步骤 2：Commit**
```bash
git commit -m "feat(designsystem): AppProgressIndicator 波浪进度封装"
```

---

## 任务 8：GroupCard 自动圆角组件

**文件：**
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/GroupCard.kt`

- [ ] **步骤 1：创建 GroupCard composable** — Surface 包裹 Column，shape 用 styledCorner(CORNER_LARGE)

- [ ] **步骤 2：创建 groupItemShape(index, listSize)** — listSize==1 全圆角 / index==0 上圆下直 / index==last 上直下圆 / else EXTRA_SMALL。替代 leadingItemShape/middleItemShape/endItemShape

- [ ] **步骤 3：Commit**
```bash
git commit -m "feat(designsystem): GroupCard 自动圆角组件"
```

---

## 任务 9：AppBottomSheet 封装

**文件：**
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/AppBottomSheet.kt`

- [ ] **步骤 1：创建 AppBottomSheet(onDismiss, title?, content)** — ModalBottomSheet + rememberModalBottomSheetState(skipPartiallyExpanded=true) + animateContentSize + padding 联动 expressiveness（>=EXPRESSIVE 32dp else 20dp）+ 可选 title + dragHandle

- [ ] **步骤 2：Commit**
```bash
git commit -m "feat(designsystem): AppBottomSheet 统一封装"
```

---

## 任务 10：DurationAssistChipRow + DialogGridConfig 扩展

**文件：**
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/DurationAssistChipRow.kt`
- 修改：`core/data/src/main/java/com/nltimer/core/data/model/DialogGridConfig.kt`

- [ ] **步骤 1：DialogGridConfig 新增 durationPresets: List&lt;Long&gt; = listOf(5, 15, 25, 45, 60)**（分钟）

- [ ] **步骤 2：创建 DurationAssistChipRow(durations, selectedDuration, onDurationSelect)** — Row + FlowRow，每个时长用 AssistChip，选中态用 FilterChip 或 border 高亮，显示 "X分" 格式

- [ ] **步骤 3：SettingsPrefsImpl 新增 durationPresets 的 DataStore 读写**

- [ ] **步骤 4：Commit**
```bash
git commit -m "feat(designsystem): DurationAssistChipRow 快捷时长 + DialogGridConfig 扩展"
```

---

## 任务 11：AppIntroScreen 6屏引导页

**文件：**
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/AppIntroScreen.kt`
- 修改：`core/data/.../SettingsPrefs.kt` — 新增 hasSeenIntro Flow
- 修改：`core/data/.../SettingsPrefsImpl.kt` — 新增 hasSeenIntro 持久化

- [ ] **步骤 1：AppIntroScreen(onFinish)** — HorizontalPager 6页，每页全屏纯色背景（seedColor HSL 派生6色），内容：大图标 + 标题(DM Serif Text) + 副文 + 页码指示器 + "跳过"/"下一步"/"开始"按钮

- [ ] **步骤 2：SettingsPrefs 新增 hasSeenIntro** — getHasSeenIntroFlow() + setHasSeenIntro(true)

- [ ] **步骤 3：Commit**
```bash
git commit -m "feat(designsystem): AppIntroScreen 6屏引导页 + hasSeenIntro 持久化"
```

---

## 任务 12：SettingsPrefs 持久化表达力字段

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/SettingsPrefsImpl.kt`

- [ ] **步骤 1：新增 6 个 DataStore key** — expressiveness_key / card_color_strategy_key / icon_container_size_key / timer_typography_key / pressed_shape_key / wavy_progress_key（均为 stringPreferencesKey）

- [ ] **步骤 2：修改 getThemeFlow** — 读取新增 key 填入 Theme.style 的对应字段

- [ ] **步骤 3：修改 updateTheme** — 写入新增 key

- [ ] **步骤 4：Commit**
```bash
git commit -m "feat(data): SettingsPrefs 持久化 Expressiveness 全部字段"
```

---

## 任务 13：ThemeSettings 页面表达力 UI

**文件：**
- 修改：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/ThemeSettingsViewModel.kt`
- 修改：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/ThemeSettingsScreen.kt`

- [ ] **步骤 1：ViewModel 新增方法** — onExpressivenessChange(preset) / onCardColorStrategyChange(strategy) / onIconContainerSizeChange(size) / onTimerTypographyChange(typography) / onPressedShapeChange(level) / onWavyProgressChange(level)

- [ ] **步骤 2：ThemeSettingsScreen 新增"表达力预设"区域** — 三段式选择（克制/标准/表达🔥），选中后自动调用 onExpressivenessChange 批量更新 StyleConfig；子项可独立切换，手动覆盖后预设显示"自定义"标签

- [ ] **步骤 3：新增子项 UI** — CardColorStrategy 单选 / IconContainerSize 单选 / TimerTypography 单选 / PressedShapeLevel 单选 / WavyProgressLevel 单选

- [ ] **步骤 4：Commit**
```bash
git commit -m "feat(settings): 表达力预设 UI + 子维度微调"
```

---

## 任务 14：ActiveCard 计时字号/衬线适配

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/components/moment/ActiveCard.kt`

- [ ] **步骤 1：将活动名和时长的 Typography 替换为 LocalTimerTypography** — 读取 LocalTimerTypography.current.timeStyle 应用到时长文字，useSerif 控制是否额外应用 DM Serif Text

- [ ] **步骤 2：Commit**
```bash
git commit -m "feat(home): ActiveCard 计时字号/衬线适配表达力"
```

---

## 任务 15：SlideActionPill 三档增强

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/components/SlideActionPill.kt`

- [ ] **步骤 1：新增 pressedShape 参数** — 从 LocalTheme.current.style 读取 PressedShapeLevel

- [ ] **步骤 2：SUBDUED 档** — 无动画，onClick 直接触发

- [ ] **步骤 3：STANDARD 档** — 线性拖动，无弹簧

- [ ] **步骤 4：EXPRESSIVE 档** — 弹簧动画(DampingRatioMediumBouncy/StiffnessLow) + 进度色填充(0→70%) + thumb 图标切换(▶→✓) + 文案切换("滑动"→"松开") + thumb 阴影(elevation)

- [ ] **步骤 5：Commit**
```bash
git commit -m "feat(home): SlideActionPill 三档表达力增强"
```

---

## 任务 16：AddBehaviorSheet 接入 DurationAssistChipRow

**文件：**
- 修改：`core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorSheet.kt`

- [ ] **步骤 1：在时长选择区域新增 DurationAssistChipRow** — 读取 DialogGridConfig.durationPresets，选中时自动填充起止时间差

- [ ] **步骤 2：Commit**
```bash
git commit -m "feat(behaviorui): AddBehaviorSheet 接入快捷时长 Chip"
```

---

## 任务 17：AppIntro 首次启动集成

**文件：**
- 修改：`app/src/main/java/com/nltimer/app/MainActivity.kt`

- [ ] **步骤 1：在 MainActivity 的 setContent 中检查 hasSeenIntro** — 未看过则先显示 AppIntroScreen，完成后设置 hasSeenIntro=true 再进入 NLtimerApp

- [ ] **步骤 2：Commit**
```bash
git commit -m "feat(app): 首次启动 AppIntro 集成"
```

---

## 任务 18：VerySunny 选中态形状

**文件：**
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/component/SelectedShapeExt.kt`

- [ ] **步骤 1：创建 selectedShape(expressiveness)** — SUBDUED/STANDARD=CircleShape / EXPRESSIVE=MaterialShapes.VerySunny.toShape()。如 API 不可用降级为 RoundedCornerShape(16)

- [ ] **步骤 2：Commit**
```bash
git commit -m "feat(designsystem): VerySunny 选中态形状（EXPRESSIVE 档）"
```
