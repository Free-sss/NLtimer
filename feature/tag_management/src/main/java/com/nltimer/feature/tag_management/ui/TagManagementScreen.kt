package com.nltimer.feature.tag_management.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.component.DragActionFab
import com.nltimer.core.designsystem.component.FabDragOptions
import com.nltimer.core.designsystem.component.rememberDragFabState
import com.nltimer.feature.tag_management.viewmodel.TagManagementViewModel
import com.nltimer.feature.tag_management.ui.components.CategoryCard

@Composable
fun TagManagementScreen(
    viewModel: TagManagementViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val dragFabState = rememberDragFabState()

    Box(
        modifier = modifier
            .onGloballyPositioned { dragFabState.boxPositionInWindow = it.positionInWindow() }
    ) {
        Scaffold(
            floatingActionButton = {
                DragActionFab(
                    state = dragFabState,
                    icon = Icons.Default.Add,
                    onClick = { viewModel.showAddCategoryDialog() },
                    onOptionSelected = { option ->
                        when (option) {
                            "添加分类" -> viewModel.showAddCategoryDialog()
                            "添加标签" -> viewModel.showAddTagDialog(null)
                        }
                    },
                )
            },
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

        FabDragOptions(
            state = dragFabState,
            options = listOf("添加分类", "添加标签"),
        )

        TagManagementSheetRouter(
            uiState = uiState,
            viewModel = viewModel,
        )
    }
}
