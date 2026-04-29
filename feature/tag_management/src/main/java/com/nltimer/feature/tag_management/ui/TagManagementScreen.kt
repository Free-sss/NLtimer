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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagManagementScreen(
    viewModel: TagManagementViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 收集 UI 状态流
    val uiState by viewModel.uiState.collectAsState()
    // 顶部栏折叠滚动行为
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
//        topBar = {
//            LargeTopAppBar(
//                title = { Text("标签管理") },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(
//                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                            contentDescription = "返回",
//                        )
//                    }
//                },
//                scrollBehavior = scrollBehavior,
//            )
//        },
        // 悬浮按钮：点击弹出添加分类对话框
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
            // 加载中状态：显示圆形进度指示器
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                // 使用 LazyColumn 展示所有分类卡片
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // "默认"分类：展示所有未分类标签
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

                    // 遍历所有用户自定义分类
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
                                    category.tags.size,
                                )
                            },
                        )
                    }

                    // 底部"增加标签分类"按钮
                    item {
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
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

    // 根据当前对话框状态弹出对应对话框
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
    // 当前选中的分类，初始为标签所属分类
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
                Spacer(modifier = Modifier.height(12.dp))

                // "未分类"选项
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text("未分类") },
                    onClick = { selectedCategory = null },
                )

                // 遍历所有分类供用户选择
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
