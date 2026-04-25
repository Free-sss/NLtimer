# NLtimer 应用框架实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 搭建 NLtimer 应用基础框架，包括模态侧边栏（Modal Drawer）和底部导航栏（Bottom Navigation），包含 4 个路由页面（主页、副页、统计、设置）作为独立 feature 模块。

**架构：** 单 Activity + ModalNavigationDrawer + Scaffold 作为全局骨架，Navigation Compose 管理 4 个路由页面，每个页面为独立 feature 模块，通过 `projects.core.designsystem` 共享设计系统。

**技术栈：** Jetpack Compose, Material Design 3, Navigation Compose, Kotlin

---

## 文件清单

| 文件 | 操作 | 职责 |
|------|------|------|
| `settings.gradle.kts` | 修改 | 注册 4 个新 feature 模块 |
| `feature/home/build.gradle.kts` | 创建 | 主页模块构建配置 |
| `feature/home/src/main/AndroidManifest.xml` | 创建 | Android 清单 |
| `feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt` | 创建 | 主页 UI |
| `feature/sub/build.gradle.kts` | 创建 | 副页模块构建配置 |
| `feature/sub/src/main/AndroidManifest.xml` | 创建 | Android 清单 |
| `feature/sub/src/main/java/com/nltimer/feature/sub/ui/SubScreen.kt` | 创建 | 副页 UI |
| `feature/stats/build.gradle.kts` | 创建 | 统计模块构建配置 |
| `feature/stats/src/main/AndroidManifest.xml` | 创建 | Android 清单 |
| `feature/stats/src/main/java/com/nltimer/feature/stats/ui/StatsScreen.kt` | 创建 | 统计页 UI |
| `feature/settings/build.gradle.kts` | 创建 | 设置模块构建配置 |
| `feature/settings/src/main/AndroidManifest.xml` | 创建 | Android 清单 |
| `feature/settings/src/main/java/com/nltimer/feature/settings/ui/SettingsScreen.kt` | 创建 | 设置页 UI |
| `app/build.gradle.kts` | 修改 | 添加 4 个 feature 模块依赖 |
| `app/src/main/java/com/nltimer/app/component/AppDrawer.kt` | 创建 | 侧边栏组件 |
| `app/src/main/java/com/nltimer/app/component/AppBottomNavigation.kt` | 创建 | 底部导航组件 |
| `app/src/main/java/com/nltimer/app/component/AppTopAppBar.kt` | 创建 | 顶部应用栏 |
| `app/src/main/java/com/nltimer/app/NLtimerScaffold.kt` | 创建 | 统一页面骨架 |
| `app/src/main/java/com/nltimer/app/NLtimerApp.kt` | 修改 | 使用新 Scaffold 替代旧实现 |
| `app/src/main/java/com/nltimer/app/navigation/NLtimerNavHost.kt` | 修改 | 新增 4 个路由，启动页改为 home |

---

### 任务 1：注册 feature 模块并创建构建配置

**文件：**
- 修改：`settings.gradle.kts`
- 创建：`feature/home/build.gradle.kts`
- 创建：`feature/home/src/main/AndroidManifest.xml`
- 创建：`feature/sub/build.gradle.kts`
- 创建：`feature/sub/src/main/AndroidManifest.xml`
- 创建：`feature/stats/build.gradle.kts`
- 创建：`feature/stats/src/main/AndroidManifest.xml`
- 创建：`feature/settings/build.gradle.kts`
- 创建：`feature/settings/src/main/AndroidManifest.xml`

- [ ] **步骤 1：修改 settings.gradle.kts，注册新模块**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "NLtimer"

val APP_VERSION_NAME: String by settings
val APP_VERSION_CODE: String by settings

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

- [ ] **步骤 2：创建 feature/home/build.gradle.kts**

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    compileSdk = libs.versions.compile.sdk.version.get().toInt()

    defaultConfig {
        minSdk = libs.versions.min.sdk.version.get().toInt()
        namespace = "com.nltimer.feature.home"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = false
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
        disable.add("GradleDependency")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
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

- [ ] **步骤 3：创建 feature/home/src/main/AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
</manifest>
```

- [ ] **步骤 4：创建 feature/sub/build.gradle.kts**（同 home，改 namespace 为 `com.nltimer.feature.sub`）

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    compileSdk = libs.versions.compile.sdk.version.get().toInt()

    defaultConfig {
        minSdk = libs.versions.min.sdk.version.get().toInt()
        namespace = "com.nltimer.feature.sub"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = false
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
        disable.add("GradleDependency")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
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

- [ ] **步骤 5：创建 feature/sub/src/main/AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
</manifest>
```

- [ ] **步骤 6：创建 feature/stats/build.gradle.kts**（同 home，改 namespace 为 `com.nltimer.feature.stats`）

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    compileSdk = libs.versions.compile.sdk.version.get().toInt()

    defaultConfig {
        minSdk = libs.versions.min.sdk.version.get().toInt()
        namespace = "com.nltimer.feature.stats"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = false
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
        disable.add("GradleDependency")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
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

- [ ] **步骤 7：创建 feature/stats/src/main/AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
</manifest>
```

- [ ] **步骤 8：创建 feature/settings/build.gradle.kts**（同 home，改 namespace 为 `com.nltimer.feature.settings`）

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    compileSdk = libs.versions.compile.sdk.version.get().toInt()

    defaultConfig {
        minSdk = libs.versions.min.sdk.version.get().toInt()
        namespace = "com.nltimer.feature.settings"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = false
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
        disable.add("GradleDependency")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
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

- [ ] **步骤 9：创建 feature/settings/src/main/AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
</manifest>
```

- [ ] **步骤 10：运行 Gradle Sync 验证模块注册成功**

运行：`./gradlew projects`
预期：输出中包含 `:feature:home`、`:feature:sub`、`:feature:stats`、`:feature:settings`

---

### 任务 2：创建 4 个 Feature 模块的 Screen 页面

**文件：**
- 创建：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt`
- 创建：`feature/sub/src/main/java/com/nltimer/feature/sub/ui/SubScreen.kt`
- 创建：`feature/stats/src/main/java/com/nltimer/feature/stats/ui/StatsScreen.kt`
- 创建：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/SettingsScreen.kt`

- [ ] **步骤 1：创建 HomeScreen.kt**

```kotlin
package com.nltimer.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeRoute() {
    HomeScreen()
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "主页",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
```

- [ ] **步骤 2：创建 SubScreen.kt**

```kotlin
package com.nltimer.feature.sub.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SubRoute() {
    SubScreen()
}

@Composable
fun SubScreen(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "副页",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
```

- [ ] **步骤 3：创建 StatsScreen.kt**

```kotlin
package com.nltimer.feature.stats.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StatsRoute() {
    StatsScreen()
}

@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "统计",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
```

- [ ] **步骤 4：创建 SettingsScreen.kt**

```kotlin
package com.nltimer.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsRoute() {
    SettingsScreen()
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "设置",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
```

---

### 任务 3：修改 app/build.gradle.kts 添加模块依赖

**文件：**
- 修改：`app/build.gradle.kts`

- [ ] **步骤 1：在 app/build.gradle.kts 的 dependencies 块中新增 4 个 feature 模块依赖**

在现有的 `implementation(projects.feature.timer)` 下方添加：

```kotlin
dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.feature.timer)
    implementation(projects.feature.home)
    implementation(projects.feature.sub)
    implementation(projects.feature.stats)
    implementation(projects.feature.settings)

    // ... 其余依赖不变
```

完整修改后的 dependencies 块：

```kotlin
dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.feature.timer)
    implementation(projects.feature.home)
    implementation(projects.feature.sub)
    implementation(projects.feature.stats)
    implementation(projects.feature.settings)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    testImplementation(libs.junit)

    debugImplementation(libs.compose.ui.test.manifest)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
}
```

- [ ] **步骤 2：运行 Gradle Sync 验证依赖解析成功**

运行：`./gradlew :app:dependencies --configuration releaseCompileClasspath`
预期：无错误，能看到 `:feature:home`、`:feature:sub`、`:feature:stats`、`:feature:settings` 在依赖树中

---

### 任务 4：创建侧边栏组件 AppDrawer.kt

**文件：**
- 创建：`app/src/main/java/com/nltimer/app/component/AppDrawer.kt`

- [ ] **步骤 1：创建 AppDrawer.kt**

```kotlin
package com.nltimer.app.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppDrawer(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalDrawerSheet(modifier = modifier) {
        Text(
            text = "NLtimer",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        NavigationDrawerItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                )
            },
            label = { Text("选项一") },
            selected = false,
            onClick = onClose,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        NavigationDrawerItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Brightness5,
                    contentDescription = null,
                )
            },
            label = { Text("选项二") },
            selected = false,
            onClick = onClose,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
    }
}
```

---

### 任务 5：创建顶部应用栏组件 AppTopAppBar.kt

**文件：**
- 创建：`app/src/main/java/com/nltimer/app/component/AppTopAppBar.kt`

- [ ] **步骤 1：创建 AppTopAppBar.kt**

```kotlin
package com.nltimer.app.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopAppBar(
    onMenuClick: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
) {
    CenterAlignedTopAppBar(
        title = { Text("NLtimer") },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "打开侧边栏",
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
        ),
        modifier = modifier,
    )
}
```

---

### 任务 6：创建底部导航组件 AppBottomNavigation.kt

**文件：**
- 创建：`app/src/main/java/com/nltimer/app/component/AppBottomNavigation.kt`

- [ ] **步骤 1：创建 AppBottomNavigation.kt**

```kotlin
package com.nltimer.app.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val navItems = listOf(
    NavItem("home", "主页", Icons.Default.Home),
    NavItem("sub", "副页", Icons.Default.Apps),
    NavItem("stats", "统计", Icons.Default.BarChart),
    NavItem("settings", "设置", Icons.Default.Settings),
)

@Composable
fun AppBottomNavigation(
    navController: androidx.navigation.NavHostController,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    NavigationBar(modifier = modifier) {
        navItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                    )
                },
                label = { Text(item.label) },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
    }
}
```

---

### 任务 7：创建统一页面骨架 NLtimerScaffold.kt

**文件：**
- 创建：`app/src/main/java/com/nltimer/app/NLtimerScaffold.kt`

- [ ] **步骤 1：创建 NLtimerScaffold.kt**

```kotlin
package com.nltimer.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.nltimer.app.component.AppBottomNavigation
import com.nltimer.app.component.AppDrawer
import com.nltimer.app.component.AppTopAppBar
import com.nltimer.app.navigation.NLtimerNavHost
import kotlinx.coroutines.launch

@Composable
fun NLtimerScaffold(
    navController: NavHostController,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
) {
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                onClose = {
                    coroutineScope.launch { drawerState.close() }
                },
            )
        },
    ) {
        Scaffold(
            topBar = {
                AppTopAppBar(
                    onMenuClick = {
                        coroutineScope.launch { drawerState.open() }
                    },
                )
            },
            bottomBar = { AppBottomNavigation(navController) },
        ) { padding ->
            NLtimerNavHost(
                navController = navController,
                modifier = Modifier.padding(padding),
            )
        }
    }
}
```

---

### 任务 8：修改 NLtimerApp.kt 和 NLtimerNavHost.kt

**文件：**
- 修改：`app/src/main/java/com/nltimer/app/NLtimerApp.kt`
- 修改：`app/src/main/java/com/nltimer/app/navigation/NLtimerNavHost.kt`

- [ ] **步骤 1：重写 NLtimerApp.kt**

```kotlin
package com.nltimer.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NLtimerApp() {
    val navController = rememberNavController()
    NLtimerScaffold(navController = navController)
}
```

- [ ] **步骤 2：重写 NLtimerNavHost.kt**

```kotlin
package com.nltimer.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nltimer.feature.home.ui.HomeRoute
import com.nltimer.feature.settings.ui.SettingsRoute
import com.nltimer.feature.stats.ui.StatsRoute
import com.nltimer.feature.sub.ui.SubRoute

@Composable
fun NLtimerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier,
    ) {
        composable("home") { HomeRoute() }
        composable("sub") { SubRoute() }
        composable("stats") { StatsRoute() }
        composable("settings") { SettingsRoute() }
    }
}
```

---

### 任务 9：构建验证

**文件：** 无

- [ ] **步骤 1：运行完整构建**

运行：`./gradlew :app:assembleDebug`
预期：BUILD SUCCESSFUL，无编译错误

- [ ] **步骤 2：运行 lint 检查**

运行：`./gradlew :app:lintDebug`
预期：BUILD SUCCESSFUL，无 error 级别问题

- [ ] **步骤 3：运行单元测试**

运行：`./gradlew test`
预期：所有测试通过

---

## 规格自检

1. **规格覆盖度** ✓：侧边栏组件（任务 4）、底部导航（任务 6）、页面骨架（任务 7）、4 个路由页面（任务 2）、路由配置（任务 8）、模块注册（任务 1）、依赖配置（任务 3）、构建验证（任务 9）全部覆盖。

2. **占位符扫描** ✓：无"待定"、"TODO"、模糊描述。每个步骤都有完整代码块。

3. **类型一致性** ✓：路由名统一使用 `"home"`、`"sub"`、`"stats"`、`"settings"`。组件命名一致使用 `App*` 前缀。`NavHostController` 类型统一。所有 Route 函数命名统一为 `{Name}Route()`。
