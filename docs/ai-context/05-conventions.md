# 代码规范与约定

## 架构模式

- **MVVM**：Route → Screen → ViewModel → Repository
- **单向数据流**：ViewModel 暴露 `StateFlow<UiState>`，UI 只读不写
- **Repository 接口 + 实现**：接口在 `repository/`，实现在 `repository/impl/`
- **领域模型与 Entity 分离**：`model/` 放领域类（带 `fromEntity`/`toEntity`），`entity/` 放 Room Entity

## 命名约定

| 类别 | 规则 | 示例 |
|------|------|------|
| 模块 | 小写下划线 | `management_activities`, `tag_management` |
| 包名 | 全小写点分 | `com.nltimer.feature.home.ui` |
| Route Composable | `*Route` | `HomeRoute`, `CategoriesRoute` |
| Screen Composable | `*Screen` | `HomeScreen`, `SettingsScreen` |
| ViewModel | `*ViewModel` | `HomeViewModel` |
| UiState | `*UiState` | `HomeUiState`, `GridCellUiState` |
| Repository 接口 | `*Repository` | `BehaviorRepository` |
| Repository 实现 | `*RepositoryImpl` | `BehaviorRepositoryImpl` |
| DAO | `*Dao` | `ActivityDao` |
| Entity | `*Entity` | `ActivityEntity` |
| Hilt Module | `*Module` | `DataModule`, `HomeModule` |
| 调试组件注册 | `*DebugComponents` | `FeatureDebugComponents` |

## Compose 约定

- Route 是 Composable 入口，负责创建/注入 ViewModel
- Screen 接收 UiState 和事件回调，纯展示
- `@Immutable` 标注所有 UiState 数据类
- 使用 `collectAsStateWithLifecycle` 收集 Flow

## 主题约定

- 所有颜色通过 Material3 主题获取，不硬编码
- 主题配置通过 `LocalTheme.current` 读取
- 动态调色板由 MaterialKolor 从种子色生成

## 表单约定

- 使用声明式 `FormSpec` 定义表单结构
- `FormRow` sealed class：TextInput / IconColor / LabelAction / Switch / NumberInput
- `GenericFormSheet` / `GenericFormDialog` 通用渲染

## Debug 约定

- 每个 feature 模块有 `debug/` 源码集，包含 `*DebugComponents.kt`
- 通过 `DebugComponentRegistry.register()` 注册预览组件
- `app/src/debug/DebugInitializer.kt` 统一调用各模块的 `registerAll()`
- Debug 模块仅通过 `debugImplementation` 引入

## 导航约定

- 路由名为小写字符串：`"home"`, `"settings"`, `"tag_management"`
- 返回导航通过 `onNavigateBack` 回调传递
- `NLtimerNavHost.debugRoutes` 用于 debug 路由动态注入

## 数据库约定

- Room Entity 放 `database/entity/`
- 领域模型放 `model/`，提供 `fromEntity()`/`toEntity()` 转换
- 迁移代码放在 `NLtimerDatabase.companion` 中
- 迁移验证器放 `migration/`
- 不使用 `fallbackToDestructiveMigration`，必须提供显式迁移

## 测试约定

- ViewModel 单元测试放在 `test/` 源码集
- 使用 JUnit + kotlinx-coroutines-test
- Repository 测试同样在 `test/` 下

## DI 约定

- Hilt `@Binds` 绑定接口到实现
- `@Provides` 提供 Room DB、DAO、第三方依赖
- ViewModel 使用 `@HiltViewModel` + `@Inject constructor`
