package com.nltimer.feature.tag_management.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nltimer.core.behaviorui.sheet.CategoryGroupCard
import com.nltimer.core.behaviorui.sheet.CategorizableItem
import com.nltimer.core.data.model.Tag
import com.nltimer.core.designsystem.component.BottomBarDragFab
import com.nltimer.core.designsystem.component.LoadingScreen
import com.nltimer.core.designsystem.component.rememberDragFabState
import com.nltimer.feature.tag_management.viewmodel.TagManagementViewModel

@Composable
fun TagManagementScreen(
    viewModel: TagManagementViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dragFabState = rememberDragFabState()

    Box(
        modifier = modifier
            .onGloballyPositioned { dragFabState.boxPositionInWindow = it.positionInWindow() }
    ) {
        Scaffold(
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                if (uiState.isLoading) {
                    LoadingScreen()
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item {
                            val items = uiState.uncategorizedTags.map { tag ->
                                ManagementTagItem(tag)
                            }
                            CategoryGroupCard(
                                index = 0,
                                groupName = "默认",
                                items = items,
                                collapsed = false,
                                showDragHandle = false,
                                emptyText = "暂无标签",
                                onItemSelected = { id ->
                                    uiState.uncategorizedTags
                                        .firstOrNull { it.id == id }
                                        ?.let(viewModel::showEditTagDialog)
                                },
                                onItemLongClick = { id ->
                                    uiState.uncategorizedTags
                                        .firstOrNull { it.id == id }
                                        ?.let { viewModel.showMoveTagDialog(it, null) }
                                },
                                onAddItem = { viewModel.showAddTagDialog(null) },
                            )
                        }

                        itemsIndexed(
                            items = uiState.categories,
                            key = { _, category -> category.categoryName },
                        ) { index, category ->
                            val items = category.tags.map { tag ->
                                ManagementTagItem(tag)
                            }
                            CategoryGroupCard(
                                index = index + 1,
                                groupName = category.categoryName,
                                items = items,
                                collapsed = false,
                                showDragHandle = false,
                                emptyText = "暂无标签",
                                headerActions = {
                                    TagCategoryActions(
                                        categoryName = category.categoryName,
                                        onRenameCategory = viewModel::showRenameCategoryDialog,
                                        onDeleteCategory = {
                                            viewModel.showDeleteCategoryDialog(
                                                category.categoryName,
                                                category.tags.size,
                                            )
                                        },
                                    )
                                },
                                onItemSelected = { id ->
                                    category.tags
                                        .firstOrNull { it.id == id }
                                        ?.let(viewModel::showEditTagDialog)
                                },
                                onItemLongClick = { id ->
                                    category.tags
                                        .firstOrNull { it.id == id }
                                        ?.let { viewModel.showMoveTagDialog(it, category.categoryName) }
                                },
                                onAddItem = { viewModel.showAddTagDialog(category.categoryName) },
                            )
                        }
                    }
                }
            }
        }

        BottomBarDragFab(
            state = dragFabState,
            icon = Icons.Default.Add,
            dragOptions = listOf("添加分类", "添加标签"),
            onClick = { viewModel.showAddCategoryDialog() },
            onOptionSelected = { option ->
                when (option) {
                    "添加分类" -> viewModel.showAddCategoryDialog()
                    "添加标签" -> viewModel.showAddTagDialog(null)
                }
            },
        )

        TagManagementSheetRouter(
            uiState = uiState,
            viewModel = viewModel,
        )
    }
}

private data class ManagementTagItem(
    val tag: Tag,
) : CategorizableItem {
    override val itemId: Long = tag.id
    override val itemName: String = tag.name
    override val category: String? = tag.category
    override val usageCount: Int = tag.usageCount
    override val lastUsedTimestamp: Long? = null
    override val iconKey: String? = tag.iconKey
}

@Composable
private fun TagCategoryActions(
    categoryName: String,
    onRenameCategory: (String) -> Unit,
    onDeleteCategory: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "更多操作",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("重命名分类") },
                onClick = {
                    expanded = false
                    onRenameCategory(categoryName)
                },
            )
            DropdownMenuItem(
                text = { Text("删除分类", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    expanded = false
                    onDeleteCategory()
                },
            )
        }
    }
}
