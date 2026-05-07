package com.nltimer.feature.home.viewmodel

import com.nltimer.core.data.SettingsPrefs
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.model.Behavior
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.model.BehaviorWithDetails
import com.nltimer.core.data.model.DialogGridConfig
import com.nltimer.core.data.model.Tag
import com.nltimer.core.data.repository.ActivityRepository
import com.nltimer.core.data.repository.BehaviorRepository
import com.nltimer.core.data.repository.TagRepository
import com.nltimer.core.designsystem.theme.Theme
import com.nltimer.core.designsystem.theme.TimeLabelConfig
import com.nltimer.core.data.util.ClockService
import com.nltimer.core.data.util.SystemClockService
import com.nltimer.core.data.util.TimeSnapService
import com.nltimer.core.data.usecase.AddBehaviorUseCase
import com.nltimer.feature.home.match.KeywordMatchStrategy
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var behaviorRepository: FakeBehaviorRepository
    private lateinit var activityRepository: FakeActivityRepository
    private lateinit var tagRepository: FakeTagRepository
    private lateinit var settingsPrefs: FakeSettingsPrefs
    private lateinit var clockService: ClockService
    private lateinit var addBehaviorUseCase: FakeAddBehaviorUseCase
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        behaviorRepository = FakeBehaviorRepository()
        activityRepository = FakeActivityRepository()
        tagRepository = FakeTagRepository()
        settingsPrefs = FakeSettingsPrefs()
        clockService = SystemClockService()
        addBehaviorUseCase = FakeAddBehaviorUseCase(behaviorRepository, TimeSnapService(), clockService)
        viewModel = HomeViewModel(
            behaviorRepository,
            activityRepository,
            tagRepository,
            settingsPrefs,
            KeywordMatchStrategy(),
            addBehaviorUseCase,
            clockService
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loading`() = runTest {
        advanceUntilIdle()
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        // By default buildUiState returns one empty placeholder row if no behaviors
        assertEquals(1, uiState.rows.size)
    }

    @Test
    fun `addActivity calls repository`() = runTest {
        viewModel.addActivity("Test Activity", "😊")
        advanceUntilIdle()
        assertEquals(1, activityRepository.insertedActivities.size)
        assertEquals("Test Activity", activityRepository.insertedActivities[0].name)
    }

    @Test
    fun `addTag calls repository`() = runTest {
        viewModel.addTag("Test Tag")
        advanceUntilIdle()
        assertEquals(1, tagRepository.insertedTags.size)
        assertEquals("Test Tag", tagRepository.insertedTags[0].name)
    }

    @Test
    fun `showAddSheet updates uiState`() = runTest {
        viewModel.showAddSheet()
        assertNotNull(viewModel.uiState.value.addSheetMode)
    }

    @Test
    fun `hideAddSheet updates uiState`() = runTest {
        viewModel.showAddSheet()
        viewModel.hideAddSheet()
        assertEquals(null, viewModel.uiState.value.addSheetMode)
    }

    @Test
    fun `onActivitySelected loads tags`() = runTest {
        val tags = listOf(Tag(1, "Tag1", null, null, null, 0, 0, 0, false))
        tagRepository.tagsByActivityId[1L] = tags
        
        viewModel.onActivitySelected(1L)
        advanceUntilIdle()
        
        assertEquals(tags, viewModel.tagsForSelectedActivity.value)
    }

    @Test
    fun `addBehavior calls repository and hides sheet`() = runTest {
        viewModel.showAddSheet()
        viewModel.addBehavior(1L, listOf(10L), 1000L, null, BehaviorNature.ACTIVE, "Note")
        advanceUntilIdle()

        assertTrue(behaviorRepository.endCurrentBehaviorCalled)
        assertEquals(1, behaviorRepository.insertedBehaviors.size)
        assertEquals(1L, behaviorRepository.insertedBehaviors[0].activityId)
        assertEquals(null, viewModel.uiState.value.addSheetMode)
    }

    @Test
    fun `addBehavior with COMPLETED endTime in future should show error`() = runTest {
        val futureTime = System.currentTimeMillis() + 3600_000 // 1小时后
        val startTime = System.currentTimeMillis() - 7200_000 // 2小时前

        viewModel.addBehavior(
            activityId = 1L,
            tagIds = emptyList(),
            startTime = startTime,
            endTime = futureTime,
            status = BehaviorNature.COMPLETED,
            note = null,
        )
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals("结束时间不能大于当前时间", uiState.errorMessage)
        assertEquals(0, behaviorRepository.insertedBehaviors.size)
    }

    @Test
    fun `addBehavior with ACTIVE startTime in future should show error`() = runTest {
        val futureTime = System.currentTimeMillis() + 3600_000 // 1小时后

        viewModel.addBehavior(
            activityId = 1L,
            tagIds = emptyList(),
            startTime = futureTime,
            endTime = null,
            status = BehaviorNature.ACTIVE,
            note = null,
        )
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals("开始时间不能大于当前时间", uiState.errorMessage)
        assertEquals(0, behaviorRepository.insertedBehaviors.size)
    }

    @Test
    fun `addBehavior should insert at correct sequence based on startTime`() = runTest {
        // Given: 已有 08:00-09:00 的记录
        val existingBehavior = Behavior(
            id = 1L,
            activityId = 1L,
            startTime = getTodayAt(8, 0),
            endTime = getTodayAt(9, 0),
            status = BehaviorNature.COMPLETED,
            note = null,
            pomodoroCount = 0,
            sequence = 0,
            estimatedDuration = null,
            actualDuration = null,
            achievementLevel = null,
            wasPlanned = false,
        )
        behaviorRepository.dayRangeBehaviors.add(existingBehavior)

        // When: 插入 06:00-07:00
        val newStartTime = getTodayAt(6, 0)
        val newEndTime = getTodayAt(7, 0)
        viewModel.addBehavior(
            activityId = 2L,
            tagIds = emptyList(),
            startTime = newStartTime,
            endTime = newEndTime,
            status = BehaviorNature.COMPLETED,
            note = null,
        )
        advanceUntilIdle()

        // Then: 新行为的 sequence 应该是 0（排在前面）
        assertEquals(1, behaviorRepository.insertedBehaviors.size)
        assertEquals(0, behaviorRepository.insertedBehaviors[0].sequence)
        // 原有行为的 sequence 应该更新为 1
        assertEquals(1, behaviorRepository.updatedSequences[1L])
    }

    private fun getTodayAt(hour: Int, minute: Int): Long {
        return java.time.LocalDate.now()
            .atTime(hour, minute)
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    @Test
    fun `completeBehavior calls repository`() = runTest {
        viewModel.completeBehavior(1L)
        advanceUntilIdle()
        assertTrue(behaviorRepository.completeCurrentAndStartNextCalled)
    }

    @Test
    fun `toggleIdleMode updates uiState`() = runTest {
        val initial = viewModel.uiState.value.isIdleMode
        viewModel.toggleIdleMode()
        assertEquals(!initial, viewModel.uiState.value.isIdleMode)
    }

    @Test
    fun `deleteBehavior calls repository`() = runTest {
        viewModel.deleteBehavior(1L)
        advanceUntilIdle()
        assertTrue(behaviorRepository.deleteCalled)
    }

    @Test
    fun `startNextPending calls repository`() = runTest {
        val pending = Behavior(
            id = 1L,
            activityId = 1L,
            startTime = 0L,
            endTime = null,
            status = BehaviorNature.PENDING,
            note = null,
            pomodoroCount = 0,
            sequence = 0,
            estimatedDuration = null,
            actualDuration = null,
            achievementLevel = null,
            wasPlanned = false,
        )
        behaviorRepository.nextPending = pending
        viewModel.startNextPending()
        advanceUntilIdle()
        assertTrue(behaviorRepository.setStatusCalled)
        assertTrue(behaviorRepository.setStartTimeCalled)
    }

    @Test
    fun `reorderGoals calls repository`() = runTest {
        val orderedIds = listOf(3L, 1L, 2L)
        viewModel.reorderGoals(orderedIds)
        advanceUntilIdle()
        assertEquals(orderedIds, behaviorRepository.reorderedIds)
    }

    @Test
    fun `toggleIdleMode toggles state`() = runTest {
        val initial = viewModel.uiState.value.isIdleMode
        viewModel.toggleIdleMode()
        assertEquals(!initial, viewModel.uiState.value.isIdleMode)
        viewModel.toggleIdleMode()
        assertEquals(initial, viewModel.uiState.value.isIdleMode)
    }

    @Test
    fun `scrollToTime updates selectedTimeHour`() = runTest {
        viewModel.scrollToTime(14)
        assertEquals(14, viewModel.uiState.value.selectedTimeHour)
    }

    @Test
    fun `onHomeLayoutChange updates theme`() = runTest {
        viewModel.onHomeLayoutChange(com.nltimer.core.designsystem.theme.HomeLayout.GRID)
        advanceUntilIdle()
        assertTrue(settingsPrefs.updateThemeCalled)
    }

    @Test
    fun `onTimeLabelConfigChange updates config`() = runTest {
        val config = TimeLabelConfig()
        viewModel.onTimeLabelConfigChange(config)
        advanceUntilIdle()
        assertTrue(settingsPrefs.updateTimeLabelConfigCalled)
    }

    @Test
    fun `addBehavior with time conflict shows error`() = runTest {
        val now = System.currentTimeMillis()
        val startTime = now - 3600_000
        val endTime = now - 1800_000
        val overlapping = Behavior(
            id = 1L,
            activityId = 1L,
            startTime = startTime - 1800_000,
            endTime = endTime + 1800_000,
            status = BehaviorNature.COMPLETED,
            note = null,
            pomodoroCount = 0,
            sequence = 0,
            estimatedDuration = null,
            actualDuration = null,
            achievementLevel = null,
            wasPlanned = false,
        )
        behaviorRepository.overlappingBehaviors.add(overlapping)

        viewModel.addBehavior(
            activityId = 1L,
            tagIds = emptyList(),
            startTime = startTime,
            endTime = endTime,
            status = BehaviorNature.COMPLETED,
            note = null,
        )
        advanceUntilIdle()

        assertEquals("该时间段与已有行为记录冲突", viewModel.uiState.value.errorMessage)
        assertEquals(0, behaviorRepository.insertedBehaviors.size)
    }

    @Test
    fun `addBehavior PENDING status does not check time conflict`() = runTest {
        val startTime = System.currentTimeMillis()
        val overlapping = Behavior(
            id = 1L,
            activityId = 1L,
            startTime = startTime - 3600_000,
            endTime = startTime + 3600_000,
            status = BehaviorNature.COMPLETED,
            note = null,
            pomodoroCount = 0,
            sequence = 0,
            estimatedDuration = null,
            actualDuration = null,
            achievementLevel = null,
            wasPlanned = false,
        )
        behaviorRepository.overlappingBehaviors.add(overlapping)

        viewModel.addBehavior(
            activityId = 1L,
            tagIds = emptyList(),
            startTime = startTime,
            endTime = null,
            status = BehaviorNature.PENDING,
            note = null,
        )
        advanceUntilIdle()

        assertEquals(1, behaviorRepository.insertedBehaviors.size)
        assertEquals(BehaviorNature.PENDING, behaviorRepository.insertedBehaviors[0].status)
    }

    private class FakeBehaviorRepository : BehaviorRepository {
        val insertedBehaviors = mutableListOf<Behavior>()
        val dayRangeBehaviors = mutableListOf<Behavior>()
        val updatedSequences = mutableMapOf<Long, Int>()
        val overlappingBehaviors = mutableListOf<Behavior>()
        var endCurrentBehaviorCalled = false
        var completeCurrentAndStartNextCalled = false
        var deleteCalled = false
        var nextPending: Behavior? = null
        var setStatusCalled = false
        var setStartTimeCalled = false
        var reorderedIds: List<Long>? = null

        override fun getByDayRange(dayStart: Long, dayEnd: Long): Flow<List<Behavior>> = flowOf(
            dayRangeBehaviors.filter { it.startTime in dayStart until dayEnd }
        )
        override fun getCurrentBehavior(): Flow<Behavior?> = flowOf(null)
        override fun getHomeBehaviors(dayStart: Long, dayEnd: Long): Flow<List<Behavior>> = flowOf(emptyList())
        override fun getBehaviorsOverlappingRange(rangeStart: Long, rangeEnd: Long): Flow<List<Behavior>> = flowOf(overlappingBehaviors)
        override fun getTagsForBehavior(behaviorId: Long): Flow<List<Tag>> = flowOf(emptyList())
        override fun getPendingBehaviors(): Flow<List<Behavior>> = flowOf(emptyList())
        override suspend fun getBehaviorWithDetails(behaviorId: Long): BehaviorWithDetails? = null
        override suspend fun getNextPending(): Behavior? = nextPending
        override suspend fun getMaxSequence(): Int = 0
        override suspend fun insert(behavior: Behavior, tagIds: List<Long>): Long {
            insertedBehaviors.add(behavior)
            return 1L
        }
        override suspend fun setEndTime(id: Long, endTime: Long) {}
        override suspend fun setStatus(id: Long, status: String) {
            setStatusCalled = true
        }
        override suspend fun setStartTime(id: Long, startTime: Long) {
            setStartTimeCalled = true
        }
        override suspend fun setActualDuration(id: Long, duration: Long) {}
        override suspend fun setAchievementLevel(id: Long, level: Int) {}
        override suspend fun setSequence(id: Long, sequence: Int) {
            updatedSequences[id] = sequence
        }
        override suspend fun setNote(id: Long, note: String?) {}
        override suspend fun endCurrentBehavior(endTime: Long) {
            endCurrentBehaviorCalled = true
        }
        override suspend fun completeCurrentAndStartNext(currentId: Long, idleMode: Boolean): Behavior? {
            completeCurrentAndStartNextCalled = true
            return null
        }
        override suspend fun reorderGoals(orderedIds: List<Long>) {
            this.reorderedIds = orderedIds
        }
        override suspend fun delete(id: Long) {
            deleteCalled = true
        }
        override suspend fun settleDay(dayStart: Long, dayEnd: Long) {}
        override suspend fun updateBehavior(id: Long, activityId: Long, startTime: Long, endTime: Long?, status: String, note: String?) {}
        override suspend fun updateTagsForBehavior(behaviorId: Long, tagIds: List<Long>) {}
    }

    private class FakeActivityRepository : ActivityRepository {
        val insertedActivities = mutableListOf<Activity>()
        
        override fun getAllActive(): Flow<List<Activity>> = flowOf(emptyList())
        override fun getAll(): Flow<List<Activity>> = flowOf(emptyList())
        override fun getAllGroups(): Flow<List<ActivityGroup>> = flowOf(emptyList())
        override fun search(query: String): Flow<List<Activity>> = flowOf(emptyList())
        override suspend fun getById(id: Long): Activity? = null
        override suspend fun getByName(name: String): Activity? = null
        override suspend fun insert(activity: Activity): Long {
            insertedActivities.add(activity)
            return 1L
        }
        override suspend fun update(activity: Activity) {}
        override suspend fun setArchived(id: Long, archived: Boolean) {}
    }

    private class FakeTagRepository : TagRepository {
        val insertedTags = mutableListOf<Tag>()
        val tagsByActivityId = mutableMapOf<Long, List<Tag>>()

        override fun getAllActive(): Flow<List<Tag>> = flowOf(emptyList())
        override fun getAll(): Flow<List<Tag>> = flowOf(emptyList())
        override fun getByCategory(category: String): Flow<List<Tag>> = flowOf(emptyList())
        override fun search(query: String): Flow<List<Tag>> = flowOf(emptyList())
        override fun getByActivityId(activityId: Long): Flow<List<Tag>> = flowOf(tagsByActivityId[activityId] ?: emptyList())
        override suspend fun getById(id: Long): Tag? = null
        override suspend fun getByName(name: String): Tag? = null
        override suspend fun insert(tag: Tag): Long {
            insertedTags.add(tag)
            return 1L
        }
        override suspend fun update(tag: Tag) {}
        override suspend fun setArchived(id: Long, archived: Boolean) {}
        override fun getDistinctCategories(): Flow<List<String>> = flowOf(emptyList())
        override suspend fun renameCategory(oldName: String, newName: String) {}
        override suspend fun resetCategory(category: String) {}
    }

    private class FakeSettingsPrefs : SettingsPrefs {
        private val _theme = MutableStateFlow(Theme())
        var updateThemeCalled = false
        var updateTimeLabelConfigCalled = false

        override fun getThemeFlow(): Flow<Theme> = _theme
        override suspend fun updateTheme(theme: Theme) {
            updateThemeCalled = true
            _theme.value = theme
        }
        override fun getSavedTagCategories(): Flow<Set<String>> = flowOf(emptySet())
        override suspend fun saveTagCategories(categories: Set<String>) {}
        override fun getDialogConfigFlow(): Flow<DialogGridConfig> = flowOf(DialogGridConfig())
        override suspend fun updateDialogConfig(config: DialogGridConfig) {}
        override fun getTimeLabelConfigFlow(): Flow<TimeLabelConfig> = flowOf(TimeLabelConfig())
        override suspend fun updateTimeLabelConfig(config: TimeLabelConfig) {
            updateTimeLabelConfigCalled = true
        }
    }

    private class FakeAddBehaviorUseCase(
        private val behaviorRepository: BehaviorRepository,
        private val timeSnapService: TimeSnapService,
        private val clockService: ClockService,
    ) : AddBehaviorUseCase(behaviorRepository, timeSnapService, clockService)
}
