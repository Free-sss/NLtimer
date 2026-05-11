# Center FAB Bottom Bar 设计规格

## 概述

新增 `BottomBarMode.CENTER_FAB` 模式，将悬浮底栏的布局从"左侧导航+右侧设置"改为"左侧设置+导航，居中FAB"。FAB 保持独立可拖拽。

## 布局

```
[⚙️设置] [🏠主页][📊统计]           [FAB↕]
  ↑          ↑                        ↑
独立圆   FloatingToolbar         独立可拖拽FAB
底栏左侧   紧邻设置               底部居中
```

三块独立浮动元素，通过同色 `surfaceContainerHigh` 统一视觉风格。

## 枚举变更

### `BottomBarMode.kt`

```kotlin
enum class BottomBarMode {
    STANDARD,
    FLOATING,
    CENTER_FAB,
}
```

### `EnumExt.kt`

```kotlin
fun BottomBarMode.toDisplayString(): String = when (this) {
    BottomBarMode.STANDARD -> "标准"
    BottomBarMode.FLOATING -> "悬浮"
    BottomBarMode.CENTER_FAB -> "悬浮居中"
}
```

## 组件变更

### `AppBottomNavigation.kt` — 新增 `AppCenterFabBottomBar`

- 接收 `navController`、`onSettingsClick`、`onSettingsLongClick`
- 使用 `Box` 布局，`contentAlignment = Alignment.BottomCenter`
- 左侧 `Row(Alignment.BottomStart)`：
  - 设置按钮：48dp 圆形 Surface，`surfaceContainerHigh` 色，支持 combinedClickable
  - 4dp 间距
  - `HorizontalFloatingToolbar`：仅包含主页+统计，不含设置
- 无 FAB（FAB 由各页面独立渲染）

### `BottomBarDragFab.kt` — 位置自适应

读取 `LocalTheme.current.bottomBarMode`：
- `CENTER_FAB` → `Alignment.BottomCenter`，无 start padding
- 其他 → `Alignment.BottomStart`，`padding(start = 12.dp)`

两种模式均保留 `navigationBarsPadding()` 和 `padding(bottom = 8.dp)`。

### `NLtimerScaffold.kt` — 处理新模式

```kotlin
val isCenterFab = theme.bottomBarMode == BottomBarMode.CENTER_FAB && !isSecondaryPage
```

当 `isCenterFab` 为 true 时，渲染 `AppCenterFabBottomBar`。

Scaffold 的 bottom padding 逻辑同步更新：
- `isFloating` 或 `isCenterFab` 时不加 80dp bottom padding
- 导航内容区 bottom padding：isFloating/isCenterFab → 0dp

### Feature 页面 — 无需变更

`HomeScreen`、`TagManagementScreen`、`ActivityManagementScreen` 使用 `BottomBarDragFab`，其位置由组件内部根据 `LocalTheme` 自动切换，无需页面级改动。

### `ThemeSettingsScreen.kt` — 自动适配

底栏模式选择器使用 `BottomBarMode.entries.forEachIndexed`，新增枚举值自动出现为新按钮。

### `SettingsPrefsImpl.kt` — 自动适配

`BottomBarMode.valueOf()` 可解析新值，无需额外持久化 key。

## 页级行为

| 页面 | 底栏 | FAB 位置 |
|------|------|----------|
| 主页 | 设置+导航 | 居中 |
| 统计 | 设置+导航 | 无 FAB |
| 标签管理 | 设置+导航 | 居中 |
| 活动管理 | 设置+导航 | 居中 |
| 设置 | 设置+导航 | 无 FAB |
| 主题配置等全屏 | 无 | 无 |

## 不变性

- 标准/悬浮模式行为完全不变
- FAB 拖拽交互不变
- 设置弹窗偏移量不变
