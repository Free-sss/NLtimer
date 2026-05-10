# NLtimer 重构优化侦察报告

生成时间：2026-05-11  
工作树：`.worktrees/refactor-optimization-scout`  
分支：`refactor/optimization-scout`

## 结论摘要

项目整体模块方向基本健康：`app -> feature -> core`。当前最值得优先处理的问题不是单点性能，而是 UI 组件边界、调试模块依赖污染、生命周期感知状态采集不一致、以及大文件/长 Composable 的维护成本。

已在本工作树落地的低风险优化：

- 新增 `buildSrc/settings.gradle.kts`，固定 `buildSrc` rootProject name，消除多 worktree 下 Gradle 生成代码缓存不稳定警告。
- 抽取 `core:tools` match 测试 fixture，集中 `ToolResult.Success` 断言和 unchecked cast，减少重复测试样板。
- 将 `core:designsystem` 中已弃用的 RTL 相关 Material icons 迁移到 `Icons.AutoMirrored`，消除相关编译警告。

## 第一阶段：全面侦察

### 代码重复度扫描

高价值重复点：

- `feature/home/.../TagChip.kt` 与 `feature/tag_management/.../TagChip.kt` 都实现了标签颜色、亮度判断、文本颜色选择。差异只在交互形态和数据模型，适合抽成 `core:designsystem` 的 `AppTagChip`。
- `core/designsystem/component/GroupCard.kt` 与 `feature/management_activities/.../GroupCard.kt` 名称重复且职责重叠。建议将 feature 版本改名为 `ActivityGroupCard`，再复用 core 的容器样式。
- `feature:tag_management`、`feature:management_activities` 均依赖 `feature:debug`，主要为了字段详情/JSON 调试 UI。调试能力应下沉到 `core:designsystem:debug` 或仅通过 debug source set 注入。
- `core:tools` match 测试中重复的 `Tag` fixture 和结果 Map cast 已处理。

### 组件耦合度分析

模块依赖图：

```text
app
  -> core:designsystem
  -> core:data
  -> feature:home/settings/categories/management_activities/tag_management/behavior_management/sub/stats

feature:home -> core:designsystem, core:data, core:behaviorui
feature:behavior_management -> core:designsystem, core:data, core:behaviorui
feature:categories -> core:designsystem, core:data
feature:settings -> core:designsystem, core:data
feature:tag_management -> core:designsystem, core:data, feature:debug
feature:management_activities -> core:designsystem, core:data, feature:debug
feature:debug -> core:designsystem, core:data, core:tools, feature:home, core:behaviorui
core:behaviorui -> core:data, core:designsystem
core:tools -> core:data
core:data -> core:designsystem
```

主要耦合风险：

- `feature:* -> feature:debug` 是架构反模式。debug 模块应依赖业务模块做展示，而业务模块不应依赖 debug。
- `core:data -> core:designsystem` 让数据层依赖 UI/主题模型，例如 `ThemeConfig`、`DialogGridConfig`。短期可接受，但长期建议拆出 `core:model` 或 `core:preferences-model`，避免 data 层带 Compose/UI classpath。
- `feature:debug -> feature:home` 会让 debug 编译携带 home 依赖，适合改成 debug registry 插件式注册，避免 debug 模块成为聚合模块。

### 性能瓶颈定位

静态风险点：

- 多个 Route 使用 `collectAsState()`：`HomeRoute`、`CategoriesRoute`、`ThemeSettingsScreen`、`TagManagementScreen`、`DialogConfigScreen`、`DataManagementScreen`、`ActivityManagementScreen`、`ToolConsoleScreen`。Android UI 层应统一迁移到 `collectAsStateWithLifecycle()`。
- `HomeRoute` 中使用多个 `remember(viewModel)` 包装简单 lambda。若项目启用 React Compiler 类似理念不可直接类比 Compose；在 Compose 中此模式可读性一般，建议改为事件对象或 `HomeActions`，减少参数和 lambda 噪声。
- `HomeScreen` 内对 `uiState.rows.flatMap` 多次 `remember(uiState.rows)`：可在 `HomeUiStateBuilder` 或 ViewModel 层预计算 active cell、pending cell、flat cells，减少 UI 重复派生。
- `ActivityGridComponent`、`TagPicker`、`CategoryGroupCard`、`GroupCard` 中使用 `forEach + key` 手动渲染可滚动/可增长集合。数据量增大后应迁移到 lazy grid/list 或明确限制集合规模。
- `MaterialIconCatalog.kt` 743 行静态表会增加编译成本。短期可保留；中期建议生成代码或拆分分类文件。

### 未使用代码/依赖检测

需要进一步用 Android Lint/Gradle dependency analysis 验证的候选：

- 多个 feature 模块都显式依赖 `compose.ui.tooling.preview`，如果只用于 preview，应迁移到 `debugImplementation` 或 `compileOnly` 策略。
- `core:tools` 为满足约定插件添加 Compose BOM 和 `compose.ui`，说明 convention plugin 对非 Compose library 支持不足。建议拆出 `nltimer.android.library.no-compose` 或让 Compose 可配置。
- `feature:stats`、`feature:sub` 只依赖 `core:designsystem` 和 Compose UI，若页面仍是占位，可延后纳入 app 或标记为 experimental。

### 架构反模式识别

- 大型 Composable 同时承担容器、状态派生、布局、事件转译：`ThemeSettingsScreen.kt` 890 行、`AddBehaviorSheetContent.kt` 519 行、`HomeScreen.kt` 422 行、`CategoriesScreen.kt` 418 行。
- UI 层持有业务时间转换逻辑：`HomeRoute` 将 `LocalTime` 转 epoch millis，应下沉为 ViewModel 方法或 `ClockService`/use case。
- 字符串路由集中在 `NLtimerRoutes`，尚未类型化参数。Settings 子页已有参数 route，应逐步引入 sealed route spec 或 typed navigation wrapper。
- 错误处理散落在 ViewModel 和 Tool 内部，缺少统一的 `UiMessage` / `DomainError` 映射。

## AST 遍历分析：可复用组件模式

最大文件 Top 候选：

```text
892  feature/debug/.../ActivityRecordCombinedPreview.kt
890  feature/settings/.../ThemeSettingsScreen.kt
743  core/designsystem/.../MaterialIconCatalog.kt
519  core/behaviorui/.../AddBehaviorSheetContent.kt
478  core/designsystem/.../SelectionDialog.kt
455  core/designsystem/.../IconPickerSheet.kt
426  feature/debug/.../ToolConsoleScreen.kt
422  feature/home/.../HomeScreen.kt
418  feature/categories/.../CategoriesScreen.kt
411  core/behaviorui/.../DualTimePickerComponent.kt
407  feature/settings/.../DialogConfigScreen.kt
```

Composable 密度高的文件：

```text
8  feature/debug/.../ToolConsoleScreen.kt
8  core/behaviorui/.../DualTimePickerComponent.kt
8  core/designsystem/.../IconPickerSheet.kt
7  feature/settings/.../DialogConfigScreen.kt
7  feature/home/.../HomeScreen.kt
6  feature/settings/.../ThemeSettingsScreen.kt
6  core/behaviorui/.../AddBehaviorSheetContent.kt
```

建议提取模式：

- `AppTagChip`: 统一标签颜色、文字色、形态、点击/长按。
- `ExpandableGroupCard`: 统一分组标题、展开箭头、菜单槽、内容槽。
- `SettingsSection` / `SettingsOptionRow`: 把主题设置、对话框设置、数据管理中的 section/row 统一。
- `RouteActions` data class：替代 Route 中大量 lambda 参数，降低 Home/Categories/Management screen API 面积。
- `LifecycleRouteState`: 对 Flow 收集统一封装或至少统一使用 `collectAsStateWithLifecycle()`。

## 依赖图谱：循环依赖和冗余引用

未发现 Gradle 层循环依赖，但发现方向污染：

- `feature:tag_management -> feature:debug`
- `feature:management_activities -> feature:debug`
- `feature:debug -> feature:home`

迁移路径：

1. 将 `FieldInfo`、`FieldDetailDialog`、`toJsonString` 等调试展示能力移入 `core:designsystem:debug` 或 `core:debugui`。
2. `feature:debug` 改为依赖 `core:debugui`，业务 feature 只在 debug source set 引用调试 UI。
3. 从 `feature:tag_management` 和 `feature:management_activities` 删除 `implementation(projects.feature.debug)`。
4. 若 `feature:debug` 需要 Home preview，改成 app debug 聚合或独立 `debugImplementation`。

## 重复代码检测：公共逻辑抽象点

### 标签 Chip

目标 API：

```kotlin
@Composable
fun AppTagChip(
    label: String,
    color: Long?,
    modifier: Modifier = Modifier,
    prefixed: Boolean = true,
    size: TagChipSize = TagChipSize.Medium,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
)
```

迁移：

- `feature/home` 的 `TagChip(TagUiState)` 改为薄 wrapper。
- `feature/tag_management` 的 `TagChip(Tag)` 改为薄 wrapper。
- 颜色亮度判断放入 `core:designsystem`。

### 分组卡片

目标 API：

```kotlin
@Composable
fun ExpandableGroupCard(
    title: String,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier,
    menu: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
)
```

迁移：

- `feature/management_activities/.../GroupCard` 改名 `ActivityGroupCard`。
- 复用 `ExpandableGroupCard` 的 header/shape/elevation。
- `feature/tag_management` 的 category card 后续也迁入该模式。

## 组件粒度评估

需要拆分：

- `ThemeSettingsScreen.kt`: 拆为 `ThemeModeSection`、`TypographySection`、`PaletteSection`、`ExpressivenessSection`、`StyleConfigSection`，Route 仅收集状态。
- `AddBehaviorSheetContent.kt`: 拆出 `AddBehaviorFormState`、`BehaviorTimeSection`、`BehaviorPickerSection`、`BehaviorConfirmBar`。
- `HomeScreen.kt`: `HomeLayoutContent` 已有雏形，下一步把 `Grid/Timeline/Log/Moment` 四个 layout 移到独立文件，并将派生数据移出 UI。
- `CategoriesScreen.kt`: Dialog、section、row 已分函数，下一步抽 `CategoryActions` 和 `CategoryDialogState`。
- `DualTimePickerComponent.kt`: 拆 `DateWheelColumn`、`TimeWheelColumn`、`DualPickerState`。

可以合并或改名：

- `TagChip` 两个实现合并为 core API + feature wrapper。
- `GroupCard` 名称冲突，feature 侧改名更清晰。
- `FormRowRenderers.kt` 与 `form/renderer/*` 存在同类职责，后续应保留一种组织方式，避免双轨维护。

## 优先级排序的优化清单

P0：架构边界和构建健康

- 移除业务 feature 对 `feature:debug` 的依赖。
- 全项目 Route 层迁移到 `collectAsStateWithLifecycle()`。
- 为非 Compose core 模块调整 convention plugin，避免 `core:tools` 被迫引入 Compose。

P1：组件复用化

- 抽 `AppTagChip`、`ExpandableGroupCard`、`SettingsSection`。
- 为 Home/Categories/Management 引入 `Actions` 参数对象，降低函数参数数量。
- 将 `HomeRoute` 时间转换下沉到 ViewModel/use case。

P2：性能优化

- 将可增长集合的 `forEach + key` 改为 `LazyColumn`/`LazyVerticalGrid` 或限制最大显示项。
- 将 `HomeScreen` 派生状态上移到 ViewModel/UI state builder。
- `MaterialIconCatalog` 按 category 拆分或生成。

P3：容错和错误边界

- 建立 `UiMessage`/`DomainError` 统一映射。
- Data import/export 使用结构化错误，不直接暴露 Exception message。
- Tool 执行日志加开关或注入 logger，避免 release 噪声。

## 实施路线图

### 第 1 批：低风险清理

目标：无业务行为变化，减少警告和重复。

- 已完成：`buildSrc` name、match 测试 fixture、AutoMirrored icons。
- 下一步：`collectAsStateWithLifecycle()` 批量迁移。
- 验证：`:app:assembleDebug`、核心单元测试。

### 第 2 批：解耦 debug

目标：恢复 feature 模块单向依赖。

- 新建 `core:debugui` 或迁移到 `core:designsystem.debug` package。
- 移动 `FieldInfo`、`FieldDetailDialog`、JSON field helpers。
- 删除 `feature:tag_management`、`feature:management_activities` 对 `feature:debug` 的依赖。

### 第 3 批：组件复用

目标：降低 UI 重复，实现统一视觉和 API。

- 在 `core:designsystem` 新增 `AppTagChip`。
- 改造 home/tag management wrapper。
- 新增 `ExpandableGroupCard`，改造 activity/tag category group。

### 第 4 批：大组件拆分

目标：降低单文件复杂度，便于后续功能迭代。

- 拆 `HomeScreen` 四种 layout 文件。
- 拆 `ThemeSettingsScreen` section 文件。
- 拆 `AddBehaviorSheetContent` 状态和区域组件。

### 第 5 批：性能和数据流

目标：减少无效重组和 UI 层派生计算。

- ViewModel 输出预计算 UI state。
- 大集合使用 lazy 渲染。
- 增加 baseline profile 或宏基准测试：首页渲染、添加行为弹窗、图标选择器。

## 验证记录

已执行：

```text
./gradlew.bat projects
./gradlew.bat :core:tools:testDebugUnitTest
./gradlew.bat :core:designsystem:compileDebugKotlin
```

结果：

- `projects` 成功。
- `core:tools:testDebugUnitTest` 成功。
- `core:designsystem:compileDebugKotlin` 成功。

仍存在的构建警告：

- `android.dependency.excludeLibraryComponentsFromConstraints=true` 已弃用。建议按 AGP 10 迁移提示改为 `android.dependency.useConstraints=false`，但需要确认现有依赖约束策略后再改。
