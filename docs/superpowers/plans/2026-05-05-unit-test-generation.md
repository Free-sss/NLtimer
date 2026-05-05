# 单元测试补全实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 根据 UNIT_TEST_AUDIT.md 审计报告，为 NLtimer 项目补全所有缺失和不完整的单元测试。

**架构：** 沿用现有 JUnit 4 + kotlinx-coroutines-test + 手动 Fake 的测试模式，按 P0→P1→P2 优先级分批生成。每个测试文件配套私有 Fake 类，使用固定时间常量避免跨日不稳定。

**技术栈：** Kotlin, JUnit 4, kotlinx-coroutines-test, 手动 Fake（无 MockK）

---

## 文件结构

### 新建测试文件（10 个）

| 文件路径 | 对应生产代码 | 优先级 | 预估用例数 |
|---------|------------|--------|-----------|
| `feature/tag_management/src/test/java/com/nltimer/feature/tag_management/viewmodel/TagManagementViewModelTest.kt` | `TagManagementViewModel.kt` | P0 | 12 |
| `feature/home/src/test/java/com/nltimer/feature/home/match/KeywordMatchStrategyTest.kt` | `KeywordMatchStrategy.kt` | P0 | 6 |
| `feature/management_activities/src/test/java/com/nltimer/feature/management_activities/viewmodel/ActivityManagementViewModelTest.kt` | `ActivityManagementViewModel.kt` | P0 | 10 |
| `core/data/src/test/java/com/nltimer/core/data/repository/impl/ActivityRepositoryImplTest.kt` | `ActivityRepositoryImpl.kt` | P0 | 8 |
| `core/data/src/test/java/com/nltimer/core/data/repository/impl/TagRepositoryImplTest.kt` | `TagRepositoryImpl.kt` | P1 | 8 |
| `core/data/src/test/java/com/nltimer/core/data/repository/impl/BehaviorRepositoryImplTest.kt` | `BehaviorRepositoryImpl.kt` | P1 | 10 |
| `core/data/src/test/java/com/nltimer/core/data/repository/impl/ActivityManagementRepositoryImplTest.kt` | `ActivityManagementRepositoryImpl.kt` | P1 | 8 |
| `core/data/src/test/java/com/nltimer/core/data/migration/CategoryMigrationValidatorTest.kt` | `CategoryMigrationValidator.kt` | P1 | 5 |
| `feature/settings/src/test/java/com/nltimer/feature/settings/ui/DialogConfigViewModelTest.kt` | `DialogConfigViewModel.kt` | P1 | 4 |

### 补充现有测试文件（4 个）

| 文件路径 | 补充用例数 | 优先级 |
|---------|-----------|--------|
| `feature/home/src/test/java/com/nltimer/feature/home/viewmodel/HomeViewModelTest.kt` | 8 | P0 |
| `feature/categories/src/test/java/com/nltimer/feature/categories/viewmodel/CategoriesViewModelTest.kt` | 5 | P1 |
| `core/data/src/test/java/com/nltimer/core/data/repository/CategoryRepositoryTest.kt` | 4 | P1 |
| `feature/settings/src/test/java/com/nltimer/feature/settings/ui/ThemeSettingsViewModelTest.kt` | 2 | P2 |

---

## 统一测试模式

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class XxxTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `specific scenario`() = runTest {
        // Given / When / Then
    }
}
```

**时间常量：** `private const val FIXED_NOW = 1_700_000_000_000L`

---

## 任务列表

### 任务 1：P0 - TagManagementViewModelTest.kt

**文件：**
- 创建：`feature/tag_management/src/test/java/com/nltimer/feature/tag_management/viewmodel/TagManagementViewModelTest.kt`

**Fake 类设计：**
- `FakeTagRepository`：维护 `MutableStateFlow<List<Tag>>`、`_categoriesFlow`，支持 insert/update/setArchived/getById/renameCategory/resetCategory
- `FakeSettingsPrefs`：维护 `MutableStateFlow<Set<String>>` 用于 tag categories

**测试用例：**
1. `initial state loading` - 验证 `uiState.isLoading = true`
2. `loadData combines tags and categories` - 验证 `combine` 后 `categories` 正确分组
3. `loadData includes added categories from settings` - 验证本地新增分类合并到列表
4. `loadData with empty repository shows empty categories` - 空数据场景
5. `addTag calls repository insert` - 验证 insert 参数
6. `updateTag calls repository update` - 验证 update 参数
7. `deleteTag calls setArchived` - 验证 archived=true
8. `moveTagToCategory with null tag does not call update` - getById 返回 null 时防御
9. `moveTagToCategory updates category` - 正常路径
10. `addCategory saves to settings` - 验证 settingsPrefs.saveTagCategories
11. `renameCategory updates settings when oldName in addedCategories` - 双写一致性
12. `deleteCategory resets tags and removes from settings` - 验证 resetCategory + settings 移除

- [ ] **步骤 1：编写 TagManagementViewModelTest.kt**
- [ ] **步骤 2：运行测试验证通过**
  运行：`./gradlew :feature:tag_management:test --tests "*TagManagementViewModelTest*"`
  预期：全部通过

---

### 任务 2：P0 - KeywordMatchStrategyTest.kt

**文件：**
- 创建：`feature/home/src/test/java/com/nltimer/feature/home/match/KeywordMatchStrategyTest.kt`

**测试用例：**
1. `empty query returns all activities` - `query = ""` 返回全部
2. `blank query returns all activities` - `query = "   "` 返回全部
3. `case insensitive match` - 大小写不敏感匹配
4. `partial match` - 部分字符串匹配
5. `no match returns empty` - 不匹配返回空列表
6. `chinese character match` - 中文字符匹配

- [ ] **步骤 1：编写 KeywordMatchStrategyTest.kt**
- [ ] **步骤 2：运行测试验证通过**
  运行：`./gradlew :feature:home:test --tests "*KeywordMatchStrategyTest*"`

---

### 任务 3：P0 - 补充 HomeViewModelTest.kt

**文件：**
- 修改：`feature/home/src/test/java/com/nltimer/feature/home/viewmodel/HomeViewModelTest.kt`

**需补充的 Fake 能力：**
- `FakeBehaviorRepository`：添加 `pendingBehaviors` 列表、`overlappingBehaviors` 列表，支持 `getPendingBehaviors`、`getBehaviorsOverlappingRange`
- `FakeActivityRepository`：添加 `allActivities` 列表，支持 `getAll`

**测试用例：**
1. `addBehavior with PENDING status uses maxSequence + 1` - PENDING 分支
2. `addBehavior with time conflict shows error` - 冲突检测路径
3. `addBehavior ACTIVE calls endCurrentBehavior with startTime` - 验证传入时间值
4. `startNextPending activates next behavior` - 启动下一个待办
5. `reorderGoals calls repository` - 重排序
6. `onHomeLayoutChange updates theme` - 验证 `theme.copy(homeLayout=...)`
7. `buildUiState with 4 columns chunked` - 网格 4 列分块
8. `buildUiState sets currentRowId for active row` - ACTIVE 行 ID 设置

- [ ] **步骤 1：补充 HomeViewModelTest.kt 用例**
- [ ] **步骤 2：运行测试验证通过**
  运行：`./gradlew :feature:home:test --tests "*HomeViewModelTest*"`

---

### 任务 4：P0 - ActivityManagementViewModelTest.kt（新建）

**文件：**
- 创建：`feature/management_activities/src/test/java/com/nltimer/feature/management_activities/viewmodel/ActivityManagementViewModelTest.kt`

**Fake 类设计：**
- `FakeActivityManagementRepository`：维护 `_activities`、`_groups`、`_stats` 三个 StateFlow，支持所有 suspend 方法

**测试用例：**
1. `initial state loading` - 验证初始状态
2. `loadData shows uncategorized activities` - 未分类活动加载
3. `addActivity adds to list` - 添加活动
4. `deleteActivity removes from list` - 删除活动
5. `addGroup creates group` - 创建分组
6. `moveActivityToGroup updates groupId` - 移动活动
7. `deleteGroup ungroups activities` - 删除分组并解绑
8. `showActivityDetail sets selectedActivityId` - 选中活动
9. `dismissDialog clears selectedActivityId` - 关闭弹窗
10. `currentActivityStats emits stats for selected activity` - 统计流切换

- [ ] **步骤 1：编写 ActivityManagementViewModelTest.kt**
- [ ] **步骤 2：运行测试验证通过**
  运行：`./gradlew :feature:management_activities:test --tests "*ActivityManagementViewModelTest*"`

---

### 任务 5：P0 - ActivityRepositoryImplTest.kt

**文件：**
- 创建：`core/data/src/test/java/com/nltimer/core/data/repository/impl/ActivityRepositoryImplTest.kt`

**Fake 类设计：**
- `FakeActivityDao`：维护 `MutableStateFlow<List<ActivityEntity>>`，支持 insert/update/delete/setArchived/getById/getByName/search/getAll/getAllActive/getByGroup/getUncategorized
- `FakeActivityGroupDao`：维护 `MutableStateFlow<List<ActivityGroupEntity>>`

**测试用例：**
1. `getAllActive maps entities to models` - 验证映射
2. `insert assigns id and stores entity` - 插入分配 ID
3. `update modifies existing entity` - 更新
4. `setArchived updates flag` - 归档
5. `getById returns mapped model` - 按 ID 查询
6. `search filters by query` - 搜索过滤
7. `getAllGroups maps group entities` - 分组映射
8. `entity to model field mapping` - 所有字段映射验证

- [ ] **步骤 1：编写 ActivityRepositoryImplTest.kt**
- [ ] **步骤 2：运行测试验证通过**
  运行：`./gradlew :core:data:test --tests "*ActivityRepositoryImplTest*"`

---

### 任务 6：P1 - TagRepositoryImplTest.kt

**文件：**
- 创建：`core/data/src/test/java/com/nltimer/core/data/repository/impl/TagRepositoryImplTest.kt`

**Fake 类设计：**
- `FakeTagDao`：维护 `MutableStateFlow<List<TagEntity>>`

**测试用例：**
1. `getAllActive maps entities to models` - 映射验证
2. `insert assigns id and stores entity` - 插入
3. `update modifies entity` - 更新
4. `setArchived updates flag` - 归档
5. `getById returns mapped model` - 按 ID 查询
6. `getDistinctCategories returns sorted unique names` - 分类去重排序
7. `renameCategory updates all matching` - 重命名分类
8. `resetCategory sets category to null` - 重置分类

- [ ] **步骤 1：编写 TagRepositoryImplTest.kt**
- [ ] **步骤 2：运行测试验证通过**
  运行：`./gradlew :core:data:test --tests "*TagRepositoryImplTest*"`

---

### 任务 7：P1 - BehaviorRepositoryImplTest.kt

**文件：**
- 创建：`core/data/src/test/java/com/nltimer/core/data/repository/impl/BehaviorRepositoryImplTest.kt`

**Fake 类设计：**
- `FakeBehaviorDao`：维护 `MutableStateFlow<List<BehaviorEntity>>`，支持所有方法
- `FakeActivityDao`：用于 `getBehaviorWithDetails`
- `FakeTagDao`：用于 `getBehaviorWithDetails`

**测试用例：**
1. `getBehaviorWithDetails returns null when behavior missing` - 行为缺失
2. `getBehaviorWithDetails returns null when activity missing` - 活动缺失
3. `getBehaviorWithDetails assembles correct object` - 正常组装
4. `insert behavior and tag cross refs` - 插入 + 标签关联
5. `completeCurrentAndStartNext with unplanned behavior` - 未计划行为
6. `completeCurrentAndStartNext with planned behavior calculates achievement` - 完成度计算
7. `completeCurrentAndStartNext with idleMode does not start next` - 空闲模式
8. `completeCurrentAndStartNext clamps endTime when now < startTime` - 时钟回拨保护
9. `completeCurrentAndStartNext returns next pending` - 启动下一个
10. `entity to model field mapping` - 字段映射

- [ ] **步骤 1：编写 BehaviorRepositoryImplTest.kt**
- [ ] **步骤 2：运行测试验证通过**
  运行：`./gradlew :core:data:test --tests "*BehaviorRepositoryImplTest*"`

---

### 任务 8：P1 - ActivityManagementRepositoryImplTest.kt

**文件：**
- 创建：`core/data/src/test/java/com/nltimer/core/data/repository/impl/ActivityManagementRepositoryImplTest.kt`

**Fake 类设计：**
- `FakeActivityDao`、`FakeActivityGroupDao`、`FakeBehaviorDao`

**测试用例：**
1. `initializePresets is idempotent` - 第二次调用幂等
2. `addGroup appends with maxOrder + 1` - 排序追加
3. `deleteGroup ungroups before delete` - 先解绑再删除
4. `getActivityStats combines three flows` - 三流合并
5. `moveActivityToGroup updates groupId` - 移动活动
6. `addActivity inserts entity` - 插入活动
7. `updateActivity updates entity` - 更新活动
8. `deleteActivity deletes by id` - 删除活动

- [ ] **步骤 1：编写 ActivityManagementRepositoryImplTest.kt**
- [ ] **步骤 2：运行测试验证通过**
  运行：`./gradlew :core:data:test --tests "*ActivityManagementRepositoryImplTest*"`

---

### 任务 9：P1 - CategoryMigrationValidatorTest.kt

**文件：**
- 创建：`core/data/src/test/java/com/nltimer/core/data/migration/CategoryMigrationValidatorTest.kt`

**Fake 类设计：**
- `FakeDataStore`：使用 `MutableStateFlow<Preferences>` 模拟 DataStore
- `FakeActivityGroupDao`：维护分组列表

**测试用例：**
1. `empty old key returns without writing` - 旧 key 为空
2. `partial existing categories only inserts diff` - 部分已存在
3. `migration removes old key after success` - 成功后删除旧 key
4. `multiple categories get correct sortOrder` - 多个分类排序
5. `no existing groups starts from sortOrder 0` - 无现有分组

- [ ] **步骤 1：编写 CategoryMigrationValidatorTest.kt**
- [ ] **步骤 2：运行测试验证通过**
  运行：`./gradlew :core:data:test --tests "*CategoryMigrationValidatorTest*"`

---

### 任务 10：P1 - DialogConfigViewModelTest.kt

**文件：**
- 创建：`feature/settings/src/test/java/com/nltimer/feature/settings/ui/DialogConfigViewModelTest.kt`

**Fake 类设计：**
- `FakeSettingsPrefs`：维护 `MutableStateFlow<DialogGridConfig>`

**测试用例：**
1. `dialogConfig emits default initially` - 初始默认值
2. `dialogConfig reflects settingsPrefs changes` - 响应变更
3. `updateConfig calls settingsPrefs` - 更新调用
4. `updateConfig with new values persists` - 新值持久化

- [ ] **步骤 1：编写 DialogConfigViewModelTest.kt**
- [ ] **步骤 2：运行测试验证通过**
  运行：`./gradlew :feature:settings:test --tests "*DialogConfigViewModelTest*"`

---

### 任务 11：P1 - 补充 CategoriesViewModelTest.kt

**文件：**
- 修改：`feature/categories/src/test/java/com/nltimer/feature/categories/viewmodel/CategoriesViewModelTest.kt`

**测试用例：**
1. `confirmAddCategory with blank name dismisses dialog` - 空白输入
2. `confirmRenameCategory case insensitive conflict` - 大小写不敏感冲突
3. `confirmRenameCategory updates addedTagCategories when oldName in set` - TAG 路径
4. `searchQuery filters tag categories too` - 搜索过滤 TAG
5. `searchQuery is case insensitive` - 大小写不敏感搜索

- [ ] **步骤 1：补充 CategoriesViewModelTest.kt**
- [ ] **步骤 2：运行测试验证通过**
  运行：`./gradlew :feature:categories:test --tests "*CategoriesViewModelTest*"`

---

### 任务 12：P1 - 补充 CategoryRepositoryTest.kt

**文件：**
- 修改：`core/data/src/test/java/com/nltimer/core/data/repository/CategoryRepositoryTest.kt`

**测试用例：**
1. `renameTagCategory does not affect tags with different category` - 部分匹配
2. `addActivityCategory with empty string` - 空字符串防御
3. `getDistinctTagCategories returns sorted` - 排序断言
4. `resetActivityCategory ungroups and deletes group` - 解绑+删除

- [ ] **步骤 1：补充 CategoryRepositoryTest.kt**
- [ ] **步骤 2：运行测试验证通过**
  运行：`./gradlew :core:data:test --tests "*CategoryRepositoryTest*"`

---

### 任务 13：P2 - 补充 ThemeSettingsViewModelTest.kt

**文件：**
- 修改：`feature/settings/src/test/java/com/nltimer/feature/settings/ui/ThemeSettingsViewModelTest.kt`

**测试用例：**
1. `theme value equals default Theme before collection` - stateIn 初始值
2. `multiple setters preserve other fields` - 字段累积保留

- [ ] **步骤 1：补充 ThemeSettingsViewModelTest.kt**
- [ ] **步骤 2：运行测试验证通过**
  运行：`./gradlew :feature:settings:test --tests "*ThemeSettingsViewModelTest*"`

---

## 全局验证

### 最终验证步骤

- [ ] **步骤 1：运行全部单元测试**
  运行：`./gradlew test`
  预期：所有模块测试通过，无失败

- [ ] **步骤 2：统计测试数量**
  预期：新增约 82 个测试用例，总用例数从 51 增至约 133

---

## 注意事项

1. **不要修改生产代码**（除非发现明显 bug）
2. **所有 Fake 类使用 `MutableStateFlow`** 模拟 Flow
3. **时间使用固定常量** `FIXED_NOW = 1_700_000_000_000L`
4. **遵循现有代码风格**：4 空格缩进、无 wildcard import、KDoc 注释
5. **每个测试文件独立运行验证**后再进行下一个
