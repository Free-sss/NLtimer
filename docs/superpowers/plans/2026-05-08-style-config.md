# 样式风格可配置化 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将 NLtimer 中硬编码的圆角、边框粗细、Alpha 透明度参数转为用户可配置，采用混合模式（预设档位 + 高级自定义滑块），全局统一生效。

**架构：** 在现有 Theme data class 中嵌套 StyleConfig，通过 LocalTheme 传播。新增 ShapeTokens/BorderTokens 集中定义基础值，新增 styledCorner()/styledBorder()/styledAlpha() 辅助函数按比例缩放。SettingsPrefsImpl 扩展 6 个偏好键持久化 StyleConfig。ThemeSettingsScreen 新增"样式风格"分区。

**技术栈：** Kotlin, Jetpack Compose, DataStore Preferences, Material3

---

## 文件结构

| 操作 | 文件路径 | 职责 |
|------|---------|------|
| 创建 | core/designsystem/.../theme/StyleConfig.kt | StyleConfig + 3 枚举 + effectiveXxxScale() |
| 创建 | core/designsystem/.../theme/ShapeTokens.kt | ShapeTokens + BorderTokens 常量 |
| 创建 | core/designsystem/.../theme/StyleExt.kt | styledCorner/styledBorder/styledAlpha |
| 修改 | core/designsystem/.../theme/ThemeConfig.kt | Theme 新增 style 字段 |
| 修改 | core/data/.../SettingsPrefsImpl.kt | 读写 StyleConfig，6 个 key |
| 修改 | feature/settings/.../ThemeSettingsViewModel.kt | StyleConfig 方法 + showBorders 联动 |
| 修改 | feature/settings/.../ThemeSettingsScreen.kt | 新增样式风格分区 UI |
| 修改 | core/designsystem/.../theme/ModifierExt.kt | appBorder 改用 effectiveBorderScale |
| 修改 | core/designsystem/.../theme/ListItemExt.kt | 圆角改用 styledCorner |
| 修改 | core/designsystem/.../theme/EnumExt.kt | 3 个 toDisplayString() |
| 修改 | feature/home/.../components/GridCell.kt | 圆角+alpha+边框 |
| 修改 | feature/home/.../components/GridCellEmpty.kt | 圆角+alpha+边框 |
| 修改 | feature/home/.../components/GridCellLocked.kt | 圆角+alpha+边框 |
| 修改 | feature/home/.../components/MomentFocusCard.kt | 圆角+alpha+边框 |
| 修改 | feature/home/.../components/SlideActionPill.kt | 圆角+alpha |
| 修改 | feature/home/.../components/BehaviorCardContainer.kt | 圆角+边框 |
| 修改 | feature/home/.../components/BehaviorLogView.kt | alpha |
| 修改 | feature/home/.../components/TimelineReverseView.kt | 圆角+alpha+边框 |
| 修改 | feature/home/.../components/TimeSideBar.kt | 圆角+alpha |
| 修改 | feature/home/.../components/TimeFloatingLabel.kt | 圆角 |
| 修改 | feature/home/.../sheet/ActivityGridComponent.kt | alpha+圆角+边框 |
| 修改 | app/.../component/RouteSettingsPopup.kt | 圆角+alpha |

---

### 任务 1：创建 StyleConfig 数据模型

**文件：** 创建 core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/StyleConfig.kt

- [ ] 步骤 1：创建 StyleConfig.kt，包含 CornerPreset/BorderPreset/AlphaPreset 枚举、StyleConfig data class、effectiveXxxScale() 扩展函数
- [ ] 步骤 2：构建验证 `gradlew.bat -p .worktrees/theme-config-style :core:designsystem:compileDebugKotlin`
- [ ] 步骤 3：Commit `feat(designsystem): 新增 StyleConfig 数据模型与预设枚举`

---

### 任务 2：创建 ShapeTokens + StyleExt

**文件：** 创建 ShapeTokens.kt 和 StyleExt.kt

- [ ] 步骤 1：创建 ShapeTokens.kt — ShapeTokens(CORNER_EXTRA_SMALL=4, CORNER_SMALL=6, CORNER_MEDIUM=12, CORNER_LARGE=16, CORNER_EXTRA_LARGE=28, CORNER_FULL=32, CORNER_PILL=36) + BorderTokens(THIN=1, STANDARD=2)
- [ ] 步骤 2：创建 StyleExt.kt — styledCorner(baseDp: Int): Dp、styledBorder(baseDp: Int): Dp、styledAlpha(baseAlpha: Float): Float，均 @Composable，读取 LocalTheme.current.style 并 coerceIn
- [ ] 步骤 3：构建验证
- [ ] 步骤 4：Commit `feat(designsystem): 新增 ShapeTokens 常量与 styledXxx 辅助函数`

---

### 任务 3：Theme data class 扩展 StyleConfig 字段

**文件：** 修改 ThemeConfig.kt

- [ ] 步骤 1：在 Theme data class 末尾添加 `val style: StyleConfig = StyleConfig(),`
- [ ] 步骤 2：构建验证
- [ ] 步骤 3：Commit `feat(designsystem): Theme 新增 StyleConfig 嵌套字段`

---

### 任务 4：SettingsPrefsImpl 持久化 StyleConfig

**文件：** 修改 SettingsPrefsImpl.kt

- [ ] 步骤 1：添加 import（floatPreferencesKey, AlphaPreset, BorderPreset, CornerPreset, StyleConfig）
- [ ] 步骤 2：修改 getThemeFlow() — Theme(...) 构造中在 showTimeSideBar 之后添加 style = StyleConfig(...) 读取 6 个 key
- [ ] 步骤 3：修改 updateTheme() — 在 showTimeSideBarKey 之后写入 6 个 key（customScale 为 null 时 remove）
- [ ] 步骤 4：在 companion object 中添加 6 个偏好键（cornerPresetKey, borderPresetKey, alphaPresetKey, cornerScaleCustomKey, borderScaleCustomKey, alphaScaleCustomKey）
- [ ] 步骤 5：构建验证
- [ ] 步骤 6：Commit `feat(data): SettingsPrefsImpl 持久化 StyleConfig 字段`

---

### 任务 5：ThemeSettingsViewModel 扩展 StyleConfig 方法

**文件：** 修改 ThemeSettingsViewModel.kt

- [ ] 步骤 1：添加 import（AlphaPreset, BorderPreset, CornerPreset, StyleConfig）
- [ ] 步骤 2：修改 onShowBordersToggle — 联动 borderPreset（关闭→NONE，开启→如果当前NONE则STANDARD）
- [ ] 步骤 3：新增方法：onCornerPresetChange, onBorderPresetChange(联动showBorders), onAlphaPresetChange, onCustomCornerScale, onCustomBorderScale, onCustomAlphaScale, onResetStyleConfig
- [ ] 步骤 4：构建验证
- [ ] 步骤 5：Commit `feat(settings): ThemeSettingsViewModel 新增 StyleConfig 方法与 showBorders 联动`

---

### 任务 6：ModifierExt 改用 effectiveBorderScale

**文件：** 修改 ModifierExt.kt

- [ ] 步骤 1：将 `val showBorders = LocalTheme.current.showBorders` + `if (showBorders)` 替换为 `val effectiveScale = LocalTheme.current.style.effectiveBorderScale()` + `if (effectiveScale > 0f)`
- [ ] 步骤 2：构建验证 + Commit `refactor(designsystem): appBorder 改用 effectiveBorderScale 判断`

---

### 任务 7：ListItemExt 改用 styledCorner

**文件：** 修改 ListItemExt.kt

- [ ] 步骤 1：删除 CONNECTED_CORNER_RADIUS=4 和 END_CORNER_RADIUS=16 常量。所有 shape 函数加 @Composable，内部改用 styledCorner(ShapeTokens.CORNER_XXX)。4→EXTRA_SMALL, 16→LARGE
- [ ] 步骤 2：构建验证 + Commit `refactor(designsystem): ListItemExt 圆角改用 styledCorner`

---

### 任务 8-14：硬编码批量替换

每个组件文件执行相同模式：添加 import → 替换 RoundedCornerShape/alpha/BorderStroke → 构建验证 → Commit

**替换映射：**
- 任务 8 GridCell.kt：12dp→MEDIUM, 0.3f/0.6f alpha, 2dp→STANDARD/1dp→THIN
- 任务 9 GridCellEmpty.kt：16dp→LARGE, 0.3f/0.5f alpha, 2dp→STANDARD
- 任务 10 GridCellLocked.kt：16dp→LARGE, 0.3f/0.35f alpha, 1dp→THIN
- 任务 11 MomentFocusCard.kt：32dp→FULL x3, 0.5f/0.6f/0.7f/0.8f alpha x6, 2dp→STANDARD
- 任务 12 SlideActionPill.kt：36dp→PILL, 0.3f base alpha（动画部分不替换）
- 任务 13 BehaviorCardContainer.kt + BehaviorLogView.kt：16dp→LARGE, 1dp→THIN, 0.15f/0.2f/0.5f/0.8f alpha
- 任务 14 TimelineReverseView.kt + TimeSideBar.kt + TimeFloatingLabel.kt：12dp→MEDIUM, 4dp→EXTRA_SMALL, 各 alpha

---

### 任务 15：ActivityGridComponent + RouteSettingsPopup 硬编码替换

- ActivityGridComponent.kt：6dp→SMALL, 0.15f/0.5f/0.9f alpha, 1dp→THIN
- RouteSettingsPopup.kt：12dp→MEDIUM, 0.5f alpha
- [ ] 构建验证 assembleDebug + Commit

---

### 任务 16：全量构建验证

- [ ] 运行 assembleDebug，预期 BUILD SUCCESSFUL
- [ ] rg 搜索剩余硬编码，确认无遗漏（排除 debug 模块和动画 alpha）

---

### 任务 17：ThemeSettingsScreen 新增样式风格分区 UI

**文件：** 修改 ThemeSettingsScreen.kt + EnumExt.kt

- [ ] 步骤 1：添加 import（SegmentedButton, Slider, CornerPreset, BorderPreset, AlphaPreset, StyleConfig）
- [ ] 步骤 2：ThemeSettingsRoute 传入新回调（onCornerPresetChange, onBorderPresetChange, onAlphaPresetChange, onCustomCornerScale, onCustomBorderScale, onCustomAlphaScale, onResetStyleConfig）
- [ ] 步骤 3：ThemeSettingsScreen + ThemeSettingsContent 添加参数并透传
- [ ] 步骤 4：在 showBorders ListItem 之后插入 StyleConfigSection 调用（showBorders 的 shape 从 endItemShape 改为 middleItemShape）
- [ ] 步骤 5：创建 StyleConfigSection composable — 3 个 SingleChoiceSegmentedButtonRow + AnimatedVisibility 高级 Slider 区 + TextButton 重置
- [ ] 步骤 6：EnumExt.kt 添加 toDisplayString() — COMPACT=紧凑/STANDARD=标准/ROUNDED=圆润/SOFT=超圆; NONE=无边/THIN=纤细/STANDARD=标准/THICK=粗厚; SUBTLE=淡雅/STANDARD=标准/VIVID=鲜明/SOLID=实心
- [ ] 步骤 7：构建验证 + Commit `feat(settings): ThemeSettingsScreen 新增样式风格分区 UI`

---

### 任务 18：最终验证与清理

- [ ] 全量构建 assembleDebug
- [ ] rg 搜索确认无遗漏硬编码
- [ ] git log 确认所有 commit 就绪

