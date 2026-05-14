package com.nltimer.core.data.usecase

import com.nltimer.core.data.model.ExportData
import com.nltimer.core.data.repository.DataExportImportRepository
import javax.inject.Inject

enum class ExportScope { ALL, ACTIVITIES, TAGS, CATEGORIES }

class ExportDataUseCase @Inject constructor(
    private val repository: DataExportImportRepository,
) {
    suspend operator fun invoke(scope: ExportScope): ExportData = when (scope) {
        ExportScope.ALL -> repository.exportAll()
        ExportScope.ACTIVITIES -> repository.exportActivities()
        ExportScope.TAGS -> repository.exportTags()
        ExportScope.CATEGORIES -> repository.exportCategories()
    }
}
