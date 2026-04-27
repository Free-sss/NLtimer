package com.nltimer.feature.categories.viewmodel

import com.nltimer.core.data.SettingsPrefs
import com.nltimer.core.data.repository.CategoryRepository
import com.nltimer.core.designsystem.theme.Theme
import com.nltimer.feature.categories.model.DialogState
import com.nltimer.feature.categories.model.SectionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var activityCategoriesFlow: MutableStateFlow<List<String>>
    private lateinit var tagCategoriesFlow: MutableStateFlow<List<String>>
    private lateinit var repository: FakeCategoryRepository
    private lateinit var settingsPrefs: FakeSettingsPrefs
    private lateinit var viewModel: CategoriesViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        activityCategoriesFlow = MutableStateFlow(emptyList())
        tagCategoriesFlow = MutableStateFlow(emptyList())
        repository = FakeCategoryRepository(activityCategoriesFlow, tagCategoriesFlow)
        settingsPrefs = FakeSettingsPrefs()
        viewModel = CategoriesViewModel(repository, settingsPrefs)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_hasEmptyCategories() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.activityCategories.isEmpty())
        assertTrue(state.tagCategories.isEmpty())
        assertNull(state.dialogState)
    }

    @Test
    fun searchQuery_filtersCategories() = runTest {
        advanceUntilIdle()

        viewModel.onSearchQueryChange("运动")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("运动", state.searchQuery)
    }

    @Test
    fun searchQuery_emptyShowsAll() = runTest {
        advanceUntilIdle()

        viewModel.onSearchQueryChange("运动")
        advanceUntilIdle()
        viewModel.onSearchQueryChange("")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
    }

    @Test
    fun addCategoryDialog_showsForActivity() = runTest {
        advanceUntilIdle()

        viewModel.onAddCategory(SectionType.ACTIVITY)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.dialogState is DialogState.AddActivityCategory)
    }

    @Test
    fun addCategoryDialog_showsForTag() = runTest {
        advanceUntilIdle()

        viewModel.onAddCategory(SectionType.TAG)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.dialogState is DialogState.AddTagCategory)
    }

    @Test
    fun renameCategory_showsDialog() = runTest {
        advanceUntilIdle()

        viewModel.onRenameCategory(SectionType.ACTIVITY, "运动")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val dialog = state.dialogState as? DialogState.RenameActivityCategory
        assertNotNull(dialog)
        assertEquals("运动", dialog?.oldName)
    }

    @Test
    fun confirmRename_withConflict_setsConflictFlag() = runTest {
        activityCategoriesFlow.value = listOf("运动", "学习")
        advanceUntilIdle()

        viewModel.onRenameCategory(SectionType.ACTIVITY, "运动")
        advanceUntilIdle()
        viewModel.confirmRenameCategory(SectionType.ACTIVITY, "运动", "学习")
        advanceUntilIdle()

        assertEquals("学习", viewModel.renameConflict.value)
    }

    @Test
    fun confirmRename_noConflict_callsRepository() = runTest {
        activityCategoriesFlow.value = listOf("运动")
        advanceUntilIdle()

        viewModel.onRenameCategory(SectionType.ACTIVITY, "运动")
        advanceUntilIdle()
        viewModel.confirmRenameCategory(SectionType.ACTIVITY, "运动", "体育")
        advanceUntilIdle()

        assertTrue(repository.renameActivityCategoryCalled)
        assertEquals("运动" to "体育", repository.lastRenameActivityPair)
    }

    @Test
    fun confirmRename_sameName_dismissesDialog() = runTest {
        activityCategoriesFlow.value = listOf("运动")
        advanceUntilIdle()

        viewModel.onRenameCategory(SectionType.ACTIVITY, "运动")
        advanceUntilIdle()
        viewModel.confirmRenameCategory(SectionType.ACTIVITY, "运动", "运动")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.dialogState)
        assertFalse(repository.renameActivityCategoryCalled)
    }

    @Test
    fun confirmDelete_callsRepository() = runTest {
        advanceUntilIdle()

        viewModel.confirmDeleteCategory(SectionType.ACTIVITY, "运动")
        advanceUntilIdle()

        assertTrue(repository.resetActivityCategoryCalled)
        assertEquals("运动", repository.lastResetActivityCategory)
    }

    @Test
    fun confirmDeleteTag_callsRepository() = runTest {
        advanceUntilIdle()

        viewModel.confirmDeleteCategory(SectionType.TAG, "优先级")
        advanceUntilIdle()

        assertTrue(repository.resetTagCategoryCalled)
    }

    @Test
    fun dismissDialog_clearsDialogState() = runTest {
        advanceUntilIdle()

        viewModel.onAddCategory(SectionType.ACTIVITY)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.dialogState)

        viewModel.dismissDialog()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.dialogState)
    }

    @Test
    fun dismissDialog_clearsConflict() = runTest {
        activityCategoriesFlow.value = listOf("运动", "学习")
        advanceUntilIdle()

        viewModel.onRenameCategory(SectionType.ACTIVITY, "运动")
        advanceUntilIdle()
        viewModel.confirmRenameCategory(SectionType.ACTIVITY, "运动", "学习")
        advanceUntilIdle()
        assertEquals("学习", viewModel.renameConflict.value)

        viewModel.dismissDialog()
        advanceUntilIdle()

        assertNull(viewModel.renameConflict.value)
    }

    @Test
    fun confirmAdd_appendsCategoryToList() = runTest {
        advanceUntilIdle()

        viewModel.onAddCategory(SectionType.ACTIVITY)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.dialogState)

        viewModel.confirmAddCategory(SectionType.ACTIVITY, "新分类")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.dialogState)
        assertTrue(viewModel.uiState.value.activityCategories.contains("新分类"))
    }

    @Test
    fun confirmAddTag_appendsToTagList() = runTest {
        advanceUntilIdle()

        viewModel.onAddCategory(SectionType.TAG)
        advanceUntilIdle()

        viewModel.confirmAddCategory(SectionType.TAG, "我的标签")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.tagCategories.contains("我的标签"))
    }

    @Test
    fun clearConflict_resetsToNull() = runTest {
        activityCategoriesFlow.value = listOf("运动", "学习")
        advanceUntilIdle()

        viewModel.onRenameCategory(SectionType.ACTIVITY, "运动")
        advanceUntilIdle()
        viewModel.confirmRenameCategory(SectionType.ACTIVITY, "运动", "学习")
        advanceUntilIdle()
        assertEquals("学习", viewModel.renameConflict.value)

        viewModel.clearConflict()
        advanceUntilIdle()

        assertNull(viewModel.renameConflict.value)
    }

    @Test
    fun confirmAdd_persistsToDataStore() = runTest {
        advanceUntilIdle()

        viewModel.onAddCategory(SectionType.ACTIVITY)
        advanceUntilIdle()

        viewModel.confirmAddCategory(SectionType.ACTIVITY, "持久分类")
        advanceUntilIdle()

        assertEquals(setOf("持久分类"), settingsPrefs.lastSavedActivityCategories)
    }

    @Test
    fun confirmDelete_removesFromDataStore() = runTest {
        advanceUntilIdle()

        viewModel.onAddCategory(SectionType.ACTIVITY)
        advanceUntilIdle()
        viewModel.confirmAddCategory(SectionType.ACTIVITY, "待删除")
        advanceUntilIdle()

        viewModel.onDeleteCategory(SectionType.ACTIVITY, "待删除")
        advanceUntilIdle()
        viewModel.confirmDeleteCategory(SectionType.ACTIVITY, "待删除")
        advanceUntilIdle()

        assertEquals(emptySet<String>(), settingsPrefs.lastSavedActivityCategories)
    }

    private class FakeSettingsPrefs : SettingsPrefs {

        private val activityCategories = MutableStateFlow<Set<String>>(emptySet())
        private val tagCategories = MutableStateFlow<Set<String>>(emptySet())

        var lastSavedActivityCategories: Set<String>? = null
        var lastSavedTagCategories: Set<String>? = null

        override fun getThemeFlow(): Flow<Theme> = flowOf(Theme())
        override suspend fun updateTheme(theme: Theme) {}

        override fun getSavedActivityCategories(): Flow<Set<String>> = activityCategories
        override suspend fun saveActivityCategories(categories: Set<String>) {
            activityCategories.value = categories
            lastSavedActivityCategories = categories
        }

        override fun getSavedTagCategories(): Flow<Set<String>> = tagCategories
        override suspend fun saveTagCategories(categories: Set<String>) {
            tagCategories.value = categories
            lastSavedTagCategories = categories
        }
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
