# NLtimer 架构与代码审查报告

**审查范围：** `core/data`（计时逻辑核心）、`di`（依赖注入）、`navigation`（导航架构）
**审查标准：** 所有计时逻辑都应在纯 Kotlin 模块中可测试
**审查日期：** 2026-05-08
**审查人：** Architecture Reviewer Agent
**工作树分支：** `review/architecture-refactor`

---

## 总结

整体实现思路清晰，Repository 接口与实现分离良好，DAO 层职责明确。但存在一个系统性架构问题：**计时逻辑散布在 ViewModel 和 RepositoryImpl 中，大量使用 `System.currentTimeMillis()` 硬编码调用，导致核心业务逻辑无法在纯 JVM 环境下进行单元测试。** 这直接违反了"所有计时逻辑都应在 library-kotlin 模块中可测试"的审查标准。

值得学习的地方：
- Repository 接口设计遵循了依赖倒置原则
- `TimeConflictUtils` 已正确抽象 `currentTime` 参数，是项目中唯一可测试的计时逻辑
- Hilt DI 模块结构清晰
- 导航层路由常量化做得不错

主要问题：
1. **[必须修复]** 计时逻辑不可测试 -- 7 处 `System.currentTimeMillis()` 硬编码
2. **[必须修复]** ViewModel 中存在 300+ 行纯业务逻辑，应提取到 domain 层
3. **[建议修改]** DI 模块职责拆分不合理
4. **[建议修改]** 导航层路由字符串散落
5. **[仅供参考]** 实体-模型转换逻辑重复

---

## 1. 计时逻辑可测试性（[必须修复]）

### 1.1 `System.currentTimeMillis()` 硬编码问题

**问题位置：**

| 文件 | 方法/位置 | 调用方式 |
|------|-----------|----------|
| `BehaviorRepositoryImpl.kt` | `completeCurrentAndStartNext()` | `System.currentTimeMillis()` x2 |
| `BehaviorRepositoryImpl.kt` | `updateBehavior()` | `System.currentTimeMillis()` x1 |
| `HomeViewModel.kt` | `addBehavior()` | `System.currentTimeMillis()` x3 |
| `HomeViewModel.kt` | `editBehavior()` | `System.currentTimeMillis()` x1 |
| `HomeViewModel.kt` | `startNextPending()` | `System.currentTimeMillis()` x1 |
| `HomeViewModel.kt` | `buildUiState()` | `System.currentTimeMillis()` x1 |

**唯一正确的实现：** `TimeConflictUtils.hasTimeConflict()` 已接受 `currentTime` 参数，是项目中唯一可测试的时间逻辑。这说明团队了解正确做法，但没有系统性执行。

**重构建议：引入 ClockService**

Step 1 -- 在 `core/data` 中定义纯 Kotlin 接口：

```kotlin
// core/data/src/main/java/com/nltimer/core/data/util/ClockService.kt
package com.nltimer.core.data.util

interface ClockService {
    fun currentTimeMillis(): Long
}

class SystemClockService : ClockService {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}

class TestClockService(var currentTime: Long = 0L) : ClockService {
    override fun currentTimeMillis(): Long = currentTime
    fun advanceBy(millis: Long) { currentTime += millis }
}
```

Step 2 -- 通过 Hilt 注入：

```kotlin
// 在 DataModule companion object 中添加：
@Provides
@Singleton
fun provideClockService(): ClockService = SystemClockService()
```

Step 3 -- 替换所有硬编码调用：

```kotlin
// BehaviorRepositoryImpl -- 重构前
@Singleton
class BehaviorRepositoryImpl @Inject constructor(
    private val behaviorDao: BehaviorDao,
    private val activityDao: ActivityDao,
    private val tagDao: TagDao,
) : BehaviorRepository {
    override suspend fun completeCurrentAndStartNext(...) {
        val now = System.currentTimeMillis() // 不可测试
    }
}

// BehaviorRepositoryImpl -- 重构后
@Singleton
class BehaviorRepositoryImpl @Inject constructor(
    private val behaviorDao: BehaviorDao,
    private val activityDao: ActivityDao,
    private val tagDao: TagDao,
    private val clockService: ClockService, // 注入
) : BehaviorRepository {
    override suspend fun completeCurrentAndStartNext(...) {
        val now = clockService.currentTimeMillis() // 可测试
    }
}
```

Step 4 -- 在测试中使用 TestClockService：

```kotlin
class BehaviorRepositoryImplTest {
    private val clock = TestClockService(currentTime = 1700000000000L)
    private val repo = BehaviorRepositoryImpl(behaviorDao, activityDao, tagDao, clock)

    @Test
    fun `completeCurrentAndStartNext calculates duration correctly`() {
        clock.currentTime = 1700000000000L
        // start a behavior...
        clock.advanceBy(30 * 60 * 1000L) // 30 minutes later
        repo.completeCurrentAndStartNext(behaviorId, idleMode = false)
        // verify duration = 30 min
    }
}
```

---

### 1.2 [必须修复] `BehaviorRepositoryImpl.completeCurrentAndStartNext()` 包含不可测试的完成度计算逻辑

**问题位置：** `BehaviorRepositoryImpl.kt:96-122`

这段方法包含两个关键业务逻辑：
1. **实际耗时计算** -- `duration = clampedEndTime - currentEntity.startTime`
2. **完成度评估** -- 基于 `estimatedDuration` 的偏差比例算法

当前代码将业务计算、DAO 调用和状态变更混在一起，无法单独测试。

**重构建议：提取为纯函数**

```kotlin
// core/data/src/main/java/com/nltimer/core/data/util/BehaviorCalculator.kt
package com.nltimer.core.data.util

object BehaviorCalculator {

    data class CompletionResult(
        val durationMs: Long,
        val achievementLevel: Int?,
    )

    /**
     * 计算行为完成时的实际耗时与完成度百分比
     *
     * @param startTime 行为开始时间 (epoch ms)
     * @param endTime 行为结束时间 (epoch ms)
     * @param wasPlanned 是否为计划行为
     * @param estimatedDurationMinutes 预估时长（分钟），null 表示无预估
     * @return CompletionResult 包含实际耗时和完成度
     */
    fun calculateCompletion(
        startTime: Long,
        endTime: Long,
        wasPlanned: Boolean,
        estimatedDurationMinutes: Long?,
    ): CompletionResult {
        val duration = (endTime - startTime).coerceAtLeast(0L)

        val achievementLevel = if (wasPlanned && estimatedDurationMinutes != null && estimatedDurationMinutes > 0) {
            val estimatedMs = estimatedDurationMinutes * 60_000L
            val diff = kotlin.math.abs(duration - estimatedMs)
            val ratio = (diff.toDouble() / estimatedMs).coerceAtMost(1.0)
            ((1.0 - ratio) * 100).toInt().coerceIn(0, 100)
        } else null

        return CompletionResult(
            durationMs = duration,
            achievementLevel = achievementLevel,
        )
    }
}
```

**测试示例：**

```kotlin
class BehaviorCalculatorTest {
    @Test
    fun `planned behavior completed exactly on time gets 100%`() {
        val result = BehaviorCalculator.calculateCompletion(
            startTime = 0L,
            endTime = 30 * 60 * 1000L,
            wasPlanned = true,
            estimatedDurationMinutes = 30L,
        )
        assertEquals(30 * 60 * 1000L, result.durationMs)
        assertEquals(100, result.achievementLevel)
    }

    @Test
    fun `unplanned behavior has no achievement level`() {
        val result = BehaviorCalculator.calculateCompletion(
            startTime = 0L,
            endTime = 30 * 60 * 1000L,
            wasPlanned = false,
            estimatedDurationMinutes = 30L,
        )
        assertNull(result.achievementLevel)
    }

    @Test
    fun `overtime reduces achievement proportionally`() {
        // 预估 30 分钟，实际 45 分钟，偏差 50%
        val result = BehaviorCalculator.calculateCompletion(
            startTime = 0L,
            endTime = 45 * 60 * 1000L,
            wasPlanned = true,
            estimatedDurationMinutes = 30L,
        )
        assertEquals(50, result.achievementLevel)
    }
}
```

---

### 1.3 [必须修复] HomeViewModel 中的时间吸附逻辑和冲突检测逻辑不可测试

**问题位置：** `HomeViewModel.kt:447-506`

`addBehavior()` 方法中包含约 60 行时间吸附（snap）和冲突检测的逻辑。这段逻辑：
- 使用了 `System.currentTimeMillis()` 不可控制
- 依赖 `behaviorRepository.getBehaviorsOverlappingRange()` 等 DAO 查询
- 包含精确的毫秒级对齐算法（分钟边界 +1ms 吸附）
- 完全无法单独测试

**重构建议：提取到 `core/data` 中的 `TimeSnapService`**

```kotlin
// core/data/src/main/java/com/nltimer/core/data/util/TimeSnapService.kt
package com.nltimer.core.data.util

import com.nltimer.core.data.model.Behavior
import com.nltimer.core.data.model.BehaviorNature

data class SnapResult(
    val adjustedStart: Long,
    val adjustedEnd: Long?,
    val hasConflict: Boolean,
)

class TimeSnapService(
    private val clockService: ClockService,
) {
    /**
     * 尝试将新行为的时间吸附到已有行为的边界，
     * 然后检测是否存在不可解决的时间冲突。
     */
    fun snapAndCheckConflict(
        newStart: Long,
        newEnd: Long?,
        newStatus: BehaviorNature,
        overlappingBehaviors: List<Behavior>,
        ignoreBehaviorId: Long? = null,
    ): SnapResult {
        val now = clockService.currentTimeMillis()
        var adjustedStart = newStart
        var adjustedEnd = newEnd

        // 自动吸附逻辑
        if (newStatus != BehaviorNature.PENDING) {
            val prevBehavior = overlappingBehaviors
                .filter { it.endTime != null && it.endTime!! >= adjustedStart }
                .maxByOrNull { it.endTime!! }
            val prevEnd = prevBehavior?.endTime
            if (prevEnd != null && prevEnd >= adjustedStart) {
                adjustedStart = prevEnd + 1
                if (newStatus == BehaviorNature.COMPLETED && adjustedEnd != null) {
                    if (newEnd != null && newEnd / 60_000 == prevEnd / 60_000) {
                        adjustedEnd = prevEnd / 60_000 * 60_000 + 59_999
                    }
                }
            }
        }

        // 冲突检测
        val hasConflict = if (newStatus != BehaviorNature.PENDING) {
            val effectiveNewEnd = when (newStatus) {
                BehaviorNature.ACTIVE -> Long.MAX_VALUE
                BehaviorNature.COMPLETED -> adjustedEnd ?: adjustedStart
                BehaviorNature.PENDING -> null
            }
            effectiveNewEnd != null && effectiveNewEnd > adjustedStart &&
                hasTimeConflict(
                    newStart = adjustedStart,
                    newEnd = adjustedEnd,
                    newStatus = newStatus,
                    existingBehaviors = overlappingBehaviors,
                    currentTime = now,
                    ignoreBehaviorId = ignoreBehaviorId,
                )
        } else false

        return SnapResult(adjustedStart, adjustedEnd, hasConflict)
    }
}
```

在 `DataModule` 中注册：

```kotlin
@Provides
@Singleton
fun provideTimeSnapService(clockService: ClockService): TimeSnapService =
    TimeSnapService(clockService)
```

ViewModel 中简化为：

```kotlin
// HomeViewModel.addBehavior() -- 重构后
fun addBehavior(...) {
    viewModelScope.launch {
        val now = clockService.currentTimeMillis()
        // ... 时间约束校验 ...

        if (status == BehaviorNature.ACTIVE) {
            behaviorRepository.endCurrentBehavior(startTime)
        }

        val overlapping = behaviorRepository
            .getBehaviorsOverlappingRange(startTime, snapQueryEnd)
            .firstOrNull().orEmpty()

        val result = timeSnapService.snapAndCheckConflict(
            newStart = startTime,
            newEnd = endTime,
            newStatus = status,
            overlappingBehaviors = overlapping,
        )

        if (result.hasConflict) {
            _uiState.update { it.copy(errorMessage = "该时间段与已有行为记录冲突") }
            return@launch
        }

        // 使用 adjustedStart/adjustedEnd 继续插入...
    }
}
```

---

## 2. ViewModel 业务逻辑提取（[必须修复]）

### 2.1 HomeViewModel 承担了过多职责

**问题分析：** `HomeViewModel.kt` 有 688 行，其中 `buildUiState()` 约 200 行、`addBehavior()` 约 170 行。ViewModel 应该只做状态协调，不应包含：
- 行为排序逻辑（第 165-177 行的 sortedBehaviors 算法）
- 时间格式转换（epoch ms -> LocalTime）
- Sequence 计算逻辑（第 512-544 行）
- 完成度计算（但这个已在上文建议提取到 BehaviorCalculator）

**重构建议：引入 UseCase 层**

```kotlin
// core/data/src/main/java/com/nltimer/core/data/usecase/BuildHomeStateUseCase.kt
package com.nltimer.core.data.usecase

import com.nltimer.core.data.model.*
import com.nltimer.core.data.repository.*
import com.nltimer.core.data.util.ClockService
import kotlinx.coroutines.flow.firstOrNull

class BuildHomeStateUseCase(
    private val behaviorRepository: BehaviorRepository,
    private val activityRepository: ActivityRepository,
    private val clockService: ClockService,
) {
    data class HomeState(
        val sortedBehaviors: List<Behavior>,
        val activityMap: Map<Long, Activity>,
        val currentTimeMs: Long,
    )

    suspend operator fun invoke(dayStart: Long, dayEnd: Long): HomeState {
        val behaviors = behaviorRepository.getHomeBehaviors(dayStart, dayEnd)
            .firstOrNull().orEmpty()
        val allActivities = activityRepository.getAll().firstOrNull().orEmpty()
        val activityMap = allActivities.associateBy { it.id }

        val sortedBehaviors = run {
            val nonPending = behaviors
                .filter { it.status != BehaviorNature.PENDING }
                .sortedBy { it.startTime }
            val pending = behaviors
                .filter { it.status == BehaviorNature.PENDING }
                .sortedBy { it.sequence }
            nonPending + pending
        }

        return HomeState(
            sortedBehaviors = sortedBehaviors,
            activityMap = activityMap,
            currentTimeMs = clockService.currentTimeMillis(),
        )
    }
}
```

```kotlin
// core/data/src/main/java/com/nltimer/core/data/usecase/AddBehaviorUseCase.kt
package com.nltimer.core.data.usecase

class AddBehaviorUseCase(
    private val behaviorRepository: BehaviorRepository,
    private val timeSnapService: com.nltimer.core.data.util.TimeSnapService,
    private val clockService: com.nltimer.core.data.util.ClockService,
) {
    sealed class Result {
        data class Success(val behaviorId: Long) : Result()
        data class Conflict(val message: String) : Result()
        data class ValidationError(val message: String) : Result()
    }

    suspend operator fun invoke(
        activityId: Long,
        tagIds: List<Long>,
        startTime: Long,
        endTime: Long?,
        status: BehaviorNature,
        note: String?,
        editBehaviorId: Long? = null,
    ): Result {
        val now = clockService.currentTimeMillis()

        // 时间约束校验
        val validationError = validateTimeConstraints(startTime, endTime, status, now)
        if (validationError != null) return validationError

        // 结束当前行为
        if (status == BehaviorNature.ACTIVE) {
            behaviorRepository.endCurrentBehavior(startTime)
        }

        // 吸附与冲突检测
        val snapResult = performSnapAndConflictCheck(startTime, endTime, status)
        if (snapResult.hasConflict) {
            return Result.Conflict("该时间段与已有行为记录冲突")
        }

        // 插入行为
        val id = insertBehavior(
            activityId, tagIds, snapResult.adjustedStart,
            snapResult.adjustedEnd, status, note, editBehaviorId,
        )
        return Result.Success(id)
    }

    // ... validateTimeConstraints, performSnapAndConflictCheck 等私有方法 ...
}
```

ViewModel 简化为：

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val addBehaviorUseCase: AddBehaviorUseCase,
    private val buildHomeStateUseCase: BuildHomeStateUseCase,
    private val behaviorRepository: BehaviorRepository,
    private val settingsPrefs: SettingsPrefs,
    private val matchStrategy: MatchStrategy,
) : ViewModel() {

    fun addBehavior(...) {
        val editId = _uiState.value.editBehaviorId
        viewModelScope.launch {
            when (val result = addBehaviorUseCase(..., editBehaviorId = editId)) {
                is AddBehaviorUseCase.Result.Success -> hideAddSheet()
                is AddBehaviorUseCase.Result.Conflict ->
                    _uiState.update { it.copy(errorMessage = result.message) }
                is AddBehaviorUseCase.Result.ValidationError ->
                    _uiState.update { it.copy(errorMessage = result.message) }
            }
        }
    }
}
```

---

## 3. DI 模块架构审查（[建议修改]）

### 3.1 DataModule 职责过重

**问题位置：** `core/data/di/DataModule.kt`

当前 `DataModule` 同时承担了：
1. Repository 接口绑定（5 个 @Binds）
2. Database 实例提供
3. 4 个 DAO 提供

这是 8 个 @Provides/@Binds 方法挤在一个类里，且 `provideDatabase()` 硬编码了 5 个迁移对象。

**重构建议：拆分为三个模块**

```kotlin
// core/data/di/DatabaseModule.kt -- 负责数据库和 DAO
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NLtimerDatabase =
        Room.databaseBuilder(context, NLtimerDatabase::class.java, "nltimer-database")
            .fallbackToDestructiveMigration(true)
            .addMigrations(*NLtimerDatabase.ALL_MIGRATIONS) // 用数组替代硬编码
            .build()

    @Provides fun provideActivityDao(db: NLtimerDatabase) = db.activityDao()
    @Provides fun provideActivityGroupDao(db: NLtimerDatabase) = db.activityGroupDao()
    @Provides fun provideTagDao(db: NLtimerDatabase) = db.tagDao()
    @Provides fun provideBehaviorDao(db: NLtimerDatabase) = db.behaviorDao()
}

// core/data/di/RepositoryModule.kt -- 只负责 Repository 绑定
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindActivityRepository(impl: ActivityRepositoryImpl): ActivityRepository
    @Binds abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository
    @Binds abstract fun bindBehaviorRepository(impl: BehaviorRepositoryImpl): BehaviorRepository
    @Binds abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository
    @Binds abstract fun bindActivityManagementRepository(impl: ActivityManagementRepositoryImpl): ActivityManagementRepository
}

// core/data/di/ServiceModule.kt -- 负责领域服务和 Clock
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides @Singleton
    fun provideClockService(): ClockService = SystemClockService()

    @Provides @Singleton
    fun provideTimeSnapService(clock: ClockService): TimeSnapService = TimeSnapService(clock)
}
```

同时在 `NLtimerDatabase` 中统一定义迁移数组：

```kotlin
companion object {
    val ALL_MIGRATIONS = arrayOf(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_7_8, MIGRATION_8_9)
    // ... 各 MIGRATION 定义不变
}
```

### 3.2 [问题] app/di/DataModule 与 core/data/di/DataModule 命名冲突

**问题位置：**
- `app/src/main/java/com/nltimer/app/di/DataModule.kt` -- 提供 DataStore 和 SettingsPrefs
- `core/data/src/main/java/com/nltimer/core/data/di/DataModule.kt` -- 提供 Repository 和 DAO

两个模块同名 `DataModule`，虽然在不同包下 Hilt 能区分，但对开发者阅读不友好。

**建议：** 将 `app/di/DataModule` 重命名为 `SettingsModule`，因为它只提供 DataStore 和 SettingsPrefs：

```kotlin
// app/di/SettingsModule.kt
@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {
    @Provides @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = ...

    @Provides @Singleton
    fun provideSettingsPrefs(dataStore: DataStore<Preferences>): SettingsPrefs = SettingsPrefsImpl(dataStore)
}
```

### 3.3 [问题] SettingsPrefsImpl 未注册在 Hilt 模块中

**问题位置：** `core/data/SettingsPrefsImpl.kt`

`SettingsPrefsImpl` 缺少 `@Inject constructor` 和 `@Singleton` 注解。当前通过 `app/di/DataModule` 中 `provideSettingsPrefs()` 手动创建实例。这使得：
- `SettingsPrefsImpl` 无法在其他模块中通过 Hilt 自动注入
- 如果 `SettingsPrefsImpl` 未来需要其他依赖（如 ClockService），必须修改 `provideSettingsPrefs()`

**建议：** 给 `SettingsPrefsImpl` 添加构造注入，在 `SettingsModule` 中用 `@Binds`：

```kotlin
// SettingsPrefsImpl
class SettingsPrefsImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsPrefs { ... }

// SettingsModule
@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {
    @Binds @Singleton
    abstract fun bindSettingsPrefs(impl: SettingsPrefsImpl): SettingsPrefs

    companion object {
        @Provides @Singleton
        fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = ...
    }
}
```

---

## 4. 导航架构审查（[建议修改]）

### 4.1 路由常量散落在多处

**问题位置：**
- `NLtimerNavHost.kt` -- 定义了 9 个路由常量（`HOME_ROUTE` 等）
- `NLtimerScaffold.kt` -- 硬编码了 `setOf("home", "sub", "stats", ...)` 字符串
- `AppBottomNavigation.kt` -- 硬编码了路由字符串 `"home"`, `"sub"`, `"stats"`, `"settings"`
- `AppDrawer.kt` -- 硬编码了路由字符串 `"home"`, `"theme_settings"` 等

`NLtimerScaffold.kt:42-46` 中：

```kotlin
val primaryRoutes = setOf(
    "home", "sub", "stats", "categories",
    "management_activities", SETTINGS_ROUTE, // 混用！
)
```

这里部分用了常量，部分用了字符串字面量，属于典型的不一致。

**重构建议：统一路由定义**

```kotlin
// core/data/src/main/java/com/nltimer/core/data/navigation/NLtimerRoutes.kt
object NLtimerRoutes {
    const val HOME = "home"
    const val SUB = "sub"
    const val STATS = "stats"
    const val CATEGORIES = "categories"
    const val MANAGEMENT_ACTIVITIES = "management_activities"
    const val TAG_MANAGEMENT = "tag_management"
    const val SETTINGS = "settings"
    const val THEME_SETTINGS = "theme_settings"
    const val DIALOG_CONFIG = "dialog_config"

    val PRIMARY_ROUTES = setOf(HOME, SUB, STATS, CATEGORIES, MANAGEMENT_ACTIVITIES, SETTINGS)
    val SETTINGS_FULLSCREEN_ROUTES = setOf(THEME_SETTINGS, DIALOG_CONFIG)
}
```

如果 `core/data` 不应依赖导航概念，可以放在 `app` 模块中：

```kotlin
// app/src/main/java/com/nltimer/app/navigation/NLtimerRoutes.kt
object NLtimerRoutes { ... }
```

所有 UI 组件统一引用 `NLtimerRoutes.HOME` 等，消除硬编码字符串。

### 4.2 [问题] debugRoutes 变量使用 internal var 暴露可变性

**问题位置：** `NLtimerNavHost.kt:80`

```kotlin
internal var debugRoutes: (NavGraphBuilder.() -> Unit)? = null
```

这是一个 `internal var`，任何同模块的代码都可以随意修改它。缺少保护机制。

**建议：** 使用 `@VisibleForTesting` 或封装为只写属性：

```kotlin
private var _debugRoutes: (NavGraphBuilder.() -> Unit)? = null

fun registerDebugRoutes(routes: NavGraphBuilder.() -> Unit) {
    _debugRoutes = routes
}

// 在 NavHost 中使用 _debugRoutes
```

### 4.3 [问题] NLtimerScaffold 承担了过多布局决策逻辑

**问题位置：** `NLtimerScaffold.kt`

Scaffold 中硬编码了：
- 哪些路由显示底栏（第 42 行 `primaryRoutes`）
- 哪些路由显示返回键顶栏（第 48 行 `settingsFullscreenRoutes`）
- 顶栏标题映射（第 52-56 行 `when (currentRoute)`）
- 底栏偏移量（第 75 行 `80.dp`）

如果新增一个页面，必须同时修改 Scaffold、AppBottomNavigation 和 AppDrawer 三处。

**建议：** 定义 RouteConfig 数据类，将导航 UI 行为声明化：

```kotlin
data class RouteConfig(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val showBottomNav: Boolean = true,
    val showTopBar: Boolean = true,
    val topBarTitle: String? = null,
    val isFullscreen: Boolean = false,
)

object NLtimerRouteConfigs {
    val configs = mapOf(
        NLtimerRoutes.HOME to RouteConfig(
            route = NLtimerRoutes.HOME,
            label = "主页",
            icon = Icons.Default.Home,
        ),
        NLtimerRoutes.THEME_SETTINGS to RouteConfig(
            route = NLtimerRoutes.THEME_SETTINGS,
            label = "主题配置",
            icon = Icons.Default.Brightness5,
            showBottomNav = false,
            isFullscreen = true,
        ),
        // ...
    )

    val bottomNavItems = configs.values.filter { it.showBottomNav }
    val drawerItems = configs.values.filter { it.showTopBar || !it.isFullscreen }
}
```

---

## 5. 实体-模型转换逻辑重复（[仅供参考]）

### 5.1 转换逻辑分散在多处

项目中存在三种不同的实体-模型转换方式：

1. **Repository 中的 private 扩展函数**（如 `ActivityRepositoryImpl.toModel()`）
2. **Model 中的 companion object 工厂方法**（如 `Activity.fromEntity()`, `Activity.toEntity()`）
3. **直接构造**（如 `BehaviorRepositoryImpl.getBehaviorWithDetails()` 中手动构造 Activity）

`BehaviorRepositoryImpl.getBehaviorWithDetails()` 第 44-57 行：

```kotlin
val activity = com.nltimer.core.data.model.Activity(
    id = activityEntity.id,
    name = activityEntity.name,
    // ... 手动逐字段映射
)
```

而 `ActivityManagementRepositoryImpl` 使用：

```kotlin
Activity.fromEntity(it) // 已有工厂方法但不一致使用
```

**建议：** 统一使用 Model 的 `fromEntity()`/`toEntity()` 方法，删除 Repository 中的重复转换扩展函数。`BehaviorRepositoryImpl` 中的 `getBehaviorWithDetails()` 应改为：

```kotlin
val activity = Activity.fromEntity(activityEntity) // 统一复用
```

---

## 6. 其他发现

### 6.1 [建议修改] `BehaviorRepositoryImpl.settleDay()` 为空实现

**问题位置：** `BehaviorRepositoryImpl.kt:148`

```kotlin
override suspend fun settleDay(dayStart: Long, dayEnd: Long) {
    // 空实现
}
```

空实现但接口中声明了该方法，调用方无法知道这个操作实际上什么都没做。建议要么实现它，要么从接口中移除，避免误导。

### 6.2 [建议修改] `BehaviorRepositoryImpl.delete()` 中有冗余查询

**问题位置：** `BehaviorRepositoryImpl.kt:145-147`

```kotlin
override suspend fun delete(id: Long) {
    val toDelete = behaviorDao.getById(id) ?: return  // 查询后未使用
    behaviorDao.delete(id)
}
```

`toDelete` 查询结果未被使用，纯粹浪费一次数据库读取。

**建议：** 直接删除，让 DAO 处理不存在的情况：

```kotlin
override suspend fun delete(id: Long) = behaviorDao.delete(id)
```

### 6.3 [仅供参考] `fallbackToDestructiveMigration(true)` 与显式迁移共存

**问题位置：** `DataModule.kt:43-44`

```kotlin
.fallbackToDestructiveMigration(true)
.addMigrations(MIGRATION_3_4, MIGRATION_4_5, ...)
```

`fallbackToDestructiveMigration(true)` 意味着如果迁移路径不完整，Room 会销毁重建数据库。这与精心维护的 5 个迁移对象矛盾。如果所有版本间迁移都覆盖了，应该移除 fallback 以防止意外数据丢失。如果覆盖不完整，则应在代码注释中明确说明哪些版本之间有缺口。

---

## 审查检查清单

- [x] 每条评论都标注了优先级
- [x] [必须修复] 的问题都给出了具体的修复建议
- [x] 没有因为面子而跳过关键问题
- [x] 没有纠结于工具能自动处理的风格问题
- [x] 对好的代码给予了肯定
- [x] 给出了整体总结
