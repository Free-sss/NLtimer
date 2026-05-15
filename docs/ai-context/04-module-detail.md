# 模块详情

## app（壳模块）

| 文件 | 职责 |
|------|------|
| MainActivity | 入口 Activity，setContent → NLtimerApp |
| NLtimerApp | 根 Composable，处理主题包裹与 NavController |
| NLtimerScaffold | 主框架：Drawer + TopBar + BottomNav + NavHost |
| NLtimerNavHost | 路由注册表，定义页面跳转与过渡动画 |
| NLtimerApplication | Hilt Application 入口 |
| component/* | AppDrawer, AppBottomNavigation, AppTopAppBar 等框架组件 |

## core/data（数据层）

- **database/**: `NLtimerDatabase` (Room) 定义，包含 Activity, Group, Tag, Behavior 实体及 DAO。
- **repository/**: 提供对数据的抽象访问，处理缓存与数据库同步。
- **SettingsPrefs**: 基于 DataStore 的偏好设���（主题、布局、弹窗配置）。

## core/behaviorui（行为 UI）

- **sheet/**: `AddBehaviorSheet` (核心底部弹窗)，包含 `WheelPicker`, `TimePickerCompact`, `ActivityPicker`, `TagPicker` 等交互组件。
- **components**: `AdaptiveActivityChip`, `StaggeredHorizontalGrid` 等。

## core/designsystem（设计系统）

- **theme/**: `NLtimerTheme`, `AppTheme` 枚举, `PaletteStyle`, 动态色彩生成。
- **form/**: `GenericFormDialog`, `FormSpec` 声明式表单构建。

## core/tools（通用工具）

- **match/**: `NoteMatcher`, `NoteDirectiveParser` (解析备注中的指令如 `-30m`)。
- **timing/**: `StartBehaviorTool`, `QueryCurrentBehaviorTool` 等逻辑单元。
- **registry**: 通用 `ToolRegistry` 机制。

## feature/home（首页）

- **HomeScreen**: 支持 Grid (网格), Timeline (时间轴), Log (日志) 三种行为展示布局。
- **HomeViewModel**: 状态驱动，聚合 Behavior 记录并转换为 UI 模型。

## feature/behavior_management（行为管理）

- **功能**: 历史行为过滤、批量导入导出 (JSON)、计时修正。
- **组件**: `BehaviorTimelineItem`, `ImportExportDialog`。

## 其他功能模块

- **settings**: 主题切换、弹窗行为配置、色彩方案选择、数据导出。
- **management_activities**: 活动的 CRUD 与分组管理。
- **tag_management**: 标签的 CRUD 与分类管理。
- **debug**: 仅在 Debug 构建中可见，用于测试 UI 组件与数据库状态。
