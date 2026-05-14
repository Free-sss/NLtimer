package com.nltimer.core.data.usecase

import com.nltimer.core.data.model.ExportData
import com.nltimer.core.data.repository.DataExportImportRepository
import com.nltimer.core.data.repository.ImportMode
import com.nltimer.core.data.repository.ImportResult
import javax.inject.Inject

enum class ImportScope { ALL, ACTIVITIES, TAGS, CATEGORIES }

class ImportDataUseCase @Inject constructor(
    private val repository: DataExportImportRepository,
) {
    suspend operator fun invoke(data: ExportData, scope: ImportScope, mode: ImportMode): ImportResult =
        when (scope) {
            ImportScope.ALL -> repository.importAll(data, mode)
            ImportScope.ACTIVITIES -> repository.importActivities(data, mode)
            ImportScope.TAGS -> repository.importTags(data, mode)
            ImportScope.CATEGORIES -> repository.importCategories(data, mode)
        }
}
