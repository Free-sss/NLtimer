# buildSrc + 惯例插件 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 创建 `buildSrc/` 目录 + 3 个 Gradle 惯例插件，消除 7 个 library 模块和 1 个 app 模块的重复 Gradle 配置。

**架构：** 3 个预编译脚本惯例插件（`nltimer.android.library`、`nltimer.android.application`、`nltimer.android.hilt`），通过 `buildSrc` 分发，各模块按需组合使用。

**技术栈：** Kotlin DSL、Gradle 预编译脚本插件、AGP 8.13.2、Kotlin 2.3.21、KSP 2.3.6

---

### 任务 1：创建 buildSrc 目录结构和 SdkVersions 常量

**文件：**
- 创建：`buildSrc/build.gradle.kts`
- 创建：`buildSrc/src/main/kotlin/SdkVersions.kt`

- [ ] **步骤 1：创建目录结构**

运行：
```powershell
mkdir -p buildSrc\src\main\kotlin
```

- [ ] **步骤 2：创建 SdkVersions.kt**

```kotlin
object SdkVersions {
    const val compileSdk = 36
    const val minSdk = 31
    const val targetSdk = 36
}
```

- [ ] **步骤 3：创建 buildSrc/build.gradle.kts**

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

- [ ] **步骤 4：Commit**

```bash
git add buildSrc/
git commit -m "chore(buildSrc): add buildSrc with SdkVersions constants"
```

---

### 任务 2：补充版本目录的插件坐标

**文件：**
- 修改：`gradle/libs.versions.toml`

- [ ] **步骤 1：在 libs.versions.toml 的 `[libraries]` 中添加插件坐标**

在当前 `[libraries]` 块的末尾（保持 `[plugins]` 不变）添加：

```toml
# Plugin coordinates for buildSrc
plugin-android = { module = "com.android.tools.build:gradle", version.ref = "agp" }
plugin-kotlin = { module = "org.jetbrains.kotlin:kotlin-gradle-plugin", version.ref = "kotlin" }
plugin-hilt = { module = "com.google.dagger:hilt-android-gradle-plugin", version.ref = "hilt" }
plugin-ksp = { module = "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin", version.ref = "ksp" }
```

- [ ] **步骤 2：Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "chore(deps): add plugin Maven coordinates for buildSrc"
```

---

### 任务 3：创建 nltimer.android.library 惯例插件

**文件：**
- 创建：`buildSrc/src/main/kotlin/nltimer.android.library.gradle.kts`

- [ ] **步骤 1：创建 library 惯例插件**

```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    compileSdk = SdkVersions.compileSdk

    defaultConfig {
        minSdk = SdkVersions.minSdk
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
        disable("GradleDependency")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
```

- [ ] **步骤 2：Commit**

```bash
git add buildSrc/src/main/kotlin/nltimer.android.library.gradle.kts
git commit -m "chore(buildSrc): add nltimer.android.library convention plugin"
```

---

### 任务 4：创建 nltimer.android.application 惯例插件

**文件：**
- 创建：`buildSrc/src/main/kotlin/nltimer.android.application.gradle.kts`

- [ ] **步骤 1：创建 application 惯例插件**

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    compileSdk = SdkVersions.compileSdk

    defaultConfig {
        minSdk = SdkVersions.minSdk
        targetSdk = SdkVersions.targetSdk
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
        warningsAsErrors = false
        abortOnError = false
        disable("GradleDependency")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
```

- [ ] **步骤 2：Commit**

```bash
git add buildSrc/src/main/kotlin/nltimer.android.application.gradle.kts
git commit -m "chore(buildSrc): add nltimer.android.application convention plugin"
```

---

### 任务 5：创建 nltimer.android.hilt 惯例插件

**文件：**
- 创建：`buildSrc/src/main/kotlin/nltimer.android.hilt.gradle.kts`

- [ ] **步骤 1：创建 Hilt 惯例插件**

```kotlin
plugins {
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

ksp {
}
```

- [ ] **步骤 2：Commit**

```bash
git add buildSrc/src/main/kotlin/nltimer.android.hilt.gradle.kts
git commit -m "chore(buildSrc): add nltimer.android.hilt convention plugin"
```

---

### 任务 6：迁移 app 模块

**文件：**
- 修改：`app/build.gradle.kts`

- [ ] **步骤 1：替换 app/build.gradle.kts**

移除 `import` 语句和 `plugins` 块中的独立插件声明，改用惯例插件。保留所有 `dependencies {}` 和签名配置等模块特有逻辑。

应用：`nltimer.android.application` + `nltimer.android.hilt`

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
        val localProperties = com.android.build.gradle.internal.cxx.configure.gradleLocalProperties(rootDir, providers)
        val releaseKeystorePath = localProperties.getProperty("keystore.path", System.getenv("KEYSTORE_PATH") ?: "")
        val releaseKeystorePassword = localProperties.getProperty("keystore.password", System.getenv("KEYSTORE_PASSWORD") ?: "")
        val releaseKeyAlias = localProperties.getProperty("key.alias", System.getenv("KEY_ALIAS") ?: "")
        val releaseKeyPassword = localProperties.getProperty("key.password", System.getenv("KEY_PASSWORD") ?: "")

        getByName("release") {
            isMinifyEnabled = false
            signingConfig = if (releaseKeystorePath.isNotBlank()) {
                signingConfigs.create("release") {
                    storeFile = file(releaseKeystorePath)
                    storePassword = releaseKeystorePassword
                    keyAlias = releaseKeyAlias
                    keyPassword = releaseKeyPassword
                }
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
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

- [ ] **步骤 2：Commit**

```bash
git add app/build.gradle.kts
git commit -m "refactor(app): migrate to nltimer convention plugins"
```

---

### 任务 7：迁移 Hilt + Compose 模块（feature/timer, feature/home, feature/settings, core/data）

**文件：**
- 修改：`feature/timer/build.gradle.kts`
- 修改：`feature/home/build.gradle.kts`
- 修改：`feature/settings/build.gradle.kts`
- 修改：`core/data/build.gradle.kts`

这 4 个模块目前使用 `library + kotlin + compose.compiler + hilt + ksp`，迁移后统一使用 `nltimer.android.library` + `nltimer.android.hilt`。

- [ ] **步骤 1：迁移 feature/timer/build.gradle.kts**

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

- [ ] **步骤 2：迁移 feature/home/build.gradle.kts**

```kotlin
plugins {
    id("nltimer.android.library")
    id("nltimer.android.hilt")
}

android {
    namespace = "com.nltimer.feature.home"
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.data)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
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
}
```

- [ ] **步骤 3：迁移 feature/settings/build.gradle.kts**

```kotlin
plugins {
    id("nltimer.android.library")
    id("nltimer.android.hilt")
}

android {
    namespace = "com.nltimer.feature.settings"
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.data)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    implementation(libs.materialkolor)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.compose.ui.tooling)
}
```

- [ ] **步骤 4：迁移 core/data/build.gradle.kts**

```kotlin
plugins {
    id("nltimer.android.library")
    id("nltimer.android.hilt")
}

android {
    namespace = "com.nltimer.core.data"
}

dependencies {
    implementation(libs.androidx.core.ktx)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.datastore.preferences)
    implementation(libs.okio)
    implementation(libs.compose.ui)

    implementation(projects.core.designsystem)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

- [ ] **步骤 5：Commit**

```bash
git add feature/timer/build.gradle.kts feature/home/build.gradle.kts feature/settings/build.gradle.kts core/data/build.gradle.kts
git commit -m "refactor: migrate Hilt+Compose modules to nltimer convention plugins"
```

---

### 任务 8：迁移纯 Compose 模块（feature/stats, feature/sub, core/designsystem）

**文件：**
- 修改：`feature/stats/build.gradle.kts`
- 修改：`feature/sub/build.gradle.kts`
- 修改：`core/designsystem/build.gradle.kts`

这 3 个模块目前使用 `library + kotlin + compose.compiler`（无 Hilt），迁移后只使用 `nltimer.android.library`。

- [ ] **步骤 1：迁移 feature/stats/build.gradle.kts**

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

- [ ] **步骤 2：迁移 feature/sub/build.gradle.kts**

```kotlin
plugins {
    id("nltimer.android.library")
}

android {
    namespace = "com.nltimer.feature.sub"
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

- [ ] **步骤 3：迁移 core/designsystem/build.gradle.kts**

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

- [ ] **步骤 4：Commit**

```bash
git add feature/stats/build.gradle.kts feature/sub/build.gradle.kts core/designsystem/build.gradle.kts
git commit -m "refactor: migrate Compose-only modules to nltimer.android.library plugin"
```

---

### 任务 9：构建验证

- [ ] **步骤 1：运行首次构建**

运行（在项目根目录）：
```powershell
./gradlew assembleDebug
```
预期：BUILD SUCCESSFUL

- [ ] **步骤 2：运行测试**

```powershell
./gradlew testDebugUnitTest
```
预期：所有测试通过

- [ ] **步骤 3：如果构建失败，排查原因**

常见问题：
- `buildSrc` 依赖版本不匹配 → 检查 KSP/Kotlin 版本兼容性
- 已删除但模块仍引用的配置 → 确认 `import` 语句已移除
- 惯例插件中漏配了某个模块需要的设置 → 补充

- [ ] **步骤 4：最终 commit**

```bash
git add -A
git commit -m "chore: finalize buildSrc convention plugin migration"
```
