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
│  core/behaviorui (行为 UI)                   │
│  AddBehaviorSheet / WheelPicker / TimePicker │
├─────────────────────────────────────────────┤
│  core/designsystem (设计系统)                │
│  Theme / FormSpec / ColorPicker             │
├─────────────────────────────────────────────┤
│  core/data (数据层)                          │
│  Repository → DAO → Room DB                  │
│  SettingsPrefs → DataStore                   │
└─────────────────────────────────────────────┘
```

## 模块依赖方向（严格单向）

```
app ──→ feature/* ──→ core/data ──→ core/designsystem
              │              │          ↑
              │              └────→ core/behaviorui
              │                        ↑
              └────────────────────────┘
```

- `app` 依赖所有 feature 模块和 core 模块
- `feature/home` 额外依赖 `core/behaviorui` 和 `core/tools`
- `core/data` 依赖 `core/designsystem`（枚举引用）
- `core/behaviorui` 依赖 `core/designsystem` 和 `core/data`（模型引用）

## 导航结构

```
NavHost (startDestination = "home")
├── home              → HomeRoute
├── stats             → StatsRoute
├── settings          → SettingsRoute
│   ├── theme_settings    → ThemeSettingsRoute
│   ├── dialog_config     → DialogConfigRoute
│   ├── data_management   → DataManagementRoute
│   ├── home_layout_config → HomeLayoutConfigRoute
│   └── color_palette     → ColorPaletteRoute
├── categories        → CategoriesRoute
├── management_activities → ActivityManagementRoute
├── tag_management    → TagManagementRoute
├── behavior_management → BehaviorManagementRoute
└── [debugRoutes]     → DebugRoute (仅 debug 构建)
```

## DI 架构

- **Hilt** + `@HiltViewModel`
- `core/data/di/DataModule`：绑定 Repository 接口 → 实现，提供 Room DB + DAO
- `app/di/SettingsModule`：提供 SettingsPrefs
- `core/tools/di/ToolsModule`：提供通用工具类注入

## 调试机制

- `DebugInitializer` 动态注册各模块的调试组件
- `NLtimerNavHost.debugRoutes` 动态注入调试路由
