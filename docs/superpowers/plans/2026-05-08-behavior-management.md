# 行为管理页面 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 新增行为管理页面，聚合查看所有行为，支持紧凑列表/时间轴视图、四维过滤、时间范围切换、编辑复用 AddBehaviorSheet、JSON 导出导入及查重。

**架构：** 新建 `core:behaviorui` 共享模块（从 feature:home 移入 AddBehaviorSheet 及其依赖子组件），新建 `feature:behavior_management` 功能模块实现管理页面。两个 feature 模块都依赖 core:behaviorui。

**技术栈：** Kotlin, Jetpack Compose, Material 3, Room, Hilt, Navigation Compose, kotlinx.serialization, SAF (Storage Access Framework)

---

## 文件结构

### 新建模块

```
core/behaviorui/
  build.gradle.kts
  src/main/AndroidManifest.xml
  src/main/java/com/nltimer/core/behaviorui/
    sheet/
      AddBehaviorSheet.kt
      AddCurrentBehaviorSheet.kt
      AddTargetBehaviorSheet.kt
      ActivityPicker.kt
      TagPicker.kt
      CategoryPickerDialog.kt
      DualTimePickerComponent.kt
      TimeAdjustmentComponent.kt
      BehaviorNatureSelector.kt
      NoteInput.kt
      ActivityNoteComponent.kt
      ActivityGridComponent.kt
      AddActivityDialog.kt
      AddTagDialog.kt
      ChipItem.kt              ← 从 ActivityGridComponent 中提取的 ChipItem/Categorizable 接口
      CategoryModels.kt        ← CategoryGroup/ActivityCategorizable/TagCategorizable

feature/behavior_management/
  build.gradle.kts
  src/main/AndroidManifest.xml
  src/main/java/com/nltimer/feature/behavior_management/
    viewmodel/
      BehaviorManagementViewModel.kt
    model/
      BehaviorManagementUiState.kt
      TimeRangePreset.kt
      ImportPreview.kt
    ui/
      BehaviorManagementRoute.kt
      BehaviorManagementScreen.kt
      BehaviorListItem.kt
      BehaviorTimelineItem.kt
      FilterBar.kt
      TimeRangeSelector.kt
      ImportExportDialog.kt
    export/
      JsonExporter.kt
      JsonImporter.kt
      BehaviorExportSchema.kt
    di/
      BehaviorManagementModule.kt
```

### 修改的文件

```
settings.gradle.kts                                        — 新增两个模块
app/build.gradle.kts                                       — 新增依赖 feature:behavior_management
app/src/.../navigation/NLtimerRoutes.kt                    — 新增 BEHAVIOR_MANAGEMENT 路由
app/src/.../navigation/NLtimerNavHost.kt                   — 新增路由 composable
app/src/.../component/AppDrawer.kt                         — 新增菜单项
feature/home/build.gradle.kts                              — 依赖改为 core:behaviorui
feature/home/src/.../ui/HomeRoute.kt                       — import 路径改为 core:behaviorui
core/data/src/.../database/dao/BehaviorDao.kt              — 新增按时间范围查询
core/data/src/.../repository/BehaviorRepository.kt         — 新增接口方法
core/data/src/.../repository/impl/BehaviorRepositoryImpl.kt — 新增实现
```

---

### 任务 1：创建 `core:behaviorui` 模块骨架

**文件：**
- 创建：`core/behaviorui/build.gradle.kts`
- 创建：`core/behaviorui/src/main/AndroidManifest.xml`
- 修改：`settings.gradle.kts`

- [ ] **步骤 1：创建 build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.nltimer.android.library)
    alias(libs.plugins.nltimer.android.hilt)
}

android {
    namespace = "com.nltimer.core.behaviorui"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))

    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
```

- [ ] **步骤 2：创建 AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest />
```

- [ ] **步骤 3：在 settings.gradle.kts 中注册模块**

在 `include(` 块中添加：
```
":core:behaviorui",
":feature:behavior_management",
```

- [ ] **步骤 4：运行 Gradle Sync 确认无错**

运行：`./gradlew :core:behaviorui:tasks --quiet`
预期：SUCCESS

- [ ] **步骤 5：Commit**

```bash
git add settings.gradle.kts core/behaviorui/
git commit -m "feat(行为UI): 创建 core:behaviorui 模块骨架"
```

---

### 任务 2：将 Sheet 组件从 feature:home 移入 core:behaviorui

**文件：**
- 移动：`feature/home/src/.../ui/sheet/*.kt` → `core/behaviorui/src/.../sheet/`
- 提取：`ChipItem.kt` 和 `CategoryModels.kt`（从 ActivityGridComponent / CategoryPickerDialog 中提取）

- [ ] **步骤 1：提取共享模型到独立文件**

创建 `core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/ChipItem.kt`，将 ActivityGridComponent 中的 `ChipItem` data class 和相关接口移入，package 改为 `com.nltimer.core.behaviorui.sheet`。

创建 `core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/CategoryModels.kt`，将 CategoryPickerDialog 中的 `CategoryGroup`、`ActivityCategorizable`、`TagCategorizable` 移入，package 改为 `com.nltimer.core.behaviorui.sheet`。

- [ ] **步骤 2：逐个移动所有 sheet 文件**

将以下文件从 `feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/` 移动到 `core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/`，并将 package 声明从 `com.nltimer.feature.home.ui.sheet` 改为 `com.nltimer.core.behaviorui.sheet`：

1. AddBehaviorSheet.kt
2. AddCurrentBehaviorSheet.kt
3. AddTargetBehaviorSheet.kt
4. ActivityPicker.kt
5. TagPicker.kt
6. CategoryPickerDialog.kt
7. DualTimePickerComponent.kt
8. TimeAdjustmentComponent.kt
9. BehaviorNatureSelector.kt
10. NoteInput.kt
11. ActivityNoteComponent.kt
12. ActivityGridComponent.kt
13. AddActivityDialog.kt
14. AddTagDialog.kt

每个文件内所有 import 路径中对 `com.nltimer.feature.home.ui.sheet` 的引用改为 `com.nltimer.core.behaviorui.sheet`。

- [ ] **步骤 3：更新 feature:home 的引用**

修改 `feature/home/build.gradle.kts`，添加依赖：
```kotlin
implementation(project(":core:behaviorui"))
```

修改 `feature/home` 中所有 import 了 `com.nltimer.feature.home.ui.sheet` 的文件，将 import 路径改为 `com.nltimer.core.behaviorui.sheet`。涉及文件：
- `HomeRoute.kt`
- `HomeScreen.kt`
- `HomeViewModel.kt`（如有引用）
- 其他引用 sheet 组件的文件

- [ ] **步骤 4：删除 feature:home 中旧的 sheet 目录**

确认所有 sheet 文件已移入 core:behaviorui 且引用已更新后：
```bash
rm -rf feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/
```

- [ ] **步骤 5：运行构建确认编译通过**

运行：`./gradlew :feature:home:assembleDebug :core:behaviorui:assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 6：Commit**

```bash
git add -A
git commit -m "refactor(行为UI): 将 AddBehaviorSheet 系列组件移入 core:behaviorui 共享模块"
```

---

### 任务 3：新增 BehaviorDao 时间范围查询方法

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/database/dao/BehaviorDao.kt`
- 测试：`core/data/src/test/java/com/nltimer/core/data/repository/BehaviorRepositoryImplTest.kt`

- [ ] **步骤 1：编写失败的测试**

在 `BehaviorRepositoryImplTest.kt` 中新增测试：

```kotlin
@Test
fun getBehaviorsWithDetailsByTimeRange_returnsBehaviorsInRange() = runTest {
    // 插入行为数据在时间范围内
    val startTime = LocalDateTime.of(2026, 5, 8, 0, 0)
        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val endTime = LocalDateTime.of(2026, 5, 8, 23, 59)
        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    // ... 插入测试数据 ...
    val result = repository.getBehaviorsWithDetailsByTimeRange(startTime, endTime).first()
    assertThat(result).isNotEmpty()
}
```

- [ ] **步骤 2：运行测试验证失败**

运行：`./gradlew :core:data:test --tests "*.BehaviorRepositoryImplTest.getBehaviorsWithDetailsByTimeRange_returnsBehaviorsInRange"`
预期：FAIL（方法不存在）

- [ ] **步骤 3：在 BehaviorDao 中添加查询方法**

```kotlin
@Transaction
@Query("SELECT * FROM behaviors WHERE startTime >= :startTime AND startTime < :endTime ORDER BY startTime ASC")
fun getBehaviorsWithDetailsByTimeRange(startTime: Long, endTime: Long): Flow<List<BehaviorWithDetails>>

@Transaction
@Query("SELECT * FROM behaviors WHERE startTime >= :startTime AND startTime < :endTime ORDER BY startTime ASC")
suspend fun getBehaviorsWithDetailsByTimeRangeSync(startTime: Long, endTime: Long): List<BehaviorWithDetails>
```

同时在 `BehaviorRepository` 接口和 `BehaviorRepositoryImpl` 中添加对应方法。

- [ ] **步骤 4：运行测试验证通过**

运行：`./gradlew :core:data:test --tests "*.BehaviorRepositoryImplTest.getBehaviorsWithDetailsByTimeRange_returnsBehaviorsInRange"`
预期：PASS

- [ ] **步骤 5：Commit**

```bash
git add core/data/
git commit -m "feat(数据层): 新增按时间范围查询行为的方法"
```

---

### 任务 4：创建 `feature:behavior_management` 模块骨架

**文件：**
- 创建：`feature/behavior_management/build.gradle.kts`
- 创建：`feature/behavior_management/src/main/AndroidManifest.xml`
- 创建：`feature/behavior_management/src/main/java/com/nltimer/feature/behavior_management/model/BehaviorManagementUiState.kt`
- 创建：`feature/behavior_management/src/main/java/com/nltimer/feature/behavior_management/model/TimeRangePreset.kt`
- 创建：`feature/behavior_management/src/main/java/com/nltimer/feature/behavior_management/model/ImportPreview.kt`

- [ ] **步骤 1：创建 build.gradle.kts**

参考 `feature/tag_management/build.gradle.kts` 的模式：

```kotlin
plugins {
    alias(libs.plugins.nltimer.android.library)
    alias(libs.plugins.nltimer.android.hilt)
}

android {
    namespace = "com.nltimer.feature.behavior_management"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:behaviorui"))

    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.collections.immutable)

    debugImplementation(libs.androidx.compose.ui.tooling)

    // kotlinx.serialization for JSON export/import
    implementation(libs.kotlinx.serialization.json)
}
```

- [ ] **步骤 2：创建 AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest />
```

- [ ] **步骤 3：创建数据模型文件**

`TimeRangePreset.kt`:
```kotlin
package com.nltimer.feature.behavior_management.model

enum class TimeRangePreset(val label: String, val hours: Long) {
    FOUR_HOURS("4小时", 4),
    EIGHT_HOURS("8小时", 8),
    ONE_DAY("1日", 24),
    THREE_DAYS("3日", 72),
    SEVEN_DAYS("7日", 168),
    ONE_MONTH("1月", 720),
    ONE_YEAR("1年", 8760),
}
```

`ImportPreview.kt`:
```kotlin
package com.nltimer.feature.behavior_management.model

data class ImportPreview(
    val totalCount: Int,
    val duplicateCount: Int,
    val newCount: Int,
    val duplicateItems: List<ImportPreviewItem>,
    val newItems: List<ImportNewItem>,
)

data class ImportPreviewItem(
    val activityName: String,
    val startTime: Long,
    val endTime: Long?,
)

data class ImportNewItem(
    val type: NewItemType,
    val name: String,
)

enum class NewItemType { ACTIVITY, TAG }

enum class DuplicateHandling {
    SKIP, OVERWRITE, ALLOW,
}
```

`BehaviorManagementUiState.kt`:
```kotlin
package com.nltimer.feature.behavior_management.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.nltimer.core.data.model.BehaviorWithDetails
import com.nltimer.core.data.model.BehaviorNature
import java.time.LocalDate

data class BehaviorManagementUiState(
    val timeRange: TimeRangePreset = TimeRangePreset.ONE_DAY,
    val rangeStartDate: LocalDate = LocalDate.now(),
    val selectedActivityGroup: String? = null,
    val selectedTagCategory: String? = null,
    val selectedStatus: BehaviorNature? = null,
    val searchQuery: String = "",
    val viewMode: ViewMode = ViewMode.LIST,
    val behaviors: ImmutableList<BehaviorWithDetails> = persistentListOf(),
    val isImporting: Boolean = false,
    val importPreview: ImportPreview? = null,
    val selectedBehaviorIds: Set<Long> = emptySet(),
    val isMultiSelectMode: Boolean = false,
    val editBehaviorId: Long? = null,
)

enum class ViewMode { LIST, TIMELINE }
```

- [ ] **步骤 4：运行 Gradle Sync 确认无错**

运行：`./gradlew :feature:behavior_management:tasks --quiet`
预期：SUCCESS

- [ ] **步骤 5：Commit**

```bash
git add feature/behavior_management/ settings.gradle.kts
git commit -m "feat(行为管理): 创建 feature:behavior_management 模块骨架及数据模型"
```

---

### 任务 5：创建 BehaviorManagementViewModel

**文件：**
- 创建：`feature/behavior_management/src/.../viewmodel/BehaviorManagementViewModel.kt`
- 创建：`feature/behavior_management/src/.../di/BehaviorManagementModule.kt`
- 测试：`feature/behavior_management/src/test/.../viewmodel/BehaviorManagementViewModelTest.kt`

- [ ] **步骤 1：编写失败的测试**

测试 ViewModel 的基本功能：时间范围切换、过滤条件变更、编辑状态管理。
测试过滤逻辑：按活动分组、标签分类、状态、关键词进行 AND 过滤。

```kotlin
@Test
fun setTimeRange_updatesState() = runTest {
    viewModel.setTimeRange(TimeRangePreset.SEVEN_DAYS)
    val state = viewModel.uiState.first()
    assertThat(state.timeRange).isEqualTo(TimeRangePreset.SEVEN_DAYS)
}

@Test
fun filterByStatus_returnsOnlyMatchingBehaviors() = runTest {
    viewModel.setStatusFilter(BehaviorNature.COMPLETED)
    val state = viewModel.uiState.first()
    state.behaviors.forEach {
        assertThat(it.behavior.status).isEqualTo(BehaviorNature.COMPLETED.key)
    }
}
```

- [ ] **步骤 2：运行测试验证失败**

运行：`./gradlew :feature:behavior_management:test --tests "*.BehaviorManagementViewModelTest"`
预期：FAIL

- [ ] **步骤 3：实现 BehaviorManagementViewModel**

```kotlin
@HiltViewModel
class BehaviorManagementViewModel @Inject constructor(
    private val behaviorRepository: BehaviorRepository,
    private val activityRepository: ActivityRepository,
    private val tagRepository: TagRepository,
    private val activityGroupRepository: ActivityGroupRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BehaviorManagementUiState())
    val uiState: StateFlow<BehaviorManagementUiState> = _uiState.asStateFlow()

    // 收集活动分组、标签分类等元数据用于过滤选项
    private val activityGroups = activityGroupRepository.getAllActivityGroups()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    private val tagCategories = tagRepository.getDistinctCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        observeBehaviors()
    }

    private fun observeBehaviors() {
        viewModelScope.launch {
            _uiState.flatMapLatest { state ->
                val startDate = state.rangeStartDate
                val rangeHours = state.timeRange.hours
                val startEpoch = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endEpoch = startEpoch + rangeHours * 3600_000

                behaviorRepository.getBehaviorsWithDetailsByTimeRange(startEpoch, endEpoch)
                    .map { behaviors -> applyFilters(behaviors, state) }
            }.collect { filtered ->
                _uiState.update { it.copy(behaviors = filtered.toImmutableList()) }
            }
        }
    }

    private fun applyFilters(
        behaviors: List<BehaviorWithDetails>,
        state: BehaviorManagementUiState,
    ): List<BehaviorWithDetails> {
        var result = behaviors

        state.selectedActivityGroup?.let { groupName ->
            val groupIds = activityGroups.value
                .filter { it.name == groupName }
                .map { it.id }
            result = result.filter { it.activity.groupId in groupIds }
        }

        state.selectedTagCategory?.let { category ->
            result = result.filter { bwd ->
                bwd.tags.any { it.category == category }
            }
        }

        state.selectedStatus?.let { nature ->
            result = result.filter { it.behavior.status == nature.key }
        }

        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.lowercase()
            result = result.filter { bwd ->
                bwd.activity.name.lowercase().contains(query) ||
                    bwd.tags.any { it.name.lowercase().contains(query) } ||
                    bwd.behavior.note?.lowercase()?.contains(query) == true
            }
        }

        return result
    }

    fun setTimeRange(preset: TimeRangePreset) {
        _uiState.update { it.copy(timeRange = preset) }
    }

    fun setRangeStartDate(date: LocalDate) {
        _uiState.update { it.copy(rangeStartDate = date) }
    }

    fun navigateRange(direction: Int) {
        _uiState.update { state ->
            val daysToAdd = when (state.timeRange) {
                TimeRangePreset.FOUR_HOURS, TimeRangePreset.EIGHT_HOURS -> 0L
                TimeRangePreset.ONE_DAY -> 1L
                TimeRangePreset.THREE_DAYS -> 3L
                TimeRangePreset.SEVEN_DAYS -> 7L
                TimeRangePreset.ONE_MONTH -> 30L
                TimeRangePreset.ONE_YEAR -> 365L
            }
            state.copy(rangeStartDate = state.rangeStartDate.plusDays(daysToAdd * direction))
        }
    }

    fun setActivityGroupFilter(groupName: String?) {
        _uiState.update { it.copy(selectedActivityGroup = groupName) }
    }

    fun setTagCategoryFilter(category: String?) {
        _uiState.update { it.copy(selectedTagCategory = category) }
    }

    fun setStatusFilter(nature: BehaviorNature?) {
        _uiState.update { it.copy(selectedStatus = nature) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setViewMode(mode: ViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun startEditBehavior(behaviorId: Long) {
        _uiState.update { it.copy(editBehaviorId = behaviorId) }
    }

    fun finishEditBehavior() {
        _uiState.update { it.copy(editBehaviorId = null) }
    }

    fun toggleMultiSelect(behaviorId: Long) {
        _uiState.update { state ->
            val newIds = if (behaviorId in state.selectedBehaviorIds) {
                state.selectedBehaviorIds - behaviorId
            } else {
                state.selectedBehaviorIds + behaviorId
            }
            state.copy(
                selectedBehaviorIds = newIds,
                isMultiSelectMode = newIds.isNotEmpty(),
            )
        }
    }

    fun exitMultiSelect() {
        _uiState.update { it.copy(selectedBehaviorIds = emptySet(), isMultiSelectMode = false) }
    }
}
```

- [ ] **步骤 4：运行测试验证通过**

运行：`./gradlew :feature:behavior_management:test --tests "*.BehaviorManagementViewModelTest"`
预期：PASS

- [ ] **步骤 5：Commit**

```bash
git add feature/behavior_management/
git commit -m "feat(行为管理): 实现 BehaviorManagementViewModel 及过滤逻辑"
```

---

### 任务 6：实现 JSON 导出数据模型和 JsonExporter

**文件：**
- 创建：`feature/behavior_management/src/.../export/BehaviorExportSchema.kt`
- 创建：`feature/behavior_management/src/.../export/JsonExporter.kt`
- 测试：`feature/behavior_management/src/test/.../export/JsonExporterTest.kt`

- [ ] **步骤 1：编写失败的测试**

```kotlin
@Test
fun exportBehaviors_producesValidJson() {
    val behaviors = listOf(testBehaviorWithDetails)
    val json = JsonExporter.export(behaviors, "1日", null, null, null)
    val parsed = Json.decodeFromString<BehaviorExportData>(json)
    assertThat(parsed.version).isEqualTo(1)
    assertThat(parsed.behaviors).hasSize(1)
    assertThat(parsed.behaviors[0].activity.name).isEqualTo("写代码")
}

@Test
fun exportBehaviors_excludesId() {
    val json = JsonExporter.export(listOf(testBehaviorWithDetails), "1日", null, null, null)
    val parsed = Json.decodeFromString<BehaviorExportData>(json)
    assertThat(parsed.behaviors[0].id).isEqualTo(0)
}
```

- [ ] **步骤 2：运行测试验证失败**

运行：`./gradlew :feature:behavior_management:test --tests "*.JsonExporterTest"`
预期：FAIL

- [ ] **步骤 3：实现 BehaviorExportSchema 和 JsonExporter**

`BehaviorExportSchema.kt`:
```kotlin
@Serializable
data class BehaviorExportData(
    val version: Int = 1,
    val exportedAt: Long,
    val timeRange: TimeRangeInfo?,
    val filters: FilterInfo?,
    val behaviors: List<BehaviorExportItem>,
)

@Serializable
data class TimeRangeInfo(val start: String, val end: String, val label: String)

@Serializable
data class FilterInfo(
    val activityGroup: String? = null,
    val tagCategory: String? = null,
    val status: String? = null,
)

@Serializable
data class BehaviorExportItem(
    val id: Int = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val status: String,
    val note: String? = null,
    val pomodoroCount: Int = 0,
    val sequence: Int = 0,
    val estimatedDuration: Long? = null,
    val actualDuration: Long? = null,
    val achievementLevel: Int? = null,
    val wasPlanned: Boolean = false,
    val activity: ActivityExportItem,
    val tags: List<TagExportItem> = emptyList(),
)

@Serializable
data class ActivityExportItem(
    val name: String,
    val iconKey: String? = null,
    val color: Long? = null,
)

@Serializable
data class TagExportItem(
    val name: String,
    val color: Long? = null,
)
```

`JsonExporter.kt`:
```kotlin
object JsonExporter {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun export(
        behaviors: List<BehaviorWithDetails>,
        timeRangeLabel: String,
        startTime: Long?,
        endTime: Long?,
        filters: FilterInfo?,
    ): String {
        val data = BehaviorExportData(
            exportedAt = System.currentTimeMillis(),
            timeRange = if (startTime != null && endTime != null) {
                TimeRangeInfo(startTime.toString(), endTime.toString(), timeRangeLabel)
            } else null,
            filters = filters,
            behaviors = behaviors.map { bwd ->
                BehaviorExportItem(
                    startTime = bwd.behavior.startTime,
                    endTime = bwd.behavior.endTime,
                    status = bwd.behavior.status,
                    note = bwd.behavior.note,
                    pomodoroCount = bwd.behavior.pomodoroCount,
                    sequence = bwd.behavior.sequence,
                    estimatedDuration = bwd.behavior.estimatedDuration,
                    actualDuration = bwd.behavior.actualDuration,
                    achievementLevel = bwd.behavior.achievementLevel,
                    wasPlanned = bwd.behavior.wasPlanned,
                    activity = ActivityExportItem(
                        name = bwd.activity.name,
                        iconKey = bwd.activity.iconKey,
                        color = bwd.activity.color,
                    ),
                    tags = bwd.tags.map { TagExportItem(name = it.name, color = it.color) },
                )
            },
        )
        return json.encodeToString(data)
    }
}
```

- [ ] **步骤 4：运行测试验证通过**

运行：`./gradlew :feature:behavior_management:test --tests "*.JsonExporterTest"`
预期：PASS

- [ ] **步骤 5：Commit**

```bash
git add feature/behavior_management/
git commit -m "feat(行为管理): 实现 JSON 导出数据模型和 JsonExporter"
```

---

### 任务 7：实现 JsonImporter 及查重逻辑

**文件：**
- 创建：`feature/behavior_management/src/.../export/JsonImporter.kt`
- 测试：`feature/behavior_management/src/test/.../export/JsonImporterTest.kt`

- [ ] **步骤 1：编写失败的测试**

```kotlin
@Test
fun parseValidJson_returnsBehaviorExportData() {
    val json = """{"version":1,"exportedAt":0,"behaviors":[]}"""
    val result = JsonImporter.parse(json)
    assertThat(result).isNotNull()
    assertThat(result.behaviors).isEmpty()
}

@Test
fun detectDuplicates_flagsOverlappingSameActivity() {
    // 本地已有一个活动"写代码"在 08:00-09:00
    // 导入数据包含相同活动在 08:30-09:30
    val preview = JsonImporter.analyzeDuplicates(exportData, localActivities, existingBehaviors)
    assertThat(preview.duplicateCount).isEqualTo(1)
}

@Test
fun detectNewItems_flagsMissingActivity() {
    // 导入数据包含不存在的活动"冥想"
    val preview = JsonImporter.analyzeDuplicates(exportData, localActivities, existingBehaviors)
    assertThat(preview.newItems.any { it.name == "冥想" && it.type == NewItemType.ACTIVITY }).isTrue()
}
```

- [ ] **步骤 2：运行测试验证失败**

运行：`./gradlew :feature:behavior_management:test --tests "*.JsonImporterTest"`
预期：FAIL

- [ ] **步骤 3：实现 JsonImporter**

```kotlin
object JsonImporter {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(jsonString: String): BehaviorExportData {
        return json.decodeFromString<BehaviorExportData>(jsonString)
    }

    fun analyzeDuplicates(
        data: BehaviorExportData,
        localActivities: List<Activity>,
        localTags: List<Tag>,
        existingBehaviors: List<Behavior>,
    ): ImportPreview {
        val activityNameMap = localActivities.associateBy { it.name }
        val tagNameMap = localTags.associateBy { it.name }

        val duplicateItems = mutableListOf<ImportPreviewItem>()
        val newItems = mutableListOf<ImportNewItem>()
        var duplicateCount = 0

        val missingActivities = mutableSetOf<String>()
        val missingTags = mutableSetOf<String>()

        for (item in data.behaviors) {
            val localActivity = activityNameMap[item.activity.name]
            if (localActivity == null) {
                missingActivities.add(item.activity.name)
                continue
            }
            // 查重：同一活动 + 时间重叠
            val hasOverlap = existingBehaviors.any { existing ->
                existing.activityId == localActivity.id && timeOverlaps(
                    existing.startTime, existing.endTime,
                    item.startTime, item.endTime,
                )
            }
            if (hasOverlap) {
                duplicateCount++
                duplicateItems.add(ImportPreviewItem(
                    activityName = item.activity.name,
                    startTime = item.startTime,
                    endTime = item.endTime,
                ))
            }

            item.tags.forEach { tag ->
                if (tagNameMap[tag.name] == null) {
                    missingTags.add(tag.name)
                }
            }
        }

        missingActivities.forEach { newItems.add(ImportNewItem(NewItemType.ACTIVITY, it)) }
        missingTags.forEach { newItems.add(ImportNewItem(NewItemType.TAG, it)) }

        return ImportPreview(
            totalCount = data.behaviors.size,
            duplicateCount = duplicateCount,
            newCount = missingActivities.size + missingTags.size,
            duplicateItems = duplicateItems,
            newItems = newItems,
        )
    }

    private fun timeOverlaps(
        existingStart: Long, existingEnd: Long?,
        newStart: Long, newEnd: Long?,
    ): Boolean {
        val eEnd = existingEnd ?: Long.MAX_VALUE
        val nEnd = newEnd ?: Long.MAX_VALUE
        return newStart < eEnd && existingStart < nEnd
    }
}
```

- [ ] **步骤 4：运行测试验证通过**

运行：`./gradlew :feature:behavior_management:test --tests "*.JsonImporterTest"`
预期：PASS

- [ ] **步骤 5：Commit**

```bash
git add feature/behavior_management/
git commit -m "feat(行为管理): 实现 JsonImporter 及时间+活动查重逻辑"
```

---

### 任务 8：实现 BehaviorListItem 和 BehaviorTimelineItem UI 组件

**文件：**
- 创建：`feature/behavior_management/src/.../ui/BehaviorListItem.kt`
- 创建：`feature/behavior_management/src/.../ui/BehaviorTimelineItem.kt`

- [ ] **步骤 1：实现 BehaviorListItem**

紧凑列表项组件，显示活动色点+名称、时间、标签文字（中点分隔）、状态图标、备注截断行。行高约 56dp，轻微背景色交替。

关键参数：
```kotlin
@Composable
fun BehaviorListItem(
    behaviorWithDetails: BehaviorWithDetails,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isEvenItem: Boolean = false,
    modifier: Modifier = Modifier,
)
```

- [ ] **步骤 2：实现 BehaviorTimelineItem**

时间轴视图组件，左侧 48dp 固定宽度时间列，右侧活动名+时长+折行标签+状态。竖线连接节点。

关键参数：
```kotlin
@Composable
fun BehaviorTimelineItem(
    behaviorWithDetails: BehaviorWithDetails,
    isLast: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```

- [ ] **步骤 3：编译验证**

运行：`./gradlew :feature:behavior_management:assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add feature/behavior_management/
git commit -m "feat(行为管理): 实现行为列表项和时间轴项 UI 组件"
```

---

### 任务 9：实现 FilterBar 和 TimeRangeSelector 组件

**文件：**
- 创建：`feature/behavior_management/src/.../ui/FilterBar.kt`
- 创建：`feature/behavior_management/src/.../ui/TimeRangeSelector.kt`

- [ ] **步骤 1：实现 TimeRangeSelector**

下拉选择器 + 日期导航（左右箭头 + DatePicker）。

```kotlin
@Composable
fun TimeRangeSelector(
    currentPreset: TimeRangePreset,
    currentDate: LocalDate,
    onPresetChange: (TimeRangePreset) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onNavigate: (direction: Int) -> Unit,
    modifier: Modifier = Modifier,
)
```

使用 `ExposedDropdownMenuBox` 或自定义下拉菜单实现时间范围选择。

- [ ] **步骤 2：实现 FilterBar**

三个下拉过滤（活动分组、标签分类、状态）+ 搜索框。

```kotlin
@Composable
fun FilterBar(
    activityGroups: List<String>,
    tagCategories: List<String>,
    selectedActivityGroup: String?,
    selectedTagCategory: String?,
    selectedStatus: BehaviorNature?,
    searchQuery: String,
    onActivityGroupChange: (String?) -> Unit,
    onTagCategoryChange: (String?) -> Unit,
    onStatusChange: (BehaviorNature?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
)
```

每个过滤使用紧凑的 `DropdownMenu`，搜索框使用 `OutlinedTextField`（mini 版本）。

- [ ] **步骤 3：编译验证**

运行：`./gradlew :feature:behavior_management:assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add feature/behavior_management/
git commit -m "feat(行为管理): 实现过滤栏和时间范围选择器组件"
```

---

### 任务 10：实现 ImportExportDialog 组件

**文件：**
- 创建：`feature/behavior_management/src/.../ui/ImportExportDialog.kt`

- [ ] **步骤 1：实现导入预览弹窗**

弹窗展示：总条数/重复数/新建数 → 重复项列表 → 新建项列表 → 处理方式单选 → 确认/取消按钮。

```kotlin
@Composable
fun ImportPreviewDialog(
    preview: ImportPreview,
    onDuplicateHandlingChange: (DuplicateHandling) -> Unit,
    selectedHandling: DuplicateHandling,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
)

@Composable
fun ExportConfirmDialog(
    behaviorCount: Int,
    filterDescription: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
)
```

- [ ] **步骤 2：编译验证**

运行：`./gradlew :feature:behavior_management:assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add feature/behavior_management/
git commit -m "feat(行为管理): 实现导入预览弹窗和导出确认弹窗"
```

---

### 任务 11：实现 BehaviorManagementScreen 主页面

**文件：**
- 创建：`feature/behavior_management/src/.../ui/BehaviorManagementScreen.kt`
- 创建：`feature/behavior_management/src/.../ui/BehaviorManagementRoute.kt`

- [ ] **步骤 1：实现 BehaviorManagementScreen**

组合所有子组件：
- TopAppBar（标题 + 导入/导出按钮）
- TimeRangeSelector
- FilterBar
- 视图切换（列表/时间轴 FilterChip）
- LazyColumn（BehaviorListItem 或 BehaviorTimelineItem）
- 底部统计摘要
- AddBehaviorSheet（编辑模式）
- MultiSelect 顶部栏

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BehaviorManagementScreen(
    uiState: BehaviorManagementUiState,
    activityGroups: List<ActivityGroup>,
    tagCategories: List<String>,
    activities: List<Activity>,
    allTags: List<Tag>,
    onTimeRangeChange: (TimeRangePreset) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onNavigate: (Int) -> Unit,
    onActivityGroupFilter: (String?) -> Unit,
    onTagCategoryFilter: (String?) -> Unit,
    onStatusFilter: (BehaviorNature?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onViewModeChange: (ViewMode) -> Unit,
    onBehaviorClick: (Long) -> Unit,
    onBehaviorLongClick: (Long) -> Unit,
    onEditConfirm: (Long, Long, List<Long>, LocalTime, LocalTime?, BehaviorNature, String?) -> Unit,
    onEditDismiss: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onDeleteSelected: () -> Unit,
    onExitMultiSelect: () -> Unit,
    modifier: Modifier = Modifier,
)
```

- [ ] **步骤 2：实现 BehaviorManagementRoute**

Hilt Navigation Compose 连接 ViewModel 和 Screen：

```kotlin
@Composable
fun BehaviorManagementRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: BehaviorManagementViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ... 收集 activityGroups, tagCategories, activities, tags ...
    // ... 导入/导出逻辑（SAF + JsonExporter/JsonImporter）...
    BehaviorManagementScreen(
        uiState = uiState,
        // ... 其他参数 ...
    )
}
```

SAF 导出流程：
1. `rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json"))`
2. 获取当前过滤的行为列表
3. 调用 `JsonExporter.export()` 生成 JSON
4. 写入 SAF 返回的 Uri

SAF 导入流程：
1. `rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument("application/json"))`
2. 读取选中文件内容
3. 调用 `JsonImporter.parse()` 解析
4. 调用 `JsonImporter.analyzeDuplicates()` 生成预览
5. 显示 `ImportPreviewDialog`
6. 用户确认后根据处理方式写入数据库

- [ ] **步骤 3：编译验证**

运行：`./gradlew :feature:behavior_management:assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add feature/behavior_management/
git commit -m "feat(行为管理): 实现行为管理主页面及 Route"
```

---

### 任务 12：集成导航和侧边抽屉入口

**文件：**
- 修改：`app/build.gradle.kts` — 新增 `implementation(project(":feature:behavior_management"))`
- 修改：`app/src/.../navigation/NLtimerRoutes.kt`
- 修改：`app/src/.../navigation/NLtimerNavHost.kt`
- 修改：`app/src/.../component/AppDrawer.kt`

- [ ] **步骤 1：修改 NLtimerRoutes**

```kotlin
const val BEHAVIOR_MANAGEMENT = "behavior_management"
```

- [ ] **步骤 2：修改 NLtimerNavHost**

在 NavHost 中新增：
```kotlin
composable(NLtimerRoutes.BEHAVIOR_MANAGEMENT) {
    BehaviorManagementRoute(
        onNavigateBack = { navController.popBackStack() }
    )
}
```

需要添加 `import com.nltimer.feature.behavior_management.ui.BehaviorManagementRoute`

- [ ] **步骤 3：修改 AppDrawer**

在"标签管理"项后插入：
```kotlin
DrawerMenuItem(NLtimerRoutes.BEHAVIOR_MANAGEMENT, "行为管理", Icons.Default.EventNote),
```

添加 import：`import androidx.compose.material.icons.filled.EventNote`

- [ ] **步骤 4：修改 app/build.gradle.kts**

在 dependencies 中添加：
```kotlin
implementation(project(":feature:behavior_management"))
```

- [ ] **步骤 5：构建验证**

运行：`./gradlew :app:assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 6：Commit**

```bash
git add app/
git commit -m "feat(行为管理): 集成导航路由和侧边抽屉入口"
```

---

### 任务 13：端到端验证

- [ ] **步骤 1：运行全部单元测试**

运行：`./gradlew test`
预期：ALL PASS

- [ ] **步骤 2：运行 debug 构建**

运行：`./gradlew assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：手动验证清单**

- [ ] 侧边抽屉显示"行为管理"入口，图标 EventNote
- [ ] 点击进入行为管理页面，TopAppBar 显示"行为管理"
- [ ] 默认显示 1 日视图
- [ ] 下拉切换时间范围（4h/8h/1日/3日/7日/1月/1年）正常
- [ ] 日期左右导航正常
- [ ] 三维过滤（活动分组/标签分类/状态）+ 关键词搜索正常
- [ ] 列表/时间轴视图切换正常
- [ ] 点击行为弹出 AddBehaviorSheet 编辑
- [ ] 导出 JSON 文件正常
- [ ] 导入 JSON 文件，查重预览正常
- [ ] Home 页面 AddBehaviorSheet 功能无回归

- [ ] **步骤 4：最终 Commit**

如有修复则提交修复 commit。
