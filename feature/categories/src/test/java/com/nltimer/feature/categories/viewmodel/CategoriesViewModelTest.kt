package com.nltimer.feature.categories.viewmodel

import com.nltimer.core.data.SettingsPrefs
import com.nltimer.core.data.model.DialogGridConfig
import com.nltimer.core.data.repository.CategoryRepository
import com.nltimer.core.designsystem.theme.TimeLabelConfig
import com.nltimer.feature.categories.model.DialogState
import com.nltimer.feature.categories.model.SectionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
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
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.activityCategories.isEmpty())
        assertTrue(state.tagCategories.isEmpty())
        assertNull(state.dialogState)
    }

    @Test
    fun searchQuery_filtersCategories() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
        activityCategoriesFlow.value = listOf("运动", "学习")
        tagCategoriesFlow.value = listOf("优先级", "状态")
        advanceUntilIdle()

        viewModel.onSearchQueryChange("运动")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("运动", state.searchQuery)
        assertEquals(listOf("运动"), state.activityCategories)
    }

    @Test
    fun searchQuery_emptyShowsAll() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
        activityCategoriesFlow.value = listOf("运动", "学习")
        tagCategoriesFlow.value = listOf("优先级", "状态")
        advanceUntilIdle()

        viewModel.onSearchQueryChange("运动")
        advanceUntilIdle()
        viewModel.onSearchQueryChange("")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertEquals(listOf("学习", "运动"), state.activityCategories)
        assertEquals(listOf("优先级", "状态"), state.tagCategories)
    }

    @Test
    fun addCategoryDialog_showsForActivity() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        viewModel.onAddCategory(SectionType.ACTIVITY)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.dialogState is DialogState.AddActivityCategory)
    }

    @Test
    fun addCategoryDialog_showsForTag() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        viewModel.onAddCategory(SectionType.TAG)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.dialogState is DialogState.AddTagCategory)
    }

    @Test
    fun renameCategory_showsDialog() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
        activityCategoriesFlow.value = listOf("运动")
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
        viewModel.uiState.launchIn(backgroundScope)
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
        viewModel.uiState.launchIn(backgroundScope)
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
        viewModel.uiState.launchIn(backgroundScope)
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
        viewModel.uiState.launchIn(backgroundScope)
        activityCategoriesFlow.value = listOf("运动")
        advanceUntilIdle()

        viewModel.confirmDeleteCategory(SectionType.ACTIVITY, "运动")
        advanceUntilIdle()

        assertTrue(repository.resetActivityCategoryCalled)
        assertEquals("运动", repository.lastResetActivityCategory)
    }

    @Test
    fun confirmDeleteTag_callsRepository() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        viewModel.confirmDeleteCategory(SectionType.TAG, "优先级")
        advanceUntilIdle()

        assertTrue(repository.resetTagCategoryCalled)
        assertTrue(settingsPrefs.saveTagCategoriesCalled)
    }

    @Test
    fun dismissDialog_clearsDialogState() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
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
        viewModel.uiState.launchIn(backgroundScope)
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
    fun confirmAddActivityCategory_callsRepository() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        viewModel.confirmAddCategory(SectionType.ACTIVITY, "新分类")
        advanceUntilIdle()

        assertTrue(repository.addActivityCategoryCalled)
        assertEquals("新分类", repository.lastAddedActivityCategory)
        assertNull(viewModel.uiState.value.dialogState)
    }

    @Test
    fun confirmAddTagCategory_updatesSettingsPrefs() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        viewModel.confirmAddCategory(SectionType.TAG, "我的标签")
        advanceUntilIdle()

        assertTrue(settingsPrefs.saveTagCategoriesCalled)
        assertEquals(setOf("我的标签"), settingsPrefs.lastSavedTagCategories)
        assertTrue(viewModel.uiState.value.tagCategories.contains("我的标签"))
    }

    @Test
    fun clearConflict_resetsToNull() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
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
    fun confirmRenameTagCategory_updatesSettingsPrefs() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
        settingsPrefs.savedTagCategories = setOf("旧标签")
        viewModel = CategoriesViewModel(repository, settingsPrefs)
        advanceUntilIdle()

        viewModel.onRenameCategory(SectionType.TAG, "旧标签")
        advanceUntilIdle()
        viewModel.confirmRenameCategory(SectionType.TAG, "旧标签", "新标签")
        advanceUntilIdle()

        assertTrue(settingsPrefs.saveTagCategoriesCalled)
        assertEquals(setOf("新标签"), settingsPrefs.lastSavedTagCategories)
    }

    @Test
    fun confirmDeleteTagCategory_removesFromSettingsPrefs() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
        settingsPrefs.savedTagCategories = setOf("待删除", "保留")
        viewModel = CategoriesViewModel(repository, settingsPrefs)
        advanceUntilIdle()

        viewModel.confirmDeleteCategory(SectionType.TAG, "待删除")
        advanceUntilIdle()

        assertTrue(settingsPrefs.saveTagCategoriesCalled)
        assertEquals(setOf("保留"), settingsPrefs.lastSavedTagCategories)
    }

    @Test
    fun onDeleteCategory_showsDeleteDialog() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        viewModel.onDeleteCategory(SectionType.ACTIVITY, "运动")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.dialogState is DialogState.DeleteActivityCategory)
        assertEquals("运动", (state.dialogState as DialogState.DeleteActivityCategory).category)
    }

    @Test
    fun confirmAdd_emptyName_dismissesDialog() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        viewModel.onAddCategory(SectionType.ACTIVITY)
        advanceUntilIdle()
        viewModel.confirmAddCategory(SectionType.ACTIVITY, "  ")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.dialogState)
        assertFalse(repository.addActivityCategoryCalled)
    }

    @Test
    fun confirmRenameTagCategory_withLocalTag_updatesLocalSet() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
        settingsPrefs.savedTagCategories = setOf("本地标签")
        viewModel = CategoriesViewModel(repository, settingsPrefs)
        advanceUntilIdle()

        viewModel.onRenameCategory(SectionType.TAG, "本地标签")
        advanceUntilIdle()
        viewModel.confirmRenameCategory(SectionType.TAG, "本地标签", "重命名后")
        advanceUntilIdle()

        assertTrue(settingsPrefs.saveTagCategoriesCalled)
        assertEquals(setOf("重命名后"), settingsPrefs.lastSavedTagCategories)
    }

    @Test
    fun confirmRenameTagCategory_conflict_setsConflictFlag() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
        tagCategoriesFlow.value = listOf("标签A", "标签B")
        advanceUntilIdle()

        viewModel.onRenameCategory(SectionType.TAG, "标签A")
        advanceUntilIdle()
        viewModel.confirmRenameCategory(SectionType.TAG, "标签A", "标签B")
        advanceUntilIdle()

        assertEquals("标签B", viewModel.renameConflict.value)
    }

    private class FakeSettingsPrefs : SettingsPrefs {

        var savedTagCategories: Set<String> = emptySet()
        var lastSavedTagCategories: Set<String>? = null
        var saveTagCategoriesCalled = false

        override fun getThemeFlow(): Flow<com.nltimer.core.designsystem.theme.Theme> =
            flowOf(com.nltimer.core.designsystem.theme.Theme())
        override suspend fun updateTheme(theme: com.nltimer.core.designsystem.theme.Theme) {}

        override fun getSavedTagCategories(): Flow<Set<String>> =
            MutableStateFlow(savedTagCategories)
        override suspend fun saveTagCategories(categories: Set<String>) {
            saveTagCategoriesCalled = true
            lastSavedTagCategories = categories
        }

        override fun getDialogConfigFlow(): Flow<DialogGridConfig> = flowOf(DialogGridConfig())

        override suspend fun updateDialogConfig(config: DialogGridConfig) {}

        override fun getTimeLabelConfigFlow(): Flow<TimeLabelConfig> = flowOf(TimeLabelConfig())

        override suspend fun updateTimeLabelConfig(config: TimeLabelConfig) {}
    }

    private class FakeCategoryRepository(
        private val activityCategories: MutableStateFlow<List<String>>,
        private val tagCategories: MutableStateFlow<List<String>>,
    ) : CategoryRepository {

        var addActivityCategoryCalled = false
        var renameActivityCategoryCalled = false
        var renameTagCategoryCalled = false
        var resetActivityCategoryCalled = false
        var resetTagCategoryCalled = false
        var lastAddedActivityCategory: String? = null
        var lastRenameActivityPair: Pair<String, String>? = null
        var lastResetActivityCategory: String? = null

        override fun getDistinctActivityCategories(parent: String?) =
            activityCategories.map { it.sorted() }
        override fun getDistinctTagCategories(parent: String?) = tagCategories

        override suspend fun addActivityCategory(name: String) {
            addActivityCategoryCalled = true
            lastAddedActivityCategory = name
        }

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

    @Test
    fun onRenameCategory_tag_showsTagRenameDialog() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()
        viewModel.onRenameCategory(SectionType.TAG, "标签A")
        advanceUntilIdle()
        val state = viewModel.uiState.value
        val dialog = state.dialogState as? DialogState.RenameTagCategory
        assertNotNull(dialog)
        assertEquals("标签A", dialog?.oldName)
        assertEquals(SectionType.TAG, dialog?.sectionType)
    }

    @Test
    fun onDeleteCategory_tag_showsTagDeleteDialog() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()
        viewModel.onDeleteCategory(SectionType.TAG, "标签分类")
        advanceUntilIdle()
        val state = viewModel.uiState.value
        val dialog = state.dialogState as? DialogState.DeleteTagCategory
        assertNotNull(dialog)
        assertEquals("标签分类", dialog?.category)
    }

    @Test
    fun confirmAddCategory_tag_emptyName_dismissesDialog() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()
        viewModel.onAddCategory(SectionType.TAG)
        advanceUntilIdle()
        viewModel.confirmAddCategory(SectionType.TAG, "  ")
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.dialogState)
        assertFalse(settingsPrefs.saveTagCategoriesCalled)
    }

    @Test
    fun searchQuery_filtersTagCategories() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
        tagCategoriesFlow.value = listOf("优先级", "状态", "紧急")
        advanceUntilIdle()
        viewModel.onSearchQueryChange("优先")
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(listOf("优先级"), state.tagCategories)
    }

    @Test
    fun confirmRenameTagCategory_noConflict_callsRepository() = runTest {
        viewModel.uiState.launchIn(backgroundScope)
        tagCategoriesFlow.value = listOf("标签A")
        advanceUntilIdle()
        viewModel.onRenameCategory(SectionType.TAG, "标签A")
        advanceUntilIdle()
        viewModel.confirmRenameCategory(SectionType.TAG, "标签A", "新标签")
        advanceUntilIdle()
        assertTrue(repository.renameTagCategoryCalled)
        assertNull(viewModel.uiState.value.dialogState)
    }

}
