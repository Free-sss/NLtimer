package com.nltimer.feature.settings.ui

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nltimer.core.data.repository.ImportMode
import com.nltimer.core.data.usecase.ExportScope
import com.nltimer.core.data.usecase.ImportScope

@Composable
fun DataManagementRoute(
    onNavigateBack: () -> Unit,
    onNavigateToBehaviorManagement: () -> Unit,
    viewModel: DataManagementViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            val data = uiState.lastExportData ?: return@rememberLauncherForActivityResult
            viewModel.writeExportToFile(context, uri, data)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            viewModel.onFileSelectedForImport(context, uri)
        }
    }

    LaunchedEffect(uiState.lastExportData) {
        val data = uiState.lastExportData
        if (data != null && !uiState.isExporting) {
            val scopeName = when (uiState.lastExportScope) {
                ExportScope.ALL -> "all"
                ExportScope.ACTIVITIES -> "activities"
                ExportScope.TAGS -> "tags"
                ExportScope.CATEGORIES -> "categories"
                null -> "all"
            }
            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                .format(java.util.Date())
            exportLauncher.launch("nltimer_${scopeName}_$timestamp.json")
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    if (uiState.pendingImportData != null) {
        ImportModeDialog(
            onConfirm = { mode -> viewModel.confirmImport(mode) },
            onDismiss = { viewModel.dismissImportDialog() },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        DataManagementScreen(
            isExporting = uiState.isExporting,
            isImporting = uiState.isImporting,
            showImportDialog = uiState.pendingImportData != null,
            onExport = { scope -> viewModel.exportData(scope) },
            onImport = { scope ->
                viewModel.triggerImportFileSelection(scope)
                importLauncher.launch(arrayOf("application/json"))
            },
            onExportToClipboard = { scope -> viewModel.exportToClipboard(context, scope) },
            onImportFromClipboard = { scope -> viewModel.importFromClipboard(context, scope) },
            onConfirmImport = { mode -> viewModel.confirmImport(mode) },
            onDismissImportDialog = { viewModel.dismissImportDialog() },
            onNavigateToBehaviorManagement = onNavigateToBehaviorManagement,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun DataManagementScreen(
    isExporting: Boolean,
    isImporting: Boolean,
    showImportDialog: Boolean,
    onExport: (ExportScope) -> Unit,
    onImport: (ImportScope) -> Unit,
    onExportToClipboard: (ExportScope) -> Unit,
    onImportFromClipboard: (ImportScope) -> Unit,
    onConfirmImport: (ImportMode) -> Unit,
    onDismissImportDialog: () -> Unit,
    onNavigateToBehaviorManagement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ActionCard(
                icon = Icons.Default.Storage,
                title = "导出到文件",
                subtitle = "导出全部数据为一个 JSON 文件",
                loading = isExporting,
                onClick = { onExport(ExportScope.ALL) },
            )
        }

        item {
            ActionCard(
                icon = Icons.Default.Storage,
                title = "从文件导入",
                subtitle = "从 JSON 文件导入全部数据",
                loading = isImporting,
                onClick = { onImport(ImportScope.ALL) },
            )
        }

        item {
            ActionCard(
                icon = Icons.Default.Storage,
                title = "导出到剪贴板",
                subtitle = "复制全部数据到剪贴板",
                loading = isExporting,
                onClick = { onExportToClipboard(ExportScope.ALL) },
            )
        }

        item {
            ActionCard(
                icon = Icons.Default.Storage,
                title = "从剪贴板导入",
                subtitle = "从剪贴板粘贴导入全部数据",
                loading = isImporting,
                onClick = { onImportFromClipboard(ImportScope.ALL) },
            )
        }

        item {
            ExpandableSection(
                title = "活动数据",
                onExport = { onExport(ExportScope.ACTIVITIES) },
                onImport = { onImport(ImportScope.ACTIVITIES) },
                onExportToClipboard = { onExportToClipboard(ExportScope.ACTIVITIES) },
                onImportFromClipboard = { onImportFromClipboard(ImportScope.ACTIVITIES) },
            )
        }

        item {
            ExpandableSection(
                title = "标签数据",
                onExport = { onExport(ExportScope.TAGS) },
                onImport = { onImport(ImportScope.TAGS) },
                onExportToClipboard = { onExportToClipboard(ExportScope.TAGS) },
                onImportFromClipboard = { onImportFromClipboard(ImportScope.TAGS) },
            )
        }

        item {
            ExpandableSection(
                title = "分类数据",
                subtitle = "活动分组 + 标签分类",
                onExport = { onExport(ExportScope.CATEGORIES) },
                onImport = { onImport(ImportScope.CATEGORIES) },
                onExportToClipboard = { onExportToClipboard(ExportScope.CATEGORIES) },
                onImportFromClipboard = { onImportFromClipboard(ImportScope.CATEGORIES) },
            )
        }

        item {
            ActionCard(
                icon = Icons.Default.Storage,
                title = "行为记录管理",
                subtitle = "管理计时记录",
                onClick = onNavigateToBehaviorManagement,
            )
        }
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!loading) Modifier else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        onClick = { if (!loading) onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    subtitle: String? = null,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onExportToClipboard: () -> Unit,
    onImportFromClipboard: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    TextButton(onClick = onExport, modifier = Modifier.weight(1f)) {
                        Text(text = "导出", style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(onClick = onImport, modifier = Modifier.weight(1f)) {
                        Text(text = "导入", style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(onClick = onExportToClipboard, modifier = Modifier.weight(1f)) {
                        Text(text = "复制", style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(onClick = onImportFromClipboard, modifier = Modifier.weight(1f)) {
                        Text(text = "粘贴", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
