# 设置模块导航改造设计

## 1. 背景

当前设置链路存在以下问题：

- `settings` 页面作为一级页，已经处于全局导航框架内。
- `theme_settings` 与 `dialog_config` 作为二级页，又在页面内部各自套了一层 `Scaffold` 和 `TopAppBar`。
- 全局导航壳与子页导航壳职责重叠，导致顶部留白割裂、标题重复、返回入口重复。

相关现状代码如下：

- 全局导航壳：`app/src/main/java/com/nltimer/app/NLtimerScaffold.kt`
- 路由注册：`app/src/main/java/com/nltimer/app/navigation/NLtimerNavHost.kt`
- 设置首页：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/SettingsScreen.kt`
- 主题配置：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/ThemeSettingsScreen.kt`
- 弹窗配置：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/DialogConfigScreen.kt`

## 2. 目标

本次改造只覆盖设置链路，范围限定为：

- `settings`
- `theme_settings`
- `dialog_config`

目标如下：

- 保留设置首页的一级页身份。
- 将主题配置与弹窗配置改为真正的全屏二级页。
- 统一二级页的标题、返回逻辑与内容边距。
- 消除重复标题、重复返回按钮与不自然的顶部间距。
- 不扩大到其他功能模块，不重构整个应用导航体系。

## 3. 范围与非目标

### 3.1 范围

本次设计包含：

- 调整设置链路的页面分层。
- 调整 `NLtimerScaffold` 对不同路由的全局栏显示策略。
- 为设置二级页引入统一页面容器。
- 重构设置首页的内容呈现方式。

### 3.2 非目标

本次设计不包含：

- 重命名既有路由。
- 重写整个 `NavHost` 结构。
- 将活动管理、标签管理等其他页面一起并入本轮导航改造。
- 顺带重构全局顶栏的全部动作按钮行为。

## 4. 设计原则

- **一级页与二级页职责分离：** 一级页由全局导航壳承载，二级页由模块内页面容器承载。
- **只改必要范围：** 先修正设置链路，不引入额外架构扩张。
- **统一视觉规则：** 相同层级页面共享同一套标题、返回与间距规范。
- **避免双重 `Scaffold`：** 页面骨架只保留一层，避免重复占位与状态分散。

## 5. 页面分层设计

### 5.1 一级页

以下页面继续视为一级页：

- `settings`

一级页规则如下：

- 继续由 `NLtimerScaffold` 提供全局顶栏与全局底栏。
- 页面身份由全局顶栏表达。
- 页面内容区不再重复渲染一个大号页面标题。

### 5.2 二级页

以下页面调整为设置模块的全屏二级页：

- `theme_settings`
- `dialog_config`

二级页规则如下：

- 不显示全局顶栏。
- 不显示全局底栏。
- 仅显示设置模块统一的子页顶栏。
- 顶栏只包含返回按钮与标题。

## 6. 导航壳职责调整

### 6.1 `NLtimerScaffold` 的职责

`NLtimerScaffold` 需要基于当前路由决定是否显示全局栏。

建议按路由分为两组：

- **主导航路由：** `home`、`sub`、`stats`、`categories`、`management_activities`、`settings`
- **设置全屏子页路由：** `theme_settings`、`dialog_config`

行为规则如下：

- 当前路由属于主导航路由时，显示全局 `AppTopAppBar` 与 `AppBottomNavigation`。
- 当前路由属于设置全屏子页路由时，不显示全局 `AppTopAppBar` 与 `AppBottomNavigation`。
- `NLtimerNavHost` 始终负责内容切换，但不再让设置二级页承受全局栏的额外占位。

### 6.2 `NLtimerNavHost` 的职责

`NLtimerNavHost` 继续保留现有路由名与跳转关系。

本轮改造中，它的主要变化不是图结构，而是页面内容职责：

- `SettingsRoute` 继续作为设置模块入口。
- `ThemeSettingsRoute` 与 `DialogConfigRoute` 改为向统一子页容器提供内容。
- 路由跳转关系保持：`settings -> theme_settings`、`settings -> dialog_config`。

## 7. 设置首页设计

### 7.1 页面定位

`SettingsScreen` 保留一级页身份，但从“内容列表页”调整为“模块入口页”。

### 7.2 页面结构

设置首页不再：

- 自己再套一层页面级 `Scaffold`
- 在内容区重复显示大号「设置」标题

设置首页改为：

- 依赖全局顶栏显示页面标题「设置」
- 内容区展示卡片式或模块式入口
- 每个入口至少包含图标、标题、说明与进入指示

### 7.3 设计效果

这样调整后，设置首页将具备以下特征：

- 视觉上更像模块首页，而不是正文列表。
- 层级上与二级页形成清晰区分。
- 不再与全局顶栏发生标题重复。

## 8. 设置二级页统一容器设计

### 8.1 容器目标

新增统一的设置子页容器，例如 `SettingsSubpageScaffold`。该容器服务于所有设置类二级页。

### 8.2 容器职责

统一容器负责：

- 渲染顶部 `TopAppBar`
- 提供返回按钮
- 显示页面标题
- 处理统一的内容边距与底部留白
- 提供一致的滚动区域起点

统一容器不负责：

- 具体设置项布局
- 业务状态管理
- 全局底栏逻辑

### 8.3 顶部区域规则

统一子页顶栏应满足：

- 只保留一个返回按钮
- 只保留一个标题
- 不叠加全局标题语义
- 样式在主题配置页与弹窗配置页之间保持一致

### 8.4 内容区规则

统一内容区应满足：

- 顶部起始间距一致
- 左右边距一致
- 底部留白一致
- 页面自身不再叠加第二层页面级 `Scaffold` padding

### 8.5 对现有页面的影响

- `ThemeSettingsScreen` 移除内部页面骨架，只保留主题设置内容。
- `DialogConfigScreen` 移除内部页面骨架，只保留弹窗配置内容。
- 两者都通过统一容器获得标题、返回与页面边距。

## 9. 数据流与交互影响

本次改造不改变以下行为：

- `ThemeSettingsViewModel` 的状态收集与更新逻辑
- `DialogConfigViewModel` 的状态收集与更新逻辑
- 主题配置页中的颜色选择器、开关与选项切换
- 弹窗配置页中的下拉菜单、开关与滚动行为

本次改造只调整页面承载方式与导航表现，不改变业务状态来源。

## 10. 验收标准

### 10.1 设置首页

进入 `settings` 后，应满足：

- 显示全局顶栏标题「设置」。
- 内容区不再重复显示大号「设置」标题。
- 页面主体表现为设置模块入口，而非普通正文列表。

### 10.2 主题配置与弹窗配置

进入 `theme_settings` 与 `dialog_config` 后，应满足：

- 不显示全局顶栏。
- 不显示全局底栏。
- 页面顶部只出现一套统一的设置子页顶栏。
- 顶栏只包含返回按钮与标题。
- 不再出现重复标题。
- 不再出现双返回入口。
- 首屏顶部留白自然，没有双重 padding 叠加。

### 10.3 返回链路

导航行为应满足：

- `settings -> theme_settings -> 返回` 可回到 `settings`
- `settings -> dialog_config -> 返回` 可回到 `settings`
- 设置首页离开后，仍遵循原有一级导航逻辑
- 返回过程中不误触发抽屉或错误切换到底部导航其他项

### 10.4 视觉一致性

`theme_settings` 与 `dialog_config` 应保持：

- 顶栏高度一致
- 标题位置一致
- 返回按钮位置一致
- 左右边距一致
- 顶部起始间距一致
- 底部留白一致

### 10.5 回归保护

以下行为不能被本次改造破坏：

- `home`、`sub`、`stats` 等一级页的全局顶栏与底栏显示
- 设置首页现有入口可正常进入
- `RouteSettingsPopup` 的覆盖层逻辑
- 主题配置页与弹窗配置页的既有交互功能

## 11. 实施边界

实现阶段应遵循以下边界：

- 优先通过页面分层与容器职责调整解决问题，不以局部修补 padding 代替结构修正。
- 不为当前需求引入额外导航框架抽象。
- 如后续新增更多设置子页，应复用统一子页容器，而不是复制页面骨架。

## 12. 后续实现方向

后续实现计划应重点覆盖：

1. `NLtimerScaffold` 的按路由显示策略。
2. 设置统一子页容器的抽取。
3. `ThemeSettingsScreen` 与 `DialogConfigScreen` 的页面骨架剥离。
4. `SettingsScreen` 的卡片式入口重构。
5. 导航返回与视觉一致性的验证。
