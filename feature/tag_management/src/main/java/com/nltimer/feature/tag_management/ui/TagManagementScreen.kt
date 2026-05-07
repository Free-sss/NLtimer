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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.Tag
import com.nltimer.feature.tag_management.model.DialogState
import com.nltimer.feature.tag_management.viewmodel.TagManagementViewModel
import com.nltimer.feature.tag_management.ui.components.CategoryCard
import com.nltimer.feature.tag_management.ui.components.dialogs.AddCategoryDialog
import com.nltimer.feature.tag_management.ui.components.dialogs.AddTagDialog
import com.nltimer.feature.tag_management.ui.components.dialogs.ConfirmDialog
import com.nltimer.feature.tag_management.ui.components.dialogs.EditTagDialog
import com.nltimer.feature.tag_management.ui.components.dialogs.RenameCategoryDialog

/**
 * 标签管理主界面
 *
 * 展示所有标签分类，支持添加、编辑、删除、移动标签和分类操作。
 *
 * @param viewModel 标签管理 ViewModel
 * @param onNavigateBack 返回上一页的回调
 * @param modifier 修饰符
 */
@Composable
fun TagManagementScreen(
    viewModel: TagManagementViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddCategoryDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "更多操作")
            }
        },
        modifier = modifier,
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
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
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

                    items(uiState.categories, key = { it.categoryName }) { category ->
                        CategoryCard(
                            categoryName = category.categoryName,
                            tags = category.tags,
                            onAddTag = { viewModel.showAddTagDialog(category.categoryName) },
                            onTagClick = { viewModel.showEditTagDialog(it) },
                            onTagLongClick = {
                                viewModel.showMoveTagDialog(it, category.categoryName)
                            },
                            onRenameCategory = {
                                viewModel.showRenameCategoryDialog(category.categoryName)
                            },
                            onDeleteCategory = {
                                viewModel.showDeleteCategoryDialog(
                                    category.categoryName,
                                    category.tags.size,
                                )
                            },
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedButton(
                            onClick = { viewModel.showAddCategoryDialog() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
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
                    onConfirm = { name, color, icon, priority, category ->
                        viewModel.addTag(name, color, icon, priority, category)
                    },
                )
            }
            is DialogState.EditTag -> {
                EditTagDialog(
                    tag = dialog.tag,
                    onDismiss = { viewModel.dismissDialog() },
                    onConfirm = { viewModel.updateTag(it) },
                    onDelete = { viewModel.showDeleteTagDialog(dialog.tag) },
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

/**
 * 移动标签对话框包装组件
 *
 * 提供下拉菜单让用户选择目标分类。
 *
 * @param tag 要移动的标签
 * @param currentCategory 当前分类
 * @param categories 所有可选分类列表
 * @param onDismiss 关闭对话框回调
 * @param onConfirm 确认移动回调，参数为目标分类名或 null（未分类）
 */
@Composable
private fun MoveTagDialogWrapper(
    tag: Tag,
    currentCategory: String?,
    categories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit,
) {
    var selectedCategory by remember { mutableStateOf(currentCategory) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("移动标签") },
        text = {
            Column {
                Text(
                    text = "将「${tag.name}」移动到：",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))

                androidx.compose.material3.DropdownMenuItem(
                    text = { Text("未分类") },
                    onClick = { selectedCategory = null },
                )

                categories.forEach { category ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(category) },
                        onClick = { selectedCategory = category },
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
