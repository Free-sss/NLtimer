# NLtimer 架构设计规格

## 概述

将 NLtimer 项目从 `kotlin-android-template` 模板状态全面重构为基于 **Material Design 3 (MD3)**、**Clean Architecture** 以及 **MVVM** 模式的 Android 计时器应用。

## 决策记录

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 实现方案 | 方案 A：全面重构 | 项目为纯模板状态，无业务逻辑需保留，重构成本最低 |
| 应用包名 | `com.nltimer.app` | 与 GitHub 仓库 NLtimer 对应，简洁明了 |
| 计时器范围 | 简单计时器 | 先建立架构基础，后续可扩展 |
| DI 框架 | Hilt + KSP | Android 官方推荐，KSP 比 kapt 编译更快 |
| UI 框架 | Jetpack Compose + MD3 | 现代声明式 UI，与 MD3 天然集成 |

## 模块结构

```
NLtimer/
├── app/                                    # :app 模块
│   └── src/main/java/com/nltimer/app/
│       ├── NLtimerApplication.kt           # Hilt Application 入口
│       ├── MainActivity.kt                 # 单 Activity，Compose 入口
│       ├── navigation/
│       │   └── NLtimerNavHost.kt           # Navigation Graph
│       └── NLtimerApp.kt                   # 顶层 Composable (Scaffold + NavHost)
│
├── core/
│   └── designsystem/                       # :core:designsystem 模块
│       └── src/main/java/com/nltimer/core/designsystem/
│           ├── theme/
│           │   ├── Theme.kt                # AppTheme composable
│           │   ├── Color.kt                # MD3 配色定义
│           │   └── Type.kt                 # MD3 字体定义
│           └── component/                  # 通用 UI 组件（预留）
│
├── feature/
│   └── timer/                              # :feature:timer 模块
│       └── src/main/java/com/nltimer/feature/timer/
│           ├── model/
│           │   └── TimerState.kt           # 数据模型
│           ├── repository/
│           │   └── TimerRepository.kt      # 计时逻辑 (Flow)
│           ├── viewmodel/
│           │   └── TimerViewModel.kt       # ViewModel + StateFlow
│           └── ui/
│               └── TimerScreen.kt          # Compose UI
│
├── gradle/
│   └── libs.versions.toml                  # 统一版本管理
├── build.gradle.kts                        # 根级构建
└── settings.gradle.kts                     # 模块注册
```

### 模块依赖关系

```
:app → :core:designsystem
:app → :feature:timer
:feature:timer → :core:designsystem
```

## 依赖配置

### 新增依赖

| 类别 | 依赖 | 版本 | 用途 |
|------|------|------|------|
| Compose | `compose-material3` | BOM 管理 | MD3 组件库 |
| Compose | `compose-ui-tooling-preview` | BOM 管理 | 预览支持 |
| Navigation | `navigation-compose` | 2.9.0 | Compose 导航 |
| Hilt | `hilt-android` | 2.56.1 | 依赖注入 |
| Hilt | `hilt-navigation-compose` | 1.2.0 | Compose + Hilt 集成 |
| Hilt | `hilt-compiler` (ksp) | 2.56.1 | 注解处理器 |
| Lifecycle | `lifecycle-viewmodel-compose` | 2.9.0 | ViewModel Compose 集成 |
| Lifecycle | `lifecycle-runtime-compose` | 2.9.0 | Runtime Compose 集成 |
| KSP | `ksp` plugin | 2.3.21-1.0.29 | 替代 kapt |

### 移除依赖

- `constraint-layout` — 不再使用传统布局
- `appcompat` — 改用 `activity-compose` + MD3 主题
- `espresso-core` — 改用 Compose UI 测试
- `nexus-publish` 插件 — 模板专用

### 新增插件

- `com.google.devtools.ksp` — Hilt 注解处理
- `com.google.dagger.hilt.android` — Hilt Gradle 插件

## MD3 主题系统（core/designsystem）

### Color.kt

使用 MD3 默认配色作为起点：

- Light Primary: `Color(0xFF6750A4)` 紫色
- Light Secondary: `Color(0xFF625B71)`
- Light Tertiary: `Color(0xFF7D5260)`
- Light Background: `Color(0xFFFFFBFE)`
- Light Surface: `Color(0xFFFFFBFE)`
- Light Error: `Color(0xFFB3261E)`
- Dark Primary: `Color(0xFFD0BCFF)`
- Dark Background: `Color(0xFF1C1B1F)`
- Dark Error: `Color(0xFFF2B8B5)`

### Type.kt

严格遵循 MD3 字体规范：

- `displayLarge`: 57sp / 64sp / W400
- `displayMedium`: 45sp / 52sp / W400
- `headlineLarge`: 32sp / 40sp / W400
- `titleLarge`: 22sp / 28sp / W400
- `bodyLarge`: 16sp / 24sp / W400
- `bodyMedium`: 14sp / 20sp / W400
- `labelLarge`: 14sp / 20sp / W500

### Theme.kt

```kotlin
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkColorScheme(...) else lightColorScheme(...)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

支持深色模式，所有 UI 组件强制使用 `MaterialTheme.colorScheme` 和 `MaterialTheme.typography`。

## 计时器功能（feature/timer）

### Model 层

```kotlin
data class TimerState(
    val elapsedSeconds: Long = 0,
    val isRunning: Boolean = false
)
```

### Repository 层

```kotlin
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

- 使用 `Flow<Long>` 每秒发射递增秒数
- 业务逻辑与 UI 完全解耦
- 后续如需持久化（Room），只需修改 Repository 实现

### ViewModel 层

```kotlin
@HiltViewModel
class TimerViewModel @Inject constructor(
    private val repository: TimerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerState())
    val uiState: StateFlow<TimerState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun toggleTimer() { /* 开始/暂停切换 */ }
    fun resetTimer() { /* 重置到初始状态 */ }
    private fun startTimer() { /* viewModelScope.launch + repository.timerFlow() */ }
    private fun pauseTimer() { /* timerJob?.cancel() */ }
}
```

- `StateFlow` 暴露不可变状态
- `toggleTimer()` 统一处理开始/暂停
- 协程 Job 管理确保暂停时正确取消

### View 层

```kotlin
@Composable
fun TimerRoute(viewModel: TimerViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TimerScreen(state = state, onToggle = viewModel::toggleTimer, onReset = viewModel::resetTimer)
}

@Composable
fun TimerScreen(state: TimerState, onToggle: () -> Unit, onReset: () -> Unit) {
    Scaffold { padding ->
        Column(/* 居中布局 */) {
            Text(text = formatTime(state.elapsedSeconds), style = MaterialTheme.typography.displayLarge)
            Row {
                FilledTonalButton(onClick = onReset) { Text("Reset") }
                Button(onClick = onToggle) { Text(if (state.isRunning) "Pause" else "Start") }
            }
        }
    }
}

fun formatTime(seconds: Long): String = "%02d:%02d:%02d".format(h, m, s)
```

- `TimerRoute` 路由入口，连接 ViewModel
- `TimerScreen` 无状态 Composable，便于预览和测试
- MD3 组件：`Button`（FilledButton）、`FilledTonalButton`

## App 模块

### NLtimerApplication.kt

```kotlin
@HiltAndroidApp
class NLtimerApplication : Application()
```

### MainActivity.kt

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppTheme { NLtimerApp() } }
    }
}
```

### NLtimerApp.kt

```kotlin
@Composable
fun NLtimerApp() {
    val navController = rememberNavController()
    Scaffold { padding ->
        NLtimerNavHost(navController = navController, modifier = Modifier.padding(padding))
    }
}
```

### NLtimerNavHost.kt

```kotlin
@Composable
fun NLtimerNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = "timer", modifier = modifier) {
        composable("timer") { TimerRoute() }
    }
}
```

- 单 Activity 架构，Compose Navigation 管理页面
- 当前仅 `timer` 一个目的地，后续扩展只需添加新路由

## 清理计划

| 删除项 | 原因 |
|--------|------|
| `library-android/` | 模板模块，ToastUtil 不再需要 |
| `library-compose/` | 模板模块，Factorial Compose 不再需要 |
| `library-kotlin/` | 模板模块，FactorialCalculator 不再需要 |
| `buildSrc/` 整个目录 | 模板专用脚本（cleanup、publish），删除后根级 build.gradle.kts 需移除 `cleanup`、`base`、`nexus-publish` 插件引用 |
| `app/src/main/res/layout/` | 传统 XML 布局，改用 Compose |
| `app/src/main/res/drawable*/` | 模板 ic_launcher 占位图标 |
| `app/src/main/res/mipmap*/` | 模板 ic_launcher 占位图标 |
| `colors.xml`, `dimens.xml`, `styles.xml` | MD3 主题替代 |
| `.github/workflows/cleanup.yaml` | 模板清理 CI |
| `.github/template-cleanup/` | 模板清理目录 |
| `renovate.json` | 模板依赖更新配置 |
| `TROUBLESHOOTING.md` | 模板文档 |

### 配置文件更新

| 文件 | 变更 |
|------|------|
| `gradle.properties` | `APP_ID=com.nltimer.app`，删除 `GROUP` 和 `VERSION`（模板发布用），保留 `APP_VERSION_NAME` 和 `APP_VERSION_CODE` |
| `settings.gradle.kts` | `rootProject.name = "NLtimer"`，模块列表改为 `include("app", "core:designsystem", "feature:timer")` |
| 根级 `build.gradle.kts` | 移除 `cleanup`、`base`、`nexus-publish` 插件及 `allprojects` 块，新增 Hilt 和 KSP 插件声明 |
| `app/build.gradle.kts` | 移除 ViewBinding，新增 Compose、Hilt、Navigation 依赖，包名改为 `com.nltimer.app` |

## 开发要点

1. **响应式布局：** 使用 `Scaffold` 作为页面骨架
2. **无障碍支持：** 为所有交互元素添加 `contentDescription`
3. **Clean Architecture：** ViewModel 通过 `StateFlow` 暴露不可变数据，业务逻辑与 Compose 组件解耦
4. **MD3 视觉一致性：** 强制使用 `MaterialTheme.colorScheme` 和 `MaterialTheme.typography`，禁止硬编码颜色值或字体大小
5. **测试：** ViewModel 和 Repository 可独立单元测试，TimerScreen 为无状态 Composable 便于 UI 测试
