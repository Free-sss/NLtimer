# NLtimer 架构重构实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [x]`）语法来跟踪进度。

**目标：** 将 NLtimer 从 kotlin-android-template 模板全面重构为基于 MD3 + Clean Architecture + MVVM 的计时器应用

**架构：** 三层模块化架构 — `:app`（导航与 DI 入口）→ `:feature:timer`（计时器业务逻辑与 UI）→ `:core:designsystem`（MD3 主题与通用组件）。单 Activity + Compose Navigation，Hilt 依赖注入，StateFlow 驱动 UI 状态。

**技术栈：** Kotlin 2.3.21, Compose BOM 2025.12.01, Material3, Hilt 2.56.1, KSP, Navigation Compose 2.9.0, Lifecycle 2.9.0

***

## 文件结构

### 删除的文件/目录

| 路径                                   | 原因        |
| ------------------------------------ | --------- |
| `library-android/`                   | 模板模块      |
| `library-compose/`                   | 模板模块      |
| `library-kotlin/`                    | 模板模块      |
| `buildSrc/`                          | 模板专用脚本    |
| `app/src/main/res/layout/`           | 传统 XML 布局 |
| `app/src/main/res/drawable*/`        | 模板占位图标    |
| `app/src/main/res/mipmap*/`          | 模板占位图标    |
| `app/src/main/res/values/colors.xml` | MD3 主题替代  |
| `app/src/main/res/values/dimens.xml` | 不再需要      |
| `app/src/main/res/values/styles.xml` | MD3 主题替代  |
| `.github/workflows/cleanup.yaml`     | 模板 CI     |
| `.github/template-cleanup/`          | 模板目录      |
| `renovate.json`                      | 模板配置      |
| `TROUBLESHOOTING.md`                 | 模板文档      |
| `app/src/main/java/com/ncorti/`      | 旧包名源码     |

### 修改的文件

| 路径                                    | 变更                                         |
| ------------------------------------- | ------------------------------------------ |
| `gradle.properties`                   | 更新 APP\_ID，删除 GROUP/VERSION                |
| `settings.gradle.kts`                 | 更新 rootProject.name，替换模块列表                 |
| `build.gradle.kts`（根级）                | 移除模板插件，新增 Hilt/KSP 插件                      |
| `gradle/libs.versions.toml`           | 新增 Hilt/Navigation/Lifecycle/KSP 依赖，移除模板依赖 |
| `app/build.gradle.kts`                | 全面重写为 Compose + Hilt                       |
| `app/src/main/AndroidManifest.xml`    | 更新引用，添加 Application 类                      |
| `app/src/main/res/values/strings.xml` | 更新应用名                                      |

### 创建的文件

| 路径                                                                                        | 职责                    |
| ----------------------------------------------------------------------------------------- | --------------------- |
| `core/designsystem/build.gradle.kts`                                                      | designsystem 模块构建配置   |
| `core/designsystem/src/main/AndroidManifest.xml`                                          | 库模块 manifest          |
| `core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/Color.kt`            | MD3 配色定义              |
| `core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/Type.kt`             | MD3 字体定义              |
| `core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/Theme.kt`            | AppTheme composable   |
| `feature/timer/build.gradle.kts`                                                          | timer 模块构建配置          |
| `feature/timer/src/main/AndroidManifest.xml`                                              | 库模块 manifest          |
| `feature/timer/src/main/java/com/nltimer/feature/timer/model/TimerState.kt`               | 计时器状态数据模型             |
| `feature/timer/src/main/java/com/nltimer/feature/timer/repository/TimerRepository.kt`     | 计时逻辑 Flow             |
| `feature/timer/src/main/java/com/nltimer/feature/timer/viewmodel/TimerViewModel.kt`       | ViewModel + StateFlow |
| `feature/timer/src/main/java/com/nltimer/feature/timer/ui/TimerScreen.kt`                 | Compose UI            |
| `feature/timer/src/test/java/com/nltimer/feature/timer/repository/TimerRepositoryTest.kt` | Repository 单元测试       |
| `feature/timer/src/test/java/com/nltimer/feature/timer/viewmodel/TimerViewModelTest.kt`   | ViewModel 单元测试        |
| `app/src/main/java/com/nltimer/app/NLtimerApplication.kt`                                 | Hilt Application      |
| `app/src/main/java/com/nltimer/app/MainActivity.kt`                                       | 单 Activity Compose 入口 |
| `app/src/main/java/com/nltimer/app/NLtimerApp.kt`                                         | 顶层 Composable         |
| `app/src/main/java/com/nltimer/app/navigation/NLtimerNavHost.kt`                          | Navigation Graph      |

***

## 任务 1：删除模板模块和模板专用文件

**文件：**

- 删除：`library-android/`
- 删除：`library-compose/`
- 删除：`library-kotlin/`
- 删除：`buildSrc/`
- 删除：`.github/workflows/cleanup.yaml`
- 删除：`.github/template-cleanup/`
- 删除：`renovate.json`
- 删除：`TROUBLESHOOTING.md`
- [x] **步骤 1：删除模板模块目录**

```bash
Remove-Item -Recurse -Force library-android/, library-compose/, library-kotlin/, buildSrc/
```

- [x] **步骤 2：删除模板专用 CI 和配置文件**

```bash
Remove-Item -Force .github/workflows/cleanup.yaml
Remove-Item -Recurse -Force .github/template-cleanup/
Remove-Item -Force renovate.json
Remove-Item -Force TROUBLESHOOTING.md
```

- [x] **步骤 3：验证删除完成**

```bash
ls
```

预期：目录中不再有 `library-android`、`library-compose`、`library-kotlin`、`buildSrc`，根目录不再有 `renovate.json`、`TROUBLESHOOTING.md`。

- [x] **步骤 4：Commit**

```bash
git add -A
git commit -m "chore: 删除模板模块和模板专用文件"
```

***

## 任务 2：更新版本目录（libs.versions.toml）

**文件：**

- 修改：`gradle/libs.versions.toml`
- [x] **步骤 1：重写 libs.versions.toml**

将整个文件替换为以下内容：

```toml
[versions]
agp = "8.13.2"
androidx_activity_compose = "1.13.0"
androidx_test = "1.7.0"
androidx_test_ext = "1.3.0"
compose_bom = "2025.12.01"
core_ktx = "1.17.0"
detekt = "1.23.8"
hilt = "2.56.1"
hilt_navigation_compose = "1.2.0"
junit = "4.13.2"
kotlin = "2.3.21"
ksp = "2.3.21-1.0.29"
lifecycle = "2.9.0"
navigation_compose = "2.9.0"
compile_sdk_version = "36"
min_sdk_version = "23"
target_sdk_version = "36"
kotlinx_coroutines_test = "1.10.2"

[libraries]
junit = { module = "junit:junit", version.ref = "junit" }
androidx_activity_compose = { module = "androidx.activity:activity-compose", version.ref = "androidx_activity_compose" }
androidx_core_ktx = { module = "androidx.core:core-ktx", version.ref = "core_ktx" }
androidx_test_rules = { module = "androidx.test:rules", version.ref = "androidx_test" }
androidx_test_runner = { module = "androidx.test:runner", version.ref = "androidx_test" }
androidx_test_ext_junit = { module = "androidx.test.ext:junit", version.ref = "androidx_test_ext" }
compose_bom = { module = "androidx.compose:compose-bom", version.ref = "compose_bom" }
compose_material3 = { module = "androidx.compose.material3:material3" }
compose_ui = { module = "androidx.compose.ui:ui" }
compose_ui_tooling = { module = "androidx.compose.ui:ui-tooling" }
compose_ui_tooling_preview = { module = "androidx.compose.ui:ui-tooling-preview" }
compose_ui_test_junit4 = { module = "androidx.compose.ui:ui-test-junit4" }
compose_ui_test_manifest = { module = "androidx.compose.ui:ui-test-manifest" }
detekt_formatting = { module = "io.gitlab.arturbosch.detekt:detekt-formatting", version.ref = "detekt" }
hilt_android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt_compiler = { module = "com.google.dagger:hilt-compiler", version.ref = "hilt" }
hilt_navigation_compose = { module = "androidx.hilt:hilt-navigation-compose", version.ref = "hilt_navigation_compose" }
lifecycle_viewmodel_compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
lifecycle_runtime_compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }
navigation_compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation_compose" }
kotlinx_coroutines_test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinx_coroutines_test" }
agp = { module = "com.android.tools.build:gradle", version.ref = "agp" }
kgp = { module = "org.jetbrains.kotlin:kotlin-gradle-plugin", version.ref = "kotlin" }

[plugins]
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
compose_compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt_android = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

- [x] **步骤 2：验证文件内容**

```bash
cat gradle/libs.versions.toml
```

预期：包含 hilt、navigation\_compose、lifecycle、ksp 等新依赖，不再有 appcompat、constraint\_layout、espresso\_core、nexus\_publish。

- [x] **步骤 3：Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "chore: 更新版本目录，新增 Hilt/Navigation/Lifecycle/KSP 依赖"
```

***

## 任务 3：更新配置文件

**文件：**

- 修改：`gradle.properties`
- 修改：`settings.gradle.kts`
- 修改：`build.gradle.kts`（根级）
- [x] **步骤 1：重写 gradle.properties**

```properties
org.gradle.jvmargs=-Xmx1536m
android.useAndroidX=true
android.enableJetifier=true
kotlin.code.style=official

APP_VERSION_NAME=1.0.0
APP_VERSION_CODE=1
APP_ID=com.nltimer.app
```

- [x] **步骤 2：重写 settings.gradle.kts**

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "NLtimer"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    "app",
    "core:designsystem",
    "feature:timer"
)
```

- [x] **步骤 3：重写根级 build.gradle.kts**

```kotlin
plugins {
    id("com.android.application") apply false
    id("com.android.library") apply false
    kotlin("android") apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt)
}

val detektFormatting = libs.detekt.formatting

subprojects {
    apply {
        plugin("io.gitlab.arturbosch.detekt")
    }

    detekt {
        config.from(rootProject.files("config/detekt/detekt.yml"))
    }

    dependencies {
        detektPlugins(detektFormatting)
    }
}
```

- [x] **步骤 4：验证配置文件**

```bash
cat gradle.properties
cat settings.gradle.kts
cat build.gradle.kts
```

预期：gradle.properties 中 APP\_ID=com.nltimer.app，无 GROUP/VERSION；settings.gradle.kts 中 rootProject.name="NLtimer"，包含 app/core:designsystem/feature:timer；根级 build.gradle.kts 无 cleanup/base/nexus-publish 引用。

- [x] **步骤 5：Commit**

```bash
git add gradle.properties settings.gradle.kts build.gradle.kts
git commit -m "chore: 更新配置文件，切换到新模块结构"
```

***

## 任务 4：创建 core:designsystem 模块

**文件：**

- 创建：`core/designsystem/build.gradle.kts`
- 创建：`core/designsystem/src/main/AndroidManifest.xml`
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/Color.kt`
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/Type.kt`
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/theme/Theme.kt`
- [x] **步骤 1：创建模块目录结构**

```bash
mkdir -p core/designsystem/src/main/java/com/nltimer/core/designsystem/theme
```

- [x] **步骤 2：创建 core/designsystem/build.gradle.kts**

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    kotlin("android")
    alias(libs.plugins.compose.compiler)
}

android {
    compileSdk = libs.versions.compile.sdk.version.get().toInt()

    defaultConfig {
        minSdk = libs.versions.min.sdk.version.get().toInt()
        namespace = "com.nltimer.core.designsystem"
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
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
}
```

- [x] **步骤 3：创建 core/designsystem/src/main/AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" />
```

- [x] **步骤 4：创建 Color.kt**

```kotlin
package com.nltimer.core.designsystem.theme

import androidx.compose.ui.graphics.Color

val md_theme_light_primary = Color(0xFF6750A4)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFEADDFF)
val md_theme_light_onPrimaryContainer = Color(0xFF21005D)
val md_theme_light_secondary = Color(0xFF625B71)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFE8DEF8)
val md_theme_light_onSecondaryContainer = Color(0xFF1D192B)
val md_theme_light_tertiary = Color(0xFF7D5260)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFFFD8E4)
val md_theme_light_onTertiaryContainer = Color(0xFF31111D)
val md_theme_light_error = Color(0xFFB3261E)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = Color(0xFFF9DEDC)
val md_theme_light_onErrorContainer = Color(0xFF410E0B)
val md_theme_light_background = Color(0xFFFFFBFE)
val md_theme_light_onBackground = Color(0xFF1C1B1F)
val md_theme_light_surface = Color(0xFFFFFBFE)
val md_theme_light_onSurface = Color(0xFF1C1B1F)
val md_theme_light_surfaceVariant = Color(0xFFE7E0EC)
val md_theme_light_onSurfaceVariant = Color(0xFF49454F)
val md_theme_light_outline = Color(0xFF79747E)
val md_theme_light_inverseOnSurface = Color(0xFFF4EFF4)
val md_theme_light_inverseSurface = Color(0xFF313033)
val md_theme_light_inversePrimary = Color(0xFFD0BCFF)
val md_theme_light_surfaceTint = Color(0xFF6750A4)
val md_theme_light_outlineVariant = Color(0xFFCAC4D0)
val md_theme_light_scrim = Color(0xFF000000)

val md_theme_dark_primary = Color(0xFFD0BCFF)
val md_theme_dark_onPrimary = Color(0xFF381E72)
val md_theme_dark_primaryContainer = Color(0xFF4F378B)
val md_theme_dark_onPrimaryContainer = Color(0xFFEADDFF)
val md_theme_dark_secondary = Color(0xFFCCC2DC)
val md_theme_dark_onSecondary = Color(0xFF332D41)
val md_theme_dark_secondaryContainer = Color(0xFF4A4458)
val md_theme_dark_onSecondaryContainer = Color(0xFFE8DEF8)
val md_theme_dark_tertiary = Color(0xFFEFB8C8)
val md_theme_dark_onTertiary = Color(0xFF492532)
val md_theme_dark_tertiaryContainer = Color(0xFF633B48)
val md_theme_dark_onTertiaryContainer = Color(0xFFFFD8E4)
val md_theme_dark_error = Color(0xFFF2B8B5)
val md_theme_dark_onError = Color(0xFF601410)
val md_theme_dark_errorContainer = Color(0xFF8C1D18)
val md_theme_dark_onErrorContainer = Color(0xFFF9DEDC)
val md_theme_dark_background = Color(0xFF1C1B1F)
val md_theme_dark_onBackground = Color(0xFFE6E1E5)
val md_theme_dark_surface = Color(0xFF1C1B1F)
val md_theme_dark_onSurface = Color(0xFFE6E1E5)
val md_theme_dark_surfaceVariant = Color(0xFF49454F)
val md_theme_dark_onSurfaceVariant = Color(0xFFCAC4D0)
val md_theme_dark_outline = Color(0xFF938F99)
val md_theme_dark_inverseOnSurface = Color(0xFF1C1B1F)
val md_theme_dark_inverseSurface = Color(0xFFE6E1E5)
val md_theme_dark_inversePrimary = Color(0xFF6750A4)
val md_theme_dark_surfaceTint = Color(0xFFD0BCFF)
val md_theme_dark_outlineVariant = Color(0xFF49454F)
val md_theme_dark_scrim = Color(0xFF000000)
```

- [x] **步骤 5：创建 Type.kt**

```kotlin
package com.nltimer.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displayLarge = TextStyle(
        fontSize = 57.sp,
        lineHeight = 64.sp,
        fontWeight = FontWeight.W400,
    ),
    displayMedium = TextStyle(
        fontSize = 45.sp,
        lineHeight = 52.sp,
        fontWeight = FontWeight.W400,
    ),
    displaySmall = TextStyle(
        fontSize = 36.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight.W400,
    ),
    headlineLarge = TextStyle(
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.W400,
    ),
    headlineMedium = TextStyle(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.W400,
    ),
    headlineSmall = TextStyle(
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.W400,
    ),
    titleLarge = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.W400,
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.W500,
    ),
    titleSmall = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.W500,
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.W400,
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.W400,
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.W400,
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.W500,
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.W500,
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.W500,
    ),
)
```

- [x] **步骤 6：创建 Theme.kt**

```kotlin
package com.nltimer.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    errorContainer = md_theme_dark_errorContainer,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
```

- [x] **步骤 7：Commit**

```bash
git add core/
git commit -m "feat: 创建 core:designsystem 模块，实现 MD3 主题系统"
```

***

## 任务 5：创建 feature:timer 模块 — Model 与 Repository（TDD）

**文件：**

- 创建：`feature/timer/build.gradle.kts`
- 创建：`feature/timer/src/main/AndroidManifest.xml`
- 创建：`feature/timer/src/main/java/com/nltimer/feature/timer/model/TimerState.kt`
- 创建：`feature/timer/src/main/java/com/nltimer/feature/timer/repository/TimerRepository.kt`
- 创建：`feature/timer/src/test/java/com/nltimer/feature/timer/repository/TimerRepositoryTest.kt`
- [x] **步骤 1：创建模块目录结构**

```bash
mkdir -p feature/timer/src/main/java/com/nltimer/feature/timer/model
mkdir -p feature/timer/src/main/java/com/nltimer/feature/timer/repository
mkdir -p feature/timer/src/main/java/com/nltimer/feature/timer/viewmodel
mkdir -p feature/timer/src/main/java/com/nltimer/feature/timer/ui
mkdir -p feature/timer/src/test/java/com/nltimer/feature/timer/repository
mkdir -p feature/timer/src/test/java/com/nltimer/feature/timer/viewmodel
```

- [x] **步骤 2：创建 feature/timer/build.gradle.kts**

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    kotlin("android")
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    compileSdk = libs.versions.compile.sdk.version.get().toInt()

    defaultConfig {
        minSdk = libs.versions.min.sdk.version.get().toInt()
        namespace = "com.nltimer.feature.timer"
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

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.hilt.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    debugImplementation(libs.compose.ui.test.manifest)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
}
```

- [x] **步骤 3：创建 feature/timer/src/main/AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" />
```

- [x] **步骤 4：编写 TimerRepository 失败测试**

创建 `feature/timer/src/test/java/com/nltimer/feature/timer/repository/TimerRepositoryTest.kt`：

```kotlin
package com.nltimer.feature.timer.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TimerRepositoryTest {

    private val repository = TimerRepository()

    @Test
    fun timerFlow_emitsIncrementingSeconds() = runTest {
        val results = mutableListOf<Long>()
        repository.timerFlow().collect { value ->
            results.add(value)
            if (value >= 3L) throw kotlinx.coroutines.CancellationException()
        }
        assertEquals(listOf(1L, 2L, 3L), results)
    }
}
```

- [x] **步骤 5：运行测试验证失败**

```bash
./gradlew :feature:timer:test --tests "com.nltimer.feature.timer.repository.TimerRepositoryTest"
```

预期：编译失败，`TimerRepository` 类不存在。

- [x] **步骤 6：创建 TimerState.kt**

```kotlin
package com.nltimer.feature.timer.model

data class TimerState(
    val elapsedSeconds: Long = 0,
    val isRunning: Boolean = false,
)
```

- [x] **步骤 7：创建 TimerRepository.kt**

```kotlin
package com.nltimer.feature.timer.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TimerRepository {

    fun timerFlow(): Flow<Long> = flow {
        var elapsed = 0L
        while (true) {
            delay(1000)
            emit(++elapsed)
        }
    }
}
```

- [x] **步骤 8：运行测试验证通过**

```bash
./gradlew :feature:timer:test --tests "com.nltimer.feature.timer.repository.TimerRepositoryTest"
```

预期：PASS。注意：`kotlinx.coroutines.test` 的 `runTest` 使用虚拟时间，`delay(1000)` 会被跳过。

- [x] **步骤 9：Commit**

```bash
git add feature/
git commit -m "feat: 创建 feature:timer 模块，实现 TimerState 和 TimerRepository（TDD）"
```

***

## 任务 6：创建 feature:timer 模块 — ViewModel（TDD）

**文件：**

- 创建：`feature/timer/src/main/java/com/nltimer/feature/timer/viewmodel/TimerViewModel.kt`
- 创建：`feature/timer/src/test/java/com/nltimer/feature/timer/viewmodel/TimerViewModelTest.kt`
- [x] **步骤 1：编写 TimerViewModel 失败测试**

创建 `feature/timer/src/test/java/com/nltimer/feature/timer/viewmodel/TimerViewModelTest.kt`：

```kotlin
package com.nltimer.feature.timer.viewmodel

import com.nltimer.feature.timer.model.TimerState
import com.nltimer.feature.timer.repository.TimerRepository
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TimerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val repository = TimerRepository()
    private lateinit var viewModel: TimerViewModel

    @Before
    fun setUp() {
        viewModel = TimerViewModel(repository)
    }

    @Test
    fun initialState_isZeroAndNotRunning() {
        val state = viewModel.uiState.value
        assertEquals(TimerState(elapsedSeconds = 0, isRunning = false), state)
    }

    @Test
    fun toggleTimer_startsTimer() = testScope.runTest {
        viewModel.toggleTimer()
        advanceUntilIdle()
        assertEquals(true, viewModel.uiState.value.isRunning)
    }

    @Test
    fun toggleTimer_twice_pausesTimer() = testScope.runTest {
        viewModel.toggleTimer()
        advanceUntilIdle()
        viewModel.toggleTimer()
        advanceUntilIdle()
        assertEquals(false, viewModel.uiState.value.isRunning)
    }

    @Test
    fun resetTimer_resetsToZero() = testScope.runTest {
        viewModel.toggleTimer()
        advanceUntilIdle()
        viewModel.resetTimer()
        advanceUntilIdle()
        assertEquals(TimerState(elapsedSeconds = 0, isRunning = false), viewModel.uiState.value)
    }
}
```

- [x] **步骤 2：运行测试验证失败**

```bash
./gradlew :feature:timer:test --tests "com.nltimer.feature.timer.viewmodel.TimerViewModelTest"
```

预期：编译失败，`TimerViewModel` 类不存在。

- [x] **步骤 3：创建 TimerViewModel.kt**

```kotlin
package com.nltimer.feature.timer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.feature.timer.model.TimerState
import com.nltimer.feature.timer.repository.TimerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val repository: TimerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerState())
    val uiState: StateFlow<TimerState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun toggleTimer() {
        if (_uiState.value.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _uiState.update { TimerState() }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            val startFrom = _uiState.value.elapsedSeconds
            repository.timerFlow()
                .collect { seconds ->
                    _uiState.update {
                        it.copy(elapsedSeconds = startFrom + seconds, isRunning = true)
                    }
                }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }
}
```

- [x] **步骤 4：运行测试验证通过**

```bash
./gradlew :feature:timer:test --tests "com.nltimer.feature.timer.viewmodel.TimerViewModelTest"
```

预期：PASS。

- [x] **步骤 5：Commit**

```bash
git add feature/timer/src/main/java/com/nltimer/feature/timer/viewmodel/
git add feature/timer/src/test/java/com/nltimer/feature/timer/viewmodel/
git commit -m "feat: 实现 TimerViewModel（TDD）"
```

***

## 任务 7：创建 feature:timer 模块 — UI

**文件：**

- 创建：`feature/timer/src/main/java/com/nltimer/feature/timer/ui/TimerScreen.kt`
- [x] **步骤 1：创建 TimerScreen.kt**

```kotlin
package com.nltimer.feature.timer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nltimer.core.designsystem.theme.AppTheme
import com.nltimer.feature.timer.model.TimerState
import com.nltimer.feature.timer.viewmodel.TimerViewModel

@Composable
fun TimerRoute(
    viewModel: TimerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TimerScreen(
        state = state,
        onToggle = viewModel::toggleTimer,
        onReset = viewModel::resetTimer,
    )
}

@Composable
fun TimerScreen(
    state: TimerState,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = formatTime(state.elapsedSeconds),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { contentDescription = "Elapsed time" },
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                FilledTonalButton(
                    onClick = onReset,
                    modifier = Modifier.semantics { contentDescription = "Reset timer" },
                ) {
                    Text("Reset")
                }
                Button(
                    onClick = onToggle,
                    modifier = Modifier.semantics { contentDescription = "Toggle timer" },
                ) {
                    Text(if (state.isRunning) "Pause" else "Start")
                }
            }
        }
    }
}

fun formatTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return "%02d:%02d:%02d".format(h, m, s)
}

@Preview(showBackground = true)
@Composable
private fun TimerScreenPreview() {
    AppTheme {
        var state by remember { mutableStateOf(TimerState(elapsedSeconds = 3661, isRunning = true)) }
        TimerScreen(
            state = state,
            onToggle = {
                state = state.copy(isRunning = !state.isRunning)
            },
            onReset = {
                state = TimerState()
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimerScreenInitialPreview() {
    AppTheme {
        TimerScreen(
            state = TimerState(),
            onToggle = {},
            onReset = {},
        )
    }
}
```

- [x] **步骤 2：Commit**

```bash
git add feature/timer/src/main/java/com/nltimer/feature/timer/ui/
git commit -m "feat: 实现 TimerScreen Compose UI"
```

***

## 任务 8：重写 app 模块

**文件：**

- 修改：`app/build.gradle.kts`
- 修改：`app/src/main/AndroidManifest.xml`
- 修改：`app/src/main/res/values/strings.xml`
- 创建：`app/src/main/java/com/nltimer/app/NLtimerApplication.kt`
- 创建：`app/src/main/java/com/nltimer/app/MainActivity.kt`
- 创建：`app/src/main/java/com/nltimer/app/NLtimerApp.kt`
- 创建：`app/src/main/java/com/nltimer/app/navigation/NLtimerNavHost.kt`
- [x] **步骤 1：重写 app/build.gradle.kts**

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    kotlin("android")
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

val APP_VERSION_NAME: String by project
val APP_VERSION_CODE: String by project
val APP_ID: String by project

android {
    compileSdk = libs.versions.compile.sdk.version.get().toInt()

    defaultConfig {
        minSdk = libs.versions.min.sdk.version.get().toInt()
        namespace = APP_ID

        applicationId = APP_ID
        versionCode = APP_VERSION_CODE.toInt()
        versionName = APP_VERSION_NAME
        targetSdk = libs.versions.target.sdk.version.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
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
    implementation(projects.feature.timer)

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

- [x] **步骤 2：重写 AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application
        android:name=".NLtimerApplication"
        android:allowBackup="true"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.NLtimer"
        tools:ignore="AllowBackup">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- [x] **步骤 3：重写 strings.xml**

```xml
<resources>
    <string name="app_name">NLtimer</string>
</resources>
```

- [x] **步骤 4：创建新包名目录结构**

```bash
mkdir -p app/src/main/java/com/nltimer/app/navigation
```

- [x] **步骤 5：创建 NLtimerApplication.kt**

```kotlin
package com.nltimer.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NLtimerApplication : Application()
```

- [x] **步骤 6：创建 MainActivity.kt**

```kotlin
package com.nltimer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nltimer.core.designsystem.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                NLtimerApp()
            }
        }
    }
}
```

- [x] **步骤 7：创建 NLtimerApp.kt**

```kotlin
package com.nltimer.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.nltimer.app.navigation.NLtimerNavHost

@Composable
fun NLtimerApp() {
    val navController = rememberNavController()
    Scaffold { padding ->
        NLtimerNavHost(
            navController = navController,
            modifier = Modifier.padding(padding),
        )
    }
}
```

- [x] **步骤 8：创建 NLtimerNavHost.kt**

```kotlin
package com.nltimer.app.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nltimer.feature.timer.ui.TimerRoute

@Composable
fun NLtimerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = "timer",
        modifier = modifier,
    ) {
        composable("timer") {
            TimerRoute()
        }
    }
}
```

- [x] **步骤 9：Commit**

```bash
git add app/
git commit -m "feat: 重写 app 模块，切换到 Compose + Hilt + Navigation"
```

***

## 任务 9：清理旧源码和资源

**文件：**

- 删除：`app/src/main/java/com/ncorti/`（旧包名目录）
- 删除：`app/src/main/res/layout/`
- 删除：`app/src/main/res/drawable/`
- 删除：`app/src/main/res/drawable-v24/`
- 删除：`app/src/main/res/mipmap-anydpi-v26/`
- 删除：`app/src/main/res/mipmap-hdpi/`
- 删除：`app/src/main/res/mipmap-mdpi/`
- 删除：`app/src/main/res/mipmap-xhdpi/`
- 删除：`app/src/main/res/mipmap-xxhdpi/`
- 删除：`app/src/main/res/mipmap-xxxhdpi/`
- 删除：`app/src/main/res/values/colors.xml`
- 删除：`app/src/main/res/values/dimens.xml`
- 删除：`app/src/main/res/values/styles.xml`
- 修改：`app/src/main/res/values/strings.xml`（已在任务 8 更新）
- [x] **步骤 1：删除旧包名源码**

```bash
Remove-Item -Recurse -Force app/src/main/java/com/ncorti/
```

- [x] **步骤 2：删除传统布局和模板图标**

```bash
Remove-Item -Recurse -Force app/src/main/res/layout/
Remove-Item -Recurse -Force app/src/main/res/drawable/
Remove-Item -Recurse -Force app/src/main/res/drawable-v24/
Remove-Item -Recurse -Force app/src/main/res/mipmap-anydpi-v26/
Remove-Item -Recurse -Force app/src/main/res/mipmap-hdpi/
Remove-Item -Recurse -Force app/src/main/res/mipmap-mdpi/
Remove-Item -Recurse -Force app/src/main/res/mipmap-xhdpi/
Remove-Item -Recurse -Force app/src/main/res/mipmap-xxhdpi/
Remove-Item -Recurse -Force app/src/main/res/mipmap-xxxhdpi/
```

- [x] **步骤 3：删除旧资源文件**

```bash
Remove-Item -Force app/src/main/res/values/colors.xml
Remove-Item -Force app/src/main/res/values/dimens.xml
Remove-Item -Force app/src/main/res/values/styles.xml
```

- [x] **步骤 4：删除旧测试目录**

```bash
Remove-Item -Recurse -Force app/src/androidTest/
```

- [x] **步骤 5：验证清理结果**

```bash
ls app/src/main/res/values/
ls app/src/main/java/com/nltimer/
```

预期：values/ 中仅有 strings.xml；nltimer/ 下有 app/ 目录含新源码。

- [x] **步骤 6：Commit**

```bash
git add -A
git commit -m "chore: 清理旧包名源码、XML 布局和模板资源"
```

***

## 任务 10：添加 MD3 主题资源并验证构建

**文件：**

- 创建：`app/src/main/res/values/themes.xml`
- [x] **步骤 1：创建 MD3 兼容的主题资源**

创建 `app/src/main/res/values/themes.xml`：

```xml
<resources>
    <style name="Theme.NLtimer" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

- [x] **步骤 2：运行 Gradle 同步和编译**

```bash
./gradlew :core:designsystem:assembleDebug
```

预期：BUILD SUCCESSFUL。

- [x] **步骤 3：编译 feature:timer 模块**

```bash
./gradlew :feature:timer:assembleDebug
```

预期：BUILD SUCCESSFUL。

- [x] **步骤 4：编译 app 模块**

```bash
./gradlew :app:assembleDebug
```

预期：BUILD SUCCESSFUL。

- [x] **步骤 5：运行全部单元测试**

```bash
./gradlew :feature:timer:test
```

预期：所有测试 PASS。

- [x] **步骤 6：Commit**

```bash
git add app/src/main/res/values/themes.xml
git commit -m "feat: 添加 MD3 主题资源，验证构建通过"
```

***

## 任务 11：最终验证与清理

- [x] **步骤 1：运行全项目构建**

```bash
./gradlew assembleDebug
```

预期：BUILD SUCCESSFUL。

- [x] **步骤 2：运行全项目测试**

```bash
./gradlew test
```

预期：所有测试 PASS。

- [x] **步骤 3：检查 Detekt**

```bash
./gradlew detekt
```

预期：无严重问题（可能有少量格式问题需修复）。

- [x] **步骤 4：验证 APK 生成**

```bash
ls app/build/outputs/apk/debug/
```

预期：存在 `app-debug.apk`。

- [x] **步骤 5：最终 Commit**

```bash
git add -A
git commit -m "chore: 最终验证，确保构建和测试全部通过"
```

