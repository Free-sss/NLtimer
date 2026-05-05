# 单元测试缺失与不完整性审计报告

> 生成日期：2026-05-05
> 审计范围：`D:\2026Code\Group_android\NLtimer`（不含 `.worktrees\test-generation\` 镜像副本）
> 审计目标：识别项目中 **缺少**、**不完整** 或 **不可用** 的单元测试，为后续补全测试提供清单依据。
> 用户原始指令："查看这个项目哪里缺少单元测试，或者是其不完整、不可用，将有问题的地方输出到文档"

---

## 0. 总览

| 模块 | 生产代码 (.kt) | 单元测试 (.kt) | 状态 |
|------|---------------:|---------------:|------|
| `app` | 10 | **0** | ❌ 完全缺失 |
| `core/data` | 36 | 4（含 2 个 mock） | ⚠️ 严重不足（仅 `CategoryRepositoryImpl` + `TimeConflictUtils`） |
| `core/designsystem` | 22 | 0 | ⚠️ 主体为 UI/纯数据可豁免；`EnumExt`、`ListItemExt` 等含逻辑应测 |
| `feature/categories` | 5 | 1 | ✅ 较好，少量分支未覆盖 |
| `feature/debug` | 17 | 0 | ⚠️ `DebugDatabaseHelper` 含逻辑应测 |
| `feature/home` | 38 | 1 | ⚠️ ViewModel 测试覆盖不全；`KeywordMatchStrategy` 完全无测试 |
| `feature/management_activities` | 12 | 1 | ⚠️ 主要分支已测，部分对话框/统计流未覆盖 |
| `feature/settings` | 5 | 1 | ⚠️ `DialogConfigViewModel` 完全无测试 |
| `feature/stats` | 1 | 0 | ✅ 仅 UI，可豁免 |
| `feature/sub` | 1 | 0 | ✅ 仅 UI，可豁免 |
| `feature/tag_management` | 9 | **0** | ❌ `TagManagementViewModel` 200+ 行业务逻辑零测试 |

> 仓库中存在 `.worktrees/test-generation/` 工作树镜像，与主分支测试文件 **完全重复**。这不是问题，但需要在 CI 中确认只跑主工作树以避免双跑。

---

## 1. 完全没有单元测试的关键生产代码

以下文件包含明确的业务逻辑（非纯 UI / 纯数据类），但 **目前没有任何测试**。按风险等级排序：

### 1.1 🔴 高风险 — 必须补测

#### A. `feature/tag_management/viewmodel/TagManagementViewModel.kt`
- **代码规模**：215 行，10 余个公开方法
- **关键逻辑**：
  - `loadData()` 中 `combine` 三个 Flow（标签、分类、本地新增分类）合并去重排序
  - `addCategory` / `renameCategory` / `deleteCategory` 同时操作 `tagRepository` 与 `settingsPrefs`，存在双写一致性风险
  - `moveTagToCategory` 的 null-tag 防御分支
- **影响**：标签管理是核心功能；与 `CategoriesViewModel` 重叠的「本地新增分类」逻辑是历史 bug 高发区。
- **建议**：参照同模块 `CategoriesViewModelTest` 的 Fake 模式新建 `TagManagementViewModelTest`，覆盖以下场景：
  - 初始加载将本地新增分类合并到 `categories` 列表
  - `renameCategory` 同时更新 DB 和本地集合（且仅当 `oldName in _addedCategories`）
  - `deleteCategory` 同时调用 `resetCategory` 并从本地集合移除
  - `loadData` Flow 异常 → `isLoading = false`
  - `moveTagToCategory` 当 `getById` 返回 null 时不调用 `update`

#### B. `feature/home/match/KeywordMatchStrategy.kt`
- **代码规模**：28 行（小但纯函数，是测试性价比最高的目标）
- **关键逻辑**：大小写不敏感关键词模糊匹配；空 query 全量返回
- **影响**：`HomeViewModel` 通过 `MatchStrategy` 注入；当前 `HomeViewModelTest` 直接 `new KeywordMatchStrategy()` 但从未触发匹配路径。
- **建议**：新建 `KeywordMatchStrategyTest`：空 query、纯空格 query、英文大小写、中文混合、不匹配返回空列表。

#### C. `core/data/repository/impl/BehaviorRepositoryImpl.kt`
- **代码规模**：236 行
- **关键逻辑**：
  - `completeCurrentAndStartNext` —— 包含 **核心算法**：
    - `clampedEndTime = now.coerceAtLeast(startTime)` 时钟回拨保护
    - `wasPlanned` 行为按 `(1 - |duration-estimated|/estimated)*100` 计算 `achievementLevel`
    - `idleMode = true` 不启动下一个；否则寻找 next pending 并设为 ACTIVE
  - `insert` 行为 + 批量插入 tag cross-ref
  - `getBehaviorWithDetails` 跨三表组装聚合对象，任一为空则返回 null
- **影响**：番茄/计时核心逻辑。`achievementLevel` 计算公式错误会直接污染历史数据。
- **建议**：以伪 Dao 单测 `BehaviorRepositoryImpl`，重点覆盖：
  - `completeCurrentAndStartNext` 三种场景：未计划行为、有估算的计划行为、idleMode
  - `getBehaviorWithDetails`：行为/活动任一缺失时返回 null
  - `clampedEndTime` 在 `now < startTime` 时取 `startTime`（时钟回拨）

#### D. `core/data/repository/impl/ActivityManagementRepositoryImpl.kt`
- **代码规模**：120 行
- **关键逻辑**：
  - `addGroup` 计算 `maxOrder + 1` 实现追加排序
  - `deleteGroup` 先 `ungroupAllActivities` 再删分组（顺序敏感）
  - `initializePresets` 仅在 `getAllPresets().first().isEmpty()` 时插入，否则幂等跳过
  - `getActivityStats` 三 Flow `combine` 拼装统计
- **影响**：预设活动重复插入会污染用户库；分组顺序错乱影响 UI 排序。
- **建议**：`ActivityManagementRepositoryImplTest` 覆盖：
  - `initializePresets` 第二次调用是幂等的
  - `addGroup` 在 `groups` 已有 sortOrder=2 时返回 sortOrder=3 的分组
  - `deleteGroup` 调用顺序：先 `ungroupAllActivities` 再 `delete`

#### E. `core/data/migration/CategoryMigrationValidator.kt`
- **代码规模**：49 行
- **关键逻辑**：
  - 旧 DataStore 字符串 → CSV 拆分 → 过滤已存在 → 批量插入 → 删除旧 key
  - `maxOrder` 累加保证多个新分组排序正确
- **影响**：版本升级路径，仅执行一次但失败后用户分类丢失。
- **建议**：`CategoryMigrationValidatorTest` 覆盖：
  - 旧 key 为空时直接返回，不写入数据库
  - 部分分类已存在时只插入差集
  - 迁移成功后 `savedActivityCategoriesKey` 被移除

### 1.2 🟡 中风险 — 应该补测

#### F. `feature/settings/ui/DialogConfigViewModel.kt`
- 只有 `dialogConfig` StateFlow + `updateConfig`，但仍是公开 API。
- **建议**：1 个测试文件，3-4 个用例，工作量 < 30 分钟。

#### G. `core/data/repository/impl/TagRepositoryImpl.kt` & `ActivityRepositoryImpl.kt`
- 大部分方法是 Dao 直通转换。**关键风险点**：
  - `TagEntity ↔ Tag` / `ActivityEntity ↔ Activity` 字段映射函数。新增字段时容易漏映射。
- **建议**：每个 Impl 写 1 个 round-trip 测试 + 几个 toModel/toEntity 字段覆盖测试。

#### H. `core/data/SettingsPrefsImpl.kt`
- DataStore 序列化逻辑（`Theme`、`DialogGridConfig`、`TimeLabelConfig`、`Set<String>`）。
- 注：DataStore 测试通常需要 `androidTest` 或临时文件 DataStore，复杂度较高，**可标记为低优先**。

#### I. `core/data/database/dao/*.kt`（4 个 DAO）
- Room 抽象类，需要 `androidTest` 通过 `Room.inMemoryDatabaseBuilder` 测试。
- 项目目前 **没有任何 `src/androidTest/`** 目录。
- **建议**：至少为 `BehaviorDao`（`getBehaviorsOverlappingRange`、`getMaxSequence`、`endCurrentBehavior` 等含 SQL 逻辑的方法）补 androidTest。

### 1.3 🟢 低风险 — 可豁免或低优先

- `app/**` — 仅 Compose 路由 / Application 入口，建议改用 UI 测试或快照测试，不在单元测试范围内。
- `feature/debug/**` — 仅 debug 工具，但 `DebugDatabaseHelper.kt` 如有 SQL/导出逻辑应单独评估。
- `feature/stats/StatsScreen.kt` & `feature/sub/SubScreen.kt` — 纯 Compose，无 ViewModel。
- `core/designsystem/**` — 主要是主题与样式；`EnumExt.kt` / `ListItemExt.kt` 如含 `fun ... when ...` 转换可补 1-2 个测试。

---

## 2. 已存在但 **不完整** 的测试

### 2.1 `feature/home/src/test/.../HomeViewModelTest.kt`

**已覆盖**（10 个 case）：
- 初始 loading 状态
- `addActivity` / `addTag` 调用 repository
- `showAddSheet` / `hideAddSheet` 切换
- `onActivitySelected` 加载标签
- `addBehavior` 基本 ACTIVE 场景
- 时间约束错误（COMPLETED endTime 未来、ACTIVE startTime 未来）
- sequence 重排序（COMPLETED 插在前面时已有行为 sequence+1）
- `completeBehavior` / `toggleIdleMode` / `deleteBehavior`

**未覆盖的关键分支**：

| # | 缺失场景 | 风险 |
|---|---------|------|
| 1 | **PENDING 行为分支**（`getMaxSequence + 1`、`startTime = 0L`、不做时间冲突检测） | 🔴 |
| 2 | **时间冲突错误路径** —— `addBehavior` 检测 `hasTimeConflict` 后设置 `errorMessage = "该时间段与已有行为记录冲突"` | 🔴 |
| 3 | **ACTIVE 路径调用 `endCurrentBehavior(startTime)`** —— 现有测试虽断言 `endCurrentBehaviorCalled`，但传入的时间值未断言 | 🟡 |
| 4 | `startNextPending()` —— 整个方法零覆盖 | 🟡 |
| 5 | `reorderGoals(orderedIds)` | 🟡 |
| 6 | `onHomeLayoutChange` —— 验证 `theme.copy(homeLayout=...)` 写回 | 🟡 |
| 7 | `onTimeLabelConfigChange` | 🟢 |
| 8 | `scrollToTime(hour)` | 🟢 |
| 9 | `buildUiState` 的网格构建：4 列 chunked、不足 4 列填充空 cell、`currentRowId` 在含 ACTIVE 行的行 ID 设置正确 | 🟡 |
| 10 | `loadHomeBehaviors` Flow 集成：当 `getHomeBehaviors` 流出新数据时 `uiState.rows` 重建 | 🟡 |

### 2.2 `feature/categories/src/test/.../CategoriesViewModelTest.kt`

**已覆盖**（17 个 case，质量较高）。

**未覆盖的关键分支**：

| # | 缺失场景 | 风险 |
|---|---------|------|
| 1 | `confirmAddCategory` 输入空白字符串 / 全空格 → 应直接 `dismissDialog()` 不写入 | 🟡 |
| 2 | `confirmRenameCategory` **大小写不敏感** 冲突检测（`equals(newName, ignoreCase=true)`），目前测试只用了完全相等的名字 | 🟡 |
| 3 | `confirmRenameCategory` TAG 路径：当 `oldName` 在本地新增集合中时同步更新 `_addedTagCategories` | 🟡 |
| 4 | 搜索 query 同时过滤 **TAG** 类别（现有测试只断言活动） | 🟢 |
| 5 | `searchQuery` 大小写不敏感（用 `contains(query, ignoreCase=true)`） | 🟢 |

### 2.3 `feature/management_activities/src/test/.../ActivityManagementViewModelTest.kt`

**已覆盖**（13 个 case）。

**未覆盖的关键分支**：

| # | 缺失场景 | 风险 |
|---|---------|------|
| 1 | `currentActivityStats` —— `_selectedActivityId` 变化时 `flatMapLatest` 切换到对应活动的统计流；`null` 时返回默认 `ActivityStats()` | 🔴 |
| 2 | `showActivityDetail` —— 同时设置 `_selectedActivityId` 和 `dialogState`；`dismissDialog` 应清空 `_selectedActivityId` | 🟡 |
| 3 | `showAddActivityToGroupDialog`、`showEditActivityDialog`、`showDeleteActivityDialog`、`showRenameGroupDialog`、`showAddGroupDialog` 五个对话框入口未单测 | 🟢 |
| 4 | `initializePresets` 在 init 中触发 —— 验证调用一次 | 🟡 |
| 5 | `loadData` Flow `.catch{}` 异常路径 —— 模拟 repository 抛错，确认 `isLoading=false` | 🟡 |
| 6 | 分组活动列表的实时更新 —— 当 `getActivitiesByGroup(groupId)` 流出新数据时 `groups[i].activities` 同步更新 | 🟡 |

### 2.4 `feature/settings/src/test/.../ThemeSettingsViewModelTest.kt`

**已覆盖**（9 个 setter 测试）。

**评估**：该 ViewModel 几乎是 `settingsPrefs` 的薄壳，现有测试已充分。但 `theme` StateFlow 的初始 `Theme()` 默认值在订阅前的语义未显式断言。

**建议补充**：

| # | 缺失场景 | 风险 |
|---|---------|------|
| 1 | 未订阅时 `theme.value` 等于默认 `Theme()`（`stateIn` 初始值语义） | 🟢 |
| 2 | 多个 setter 顺序调用后 theme 字段全部累积保留（仅改单字段不重置其他字段） | 🟢 |

### 2.5 `core/data/src/test/.../CategoryRepositoryTest.kt`

**已覆盖**（7 个 case，质量较好）。

**未覆盖的关键分支**：

| # | 缺失场景 | 风险 |
|---|---------|------|
| 1 | `renameTagCategory` 部分匹配：不属于该分类的标签 `category` 字段不变（现有测试只断言新分类存在） | 🟡 |
| 2 | `addActivityCategory("")` 空字符串 / 重复名称的行为（当前 Impl 是否做防御未知） | 🟢 |
| 3 | `getDistinctTagCategories` 排序顺序断言 | 🟢 |

### 2.6 `core/data/src/test/.../TimeConflictUtilsTest.kt`

**已覆盖**（12 个 case，覆盖度优秀，几乎是教科书级别）。

**未覆盖的次要分支**：

| # | 缺失场景 | 风险 |
|---|---------|------|
| 1 | `existingBehaviors` 包含多个行为时，命中其中之一即返回 true | 🟢 |
| 2 | `newStart > newEnd`（非法输入）的行为定义 | 🟢 |
| 3 | `ignoreBehaviorId` 与多个相同 id 的边缘情况 | 🟢 |

---

## 3. 测试基础设施问题

### 3.1 `.worktrees/test-generation/` 镜像重复
- 该子目录是一个 git worktree，包含与主工作树 **完全相同** 的测试文件 7 份。
- **建议**：在根 `build.gradle.kts` 或 `settings.gradle.kts` 中显式排除 `.worktrees/**`，避免 Gradle 误识别为同名模块；或在 CI 中 `--exclude-task ":worktrees:..."`。

### 3.2 缺少 `androidTest/` 目录
- 全项目无 instrumentation 测试。Room DAO（`BehaviorDao`、`ActivityDao`、`TagDao`、`ActivityGroupDao`）的 SQL 语句、@Transaction 行为无法仅通过单元测试覆盖。
- **建议**：在 `core/data/` 下创建 `src/androidTest/` 并使用 `Room.inMemoryDatabaseBuilder` 测试 DAO 关键查询。

### 3.3 测试依赖配置
- 各 feature 模块各自维护一份 Fake（如 `FakeSettingsPrefs` 在 `Categories`、`Home`、`ThemeSettings` 三处重复实现）。
- **建议**：在 `core/data/src/test/java/.../mock/` 下已存在的 `MockData.kt` / `TestDataFactory.kt` 旁，增加 `FakeSettingsPrefs` / `FakeBehaviorRepository` 等共享 Fake，并通过 `testImplementation project(":core:data")` 让其他模块复用（或单独建一个 `core/testing` 模块）。

### 3.4 `kotlinx.coroutines.flow.collect` 已弃用
- `ThemeSettingsViewModelTest.kt:17` 仍在 `import kotlinx.coroutines.flow.collect`。新版 Kotlin 编译器对该函数发出警告，且 `Flow.collect()` 的扩展点已变更。
- **建议**：替换为 `viewModel.theme.first()` 或 `.toList(...)`。

### 3.5 测试文件硬编码当前时间
- `HomeViewModelTest.kt` 使用 `System.currentTimeMillis()` 与 `LocalDate.now()` 构造测试数据，导致测试 **跨日运行可能不稳定**（如运行时跨越午夜）。
- **建议**：注入 `Clock` 或固定时间常量。

---

## 4. 优先级补测路线图

| 阶段 | 内容 | 估算工作量 |
|------|------|-----------|
| **P0** | `TagManagementViewModelTest`（新建） | 4-6 小时 |
| **P0** | `KeywordMatchStrategyTest`（新建） | 30 分钟 |
| **P0** | `BehaviorRepositoryImplTest` 覆盖 `completeCurrentAndStartNext` 算法 | 4 小时 |
| **P0** | `HomeViewModelTest` 补 PENDING 分支与时间冲突错误路径 | 2 小时 |
| **P1** | `ActivityManagementRepositoryImplTest`（新建） | 2 小时 |
| **P1** | `CategoryMigrationValidatorTest`（新建） | 1.5 小时 |
| **P1** | `ActivityManagementViewModelTest` 补 `currentActivityStats` 与对话框 | 2 小时 |
| **P1** | `DialogConfigViewModelTest`（新建） | 30 分钟 |
| **P2** | `TagRepositoryImplTest` / `ActivityRepositoryImplTest`（实体映射） | 2 小时 |
| **P2** | `androidTest/` 起步 + 关键 DAO 测试（`BehaviorDao`） | 6-8 小时 |
| **P2** | 共享 Fake 提取到 `core/testing` 模块 | 3 小时 |
| **P3** | `CategoriesViewModelTest` / `CategoryRepositoryTest` 补缺失分支 | 1.5 小时 |

---

## 5. 附录：当前测试文件清单

```
core/data/src/test/java/com/nltimer/core/data/
├── mock/
│   ├── MockData.kt              (mock 数据)
│   └── TestDataFactory.kt       (mock 工厂)
├── repository/
│   └── CategoryRepositoryTest.kt
└── util/
    └── TimeConflictUtilsTest.kt

feature/categories/src/test/java/com/nltimer/feature/categories/viewmodel/
└── CategoriesViewModelTest.kt   (17 cases)

feature/home/src/test/java/com/nltimer/feature/home/viewmodel/
└── HomeViewModelTest.kt         (10 cases)

feature/management_activities/src/test/java/com/nltimer/feature/management_activities/viewmodel/
└── ActivityManagementViewModelTest.kt (13 cases)

feature/settings/src/test/java/com/nltimer/feature/settings/ui/
└── ThemeSettingsViewModelTest.kt (9 cases)
```

**测试文件总计**：5 个测试文件 + 2 个 mock 工具，约 **51 个测试用例**。
**生产 Kotlin 文件总计**：约 **156 个**（不含 worktree 副本）。
**估算覆盖率（按文件粒度）**：5/120 ≈ **4.2%**（剔除纯 UI/纯数据类后应约 15-20%）。

---

*报告结束。建议结合 JaCoCo 覆盖率报告（如未启用，建议在各 module `build.gradle.kts` 中加入）做精确度量。*
