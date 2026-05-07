# 设置模块导航改造 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 保留设置首页的一级页身份并改成卡片入口，同时将主题配置和弹窗配置改为统一壳子的全屏二级页，并由 `NLtimerScaffold` 按路由决定是否显示全局导航栏。

**架构：** 在 `app` 模块中为设置二级页建立显式路由分组，让 `NLtimerScaffold` 按当前路由切换全局顶栏与底栏的显示策略。在 `feature/settings` 模块中抽出统一的 `SettingsSubpageScaffold`，让 `ThemeSettingsScreen` 与 `DialogConfigScreen` 只保留内容本体，设置首页则改为依赖全局顶栏的卡片入口页。

**技术栈：** Kotlin、Jetpack Compose、Material 3、Navigation Compose、Compose UI Test、JUnit

---

## 文件结构

| 操作 | 文件 | 职责 |
|------|------|------|
| 新增 | `feature/settings/src/main/java/com/nltimer/feature/settings/ui/SettingsSubpageScaffold.kt` | 设置二级页统一页面壳，承载标题、返回按钮、内容区边距 |
| 修改 | `app/src/main/java/com/nltimer/app/NLtimerScaffold.kt` | 按当前路由切换全局顶栏/底栏的显示策略 |
| 修改 | `app/src/main/java/com/nltimer/app/component/AppTopAppBar.kt` | 支持外部传入动态标题，避免设置首页仍显示 `NLtimer` |
| 修改 | `app/src/main/java/com/nltimer/app/navigation/NLtimerNavHost.kt` | 暴露设置链路路由常量，维持设置首页到二级页的跳转 |
| 修改 | `feature/settings/src/main/java/com/nltimer/feature/settings/ui/SettingsScreen.kt` | 去掉内部 `Scaffold` 和重复标题，改成卡片式模块入口 |
| 修改 | `feature/settings/src/main/java/com/nltimer/feature/settings/ui/ThemeSettingsScreen.kt` | 移除内部页面骨架，改为挂载到统一子页容器 |
| 修改 | `feature/settings/src/main/java/com/nltimer/feature/settings/ui/DialogConfigScreen.kt` | 移除内部页面骨架，改为挂载到统一子页容器 |
| 修改 | `feature/settings/build.gradle.kts` | 补充 `androidTest` 依赖，支持设置模块 Compose UI 测试 |
| 新增 | `feature/settings/src/androidTest/java/com/nltimer/feature/settings/ui/SettingsScreenTest.kt` | 验证设置首页不再渲染重复标题，且入口项可点击 |
| 新增 | `app/src/androidTest/java/com/nltimer/app/SettingsNavigationScaffoldTest.kt` | 验证设置首页显示全局栏，设置二级页隐藏全局栏 |

---

### 任务 1：先用测试锁定设置首页与二级页的导航可见性

**文件：**
- 修改：`feature/settings/build.gradle.kts`
- 新增：`feature/settings/src/androidTest/java/com/nltimer/feature/settings/ui/SettingsScreenTest.kt`
- 新增：`app/src/androidTest/java/com/nltimer/app/SettingsNavigationScaffoldTest.kt`

- [ ] **步骤 1：为 `feature/settings` 模块补充 Compose UI 测试依赖**

在 `feature/settings/build.gradle.kts` 的 `dependencies` 块末尾添加：

```kotlin
    debugImplementation(libs.compose.ui.test.manifest)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
```

- [ ] **步骤 2：新增设置首页 UI 测试，先写失败用例**

创建 `feature/settings/src/androidTest/java/com/nltimer/feature/settings/ui/SettingsScreenTest.kt`：

```kotlin
package com.nltimer.feature.settings.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.nltimer.core.designsystem.theme.NLtimerTheme
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreen_doesNotRenderDuplicateLargeTitle() {
        composeTestRule.setContent {
            NLtimerTheme {
                SettingsScreen()
            }
        }

        composeTestRule.onAllNodesWithText("设置").assertCountEquals(0)
        composeTestRule.onNodeWithText("主题配置").assertHasClickAction()
        composeTestRule.onNodeWithText("弹窗配置").assertHasClickAction()
    }
}
```

说明：这个测试先锁定最终目标——`SettingsScreen` 自己不再渲染「设置」标题，只保留模块入口项。当前实现里会失败，因为 `SettingsScreen` 仍然渲染了 `Text("设置")`。

- [ ] **步骤 3：新增应用级导航外壳测试，先写失败用例**

创建 `app/src/androidTest/java/com/nltimer/app/SettingsNavigationScaffoldTest.kt`：

```kotlin
package com.nltimer.app

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class SettingsNavigationScaffoldTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun settingsRoute_showsGlobalBars_butThemeRouteHidesThem() {
        composeTestRule.onNodeWithText("设置").performClick()

        composeTestRule.onNodeWithContentDescription("打开侧边栏").assertExists()
        composeTestRule.onNodeWithText("主题配置").performClick()

        composeTestRule.onNodeWithContentDescription("打开侧边栏").assertDoesNotExist()
        composeTestRule.onNodeWithText("主题配置").assertExists()
        composeTestRule.onNodeWithContentDescription("返回").assertExists()
    }
}
```

说明：当前实现里，进入主题配置后全局顶栏仍然在，测试应先失败。

- [ ] **步骤 4：运行设置模块测试，确认失败**

运行：`./gradlew :feature:settings:connectedDebugAndroidTest`

预期：
- `SettingsScreenTest.settingsScreen_doesNotRenderDuplicateLargeTitle` 失败
- 失败原因是仍能找到 `设置` 标题

- [ ] **步骤 5：运行应用级导航测试，确认失败**

运行：`./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.nltimer.app.SettingsNavigationScaffoldTest`

预期：
- `settingsRoute_showsGlobalBars_butThemeRouteHidesThem` 失败
- 失败原因是进入 `theme_settings` 后仍能找到全局顶栏菜单按钮

- [ ] **步骤 6：Commit**

```bash
git add feature/settings/build.gradle.kts feature/settings/src/androidTest/java/com/nltimer/feature/settings/ui/SettingsScreenTest.kt app/src/androidTest/java/com/nltimer/app/SettingsNavigationScaffoldTest.kt
git commit -m "test(设置模块): 添加导航层级与页面骨架测试"
```

---

### 任务 2：让全局导航壳按路由切换顶栏与底栏

**文件：**
- 修改：`app/src/main/java/com/nltimer/app/NLtimerScaffold.kt`
- 修改：`app/src/main/java/com/nltimer/app/component/AppTopAppBar.kt`
- 修改：`app/src/main/java/com/nltimer/app/navigation/NLtimerNavHost.kt`
- 测试：`app/src/androidTest/java/com/nltimer/app/SettingsNavigationScaffoldTest.kt`

- [ ] **步骤 1：在导航文件中集中定义设置链路路由常量**

在 `NLtimerNavHost.kt` 顶部 import 下方添加：

```kotlin
const val HOME_ROUTE = "home"
const val SUB_ROUTE = "sub"
const val STATS_ROUTE = "stats"
const val CATEGORIES_ROUTE = "categories"
const val MANAGEMENT_ACTIVITIES_ROUTE = "management_activities"
const val TAG_MANAGEMENT_ROUTE = "tag_management"
const val SETTINGS_ROUTE = "settings"
const val THEME_SETTINGS_ROUTE = "theme_settings"
const val DIALOG_CONFIG_ROUTE = "dialog_config"
```

并将 `NavHost` 中的字符串替换为对应常量，例如：

```kotlin
    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE,
        modifier = modifier,
    ) {
        composable(HOME_ROUTE) { HomeRoute() }
        composable(SUB_ROUTE) { SubRoute() }
        composable(STATS_ROUTE) { StatsRoute() }
        composable(CATEGORIES_ROUTE) { CategoriesRoute() }
        composable(MANAGEMENT_ACTIVITIES_ROUTE) { ActivityManagementRoute() }
        composable(TAG_MANAGEMENT_ROUTE) {
            TagManagementRoute(onNavigateBack = { navController.popBackStack() })
        }
        composable(SETTINGS_ROUTE) {
            SettingsRoute(
                onNavigateToThemeSettings = { navController.navigate(THEME_SETTINGS_ROUTE) },
                onNavigateToDialogConfig = { navController.navigate(DIALOG_CONFIG_ROUTE) },
            )
        }
        composable(THEME_SETTINGS_ROUTE) {
            ThemeSettingsRoute(onNavigateBack = { navController.popBackStack() })
        }
        composable(DIALOG_CONFIG_ROUTE) {
            DialogConfigRoute(onNavigateBack = { navController.popBackStack() })
        }
```

- [ ] **步骤 2：让 `AppTopAppBar` 支持动态标题**

将 `AppTopAppBar` 函数签名改为：

```kotlin
@Composable
fun AppTopAppBar(
    title: String,
    onMenuClick: () -> Unit,
    onSettingClick: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "打开侧边栏",
                )
            }
        },
        actions = {
            IconButton(onClick = onSettingClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "配置项",
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        modifier = modifier,
    )
}
```

- [ ] **步骤 3：在 `NLtimerScaffold` 中按路由决定是否显示全局栏**

将 `NLtimerScaffold.kt` 中 `currentRoute` 计算后的逻辑调整为：

```kotlin
    val currentRoute = navBackStackEntry?.destination?.route
    val primaryRoutes = setOf(
        HOME_ROUTE,
        SUB_ROUTE,
        STATS_ROUTE,
        CATEGORIES_ROUTE,
        MANAGEMENT_ACTIVITIES_ROUTE,
        SETTINGS_ROUTE,
    )
    val settingsFullscreenRoutes = setOf(
        THEME_SETTINGS_ROUTE,
        DIALOG_CONFIG_ROUTE,
    )
    val showGlobalBars = currentRoute in primaryRoutes
    val topBarTitle = when (currentRoute) {
        SETTINGS_ROUTE -> "设置"
        else -> "NLtimer"
    }
```

然后把 `Scaffold` 改为按条件渲染：

```kotlin
            Scaffold(
                topBar = {
                    if (showGlobalBars) {
                        AppTopAppBar(
                            title = topBarTitle,
                            onMenuClick = {
                                coroutineScope.launch { drawerState.open() }
                            },
                            onSettingClick = {
                                showSettingsPopup = true
                            },
                        )
                    }
                },
                bottomBar = {
                    if (showGlobalBars) {
                        AppBottomNavigation(navController)
                    }
                },
            ) { padding ->
                val hostModifier = if (showGlobalBars) {
                    Modifier.padding(padding)
                } else {
                    Modifier
                }
                NLtimerNavHost(
                    navController = navController,
                    modifier = hostModifier,
                )
            }
```

保持 `RouteSettingsPopup` 逻辑不变，不要把它错误地绑定到设置二级页。

- [ ] **步骤 4：运行应用级导航测试，确认转为通过**

运行：`./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.nltimer.app.SettingsNavigationScaffoldTest`

预期：
- `SettingsNavigationScaffoldTest` 全部通过
- 进入 `theme_settings` 后不再能找到全局菜单按钮

- [ ] **步骤 5：运行应用模块编译验证**

运行：`./gradlew :app:compileDebugKotlin`

预期：`BUILD SUCCESSFUL`

- [ ] **步骤 6：Commit**

```bash
git add app/src/main/java/com/nltimer/app/NLtimerScaffold.kt app/src/main/java/com/nltimer/app/component/AppTopAppBar.kt app/src/main/java/com/nltimer/app/navigation/NLtimerNavHost.kt
git commit -m "feat(导航壳): 按设置路由切换全局栏显示"
```

---

### 任务 3：抽出统一的设置二级页容器

**文件：**
- 新增：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/SettingsSubpageScaffold.kt`
- 修改：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/ThemeSettingsScreen.kt`
- 修改：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/DialogConfigScreen.kt`
- 测试：`app/src/androidTest/java/com/nltimer/app/SettingsNavigationScaffoldTest.kt`

- [ ] **步骤 1：新增统一的 `SettingsSubpageScaffold`**

创建 `feature/settings/src/main/java/com/nltimer/feature/settings/ui/SettingsSubpageScaffold.kt`：

```kotlin
package com.nltimer.feature.settings.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateBottomPadding
import androidx.compose.foundation.layout.calculateLeftPadding
import androidx.compose.foundation.layout.calculateRightPadding
import androidx.compose.foundation.layout.calculateTopPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSubpageScaffold(
    title: String,
    onNavigateBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                },
            )
        }
    ) { padding ->
        val layoutDirection = LocalLayoutDirection.current
        content(
            PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 24.dp,
                start = padding.calculateLeftPadding(layoutDirection) + 16.dp,
                end = padding.calculateRightPadding(layoutDirection) + 16.dp,
            )
        )
    }
}
```

- [ ] **步骤 2：让主题配置页只保留内容本体**

将 `ThemeSettingsScreen.kt` 中页面函数拆成“Route -> SubpageScaffold -> Content”。保留 `ThemeSettingsRoute`，但把 `ThemeSettingsScreen` 改成：

```kotlin
@Composable
fun ThemeSettingsScreen(
    theme: Theme,
    onSeedColorChange: (Color) -> Unit,
    onThemeSwitch: (AppTheme) -> Unit,
    onAmoledSwitch: (Boolean) -> Unit,
    onPaletteChange: (PaletteStyle) -> Unit,
    onMaterialYouToggle: (Boolean) -> Unit,
    onFontChange: (Fonts) -> Unit,
    onShowBordersToggle: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsSubpageScaffold(
        title = "主题配置",
        onNavigateBack = onNavigateBack,
    ) { padding ->
        ThemeSettingsContent(
            theme = theme,
            onSeedColorChange = onSeedColorChange,
            onThemeSwitch = onThemeSwitch,
            onAmoledSwitch = onAmoledSwitch,
            onPaletteChange = onPaletteChange,
            onMaterialYouToggle = onMaterialYouToggle,
            onFontChange = onFontChange,
            onShowBordersToggle = onShowBordersToggle,
            contentPadding = padding,
            modifier = modifier,
        )
    }
}
```

新增内容函数签名：

```kotlin
@Composable
private fun ThemeSettingsContent(
    theme: Theme,
    onSeedColorChange: (Color) -> Unit,
    onThemeSwitch: (AppTheme) -> Unit,
    onAmoledSwitch: (Boolean) -> Unit,
    onPaletteChange: (PaletteStyle) -> Unit,
    onMaterialYouToggle: (Boolean) -> Unit,
    onFontChange: (Fonts) -> Unit,
    onShowBordersToggle: (Boolean) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
)
```

并把原 `LazyColumn` 的 `contentPadding` 改为：

```kotlin
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = contentPadding,
        ) {
```

同时删除当前 `Scaffold`、`TopAppBar`、`nestedScroll`、`enterAlwaysScrollBehavior` 相关代码。

- [ ] **步骤 3：让弹窗配置页只保留内容本体**

将 `DialogConfigScreen.kt` 中私有页面函数改成公有页面函数，并挂到统一子页容器：

```kotlin
@Composable
fun DialogConfigScreen(
    config: DialogGridConfig,
    onUpdateConfig: (DialogGridConfig) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsSubpageScaffold(
        title = "弹窗配置",
        onNavigateBack = onNavigateBack,
    ) { padding ->
        DialogConfigContent(
            config = config,
            onUpdateConfig = onUpdateConfig,
            contentPadding = padding,
            modifier = modifier,
        )
    }
}
```

新增内容函数：

```kotlin
@Composable
private fun DialogConfigContent(
    config: DialogGridConfig,
    onUpdateConfig: (DialogGridConfig) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
    ) {
        // 保留现有 GridConfigBlock、ToggleControl、PathDrawModeSelector 结构
    }
}
```

删除内部 `Scaffold` 和 `TopAppBar` 相关代码，只让容器统一提供页面骨架。

- [ ] **步骤 4：运行设置模块测试，确认页面骨架测试通过**

运行：`./gradlew :feature:settings:connectedDebugAndroidTest`

预期：
- `SettingsScreenTest` 若仍失败，只剩首页标题问题
- 二级页相关可见性不再因重复骨架失败

- [ ] **步骤 5：运行应用级导航测试，确认设置二级页只剩一套返回与标题**

运行：`./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.nltimer.app.SettingsNavigationScaffoldTest`

预期：`PASS`

- [ ] **步骤 6：Commit**

```bash
git add feature/settings/src/main/java/com/nltimer/feature/settings/ui/SettingsSubpageScaffold.kt feature/settings/src/main/java/com/nltimer/feature/settings/ui/ThemeSettingsScreen.kt feature/settings/src/main/java/com/nltimer/feature/settings/ui/DialogConfigScreen.kt
git commit -m "refactor(设置模块): 统一二级页页面骨架"
```

---

### 任务 4：把设置首页改成依赖全局顶栏的卡片入口页

**文件：**
- 修改：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/SettingsScreen.kt`
- 测试：`feature/settings/src/androidTest/java/com/nltimer/feature/settings/ui/SettingsScreenTest.kt`

- [ ] **步骤 1：将 `SettingsScreen` 改成纯内容页**

删除 `SettingsScreen.kt` 中的内部 `Scaffold` 和大标题 `Text("设置")`，将主体改为：

```kotlin
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateToThemeSettings: () -> Unit = {},
    onNavigateToDialogConfig: () -> Unit = {},
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SettingsEntryCard(
                icon = Icons.Default.Palette,
                title = "主题配置",
                summary = "调整主题模式、配色方案、字体和主题色",
                onClick = onNavigateToThemeSettings,
            )
        }
        item {
            SettingsEntryCard(
                icon = Icons.Default.Dashboard,
                title = "弹窗配置",
                summary = "统一管理活动与标签弹窗的布局和显示样式",
                onClick = onNavigateToDialogConfig,
            )
        }
    }
}
```

- [ ] **步骤 2：将原 `SettingsItem` 升级为卡片入口组件**

在同文件中新增：

```kotlin
@Composable
private fun SettingsEntryCard(
    icon: ImageVector,
    title: String,
    summary: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
```

删除旧的 `HorizontalDivider` 列表项结构。

- [ ] **步骤 3：运行设置模块 UI 测试，确认首页不再重复渲染标题**

运行：`./gradlew :feature:settings:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.nltimer.feature.settings.ui.SettingsScreenTest`

预期：
- `settingsScreen_doesNotRenderDuplicateLargeTitle` 通过
- 两个入口项都可点击

- [ ] **步骤 4：运行设置模块编译验证**

运行：`./gradlew :feature:settings:compileDebugKotlin`

预期：`BUILD SUCCESSFUL`

- [ ] **步骤 5：Commit**

```bash
git add feature/settings/src/main/java/com/nltimer/feature/settings/ui/SettingsScreen.kt
git commit -m "feat(设置首页): 改为卡片式模块入口"
```

---

### 任务 5：执行最终回归验证并收口

**文件：**
- 验证：`app/src/androidTest/java/com/nltimer/app/SettingsNavigationScaffoldTest.kt`
- 验证：`feature/settings/src/androidTest/java/com/nltimer/feature/settings/ui/SettingsScreenTest.kt`
- 验证：`app/src/main/java/com/nltimer/app/NLtimerScaffold.kt`
- 验证：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/*.kt`

- [ ] **步骤 1：运行设置模块全部编译与测试**

运行：`./gradlew :feature:settings:compileDebugKotlin :feature:settings:connectedDebugAndroidTest`

预期：
- Kotlin 编译通过
- `SettingsScreenTest` 通过

- [ ] **步骤 2：运行应用模块编译与导航测试**

运行：`./gradlew :app:compileDebugKotlin :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.nltimer.app.SettingsNavigationScaffoldTest`

预期：
- Kotlin 编译通过
- `SettingsNavigationScaffoldTest` 通过

- [ ] **步骤 3：手工验证关键导航路径**

运行：`./gradlew :app:installDebug`

然后在设备或模拟器中手工检查：

- 从底部导航进入「设置」，全局顶栏标题显示为「设置」
- 设置首页内容区不再出现重复的「设置」大标题
- 点击「主题配置」后，全局顶栏和底栏消失，只保留统一子页顶栏
- 点击「弹窗配置」后，全局顶栏和底栏消失，只保留统一子页顶栏
- 从两个二级页点击返回后，都回到设置首页
- 切回 `home`、`sub`、`stats` 后，全局顶栏和底栏仍正常显示

- [ ] **步骤 4：查看工作区差异，确认未误改无关模块**

运行：`git diff -- app/src/main/java/com/nltimer/app feature/settings`

预期：
- 只包含导航壳、设置首页、设置二级页骨架和对应测试改动
- 不包含 `RouteSettingsPopup` 的意外行为修改

- [ ] **步骤 5：Commit**

```bash
git add app/src/androidTest/java/com/nltimer/app/SettingsNavigationScaffoldTest.kt feature/settings/src/androidTest/java/com/nltimer/feature/settings/ui/SettingsScreenTest.kt
git commit -m "test(设置导航): 补齐回归验证并收口改造"
```

---

## 规格覆盖检查

- **设置首页保留一级页身份：** 任务 2 通过 `AppTopAppBar` 动态标题和 `NLtimerScaffold` 路由分组落地，任务 4 去掉首页内部标题。
- **主题配置与弹窗配置改为全屏二级页：** 任务 2 隐藏全局栏，任务 3 引入 `SettingsSubpageScaffold` 承载二级页。
- **统一标题、返回与边距：** 任务 3 用统一容器收口。
- **消除重复标题、重复返回和顶部间距割裂：** 任务 3 和任务 4 分别覆盖二级页与设置首页。
- **验证导航返回与一级页回归：** 任务 1、任务 5 提供 UI 测试和手工回归路径。

## 占位符检查

- 本计划未使用「TODO」「待定」「后续实现」等占位词作为执行说明。
- 所有任务都给出了精确文件路径、命令和预期结果。
- 所有 commit message 使用中文 Conventional Commits 风格。
