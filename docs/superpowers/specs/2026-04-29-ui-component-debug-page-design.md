# UI 组件调试页面设计文档

**日期：** 2026-04-29
**状态：** ✅ 已批准
**架构方案：** Build Variant + 组件注册机制（feature/debug 独立模块）

---

## 1. 目标与范围

### 目标
在 NLtimer 应用中新建独立的 UI 组件调试页面，满足开发阶段快速预览和调试自定义 Composable 组件的需求。

### 范围
- ✅ **空白调试容器**：独立的 Composable 容器页面，不耦合任何业务代码（ViewModel、Repository 等）
- ✅ **组件热切换**：通过分组筛选 + 组件列表，即时切换查看不同 UI 组件的展示效果
- ✅ **实时交互预览**：预览区域支持组件的完整渲染和真实交互（点击、输入、滚动等）
- ✅ **可控入口**：通过 Build Variant 控制，debug 构建可见、release 完全移除
- ✅ **组件注册机制**：各 feature 模块在 src/debug/ 下自主注册待调试组件，debug 模块统一收集展示
- ❌ **不在范围内**：组件参数动态调节面板、截图对比、性能分析（后续迭代）

---

## 2. 架构设计

### 2.1 模块依赖关系

依赖方向（单向，无循环）：

```
core/designsystem        ← 定义 DebugComponent / DebugComponentRegistry 协议
      ↑
各 feature 模块           ← src/debug/java/ 下注册自己的待调试 Composable
      ↑
feature/debug            ← 新模块，收集 Registry 中的组件，提供调试页面 UI
      ↑
app (debugImplementation) ← debug 编译时才包含，release 完全剥离
```

### 2.2 模块结构

```
core/designsystem/                                 ☆ 新增 1 个文件
└── src/main/java/com/nltimer/core/designsystem/debug/
    └── DebugComponent.kt                         ★ DebugComponent 数据类 + Registry 单例

feature/debug/                                    ★ 新建模块
├── build.gradle.kts                              ★ Gradle 配置
├── src/main/
│   ├── AndroidManifest.xml                       ★ 模块清单
│   └── java/com/nltimer/feature/debug/
│       ├── DebugRoute.kt                         ★ Composable 导航入口
│       └── ui/
│           └── DebugPage.kt                      ★ 主页面（三段式布局）

feature/home/                                      ☆ 新增 1 个文件（debug 源码集）
└── src/debug/java/com/nltimer/feature/home/debug/
    └── HomeDebugComponents.kt                    ★ 注册 home 模块组件

feature/sub/                                       ☆ 新增 1 个文件（debug 源码集）
└── src/debug/java/com/nltimer/feature/sub/debug/
    └── SubDebugComponents.kt                     ★ 注册 sub 模块组件

...（其他 feature 模块同 pattern）

app/                                               ☆ 修改 3 个文件
├── build.gradle.kts                               ☆ + debugImplementation(projects.feature.debug)
├── src/main/java/com/nltimer/app/
│   ├── NLtimerApplication.kt                      ☆ onCreate 中调用各模块注册
│   └── navigation/NLtimerNavHost.kt               ☆ + composable("debug") { DebugRoute() }
└── component/
    └── AppDrawer.kt                               ☆ 追加 "🐛 调试" 菜单项（仅 debug）
```

---

## 3. 核心组件设计

### 3.1 DebugComponent — 组件协议（core/designsystem）

```kotlin
// core/designsystem/src/main/java/.../debug/DebugComponent.kt

data class DebugComponent(
    val id: String,                              // 唯一标识，如 "AddBehaviorSheet"
    val name: String,                            // 显示名称，如 "新增行为弹窗"
    val group: String,                           // 分组，如 "Sheets"、"Cards"
    val description: String = "",                 // 组件描述
    val content: @Composable () -> Unit,         // 组件 Composable lambda
)

object DebugComponentRegistry {
    private val _components = mutableListOf<DebugComponent>()
    val components: List<DebugComponent> get() = _components

    fun register(component: DebugComponent) {
        _components.add(component)
    }
}
```

**位置选择理由：** core/designsystem 是所有 feature 模块的共同依赖，将协议放在这里避免了为调试单独新建下层模块，架构成本最低。

### 3.2 DebugPage — 主页面布局（feature/debug）

三段式布局：

| 左栏（flex:1） | 中栏（flex:1.5） | 右栏（flex:3） |
|---|---|---|
| 分组筛选 | 组件列表 | 实时预览 |

- **分组筛选**：从 Registry 中提取去重 group 值，显示为可点击列表，"全部"为默认选中
- **组件列表**：根据选中分组过滤组件，当前选中组件高亮
- **预览区域**：调用 `selectedComponent.content()` 直接渲染，支持完整交互

### 3.3 各 Feature 注册方式

```kotlin
// feature/home/src/debug/java/.../HomeDebugComponents.kt
// release 编译时此文件完全不存在

object HomeDebugComponents {
    fun registerAll() {
        DebugComponentRegistry.register(
            DebugComponent(
                id = "AddBehaviorSheet",
                name = "新增行为弹窗",
                group = "Sheets",
                description = "添加/编辑行为记录的底部弹窗",
                content = { AddBehaviorSheetDebugPreview() }
            )
        )
        // 更多组件...
    }
}
```

### 3.4 Application 组装点

```kotlin
// NLtimerApplication.onCreate()

override fun onCreate() {
    super.onCreate()
    // ... 其他初始化代码

    // 通过 debugImplementation 引入的调试模块注册
    HomeDebugComponents.registerAll()
    SubDebugComponents.registerAll()
    StatsDebugComponents.registerAll()
    CategoriesDebugComponents.registerAll()
    SettingsDebugComponents.registerAll()
    // ... 其他模块
}
```

### 3.5 导航入口

```kotlin
// NLtimerNavHost.kt
composable("debug") { DebugRoute() }

// AppDrawer.kt — 菜单项（仅 debug 构建可见）
// 利用 BuildConfig.DEBUG 控制显隐，或直接放在 debug 源码集中
```

---

## 4. 数据流

```
[Application.onCreate]
       │
       ▼
[各 feature DebugComponents.registerAll()]
       │
       ├──→ DebugComponentRegistry._components
       │
       ▼
[DebugPage 读取 Registry.components]
       │
       ├──→ 提取分组列表 → 左栏渲染
       ├──→ 按分组过滤 → 中栏渲染
       └──→ 选中组件 → 右栏 content() 调用 → Compose 渲染
```

无 ViewModel、无 Repository、无 LiveData/Flow。纯函数式 UI，状态仅存在于 DebugPage 内部的 `remember { mutableStateOf() }`。

---

## 5. Build Variant 隔离

| 构建类型 | feature/debug 模块 | debug 源码集 | 导航路由 | Drawer 菜单项 |
|---|---|---|---|---|
| **debug** | ✅ 包含 | ✅ 编译 | ✅ 可见 | ✅ 可见 |
| **release** | ❌ 完全不存在 | ❌ 不编译 | ❌ 不可见 | ❌ 不可见 |

- app/build.gradle.kts 使用 `debugImplementation(projects.feature.debug)` 确保 release 构建完全不包含模块
- 各 feature 的注册代码放在 `src/debug/java/` 目录，release 编译时自动排除
- Navigation composable("debug") 路由因 feature/debug 不存在，release 编译时不会注册

---

## 6. 文件清单

### 新建文件

| 文件 | 路径 | 说明 |
|---|---|---|
| DebugComponent.kt | core/designsystem/.../debug/ | 数据类 + Registry |
| build.gradle.kts | feature/debug/ | 模块构建配置 |
| AndroidManifest.xml | feature/debug/src/main/ | 模块清单 |
| DebugRoute.kt | feature/debug/.../ | 导航入口 Composable |
| DebugPage.kt | feature/debug/.../ui/ | 主页面三段式布局 |
| HomeDebugComponents.kt | feature/home/src/debug/.../ | home 模块组件注册 |
| SubDebugComponents.kt | feature/sub/src/debug/.../ | sub 模块组件注册 |
| StatsDebugComponents.kt | feature/stats/src/debug/.../ | stats 模块组件注册 |
| CategoriesDebugComponents.kt | feature/categories/src/debug/.../ | categories 模块组件注册 |
| SettingsDebugComponents.kt | feature/settings/src/debug/.../ | settings 模块组件注册 |
| ManagementDebugComponents.kt | feature/management_activities/src/debug/.../ | 活动管理模块组件注册 |
| TagManagementDebugComponents.kt | feature/tag_management/src/debug/.../ | 标签管理模块组件注册 |

### 修改文件

| 文件 | 修改内容 |
|---|---|
| settings.gradle.kts | + include("feature:debug") |
| app/build.gradle.kts | + debugImplementation(projects.feature.debug) |
| NLtimerApplication.kt | onCreate 中添加各模块注册调用 |
| NLtimerNavHost.kt | + composable("debug") 路由 |
| AppDrawer.kt | + "🐛 调试" 菜单项 |

---

## 7. 边界情况与错误处理

- **Registry 为空**：DebugPage 显示 "暂无已注册的调试组件" 空状态提示
- **组件渲染异常**：用 try-catch 包裹 content() 调用，异常时显示错误信息卡片（含组件名和异常消息）
- **重复注册**：Registry.register() 不检查去重（允许同名覆盖场景），通过约定保证 id 唯一性
- **运行时无 feature 注册**：如果忘记在 Application 中调用 registerAll()，Registry 为空，页面正常显示空状态
