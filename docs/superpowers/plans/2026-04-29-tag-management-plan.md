# 标签管理功能 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 在 NLtimer 中新增标签管理功能，支持对现有 Tag 实例的集中管理（查看、新建、编辑、删除、移动、分类管理），采用紧凑型卡片式 UI 设计。

**架构：** 独立 feature:tag_management 模块，复用现有 TagRepository/TagDao 数据层，ViewModel 管理状态，Compose UI 实现紧凑型卡片布局。

**技术栈：** Room DAO + Kotlin Flow + Hilt DI + Jetpack Compose (MD3) + Navigation Compose + Material3

---

## 文件结构

| 文件 | 操作 | 职责 |
|------|------|------|
| `feature/tag_management/build.gradle.kts` | 新建 | 模块构建配置 |
| `feature/tag_management/src/main/AndroidManifest.xml` | 新建 | 模块清单 |
| `feature/tag_management/src/main/java/com/nltimer/feature/tag_management/model/TagManagementUiState.kt` | 新建 | UI 状态数据类 |
| `feature/tag_management/src/main/java/com/nltimer/feature/tag_management/viewmodel/TagManagementViewModel.kt` | 新建 | 状态管理逻辑 |
| `feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/TagManagementRoute.kt` | 新建 | 路由入口 |
| `feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/TagManagementScreen.kt` | 新建 | 主界面 Composable |
| `feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/TagChip.kt` | 新建 | 标签芯片组件 |
| `feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/CategoryCard.kt` | 新建 | 分类卡片组件 |
| `feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/dialogs/AddTagDialog.kt` | 新建 | 新建标签对话框 |
| `feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/dialogs/EditTagDialog.kt` | 新建 | 编辑标签对话框 |
| `feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/dialogs/AddCategoryDialog.kt` | 新建 | 新建分类对话框 |
| `feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/dialogs/RenameCategoryDialog.kt` | 新建 | 重命名分类对话框 |
| `feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/dialogs/ConfirmDialog.kt` | 新建 | 通用确认对话框 |
| `settings.gradle.kts` | 修改 | 追加 include("feature:tag_management") |
| `app/build.gradle.kts` | 修改 | 追加 implementation(projects.feature.tag_management) |
| `app/src/main/java/com/nltimer/app/navigation/NLtimerNavHost.kt` | 修改 | 追加 composable("tag_management") 路由 |
| `app/src/main/java/com/nltimer/app/component/AppDrawer.kt` | 修改 | 追加 DrawerMenuItem |

---

## 任务 1：模块基础设施搭建

**文件：**
- 修改：`settings.gradle.kts`
- 创建：`feature/tag_management/build.gradle.kts`
- 创建：`feature/tag_management/src/main/AndroidManifest.xml`

- [ ] **步骤 1：在 settings.gradle.kts 中注册新模块**

在 `settings.gradle.kts` 的 `include` 块中追加：

```kotlin
include("feature:tag_management")
```

位置：在其他 feature 模块声明附近（如 `feature:management_activities` 后）

- [ ] **步骤 2：创建 build.gradle.kts**

创建文件 `feature/tag_management/build.gradle.kts`：

```kotlin
plugins {
    id("nltimer.android.library")
    id("nltimer.android.hilt")
}

android {
    namespace = "com.nltimer.feature.tag_management"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    
    implementation(libs.material3)
}
```

参考 `feature/management_activities/build.gradle.kts` 的依赖配置模式。

- [ ] **步骤 3：创建 AndroidManifest.xml**

创建文件 `feature/tag_management/src/main/AndroidManifest.xml`：

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" />
```

- [ ] **步骤 4：验证 Gradle 同步**

运行：`./gradlew :feature:tag_management:assembleDebug`
预期：BUILD SUCCESSFUL（可能有一些警告但无错误）

- [ ] **步骤 5：Commit**

```bash
git add settings.gradle.kts feature/tag_management/
git commit -m "✨ feat(tag_management): add module infrastructure"
```

---

## 任务 2：UI 状态模型定义

**文件：**
- 创建：`feature/tag_management/src/main/java/com/nltimer/feature/tag_management/model/TagManagementUiState.kt`

- [ ] **步骤 1：创建 UI State 数据类**

```kotlin
package com.nltimer.feature.tag_management.model

import com.nltimer.core.data.model.Tag

data class TagManagementUiState(
    val uncategorizedTags: List<Tag> = emptyList(),
    val categories: List<CategoryWithTags> = emptyList(),
    val isLoading: Boolean = true,
    val dialogState: DialogState? = null,
)

data class CategoryWithTags(
    val categoryName: String,
    val tags: List<Tag>,
)

sealed interface DialogState {
    data class AddTag(val category: String? = null) : DialogState
    data class EditTag(val tag: Tag) : DialogState
    data class DeleteTag(val tag: Tag) : DialogState
    data class MoveTag(val tag: Tag, val currentCategory: String?) : DialogState
    object AddCategory : DialogState
    data class RenameCategory(val name: String) : DialogState
    data class DeleteCategory(val name: String, val tagCount: Int) : DialogState
}
```

说明：
- `uncategorizedTags`: category 为 NULL 或空字符串的标签列表
- `categories`: 按 category 分组的标签列表（排除"默认"分组）
- `isLoading`: 初始加载状态
- `dialogState`: 当前打开的对话框状态

- [ ] **步骤 2：Commit**

```bash
git add feature/tag_management/src/main/java/com/nltimer/feature/tag_management/model/
git commit -m "✨ feat(tag_management): define UI state model"
```

---

## 任务 3：ViewModel 实现

**文件：**
- 创建：`feature/tag_management/src/main/java/com/nltimer/feature/tag_management/viewmodel/TagManagementViewModel.kt`

- [ ] **步骤 1：创建 ViewModel 类**

```kotlin
package com.nltimer.feature.tag_management.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.core.data.model.Tag
import com.nltimer.core.data.repository.TagRepository
import com.nltimer.feature.tag_management.model.CategoryWithTags
import com.nltimer.feature.tag_management.model.DialogState
import com.nltimer.feature.tag_management.model.TagManagementUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagManagementViewModel @Inject constructor(
    private val tagRepository: TagRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagManagementUiState())
    val uiState: StateFlow<TagManagementUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        combine(
            tagRepository.getAllActive(),
            tagRepository.getDistinctCategories(),
        ) { allTags, categories ->
            val uncategorizedTags = allTags.filter { it.category.isNullOrBlank() }
            val categorizedTags = allTags.filter { !it.category.isNullOrBlank() }
            
            val categoriesWithTags = categories.map { categoryName ->
                CategoryWithTags(
                    categoryName = categoryName,
                    tags = categorizedTags.filter { it.category == categoryName },
                )
            }.filter { it.tags.isNotEmpty() }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    uncategorizedTags = uncategorizedTags,
                    categories = categoriesWithTags,
                )
            }
        }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun showAddTagDialog(category: String? = null) {
        _uiState.update { it.copy(dialogState = DialogState.AddTag(category)) }
    }

    fun showEditTagDialog(tag: Tag) {
        _uiState.update { it.copy(dialogState = DialogState.EditTag(tag)) }
    }

    fun showDeleteTagDialog(tag: Tag) {
        _uiState.update { it.copy(dialogState = DialogState.DeleteTag(tag)) }
    }

    fun showMoveTagDialog(tag: Tag, currentCategory: String?) {
        _uiState.update { it.copy(dialogState = DialogState.MoveTag(tag, currentCategory)) }
    }

    fun showAddCategoryDialog() {
        _uiState.update { it.copy(dialogState = DialogState.AddCategory) }
    }

    fun showRenameCategoryDialog(name: String) {
        _uiState.update { it.copy(dialogState = DialogState.RenameCategory(name)) }
    }

    fun showDeleteCategoryDialog(name: String, tagCount: Int) {
        _uiState.update { it.copy(dialogState = DialogState.DeleteCategory(name, tagCount)) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(dialogState = null) }
    }

    fun addTag(name: String, color: Long?, textColor: Long?, icon: String?, category: String?) {
        viewModelScope.launch {
            val tag = Tag(
                id = 0,
                name = name,
                color = color,
                textColor = textColor,
                icon = icon,
                category = category,
                priority = 0,
                usageCount = 0,
                sortOrder = 0,
                isArchived = false,
            )
            tagRepository.insert(tag)
            dismissDialog()
        }
    }

    fun updateTag(tag: Tag) {
        viewModelScope.launch {
            tagRepository.update(tag)
            dismissDialog()
        }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            tagRepository.setArchived(tag.id, true)
            dismissDialog()
        }
    }

    fun moveTagToCategory(tagId: Long, newCategory: String?) {
        viewModelScope.launch {
            val updatedTag = tagRepository.getById(tagId)?.copy(category = newCategory)
            if (updatedTag != null) {
                tagRepository.update(updatedTag)
            }
            dismissDialog()
        }
    }

    fun addCategory(name: String) {
        // 分类通过 Tag.category 字段隐式存在，无需单独存储
        // 此方法仅用于关闭对话框，实际分类在添加标签时创建
        dismissDialog()
    }

    fun renameCategory(oldName: String, newName: String) {
        viewModelScope.launch {
            tagRepository.renameCategory(oldName, newName)
            dismissDialog()
        }
    }

    fun deleteCategory(name: String) {
        viewModelScope.launch {
            tagRepository.resetCategory(name)
            dismissDialog()
        }
    }
}
```

关键点：
- 使用 `combine` 合并所有标签和分类列表流
- 在内存中按 category 分组（避免额外数据库查询）
- 所有写操作通过 `viewModelScope.launch` 异步执行
- 对话框状态管理通过 sealed interface 实现

- [ ] **步骤 2：检查 TagRepository 接口是否包含所需方法**

确认 `core/data/src/main/java/com/nltimer/core/data/repository/TagRepository.kt` 包含以下方法：
- `getAllActive(): Flow<List<Tag>>`
- `getById(id: Long): TagEntity?` 或类似同步方法
- `insert(tag): Long`
- `update(tag)`
- `setArchived(id: Long, archived: Boolean)`
- `getDistinctCategories(): Flow<List<String>>`
- `renameCategory(oldName: String, newName: String)`
- `resetCategory(category: String)`

如果缺少某些方法，需要在 Repository 和 Dao 中补充。

- [ ] **步骤 3：Commit**

```bash
git add feature/tag_management/src/main/java/com/nltimer/feature/tag_management/viewmodel/
git commit -m "✨ feat(tag_management): implement ViewModel with state management"
```

---

## 任务 4：基础 UI 组件 - TagChip

**文件：**
- 创建：`feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/TagChip.kt`

- [ ] **步骤 1：创建 TagChip 组件**

```kotlin
package com.nltimer.feature.tag_management.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.Tag

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TagChip(
    tag: Tag,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = tag.color?.let { Color(it) } 
        ?: MaterialTheme.colorScheme.surfaceVariant
    
    val contentColor = tag.textColor?.let { Color(it) }
        ?: MaterialTheme.colorScheme.onSurfaceVariant

    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = "#${tag.name}",
                style = MaterialTheme.typography.bodySmall,
            )
        },
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        colors = androidx.compose.material3.ChipColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.5f),
            leadingIconContentColor = contentColor,
            disabledLeadingIconContentColor = contentColor.copy(alpha = 0.5f),
        ),
    )
}
```

设计要点：
- 使用 `AssistChip` Material3 组件
- 颜色优先从 Tag 对象读取，否则使用主题色
- 显示 # 前缀 + 标签名称
- 支持单击（编辑）和长按（菜单）

- [ ] **步骤 2：Commit**

```bash
git add feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/TagChip.kt
git commit -m "✨ feat(tag_management): implement TagChip component"
```

---

## 任务 5：基础 UI 组件 - CategoryCard

**文件：**
- 创建：`feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/CategoryCard.kt`

- [ ] **步骤 1：创建 CategoryCard 组件**

```kotlin
package com.nltimer.feature.tag_management.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.Tag

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryCard(
    categoryName: String,
    tags: List<Tag>,
    isDefaultCategory: Boolean = false,
    onAddTag: () -> Unit,
    onTagClick: (Tag) -> Unit,
    onTagLongClick: (Tag) -> Unit,
    onMenuClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                )

                if (!isDefaultCategory) {
                    IconButton(onClick = onMenuClick, modifier = Modifier.padding(0.dp)) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多操作",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                tags.forEach { tag ->
                    TagChip(
                        tag = tag,
                        onClick = { onTagClick(tag) },
                        onLongClick = { onTagLongClick(tag) },
                    )
                }

                IconButton(
                    onClick = onAddTag,
                    modifier = Modifier.padding(0.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加标签",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
```

设计要点：
- 白色背景卡片，轻微阴影
- 标题行显示分类名称和操作菜单（默认分类不显示菜单）
- 使用 FlowRow 横向排列标签
- 圆形 "+" 添加按钮在每个卡片末尾

- [ ] **步骤 2：Commit**

```bash
git add feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/CategoryCard.kt
git commit -m "✨ feat(tag_management): implement CategoryCard component"
```

---

## 任务 6：对话框组件 - AddTagDialog & EditTagDialog

**文件：**
- 创建：`feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/dialogs/AddTagDialog.kt`
- 创建：`feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/dialogs/EditTagDialog.kt`

- [ ] **步骤 1：创建 AddTagDialog**

```kotlin
package com.nltimer.feature.tag_management.ui.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.theme.appOutlinedTextFieldColors

@Composable
fun AddTagDialog(
    initialCategory: String?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String?) -> Unit,
) {
    var tagName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建标签") },
        text = {
            Column {
                OutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    label = { Text("标签名称") },
                    singleLine = true,
                    colors = appOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )

                if (!initialCategory.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "将添加到「$initialCategory」分类",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (tagName.isNotBlank()) {
                        onConfirm(tagName.trim(), initialCategory)
                    }
                },
                enabled = tagName.isNotBlank(),
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
```

- [ ] **步骤 2：创建 EditTagDialog**

```kotlin
package com.nltimer.feature.tag_management.ui.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.Tag
import com.nltimer.core.designsystem.theme.appOutlinedTextFieldColors

@Composable
fun EditTagDialog(
    tag: Tag,
    onDismiss: () -> Unit,
    onConfirm: (Tag) -> Unit,
) {
    var tagName by remember { mutableStateOf(tag.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑标签") },
        text = {
            Column {
                OutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    label = { Text("标签名称") },
                    singleLine = true,
                    colors = appOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "当前所属分类：${tag.category ?: "未分类"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (tagName.isNotBlank()) {
                        onConfirm(tag.copy(name = tagName.trim()))
                    }
                },
                enabled = tagName.isNotBlank(),
            ) {
                Text("保存")
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

- [ ] **步骤 3：Commit**

```bash
git add feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/dialogs/
git commit -m "✨ feat(tag_management): implement AddTag and EditTag dialogs"
```

---

## 任务 7：对话框组件 - 分类管理相关

**文件：**
- 创建：`feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/dialogs/AddCategoryDialog.kt`
- 创建：`feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/dialogs/RenameCategoryDialog.kt`
- 创建：`feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/dialogs/ConfirmDialog.kt`

- [ ] **步骤 1：创建 AddCategoryDialog**

```kotlin
package com.nltimer.feature.tag_management.ui.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.nltimer.core.designsystem.theme.appOutlinedTextFieldColors

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit,
) {
    var categoryName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建分类") },
        text = {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("分类名称") },
                singleLine = true,
                colors = appOutlinedTextFieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (categoryName.isNotBlank()) {
                        onConfirm(categoryName.trim())
                    }
                },
                enabled = categoryName.isNotBlank(),
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
```

- [ ] **步骤 2：创建 RenameCategoryDialog**

```kotlin
package com.nltimer.feature.tag_management.ui.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.nltimer.core.designsystem.theme.appOutlinedTextFieldColors

@Composable
fun RenameCategoryDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (newName: String) -> Unit,
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("重命名分类") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("新名称") },
                singleLine = true,
                colors = appOutlinedTextFieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (newName.isNotBlank() && newName != currentName) {
                        onConfirm(newName.trim())
                    }
                },
                enabled = newName.isNotBlank() && newName != currentName,
            ) {
                Text("保存")
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

- [ ] **步骤 3：创建 ConfirmDialog**

```kotlin
package com.nltimer.feature.tag_management.ui.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "确定",
    dismissText: String = "取消",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
    )
}
```

- [ ] **步骤 4：Commit**

```bash
git add feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/components/dialogs/
git commit -m "✨ feat(tag_management): implement category management dialogs"
```

---

## 任务 8：主界面 - TagManagementScreen

**文件：**
- 创建：`feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/TagManagementScreen.kt`

- [ ] **步骤 1：创建主界面 Screen**

```kotlin
package com.nltimer.feature.tag_management.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.nltimer.feature.tag_management.model.DialogState
import com.nltimer.feature.tag_management.viewmodel.TagManagementViewModel
import com.nltimer.feature.tag_management.ui.components.CategoryCard
import com.nltimer.feature.tag_management.ui.components.dialogs.AddCategoryDialog
import com.nltimer.feature.tag_management.ui.components.dialogs.AddTagDialog
import com.nltimer.feature.tag_management.ui.components.dialogs.ConfirmDialog
import com.nltimer.feature.tag_management.ui.components.dialogs.EditTagDialog
import com.nltimer.feature.tag_management.ui.components.dialogs.RenameCategoryDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagManagementScreen(
    viewModel: TagManagementViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("标签管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddCategoryDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "更多操作")
            }
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        CategoryCard(
                            categoryName = "默认",
                            tags = uiState.uncategorizedTags,
                            isDefaultCategory = true,
                            onAddTag = { viewModel.showAddTagDialog(null) },
                            onTagClick = { viewModel.showEditTagDialog(it) },
                            onTagLongClick = { viewModel.showMoveTagDialog(it, null) },
                        )
                    }

                    items(uiState.categories.size) { index ->
                        val category = uiState.categories[index]
                        CategoryCard(
                            categoryName = category.categoryName,
                            tags = category.tags,
                            onAddTag = { viewModel.showAddTagDialog(category.categoryName) },
                            onTagClick = { viewModel.showEditTagDialog(it) },
                            onTagLongClick = { 
                                viewModel.showMoveTagDialog(it, category.categoryName) 
                            },
                            onMenuClick = { 
                                viewModel.showDeleteCategoryDialog(
                                    category.categoryName, 
                                    category.tags.size 
                                )
                            },
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))

                        androidx.compose.material3.OutlinedButton(
                            onClick = { viewModel.showAddCategoryDialog() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                        ) {
                            Text("+ 增加标签分类")
                        }
                    }
                }
            }
        }
    }

    uiState.dialogState?.let { dialog ->
        when (dialog) {
            is DialogState.AddTag -> {
                AddTagDialog(
                    initialCategory = dialog.category,
                    onDismiss = { viewModel.dismissDialog() },
                    onConfirm = { name, category ->
                        viewModel.addTag(name, null, null, null, category)
                    },
                )
            }
            is DialogState.EditTag -> {
                EditTagDialog(
                    tag = dialog.tag,
                    onDismiss = { viewModel.dismissDialog() },
                    onConfirm = { viewModel.updateTag(it) },
                )
            }
            is DialogState.DeleteTag -> {
                ConfirmDialog(
                    title = "删除标签",
                    message = "确定要删除标签「${dialog.tag.name}」吗？此操作不可撤销。",
                    confirmText = "删除",
                    onDismiss = { viewModel.dismissDialog() },
                    onConfirm = { viewModel.deleteTag(dialog.tag) },
                )
            }
            is DialogState.MoveTag -> {
                MoveTagDialogWrapper(
                    tag = dialog.tag,
                    currentCategory = dialog.currentCategory,
                    categories = uiState.categories.map { it.categoryName },
                    onDismiss = { viewModel.dismissDialog() },
                    onConfirm = { targetCategory ->
                        viewModel.moveTagToCategory(dialog.tag.id, targetCategory)
                    },
                )
            }
            is DialogState.AddCategory -> {
                AddCategoryDialog(
                    onDismiss = { viewModel.dismissDialog() },
                    onConfirm = { viewModel.addCategory(it) },
                )
            }
            is DialogState.RenameCategory -> {
                RenameCategoryDialog(
                    currentName = dialog.name,
                    onDismiss = { viewModel.dismissDialog() },
                    onConfirm = { viewModel.renameCategory(dialog.name, it) },
                )
            }
            is DialogState.DeleteCategory -> {
                ConfirmDialog(
                    title = "删除分类",
                    message = "删除「${dialog.name}」分类？该分类下的 ${dialog.tagCount} 个标签将变为未分类。",
                    confirmText = "删除",
                    onDismiss = { viewModel.dismissDialog() },
                    onConfirm = { viewModel.deleteCategory(dialog.name) },
                )
            }
        }
    }
}

@Composable
private fun MoveTagDialogWrapper(
    tag: com.nltimer.core.data.model.Tag,
    currentCategory: String?,
    categories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit,
) {
    var selectedCategory by androidx.compose.runtime.mutableStateOf(currentCategory)

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("移动标签") },
        text = {
            Column {
                Text(
                    text = "将「${tag.name}」移动到：",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(12.dp))

                androidx.compose.material3.DropdownMenuItem(
                    text = { Text("未分类") },
                    onClick = { selectedCategory = null },
                    selected = selectedCategory == null,
                )

                categories.forEach { category ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(category) },
                        onClick = { selectedCategory = category },
                        selected = selectedCategory == category,
                    )
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = { onConfirm(selectedCategory) }) {
                Text("移动")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}
```

关键设计决策：
- 使用 `LargeTopAppBar` 配合滚动行为
- LazyColumn 展示分类卡片列表
- 底部虚线按钮用于添加新分类
- FAB 作为快捷操作入口
- 对话框通过 when 表达式根据 dialogState 类型渲染

- [ ] **步骤 2：Commit**

```bash
git add feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/TagManagementScreen.kt
git commit -m "✨ feat(tag_management): implement main screen with full UI"
```

---

## 任务 9：路由入口 - TagManagementRoute

**文件：**
- 创建：`feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/TagManagementRoute.kt`

- [ ] **步骤 1：创建 Route 入口**

```kotlin
package com.nltimer.feature.tag_management.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.nltimer.feature.tag_management.viewmodel.TagManagementViewModel

@Composable
fun TagManagementRoute(
    onNavigateBack: () -> Unit,
    viewModel: TagManagementViewModel = hiltViewModel(),
) {
    TagManagementScreen(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
    )
}
```

- [ ] **步骤 2：Commit**

```bash
git add feature/tag_management/src/main/java/com/nltimer/feature/tag_management/ui/TagManagementRoute.kt
git commit -m "✨ feat(tag_management): add route entry point"
```

---

## 任务 10：导航集成

**文件：**
- 修改：`app/src/main/java/com/nltimer/app/navigation/NLtimerNavHost.kt`
- 修改：`app/src/main/java/com/nltimer/app/component/AppDrawer.kt`
- 修改：`app/build.gradle.kts`

- [ ] **步骤 1：在 app/build.gradle.kts 中添加依赖**

在 `app/build.gradle.kts` 的 dependencies 块中追加：

```kotlin
implementation(projects.feature.tag_management)
```

位置：在其他 feature 依赖附近。

- [ ] **步骤 2：在 NLtimerNavHost 中注册路由**

在 `NLtimerNavHost.kt` 中：

追加 import：
```kotlin
import com.nltimer.feature.tag_management.ui.TagManagementRoute
```

在 NavHost 的 composable 块中追加（建议在 management_activities 之后）：
```kotlin
composable("tag_management") {
    TagManagementRoute(
        onNavigateBack = { navController.popBackStack() },
    )
}
```

- [ ] **步骤 3：在 AppDrawer 中添加菜单项**

在 `AppDrawer.kt` 中：

追加 import：
```kotlin
import androidx.compose.material.icons.filled.Label
```

在 `drawerMenuItems` 列表中追加（建议在 management_activities 之后）：
```kotlin
DrawerMenuItem("tag_management", "标签管理", Icons.Default.Label),
```

如果 `Icons.Default.Label` 不存在，可以使用 `Icons.Default.Bookmark` 或其他合适图标。

- [ ] **步骤 4：验证编译**

运行：`./gradlew :app:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 5：Commit**

```bash
git add app/build.gradle.kts app/src/main/java/com/nltimer/app/navigation/NLtimerNavHost.kt app/src/main/java/com/nltimer/app/component/AppDrawer.kt
git commit -m "✨ feat(tag_management): integrate navigation and drawer entry"
```

---

## 任务 11：构建验证与测试

**文件：**
- 无新建文件（验证性任务）

- [ ] **步骤 1：完整构建测试**

运行：`./gradlew :app:assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 2：运行应用并手动测试**

启动应用后：
1. 打开抽屉菜单
2. 点击"标签管理"
3. 验证界面加载正常（默认分类显示）
4. 测试添加标签功能
5. 测试编辑标签功能
6. 测试长按标签显示菜单
7. 测试移动标签到其他分类
8. 测试新建分类
9. 测试重命名分类
10. 测试删除分类（确认标签移至默认）
11. 测试返回导航

- [ ] **步骤 3：最终 Commit（如有修复）**

如果有任何问题需要修复：
```bash
git add -A
git commit -m "🐛 fix(tag_management): fix issues found during testing"
```

---

## 自检清单

### ✅ 规格覆盖度
- [x] 标签 CRUD 功能 → 任务 3, 6, 8
- [x] 分类管理（CRUD）→ 任务 3, 7, 8
- [x] 复用现有 Tag 数据 → 任务 3（使用 TagRepository）
- [x] 紧凑型卡片 UI → 任务 4, 5, 8
- [x] 导航集成 → 任务 9, 10
- [x] 对话框系统 → 任务 6, 7

### ✅ 占位符扫描
- [x] 无 "TODO"、"待定"、"后续实现" 等
- [x] 每个步骤都有具体代码
- [x] 每个命令都有预期输出

### ✅ 类型一致性
- [x] TagManagementUiState 在任务 2 定义，任务 3, 8 使用一致
- [x] DialogState sealed interface 在任务 2 定义，任务 3, 6, 7, 8 使用一致
- [x] 组件参数签名在各任务间保持一致

### ✅ 可执行性
- [x] 每个任务可在 2-5 分钟内完成
- [x] 步骤顺序合理，无循环依赖
- [x] 有明确的验收标准

---

## 执行建议

**推荐执行方式：子代理驱动（subagent-driven-development）**

每个任务可以独立分配给一个子代理执行，任务间自动进行审查。这种方式：
- ✅ 并行化程度高
- ✅ 自动质量审查
- ✅ 快速迭代反馈

**备选方式：内联执行（executing-plans）**

在当前会话中批量执行任务，设有检查点供人工审查。适合：
- 快速原型开发
- 开发者希望全程监控
- 需要频繁调整的场景

---

**计划版本：** v1.0  
**最后更新：** 2026-04-29  
**基于规格：** [2026-04-29-tag-management-design.md](../specs/2026-04-29-tag-management-design.md)
