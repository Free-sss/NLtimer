# 主页色彩层次重构设计

## 背景

NLtimer 主页色彩单调——4 种布局模式（GRID / TIMELINE_REVERSE / LOG / MOMENT）中的已完成行为、空单元格、锁定区域大面积使用 `surfaceContainerLow` / `surfaceVariant`，缺乏视觉层次。固定 UI 元素（FAB、底栏、抽屉、顶栏、时间侧边栏）也未充分利用 primary / secondary / tertiary 三个色调族制造区分。

项目已有完善的 MaterialKolor 动态主题系统（种子色 + 9 种调色板风格），但主页组件几乎只用 primary 和 surface 两个色调。

## 设计决策

通过头脑风暴确定的设计方向：

- **含蓄分层**（container 级色调），不用过于鲜明的对比
- **区域驱动映射** — primary/secondary 同时覆盖行为状态和对应侧边栏区域，tertiary 保留给成就/亮点
- **状态-区域对齐** — 每种状态固定映射一个色调族，简洁直接

## 色彩映射规则

### 核心状态映射（跨所有布局统一）

所有布局中的行为卡片/单元格遵循同一套状态→色调映射：

| 状态 | 背景色 | 边框色 | 文字色 | 用途 |
|------|--------|--------|--------|------|
| 活跃/当前 | `primaryContainer` | `primary` | `onPrimaryContainer` | 正在进行的行为 |
| 已完成 | `secondaryContainer` | `outlineVariant` | `onSecondaryContainer` | 已结束的行为 |
| 待填/空闲 | `tertiaryContainer` | `tertiary` | `onTertiaryContainer` | 空白待填区域 |
| 锁定/未来 | `surfaceDim` | `outlineVariant` | `outlineVariant` | 不可编辑区域 |
| 待办/目标 | `secondaryContainer` | `outlineVariant` | `onSecondaryContainer` | PENDING 状态 |
| 白金成就 | `surfaceContainerLow` | `tertiary`（渐变） | `onSurface` | 完美达成的行为 |

### TimeSideBar（网格布局时间侧边栏）

| 状态 | 背景色 | 文字色 |
|------|--------|--------|
| 当前小时 | `primary` | `onPrimary` |
| 活跃小时（有行为） | `secondaryContainer` | `onSecondaryContainer` |
| 非活跃小时 | 透明 | `outline` |

### TimeFloatingLabel（网格行时间标签）

| 状态 | 文字色 |
|------|--------|
| 当前行 | `primary` |
| 其他行 | `onSurfaceVariant`（保持） |

### 固定 UI 元素

| 元素 | 变更 | 说明 |
|------|------|------|
| **BottomBarDragFab** (FAB) | 活跃时 `primary`/`onPrimary`，空闲时 `secondaryContainer`/`onSecondaryContainer` | 空闲状态从 `primaryContainer` 改为 `secondaryContainer`，与"已完成"区域呼应 |
| **AppFloatingBottomBar** (浮动底栏) | `surfaceContainerHigh` → `secondaryContainer` | 底栏用 secondary 色调与主内容区 primary 形成区分 |
| **AppCenterFabBottomBar** (居中FAB底栏) | 同上 | 同上 |
| **AppDrawer** (侧边抽屉) | 保持不变 | 标准 M3 NavigationDrawerItem 已自动适配主题色 |
| **AppTopAppBar** (顶栏) | 保持 `background` | 顶栏保持中性，不与内容争色彩 |
| **AppBottomNavigation** (标准底栏) | 保持不变 | 标准 NavigationBar 已自动适配 |
| **Settings 按钮** (浮动底栏) | `surfaceContainerHigh` → `secondaryContainer` | 与底栏一致 |

### 行为状态标签（BehaviorLogCard 内的状态 pill）

| 状态 | 背景色 | 文字色 |
|------|--------|--------|
| ACTIVE | `primaryContainer` | `onPrimaryContainer`（保持） |
| COMPLETED | `secondaryContainer` | `onSecondaryContainer` |
| PENDING | `tertiaryContainer` | `onTertiaryContainer` |

## 变更范围

### 需修改的文件

#### 行为卡片/单元格（核心状态映射）

1. **GridCell.kt** (`feature/home/.../components/`)
   - 已完成行为背景：`surfaceContainerLow` → `secondaryContainer`
   - 已完成行为文字：`onSurface` → `onSecondaryContainer`
   - 白金成就边框：硬编码蓝色 RGB → `tertiary` 渐变

2. **GridCellEmpty.kt** (`feature/home/.../components/`)
   - 背景：`surfaceVariant` (0.3 alpha) → `tertiaryContainer` (styled alpha)
   - 边框：`outlineVariant` → `tertiary`，样式改为 dashed
   - 文字：`primary` → `onTertiaryContainer`

3. **TimelineReverseView.kt** (`feature/home/.../components/`)
   - `TimelineBehaviorItem` 已完成：`surfaceVariant` (0.2 alpha) → `secondaryContainer` (styled alpha)，边框 `outlineVariant` (0.5) → `outlineVariant`
   - `TimelineIdleItem`（空闲段）：`surfaceVariant` (0.1 alpha) → `tertiaryContainer` (styled alpha)，添加图标 `onPrimaryContainer` → `onTertiaryContainer`

4. **BehaviorLogCard.kt** (`feature/home/.../components/`)
   - 已完成卡片背景：`surfaceVariant` (0.2 alpha) → `secondaryContainer` (styled alpha)
   - 状态 pill：COMPLETED `tertiaryContainer` → `secondaryContainer`，PENDING `secondaryContainer` → `tertiaryContainer`

5. **MomentBehaviorItem.kt** (`feature/home/.../components/`)
   - 已完成卡片背景：`surfaceVariant` (0.2 alpha) → `secondaryContainer` (styled alpha)
   - "目标"标签文字：`tertiary` → `onTertiaryContainer`（与 PENDING 用 tertiaryContainer 呼应）

#### 时间侧边栏

6. **TimeSideBar.kt** (`feature/home/.../components/`)
   - 活跃小时：无背景 → `secondaryContainer` 背景 + `onSecondaryContainer` 文字
   - 当前小时：无背景 → `primary` 背景 + `onPrimary` 文字

#### 固定 UI 元素

7. **HomeScreen.kt** (`feature/home/.../ui/`)
   - BottomBarDragFab 空闲状态：`primaryContainer` / `onPrimaryContainer` → `secondaryContainer` / `onSecondaryContainer`

8. **AppBottomNavigation.kt** (`app/.../component/`)
   - `AppFloatingBottomBar` 浮动工具栏颜色：`surfaceContainerHigh` → `secondaryContainer`
   - `AppCenterFabBottomBar` 浮动工具栏颜色：`surfaceContainerHigh` → `secondaryContainer`
   - Settings 按钮背景色：`surfaceContainerHigh` → `secondaryContainer`

### 不修改的文件

- `GridCellLocked.kt` — 已使用 `surfaceDim`，符合设计
- `AppDrawer.kt` — 标准 M3 组件自动适配
- `AppTopAppBar.kt` — 保持中性 `background`
- `Theme.kt` / `ThemeConfig.kt` / `Color.kt` — 主题系统不变
- `StyleExt.kt` / `Expressiveness.kt` — 样式扩展不变
- `MomentFocusCard.kt` — 仅路由到子卡片，无颜色逻辑
- `BehaviorDetailDialog.kt` — 弹窗使用标准 M3 AlertDialog，自动适配
- `BehaviorCardContainer.kt` — `behaviorCardStyle` 是通用修饰符，颜色由调用方决定

## 约束

- 所有颜色来自 `MaterialTheme.colorScheme`，随种子色动态变化
- 不引入新的颜色 token 或硬编码颜色值（白金成就边框从硬编码改为 tertiary 是减少硬编码）
- 遵循现有的 `styledAlpha()` / `styledBorder()` / `styledCorner()` 样式扩展
- 暗色模式自动适配（MaterialKolor 会为暗色生成对应的 container 色）
- 同一状态在不同布局中的颜色必须一致（用户切换布局时体验统一）
