# NLtimer 主题系统架构设计规格

> 状态：已批准 | 日期：2026-04-27 | 参考：Momentum 应用主题系统

## 1. 概述

基于 Momentum 应用的 `DynamicMaterialTheme` 主题架构，为 NLtimer 构建可配置、可切换、可持久化的完整主题系统。主题通过 Compose `CompositionLocal` 机制自动分发到所有子组件，无需 ViewModel 中转。

### 1.1 目标

1. 全面分析并采纳 Momentum 的视觉设计元素
2. 将硬编码主题重构为基于配置的主题切换架构
3. 建立集中管理的主题配置文件
4. 支持主题的灵活扩展和运行时切换

### 1.2 关键约束

- DI 框架：保持 Hilt（不切换到 Koin）
- 导航：保持 Navigation Compose（不切换到 Navigation3）
- 字体：仅 Figtree + System Default（Google Sans 因许可问题排除）
- PaletteStyle：9 种全保留

---

## 2. 模块分层

```
app 模块
  ├── 职责：DI 组装（Hilt）、setContent 入口、主题设置 UI
  ├── depends on: core:designsystem, core:data
  │
core:designsystem 模块
  ├── 职责：Theme 数据模型、枚举、NLtimerTheme、组件样式
  ├── 被所有模块依赖
  │
core:data 模块
  ├── 职责：SettingsPrefs 接口 + DataStore 实现
  └── depends on: core:designsystem
```

---

## 3. 数据模型

### 3.1 Theme 数据类

```kotlin
// core:designsystem/Theme.kt
data class Theme(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val isAmoled: Boolean = false,
    val paletteStyle: PaletteStyle = PaletteStyle.TONALSPOT,
    val isMaterialYou: Boolean = false,
    val seedColor: Color = Color(0xFF1565C0),
    val font: Fonts = Fonts.FIGTREE,
)
```

### 3.2 枚举定义

```kotlin
// AppTheme.kt
enum class AppTheme { LIGHT, DARK, SYSTEM }

// Fonts.kt — 仅 2 种（已移除 Google Sans）
enum class Fonts { FIGTREE, SYSTEM_DEFAULT }

// PaletteStyle.kt — 9 种全保留
enum class PaletteStyle {
    TONALSPOT, NEUTRAL, VIBRANT, EXPRESSIVE,
    RAINBOW, FRUITSALAD, MONOCHROME, FIDELITY, CONTENT,
}
```

---

## 4. 渲染引擎

### 4.1 NLtimerTheme

```kotlin
@Composable
fun NLtimerTheme(theme: Theme = Theme(), content: @Composable () -> Unit) {
    val isDark = when (theme.appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    val typography = provideTypography(font = theme.font.toFontRes())

    DynamicMaterialTheme(
        seedColor = theme.seedColor,
        isDark = isDark,
        isAmoled = theme.isAmoled,
        style = theme.paletteStyle.toMPaletteStyle(),
        typography = typography,
    ) {
        AnimatedContent(
            targetState = theme.appTheme to theme.isAmoled,
            transitionSpec = { fadeIn(tween(300)) + fadeOut(tween(300)) },
        ) {
            content()
        }
    }
}
```

- 不移除 `animateColorScheme`，也不手动调用 `rememberDynamicColorScheme`
- 所有参数直接传入 `DynamicMaterialTheme`，由其内部生成 ColorScheme
- `AnimatedContent` 仅在 Light↔Dark 或 Amoled 切换时触发淡入淡出
- 字体/PaletteStyle/seedColor 变化不触发全屏过渡

### 4.2 Typography

```kotlin
// 普通函数，非 @Composable
fun provideTypography(font: Int? = null): Typography {
    val selectedFont = font?.let { FontFamily(Font(it)) } ?: FontFamily.Default

    return Typography(
        displayLarge  = TYPOGRAPHY.displayLarge.copy(fontFamily = selectedFont),
        headlineLarge = TYPOGRAPHY.headlineLarge.copy(fontFamily = selectedFont),
        titleLarge    = TYPOGRAPHY.titleLarge.copy(fontFamily = selectedFont),
        titleMedium   = TYPOGRAPHY.titleMedium.copy(fontFamily = selectedFont),
        titleSmall    = TYPOGRAPHY.titleSmall.copy(fontFamily = selectedFont),
        bodyLarge     = TYPOGRAPHY.bodyLarge.copy(fontFamily = selectedFont),
        bodyMedium    = TYPOGRAPHY.bodyMedium.copy(fontFamily = selectedFont),
        bodySmall     = TYPOGRAPHY.bodySmall.copy(fontFamily = selectedFont),
        labelLarge    = TYPOGRAPHY.labelLarge.copy(fontFamily = selectedFont),
        labelMedium   = TYPOGRAPHY.labelMedium.copy(fontFamily = selectedFont),
        labelSmall    = TYPOGRAPHY.labelSmall.copy(fontFamily = selectedFont),
        displayMedium = TYPOGRAPHY.displayMedium.copy(fontFamily = selectedFont),
        displaySmall  = TYPOGRAPHY.displaySmall.copy(fontFamily = selectedFont),
        headlineMedium = TYPOGRAPHY.headlineMedium.copy(fontFamily = selectedFont),
        headlineSmall = TYPOGRAPHY.headlineSmall.copy(fontFamily = selectedFont),
    )
}

private val TYPOGRAPHY = Typography()
```

### 4.3 枚举映射扩展

```kotlin
fun Fonts.toFontRes(): Int? = when (this) {
    Fonts.FIGTREE -> R.font.figtree
    Fonts.SYSTEM_DEFAULT -> null
}

fun Fonts.toDisplayString(): String = when (this) {
    Fonts.FIGTREE -> "Figtree"
    Fonts.SYSTEM_DEFAULT -> "System Default"
}

fun PaletteStyle.toMPaletteStyle(): com.materialkolor.PaletteStyle = when (this) {
    PaletteStyle.TONALSPOT   -> com.materialkolor.PaletteStyle.TonalSpot
    PaletteStyle.NEUTRAL     -> com.materialkolor.PaletteStyle.Neutral
    PaletteStyle.VIBRANT     -> com.materialkolor.PaletteStyle.Vibrant
    PaletteStyle.EXPRESSIVE  -> com.materialkolor.PaletteStyle.Expressive
    PaletteStyle.RAINBOW     -> com.materialkolor.PaletteStyle.Rainbow
    PaletteStyle.FRUITSALAD  -> com.materialkolor.PaletteStyle.FruitSalad
    PaletteStyle.MONOCHROME  -> com.materialkolor.PaletteStyle.Monochrome
    PaletteStyle.FIDELITY    -> com.materialkolor.PaletteStyle.Fidelity
    PaletteStyle.CONTENT     -> com.materialkolor.PaletteStyle.Content
}
```

### 4.4 ListItem 曲面连接

| 函数 | 形状 |
|------|------|
| `leadingItemShape()` | 顶角 16dp，底角 4dp |
| `middleItemShape()` | 四角 4dp |
| `endItemShape()` | 顶角 4dp，底角 16dp |
| `detachedItemShape()` | 四角 16dp |
| `listItemColors()` | containerColor = surfaceContainerHigh |

---

## 5. 持久化层

### 5.1 SettingsPrefs 接口

```kotlin
// core:data/SettingsPrefs.kt
interface SettingsPrefs {
    fun getThemeFlow(): Flow<Theme>
    suspend fun updateTheme(theme: Theme)
}
```

- 仅 2 个方法，所有 DataStore 键名和序列化逻辑全部封装在 `SettingsPrefsImpl` 内部
- 禁止外部模块直接依赖 PreferencesKeys

### 5.2 实现要点

| 关注点 | 实现 |
|--------|------|
| Preferences Keys | `seedColor(int)` + `appTheme(string)` + `isAmoled(bool)` + `paletteStyle(string)` + `isMaterialYou(bool)` + `font(string)` — 全部 private |
| getThemeFlow() | `combine(6 个 Flow) { ... -> Theme(...) }` |
| updateTheme() | `dataStore.edit { 逐个写入 6 个字段 }` |
| 异常处理 | PaletteStyle 反序列化 catch → fallback TONALSPOT |

### 5.3 Hilt DI 绑定

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.createWithPath {
            context.filesDir.resolve("settings.preferences_pb").absolutePath.toPath()
        }

    @Provides @Singleton
    fun provideSettingsPrefs(dataStore: DataStore<Preferences>): SettingsPrefs =
        SettingsPrefsImpl(dataStore)
}
```

---

## 6. App 层接入

### 6.1 MainActivity

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var settingsPrefs: SettingsPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val theme by settingsPrefs.getThemeFlow()
                .collectAsStateWithLifecycle(initialValue = Theme())

            NLtimerTheme(theme = theme) {
                NLtimerApp()
            }
        }
    }
}
```

- 直接在 `setContent` 层 collect Flow，不经任何 ViewModel 中转
- Theme 通过 Compose `CompositionLocal` 自动分发到所有子组件

### 6.2 现有代码重构

| 文件 | 原代码 | 重构方式 |
|------|--------|---------|
| themes.xml | 空壳 Theme.NLtimer | 保持 XML 壳，主题由 Compose 运行时接管 |
| AppTopAppBar.kt | containerColor = Color.Transparent | 改为 MaterialTheme.colorScheme.surfaceContainer |
| AppDrawer.kt | 已使用 MaterialTheme.typography | 无需修改 |
| NLtimerScaffold.kt | 无问题 | 无需修改，自动继承 MaterialTheme |
| NLtimerApp.kt | 无主题注入 | 无需修改，自动继承 MaterialTheme |

---

## 7. Gradle 依赖

### core:designsystem/build.gradle.kts

```
add: materialkolor
```

### core:data/build.gradle.kts

```
add: datastore-preferences, okio
add: implementation(projects.core.designsystem)
```

---

## 8. 数据流

```
写入路径（设置页触发）：
SettingsScreen → SettingsAction → SettingsViewModel
  → settingsPrefs.updateTheme(newTheme) → DataStore

读取路径（Compose 声明式）：
DataStore → getThemeFlow(): Flow<Theme>
  → MainActivity.setContent 层 collectAsStateWithLifecycle
  → NLtimerTheme(theme)
  → CompositionLocal 自动分发到所有子组件

子组件获取主题：
MaterialTheme.colorScheme / MaterialTheme.typography
无需 ViewModel，无需参数传递
```

---

## 9. 文件清单

### 新增文件（11 个）

| # | 文件 | 模块 |
|---|------|------|
| 1 | Theme.kt | core:designsystem |
| 2 | Fonts.kt | core:designsystem |
| 3 | AppTheme.kt | core:designsystem |
| 4 | PaletteStyle.kt | core:designsystem |
| 5 | NLtimerTheme.kt | core:designsystem |
| 6 | provideTypography.kt | core:designsystem |
| 7 | listItemExt.kt | core:designsystem |
| 8 | enumExt.kt | core:designsystem |
| 9 | SettingsPrefs.kt | core:data |
| 10 | SettingsPrefsImpl.kt | core:data |
| 11 | DataModule.kt | app |

### 修改文件（5 个）

| # | 文件 | 模块 |
|---|------|------|
| 1 | build.gradle.kts | core:designsystem |
| 2 | build.gradle.kts | core:data |
| 3 | MainActivity.kt | app |
| 4 | themes.xml | app |
| 5 | AppTopAppBar.kt | app |

### 字体资源

| 文件 | 路径 | 来源 |
|------|------|------|
| figtree.ttf | core:designsystem/src/main/res/font/ | 从 Momentum 复制（需确认 OFL 许可） |

---

## 10. 不做（本期范围外）

- 主题设置页面 UI（LookAndFeel 等）— 后续单独规划
- zigZagBackground、MomentumDialog 等装饰组件 — 按需引入
- Google Sans Flex 可变字体 — 许可问题，不引入

---

## 11. 设计决策记录

| 决策 | 选项 | 结论 |
|------|------|------|
| 复刻度 | A: 完整复刻 / B: 架构借鉴+差异化 / C: 最小化 | A |
| DI 框架 | A: 保持 Hilt / B: 切换到 Koin | A |
| 字体 | 用户指定 | Figtree + System Default |
| 色彩引擎 | A: MaterialKolor / B: 自建 | A |
| 模块结构 | A: designsystem / B: app 内 / C: data+UI 分离 | A |
| 主题分发 | 用户反馈修正 | setContent 层 collect Flow，不经 ViewModel |
| 接口粒度 | 用户反馈修正 | 2 方法：getThemeFlow + updateTheme |
| 动画策略 | 用户反馈修正 | AnimatedContent，仅 appTheme/isAmoled 变化触发 |
| Google Sans | 用户反馈移除 | 许可问题，不引入 |
