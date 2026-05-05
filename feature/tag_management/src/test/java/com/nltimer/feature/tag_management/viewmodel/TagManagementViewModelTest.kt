package com.nltimer.feature.tag_management.viewmodel

import com.nltimer.core.data.SettingsPrefs
import com.nltimer.core.data.model.Tag
import com.nltimer.core.data.repository.TagRepository
import com.nltimer.feature.tag_management.model.DialogState
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
class TagManagementViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var tagRepository: FakeTagRepository
    private lateinit var settingsPrefs: FakeSettingsPrefs
    private lateinit var viewModel: TagManagementViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        tagRepository = FakeTagRepository()
        settingsPrefs = FakeSettingsPrefs()
        viewModel = TagManagementViewModel(tagRepository, settingsPrefs)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest {
        val uiState = viewModel.uiState.value
        assertTrue(uiState.isLoading)
        assertTrue(uiState.categories.isEmpty())
        assertTrue(uiState.uncategorizedTags.isEmpty())
        assertNull(uiState.dialogState)
    }

    @Test
    fun `loadData combines tags and categories`() = runTest {
        val workTag = Tag(1L, "工作", null, null, null, "分类A", 0, 0, 0, false)
        val personalTag = Tag(2L, "个人", null, null, null, "分类B", 0, 0, 0, false)
        tagRepository.emitTags(listOf(workTag, personalTag))
        tagRepository.emitCategories(listOf("分类A", "分类B"))

        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(2, uiState.categories.size)
        assertEquals("分类A", uiState.categories[0].categoryName)
        assertEquals(1, uiState.categories[0].tags.size)
        assertEquals("工作", uiState.categories[0].tags[0].name)
    }

    @Test
    fun `loadData includes added categories from settings`() = runTest {
        settingsPrefs.emitTagCategories(setOf("本地分类"))
        tagRepository.emitTags(emptyList())
        tagRepository.emitCategories(emptyList())

        // Recreate viewModel so init picks up the new settings value
        viewModel = TagManagementViewModel(tagRepository, settingsPrefs)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(1, uiState.categories.size)
        assertEquals("本地分类", uiState.categories[0].categoryName)
    }

    @Test
    fun `loadData with empty repository shows empty categories`() = runTest {
        tagRepository.emitTags(emptyList())
        tagRepository.emitCategories(emptyList())

        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertTrue(uiState.categories.isEmpty())
        assertTrue(uiState.uncategorizedTags.isEmpty())
    }

    @Test
    fun `addTag calls repository insert`() = runTest {
        viewModel.addTag("新标签", 0xFF0000FF, 0xFFFFFFFF, "icon", 1, "分类A")
        advanceUntilIdle()

        assertEquals(1, tagRepository.insertedTags.size)
        assertEquals("新标签", tagRepository.insertedTags[0].name)
        assertEquals("分类A", tagRepository.insertedTags[0].category)
    }

    @Test
    fun `updateTag calls repository update`() = runTest {
        val tag = Tag(1L, "旧名称", null, null, null, null, 0, 0, 0, false)
        viewModel.updateTag(tag)
        advanceUntilIdle()

        assertEquals(1, tagRepository.updatedTags.size)
        assertEquals("旧名称", tagRepository.updatedTags[0].name)
    }

    @Test
    fun `deleteTag calls setArchived`() = runTest {
        val tag = Tag(1L, "待删除", null, null, null, null, 0, 0, 0, false)
        viewModel.deleteTag(tag)
        advanceUntilIdle()

        assertEquals(1L, tagRepository.archivedTagId)
        assertTrue(tagRepository.archivedValue)
    }

    @Test
    fun `moveTagToCategory with null tag does not call update`() = runTest {
        tagRepository.tagById = null
        viewModel.moveTagToCategory(999L, "新分类")
        advanceUntilIdle()

        assertTrue(tagRepository.updatedTags.isEmpty())
    }

    @Test
    fun `moveTagToCategory updates category`() = runTest {
        val tag = Tag(1L, "标签", null, null, null, "旧分类", 0, 0, 0, false)
        tagRepository.tagById = tag
        viewModel.moveTagToCategory(1L, "新分类")
        advanceUntilIdle()

        assertEquals(1, tagRepository.updatedTags.size)
        assertEquals("新分类", tagRepository.updatedTags[0].category)
    }

    @Test
    fun `addCategory saves to settings`() = runTest {
        viewModel.addCategory("新分类")
        advanceUntilIdle()

        assertTrue(settingsPrefs.saveTagCategoriesCalled)
        assertEquals(setOf("新分类"), settingsPrefs.lastSavedTagCategories)
    }

    @Test
    fun `renameCategory updates settings when oldName in addedCategories`() = runTest {
        settingsPrefs.emitTagCategories(setOf("旧分类"))
        tagRepository.emitTags(emptyList())
        tagRepository.emitCategories(emptyList())
        // Recreate viewModel so init picks up the settings value
        viewModel = TagManagementViewModel(tagRepository, settingsPrefs)
        advanceUntilIdle()

        viewModel.renameCategory("旧分类", "新分类")
        advanceUntilIdle()

        assertTrue(tagRepository.renameCategoryCalled)
        assertEquals("旧分类" to "新分类", tagRepository.lastRenamePair)
        assertTrue(settingsPrefs.saveTagCategoriesCalled)
        assertEquals(setOf("新分类"), settingsPrefs.lastSavedTagCategories)
    }

    @Test
    fun `renameCategory does not update settings when oldName not in addedCategories`() = runTest {
        settingsPrefs.emitTagCategories(setOf("其他分类"))
        tagRepository.emitTags(emptyList())
        tagRepository.emitCategories(emptyList())
        advanceUntilIdle()

        viewModel.renameCategory("旧分类", "新分类")
        advanceUntilIdle()

        assertTrue(tagRepository.renameCategoryCalled)
        assertFalse(settingsPrefs.saveTagCategoriesCalled)
    }

    @Test
    fun `deleteCategory resets tags and removes from settings`() = runTest {
        settingsPrefs.emitTagCategories(setOf("待删分类"))
        tagRepository.emitTags(emptyList())
        tagRepository.emitCategories(emptyList())
        advanceUntilIdle()

        viewModel.deleteCategory("待删分类")
        advanceUntilIdle()

        assertTrue(tagRepository.resetCategoryCalled)
        assertEquals("待删分类", tagRepository.lastResetCategory)
        assertTrue(settingsPrefs.saveTagCategoriesCalled)
        assertEquals(emptySet<String>(), settingsPrefs.lastSavedTagCategories)
    }

    @Test
    fun `showAddTagDialog updates dialogState`() = runTest {
        viewModel.showAddTagDialog("分类A")
        val state = viewModel.uiState.value.dialogState as? DialogState.AddTag
        assertNotNull(state)
        assertEquals("分类A", state?.category)
    }

    @Test
    fun `dismissDialog clears dialogState`() = runTest {
        viewModel.showAddTagDialog()
        assertNotNull(viewModel.uiState.value.dialogState)

        viewModel.dismissDialog()
        assertNull(viewModel.uiState.value.dialogState)
    }

    private class FakeTagRepository : TagRepository {
        private val _tags = MutableStateFlow<List<Tag>>(emptyList())
        private val _categories = MutableStateFlow<List<String>>(emptyList())

        val insertedTags = mutableListOf<Tag>()
        val updatedTags = mutableListOf<Tag>()
        var archivedTagId: Long? = null
        var archivedValue: Boolean = false
        var tagById: Tag? = null
        var renameCategoryCalled = false
        var lastRenamePair: Pair<String, String>? = null
        var resetCategoryCalled = false
        var lastResetCategory: String? = null

        fun emitTags(tags: List<Tag>) {
            _tags.value = tags
        }

        fun emitCategories(categories: List<String>) {
            _categories.value = categories
        }

        override fun getAllActive(): Flow<List<Tag>> = _tags
        override fun getAll(): Flow<List<Tag>> = _tags
        override fun getByCategory(category: String): Flow<List<Tag>> = flowOf(emptyList())
        override fun search(query: String): Flow<List<Tag>> = flowOf(emptyList())
        override fun getByActivityId(activityId: Long): Flow<List<Tag>> = flowOf(emptyList())
        override suspend fun getById(id: Long): Tag? = tagById
        override suspend fun getByName(name: String): Tag? = null
        override suspend fun insert(tag: Tag): Long {
            insertedTags.add(tag)
            return 1L
        }
        override suspend fun update(tag: Tag) {
            updatedTags.add(tag)
        }
        override suspend fun setArchived(id: Long, archived: Boolean) {
            archivedTagId = id
            archivedValue = archived
        }
        override fun getDistinctCategories(): Flow<List<String>> = _categories
        override suspend fun renameCategory(oldName: String, newName: String) {
            renameCategoryCalled = true
            lastRenamePair = oldName to newName
        }
        override suspend fun resetCategory(category: String) {
            resetCategoryCalled = true
            lastResetCategory = category
        }
    }

    private class FakeSettingsPrefs : SettingsPrefs {
        private val _tagCategories = MutableStateFlow<Set<String>>(emptySet())

        var saveTagCategoriesCalled = false
        var lastSavedTagCategories: Set<String> = emptySet()

        fun emitTagCategories(categories: Set<String>) {
            _tagCategories.value = categories
        }

        override fun getThemeFlow(): Flow<com.nltimer.core.designsystem.theme.Theme> = flowOf(com.nltimer.core.designsystem.theme.Theme())
        override suspend fun updateTheme(theme: com.nltimer.core.designsystem.theme.Theme) {}
        override fun getSavedTagCategories(): Flow<Set<String>> = _tagCategories
        override suspend fun saveTagCategories(categories: Set<String>) {
            saveTagCategoriesCalled = true
            lastSavedTagCategories = categories
            _tagCategories.value = categories
        }
        override fun getDialogConfigFlow(): Flow<com.nltimer.core.data.model.DialogGridConfig> = flowOf(com.nltimer.core.data.model.DialogGridConfig())
        override suspend fun updateDialogConfig(config: com.nltimer.core.data.model.DialogGridConfig) {}
        override fun getTimeLabelConfigFlow(): Flow<com.nltimer.core.designsystem.theme.TimeLabelConfig> = flowOf(com.nltimer.core.designsystem.theme.TimeLabelConfig())
        override suspend fun updateTimeLabelConfig(config: com.nltimer.core.designsystem.theme.TimeLabelConfig) {}
    }
}
