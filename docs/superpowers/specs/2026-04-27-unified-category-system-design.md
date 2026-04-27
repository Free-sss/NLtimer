# NLtimer 统一分类体系——设计规格

**日期**: 2026-04-27  
**状态**: ✅ 已批准，待实现

---

## 1. 概述

### 1.1 背景

当前项目中存在两套分类体系，各自独立运作：

| 分类体系 | 存储方式 | 关联方式 | 使用方 |
|----------|----------|----------|--------|
| Category（字符串分类） | `activities.category: String?` | 活动属性字段 | CategoriesScreen |
| ActivityGroup（实体分组） | `activity_groups` 独立表 | `activities.groupId: Long?` 外键 | ActivityManagementScreen |

此外，Category 为支持"暂未使用的自定义分类"，将分类名以逗号分隔形式存储在 DataStore（`SettingsPrefs`）中，形成了 DB + DataStore 双数据源的局面。

**问题：**
1. `category` 字符串字段和 `groupId` 外键同时存在，概念冗余
2. DataStore 替代数据库职责存储结构化数据，属于架构异味
3. 两套分类体系的数据不同步，一个活动的分类属性与分组归属可能不一致
4. 字符串方案的可扩展性极低，无法支持排序/图标/颜色/层级等需求

### 1.2 目标

将 Activity 的分类体系统一到 `activity_groups` 实体表，让 CategoriesScreen 和 ActivityManagementScreen 共享同一个数据源。

### 1.3 核心原则

**两种 Screen，一张表，一个真相来源。**

```
                     activity_groups 表
                    (唯一的分类数据源)
                           │
           ┌───────────────┼───────────────┐
           ▼                               ▼
    CategoriesScreen              ActivityManagementScreen
    (管理分类名 CRUD)              (管理活动归属 + 分组)
```

---

## 2. 决策记录

| 决策项 | 选择 | 理由 |
|--------|------|------|
| Activity 分类存储 | `activity_groups` 实体表（方案 B） | 数据完整性、可扩展性、单数据源（详见利弊分析） |
| Tag 分类存储 | 保持现有字符串方案 | Tag 分类体系独立，本次不做变更 |
| `ActivityEntity.category` | 移除 | 统一到 `groupId` |
| DataStore 自定义分类 | 移除，数据迁移到 `activity_groups` | 消除双数据源 |
| Room 迁移 | 版本 3 → 4，手动 Migration | `category` → `groupId` 映射需业务逻辑 |
| CategoryRepository | 重构，注入 `ActivityGroupDao` 替代 `ActivityDao` 分类查询 | 数据源变更 |

---

## 3. 利弊分析（方案 A vs 方案 B）

| 维度 | 方案 A（字符串分类） | 方案 B（实体表分组） | 胜出 |
|------|:--:|:--:|------|
| 重命名效率 | ⭐⭐ O(N) 全表更新 | ⭐⭐⭐⭐⭐ O(1) 单行更新 | B |
| 删除安全 | ⭐⭐⭐ | ⭐⭐⭐⭐ ON DELETE SET NULL | B |
| 新建暂未使用分类 | ⭐⭐ 依赖 DataStore hack | ⭐⭐⭐⭐⭐ 直接 INSERT | B |
| 引用完整性 | ⭐ 无约束 | ⭐⭐⭐⭐⭐ ForeignKey | B |
| 可扩展性（排序/图标/层级） | ⭐ 无法扩展 | ⭐⭐⭐⭐⭐ 字段随意加 | B |
| 查询性能 | ⭐⭐⭐ 字符串匹配 | ⭐⭐⭐⭐ 整数索引 | B |
| 存储开销 | ⭐⭐⭐⭐ 略低 | ⭐⭐⭐ 多一张小表 | A（忽略不计）|
| 数据源单一性 | ⭐ 两个数据源 | ⭐⭐⭐⭐⭐ 一个数据源 | B |

**结论：方案 B 在 7/8 个维度上优于方案 A。**

---

## 4. 数据库 Schema 变更

### 4.1 ActivityEntity（修改）

```kotlin
// 之前
@Entity(tableName = "activities")
data class ActivityEntity(
    val category: String? = null,   // ❌ 删除
    val groupId: Long? = null,
    // ...
)

// 之后
@Entity(tableName = "activities")
data class ActivityEntity(
    val groupId: Long? = null,       // ✅ 唯一分类关联字段
    // ...
)
```

### 4.2 activity_groups 表（不变）

```kotlin
@Entity(tableName = "activity_groups")
data class ActivityGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)
```

### 4.3 Room Migration（3 → 4）

```kotlin
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Step 1: 将 category 字符串映射为 activity_groups 行
        db.execSQL("""
            INSERT INTO activity_groups (name, sortOrder, createdAt)
            SELECT DISTINCT category, 0, ${System.currentTimeMillis()}
            FROM activities
            WHERE category IS NOT NULL AND category != ''
              AND category NOT IN (SELECT name FROM activity_groups)
        """)

        // Step 2: 设置 activities.groupId 指向对应分组
        db.execSQL("""
            UPDATE activities SET groupId = (
                SELECT ag.id FROM activity_groups ag
                WHERE ag.name = activities.category
            ) WHERE category IS NOT NULL AND category != ''
        """)
        // Note: category 列由 Room 在 entity 变更后自动处理 DROP
    }
}
```

**最终方案**：项目处早期开发阶段，使用 `fallbackToDestructiveMigration()` 简化处理。Room 版本升级时若 schema 不匹配（Entity 移除字段后），会自动重建数据库，结合 Migration 中的数据映射逻辑（已在 Step 1 和 Step 2 中将 category 数据迁移到 activity_groups），数据不会丢失。

`NLtimerDatabase.kt` 中保持现有配置：
```kotlin
Room.databaseBuilder(context, NLtimerDatabase::class.java, "nltimer-database")
    .fallbackToDestructiveMigration(false)  // 保持 false，由 Migration 处理
    .addMigrations(MIGRATION_3_4)
    .build()
```

Migration 中仅需上述 Step 1 + Step 2（数据映射），Room 在后续的 schema 验证中检测到 `category` 列已从 Entity 中移除，会尝试 ALTER TABLE DROP COLUMN（API 33+ 的 Room 支持），低版本则由 Migration 兜底。

---

## 5. DAO 变更

### 5.1 ActivityDao（删除 3 个分类方法 + getByCategory 改为按 groupId 查）

| 方法 | 操作 | 说明 |
|------|------|------|
| `getDistinctCategories()` | **删除** | 不再从 activity.category 做 DISTINCT |
| `renameCategory(old, new)` | **删除** | 改为 ActivityGroupDao.renameByName() |
| `resetCategory(category)` | **删除** | 改为 ActivityGroupDao.deleteByName() |
| `getByCategory(category)` | **删除** | 改为 getByGroup(groupId)，已存在 |

### 5.2 ActivityGroupDao（新增 3 个方法）

```kotlin
// 新增：按名查询
@Query("SELECT * FROM activity_groups WHERE name = :name LIMIT 1")
suspend fun getByName(name: String): ActivityGroupEntity?

// 新增：按名重命名
@Query("UPDATE activity_groups SET name = :newName WHERE name = :oldName")
suspend fun renameByName(oldName: String, newName: String)

// 新增：按名删除（先解绑活动）
@Query("DELETE FROM activity_groups WHERE name = :name")
suspend fun deleteByName(name: String)
```

**注意**：`deleteByName` 前需先调用 `ungroupAllActivities()` 将活动归入未分类。

### 5.3 TagDao（不变）

Tag 的 `category` 字段和 `getDistinctCategories()`、`renameCategory()`、`resetCategory()` 保持不变。Tag 分类体系独立运作。

---

## 6. CategoryRepository 重构

### 6.1 接口变更

```kotlin
interface CategoryRepository {
    // Activity 分类 —— 改为对接 activity_groups 表
    fun getDistinctActivityCategories(parent: String? = null): Flow<List<String>>
    suspend fun addActivityCategory(name: String)                          // ← 新增
    suspend fun renameActivityCategory(oldName: String, newName: String, parent: String? = null)
    suspend fun resetActivityCategory(category: String)

    // Tag 分类 —— 保持不变，继续走字符串方案
    fun getDistinctTagCategories(parent: String? = null): Flow<List<String>>
    suspend fun renameTagCategory(oldName: String, newName: String, parent: String? = null)
    suspend fun resetTagCategory(category: String)
}
```

### 6.2 实现变更

```kotlin
@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val groupDao: ActivityGroupDao,    // ← 替换 activityDao
    private val tagDao: TagDao,
) : CategoryRepository {

    override fun getDistinctActivityCategories(parent: String?): Flow<List<String>> =
        groupDao.getAll().map { groups ->
            groups.map { it.name }.sorted()
        }

    override suspend fun addActivityCategory(name: String) {
        val existing = groupDao.getAll().first()
        val maxOrder = existing.maxOfOrNull { it.sortOrder } ?: -1
        groupDao.insert(ActivityGroupEntity(name = name, sortOrder = maxOrder + 1))
    }

    override suspend fun renameActivityCategory(oldName: String, newName: String, parent: String?) {
        groupDao.renameByName(oldName, newName)
    }

    override suspend fun resetActivityCategory(category: String) {
        val group = groupDao.getByName(category) ?: return
        groupDao.ungroupAllActivities(group.id)
        groupDao.delete(group)
    }

    // Tag 相关实现不变
    // ...
}
```

---

## 7. SettingsPrefs 清理

### 7.1 接口删除

```kotlin
interface SettingsPrefs {
    fun getThemeFlow(): Flow<Theme>
    suspend fun updateTheme(theme: Theme)

    // ❌ 删除 Activity 分类相关方法（统一到 activity_groups 表）
    // fun getSavedActivityCategories(): Flow<Set<String>>
    // suspend fun saveActivityCategories(categories: Set<String>)

    // ✅ Tag 分类相关方法暂保留（Tag 仍走字符串方案）
    fun getSavedTagCategories(): Flow<Set<String>>
    suspend fun saveTagCategories(categories: Set<String>)
}
```

### 7.2 实现删除（SettingsPrefsImpl）

删除 Activity 分类相关的 DataStore keys 和实现方法：
- `savedActivityCategoriesKey`（删除）
- `getSavedActivityCategories()`（删除）
- `saveActivityCategories()`（删除）

保留 Tag 分类相关实现不变：
- `savedTagCategoriesKey`（保留）
- `getSavedTagCategories()`（保留）
- `saveTagCategories()`（保留）

### 7.3 App 层 DataModule

`app/src/main/java/.../app/di/DataModule.kt` 中 `provideSettingsPrefs()` 保持不变，因为 SettingsPrefsImpl 仍然需要注入 DataStore 来管理主题。

---

## 8. CategoriesViewModel 简化

### 8.1 变更点

| 变更 | 说明 |
|------|------|
| 保留 `settingsPrefs` 依赖 | 仅用于 Tag 维度的自定义分类持久化 |
| 删除 `_addedActivityCategories` | Activity 分类直接在 activity_groups 表中 |
| 保留 `_addedTagCategories` | Tag 分类仍走 DataStore |
| 删除 `mergeAndSort()` | Activity 分类不需要合并两个数据源 |
| `confirmAddCategory` Activity 分支 | 调用 `categoryRepository.addActivityCategory(name)` 替代 DataStore 写入 |
| `confirmRenameCategory` Activity 分支 | 移除 `_addedActivityCategories` 相关逻辑 |
| `confirmDeleteCategory` Activity 分支 | 移除 `_addedActivityCategories` 相关逻辑 |

### 8.2 简化后的 ViewModel 核心

```kotlin
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val settingsPrefs: SettingsPrefs,   // 仅用于 Tag 维度
) : ViewModel() {

    private val _addedTagCategories = MutableStateFlow<Set<String>>(emptySet())

    init {
        viewModelScope.launch {
            _addedTagCategories.value = settingsPrefs.getSavedTagCategories().first()
        }
    }

    val uiState: StateFlow<CategoriesUiState> = combine(
        categoryRepository.getDistinctActivityCategories(),
        categoryRepository.getDistinctTagCategories(),
        _searchQuery,
        _dialogState,
        _addedTagCategories,
    ) { activityCats, tagCats, query, dialog, addedTag ->
        val mergedTag = (tagCats + addedTag).distinct().sorted()
        CategoriesUiState(
            activityCategories = if (query.isBlank()) activityCats
                else activityCats.filter { it.contains(query, ignoreCase = true) },
            tagCategories = if (query.isBlank()) mergedTag
                else mergedTag.filter { it.contains(query, ignoreCase = true) },
            searchQuery = query,
            isLoading = false,
            dialogState = dialog,
        )
    }.stateIn(...)

    fun confirmAddCategory(sectionType: SectionType, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) { dismissDialog(); return }
        viewModelScope.launch {
            when (sectionType) {
                SectionType.ACTIVITY -> categoryRepository.addActivityCategory(trimmed)
                SectionType.TAG -> {
                    val updated = _addedTagCategories.value + trimmed
                    _addedTagCategories.value = updated
                    settingsPrefs.saveTagCategories(updated)
                }
            }
            dismissDialog()
        }
    }
}
```

---

## 9. 其他受影响的文件

### 9.1 Activity 领域模型

[core/data/model/Activity.kt](file:///d:/2026Code/Group_android/NLtimer/core/data/src/main/java/com/nltimer/core/data/model/Activity.kt) 移除 `category` 字段：

```kotlin
data class Activity(
    val id: Long = 0,
    val name: String,
    val emoji: String? = null,
    val iconKey: String? = null,
    // val category: String? = null,  ← 删除
    val groupId: Long? = null,
    // ...
)
```

对应的 `toEntity()` 和 `fromEntity()` 也移除 `category` 映射。

### 9.2 BehaviorRepositoryImpl

[BehaviorRepositoryImpl.kt](file:///d:/2026Code/Group_android/NLtimer/core/data/src/main/java/com/nltimer/core/data/repository/impl/BehaviorRepositoryImpl.kt) 中 `getBehaviorWithDetails()` 方法构建 `Activity` 时移除 `category` 参数（约第 64 行）。

### 9.3 CategoryRepository 测试

[CategoryRepositoryTest.kt](file:///d:/2026Code/Group_android/NLtimer/core/data/src/test/java/com/nltimer/core/data/repository/CategoryRepositoryTest.kt) 需要完全重写：
- 移除 `fakeActivityDao` 中的 `getDistinctCategories`/`renameCategory`/`resetCategory` mock
- 新增 `fakeActivityGroupDao` mock
- 重写所有 Activity 分类相关测试用例

### 9.4 CategoriesViewModel 测试

[CategoriesViewModelTest.kt](file:///d:/2026Code/Group_android/NLtimer/feature/categories/src/test/java/com/nltimer/feature/categories/viewmodel/CategoriesViewModelTest.kt) 需要更新：
- 移除 Activity 分类的 DataStore 持久化测试（`confirmAdd_persistsToDataStore` 中验证 `lastSavedActivityCategories`）
- 保留 Tag 分类的 DataStore 持久化测试（`lastSavedTagCategories`）
- 新增 `CategoryRepository.addActivityCategory` 调用验证测试
- 更新 `FakeCategoryRepository` 以匹配新接口（新增 `addActivityCategory`）

### 9.5 CategoriesScreen（不变）

Compose UI 层不需要改动，因为 ViewModel 对外暴露的 `CategoriesUiState` 接口不变——仍然是一个 `List<String>`。

---

## 10. 数据迁移完整性检查（运行时）

```kotlin
class CategoryMigrationValidator @Inject constructor(
    private val settingsPrefs: SettingsPrefs,
    private val groupDao: ActivityGroupDao,
) {
    suspend fun migrateIfNeeded() {
        val savedCategories = settingsPrefs.getSavedActivityCategories().first()
        if (savedCategories.isEmpty()) return

        val existingNames = groupDao.getAll().first().map { it.name }.toSet()
        savedCategories
            .filter { it !in existingNames }
            .forEach { name ->
                groupDao.insert(ActivityGroupEntity(name = name))
            }

        settingsPrefs.saveActivityCategories(emptySet())
        settingsPrefs.saveTagCategories(emptySet())
    }
}
```

在 `NLtimerApplication.onCreate()` 或对应的初始化点中调用 `migrateIfNeeded()`。

---

## 11. 文件变更清单

| 模块 | 文件 | 操作 |
|------|------|------|
| `core:data` | `database/entity/ActivityEntity.kt` | **修改**：移除 `category` 字段 |
| `core:data` | `database/dao/ActivityDao.kt` | **修改**：删除 `getDistinctCategories`/`renameCategory`/`resetCategory`/`getByCategory` |
| `core:data` | `database/dao/ActivityGroupDao.kt` | **修改**：新增 `getByName`/`renameByName`/`deleteByName` |
| `core:data` | `database/NLtimerDatabase.kt` | **修改**：版本号 3→4，新增 Migration |
| `core:data` | `model/Activity.kt` | **修改**：移除 `category` 字段及映射 |
| `core:data` | `repository/CategoryRepository.kt` | **修改**：接口新增 `addActivityCategory()` |
| `core:data` | `repository/impl/CategoryRepositoryImpl.kt` | **修改**：注入 `ActivityGroupDao` 替代 `ActivityDao` |
| `core:data` | `repository/impl/BehaviorRepositoryImpl.kt` | **修改**：移除 Activity 构造中的 `category` 参数 |
| `core:data` | `SettingsPrefs.kt` | **修改**：删除 4 个分类方法 |
| `core:data` | `SettingsPrefsImpl.kt` | **修改**：删除分类相关实现 |
| `core:data` | **新建** `migration/CategoryMigrationValidator.kt` | **新建**：DataStore 数据迁移 |
| `core:data` | `di/DataModule.kt` | 可能需要调整 |
| `core:data` | `test/.../CategoryRepositoryTest.kt` | **重写** |
| `feature:categories` | `viewmodel/CategoriesViewModel.kt` | **修改**：删除 DataStore 依赖，简化逻辑 |
| `feature:categories` | `test/.../CategoriesViewModelTest.kt` | **重写** |
| `feature:management` | 不受影响 | ✅ 已在用 activity_groups |
| `app` | `di/DataModule.kt` | 可能需要调整 |

---

## 12. 验收标准

### 功能验收
- [ ] CategoriesScreen 显示的分类列表来自 `activity_groups` 表
- [ ] CategoriesScreen 新建分类 → `activity_groups` 表新增一行
- [ ] CategoriesScreen 重命名分类 → `activity_groups.name` 更新，主界面分组卡片同步反映
- [ ] CategoriesScreen 删除分类 → `activity_groups` 行删除，活动移至未分组
- [ ] ActivityManagementScreen 分组卡片与 CategoriesScreen 的分类列表完全一致
- [ ] Room 数据库升级无 crash，现有数据正确迁移
- [ ] DataStore 旧数据迁移到 activity_groups 表后清空

### 代码质量验收
- [ ] `ActivityEntity.category` 字段完全移除
- [ ] `SettingsPrefs` 接口中无分类相关方法
- [ ] CategoryRepository 测试通过
- [ ] CategoriesViewModel 测试通过
- [ ] 编译无错误

---

## 13. 风险与缓解

| 风险 | 缓解措施 |
|------|---------|
| Room Migration 数据丢失 | Migration 中先 INSERT 再 UPDATE groupId，最后 DROP category |
| 重命名冲突（与已有分组重名） | ViewModel 层保留冲突检测逻辑，查询 `activity_groups.name` 判重 |
| BehaviorRepository 使用了 Activity.category | 全面搜索 `.category` 引用，确保编译通过 |
| Tag 的 DataStore 逻辑不能直接删 | Tag 维度的 SettingsPrefs 方法暂保留，等后续统一 |
| Tag 分类也需要统一但本次不做 | 记录为后续优化项 |

---

**文档版本**: 1.0  
**作者**: AI Assistant
