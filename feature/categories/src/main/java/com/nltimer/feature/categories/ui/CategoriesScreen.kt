package com.nltimer.feature.categories.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.theme.appInputChipBorder
import com.nltimer.core.designsystem.theme.appAssistChipBorder
import com.nltimer.core.designsystem.component.ConfirmDialog
import com.nltimer.core.designsystem.theme.appOutlinedTextFieldColors
import com.nltimer.feature.categories.model.CategoriesUiState
import com.nltimer.feature.categories.model.DialogState
import com.nltimer.feature.categories.model.SectionType

/**
 * Main screen composable for managing activity and tag categories.
 * Displays a search bar, two category sections, and conditionally renders dialogs.
 *
 * @param uiState current [CategoriesUiState]
 * @param onSearchQueryChange callback when search query text changes
 * @param onAddCategory callback to trigger the add-category dialog for a given section type
 * @param onRenameCategory callback to trigger the rename dialog for a given category
 * @param onDeleteCategory callback to trigger the delete confirmation dialog for a given category
 * @param onDismissDialog callback to dismiss any active dialog
 * @param onConfirmAdd callback to confirm adding a new category
 * @param onConfirmRename callback to confirm renaming an existing category
 * @param onConfirmDelete callback to confirm deleting a category
 * @param renameConflict non-null when a rename conflict exists, holding the conflicting name
 * @param onClearConflict callback to clear the rename conflict state
 * @param modifier optional [Modifier]
 */
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
    modifier: Modifier = Modifier,
) {
    // 顶栏 + 可滚动内容布局
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

            // 搜索输入框：根据关键字过滤分类列表
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("搜索分类...") },
                singleLine = true,
                colors = appOutlinedTextFieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 活动分类区域
            CategorySection(
                title = "活动分类",
                categories = uiState.activityCategories,
                sectionType = SectionType.ACTIVITY,
                onAdd = onAddCategory,
                onRename = onRenameCategory,
                onDelete = onDeleteCategory,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 标签分类区域
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

    // 如果有活跃的对话框状态，渲染对应的对话框
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

/**
 * A section displaying a title row and a [FlowRow] of category chips.
 * Each chip supports long-press to show a context menu for rename/delete.
 *
 * @param title section header text
 * @param categories list of category names to display as chips
 * @param sectionType the type of section (ACTIVITY or TAG)
 * @param onAdd callback to trigger add dialog for this section
 * @param onRename callback to trigger rename dialog for a specific category
 * @param onDelete callback to trigger delete confirmation for a specific category
 * @param modifier optional [Modifier]
 */
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
        // 遍历每个分类，生成可长按弹出菜单的 AssistChip
        categories.forEach { category ->
            var showMenu by remember { mutableStateOf(false) }

            Box {
                AssistChip(
                    onClick = {},
                    label = { Text(category) },
                    border = appAssistChipBorder(),
                    modifier = Modifier.combinedClickable(
                        onClick = {},
                        onLongClick = { showMenu = true },
                    ),
                )
                // 长按弹出的上下文菜单：重命名 / 删除
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

        // 末尾的"新建分类"按钮
        InputChip(
            selected = false,
            onClick = { onAdd(sectionType) },
            label = { Text("+ 新建分类") },
            border = appInputChipBorder(selected = false),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                )
            },
        )
    }
}

/**
 * Dispatches the active [DialogState] to the correct dialog composable.
 *
 * @param dialogState the current dialog state determining which dialog to show
 * @param onDismiss callback to dismiss the dialog
 * @param onConfirmAdd callback to confirm adding a category
 * @param onConfirmRename callback to confirm renaming a category
 * @param onConfirmDelete callback to confirm deleting a category
 * @param renameConflict non-null conflict name when a rename would cause a duplicate
 * @param onClearConflict callback to clear the rename conflict
 */
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
        // 新增分类对话框（活动 / 标签共用 UI）
        is DialogState.AddActivityCategory,
        is DialogState.AddTagCategory -> {
            var name by remember { mutableStateOf("") }
            // 根据密封类型推断 sectionType
            val sectionType = when (dialogState) {
                is DialogState.AddActivityCategory -> SectionType.ACTIVITY
                is DialogState.AddTagCategory -> SectionType.TAG
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
                        colors = appOutlinedTextFieldColors(),
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { onConfirmAdd(sectionType, name.trim()) },
                        // 名称为空时禁用确定按钮
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

        // 重命名活动分类对话框
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

        // 重命名标签分类对话框
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

        // 确认删除活动分类
        is DialogState.DeleteActivityCategory -> {
            ConfirmDialog(
                title = "删除分类",
                message = "确定要删除分类「${dialogState.category}」吗？该分类下的所有活动和标签将变为未分类状态。",
                confirmText = "删除",
                onDismiss = onDismiss,
                onConfirm = { onConfirmDelete(SectionType.ACTIVITY, dialogState.category) },
                confirmTextColor = MaterialTheme.colorScheme.error,
            )
        }

        // 确认删除标签分类
        is DialogState.DeleteTagCategory -> {
            ConfirmDialog(
                title = "删除分类",
                message = "确定要删除分类「${dialogState.category}」吗？该分类下的所有活动和标签将变为未分类状态。",
                confirmText = "删除",
                onDismiss = onDismiss,
                onConfirm = { onConfirmDelete(SectionType.TAG, dialogState.category) },
                confirmTextColor = MaterialTheme.colorScheme.error,
            )
        }
    }
}

/**
 * Dialog for renaming a category, with inline conflict detection.
 *
 * @param oldName the current category name to rename from
 * @param conflict non-null when the new name already exists
 * @param onDismiss callback to dismiss the dialog
 * @param onConfirm callback with the new name to confirm the rename
 * @param onClearConflict callback to clear the rename conflict
 */
@Composable
private fun RenameDialog(
    oldName: String,
    conflict: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onClearConflict: () -> Unit,
) {
    // 用旧名称初始化文本框
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
                        // 用户修改输入时清除冲突状态
                        if (isConflict) onClearConflict()
                    },
                    label = { Text("新名称") },
                    singleLine = true,
                    isError = isConflict,
                    supportingText = if (isConflict) {
                        { Text("该分类已存在") }
                    } else null,
                    colors = appOutlinedTextFieldColors(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(newName.trim()) },
                // 名称为空或存在冲突时禁用确定
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

