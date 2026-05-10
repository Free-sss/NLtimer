package com.nltimer.feature.settings.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.core.data.model.ExportData
import com.nltimer.core.data.repository.ImportMode
import com.nltimer.core.data.repository.ImportResult
import com.nltimer.core.data.usecase.ExportDataUseCase
import com.nltimer.core.data.usecase.ExportScope
import com.nltimer.core.data.usecase.ImportDataUseCase
import com.nltimer.core.data.usecase.ImportScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class DataManagementUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val snackbarMessage: String? = null,
    val pendingImportData: ExportData? = null,
    val pendingImportScope: ImportScope? = null,
    val lastExportData: ExportData? = null,
    val lastExportScope: ExportScope? = null,
)

@HiltViewModel
class DataManagementViewModel @Inject constructor(
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase,
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }

    private val _uiState = MutableStateFlow(DataManagementUiState())
    val uiState: StateFlow<DataManagementUiState> = _uiState.asStateFlow()

    fun exportData(scope: ExportScope) {
        _uiState.update { it.copy(isExporting = true) }
        viewModelScope.launch {
            try {
                val data = exportDataUseCase(scope)
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        lastExportData = data,
                        lastExportScope = scope,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        snackbarMessage = e.message ?: "Export failed",
                    )
                }
            }
        }
    }

    fun writeExportToFile(context: Context, uri: Uri, data: ExportData) {
        viewModelScope.launch {
            try {
                val content = json.encodeToString(ExportData.serializer(), data)
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(content.toByteArray())
                }
                _uiState.update {
                    it.copy(
                        lastExportData = null,
                        lastExportScope = null,
                        snackbarMessage = "Export saved successfully",
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(snackbarMessage = e.message ?: "Write failed")
                }
            }
        }
    }

    fun triggerImportFileSelection(scope: ImportScope) {
        _uiState.update { it.copy(pendingImportScope = scope) }
    }

    fun onFileSelectedForImport(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.readBytes().decodeToString()
                } ?: throw IllegalStateException("Cannot read file")
                val data = json.decodeFromString(ExportData.serializer(), content)
                _uiState.update { it.copy(pendingImportData = data) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(snackbarMessage = e.message ?: "Failed to parse import file")
                }
            }
        }
    }

    fun confirmImport(mode: ImportMode) {
        val data = _uiState.value.pendingImportData ?: return
        val scope = _uiState.value.pendingImportScope ?: return
        _uiState.update { it.copy(isImporting = true) }
        viewModelScope.launch {
            val result = importDataUseCase(data, scope, mode)
            _uiState.update {
                when (result) {
                    is ImportResult.Success -> it.copy(
                        isImporting = false,
                        pendingImportData = null,
                        pendingImportScope = null,
                        snackbarMessage = buildString {
                            val parts = mutableListOf<String>()
                            if (result.activityGroupsImported > 0) parts.add("${result.activityGroupsImported} groups")
                            if (result.activitiesImported > 0) parts.add("${result.activitiesImported} activities")
                            if (result.tagsImported > 0) parts.add("${result.tagsImported} tags")
                            if (result.tagCategoriesImported > 0) parts.add("${result.tagCategoriesImported} categories")
                            if (parts.isEmpty()) "No data imported" else parts.joinToString(", ") + " imported"
                        },
                    )
                    is ImportResult.Error -> it.copy(
                        isImporting = false,
                        snackbarMessage = result.message,
                    )
                }
            }
        }
    }

    fun dismissImportDialog() {
        _uiState.update {
            it.copy(
                pendingImportData = null,
                pendingImportScope = null,
            )
        }
    }

    fun exportToClipboard(context: Context, scope: ExportScope) {
        _uiState.update { it.copy(isExporting = true) }
        viewModelScope.launch {
            try {
                val data = exportDataUseCase(scope)
                val content = json.encodeToString(ExportData.serializer(), data)
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("NLtimer Export", content))
                _uiState.update {
                    it.copy(isExporting = false, snackbarMessage = "已复制到剪贴板")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isExporting = false, snackbarMessage = "导出到剪贴板失败")
                }
            }
        }
    }

    fun importFromClipboard(context: Context, scope: ImportScope) {
        _uiState.update { it.copy(pendingImportScope = scope) }
        viewModelScope.launch {
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val content = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                    ?: throw IllegalStateException("剪贴板为空")
                val data = json.decodeFromString(ExportData.serializer(), content)
                _uiState.update { it.copy(pendingImportData = data) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(snackbarMessage = "剪贴板数据格式无效", pendingImportScope = null)
                }
            }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
