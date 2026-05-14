package com.nltimer.core.data.usecase

import com.nltimer.core.data.model.ExportData
import com.nltimer.core.data.repository.DataExportImportRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExportDataUseCaseTest {

    private lateinit var repository: DataExportImportRepository
    private lateinit var useCase: ExportDataUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = ExportDataUseCase(repository)
    }

    @Test
    fun exportAllDelegatesToRepository() = runTest {
        val expected = ExportData()
        coEvery { repository.exportAll() } returns expected

        val result = useCase(ExportScope.ALL)

        assertEquals(expected, result)
        coVerify(exactly = 1) { repository.exportAll() }
    }

    @Test
    fun exportActivitiesDelegatesToRepository() = runTest {
        val expected = ExportData(activities = emptyList())
        coEvery { repository.exportActivities() } returns expected

        val result = useCase(ExportScope.ACTIVITIES)

        assertEquals(expected, result)
        coVerify(exactly = 1) { repository.exportActivities() }
    }
}
