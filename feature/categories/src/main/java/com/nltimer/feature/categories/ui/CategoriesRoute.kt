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
    )
}
