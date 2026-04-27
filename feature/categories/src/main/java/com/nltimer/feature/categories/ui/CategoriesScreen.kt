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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("搜索分类...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

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
