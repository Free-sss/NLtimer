# 设计规格：统一主页布局与动态专注卡片

## 1. 目标
将现有的四个独立主页布局（网格、时刻、日志、反向时间线）整合到一个统一的页面容器中，实现无缝切换和更连贯的用户体验。

## 2. 核心变更

### 2.1 统一页面结构
- **容器**：在 `HomeScreen.kt` 中引入 `AnimatedContent` 或 `HorizontalPager` 作为四种布局的统一宿主。
- **切换动画**：布局切换时提供侧滑（Horizontal Slide）效果。
- **共享状态**：所有布局继续使用同一个 `HomeUiState` 数据源。

### 2.2 专注卡片 (MomentFocusCard) 动态化
- **常驻显示**：无论是否有活跃行为，`MomentFocusCard` 始终显示。
- **动态位置**：
    - **网格布局 (GRID)**：由于时间轴为正序，专注卡片作为列表的**最后一项**（放在滚动区域底部）。
    - **其他布局 (MOMENT, LOG, TIMELINE_REVERSE)**：专注卡片作为列表的**第一项**（放在滚动区域顶部）。

## 3. 技术实现

### 3.1 HomeScreen.kt 重构
- 移除原有的 `when(layout)` 直接切换逻辑。
- 使用 `AnimatedContent` 包装 `HomeLayoutContent`。
- 注入侧滑动画 `slideInHorizontally` 和 `slideOutHorizontally`。

### 3.2 视图组件调整
- **MomentView.kt**: 移除内部的 `MomentDisplayItem.FocusCard` 逻辑。
- **TimeAxisGrid.kt**: 允许在网格末尾插入额外内容（专注卡片）。
- **BehaviorLogView.kt / TimelineReverseView.kt**: 允许在列表头部插入专注卡片。

### 3.3 布局适配器
创建一个通用的布局适配逻辑，根据当前 `HomeLayout` 决定 `MomentFocusCard` 的位置，并确保它能正确响应数据源变化。

## 4. 交互入口
- 保持现状：使用 `LayoutMenuHeader` 下拉菜单切换布局。
- 逻辑流：`LayoutMenuHeader` -> 更新 `LocalTheme.homeLayout` -> `HomeScreen` 触发 `AnimatedContent` 动画 -> 渲染对应布局。

## 5. 验收标准
1. 切换布局时，页面不发生导航跳转，且有明显的横向滑动动画。
2. 在网格视图中，向下滚动到最后能看到专注卡片。
3. 在其他视图中，顶部最先看到的是专注卡片。
4. 专注卡片中的倒计时、状态更新在所有布局中保持同步。
