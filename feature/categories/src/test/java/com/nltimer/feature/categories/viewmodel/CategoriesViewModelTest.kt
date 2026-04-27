package com.nltimer.feature.categories.viewmodel

import com.nltimer.core.data.repository.CategoryRepository
import com.nltimer.feature.categories.model.DialogState
import com.nltimer.feature.categories.model.SectionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var activityCategoriesFlow: MutableStateFlow<List<String>>
    private lateinit var tagCategoriesFlow: MutableStateFlow<List<String>>
    private lateinit var repository: FakeCategoryRepository
    private lateinit var viewModel: CategoriesViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        activityCategoriesFlow = MutableStateFlow(emptyList())
        tagCategoriesFlow = MutableStateFlow(emptyList())
        repository = FakeCategoryRepository(activityCategoriesFlow, tagCategoriesFlow)
        viewModel = CategoriesViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_hasEmptyCategories() = runTest {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.activityCategories.isEmpty())
        assertTrue(state.tagCategories.isEmpty())
        assertNull(state.dialogState)
    }

    @Test
    fun collectCategories_emitsUiState() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        activityCategoriesFlow.value = listOf("运动", "学习")
        tagCategoriesFlow.value = listOf("优先级")

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(listOf("运动", "学习"), state.activityCategories)
        assertEquals(listOf("优先级"), state.tagCategories)
    }

    @Test
    fun searchQuery_filtersCategories() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        activityCategoriesFlow.value = listOf("运动", "学习", "工作")
        tagCategoriesFlow.value = listOf("优先级", "状态")

        viewModel.onSearchQueryChange("习")

        val state = viewModel.uiState.value
        assertEquals(listOf("学习"), state.activityCategories)
        assertTrue(state.tagCategories.isEmpty())
    }

    @Test
    fun searchQuery_cleared_restoresAll() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        activityCategoriesFlow.value = listOf("运动", "学习")
        tagCategoriesFlow.value = listOf("优先级")

        viewModel.onSearchQueryChange("习")
        viewModel.onSearchQueryChange("")

        val state = viewModel.uiState.value
        assertEquals(listOf("运动", "学习"), state.activityCategories)
        assertEquals(listOf("优先级"), state.tagCategories)
    }

    @Test
    fun addCategoryDialog_showsForActivity() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onAddCategory(SectionType.ACTIVITY)

        val state = viewModel.uiState.value
        assertTrue(state.dialogState is DialogState.AddActivityCategory)
    }

    @Test
    fun addCategoryDialog_showsForTag() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onAddCategory(SectionType.TAG)

        val state = viewModel.uiState.value
        assertTrue(state.dialogState is DialogState.AddTagCategory)
    }

    @Test
    fun renameCategory_showsDialog() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onRenameCategory(SectionType.ACTIVITY, "运动")

        val state = viewModel.uiState.value
        val dialog = state.dialogState as? DialogState.RenameActivityCategory
        assertNotNull(dialog)
        assertEquals("运动", dialog?.oldName)
    }

    @Test
    fun confirmRename_withConflict_setsConflictFlag() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        activityCategoriesFlow.value = listOf("运动", "学习")

        viewModel.onRenameCategory(SectionType.ACTIVITY, "运动")
        viewModel.confirmRenameCategory(SectionType.ACTIVITY, "运动", "学习")

        assertEquals("学习", viewModel.renameConflict.value)
    }

    @Test
    fun confirmRename_noConflict_callsRepository() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        activityCategoriesFlow.value = listOf("运动")

        viewModel.onRenameCategory(SectionType.ACTIVITY, "运动")
        viewModel.confirmRenameCategory(SectionType.ACTIVITY, "运动", "体育")

        assertTrue(repository.renameActivityCategoryCalled)
        assertEquals("运动" to "体育", repository.lastRenameActivityPair)
    }

    @Test
    fun confirmRename_sameName_dismissesDialog() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        activityCategoriesFlow.value = listOf("运动")

        viewModel.onRenameCategory(SectionType.ACTIVITY, "运动")
        viewModel.confirmRenameCategory(SectionType.ACTIVITY, "运动", "运动")

        assertNull(viewModel.uiState.value.dialogState)
        assertFalse(repository.renameActivityCategoryCalled)
    }

    @Test
    fun confirmDelete_callsRepository() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.confirmDeleteCategory(SectionType.ACTIVITY, "运动")

        assertTrue(repository.resetActivityCategoryCalled)
        assertEquals("运动", repository.lastResetActivityCategory)
    }

    @Test
    fun confirmDeleteTag_callsRepository() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.confirmDeleteCategory(SectionType.TAG, "优先级")

        assertTrue(repository.resetTagCategoryCalled)
    }

    @Test
    fun dismissDialog_clearsDialogState() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onAddCategory(SectionType.ACTIVITY)
        assertNotNull(viewModel.uiState.value.dialogState)

        viewModel.dismissDialog()

        assertNull(viewModel.uiState.value.dialogState)
    }

    @Test
    fun dismissDialog_clearsConflict() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        activityCategoriesFlow.value = listOf("运动", "学习")

        viewModel.onRenameCategory(SectionType.ACTIVITY, "运动")
        viewModel.confirmRenameCategory(SectionType.ACTIVITY, "运动", "学习")
        assertEquals("学习", viewModel.renameConflict.value)

        viewModel.dismissDialog()

        assertNull(viewModel.renameConflict.value)
    }

    @Test
    fun confirmAdd_dismissesDialog() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onAddCategory(SectionType.ACTIVITY)
        assertNotNull(viewModel.uiState.value.dialogState)

        viewModel.confirmAddCategory(SectionType.ACTIVITY, "新分类")

        assertNull(viewModel.uiState.value.dialogState)
    }

    @Test
    fun clearConflict_resetsToNull() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }

        activityCategoriesFlow.value = listOf("运动", "学习")

        viewModel.onRenameCategory(SectionType.ACTIVITY, "运动")
        viewModel.confirmRenameCategory(SectionType.ACTIVITY, "运动", "学习")
        assertEquals("学习", viewModel.renameConflict.value)

        viewModel.clearConflict()

        assertNull(viewModel.renameConflict.value)
    }

    private class FakeCategoryRepository(
        private val activityCategories: MutableStateFlow<List<String>>,
        private val tagCategories: MutableStateFlow<List<String>>,
    ) : CategoryRepository {

        var renameActivityCategoryCalled = false
        var renameTagCategoryCalled = false
        var resetActivityCategoryCalled = false
        var resetTagCategoryCalled = false
        var lastRenameActivityPair: Pair<String, String>? = null
        var lastResetActivityCategory: String? = null

        override fun getDistinctActivityCategories(parent: String?) = activityCategories
        override fun getDistinctTagCategories(parent: String?) = tagCategories

        override suspend fun renameActivityCategory(oldName: String, newName: String, parent: String?) {
            renameActivityCategoryCalled = true
            lastRenameActivityPair = oldName to newName
        }

        override suspend fun resetActivityCategory(category: String) {
            resetActivityCategoryCalled = true
            lastResetActivityCategory = category
        }

        override suspend fun renameTagCategory(oldName: String, newName: String, parent: String?) {
            renameTagCategoryCalled = true
        }

        override suspend fun resetTagCategory(category: String) {
            resetTagCategoryCalled = true
        }
    }
}
