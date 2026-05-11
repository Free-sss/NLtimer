package com.nltimer.feature.categories.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nltimer.feature.categories.viewmodel.CategoriesViewModel

/**
 * Route composable for the categories management screen.
 * Wires the ViewModel state and callbacks into [CategoriesScreen].
 *
 * @param viewModel the Hilt-injected [CategoriesViewModel]
 */
@Composable
fun CategoriesRoute(
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    // 收集 ViewModel 中的 UI 状态流和重命名冲突状态流
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val renameConflict by viewModel.renameConflict.collectAsStateWithLifecycle()

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
    )
}
