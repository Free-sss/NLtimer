# NLtimer 应用框架设计规格

## 概述

为 NLtimer 应用搭建基础应用框架，包括模态侧边栏（Modal Drawer）和底部导航栏（Bottom Navigation Bar），为后续功能开发提供统一的页面骨架和导航结构。

## 设计决策

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 侧边栏类型 | ModalNavigationDrawer (抽屉式) | 点击外部区域关闭，带半透明遮罩，交互清晰 |
| 底部导航项 | 4 个：主页、副页、统计、设置 | 满足用户导航需求 |
| 图标风格 | Material Icons | Android 标准，与 MD3 风格统一 |
| 模块组织 | 每个页面一个独立 feature 模块 | Clean Architecture，职责清晰，便于扩展 |
| 启动页面 | `home` 替代原 `timer` | 主页作为应用默认入口 |

## 文件结构

```
app/src/main/java/com/nltimer/app/
├── NLtimerApplication.kt            # Hilt 入口（不变）
├── MainActivity.kt                  # Activity 入口（不变）
├── NLtimerApp.kt                    # 修改：添加 Drawer + BottomNav
├── NLtimerScaffold.kt               # 新建：统一页面骨架
├── component/
│   ├── AppDrawer.kt                 # 新建：侧边栏组件
│   └── AppBottomNavigation.kt       # 新建：底部导航组件
└── navigation/
    └── NLtimerNavHost.kt            # 修改：新增 4 个路由

feature/
├── timer/                           # 保持不变
├── home/                            # 新建：主页模块
│   └── src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt
├── sub/                             # 新建：副页模块
│   └── src/main/java/com/nltimer/feature/sub/ui/SubScreen.kt
├── stats/                           # 新建：统计模块
│   └── src/main/java/com/nltimer/feature/stats/ui/StatsScreen.kt
└── settings/                        # 新建：设置模块
    └── src/main/java/com/nltimer/feature/settings/ui/SettingsScreen.kt
```

## 模块依赖关系

```
:app → :core:designsystem
:app → :feature:timer
:app → :feature:home
:app → :feature:sub
:app → :feature:stats
:app → :feature:settings
:feature:* → :core:designsystem
```

## 组件设计

### NLtimerScaffold.kt

```kotlin
@Composable
fun NLtimerScaffold(
    navController: NavHostController,
    drawerState: DrawerState,
    onDrawerOpen: () -> Unit,
    onDrawerClose: () -> Unit,
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { AppDrawer(onClose = onDrawerClose) },
    ) {
        Scaffold(
            topBar = { AppTopAppBar(onMenuClick = onDrawerOpen) },
            bottomBar = { AppBottomNavigation(navController) },
        ) { padding ->
            NLtimerNavHost(navController, modifier = Modifier.padding(padding))
        }
    }
}
```

### AppDrawer.kt

```kotlin
@Composable
fun AppDrawer(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalDrawerSheet { /* 标题 + 选项列表 */ }
}
```

- 使用 `ModalNavigationDrawer` + `ModalDrawerSheet`
- `dismissible = true`，点击遮罩关闭
- 包含至少 2 个测试选项（"选项一"、"选项二"）
- 动画效果由 Compose Material 3 内置提供

### AppBottomNavigation.kt

```kotlin
@Composable
fun AppBottomNavigation(navController: NavHostController) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = { navigateTo(item.route) },
            )
        }
    }
}
```

- 4 个 NavigationBarItem
- 图标：Home, Apps, BarChart, Settings (Material Icons Extended)
- 选中状态根据 `navController.currentBackStackEntry` 自动判断

### NLtimerNavHost.kt

```kotlin
NavHost(navController, startDestination = "home") {
    composable("home") { HomeRoute() }
    composable("sub") { SubRoute() }
    composable("stats") { StatsRoute() }
    composable("settings") { SettingsRoute() }
}
```

### 路由页面（4 个 feature 模块）

每个 Screen 为无状态 Composable，使用 MD3 组件展示占位内容：

```kotlin
@Composable
fun HomeScreen() {
    Scaffold { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Default.Home, contentDescription = null, Modifier.size(64.dp))
            Text("主页", style = MaterialTheme.typography.headlineLarge)
        }
    }
}
```

## 依赖配置

### settings.gradle.kts

新增模块注册：

```kotlin
include(
    "app",
    "core:designsystem",
    "feature:timer",
    "feature:home",
    "feature:sub",
    "feature:stats",
    "feature:settings",
)
```

### feature/home|sub|stats|settings/build.gradle.kts

每个模块使用相同的构建配置模板：

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.nltimer.feature.{module}"
    // ... 标准配置
    buildFeatures { compose = true }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
}
```

## 导航路由表

| 路由名 | 页面 | 底部导航图标 |
|--------|------|-------------|
| `home` | HomeRoute | `Icons.Default.Home` |
| `sub` | SubRoute | `Icons.Default.Apps` |
| `stats` | StatsRoute | `Icons.Default.BarChart` |
| `settings` | SettingsRoute | `Icons.Default.Settings` |

## 侧边栏内容

```
┌─────────────────┐
│   NLtimer       │ ← 标题
├─────────────────┤
│ 📋 选项一       │ ← 测试选项
│ 📦 选项二       │ ← 测试选项
└─────────────────┘
```

## 开发要点

1. **响应式布局**：使用 `ModalNavigationDrawer` 作为最外层，`Scaffold` 作为页面骨架
2. **无障碍支持**：为所有 Icon 添加 `contentDescription`（装饰性图标传 `null`）
3. **状态管理**：侧边栏展开状态使用 `rememberDrawerState`，在 `NLtimerApp` 层级管理
4. **MD3 一致性**：强制使用 `MaterialTheme.colorScheme` 和 `MaterialTheme.typography`
5. **模块隔离**：每个 feature 模块独立构建，通过 `projects.core.designsystem` 共享设计系统
