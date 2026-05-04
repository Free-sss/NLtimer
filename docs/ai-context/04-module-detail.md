# 模块详情

## app（壳模块）

| 文件 | 职责 |
|------|------|
| MainActivity | 入口 Activity，setContent → NLtimerApp |
| NLtimerApp | 根 Composable，创建 NavController |
| NLtimerScaffold | 主框架：Drawer + TopBar + BottomNav + NavHost + 设置弹窗 |
| NLtimerNavHost | 路由注册表，所有 feature Route 在此注册 |
| NLtimerApplication | Application 类，Hilt 入口 |
| component/AppBottomNavigation | 底部导航栏（主页/副页/统计/设置） |
| component/AppDrawer | 侧边抽屉导航 |
| component/AppTopAppBar | 顶栏（菜单+设置按钮） |
| component/RouteSettingsPopup | 路由级设置弹窗（布局切换等） |
| di/DataModule | 提供 SettingsPrefs 绑定 |

## core/data（数据层）

```
core/data/
├── database/
│   ├── NLtimerDatabase.kt    # Room DB 定义 + 迁移
│   ├── dao/                   # ActivityDao, ActivityGroupDao, TagDao, BehaviorDao
│   └── entity/                # Entity 类（与 DB 表一一对应）
├── model/                     # 领域模型（Activity, Tag, Behavior, 等）
├── repository/                # Repository 接口
│   └── impl/                  # Repository 实现
├── migration/                 # 迁移验证器
├── di/DataModule.kt           # Hilt DI 绑定
├── SettingsPrefs.kt           # 偏好接口
└── SettingsPrefsImpl.kt       # DataStore 实现
```

## core/designsystem（设计系统）

```
core/designsystem/
├── theme/
│   ├── Theme.kt               # NLtimerTheme 入口
│   ├── ThemeConfig.kt         # Theme 数据类
│   ├── AppTheme.kt            # LIGHT / DARK / SYSTEM 枚举
│   ├── PaletteStyle.kt        # 9 种调色板风格枚举
│   ├── Fonts.kt               # FIGTREE / SYSTEM_DEFAULT 枚举
│   ├── HomeLayout.kt          # GRID / TIMELINE_REVERSE / LOG 枚举
│   ├── Color.kt               # 颜色常量
│   ├── Type.kt                # 排版系统
│   ├── ComponentExt.kt        # 组件扩展函数
│   ├── DialogDisplayConfig.kt # Chip/Grid/Path 绘制模式枚举
│   ├── EnumExt.kt             # 枚举扩展
│   ├── ListItemExt.kt         # 列表项扩展
│   ├── ModifierExt.kt         # Modifier 扩展
│   └── Fonts.kt               # 字体资源映射
├── form/
│   ├── FormSpec.kt            # 声明式表单规范（FormRow sealed class）
│   ├── ActivityFormSpecs.kt   # 活动/标签表单规范定义
│   ├── GenericFormDialog.kt   # 通用表单弹窗
│   └── GenericFormSheet.kt    # 通用表单底部弹窗
├── component/
│   └── ColorPickerDialog.kt   # 颜色选择弹窗
└── debug/
    ├── DebugComponent.kt      # 调试组件描述 + 注册中心
    └── DebugComponentRegistry # 全局组件注册表
```

## feature/home（首页）

| 文件 | 职责 |
|------|------|
| HomeRoute | Composable 入口，连接 ViewModel |
| HomeScreen | 主页 UI：网格/时间轴/日志布局 |
| HomeViewModel | 核心逻辑：加载行为、添加/完成行为、布局切换 |
| HomeUiState | 首页整体状态（行列表 + 弹窗状态 + 空闲模式） |
| GridRowUiState | 网格行状态 |
| GridCellUiState | 网格单元格状态（行为/占位/添加按钮） |
| TagUiState | 标签 UI 状态 |
| BehaviorDetailUiState | 行为详情弹窗状态 |
| MatchStrategy | 搜索匹配策略接口 |
| KeywordMatchStrategy | 关键词模糊匹配实现 |
| di/HomeModule | 绑定 MatchStrategy |

**HomeViewModel 核心流程：**
1. `init` → 加载当天行为 + 活动列表 + 标签列表
2. `buildUiState()` → Behavior 列表 → GridRow + GridCell（每行 4 格）
3. `addBehavior()` → ACTIVE 时先结束当前 → 插入新记录
4. `completeBehavior()` → 调用 `completeCurrentAndStartNext`

## feature/settings（设置）

| 文件 | 职责 |
|------|------|
| SettingsScreen | 设置主页（主题/弹窗配置入口） |
| ThemeSettingsScreen | 主题配置 UI |
| ThemeSettingsViewModel | 主题读写 + 布局切换回调 |
| DialogConfigScreen | 弹窗网格配置 UI |
| DialogConfigViewModel | 弹窗配置读写 |

## feature/categories（分类管理）

| 文件 | 职责 |
|------|------|
| CategoriesRoute | 入口 |
| CategoriesScreen | 分类列表 UI |
| CategoriesViewModel | 分类 CRUD |
| CategoriesUiState | 分类状态 |

## feature/management_activities（活动管理）

| 文件 | 职责 |
|------|------|
| ActivityManagementRoute | 入口 |
| ActivityManagementScreen | 活动列表 + 分组 UI |
| ActivityManagementViewModel | 活动/分组 CRUD + 预设初始化 |
| ActivityManagementUiState | 管理页状态 |

## feature/tag_management（标签管理）

| 文件 | 职责 |
|------|------|
| TagManagementRoute | 入口 |
| TagManagementScreen | 标签列表 UI |
| TagManagementViewModel | 标签 CRUD |
| TagManagementUiState | 标签管理状态 |

## feature/debug（调试工具）

| 文件 | 职责 |
|------|------|
| DebugRoute | 调试页入口 |
| DebugPage | 调试组件列表页 |
| FeatureDebugComponents | 注册所有调试预览组件 |
| GenericFormSheet | 通用表单底部弹窗 |
| DebugDatabaseHelper | 数据库调试工具 |
| FormSpec | 表单规范模型 |

**调试组件分组：** Pickers / Inputs / Forms / Components / Dialogs / Database

## feature/sub & feature/stats（占位模块）

- SubScreen / StatsScreen：简单占位页面，待后续开发
