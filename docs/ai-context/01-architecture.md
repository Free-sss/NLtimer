# 架构概览

## 分层架构

```
┌─────────────────────────────────────────────┐
│  app (壳模块)                                │
│  MainActivity → NLtimerApp → NLtimerScaffold │
│  NLtimerNavHost (路由注册)                    │
├─────────────────────────────────────────────┤
│  feature/* (功能模块)                        │
│  Route → Screen → ViewModel → Repository    │
├─────────────────────────────────────────────┤
│  core/designsystem (设计系统)                │
│  Theme / FormSpec / DebugComponent           │
├─────────────────────────────────────────────┤
│  core/data (数据层)                          │
│  Repository → DAO → Room DB                  │
│  SettingsPrefs → DataStore                   │
└─────────────────────────────────────────────┘
```

## 模块依赖方向（严格单向）

```
app ──→ feature/* ──→ core/data ──→ core/designsystem
              │                        ↑
              └────────────────────────┘
```

- `app` 依赖所有 feature 模块和 core 模块
- `feature/*` 依赖 `core/data` 和 `core/designsystem`
- `core/data` 依赖 `core/designsystem`（仅 Theme 枚举引用）
- `core/designsystem` 不依赖任何项目模块

## 导航结构

```
NavHost (startDestination = "home")
├── home              → HomeRoute
├── sub               → SubRoute
├── stats             → StatsRoute
├── settings          → SettingsRoute
│   ├── theme_settings  → ThemeSettingsRoute
│   └── dialog_config   → DialogConfigRoute
├── categories        → CategoriesRoute
├── management_activities → ActivityManagementRoute
├── tag_management    → TagManagementRoute
└── [debugRoutes]     → DebugRoute (仅 debug 构建)
```

底部导航栏固定 4 项：主页 / 副页 / 统计 / 设置

## DI 架构

- **Hilt** + `@HiltViewModel`
- `core/data/di/DataModule`：绑定 Repository 接口 → 实现，提供 Room DB + DAO
- `app/di/DataModule`：提供 SettingsPrefs
- `feature/home/di/HomeModule`：绑定 MatchStrategy → KeywordMatchStrategy

## 主题注入链

```
SettingsPrefs (DataStore)
  → ThemeSettingsViewModel 读取 Flow<Theme>
    → NLtimerTheme(theme) 包裹根 Composable
      → LocalTheme 提供给子组件
      → DynamicMaterialTheme 生成 Material3 调色板
```

## Debug 模块注入机制

```
DebugInitializer (app/src/debug) → 调用各 feature 的 DebugComponents.registerAll()
                                    ↓
FeatureDebugComponents.registerAll() → DebugComponentRegistry.register(...)
                                    ↓
DebugPage → 读取 DebugComponentRegistry.components 渲染列表

NLtimerNavHost.debugRoutes → 动态注入 debug 路由（release 为 null）
```
