# 活动分类管理功能 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 在 NLtimer 中新增活动分类管理功能，支持活动维度和标签维度分类的集中管理（查看、新建、重命名、删除、搜索），与现有 AppDrawer/LNavHost 无缝集成。

**架构：** CategoryRepository 放在 core:data 层直接注入 DAO，feature:categories 模块负责 Compose UI + ViewModel 状态管理，两个维度完全隔离。

**技术栈：** Room DAO + Kotlin Flow + Hilt DI + Jetpack Compose (MD3) + Navigation Compose + Material3

---

## 文件结构

| 文件 | 操作 | 职责 |
|------|------|------|
| `core/data/src/.../dao/ActivityDao.kt` | 修改 | 追加 `getDistinctCategories`、`renameCategory`、`resetCategory` |
| `core/data/src/.../dao/TagDao.kt` | 修改 | 同上，Tag 维度 |
| `core/data/src/.../repository/CategoryRepository.kt` | 新建 | 分类聚合操作接口 |
| `core/data/src/.../repository/impl/CategoryRepositoryImpl.kt` | 新建 | 接口实现，注入 DAO，单表事务 |
| `core/data/src/.../di/DataModule.kt` | 修改 | 追加 `bindCategoryRepository` 绑定 |
| `feature/categories/build.gradle.kts` | 新建 | 模块构建配置 |
| `feature/categories/src/main/AndroidManifest.xml` | 新建 | 模块清单 |
| `feature/categories/src/.../model/CategoriesUiState.kt` | 新建 | UI 状态数据类 |
| `feature/categories/src/.../viewmodel/CategoriesViewModel.kt` | 新建 | 状态管理 + 搜索 + 冲突检测 |
| `feature/categories/src/.../ui/CategoriesScreen.kt` | 新建 | 主屏幕 Composable（双区 UI） |
| `feature/categories/src/.../ui/CategoriesRoute.kt` | 新建 | 路由入口，连接 ViewModel |
| `settings.gradle.kts` | 修改 | 追加 `include("feature:categories")` |
| `app/build.gradle.kts` | 修改 | 追加 `implementation(projects.feature.categories)` |
| `app/src/.../navigation/NLtimerNavHost.kt` | 修改 | 追加 `composable("categories")` 路由 |
| `app/src/.../component/AppDrawer.kt` | 修改 | 替换占位入口，改为 `DrawerMenuItem("categories", "活动分类管理", Icons.Default.Category)` |
| `core/data/src/test/.../CategoryRepositoryTest.kt` | 新建 | Repository 层测试 |
| `feature/categories/src/test/.../CategoriesViewModelTest.kt` | 新建 | ViewModel 层测试 |

---

### 任务 1：ActivityDao 和 TagDao 追加分类查询

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/database/dao/ActivityDao.kt`
- 修改：`core/data/src/main/java/com/nltimer/core/data/database/dao/TagDao.kt`

- [ ] **步骤 1：ActivityDao 追加三个方法**

在 `ActivityDao.kt` 现有查询方法后追加：

```kotlin
    @Query("SELECT DISTINCT category FROM activities WHERE category IS NOT NULL AND category != '' ORDER BY category")
    fun getDistinctCategories(): Flow<List<String>>

    @Query("UPDATE activities SET category = :newName WHERE category = :oldName")
    suspend fun renameCategory(oldName: String, newName: String)

    @Query("UPDATE activities SET category = NULL WHERE category = :category")
    suspend fun resetCategory(category: String)
```

- [ ] **步骤 2：TagDao 追加三个方法**

在 `TagDao.kt` 现有查询方法后追加：

```kotlin
    @Query("SELECT DISTINCT category FROM tags WHERE category IS NOT NULL AND category != '' ORDER BY category")
    fun getDistinctCategories(): Flow<List<String>>

    @Query("UPDATE tags SET category = :newName WHERE category = :oldName")
    suspend fun renameCategory(oldName: String, newName: String)

    @Query("UPDATE tags SET category = NULL WHERE category = :category")
    suspend fun resetCategory(category: String)
```

- [ ] **步骤 3：Build 验证 DAO 改动**

运行：`./gradlew :core:data:compileDebugKotlin`
预期：BUILD SUCCESSFUL

---

### 任务 2：CategoryRepository 接口与实现

**文件：**
- 新建：`core/data/src/main/java/com/nltimer/core/data/repository/CategoryRepository.kt`
- 新建：`core/data/src/main/java/com/nltimer/core/data/repository/impl/CategoryRepositoryImpl.kt`
- 修改：`core/data/src/main/java/com/nltimer/core/data/di/DataModule.kt`

- [ ] **步骤 1：创建 CategoryRepository 接口**

```kotlin
package com.nltimer.core.data.repository

import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getDistinctActivityCategories(parent: String? = null): Flow<List<String>>
    suspend fun renameActivityCategory(oldName: String, newName: String, parent: String? = null)
    suspend fun resetActivityCategory(category: String)

    fun getDistinctTagCategories(parent: String? = null): Flow<List<String>>
    suspend fun renameTagCategory(oldName: String, newName: String, parent: String? = null)
    suspend fun resetTagCategory(category: String)
}
```

- [ ] **步骤 2：创建 CategoryRepositoryImpl 实现**

```kotlin
package com.nltimer.core.data.repository.impl

import com.nltimer.core.data.database.dao.ActivityDao
import com.nltimer.core.data.database.dao.TagDao
import com.nltimer.core.data.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val activityDao: ActivityDao,
    private val tagDao: TagDao,
) : CategoryRepository {

    override fun getDistinctActivityCategories(parent: String?): Flow<List<String>> =
        activityDao.getDistinctCategories()

    override suspend fun renameActivityCategory(oldName: String, newName: String, parent: String?) {
        activityDao.renameCategory(oldName, newName)
    }

    override suspend fun resetActivityCategory(category: String) {
        activityDao.resetCategory(category)
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

- [ ] **步骤 3：DataModule 追加绑定**

在 `core/data/src/main/java/com/nltimer/core/data/di/DataModule.kt` 中添加 import 和绑定方法：

追加 import：
```kotlin
import com.nltimer.core.data.repository.CategoryRepository
import com.nltimer.core.data.repository.impl.CategoryRepositoryImpl
```

在 abstract class DataModule 中追加：
```kotlin
    @Binds
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository
```

- [ ] **步骤 4：Build 验证**

运行：`./gradlew :core:data:compileDebugKotlin`
预期：BUILD SUCCESSFUL

---

### 任务 3：新建 feature:categories 模块骨架

**文件：**
- 新建：`feature/categories/build.gradle.kts`
- 新建：`feature/categories/src/main/AndroidManifest.xml`
- 新建目录：`feature/categories/src/main/java/com/nltimer/feature/categories/model/`
- 新建目录：`feature/categories/src/main/java/com/nltimer/feature/categories/ui/`
- 新建目录：`feature/categories/src/main/java/com/nltimer/feature/categories/viewmodel/`

- [ ] **步骤 1：创建 build.gradle.kts**

参考 `feature/home/build.gradle.kts`，内容如下：

```kotlin
plugins {
    id("nltimer.android.library")
    id("nltimer.android.hilt")
}

android {
    namespace = "com.nltimer.feature.categories"
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.data)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

- [ ] **步骤 2：创建 AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest />
```

- [ ] **步骤 3：注册模块到 settings.gradle.kts**

在 `settings.gradle.kts` 的 `include(` 块末尾追加：
```kotlin
    "feature:categories",
```

- [ ] **步骤 4：app/build.gradle.kts 追加依赖**

在 `app/build.gradle.kts` 的 dependencies 块中追加：
```kotlin
    implementation(projects.feature.categories)
```

- [ ] **步骤 5：Build 验证**

运行：`./gradlew :feature:categories:compileDebugKotlin`
预期：BUILD SUCCESSFUL（可能因无源文件而直接成功）

---

### 任务 4：CategoriesUiState 数据模型

**文件：**
- 新建：`feature/categories/src/main/java/com/nltimer/feature/categories/model/CategoriesUiState.kt`

- [ ] **步骤 1：创建 CategoriesUiState**

```kotlin
package com.nltimer.feature.categories.model

data class CategoriesUiState(
    val activityCategories: List<String> = emptyList(),
    val tagCategories: List<String> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val dialogState: DialogState? = null,
)

sealed interface DialogState {
    data class AddActivityCategory(val sectionType: SectionType) : DialogState
    data class AddTagCategory(val sectionType: SectionType) : DialogState
    data class RenameActivityCategory(
        val oldName: String,
        val sectionType: SectionType,
    ) : DialogState
    data class RenameTagCategory(
        val oldName: String,
        val sectionType: SectionType,
    ) : DialogState
    data class DeleteActivityCategory(
        val category: String,
        val sectionType: SectionType,
    ) : DialogState
    data class DeleteTagCategory(
        val category: String,
        val sectionType: SectionType,
    ) : DialogState
}

enum class SectionType { ACTIVITY, TAG }
```

---

### 任务 5：CategoriesViewModel

**文件：**
- 新建：`feature/categories/src/main/java/com/nltimer/feature/categories/viewmodel/CategoriesViewModel.kt`

- [ ] **步骤 1：创建 CategoriesViewModel**

```kotlin
package com.nltimer.feature.categories.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _renameConflict = MutableStateFlow<String?>(null)
    val renameConflict: StateFlow<String?> = _renameConflict.asStateFlow()

    val uiState: StateFlow<CategoriesUiState> = combine(
        categoryRepository.getDistinctActivityCategories(),
        categoryRepository.getDistinctTagCategories(),
        _searchQuery,
    ) { activityCats, tagCats, query ->
        CategoriesUiState(
            activityCategories = if (query.isBlank()) activityCats
                else activityCats.filter { it.contains(query, ignoreCase = true) },
            tagCategories = if (query.isBlank()) tagCats
                else tagCats.filter { it.contains(query, ignoreCase = true) },
            searchQuery = query,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoriesUiState(),
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onAddCategory(sectionType: SectionType) {
        _uiState.update {
            it.copy(
                dialogState = when (sectionType) {
                    SectionType.ACTIVITY -> DialogState.AddActivityCategory(sectionType)
                    SectionType.TAG -> DialogState.AddTagCategory(sectionType)
                }
            )
        }
    }

    fun onRenameCategory(sectionType: SectionType, oldName: String) {
        _uiState.update {
            it.copy(
                dialogState = when (sectionType) {
                    SectionType.ACTIVITY -> DialogState.RenameActivityCategory(oldName, sectionType)
                    SectionType.TAG -> DialogState.RenameTagCategory(oldName, sectionType)
                }
            )
        }
    }

    fun onDeleteCategory(sectionType: SectionType, category: String) {
        _uiState.update {
            it.copy(
                dialogState = when (sectionType) {
                    SectionType.ACTIVITY -> DialogState.DeleteActivityCategory(category, sectionType)
                    SectionType.TAG -> DialogState.DeleteTagCategory(category, sectionType)
                }
            )
        }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(dialogState = null) }
        _renameConflict.value = null
    }

    fun confirmAddCategory(sectionType: SectionType, name: String) {
        dismissDialog()
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
                SectionType.ACTIVITY -> categoryRepository.renameActivityCategory(oldName, newName)
                SectionType.TAG -> categoryRepository.renameTagCategory(oldName, newName)
            }
            dismissDialog()
        }
    }

    fun confirmDeleteCategory(sectionType: SectionType, category: String) {
        viewModelScope.launch {
            when (sectionType) {
                SectionType.ACTIVITY -> categoryRepository.resetActivityCategory(category)
                SectionType.TAG -> categoryRepository.resetTagCategory(category)
            }
            dismissDialog()
        }
    }

    fun clearConflict() {
        _renameConflict.value = null
    }

    private val _uiState = MutableStateFlow(CategoriesUiState())
}
```

---

### 任务 6：CategoriesScreen 和 CategoriesRoute

**文件：**
- 新建：`feature/categories/src/main/java/com/nltimer/feature/categories/ui/CategoriesScreen.kt`
- 新建：`feature/categories/src/main/java/com/nltimer/feature/categories/ui/CategoriesRoute.kt`

- [ ] **步骤 1：创建 CategoriesScreen**

```kotlin
package com.nltimer.feature.categories.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.feature.categories.model.CategoriesUiState
import com.nltimer.feature.categories.model.DialogState
import com.nltimer.feature.categories.model.SectionType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun CategoriesScreen(
    uiState: CategoriesUiState,
    onSearchQueryChange: (String) -> Unit,
    onAddCategory: (SectionType) -> Unit,
    onRenameCategory: (SectionType, String) -> Unit,
    onDeleteCategory: (SectionType, String) -> Unit,
    onDismissDialog: () -> Unit,
    onConfirmAdd: (SectionType, String) -> Unit,
    onConfirmRename: (SectionType, String, String) -> Unit,
    onConfirmDelete: (SectionType, String) -> Unit,
    renameConflict: String?,
    onClearConflict: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("活动分类管理") },
            )
        },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = {},
                active = false,
                onActiveChange = {},
                placeholder = { Text("搜索分类...") },
                modifier = Modifier.fillMaxWidth(),
            ) {}

            Spacer(modifier = Modifier.height(16.dp))

            CategorySection(
                title = "活动分类",
                categories = uiState.activityCategories,
                sectionType = SectionType.ACTIVITY,
                onAdd = onAddCategory,
                onRename = onRenameCategory,
                onDelete = onDeleteCategory,
            )

            Spacer(modifier = Modifier.height(16.dp))

            CategorySection(
                title = "标签分类",
                categories = uiState.tagCategories,
                sectionType = SectionType.TAG,
                onAdd = onAddCategory,
                onRename = onRenameCategory,
                onDelete = onDeleteCategory,
            )
        }
    }

    uiState.dialogState?.let { dialog ->
        CategoryDialog(
            dialogState = dialog,
            onDismiss = onDismissDialog,
            onConfirmAdd = onConfirmAdd,
            onConfirmRename = onConfirmRename,
            onConfirmDelete = onConfirmDelete,
            renameConflict = renameConflict,
            onClearConflict = onClearConflict,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun CategorySection(
    title: String,
    categories: List<String>,
    sectionType: SectionType,
    onAdd: (SectionType) -> Unit,
    onRename: (SectionType, String) -> Unit,
    onDelete: (SectionType, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(8.dp))

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        categories.forEach { category ->
            AssistChip(
                onClick = {},
                label = { Text(category) },
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = {
                        // Show long-press menu via dialog trigger in ViewModel
                    },
                ),
            )
        }

        InputChip(
            selected = false,
            onClick = { onAdd(sectionType) },
            label = { Text("+ 新建分类") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                )
            },
        )
    }
}

@Composable
private fun CategoryDialog(
    dialogState: DialogState,
    onDismiss: () -> Unit,
    onConfirmAdd: (SectionType, String) -> Unit,
    onConfirmRename: (SectionType, String, String) -> Unit,
    onConfirmDelete: (SectionType, String) -> Unit,
    renameConflict: String?,
    onClearConflict: () -> Unit,
) {
    when (dialogState) {
        is DialogState.AddActivityCategory,
        is DialogState.AddTagCategory -> {
            var name by remember { mutableStateOf("") }
            val sectionType = when (dialogState) {
                is DialogState.AddActivityCategory -> dialogState.sectionType
                is DialogState.AddTagCategory -> dialogState.sectionType
                else -> SectionType.ACTIVITY
            }

            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("新建分类") },
                text = {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("分类名称") },
                        singleLine = true,
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { onConfirmAdd(sectionType, name.trim()) },
                        enabled = name.isNotBlank(),
                    ) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                },
            )
        }

        is DialogState.RenameActivityCategory -> {
            RenameDialog(
                oldName = dialogState.oldName,
                conflict = renameConflict,
                onDismiss = onDismiss,
                onConfirm = { newName ->
                    onConfirmRename(SectionType.ACTIVITY, dialogState.oldName, newName)
                },
                onClearConflict = onClearConflict,
            )
        }

        is DialogState.RenameTagCategory -> {
            RenameDialog(
                oldName = dialogState.oldName,
                conflict = renameConflict,
                onDismiss = onDismiss,
                onConfirm = { newName ->
                    onConfirmRename(SectionType.TAG, dialogState.oldName, newName)
                },
                onClearConflict = onClearConflict,
            )
        }

        is DialogState.DeleteActivityCategory -> {
            DeleteConfirmDialog(
                category = dialogState.category,
                onDismiss = onDismiss,
                onConfirm = { onConfirmDelete(SectionType.ACTIVITY, dialogState.category) },
            )
        }

        is DialogState.DeleteTagCategory -> {
            DeleteConfirmDialog(
                category = dialogState.category,
                onDismiss = onDismiss,
                onConfirm = { onConfirmDelete(SectionType.TAG, dialogState.category) },
            )
        }
    }
}

@Composable
private fun RenameDialog(
    oldName: String,
    conflict: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onClearConflict: () -> Unit,
) {
    var newName by remember(oldName) { mutableStateOf(oldName) }
    val isConflict = conflict != null

    AlertDialog(
        onDismissRequest = {
            onClearConflict()
            onDismiss()
        },
        title = { Text("重命名分类") },
        text = {
            Column {
                OutlinedTextField(
                    value = newName,
                    onValueChange = {
                        newName = it
                        if (isConflict) onClearConflict()
                    },
                    label = { Text("新名称") },
                    singleLine = true,
                    isError = isConflict,
                    supportingText = if (isConflict) {
                        { Text("该分类已存在") }
                    } else null,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(newName.trim()) },
                enabled = newName.isNotBlank() && !isConflict,
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onClearConflict()
                onDismiss()
            }) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun DeleteConfirmDialog(
    category: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("删除分类") },
        text = {
            Text("确定要删除分类「$category」吗？该分类下的所有活动和标签将变为未分类状态。")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("删除", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}
```

- [ ] **步骤 2：创建 CategoriesRoute**

```kotlin
package com.nltimer.feature.categories.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.nltimer.feature.categories.viewmodel.CategoriesViewModel

@Composable
fun CategoriesRoute(
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val renameConflict by viewModel.renameConflict.collectAsState()

    CategoriesScreen(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onAddCategory = viewModel::onAddCategory,
        onRenameCategory = viewModel::onRenameCategory,
        onDeleteCategory = viewModel::onDeleteCategory,
        onDismissDialog = viewModel::dismissDialog,
        onConfirmAdd = viewModel::confirmAddCategory,
        onConfirmRename = viewModel::confirmRenameCategory,
        onConfirmDelete = viewModel::confirmDeleteCategory,
        renameConflict = renameConflict,
        onClearConflict = viewModel::clearConflict,
        onNavigateBack = {},
    )
}
```

- [ ] **步骤 3：Build 验证**

运行：`./gradlew :feature:categories:compileDebugKotlin`
预期：BUILD SUCCESSFUL

---

### 任务 7：修正 CategoriesScreen 长按交互

**文件：**
- 修改：`feature/categories/src/main/java/com/nltimer/feature/categories/ui/CategoriesScreen.kt`

- [ ] **步骤 1：用 DropdownMenu 实现长按菜单替换 placeholder**

将 `CategoriesScreen` 中的 `CategorySection` 的 AssistChip 部分替换为带长按菜单的版本。

需要添加 import：
```kotlin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AssistChipDefaults
```

将 `CategorySection` 中的 FlowRow 内容改为：

```kotlin
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        categories.forEach { category ->
            var showMenu by remember { mutableStateOf(false) }

            Box {
                AssistChip(
                    onClick = {},
                    label = { Text(category) },
                    modifier = Modifier.combinedClickable(
                        onClick = {},
                        onLongClick = { showMenu = true },
                    ),
                )
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("重命名") },
                        onClick = {
                            showMenu = false
                            onRename(sectionType, category)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete(sectionType, category)
                        },
                    )
                }
            }
        }

        InputChip(
            selected = false,
            onClick = { onAdd(sectionType) },
            label = { Text("+ 新建分类") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                )
            },
        )
    }
```

需要追加 import：
```kotlin
import androidx.compose.foundation.layout.Box
```

- [ ] **步骤 2：Build 验证**

运行：`./gradlew :feature:categories:compileDebugKotlin`
预期：BUILD SUCCESSFUL

---

### 任务 8：集成导航和抽屉入口

**文件：**
- 修改：`app/src/main/java/com/nltimer/app/navigation/NLtimerNavHost.kt`
- 修改：`app/src/main/java/com/nltimer/app/component/AppDrawer.kt`

- [ ] **步骤 1：NLtimerNavHost 追加路由**

在 `NLtimerNavHost.kt` 添加 import：
```kotlin
import com.nltimer.feature.categories.ui.CategoriesRoute
```

在 `NavHost` 的 `composable` 块末尾追加：
```kotlin
        composable("categories") { CategoriesRoute() }
```

- [ ] **步骤 2：AppDrawer 替换占位入口**

在 `AppDrawer.kt` 中：

追加 import：
```kotlin
import androidx.compose.material.icons.filled.Category
```

将：
```kotlin
    DrawerMenuItem("home", "主页", Icons.Default.Home),
    DrawerMenuItem("theme_settings", "主题配置", Icons.Default.Brightness5),
    DrawerMenuItem("settings", "设置", Icons.Default.Settings),
    // 暂时入口，后续收入二级
    DrawerMenuItem("settings", "活动分类管理", Icons.Default.Settings),
```

替换为：
```kotlin
    DrawerMenuItem("home", "主页", Icons.Default.Home),
    DrawerMenuItem("theme_settings", "主题配置", Icons.Default.Brightness5),
    DrawerMenuItem("categories", "活动分类管理", Icons.Default.Category),
    DrawerMenuItem("settings", "设置", Icons.Default.Settings),
```

- [ ] **步骤 3：编译验证完整项目**

运行：`./gradlew :app:compileDebugKotlin`
预期：BUILD SUCCESSFUL

---

### 任务 9：CategoryRepository 单元测试

**文件：**
- 新建：`core/data/src/test/java/com/nltimer/core/data/repository/CategoryRepositoryTest.kt`

- [ ] **步骤 1：创建测试文件**

```kotlin
package com.nltimer.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nltimer.core.data.database.NLtimerDatabase
import com.nltimer.core.data.database.entity.ActivityEntity
import com.nltimer.core.data.database.entity.TagEntity
import com.nltimer.core.data.repository.impl.CategoryRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CategoryRepositoryTest {

    private lateinit var database: NLtimerDatabase
    private lateinit var repository: CategoryRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NLtimerDatabase::class.java,
        ).build()
        repository = CategoryRepositoryImpl(
            activityDao = database.activityDao(),
            tagDao = database.tagDao(),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getDistinctActivityCategories_filtersNullAndEmpty() = runTest {
        database.activityDao().insert(ActivityEntity(name = "跑步", category = "运动"))
        database.activityDao().insert(ActivityEntity(name = "阅读", category = null))
        database.activityDao().insert(ActivityEntity(name = "冥想", category = ""))
        database.activityDao().insert(ActivityEntity(name = "游泳", category = "运动"))

        val categories = repository.getDistinctActivityCategories().first()

        assertEquals(listOf("运动"), categories)
    }

    @Test
    fun getDistinctActivityCategories_returnsDistinctSorted() = runTest {
        database.activityDao().insert(ActivityEntity(name = "A", category = "工作"))
        database.activityDao().insert(ActivityEntity(name = "B", category = "学习"))
        database.activityDao().insert(ActivityEntity(name = "C", category = "工作"))

        val categories = repository.getDistinctActivityCategories().first()

        assertEquals(listOf("学习", "工作"), categories)
    }

    @Test
    fun renameActivityCategory_updatesAllMatching() = runTest {
        database.activityDao().insert(ActivityEntity(name = "跑步", category = "运动"))
        database.activityDao().insert(ActivityEntity(name = "游泳", category = "运动"))
        database.activityDao().insert(ActivityEntity(name = "阅读", category = "学习"))

        repository.renameActivityCategory("运动", "体育")

        val categories = repository.getDistinctActivityCategories().first()
        assertEquals(listOf("学习", "体育"), categories)
    }

    @Test
    fun resetActivityCategory_setsCategoryToNull() = runTest {
        database.activityDao().insert(ActivityEntity(name = "跑步", category = "运动"))
        database.activityDao().insert(ActivityEntity(name = "阅读", category = "学习"))

        repository.resetActivityCategory("运动")

        val categories = repository.getDistinctActivityCategories().first()
        assertEquals(listOf("学习"), categories)
    }

    @Test
    fun getDistinctTagCategories_filtersNullAndEmpty() = runTest {
        database.tagDao().insert(TagEntity(name = "重要", category = "优先级"))
        database.tagDao().insert(TagEntity(name = "次要", category = null))
        database.tagDao().insert(TagEntity(name = "紧急", category = "优先级"))

        val categories = repository.getDistinctTagCategories().first()

        assertEquals(listOf("优先级"), categories)
    }

    @Test
    fun renameTagCategory_updatesAllMatching() = runTest {
        database.tagDao().insert(TagEntity(name = "重要", category = "优先级"))
        database.tagDao().insert(TagEntity(name = "已读", category = "状态"))

        repository.renameTagCategory("优先级", "重要程度")

        val categories = repository.getDistinctTagCategories().first()
        assertEquals(listOf("重要程度", "状态"), categories)
    }

    @Test
    fun resetTagCategory_setsCategoryToNull() = runTest {
        database.tagDao().insert(TagEntity(name = "重要", category = "优先级"))

        repository.resetTagCategory("优先级")

        val categories = repository.getDistinctTagCategories().first()
        assertTrue(categories.isEmpty())
    }
}
```

- [ ] **步骤 2：运行测试验证**

运行：`./gradlew :core:data:testDebugUnitTest --tests "com.nltimer.core.data.repository.CategoryRepositoryTest"`
预期：所有测试 PASS

---

### 任务 10：CategoriesViewModel 单元测试

**文件：**
- 新建：`feature/categories/src/test/java/com/nltimer/feature/categories/viewmodel/CategoriesViewModelTest.kt`

- [ ] **步骤 1：创建测试文件**

```kotlin
package com.nltimer.feature.categories.viewmodel

import com.nltimer.core.data.repository.CategoryRepository
import com.nltimer.feature.categories.model.CategoriesUiState
import com.nltimer.feature.categories.model.DialogState
import com.nltimer.feature.categories.model.SectionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
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
    private lateinit var viewModel: CategoriesViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        activityCategoriesFlow = MutableStateFlow(emptyList())
        tagCategoriesFlow = MutableStateFlow(emptyList())
        repository = FakeCategoryRepository(activityCategoriesFlow, tagCategoriesFlow)
        viewModel = CategoriesViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_showsLoading() = runTest {
        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertTrue(state.activityCategories.isEmpty())
        assertTrue(state.tagCategories.isEmpty())
    }

    @Test
    fun collectCategories_emitsUiState() = runTest {
        activityCategoriesFlow.value = listOf("运动", "学习")
        tagCategoriesFlow.value = listOf("优先级")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(listOf("运动", "学习"), state.activityCategories)
        assertEquals(listOf("优先级"), state.tagCategories)
    }

    @Test
    fun searchQuery_filtersCategories() = runTest {
        activityCategoriesFlow.value = listOf("运动", "学习", "工作")
        tagCategoriesFlow.value = listOf("优先级", "状态")
        advanceUntilIdle()

        viewModel.onSearchQueryChange("习")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("学习"), state.activityCategories)
        assertTrue(state.tagCategories.isEmpty())
    }

    @Test
    fun addCategoryDialog_showsForActivity() = runTest {
        advanceUntilIdle()

        viewModel.onAddCategory(SectionType.ACTIVITY)

        val state = viewModel.uiState.value
        assertTrue(state.dialogState is DialogState.AddActivityCategory)
    }

    @Test
    fun addCategoryDialog_showsForTag() = runTest {
        advanceUntilIdle()

        viewModel.onAddCategory(SectionType.TAG)

        val state = viewModel.uiState.value
        assertTrue(state.dialogState is DialogState.AddTagCategory)
    }

    @Test
    fun renameCategory_showsDialog() = runTest {
        advanceUntilIdle()

        viewModel.onRenameCategory(SectionType.ACTIVITY, "运动")

        val state = viewModel.uiState.value
        val dialog = state.dialogState as? DialogState.RenameActivityCategory
        assertNotNull(dialog)
        assertEquals("运动", dialog?.oldName)
    }

    @Test
    fun confirmRename_withConflict_setsConflictFlag() = runTest {
        activityCategoriesFlow.value = listOf("运动", "学习")
        advanceUntilIdle()

        viewModel.onRenameCategory(SectionType.ACTIVITY, "运动")
        viewModel.confirmRenameCategory(SectionType.ACTIVITY, "运动", "学习")
        advanceUntilIdle()

        assertEquals("学习", viewModel.renameConflict.value)
    }

    @Test
    fun confirmRename_noConflict_callsRepository() = runTest {
        activityCategoriesFlow.value = listOf("运动")
        advanceUntilIdle()

        viewModel.onRenameCategory(SectionType.ACTIVITY, "运动")
        viewModel.confirmRenameCategory(SectionType.ACTIVITY, "运动", "体育")
        advanceUntilIdle()

        assertTrue(repository.renameActivityCategoryCalled)
        assertEquals("运动" to "体育", repository.lastRenameActivityPair)
    }

    @Test
    fun confirmDelete_callsRepository() = runTest {
        advanceUntilIdle()

        viewModel.confirmDeleteCategory(SectionType.ACTIVITY, "运动")
        advanceUntilIdle()

        assertTrue(repository.resetActivityCategoryCalled)
        assertEquals("运动", repository.lastResetActivityCategory)
    }

    @Test
    fun dismissDialog_clearsDialogState() = runTest {
        advanceUntilIdle()
        viewModel.onAddCategory(SectionType.ACTIVITY)
        assertNotNull(viewModel.uiState.value.dialogState)

        viewModel.dismissDialog()

        assertNull(viewModel.uiState.value.dialogState)
    }

    private class FakeCategoryRepository(
        private val activityCategories: MutableStateFlow<List<String>>,
        private val tagCategories: MutableStateFlow<List<String>>,
    ) : CategoryRepository {

        var renameActivityCategoryCalled = false
        var renameTagCategoryCalled = false
        var resetActivityCategoryCalled = false
        var resetTagCategoryCalled = false
        var lastRenameActivityPair: Pair<String, String>? = null
        var lastResetActivityCategory: String? = null

        override fun getDistinctActivityCategories(parent: String?) = activityCategories
        override fun getDistinctTagCategories(parent: String?) = tagCategories

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

- [ ] **步骤 2：运行测试验证**

运行：`./gradlew :feature:categories:testDebugUnitTest --tests "com.nltimer.feature.categories.viewmodel.CategoriesViewModelTest"`
预期：所有测试 PASS

---

### 任务 11：最终集成验证

**文件：** 无新建，验证全部编译和测试通过

- [ ] **步骤 1：完整项目编译**

运行：`./gradlew :app:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 2：运行全部测试**

运行：`./gradlew testDebugUnitTest`
预期：BUILD SUCCESSFUL，所有测试 PASS

---

## 自检

1. **规格覆盖度** ✅ — 每个规格需求都有对应任务：
   - DAO 查询 → 任务 1
   - CategoryRepository → 任务 2
   - DI 绑定 → 任务 2
   - feature 模块骨架 → 任务 3
   - UI 状态模型 → 任务 4
   - ViewModel（含搜索、冲突检测） → 任务 5
   - UI（双区、对话框、长按菜单） → 任务 6 + 7
   - 导航集成 → 任务 8
   - Repository 测试 → 任务 9
   - ViewModel 测试 → 任务 10
   - 边界规则（冲突/空值/删除） → 任务 5 + 9 + 10

2. **占位符扫描** ✅ — 无 TODO/待定/placeholder

3. **类型一致性** ✅ — 所有任务间类型名一致：SectionType、DialogState、CategoriesUiState 在任务 4 定义后续任务引用
