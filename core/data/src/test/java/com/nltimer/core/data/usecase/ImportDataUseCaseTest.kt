package com.nltimer.core.data.usecase

import com.nltimer.core.data.model.ExportData
import com.nltimer.core.data.repository.DataExportImportRepository
import com.nltimer.core.data.repository.ImportMode
import com.nltimer.core.data.repository.ImportResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImportDataUseCaseTest {

    private lateinit var repository: DataExportImportRepository
    private lateinit var useCase: ImportDataUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = ImportDataUseCase(repository)
    }

    @Test
    fun importAllWithSmartMode() = runTest {
        val data = ExportData()
        val expected = ImportResult.Success(activitiesImported = 5)
        coEvery { repository.importAll(data, ImportMode.SMART) } returns expected

        val result = useCase(data, ImportScope.ALL, ImportMode.SMART)

        assertEquals(expected, result)
        coVerify(exactly = 1) { repository.importAll(data, ImportMode.SMART) }
    }

    @Test
    fun importReturnsErrorOnFailure() = runTest {
        val data = ExportData()
        val expected = ImportResult.Error("导入失败")
        coEvery { repository.importAll(data, ImportMode.OVERWRITE) } returns expected

        val result = useCase(data, ImportScope.ALL, ImportMode.OVERWRITE)

        assertTrue(result is ImportResult.Error)
        assertEquals("导入失败", (result as ImportResult.Error).message)
        coVerify(exactly = 1) { repository.importAll(data, ImportMode.OVERWRITE) }
    }
}
