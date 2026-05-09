# TopBar V2 设计：TopBarStyle 可配置化

## 背景

当前 NLtimer 的顶栏（topBar）始终渲染，无法配置为隐藏。部分场景下（沉浸式体验、全屏内容展示）用户希望移除顶栏，让内容延伸至状态栏区域。

## 目标

- 在主题配置二级页面新增 TopBarStyle 配置项
- 支持 `STANDARD`（标准）和 `NONE`（无）两种样式
- `NONE` 模式下顶栏不渲染、不遮挡内容，内容扩展到状态栏下方（沉浸式），各页面内容自行处理边距
- 使用枚举类型预留扩展空间，未来可加 `COLLAPSING`（折叠/滚动隐藏）等样式

## 方案选择

**采用全局 TopBarStyle 配置方案**（方案 1），理由：
- 配置入口在 ThemeSettings（全局主题页），语义上是全局风格配置
- 未来加 `COLLAPSING` 等样式也是全局生效，符合主题配置模型
- 如果后续需要 per-route 差异，可在枚举基础上扩展，无需现在过度设计

## 详细设计

### 1. 数据层

**枚举定义** — 在 `core:data` 模块新增 `TopBarStyle`：

```kotlin
// core/data/src/main/java/com/nltimer/core/data/model/TopBarStyle.kt
package com.nltimer.core.data.model

enum TopBarStyle {
    STANDARD,
    NONE,
}
```

**SettingsPrefs 接口扩展**：

```kotlin
// SettingsPrefs.kt 新增
fun getTopBarStyleFlow(): Flow<TopBarStyle>
suspend fun updateTopBarStyle(style: TopBarStyle)
```

**SettingsPrefsImpl 实现**：

- DataStore key: `top_bar_style`（`stringPreferencesKey`）
- 存储枚举 `name` 字符串，默认 `STANDARD`
- 读取时 `try { TopBarStyle.valueOf(name) } catch { TopBarStyle.STANDARD }`

### 2. Scaffold 层

**NLtimerScaffold 修改**：

- 注入 `SettingsPrefs`，收集 `topBarStyle` Flow
- 当 `topBarStyle == NONE` 时：
  - Scaffold 的 `topBar = {}`（空 lambda），不渲染任何 topBar 组件
  - 内容区域自然扩展到状态栏下方（沉浸式）
  - 各页面内容自行通过 `WindowInsets.statusBars` 等机制处理顶部 padding
  - 抽屉菜单入口和齿轮设置弹窗均不可用（因顶栏不存在）
- 当 `topBarStyle == STANDARD` 时：保持现有逻辑不变

**关键优化**：`NONE` 模式下 topBar 代码不执行，只传空 lambda 给 Scaffold，零渲染开销。

**ModalNavigationDrawer 处理**：`NONE` 模式下抽屉手势仍可保留（从左边缘滑动打开），但顶栏菜单图标不存在。

### 3. ThemeSettings UI 层

**ThemeSettingsViewModel 修改**：

- 新增 `topBarStyle: StateFlow<TopBarStyle>` 状态流
- 新增 `onTopBarStyleChange(style: TopBarStyle)` 方法

**ThemeSettingsScreen 修改**：

- 在"显示边框"配置项之前，新增"顶栏样式"配置项
- 使用 `SingleChoiceSegmentedButtonRow` 展示选项（与样式风格区域风格一致）
  - `STANDARD` → "标准"
  - `NONE` → "无"
- 参数 `topBarStyle` 和 `onTopBarStyleChange` 从 ThemeSettingsRoute 透传到 ThemeSettingsContent

### 4. 涉及文件清单

| 文件 | 变更类型 |
|------|---------|
| `core/data/src/main/java/com/nltimer/core/data/model/TopBarStyle.kt` | 新增 |
| `core/data/src/main/java/com/nltimer/core/data/SettingsPrefs.kt` | 修改（新增2个方法） |
| `core/data/src/main/java/com/nltimer/core/data/SettingsPrefsImpl.kt` | 修改（实现2个方法+key） |
| `app/src/main/java/com/nltimer/app/NLtimerScaffold.kt` | 修改（读取配置+条件渲染） |
| `feature/settings/src/main/java/com/nltimer/feature/settings/ui/ThemeSettingsViewModel.kt` | 修改（新增state+方法） |
| `feature/settings/src/main/java/com/nltimer/feature/settings/ui/ThemeSettingsScreen.kt` | 修改（新增配置UI行） |

### 5. 未来扩展

枚举设计预留了扩展空间，后续可新增：
- `COLLAPSING`：顶栏随滚动折叠/展开，标题和按钮可滚动隐藏
- `COMPACT`：紧凑模式，减少顶栏高度
- `TRANSPARENT`：透明背景顶栏，内容延伸到顶栏下方

新增样式只需在枚举中加一项，Scaffold 层加一个分支，ThemeSettings UI 加一个按钮。
