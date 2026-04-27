# buildSrc + 惯例插件重构方案

## 背景

NLtimer 项目现有 1 个 app 模块和 7 个 library 模块，各模块的 `build.gradle.kts` 中存在大量重复配置：

- `compileSdk` / `minSdk` / `targetSdk` 版本号
- `compileOptions` Java 17 配置
- `tasks.withType<KotlinCompile>` JVM 目标配置
- `buildFeatures { compose = true }` 及 `buildConfig = false`
- Lint 配置
- `testInstrumentationRunner`
- `compose.compiler` 等公共插件声明

## 目标

通过 `buildSrc` + Gradle 惯例插件（Convention Plugins）消除重复配置，同时保持各模块依赖关系的显式可见性。

## 方案：2+1 惯例插件

### buildSrc 结构

```
buildSrc/
├── build.gradle.kts                   # 声明插件依赖
├── src/main/kotlin/
│   ├── SdkVersions.kt                 # SDK 常量
│   ├── nltimer.android.library.gradle.kts     # Library + Compose 基础插件
│   ├── nltimer.android.application.gradle.kts # Application 插件
│   └── nltimer.android.hilt.gradle.kts        # Hilt + KSP 插件
```

### buildSrc/build.gradle.kts

预编译脚本插件需要使用 `kotlin-dsl` 插件，并声明 AGP、Kotlin、Hilt、KSP 插件为编译依赖（这些依赖来自版本目录）：

```kotlin
plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(libs.plugin.android)
    implementation(libs.plugin.kotlin)
    implementation(libs.plugin.hilt)
    implementation(libs.plugin.ksp)
}
```

需要在 `libs.versions.toml` 中补充以下 `[libraries]` 条目（已有但未作为 library 导出）：

```toml
[versions]
# ... 已有版本定义 ...

[libraries]
# ... 已有库定义 ...
# 以下为 buildSrc 所需的插件坐标
plugin-android = { module = "com.android.tools.build:gradle", version.ref = "agp" }
plugin-kotlin = { module = "org.jetbrains.kotlin:kotlin-gradle-plugin", version.ref = "kotlin" }
plugin-hilt = { module = "com.google.dagger:hilt-android-gradle-plugin", version.ref = "hilt" }
plugin-ksp = { module = "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin", version.ref = "ksp" }
```

### SdkVersions.kt

```kotlin
object SdkVersions {
    const val compileSdk = 36
    const val minSdk = 31
    const val targetSdk = 36
}
```

### 插件一：nltimer.android.library

应用于所有 Android Library 模块（包含 Compose 支持）。

**职责：**

- 应用插件：`com.android.library`、`org.jetbrains.kotlin.android`、`org.jetbrains.kotlin.plugin.compose`
- `android { compileSdk = SdkVersions.compileSdk }`
- `android { defaultConfig { minSdk = SdkVersions.minSdk } }` — library 模块不需要显式设置 `targetSdk`，AGP 会自动处理
- `android { buildFeatures { compose = true; buildConfig = false } }`
- `android { compileOptions { Java 17 配置 } }`
- `android { lint { warningsAsErrors = true; abortOnError = true; disable("GradleDependency") } }` — `lint` 块必须在 `android {}` 内部
- `android { defaultConfig { testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" } }`
- `tasks.withType<KotlinCompile>().configureEach { compilerOptions { jvmTarget = JVM_17 } }` — 此配置在 `android {}` 外部

**不包含**任何 `dependencies {}` 块。每个模块自行管理依赖。

**适用模块：** feature/timer、feature/home、feature/settings、feature/stats、feature/sub、core/designsystem、core/data

### 插件二：nltimer.android.application

应用于 app 模块。

**职责：**

- 应用插件：`com.android.application`、`org.jetbrains.kotlin.android`、`org.jetbrains.kotlin.plugin.compose`
- `android { compileSdk = SdkVersions.compileSdk }`
- `android { defaultConfig { minSdk = SdkVersions.minSdk; targetSdk = SdkVersions.targetSdk } }`
- `android { buildFeatures { compose = true; buildConfig = false } }`
- `android { compileOptions { Java 17 配置 } }`
- `android { lint { warningsAsErrors = false; abortOnError = false; disable("GradleDependency") } }`
- `tasks.withType<KotlinCompile>().configureEach { compilerOptions { jvmTarget = JVM_17 } }`

**不包含**签名配置和 `dependencies {}`。签名配置为 app 模块特有逻辑，保持原位。

### 插件三：nltimer.android.hilt

按需应用到需要 Hilt 注入的模块。

**职责：**

- 应用插件：`com.google.dagger.hilt.android`、`com.google.devtools.ksp`
- 声明空 `ksp {}` 块以备后续扩展

```kotlin
plugins {
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

ksp {
}
```

**不包含**任何 `dependencies {}`。Hilt 的 `hilt-android` 和 `hilt-compiler` 依赖由各模块自行声明。

> KSP 版本兼容性：当前项目使用 Kotlin 2.3.21 + KSP 2.3.6，两者兼容（KSP 2.3.0+ 已独立于 Kotlin 编译器版本）。更新时需同时更新 `kotlin` 和 `ksp` 版本。

## 迁移后各模块示例

### app/build.gradle.kts

```kotlin
plugins {
    id("nltimer.android.application")
    id("nltimer.android.hilt")
}

val APP_VERSION_NAME: String by project
val APP_VERSION_CODE: String by project
val APP_ID: String by project

android {
    namespace = APP_ID
    defaultConfig {
        applicationId = APP_ID
        versionCode = APP_VERSION_CODE.toInt()
        versionName = APP_VERSION_NAME
    }
    buildTypes {
        // 签名配置保持不变
    }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.data)
    implementation(projects.feature.timer)
    implementation(projects.feature.home)
    implementation(projects.feature.sub)
    implementation(projects.feature.stats)
    implementation(projects.feature.settings)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.datastore.preferences)
    implementation(libs.okio)
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

### feature/timer/build.gradle.kts

```kotlin
plugins {
    id("nltimer.android.library")
    id("nltimer.android.hilt")
}

android {
    namespace = "com.nltimer.feature.timer"
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

### core/designsystem/build.gradle.kts

```kotlin
plugins {
    id("nltimer.android.library")
}

android {
    namespace = "com.nltimer.core.designsystem"
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.materialkolor)
}
```

### feature/stats/build.gradle.kts

```kotlin
plugins {
    id("nltimer.android.library")
}

android {
    namespace = "com.nltimer.feature.stats"
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
}
```

## 未覆盖的部分

以下配置模块特有，保留在原模块中：

- `namespace` — 每个模块不同
- `applicationId` / `versionCode` / `versionName` — app 模块特有
- 签名配置 — app 模块特有
- `buildTypes` — app 模块特有
- 所有 `dependencies {}` — 每个模块自行管理

## 备注

- **Lint 配置**：library 插件设 `warningsAsErrors = true`，application 插件设 `warningsAsErrors = false`。迁移前当前所有 library 模块已全部为 `true`，直接迁移不会引入构建失败。
- **buildConfig**：app 模块和所有 library 模块均设 `buildConfig = false`，因为版本信息来自 `gradle.properties` 的 `by project` 扩展，不依赖 BuildConfig。
- **`targetSdk` 在 library 模块中**：library 插件不设 `targetSdk`。AGP 对 library 的 `targetSdk` 已弃用，显式设置会产生弃用警告。不设置时按 AGP 默认行为处理，迁移后行为不变。
- **KSP 版本**：当前 Kotlin 2.3.21 + KSP 2.3.6 兼容。KSP 2.3.0+ 已独立于 Kotlin 编译器版本，但更新时仍需比对官方兼容性表。
