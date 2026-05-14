# 数据管理功能实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 在设置页面新增"数据管理"二级页面，支持活动/标签/分类数据的导出和导入（智能处理/直接覆盖两种模式），并提供行为记录管理入口。

**架构：** 新增 `DataExportImportRepository` 仓库层处理序列化与数据库操作，`ExportDataUseCase` / `ImportDataUseCase` 封装业务逻辑，`DataManagementViewModel` 管理页面状态和文件选择回调，`DataManagementScreen` 实现折叠卡片式 UI。使用 `kotlinx.serialization` 进行 JSON 序列化，关联表外键通过名称重映射。

**技术栈：** Kotlin, Compose, Hilt, Room, kotlinx.serialization, Android ActivityResultContracts

---

## 文件结构

### 新增文件

| 文件 | 职责 |
|------|------|
| `core/data/src/main/java/com/nltimer/core/data/model/ExportData.kt` | JSON 导出数据结构 `@Serializable` 模型 |
| `core/data/src/main/java/com/nltimer/core/data/repository/DataExportImportRepository.kt` | 导出导入仓库接口 + ImportMode + ImportResult |
| `core/data/src/main/java/com/nltimer/core/data/repository/impl/DataExportImportRepositoryImpl.kt` | 导出导入仓库实现 |
| `core/data/src/main/java/com/nltimer/core/data/usecase/ExportDataUseCase.kt` | 导出业务逻辑 |
| `core/data/src/main/java/com/nltimer/core/data/usecase/ImportDataUseCase.kt` | 导入业务逻辑（智能/覆盖） |
| `feature/settings/src/main/java/com/nltimer/feature/settings/ui/DataManagementScreen.kt` | 数据管理页面 Composable |
| `feature/settings/src/main/java/com/nltimer/feature/settings/ui/DataManagementViewModel.kt` | 数据管理页面 ViewModel |
| `feature/settings/src/main/java/com/nltimer/feature/settings/ui/ImportModeDialog.kt` | 导入模式选择对话框 |
| `core/data/src/test/java/com/nltimer/core/data/model/ExportDataTest.kt` | 序列化模型测试 |
| `core/data/src/test/java/com/nltimer/core/data/usecase/ExportDataUseCaseTest.kt` | 导出用例测试 |
| `core/data/src/test/java/com/nltimer/core/data/usecase/ImportDataUseCaseTest.kt` | 导入用例测试 |

### 修改文件

| 文件 | 变更 |
|------|------|
| `gradle/libs.versions.toml` | 新增 kotlinx-serialization 版本和库定义、插件定义 |
| `build.gradle.kts`（根项目） | 新增 serialization 插件 apply false |
| `core/data/build.gradle.kts` | 新增 serialization 插件和依赖 |
| `feature/settings/build.gradle.kts` | 新增 serialization 依赖 |
| `core/data/src/main/java/com/nltimer/core/data/di/DataModule.kt` | 注册 DataExportImportRepository 绑定 |
| `core/data/src/main/java/com/nltimer/core/data/database/dao/TagDao.kt` | 新增 `getAllDistinctSync()` |
| `core/data/src/main/java/com/nltimer/core/data/database/dao/ActivityGroupDao.kt` | 新增 `getAllSync()` |
| `feature/settings/src/main/java/com/nltimer/feature/settings/ui/SettingsScreen.kt` | 新增"数据管理"入口卡片 + 导航回调 |
| `app/src/main/java/com/nltimer/app/navigation/NLtimerRoutes.kt` | 新增 DATA_MANAGEMENT 路由 |
| `app/src/main/java/com/nltimer/app/navigation/NLtimerNavHost.kt` | 注册数据管理路由 + 传递导航参数 |

---

### 任务 1：添加 kotlinx.serialization 依赖

**文件：**
- 修改：`gradle/libs.versions.toml`
- 修改：`build.gradle.kts`（根项目）
- 修改：`core/data/build.gradle.kts`
- 修改：`feature/settings/build.gradle.kts`

- [ ] **步骤 1：在 libs.versions.toml 中新增版本和库**

在 `[versions]` 中新增：
```toml
kotlinx_serialization = "1.8.1"
```

在 `[libraries]` 中新增：
```toml
kotlinx_serialization_json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx_serialization" }
```

在 `[plugins]` 中新增：
```toml
kotlin_serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

- [ ] **步骤 2：在根 build.gradle.kts 中注册 serialization 插件**

```kotlin
plugins {
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
```

- [ ] **步骤 3：在 core/data/build.gradle.kts 中启用 serialization 插件和依赖**

在 plugins 块中新增：
```kotlin
alias(libs.plugins.kotlin.serialization)
```

在 dependencies 块中新增：
```kotlin
implementation(libs.kotlinx.serialization.json)
```

- [ ] **步骤 4：在 feature/settings/build.gradle.kts 中添加依赖**

在 plugins 块中新增：
```kotlin
alias(libs.plugins.kotlin.serialization)
```

在 dependencies 块中新增：
```kotlin
implementation(libs.kotlinx.serialization.json)
```

- [ ] **步骤 5：Sync Gradle 并确认无报错**

运行：`./gradlew :core:data:dependencies --configuration runtimeClasspath 2>&1 | Select-String "serialization"`
预期：看到 `kotlinx-serialization-json` 依赖

- [ ] **步骤 6：Commit**

```bash
git add gradle/libs.versions.toml build.gradle.kts core/data/build.gradle.kts feature/settings/build.gradle.kts
git commit -m "build: 添加 kotlinx.serialization 依赖"
```

---

### 任务 2：定义 ExportData 序列化模型

**文件：**
- 创建：`core/data/src/main/java/com/nltimer/core/data/model/ExportData.kt`
- 测试：`core/data/src/test/java/com/nltimer/core/data/model/ExportDataTest.kt`

- [ ] **步骤 1：创建 ExportData.kt**

```kotlin
package com.nltimer.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ExportData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val activities: List<ExportedActivity>? = null,
    val activityGroups: List<ExportedActivityGroup>? = null,
    val tags: List<ExportedTag>? = null,
    val tagCategories: List<String>? = null,
)

@Serializable
data class ExportedActivity(
    val name: String,
    val iconKey: String? = null,
    val keywords: String? = null,
    val groupName: String? = null,
    val isPreset: Boolean = false,
    val isArchived: Boolean = false,
    val archivedAt: Long? = null,
    val color: Long? = null,
    val usageCount: Int = 0,
    val tagNames: List<String> = emptyList(),
)

@Serializable
data class ExportedActivityGroup(
    val name: String,
    val sortOrder: Int = 0,
    val isArchived: Boolean = false,
    val archivedAt: Long? = null,
)

@Serializable
data class ExportedTag(
    val name: String,
    val color: Long? = null,
    val iconKey: String? = null,
    val category: String? = null,
    val priority: Int = 0,
    val usageCount: Int = 0,
    val sortOrder: Int = 0,
    val keywords: String? = null,
    val isArchived: Boolean = false,
    val archivedAt: Long? = null,
)
```

- [ ] **步骤 2：编写序列化/反序列化测试**

创建 `core/data/src/test/java/com/nltimer/core/data/model/ExportDataTest.kt`：

```kotlin
package com.nltimer.core.data.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class ExportDataTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun roundTripSerializationPreservesAllFields() {
        val data = ExportData(
            version = 1,
            exportedAt = 1715299200000L,
            activities = listOf(
                ExportedActivity(
                    name = "阅读",
                    iconKey = "book",
                    groupName = "学习",
                    color = 4280391411,
                    usageCount = 15,
                    tagNames = listOf("专注"),
                ),
            ),
            activityGroups = listOf(
                ExportedActivityGroup(name = "学习", sortOrder = 0),
            ),
            tags = listOf(
                ExportedTag(name = "专注", category = "状态", priority = 1),
            ),
            tagCategories = listOf("状态", "场景"),
        )
        val encoded = json.encodeToString(ExportData.serializer(), data)
        val decoded = json.decodeFromString(ExportData.serializer(), encoded)
        assertEquals(data, decoded)
    }

    @Test
    fun partialExportOnlyContainsNonNullFields() {
        val data = ExportData(
            activities = listOf(ExportedActivity(name = "跑步")),
        )
        val encoded = json.encodeToString(ExportData.serializer(), data)
        val decoded = json.decodeFromString(ExportData.serializer(), encoded)
        assertEquals(null, decoded.tags)
        assertEquals(null, decoded.activityGroups)
        assertEquals(1, decoded.activities?.size)
    }
}
```

- [ ] **步骤 3：运行测试确认通过**

运行：`./gradlew :core:data:test --tests "com.nltimer.core.data.model.ExportDataTest"`
预期：2 个测试通过

- [ ] **步骤 4：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/model/ExportData.kt core/data/src/test/java/com/nltimer/core/data/model/ExportDataTest.kt
git commit -m "feat: 添加 ExportData 序列化模型及测试"
```

---

### 任务 3：补充 DAO 同步查询方法

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/database/dao/TagDao.kt`
- 修改：`core/data/src/main/java/com/nltimer/core/data/database/dao/ActivityGroupDao.kt`

- [ ] **步骤 1：在 TagDao 中新增 getAllDistinctSync**

在 TagDao 接口中新增：

```kotlin
@Query("SELECT * FROM tags ORDER BY name")
suspend fun getAllDistinctSync(): List<TagEntity>
```

- [ ] **步骤 2：在 ActivityGroupDao 中新增 getAllSync**

在 ActivityGroupDao 接口中新增：

```kotlin
@Query("SELECT * FROM activity_groups ORDER BY sortOrder ASC, id ASC")
suspend fun getAllSync(): List<ActivityGroupEntity>
```

- [ ] **步骤 3：编译验证**

运行：`./gradlew :core:data:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/database/dao/TagDao.kt core/data/src/main/java/com/nltimer/core/data/database/dao/ActivityGroupDao.kt
git commit -m "feat: 为 TagDao 和 ActivityGroupDao 补充同步查询方法"
```

---

### 任务 4：定义 DataExportImportRepository 接口和实现

**文件：**
- 创建：`core/data/src/main/java/com/nltimer/core/data/repository/DataExportImportRepository.kt`
- 创建：`core/data/src/main/java/com/nltimer/core/data/repository/impl/DataExportImportRepositoryImpl.kt`
- 修改：`core/data/src/main/java/com/nltimer/core/data/di/DataModule.kt`

- [ ] **步骤 1：创建仓库接口**

创建 `core/data/src/main/java/com/nltimer/core/data/repository/DataExportImportRepository.kt`：

```kotlin
package com.nltimer.core.data.repository

import com.nltimer.core.data.model.ExportData

enum class ImportMode { SMART, OVERWRITE }

sealed class ImportResult {
    data class Success(
        val activitiesImported: Int,
        val tagsImported: Int,
        val groupsImported: Int,
        val tagCategoriesImported: Int,
    ) : ImportResult()

    data class Error(val message: String) : ImportResult()
}

interface DataExportImportRepository {
    suspend fun exportAll(): ExportData
    suspend fun exportActivities(): ExportData
    suspend fun exportTags(): ExportData
    suspend fun exportCategories(): ExportData

    suspend fun importAll(data: ExportData, mode: ImportMode): ImportResult
    suspend fun importActivities(data: ExportData, mode: ImportMode): ImportResult
    suspend fun importTags(data: ExportData, mode: ImportMode): ImportResult
    suspend fun importCategories(data: ExportData, mode: ImportMode): ImportResult
}
```

- [ ] **步骤 2：创建仓库实现**

创建 `core/data/src/main/java/com/nltimer/core/data/repository/impl/DataExportImportRepositoryImpl.kt`。

核心逻辑：
- 导出：从各 DAO 同步查询，构建 ExportData。外键用名称替代：`groupName` 替代 `groupId`，`tagNames` 替代 `tagIds`。
- 智能导入：按名称匹配，同名则补全空字段，不同名新增，关联表 IGNORE 插入。
- 覆盖导入：在 `withTransaction` 中删除目标类型数据后插入导入数据，重建关联表。

构造函数注入：`ActivityDao`、`ActivityGroupDao`、`TagDao`、`BehaviorDao`、`NLtimerDatabase`。

关键私有方法：
- `smartImportActivities(activities: List<ExportedActivity>): Int` — 遍历导入活动，按名称查现有记录，存在则 `existing.copy(iconKey = existing.iconKey ?: act.iconKey, ...)` 并 update，不存在则 insert。
- `overwriteImportActivities(activities: List<ExportedActivity>): Int` — 事务中 deleteAll + 逐条 insert。
- `smartImportTags(tags: List<ExportedTag>, tagCategories: List<String>): Int` — 同上逻辑，补全 `color`、`iconKey`、`keywords`、`category`。
- `overwriteImportTags(tags: List<ExportedTag>, tagCategories: List<String>): Int` — 事务中删除关联表 + 删除标签 + 插入。
- `smartImportGroups(groups: List<ExportedActivityGroup>): Int` — 同名跳过，不同名 insert。
- `overwriteImportGroups(groups: List<ExportedActivityGroup>): Int` — 事务中 deleteAll + 逐条 insert。
- `smartImportActivityTagBindings(data: ExportData)` — 按活动名称和标签名称查找对应 ID，构建 `ActivityTagBindingEntity` IGNORE 插入。
- `rebuildActivityTagBindings(data: ExportData)` — 覆盖模式下重建关联，逻辑同上。
- Entity 与 Exported* 的互转扩展函数：`ActivityEntity.toExported(groupName, tagNames)`、`ExportedActivity.toEntity(groupId)` 等。

覆盖全部导入 `overwriteImportAll` 的删除顺序：`behavior_tag_cross_ref` → `activity_tag_binding` → `behaviors` → `tags` → `activities` → `activity_groups`，然后按依赖顺序插入。

- [ ] **步骤 3：在 DataModule.kt 中注册仓库绑定**

新增 import：
```kotlin
import com.nltimer.core.data.repository.DataExportImportRepository
import com.nltimer.core.data.repository.impl.DataExportImportRepositoryImpl
```

新增绑定方法：
```kotlin
@Binds
abstract fun bindDataExportImportRepository(
    impl: DataExportImportRepositoryImpl,
): DataExportImportRepository
```

- [ ] **步骤 4：编译验证**

运行：`./gradlew :core:data:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 5：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/repository/DataExportImportRepository.kt core/data/src/main/java/com/nltimer/core/data/repository/impl/DataExportImportRepositoryImpl.kt core/data/src/main/java/com/nltimer/core/data/di/DataModule.kt
git commit -m "feat: 添加 DataExportImportRepository 接口与实现"
```

---

### 任务 5：创建 ExportDataUseCase 和 ImportDataUseCase

**文件：**
- 创建：`core/data/src/main/java/com/nltimer/core/data/usecase/ExportDataUseCase.kt`
- 创建：`core/data/src/main/java/com/nltimer/core/data/usecase/ImportDataUseCase.kt`
- 创建：`core/data/src/test/java/com/nltimer/core/data/usecase/ExportDataUseCaseTest.kt`
- 创建：`core/data/src/test/java/com/nltimer/core/data/usecase/ImportDataUseCaseTest.kt`

- [ ] **步骤 1：创建 ExportDataUseCase**

```kotlin
package com.nltimer.core.data.usecase

import com.nltimer.core.data.model.ExportData
import com.nltimer.core.data.repository.DataExportImportRepository
import javax.inject.Inject

enum class ExportScope { ALL, ACTIVITIES, TAGS, CATEGORIES }

class ExportDataUseCase @Inject constructor(
    private val repository: DataExportImportRepository,
) {
    suspend operator fun invoke(scope: ExportScope): ExportData = when (scope) {
        ExportScope.ALL -> repository.exportAll()
        ExportScope.ACTIVITIES -> repository.exportActivities()
        ExportScope.TAGS -> repository.exportTags()
        ExportScope.CATEGORIES -> repository.exportCategories()
    }
}
```

- [ ] **步骤 2：创建 ImportDataUseCase**

```kotlin
package com.nltimer.core.data.usecase

import com.nltimer.core.data.model.ExportData
import com.nltimer.core.data.repository.DataExportImportRepository
import com.nltimer.core.data.repository.ImportMode
import com.nltimer.core.data.repository.ImportResult
import javax.inject.Inject

enum class ImportScope { ALL, ACTIVITIES, TAGS, CATEGORIES }

class ImportDataUseCase @Inject constructor(
    private val repository: DataExportImportRepository,
) {
    suspend operator fun invoke(data: ExportData, scope: ImportScope, mode: ImportMode): ImportResult =
        when (scope) {
            ImportScope.ALL -> repository.importAll(data, mode)
            ImportScope.ACTIVITIES -> repository.importActivities(data, mode)
            ImportScope.TAGS -> repository.importTags(data, mode)
            ImportScope.CATEGORIES -> repository.importCategories(data, mode)
        }
}
```

- [ ] **步骤 3：编写 ExportDataUseCase 测试**

创建 `core/data/src/test/java/com/nltimer/core/data/usecase/ExportDataUseCaseTest.kt`：

```kotlin
package com.nltimer.core.data.usecase

import com.nltimer.core.data.model.ExportData
import com.nltimer.core.data.repository.DataExportImportRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ExportDataUseCaseTest {

    private val repository = mockk<DataExportImportRepository>()
    private val useCase = ExportDataUseCase(repository)

    @Test
    fun exportAllDelegatesToRepository() = runTest {
        val expected = ExportData(version = 1, exportedAt = 1000L)
        coEvery { repository.exportAll() } returns expected
        val result = useCase(ExportScope.ALL)
        assertEquals(expected, result)
    }

    @Test
    fun exportActivitiesDelegatesToRepository() = runTest {
        val expected = ExportData(activities = emptyList())
        coEvery { repository.exportActivities() } returns expected
        val result = useCase(ExportScope.ACTIVITIES)
        assertEquals(expected, result)
    }
}
```

- [ ] **步骤 4：编写 ImportDataUseCase 测试**

创建 `core/data/src/test/java/com/nltimer/core/data/usecase/ImportDataUseCaseTest.kt`：

```kotlin
package com.nltimer.core.data.usecase

import com.nltimer.core.data.model.ExportData
import com.nltimer.core.data.repository.DataExportImportRepository
import com.nltimer.core.data.repository.ImportMode
import com.nltimer.core.data.repository.ImportResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportDataUseCaseTest {

    private val repository = mockk<DataExportImportRepository>()
    private val useCase = ImportDataUseCase(repository)

    @Test
    fun importAllWithSmartMode() = runTest {
        val data = ExportData()
        val expected = ImportResult.Success(1, 2, 3, 4)
        coEvery { repository.importAll(data, ImportMode.SMART) } returns expected
        val result = useCase(data, ImportScope.ALL, ImportMode.SMART)
        assertEquals(expected, result)
    }

    @Test
    fun importReturnsErrorOnFailure() = runTest {
        val data = ExportData()
        coEvery { repository.importAll(data, ImportMode.OVERWRITE) } returns ImportResult.Error("fail")
        val result = useCase(data, ImportScope.ALL, ImportMode.OVERWRITE)
        assertTrue(result is ImportResult.Error)
    }
}
```

- [ ] **步骤 5：运行测试**

运行：`./gradlew :core:data:test --tests "com.nltimer.core.data.usecase.*"`
预期：4 个测试通过

- [ ] **步骤 6：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/usecase/ExportDataUseCase.kt core/data/src/main/java/com/nltimer/core/data/usecase/ImportDataUseCase.kt core/data/src/test/java/com/nltimer/core/data/usecase/ExportDataUseCaseTest.kt core/data/src/test/java/com/nltimer/core/data/usecase/ImportDataUseCaseTest.kt
git commit -m "feat: 添加 ExportDataUseCase 与 ImportDataUseCase 及测试"
```

---

### 任务 6：创建 DataManagementViewModel

**文件：**
- 创建：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/DataManagementViewModel.kt`

- [ ] **步骤 1：创建 ViewModel**

```kotlin
package com.nltimer.feature.settings.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.core.data.model.ExportData
import com.nltimer.core.data.repository.ImportMode
import com.nltimer.core.data.repository.ImportResult
import com.nltimer.core.data.usecase.ExportDataUseCase
import com.nltimer.core.data.usecase.ExportScope
import com.nltimer.core.data.usecase.ImportDataUseCase
import com.nltimer.core.data.usecase.ImportScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class DataManagementUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val snackbarMessage: String? = null,
    val pendingImportData: ExportData? = null,
    val pendingImportScope: ImportScope? = null,
    val lastExportData: ExportData? = null,
    val lastExportScope: ExportScope? = null,
)

@HiltViewModel
class DataManagementViewModel @Inject constructor(
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DataManagementUiState())
    val uiState: StateFlow<DataManagementUiState> = _uiState.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }

    fun exportData(scope: ExportScope) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                val data = exportDataUseCase(scope)
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    lastExportData = data,
                    lastExportScope = scope,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    snackbarMessage = "导出失败",
                )
            }
        }
    }

    fun writeExportToFile(context: Context, uri: Uri, data: ExportData) {
        viewModelScope.launch {
            try {
                val jsonString = json.encodeToString(ExportData.serializer(), data)
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(jsonString.toByteArray(Charsets.UTF_8))
                }
                _uiState.value = _uiState.value.copy(
                    lastExportData = null,
                    lastExportScope = null,
                    snackbarMessage = "数据已导出",
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(snackbarMessage = "导出失败")
            }
        }
    }

    fun triggerImportFileSelection(scope: ImportScope) {
        _uiState.value = _uiState.value.copy(pendingImportScope = scope)
    }

    fun onFileSelectedForImport(context: Context, uri: Uri) {
        val scope = _uiState.value.pendingImportScope ?: return
        viewModelScope.launch {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { input ->
                    input.readBytes().toString(Charsets.UTF_8)
                } ?: throw IllegalStateException("无法读取文件")
                val data = json.decodeFromString(ExportData.serializer(), jsonString)
                _uiState.value = _uiState.value.copy(pendingImportData = data)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = "文件格式无效",
                    pendingImportScope = null,
                )
            }
        }
    }

    fun confirmImport(mode: ImportMode) {
        val data = _uiState.value.pendingImportData ?: return
        val scope = _uiState.value.pendingImportScope ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            val result = importDataUseCase(data, scope, mode)
            _uiState.value = when (result) {
                is ImportResult.Success -> _uiState.value.copy(
                    isImporting = false,
                    pendingImportData = null,
                    pendingImportScope = null,
                    snackbarMessage = "已导入 ${result.activitiesImported} 条活动、${result.tagsImported} 条标签、${result.groupsImported} 个分组、${result.tagCategoriesImported} 个标签分类",
                )
                is ImportResult.Error -> _uiState.value.copy(
                    isImporting = false,
                    pendingImportData = null,
                    pendingImportScope = null,
                    snackbarMessage = "导入失败：${result.message}",
                )
            }
        }
    }

    fun dismissImportDialog() {
        _uiState.value = _uiState.value.copy(
            pendingImportData = null,
            pendingImportScope = null,
        )
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }
}
```

- [ ] **步骤 2：编译验证**

运行：`./gradlew :feature:settings:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add feature/settings/src/main/java/com/nltimer/feature/settings/ui/DataManagementViewModel.kt
git commit -m "feat: 添加 DataManagementViewModel"
```

---

### 任务 7：创建 ImportModeDialog 和 DataManagementScreen

**文件：**
- 创建：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/ImportModeDialog.kt`
- 创建：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/DataManagementScreen.kt`

- [ ] **步骤 1：创建 ImportModeDialog**

创建 `feature/settings/src/main/java/com/nltimer/feature/settings/ui/ImportModeDialog.kt`：

使用 Material3 `AlertDialog` + `RadioButton` 实现双选对话框。两个选项：
- **智能处理**：同名数据补全空字段，不同名新增，保留所有现有数据。适合合并数据或回导 AI 处理后的字段。
- **直接覆盖**：清空对应类型的现有数据后写入导入数据。适合设备迁移或全新开始。

默认选中"智能处理"。确认按钮触发 `onConfirm(selectedMode)`，取消按钮触发 `onDismiss()`。

- [ ] **步骤 2：创建 DataManagementScreen**

创建 `feature/settings/src/main/java/com/nltimer/feature/settings/ui/DataManagementScreen.kt`。

`DataManagementRoute` Composable：
- 注入 `DataManagementViewModel`
- 用 `rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument)` 处理导出文件创建
- 用 `rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument)` 处理导入文件选择
- 观察 `uiState.lastExportData` 变化，触发 `exportLauncher.launch()` 打开系统文件选择器
- 观察 `uiState.snackbarMessage` 显示 Snackbar
- 当 `uiState.pendingImportData != null` 时显示 `ImportModeDialog`

`DataManagementScreen` Composable（折叠卡片式）：
- LazyColumn 布局，包含以下 item：
  1. "导出到文件" ActionCard → `onExport(ExportScope.ALL)`
  2. "从文件导入" ActionCard → `onImport(ImportScope.ALL)`
  3. "活动数据" ExpandableSection → 导出/导入按钮
  4. "标签数据" ExpandableSection → 导出/导入按钮
  5. "分类数据" ExpandableSection（副标题"活动分组 + 标签分类"）
  6. "行为记录管理" ActionCard → `onNavigateToBehaviorManagement`

`ActionCard`：与 SettingsScreen 中 `SettingsEntryCard` 一致的卡片样式，可显示 loading CircularProgressIndicator。

`ExpandableSection`：Card 内顶部为标题行（点击切换展开/收起）+ 展开/收起 `IconButton`（使用 `ExpandMore`/`ExpandLess` icon），底部 `AnimatedVisibility` 包含"导出"和"导入"两个 `TextButton`。

- [ ] **步骤 3：编译验证**

运行：`./gradlew :feature:settings:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add feature/settings/src/main/java/com/nltimer/feature/settings/ui/ImportModeDialog.kt feature/settings/src/main/java/com/nltimer/feature/settings/ui/DataManagementScreen.kt
git commit -m "feat: 添加数据管理页面 UI（折叠卡片式布局 + 导入模式对话框）"
```

---

### 任务 8：注册导航路由与设置页入口

**文件：**
- 修改：`app/src/main/java/com/nltimer/app/navigation/NLtimerRoutes.kt`
- 修改：`app/src/main/java/com/nltimer/app/navigation/NLtimerNavHost.kt`
- 修改：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/SettingsScreen.kt`

- [ ] **步骤 1：在 NLtimerRoutes 中新增路由常量**

在 `NLtimerRoutes` object 中新增：
```kotlin
const val DATA_MANAGEMENT = "data_management"
```

在 `SETTINGS_FULLSCREEN_ROUTES` set 中新增 `DATA_MANAGEMENT`：
```kotlin
val SETTINGS_FULLSCREEN_ROUTES = setOf(THEME_SETTINGS, DIALOG_CONFIG, BEHAVIOR_MANAGEMENT, DATA_MANAGEMENT)
```

- [ ] **步骤 2：在 NLtimerNavHost 中注册路由**

添加 import：
```kotlin
import com.nltimer.feature.settings.ui.DataManagementRoute
```

修改 SettingsRoute 调用，新增导航回调：
```kotlin
composable(NLtimerRoutes.SETTINGS) {
    SettingsRoute(
        onNavigateToThemeSettings = { navController.navigate(NLtimerRoutes.THEME_SETTINGS) },
        onNavigateToDialogConfig = { navController.navigate(NLtimerRoutes.DIALOG_CONFIG) },
        onNavigateToDataManagement = { navController.navigate(NLtimerRoutes.DATA_MANAGEMENT) },
    )
}
```

新增路由注册（与其他全屏设置页面一致使用滑入/滑出动画）：
```kotlin
composable(
    NLtimerRoutes.DATA_MANAGEMENT,
    enterTransition = { slideInHorizontally { it } },
    exitTransition = { slideOutHorizontally { -it } },
    popEnterTransition = { slideInHorizontally { -it } },
    popExitTransition = { slideOutHorizontally { it } },
) {
    DataManagementRoute(
        onNavigateBack = { navController.popBackStack() },
        onNavigateToBehaviorManagement = { navController.navigate(NLtimerRoutes.BEHAVIOR_MANAGEMENT) },
    )
}
```

- [ ] **步骤 3：在 SettingsScreen 中新增数据管理入口**

在 `SettingsRoute` 中新增参数：
```kotlin
onNavigateToDataManagement: () -> Unit = {},
```

在 `SettingsScreen` 中新增参数：
```kotlin
onNavigateToDataManagement: () -> Unit = {},
```

添加 import：
```kotlin
import androidx.compose.material.icons.filled.Storage
```

在 LazyColumn 中（弹窗配置 item 之后）新增：
```kotlin
item {
    SettingsEntryCard(
        icon = Icons.Default.Storage,
        title = "数据管理",
        subtitle = "导出、导入与迁移应用数据",
        onClick = onNavigateToDataManagement,
    )
}
```

- [ ] **步骤 4：编译验证**

运行：`./gradlew :app:assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/nltimer/app/navigation/NLtimerRoutes.kt app/src/main/java/com/nltimer/app/navigation/NLtimerNavHost.kt feature/settings/src/main/java/com/nltimer/feature/settings/ui/SettingsScreen.kt
git commit -m "feat: 注册数据管理路由并添加设置页入口"
```

---

### 任务 9：端到端验证

**文件：** 无新增

- [ ] **步骤 1：全量编译**

运行：`./gradlew assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 2：运行所有测试**

运行：`./gradlew test`
预期：所有测试通过

- [ ] **步骤 3：Commit 最终状态（如有修复）**

```bash
git add -A
git commit -m "fix: 修复端到端验证中发现的问题"
```
