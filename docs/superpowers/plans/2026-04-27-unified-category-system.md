# 统一分类体系 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将 Activity 分类统一到 `activity_groups` 实体表，移除 `ActivityEntity.category` 字段和 DataStore 分类存储，让 CategoriesScreen 和 ActivityManagementScreen 共享同一数据源。

**架构：** ActivityGroupDao 新增按名操作方法；CategoryRepositoryImpl 改为注入 ActivityGroupDao；CategoriesViewModel 删除 Activity 维度的 DataStore 逻辑；Room 版本 3→4 手动 Migration。

**技术栈：** Room DAO + Kotlin Flow + Hilt DI + Jetpack Compose + DataStore Preferences

---

## 文件结构

| 文件 | 操作 | 职责 |
|------|------|------|
| `core/data/src/main/java/com/nltimer/core/data/database/dao/ActivityGroupDao.kt` | 修改 | 新增 `getByName`、`renameByName`、`deleteByName` |
| `core/data/src/main/java/com/nltimer/core/data/database/dao/ActivityDao.kt` | 修改 | 删除 4 个分类相关方法 |
| `core/data/src/main/java/com/nltimer/core/data/database/entity/ActivityEntity.kt` | 修改 | 移除 `category` 字段 |
| `core/data/src/main/java/com/nltimer/core/data/model/Activity.kt` | 修改 | 移除 `category` 字段及映射 |
| `core/data/src/main/java/com/nltimer/core/data/database/NLtimerDatabase.kt` | 修改 | 版本 3→4，新增 Migration |
| `core/data/src/main/java/com/nltimer/core/data/repository/CategoryRepository.kt` | 修改 | 新增 `addActivityCategory` |
| `core/data/src/main/java/com/nltimer/core/data/repository/impl/CategoryRepositoryImpl.kt` | 修改 | 注入 ActivityGroupDao 替代 ActivityDao |
| `core/data/src/main/java/com/nltimer/core/data/repository/impl/BehaviorRepositoryImpl.kt` | 修改 | 移除 Activity 构造中的 `category` 参数 |
| `core/data/src/main/java/com/nltimer/core/data/SettingsPrefs.kt` | 修改 | 删除 Activity 分类方法 |
| `core/data/src/main/java/com/nltimer/core/data/SettingsPrefsImpl.kt` | 修改 | 删除 Activity 分类实现 |
| `feature/categories/src/main/java/com/nltimer/feature/categories/viewmodel/CategoriesViewModel.kt` | 修改 | 删除 `_addedActivityCategories`，简化合并逻辑 |
| `core/data/src/test/java/com/nltimer/core/data/repository/CategoryRepositoryTest.kt` | 重写 | FakeActivityGroupDao 替代 FakeActivityDao |
| `feature/categories/src/test/java/com/nltimer/feature/categories/viewmodel/CategoriesViewModelTest.kt` | 修改 | 更新 FakeRepository，移除 Activity DataStore 测试 |

---

### 任务 1：ActivityGroupDao 新增按名操作方法

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/database/dao/ActivityGroupDao.kt`

新增 `getByName`、`renameByName`、`deleteByName` 三个方法，为 CategoryRepository 从名称查询分组提供支撑。

- [ ] **步骤 1：在 ActivityGroupDao 中添加 3 个新方法**

在 `ActivityGroupDao.kt` 的 `ungroupAllActivities` 方法后追加：

```kotlin
    @Query("SELECT * FROM activity_groups WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): ActivityGroupEntity?

    @Query("UPDATE activity_groups SET name = :newName WHERE name = :oldName")
    suspend fun renameByName(oldName: String, newName: String)

    @Query("DELETE FROM activity_groups WHERE name = :name")
    suspend fun deleteByName(name: String)
```

**注意**：`deleteByName` 调用前必须在 Repository 层先调用 `ungroupAllActivities()` 将活动归入未分类。

- [ ] **步骤 2：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/database/dao/ActivityGroupDao.kt
git commit -m "feat(dao): add getByName, renameByName, deleteByName to ActivityGroupDao"
```

---

### 任务 2：ActivityDao 删除分类相关方法

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/database/dao/ActivityDao.kt`

移除 4 个与 `category` 字符串字段相关的方法，这些方法将统一到 `ActivityGroupDao`。

- [ ] **步骤 1：删除 getDistinctCategories、renameCategory、resetCategory、getByCategory**

删除 `ActivityDao.kt` 中以下 4 个方法：

```kotlin
// 删除以下方法：

// @Query("SELECT DISTINCT category FROM activities WHERE category IS NOT NULL AND category != '' ORDER BY category")
// fun getDistinctCategories(): Flow<List<String>>

// @Query("UPDATE activities SET category = :newName WHERE category = :oldName")
// suspend fun renameCategory(oldName: String, newName: String)

// @Query("UPDATE activities SET category = NULL WHERE category = :category")
// suspend fun resetCategory(category: String)

// @Query("SELECT * FROM activities WHERE category = :category AND isArchived = 0 ORDER BY name")
// fun getByCategory(category: String): Flow<List<ActivityEntity>>
```

- [ ] **步骤 2：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/database/dao/ActivityDao.kt
git commit -m "refactor(dao): remove category-related methods from ActivityDao"
```

---

### 任务 3：ActivityEntity 移除 category 字段

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/database/entity/ActivityEntity.kt`
- 修改：`core/data/src/main/java/com/nltimer/core/data/model/Activity.kt`

- [ ] **步骤 1：从 ActivityEntity 中移除 category 字段**

修改前：
```kotlin
@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val emoji: String? = null,
    val iconKey: String? = null,
    val category: String? = null,
    val groupId: Long? = null,
    val isPreset: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
```

修改后：
```kotlin
@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val emoji: String? = null,
    val iconKey: String? = null,
    val groupId: Long? = null,
    val isPreset: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
```

- [ ] **步骤 2：从 Activity 领域模型中移除 category**

[core/data/model/Activity.kt](file:///d:/2026Code/Group_android/NLtimer/core/data/src/main/java/com/nltimer/core/data/model/Activity.kt) 修改：

```kotlin
data class Activity(
    val id: Long = 0,
    val name: String,
    val emoji: String? = null,
    val iconKey: String? = null,
    val groupId: Long? = null,
    val isPreset: Boolean = false,
    val isArchived: Boolean = false,
) {
    fun toEntity() = ActivityEntity(
        id = id,
        name = name,
        emoji = emoji,
        iconKey = iconKey,
        groupId = groupId,
        isPreset = isPreset,
        isArchived = isArchived,
    )

    companion object {
        fun fromEntity(entity: ActivityEntity) = Activity(
            id = entity.id,
            name = entity.name,
            emoji = entity.emoji,
            iconKey = entity.iconKey,
            groupId = entity.groupId,
            isPreset = entity.isPreset,
            isArchived = entity.isArchived,
        )
    }
}
```

- [ ] **步骤 3：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/database/entity/ActivityEntity.kt
git add core/data/src/main/java/com/nltimer/core/data/model/Activity.kt
git commit -m "refactor(entity): remove category field from ActivityEntity and Activity model"
```

---

### 任务 4：NLtimerDatabase 升级版本号并添加 Migration

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/database/NLtimerDatabase.kt`

- [ ] **步骤 1：更新版本号和添加 Migration**

在 `NLtimerDatabase.kt` 中修改版本号为 4，创建 `MIGRATION_3_4`，并在 databaseBuilder 中注册：

```kotlin
package com.nltimer.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nltimer.core.data.database.dao.ActivityDao
import com.nltimer.core.data.database.dao.ActivityGroupDao
import com.nltimer.core.data.database.dao.BehaviorDao
import com.nltimer.core.data.database.dao.TagDao
import com.nltimer.core.data.database.entity.ActivityEntity
import com.nltimer.core.data.database.entity.ActivityGroupEntity
import com.nltimer.core.data.database.entity.ActivityTagBindingEntity
import com.nltimer.core.data.database.entity.BehaviorEntity
import com.nltimer.core.data.database.entity.BehaviorTagCrossRefEntity
import com.nltimer.core.data.database.entity.TagEntity

@Database(
    entities = [
        ActivityEntity::class,
        ActivityGroupEntity::class,
        TagEntity::class,
        BehaviorEntity::class,
        ActivityTagBindingEntity::class,
        BehaviorTagCrossRefEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class NLtimerDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun activityGroupDao(): ActivityGroupDao
    abstract fun tagDao(): TagDao
    abstract fun behaviorDao(): BehaviorDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val now = System.currentTimeMillis()
                db.execSQL(
                    """
                    INSERT INTO activity_groups (name, sortOrder, createdAt)
                    SELECT DISTINCT category, 0, $now
                    FROM activities
                    WHERE category IS NOT NULL AND category != ''
                      AND category NOT IN (SELECT name FROM activity_groups)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    UPDATE activities SET groupId = (
                        SELECT ag.id FROM activity_groups ag
                        WHERE ag.name = activities.category
                    ) WHERE category IS NOT NULL AND category != ''
                    """.trimIndent()
                )
            }
        }
    }
}
```

- [ ] **步骤 2：在 DataModule 中注册 Migration**

修改 `core/data/src/main/java/com/nltimer/core/data/di/DataModule.kt` 中的 `provideDatabase`：

```kotlin
@Provides
@Singleton
fun provideDatabase(@ApplicationContext context: Context): NLtimerDatabase =
    Room.databaseBuilder(
        context,
        NLtimerDatabase::class.java,
        "nltimer-database",
    )
        .fallbackToDestructiveMigration(false)
        .addMigrations(NLtimerDatabase.MIGRATION_3_4)
        .build()
```

- [ ] **步骤 3：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/database/NLtimerDatabase.kt
git add core/data/src/main/java/com/nltimer/core/data/di/DataModule.kt
git commit -m "feat(database): add Migration 3→4 for category to activity_groups migration"
```

---

### 任务 5：CategoryRepository 接口新增 addActivityCategory

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/repository/CategoryRepository.kt`

- [ ] **步骤 1：新增 addActivityCategory 方法**

```kotlin
package com.nltimer.core.data.repository

import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getDistinctActivityCategories(parent: String? = null): Flow<List<String>>
    suspend fun addActivityCategory(name: String)
    suspend fun renameActivityCategory(oldName: String, newName: String, parent: String? = null)
    suspend fun resetActivityCategory(category: String)

    fun getDistinctTagCategories(parent: String? = null): Flow<List<String>>
    suspend fun renameTagCategory(oldName: String, newName: String, parent: String? = null)
    suspend fun resetTagCategory(category: String)
}
```

- [ ] **步骤 2：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/repository/CategoryRepository.kt
git commit -m "feat(repo): add addActivityCategory to CategoryRepository interface"
```

---

### 任务 6：CategoryRepositoryImpl 重构注入 ActivityGroupDao

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/repository/impl/CategoryRepositoryImpl.kt`

- [ ] **步骤 1：重写实现类**

将 `ActivityDao` 替换为 `ActivityGroupDao`，实现新的 `addActivityCategory` 方法：

```kotlin
package com.nltimer.core.data.repository.impl

import com.nltimer.core.data.database.dao.ActivityGroupDao
import com.nltimer.core.data.database.dao.TagDao
import com.nltimer.core.data.database.entity.ActivityGroupEntity
import com.nltimer.core.data.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val groupDao: ActivityGroupDao,
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

    override fun getDistinctTagCategories(parent: String?): Flow<List<String>> =
        tagDao.getDistinctCategories()

    override suspend fun renameTagCategory(oldName: String, newName: String, parent: String?) {
        tagDao.renameCategory(oldName, newName)
    }

    override suspend fun resetTagCategory(category: String) {
        tagDao.resetCategory(category)
    }
}
```

- [ ] **步骤 2：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/repository/impl/CategoryRepositoryImpl.kt
git commit -m "refactor(repo): rewrite CategoryRepositoryImpl to use ActivityGroupDao"
```

---

### 任务 7：运行时 DataStore 分类数据迁移

**文件：**
- 新建：`core/data/src/main/java/com/nltimer/core/data/migration/CategoryMigrationValidator.kt`

在 SettingsPrefs 接口清理之前，先将 DataStore 中残留的用户自定义分类迁移到 `activity_groups` 表。

- [ ] **步骤 1：创建 CategoryMigrationValidator**

```kotlin
package com.nltimer.core.data.migration

import com.nltimer.core.data.SettingsPrefs
import com.nltimer.core.data.database.dao.ActivityGroupDao
import com.nltimer.core.data.database.entity.ActivityGroupEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
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
    }
}
```

- [ ] **步骤 2：在 DataModule 中绑定 CategoryMigrationValidator**

在 `core/data/src/main/java/com/nltimer/core/data/di/DataModule.kt` 中追加 import 和绑定：

```kotlin
import com.nltimer.core.data.migration.CategoryMigrationValidator

// 在 abstract class 末尾不需要 Binds（该类使用 @Inject constructor，Hilt 自动解析）
// 如果 CategoryMigrationValidator 需要手动提供，在 companion object 中加 @Provides
```

由于 `CategoryMigrationValidator` 使用了 `@Inject constructor` 和 `@Singleton`，Hilt 会自动绑定，无需额外配置。

- [ ] **步骤 3：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/migration/CategoryMigrationValidator.kt
git commit -m "feat(migration): add CategoryMigrationValidator for DataStore to DB migration"
```

---

### 任务 8：SettingsPrefs 删除 Activity 分类方法

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/SettingsPrefs.kt`
- 修改：`core/data/src/main/java/com/nltimer/core/data/SettingsPrefsImpl.kt`

- [ ] **步骤 1：SettingsPrefs 接口删除 getSavedActivityCategories 和 saveActivityCategories**

```kotlin
package com.nltimer.core.data

import com.nltimer.core.designsystem.theme.Theme
import kotlinx.coroutines.flow.Flow

interface SettingsPrefs {
    fun getThemeFlow(): Flow<Theme>
    suspend fun updateTheme(theme: Theme)

    fun getSavedTagCategories(): Flow<Set<String>>
    suspend fun saveTagCategories(categories: Set<String>)
}
```

- [ ] **步骤 2：SettingsPrefsImpl 删除对应的实现和 key**

删除以下内容：
- `savedActivityCategoriesKey` 的定义
- `getSavedActivityCategories()` 方法
- `saveActivityCategories()` 方法

保留 `savedTagCategoriesKey`、`getSavedTagCategories()`、`saveTagCategories()` 不变。

最终文件 `SettingsPrefsImpl.kt` 应如下：

```kotlin
package com.nltimer.core.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nltimer.core.designsystem.theme.AppTheme
import com.nltimer.core.designsystem.theme.Fonts
import com.nltimer.core.designsystem.theme.PaletteStyle
import com.nltimer.core.designsystem.theme.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsPrefsImpl @Inject constructor(private val dataStore: DataStore<Preferences>) : SettingsPrefs {

    private companion object {
        val seedColorKey = intPreferencesKey("seed_color")
        val appThemeKey = stringPreferencesKey("app_theme")
        val isAmoledKey = booleanPreferencesKey("is_amoled")
        val paletteStyleKey = stringPreferencesKey("palette_style")
        val isMaterialYouKey = booleanPreferencesKey("is_material_you")
        val fontKey = stringPreferencesKey("font")
        val savedTagCategoriesKey = stringPreferencesKey("saved_tag_categories")
    }

    override fun getThemeFlow(): Flow<Theme> = dataStore.data.map { prefs ->
        val seed = prefs[seedColorKey] ?: Color(0xFF1565C0).toArgb()
        val appThemeName = prefs[appThemeKey] ?: AppTheme.SYSTEM.name
        val paletteStyleName = prefs[paletteStyleKey] ?: PaletteStyle.TONALSPOT.name
        val fontName = prefs[fontKey] ?: Fonts.FIGTREE.name

        Theme(
            seedColor = Color(seed),
            appTheme = try { AppTheme.valueOf(appThemeName) } catch (_: Exception) { AppTheme.SYSTEM },
            isAmoled = prefs[isAmoledKey] == true,
            paletteStyle = try { PaletteStyle.valueOf(paletteStyleName) } catch (_: Exception) { PaletteStyle.TONALSPOT },
            isMaterialYou = prefs[isMaterialYouKey] == true,
            font = try { Fonts.valueOf(fontName) } catch (_: Exception) { Fonts.FIGTREE },
        )
    }

    override suspend fun updateTheme(theme: Theme) {
        dataStore.edit { prefs ->
            prefs[seedColorKey] = theme.seedColor.toArgb()
            prefs[appThemeKey] = theme.appTheme.name
            prefs[isAmoledKey] = theme.isAmoled
            prefs[paletteStyleKey] = theme.paletteStyle.name
            prefs[isMaterialYouKey] = theme.isMaterialYou
            prefs[fontKey] = theme.font.name
        }
    }

    override fun getSavedTagCategories(): Flow<Set<String>> = dataStore.data.map { prefs ->
        val raw = prefs[savedTagCategoriesKey] ?: ""
        if (raw.isBlank()) emptySet() else raw.split(",").toSet()
    }

    override suspend fun saveTagCategories(categories: Set<String>) {
        dataStore.edit { prefs ->
            prefs[savedTagCategoriesKey] = categories.joinToString(",")
        }
    }
}
```

**注意**：`savedActivityCategoriesKey` 从 companion object 中完全移除。

- [ ] **步骤 3：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/SettingsPrefs.kt
git add core/data/src/main/java/com/nltimer/core/data/SettingsPrefsImpl.kt
git commit -m "refactor(prefs): remove Activity category storage from SettingsPrefs"
```

---

### 任务 9：BehaviorRepositoryImpl 移除 Activity 构造中的 category

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/repository/impl/BehaviorRepositoryImpl.kt`

- [ ] **步骤 1：修改 getBehaviorWithDetails 中 Activity 构造**

找到 `getBehaviorWithDetails()` 方法（约第 60-65 行），删除 `category = activityEntity.category,`：

修改前：
```kotlin
val activity = com.nltimer.core.data.model.Activity(
    id = activityEntity.id,
    name = activityEntity.name,
    emoji = activityEntity.emoji,
    iconKey = activityEntity.iconKey,
    category = activityEntity.category,
    isArchived = activityEntity.isArchived,
)
```

修改后：
```kotlin
val activity = com.nltimer.core.data.model.Activity(
    id = activityEntity.id,
    name = activityEntity.name,
    emoji = activityEntity.emoji,
    iconKey = activityEntity.iconKey,
    isArchived = activityEntity.isArchived,
)
```

- [ ] **步骤 2：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/repository/impl/BehaviorRepositoryImpl.kt
git commit -m "fix(repo): remove category field from Activity construction in BehaviorRepositoryImpl"
```

---

### 任务 10：CategoriesViewModel 简化 Activity 分类逻辑

**文件：**
- 修改：`feature/categories/src/main/java/com/nltimer/feature/categories/viewmodel/CategoriesViewModel.kt`

- [ ] **步骤 1：重写 CategoriesViewModel**

删除 `_addedActivityCategories`、`mergeAndSort()`，保留 `_addedTagCategories` 和 `SettingsPrefs`：

```kotlin
package com.nltimer.feature.categories.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.core.data.SettingsPrefs
import com.nltimer.core.data.repository.CategoryRepository
import com.nltimer.feature.categories.model.CategoriesUiState
import com.nltimer.feature.categories.model.DialogState
import com.nltimer.feature.categories.model.SectionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val settingsPrefs: SettingsPrefs,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _renameConflict = MutableStateFlow<String?>(null)
    val renameConflict: StateFlow<String?> = _renameConflict.asStateFlow()

    private val _dialogState = MutableStateFlow<DialogState?>(null)

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
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = CategoriesUiState(),
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onAddCategory(sectionType: SectionType) {
        _dialogState.value = when (sectionType) {
            SectionType.ACTIVITY -> DialogState.AddActivityCategory(sectionType)
            SectionType.TAG -> DialogState.AddTagCategory(sectionType)
        }
    }

    fun onRenameCategory(sectionType: SectionType, oldName: String) {
        _dialogState.value = when (sectionType) {
            SectionType.ACTIVITY -> DialogState.RenameActivityCategory(oldName, sectionType)
            SectionType.TAG -> DialogState.RenameTagCategory(oldName, sectionType)
        }
    }

    fun onDeleteCategory(sectionType: SectionType, category: String) {
        _dialogState.value = when (sectionType) {
            SectionType.ACTIVITY -> DialogState.DeleteActivityCategory(category, sectionType)
            SectionType.TAG -> DialogState.DeleteTagCategory(category, sectionType)
        }
    }

    fun dismissDialog() {
        _dialogState.value = null
        _renameConflict.value = null
    }

    fun confirmAddCategory(sectionType: SectionType, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            dismissDialog()
            return
        }
        viewModelScope.launch {
            when (sectionType) {
                SectionType.ACTIVITY -> {
                    categoryRepository.addActivityCategory(trimmed)
                }
                SectionType.TAG -> {
                    val updated = _addedTagCategories.value + trimmed
                    _addedTagCategories.value = updated
                    settingsPrefs.saveTagCategories(updated)
                }
            }
            dismissDialog()
        }
    }

    fun confirmRenameCategory(sectionType: SectionType, oldName: String, newName: String) {
        if (oldName == newName) {
            dismissDialog()
            return
        }

        val currentState = uiState.value
        val conflict = when (sectionType) {
            SectionType.ACTIVITY -> currentState.activityCategories.any {
                it.equals(newName, ignoreCase = true)
            }
            SectionType.TAG -> currentState.tagCategories.any {
                it.equals(newName, ignoreCase = true)
            }
        }

        if (conflict) {
            _renameConflict.value = newName
            return
        }

        viewModelScope.launch {
            when (sectionType) {
                SectionType.ACTIVITY -> {
                    categoryRepository.renameActivityCategory(oldName, newName)
                }
                SectionType.TAG -> {
                    if (oldName in _addedTagCategories.value) {
                        val updated = _addedTagCategories.value - oldName + newName
                        _addedTagCategories.value = updated
                        settingsPrefs.saveTagCategories(updated)
                    }
                    categoryRepository.renameTagCategory(oldName, newName)
                }
            }
            dismissDialog()
        }
    }

    fun confirmDeleteCategory(sectionType: SectionType, category: String) {
        viewModelScope.launch {
            when (sectionType) {
                SectionType.ACTIVITY -> {
                    categoryRepository.resetActivityCategory(category)
                }
                SectionType.TAG -> {
                    val updated = _addedTagCategories.value - category
                    _addedTagCategories.value = updated
                    settingsPrefs.saveTagCategories(updated)
                    categoryRepository.resetTagCategory(category)
                }
            }
            dismissDialog()
        }
    }

    fun clearConflict() {
        _renameConflict.value = null
    }
}
```

- [ ] **步骤 2：Commit**

```bash
git add feature/categories/src/main/java/com/nltimer/feature/categories/viewmodel/CategoriesViewModel.kt
git commit -m "refactor(vm): simplify CategoriesViewModel, remove Activity DataStore dependency"
```

---

### 任务 11：重写 CategoryRepositoryTest

**文件：**
- 修改：`core/data/src/test/java/com/nltimer/core/data/repository/CategoryRepositoryTest.kt`

- [ ] **步骤 1：重写测试，使用 FakeActivityGroupDao**

```kotlin
package com.nltimer.core.data.repository

import com.nltimer.core.data.database.dao.ActivityGroupDao
import com.nltimer.core.data.database.dao.TagDao
import com.nltimer.core.data.database.entity.ActivityGroupEntity
import com.nltimer.core.data.database.entity.TagEntity
import com.nltimer.core.data.repository.impl.CategoryRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryRepositoryTest {

    private val groupEntities = mutableListOf<ActivityGroupEntity>()
    private val tagEntities = mutableListOf<TagEntity>()
    private val groupFlow = MutableStateFlow<List<ActivityGroupEntity>>(emptyList())
    private val tagFlow = MutableStateFlow<List<TagEntity>>(emptyList())

    private val fakeGroupDao = object : ActivityGroupDao {
        override fun getAll(): Flow<List<ActivityGroupEntity>> = groupFlow

        override suspend fun insert(group: ActivityGroupEntity): Long {
            val id = groupEntities.size.toLong() + 1
            groupEntities.add(group.copy(id = id))
            groupFlow.value = groupEntities.toList()
            return id
        }

        override suspend fun update(group: ActivityGroupEntity) {
            groupEntities.replaceAll { if (it.id == group.id) group else it }
            groupFlow.value = groupEntities.toList()
        }

        override suspend fun delete(group: ActivityGroupEntity) {
            groupEntities.removeAll { it.id == group.id }
            groupFlow.value = groupEntities.toList()
        }

        override suspend fun ungroupAllActivities(groupId: Long) {}

        override suspend fun getByName(name: String): ActivityGroupEntity? =
            groupEntities.find { it.name == name }

        override suspend fun renameByName(oldName: String, newName: String) {
            groupEntities.replaceAll {
                if (it.name == oldName) it.copy(name = newName) else it
            }
            groupFlow.value = groupEntities.toList()
        }

        override suspend fun deleteByName(name: String) {
            groupEntities.removeAll { it.name == name }
            groupFlow.value = groupEntities.toList()
        }
    }

    private val fakeTagDao = object : TagDao {
        override suspend fun insert(tag: TagEntity): Long {
            val id = tagEntities.size.toLong() + 1
            tagEntities.add(tag.copy(id = id))
            tagFlow.value = tagEntities.toList()
            return id
        }
        override suspend fun update(tag: TagEntity) {}
        override suspend fun delete(tag: TagEntity) {}
        override fun getAllActive(): Flow<List<TagEntity>> = flowOf(emptyList())
        override fun getAll(): Flow<List<TagEntity>> = tagFlow
        override suspend fun getById(id: Long): TagEntity? = null
        override suspend fun getByName(name: String): TagEntity? = null
        override fun getByCategory(category: String): Flow<List<TagEntity>> = flowOf(emptyList())
        override suspend fun setArchived(id: Long, archived: Boolean) {}
        override fun search(query: String): Flow<List<TagEntity>> = flowOf(emptyList())
        override fun getByActivityId(activityId: Long): Flow<List<TagEntity>> = flowOf(emptyList())
        override suspend fun getTagsForBehaviorSync(behaviorId: Long): List<TagEntity> = emptyList()

        override fun getDistinctCategories(): Flow<List<String>> =
            tagFlow.map { entities ->
                entities.mapNotNull { it.category }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
            }

        override suspend fun renameCategory(oldName: String, newName: String) {
            tagEntities.replaceAll {
                if (it.category == oldName) it.copy(category = newName) else it
            }
            tagFlow.value = tagEntities.toList()
        }

        override suspend fun resetCategory(category: String) {
            tagEntities.replaceAll {
                if (it.category == category) it.copy(category = null) else it
            }
            tagFlow.value = tagEntities.toList()
        }
    }

    private val repository = CategoryRepositoryImpl(fakeGroupDao, fakeTagDao)

    @Test
    fun getDistinctActivityCategories_returnsSortedNames() = runTest {
        fakeGroupDao.insert(ActivityGroupEntity(name = "运动"))
        fakeGroupDao.insert(ActivityGroupEntity(name = "学习"))
        fakeGroupDao.insert(ActivityGroupEntity(name = "工作"))

        val categories = repository.getDistinctActivityCategories().first()

        assertEquals(listOf("学习", "工作", "运动"), categories)
    }

    @Test
    fun addActivityCategory_insertsNewGroup() = runTest {
        repository.addActivityCategory("体育")

        val categories = repository.getDistinctActivityCategories().first()
        assertTrue(categories.contains("体育"))
    }

    @Test
    fun renameActivityCategory_renamesInGroups() = runTest {
        fakeGroupDao.insert(ActivityGroupEntity(name = "运动"))
        fakeGroupDao.insert(ActivityGroupEntity(name = "学习"))

        repository.renameActivityCategory("运动", "体育")

        val categories = repository.getDistinctActivityCategories().first()
        assertTrue(categories.contains("体育"))
        assertTrue(categories.contains("学习"))
    }

    @Test
    fun resetActivityCategory_deletesGroup() = runTest {
        fakeGroupDao.insert(ActivityGroupEntity(name = "运动"))
        fakeGroupDao.insert(ActivityGroupEntity(name = "学习"))

        repository.resetActivityCategory("运动")

        val categories = repository.getDistinctActivityCategories().first()
        assertEquals(listOf("学习"), categories)
    }

    @Test
    fun resetActivityCategory_nonexistent_doesNothing() = runTest {
        fakeGroupDao.insert(ActivityGroupEntity(name = "运动"))

        repository.resetActivityCategory("不存在的分组")

        val categories = repository.getDistinctActivityCategories().first()
        assertEquals(listOf("运动"), categories)
    }

    @Test
    fun getDistinctTagCategories_filtersNullAndEmpty() = runTest {
        fakeTagDao.insert(TagEntity(name = "重要", category = "优先级"))
        fakeTagDao.insert(TagEntity(name = "次要", category = null))
        fakeTagDao.insert(TagEntity(name = "紧急", category = "优先级"))

        val categories = repository.getDistinctTagCategories().first()

        assertEquals(listOf("优先级"), categories)
    }

    @Test
    fun renameTagCategory_updatesAllMatching() = runTest {
        fakeTagDao.insert(TagEntity(name = "重要", category = "优先级"))
        fakeTagDao.insert(TagEntity(name = "已读", category = "状态"))

        repository.renameTagCategory("优先级", "重要程度")

        val categories = repository.getDistinctTagCategories().first()
        assertTrue(categories.contains("重要程度"))
        assertTrue(categories.contains("状态"))
    }

    @Test
    fun resetTagCategory_setsCategoryToNull() = runTest {
        fakeTagDao.insert(TagEntity(name = "重要", category = "优先级"))

        repository.resetTagCategory("优先级")

        val categories = repository.getDistinctTagCategories().first()
        assertTrue(categories.isEmpty())
    }
}
```

- [ ] **步骤 2：运行测试验证通过**

```bash
./gradlew :core:data:test --tests "com.nltimer.core.data.repository.CategoryRepositoryTest"
```

预期：所有 8 个测试 PASS。

- [ ] **步骤 3：Commit**

```bash
git add core/data/src/test/java/com/nltimer/core/data/repository/CategoryRepositoryTest.kt
git commit -m "test(repo): rewrite CategoryRepositoryTest for ActivityGroupDao-based impl"
```

---

### 任务 12：更新 CategoriesViewModelTest

**文件：**
- 修改：`feature/categories/src/test/java/com/nltimer/feature/categories/viewmodel/CategoriesViewModelTest.kt`

- [ ] **步骤 1：重写测试 — 更新 FakeCategoryRepository，新增 addActivityCategory**

完整重写测试文件：

```kotlin
package com.nltimer.feature.categories.viewmodel

import com.nltimer.core.data.SettingsPrefs
import com.nltimer.core.data.repository.CategoryRepository
import com.nltimer.core.designsystem.theme.Theme
import com.nltimer.feature.categories.model.DialogState
import com.nltimer.feature.categories.model.SectionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var activityCategoriesFlow: MutableStateFlow<List<String>>
    private lateinit var tagCategoriesFlow: MutableStateFlow<List<String>>
    private lateinit var repository: FakeCategoryRepository
    private lateinit var settingsPrefs: FakeSettingsPrefs
    private lateinit var viewModel: CategoriesViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        activityCategoriesFlow = MutableStateFlow(emptyList())
        tagCategoriesFlow = MutableStateFlow(emptyList())
        repository = FakeCategoryRepository(activityCategoriesFlow, tagCategoriesFlow)
        settingsPrefs = FakeSettingsPrefs()
        viewModel = CategoriesViewModel(repository, settingsPrefs)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_hasEmptyCategories() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.activityCategories.isEmpty())
        assertTrue(state.tagCategories.isEmpty())
        assertNull(state.dialogState)
    }

    @Test
    fun searchQuery_filtersCategories() = runTest {
        advanceUntilIdle()
        viewModel.onSearchQueryChange("运动")
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("运动", state.searchQuery)
    }

    @Test
    fun searchQuery_emptyShowsAll() = runTest {
        advanceUntilIdle()
        viewModel.onSearchQueryChange("运动")
        advanceUntilIdle()
        viewModel.onSearchQueryChange("")
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
    }

    @Test
    fun addActivityCategory_showsDialog() = runTest {
        advanceUntilIdle()
        viewModel.onAddCategory(SectionType.ACTIVITY)
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state.dialogState is DialogState.AddActivityCategory)
    }

    @Test
    fun addTagCategory_showsDialog() = runTest {
        advanceUntilIdle()
        viewModel.onAddCategory(SectionType.TAG)
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state.dialogState is DialogState.AddTagCategory)
    }

    @Test
    fun confirmAddActivityCategory_callsRepositoryAdd() = runTest {
        advanceUntilIdle()
        viewModel.onAddCategory(SectionType.ACTIVITY)
        advanceUntilIdle()
        viewModel.confirmAddCategory(SectionType.ACTIVITY, "运动")
        advanceUntilIdle()

        assertTrue(repository.addActivityCategoryCalled)
        assertEquals("运动", repository.lastAddedActivityCategory)
    }

    @Test
    fun confirmAddTagCategory_persistsToDataStore() = runTest {
        advanceUntilIdle()
        viewModel.onAddCategory(SectionType.TAG)
        advanceUntilIdle()
        viewModel.confirmAddCategory(SectionType.TAG, "优先级")
        advanceUntilIdle()

        assertEquals(setOf("优先级"), settingsPrefs.lastSavedTagCategories)
    }

    @Test
    fun confirmRenameActivityCategory_callsRepository() = runTest {
        activityCategoriesFlow.value = listOf("运动")
        advanceUntilIdle()

        viewModel.onRenameCategory(SectionType.ACTIVITY, "运动")
        advanceUntilIdle()
        viewModel.confirmRenameCategory(SectionType.ACTIVITY, "运动", "体育")
        advanceUntilIdle()

        assertTrue(repository.renameActivityCategoryCalled)
        assertEquals("运动" to "体育", repository.lastRenameActivityPair)
    }

    @Test
    fun confirmRename_withConflict_setsConflictFlag() = runTest {
        activityCategoriesFlow.value = listOf("运动", "学习")
        advanceUntilIdle()

        viewModel.onRenameCategory(SectionType.ACTIVITY, "运动")
        advanceUntilIdle()
        viewModel.confirmRenameCategory(SectionType.ACTIVITY, "运动", "学习")
        advanceUntilIdle()

        assertEquals("学习", viewModel.renameConflict.value)
    }

    @Test
    fun confirmDeleteActivityCategory_callsRepository() = runTest {
        advanceUntilIdle()

        viewModel.confirmDeleteCategory(SectionType.ACTIVITY, "运动")
        advanceUntilIdle()

        assertTrue(repository.resetActivityCategoryCalled)
        assertEquals("运动", repository.lastResetActivityCategory)
    }

    @Test
    fun confirmDeleteTagCategory_callsRepository() = runTest {
        advanceUntilIdle()

        viewModel.confirmDeleteCategory(SectionType.TAG, "优先级")
        advanceUntilIdle()

        assertTrue(repository.resetTagCategoryCalled)
    }

    @Test
    fun dismissDialog_clearsDialogState() = runTest {
        advanceUntilIdle()
        viewModel.onAddCategory(SectionType.ACTIVITY)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.dialogState)

        viewModel.dismissDialog()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.dialogState)
    }

    @Test
    fun clearConflict_resetsToNull() = runTest {
        activityCategoriesFlow.value = listOf("运动", "学习")
        advanceUntilIdle()

        viewModel.onRenameCategory(SectionType.ACTIVITY, "运动")
        advanceUntilIdle()
        viewModel.confirmRenameCategory(SectionType.ACTIVITY, "运动", "学习")
        advanceUntilIdle()
        assertEquals("学习", viewModel.renameConflict.value)

        viewModel.clearConflict()
        advanceUntilIdle()

        assertNull(viewModel.renameConflict.value)
    }

    private class FakeSettingsPrefs : SettingsPrefs {

        private val tagCategories = MutableStateFlow<Set<String>>(emptySet())

        var lastSavedTagCategories: Set<String>? = null

        override fun getThemeFlow(): Flow<Theme> = flowOf(Theme())
        override suspend fun updateTheme(theme: Theme) {}

        override fun getSavedTagCategories(): Flow<Set<String>> = tagCategories
        override suspend fun saveTagCategories(categories: Set<String>) {
            tagCategories.value = categories
            lastSavedTagCategories = categories
        }
    }

    private class FakeCategoryRepository(
        private val activityCategories: MutableStateFlow<List<String>>,
        private val tagCategories: MutableStateFlow<List<String>>,
    ) : CategoryRepository {

        var addActivityCategoryCalled = false
        var renameActivityCategoryCalled = false
        var renameTagCategoryCalled = false
        var resetActivityCategoryCalled = false
        var resetTagCategoryCalled = false
        var lastAddedActivityCategory: String? = null
        var lastRenameActivityPair: Pair<String, String>? = null
        var lastResetActivityCategory: String? = null

        override fun getDistinctActivityCategories(parent: String?) = activityCategories
        override fun getDistinctTagCategories(parent: String?) = tagCategories

        override suspend fun addActivityCategory(name: String) {
            addActivityCategoryCalled = true
            lastAddedActivityCategory = name
        }

        override suspend fun renameActivityCategory(oldName: String, newName: String, parent: String?) {
            renameActivityCategoryCalled = true
            lastRenameActivityPair = oldName to newName
        }

        override suspend fun resetActivityCategory(category: String) {
            resetActivityCategoryCalled = true
            lastResetActivityCategory = category
        }

        override suspend fun renameTagCategory(oldName: String, newName: String, parent: String?) {
            renameTagCategoryCalled = true
        }

        override suspend fun resetTagCategory(category: String) {
            resetTagCategoryCalled = true
        }
    }
}
```

- [ ] **步骤 2：运行测试验证通过**

```bash
./gradlew :feature:categories:test --tests "com.nltimer.feature.categories.viewmodel.CategoriesViewModelTest"
```

预期：所有 14 个测试 PASS。

- [ ] **步骤 3：Commit**

```bash
git add feature/categories/src/test/java/com/nltimer/feature/categories/viewmodel/CategoriesViewModelTest.kt
git commit -m "test(vm): update CategoriesViewModelTest for unified category system"
```

---

### 任务 13：全量编译与验证

**无需修改文件**，验证所有模块能正常编译。

- [ ] **步骤 1：全量编译**

```bash
./gradlew :core:data:compileDebugKotlin :feature:categories:compileDebugKotlin :app:compileDebugKotlin
```

预期：BUILD SUCCESSFUL，无编译错误。

- [ ] **步骤 2：运行全部相关单元测试**

```bash
./gradlew :core:data:test :feature:categories:test
```

预期：所有测试 PASS。

- [ ] **步骤 3：Commit**

```bash
git commit --allow-empty -m "chore: verify full build after unified category system refactor"
```

---

## 验证命令汇总

```bash
# Room DAO 编译
./gradlew :core:data:compileDebugKotlin

# Categories 模块编译
./gradlew :feature:categories:compileDebugKotlin

# 全量编译
./gradlew :app:compileDebugKotlin

# Repository 测试
./gradlew :core:data:test --tests "com.nltimer.core.data.repository.CategoryRepositoryTest"

# ViewModel 测试
./gradlew :feature:categories:test --tests "com.nltimer.feature.categories.viewmodel.CategoriesViewModelTest"

# 全部测试
./gradlew :core:data:test :feature:categories:test
```
