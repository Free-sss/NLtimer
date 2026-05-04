# 技术栈

## 版本总览

| 技术 | 版本 |
|------|------|
| AGP | 9.2.0 |
| Kotlin | 2.3.21 |
| KSP | 2.3.6 |
| Compose BOM | 2025.12.01 |
| Material3 | 随 BOM |
| Room | 2.7.1 |
| Hilt | 2.59.2 |
| Navigation Compose | 2.9.0 |
| Lifecycle | 2.9.0 |
| MaterialKolor | 4.1.1 |
| DataStore Preferences | 1.2.1 |
| Detekt | 1.23.8 |
| compileSdk / targetSdk | 36 |
| minSdk | 31 |

## 关键依赖说明

| 依赖 | 用途 |
|------|------|
| `com.materialkolor:material-kolor` | 基于种子色动态生成 Material3 调色板 |
| `com.github.skydoves:colorpicker-compose` | 颜色选择器组件 |
| `com.squareup.okio:okio` | 文件/IO 操作 |
| `androidx.profileinstaller` | Baseline Profile 安装 |

## 构建约定（buildSrc Convention Plugins）

| 插件 ID | 文件 | 作用 |
|---------|------|------|
| `nltimer.android.application` | nltimer.android.application.gradle.kts | Application 模块公共配置 |
| `nltimer.android.library` | nltimer.android.library.gradle.kts | Library 模块公共配置 |
| `nltimer.android.hilt` | nltimer.android.hilt.gradle.kts | Hilt + KSP 注入配置 |

所有 feature 模块使用 `nltimer.android.library` + `nltimer.android.hilt`。
app 模块使用 `nltimer.android.application` + `nltimer.android.hilt`。

## 构建变体

| 变体 | 说明 |
|------|------|
| debug | 可调试，包含 debug 模块（`debugImplementation`） |
| release | 开启 minify + shrinkResources，签名从 local.properties 读取 |

## 静态分析

- Detekt + ktlint（detekt-formatting 插件）
- 配置文件：`config/detekt/detekt.yml`
