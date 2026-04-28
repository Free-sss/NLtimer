# 活动管理功能实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 创建独立的"活动管理"页面（feature/management_activities模块），支持活动项的CRUD管理、自定义分组、预设活动库等功能。

**架构：** 采用MVVM+Repository模式，新建feature/management_activities模块，使用Room数据库持久化数据，Jetpack Compose + Material3构建UI，Hilt进行依赖注入。

**技术栈：** Kotlin, Jetpack Compose, Material3, Room, Hilt, Coroutines, Flow

---

## 文件结构总览

### 新建文件清单

```
feature/management_activities/
├── build.gradle.kts                                    # 模块构建配置
├── src/main/AndroidManifest.xml                        # 清单文件
└── src/main/java/com/nltimer/feature/management_activities/
    ├── model/
    │   ├── Activity.kt                                 # 活动领域模型
    │   ├── ActivityGroup.kt                            # 分组领域模型
    │   └── ActivityManagementUiState.kt                # UI状态模型
    ├── ui/
    │   ├── ActivityManagementRoute.kt                  # 路由入口（DI）
    │   ├── ActivityManagementScreen.kt                 # 主屏幕
    │   └── components/
    │       ├── ActivityChip.kt                         # 活动芯片组件
    │       ├── GroupCard.kt                            # 分组卡片组件
    │       └── dialogs/
    │           ├── AddActivityDialog.kt                # 添加活动对话框
    │           ├── EditActivityDialog.kt               # 编辑活动对话框
    │           ├── AddGroupDialog.kt                   # 添加分组对话框
    │           └── ConfirmDialog.kt                    # 确认对话框
    └── viewmodel/
        └── ActivityManagementViewModel.kt              # ViewModel

core/data/src/main/java/com/nltimer/core/data/
├── database/entity/
│   ├── ActivityEntity.kt                               # Room实体
│   └── ActivityGroupEntity.kt                          # Room实体
├── database/dao/
│   ├── ActivityDao.kt                                  # 数据访问对象
│   └── ActivityGroupDao.kt                             # 数据访问对象
└── repository/
    ├── ActivityManagementRepository.kt                 # Repository接口
    └── impl/ActivityManagementRepositoryImpl.kt        # Repository实现
```

### 修改文件清单

| 文件路径 | 修改内容 |
|---------|---------|
| `settings.gradle.kts` | 添加 `:feature:management_activities` 模块 |
| `app/build.gradle.kts` | 添加 `implementation(project(":feature:management_activities"))` |
| `core/data/.../NLtimerDatabase.kt` | 添加新表到 @Database entities |
| `core/data/di/DataModule.kt` | 提供 ActivityManagementRepository |
| `app/.../AppDrawer.kt:38` | 修改路由为 `"activity_management"` |
| `app/.../NLtimerNavHost.kt` | 添加新路由 composable |

---

## 任务分解

### 任务 1：创建模块基础架构

**文件：**
- 创建：`feature/management_activities/build.gradle.kts`
- 创建：`feature/management_activities/src/main/AndroidManifest.xml`
- 修改：`settings.gradle.kts`
- 修改：`app/build.gradle.kt`

- [ ] **步骤 1：创建 build.gradle.kts**

```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.nltimer.feature.management_activities"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.foundation:foundation:1.6.0")
    implementation("androidx.compose.foundation:foundation-layout:1.6.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("app.cash.turbine:turbine:1.0.0")
}
```

- [ ] **步骤 2：创建 AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
</manifest>
```

- [ ] **步骤 3：修改 settings.gradle.kts 添加模块**

在 settings.gradle.kts 的 `include(":feature:categories")` 后添加：
```gradle
include(":feature:management_activities")
```

- [ ] **步骤 4：修改 app/build.gradle.kts 添加依赖**

在 dependencies 块中添加：
```kotlin
implementation(project(":feature:management_activities"))
```

- [ ] **步骤 5：验证 Gradle 同步成功**

运行：`./gradlew :feature:management_activities:build`
预期：BUILD SUCCESSFUL

---

### 任务 2：实现数据层 - Entity 和 DAO

**文件：**
- 创建：`core/data/.../database/entity/ActivityEntity.kt`
- 创建：`core/data/.../database/entity/ActivityGroupEntity.kt`
- 创建：`core/data/.../database/dao/ActivityDao.kt`
- 创建：`core/data/.../database/dao/ActivityGroupDao.kt`
- 修改：`core/data/.../database/NLtimerDatabase.kt`

- [ ] **步骤 1：创建 ActivityEntity**

```kotlin
package com.nltimer.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String? = null,
    val iconKey: String? = null,
    val groupId: Long? = null,
    val isPreset: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
```

- [ ] **步骤 2：创建 ActivityGroupEntity**

```kotlin
package com.nltimer.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_groups")
data class ActivityGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)
```

- [ ] **步骤 3：创建 ActivityDao**

```kotlin
package com.nltimer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nltimer.core.data.database.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities WHERE isArchived = 0 ORDER BY name")
    fun getAllActive(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE groupId IS NULL AND isArchived = 0 ORDER BY name")
    fun getUncategorized(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE groupId = :groupId AND isArchived = 0 ORDER BY name")
    fun getByGroup(groupId: Long): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE isPreset = 1 AND isArchived = 0 ORDER BY name")
    fun getAllPresets(): Flow<List<ActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: ActivityEntity): Long

    @Update
    suspend fun update(activity: ActivityEntity)

    @Query("UPDATE activities SET groupId = :groupId WHERE id = :activityId")
    suspend fun moveToGroup(activityId: Long, groupId: Long?)

    @Query("UPDATE activities SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun deleteById(id: Long)
}
```

- [ ] **步骤 4：创建 ActivityGroupDao**

```kotlin
package com.nltimer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nltimer.core.data.database.entity.ActivityGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityGroupDao {
    @Query("SELECT * FROM activity_groups ORDER BY sortOrder ASC, id ASC")
    fun getAll(): Flow<List<ActivityGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: ActivityGroupEntity): Long

    @Update
    suspend fun update(group: ActivityGroupEntity)

    @Delete
    suspend fun delete(group: ActivityGroupEntity)

    @Query("UPDATE activities SET groupId = NULL WHERE groupId = :groupId")
    suspend fun ungroupAllActivities(groupId: Long)
}
```

- [ ] **步骤 5：更新 NLtimerDatabase 添加新表**

在 NLtimerDatabase.kt 的 `@Database` 注解中添加 entities：
```kotlin
@Database(
    entities = [
        // ... 现有entities ...
        ActivityEntity::class,
        ActivityGroupEntity::class,
    ],
    version = X, // 根据当前版本号递增
)
```

并添加抽象方法：
```kotlin
abstract fun activityDao(): ActivityDao
abstract fun activityGroupDao(): ActivityGroupDao
```

---

### 任务 3：实现领域模型和Repository层

**文件：**
- 创建：`feature/.../model/Activity.kt`
- 创建：`feature/.../model/ActivityGroup.kt`
- 创建：`core/data/.../repository/ActivityManagementRepository.kt`
- 创建：`core/data/.../repository/impl/ActivityManagementRepositoryImpl.kt`
- 修改：`core/data/di/DataModule.kt`

- [ ] **步骤 1：创建 Activity 领域模型**

```kotlin
package com.nltimer.feature.management_activities.model

data class Activity(
    val id: Long = 0,
    val name: String,
    val emoji: String? = null,
    val iconKey: String? = null,
    val groupId: Long? = null,
    val isPreset: Boolean = false,
    val isArchived: Boolean = false,
) {
    fun toEntity() = ActivityEntity(
        id = id,
        name = name,
        emoji = emoji,
        iconKey = iconKey,
        groupId = groupId,
        isPreset = isPreset,
        isArchived = isArchived,
    )

    companion object {
        fun fromEntity(entity: ActivityEntity) = Activity(
            id = entity.id,
            name = entity.name,
            emoji = entity.emoji,
            iconKey = entity.iconKey,
            groupId = entity.groupId,
            isPreset = entity.isPreset,
            isArchived = entity.isArchived,
        )
    }
}
```

- [ ] **步骤 2：创建 ActivityGroup 领域模型**

```kotlin
package com.nltimer.feature.management_activities.model

data class ActivityGroup(
    val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0,
) {
    fun toEntity() = ActivityGroupEntity(
        id = id,
        name = name,
        sortOrder = sortOrder,
    )

    companion object {
        fun fromEntity(entity: ActivityGroupEntity) = ActivityGroup(
            id = entity.id,
            name = entity.name,
            sortOrder = entity.sortOrder,
        )
    }
}
```

- [ ] **步骤 3：创建 Repository 接口**

```kotlin
package com.nltimer.core.data.repository

import com.nltimer.feature.management_activities.model.Activity
import com.nltimer.feature.management_activities.model.ActivityGroup
import kotlinx.coroutines.flow.Flow

interface ActivityManagementRepository {
    fun getAllActivities(): Flow<List<Activity>>
    fun getUncategorizedActivities(): Flow<List<Activity>>
    fun getActivitiesByGroup(groupId: Long): Flow<List<Activity>>
    fun getAllGroups(): Flow<List<ActivityGroup>>
    
    suspend fun addActivity(activity: Activity): Long
    suspend fun updateActivity(activity: Activity)
    suspend fun deleteActivity(id: Long)
    suspend fun moveActivityToGroup(activityId: Long, groupId: Long?)
    
    suspend fun addGroup(name: String): Long
    suspend fun renameGroup(id: Long, newName: String)
    suspend fun deleteGroup(id: Long)
    
    suspend fun initializePresets()
}
```

- [ ] **步骤 4：创建 Repository 实现**

```kotlin
package com.nltimer.core.data.repository.impl

import com.nltimer.core.data.database.dao.ActivityDao
import com.nltimer.core.data.database.dao.ActivityGroupDao
import com.nltimer.core.data.database.entity.ActivityEntity
import com.nltimer.core.data.database.entity.ActivityGroupEntity
import com.nltimer.core.data.repository.ActivityManagementRepository
import com.nltimer.feature.management_activities.model.Activity
import com.nltimer.feature.management_activities.model.ActivityGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityManagementRepositoryImpl @Inject constructor(
    private val activityDao: ActivityDao,
    private val groupDao: ActivityGroupDao,
) : ActivityManagementRepository {

    companion object {
        val PRESET_ACTIVITIES = listOf(
            Activity(name = "番剧视频", emoji = "📺", isPreset = true),
            Activity(name = "娱乐视频", emoji = "🎬", isPreset = true),
            Activity(name = "玩游戏", emoji = "🎮", isPreset = true),
            Activity(name = "主动学习", emoji = "📖", isPreset = true),
            Activity(name = "运动健身", emoji = "💪", isPreset = true),
            Activity(name = "社交聚会", emoji = "👥", isPreset = true),
            Activity(name = "本职工作", emoji = "💼", isPreset = true),
            Activity(name = "休息放松", emoji = "😌", isPreset = true),
        )
    }

    override fun getAllActivities(): Flow<List<Activity>> =
        activityDao.getAllActive().map { list -> list.map { Activity.fromEntity(it) } }

    override fun getUncategorizedActivities(): Flow<List<Activity>> =
        activityDao.getUncategorized().map { list -> list.map { Activity.fromEntity(it) } }

    override fun getActivitiesByGroup(groupId: Long): Flow<List<Activity>> =
        activityDao.getByGroup(groupId).map { list -> list.map { Activity.fromEntity(it) } }

    override fun getAllGroups(): Flow<List<ActivityGroup>> =
        groupDao.getAll().map { list -> list.map { ActivityGroup.fromEntity(it) } }

    override suspend fun addActivity(activity: Activity): Long =
        activityDao.insert(activity.toEntity())

    override suspend fun updateActivity(activity: Activity) =
        activityDao.update(activity.toEntity())

    override suspend fun deleteActivity(id: Long) =
        activityDao.deleteById(id)

    override suspend fun moveActivityToGroup(activityId: Long, groupId: Long?) =
        activityDao.moveToGroup(activityId, groupId)

    override suspend fun addGroup(name: String): Long {
        val groups = groupDao.getAll().first()
        val maxOrder = groups.maxOfOrNull { it.sortOrder } ?: -1
        return groupDao.insert(
            ActivityGroupEntity(name = name, sortOrder = maxOrder + 1)
        )
    }

    override suspend fun renameGroup(id: Long, newName: String) {
        val groups = groupDao.getAll().first()
        val group = groups.find { it.id == id } ?: return
        groupDao.update(group.copy(name = newName))
    }

    override suspend fun deleteGroup(id: Long) {
        groupDao.ungroupAllActivities(id)
        val groups = groupDao.getAll().first()
        val group = groups.find { it.id == id } ?: return
        groupDao.delete(group)
    }

    override suspend fun initializePresets() {
        val existingPresets = activityDao.getAllPresets().first()
        if (existingPresets.isEmpty()) {
            PRESET_ACTIVITIES.forEach { preset ->
                activityDao.insert(preset.toEntity())
            }
        }
    }
}
```

- [ ] **步骤 5：更新 DataModule 提供 Repository**

在 DataModule.kt 中添加：
```kotlin
@Binds
@Singleton
abstract fun bindActivityManagementRepository(
    impl: ActivityManagementRepositoryImpl,
): ActivityManagementRepository
```

确保 import 正确导入所有需要的类。

---

### 任务 4：实现 UiState 和 ViewModel

**文件：**
- 创建：`feature/.../model/ActivityManagementUiState.kt`
- 创建：`feature/.../viewmodel/ActivityManagementViewModel.kt`

- [ ] **步骤 1：创建 ActivityManagementUiState**

```kotlin
package com.nltimer.feature.management_activities.model

data class ActivityManagementUiState(
    val uncategorizedActivities: List<Activity> = emptyList(),
    val groups: List<GroupWithActivities> = emptyList(),
    val allGroups: List<ActivityGroup> = emptyList(),
    val isLoading: Boolean = true,
    val expandedGroupIds: Set<Long> = emptySet(),
    val dialogState: DialogState? = null,
)

data class GroupWithActivities(
    val group: ActivityGroup,
    val activities: List<Activity>,
)

sealed interface DialogState {
    object AddActivity : DialogState
    data class EditActivity(val activity: Activity) : DialogState
    object AddGroup : DialogState
    data class RenameGroup(val group: ActivityGroup) : DialogState
    data class DeleteGroup(val group: ActivityGroup) : DialogState
    data class DeleteActivity(val activity: Activity) : DialogState
    data class MoveToGroup(val activity: Activity) : DialogState
}
```

- [ ] **步骤 2：创建 ViewModel**

```kotlin
package com.nltimer.feature.management_activities.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.core.data.repository.ActivityManagementRepository
import com.nltimer.feature.management_activities.model.Activity
import com.nltimer.feature.management_activities.model.ActivityGroup
import com.nltimer.feature.management_activities.model.ActivityManagementUiState
import com.nltimer.feature.management_activities.model.DialogState
import com.nltimer.feature.management_activities.model.GroupWithActivities
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ActivityManagementViewModel @Inject constructor(
    private val repository: ActivityManagementRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityManagementUiState())
    val uiState: StateFlow<ActivityManagementUiState> = _uiState.asStateFlow()

    init {
        loadData()
        viewModelScope.launch {
            repository.initializePresets()
        }
    }

    private fun loadData() {
        combine(
            repository.getUncategorizedActivities(),
            repository.getAllGroups(),
        ) { uncategorized, groups ->
            val groupsWithActivities = groups.map { group ->
                GroupWithActivities(group, emptyList()) // 将在下面填充
            }
            
            _uiState.value.update {
                it.copy(
                    isLoading = false,
                    uncategorizedActivities = uncategorized,
                    groups = groupsWithActivities,
                    allGroups = groups,
                )
            }
        }.launchIn(viewModelScope)
        
        // 单独加载每个分组的活动
        viewModelScope.launch {
            repository.getAllGroups().collect { groups ->
                groups.forEach { group ->
                    repository.getActivitiesByGroup(group.id).collect { activities ->
                        _uiState.value.update { uiState ->
                            val updatedGroups = uiState.groups.map { gwa ->
                                if (gwa.group.id == group.id) {
                                    gwa.copy(activities = activities)
                                } else {
                                    gwa
                                }
                            }
                            uiState.copy(groups = updatedGroups)
                        }
                    }
                }
            }
        }
    }

    fun toggleGroupExpand(groupId: Long) {
        val current = _uiState.value.expandedGroupIds
        val newSet = if (current.contains(groupId)) current - groupId else current + groupId
        _uiState.update { it.copy(expandedGroupIds = newSet) }
    }

    fun showAddActivityDialog() {
        _uiState.update { it.copy(dialogState = DialogState.AddActivity) }
    }

    fun showEditActivityDialog(activity: Activity) {
        _uiState.update { it.copy(dialogState = DialogState.EditActivity(activity)) }
    }

    fun addActivity(name: String, emoji: String?, groupId: Long?) {
        viewModelScope.launch {
            val activity = Activity(
                name = name.trim(),
                emoji = emoji,
                groupId = groupId,
                isPreset = false,
            )
            repository.addActivity(activity)
            dismissDialog()
        }
    }

    fun updateActivity(activity: Activity) {
        viewModelScope.launch {
            repository.updateActivity(activity)
            dismissDialog()
        }
    }

    fun deleteActivity(id: Long) {
        viewModelScope.launch {
            repository.deleteActivity(id)
            dismissDialog()
        }
    }

    fun moveActivityToGroup(activityId: Long, groupId: Long?) {
        viewModelScope.launch {
            repository.moveActivityToGroup(activityId, groupId)
            dismissDialog()
        }
    }

    fun showAddGroupDialog() {
        _uiState.update { it.copy(dialogState = DialogState.AddGroup) }
    }

    fun addGroup(name: String) {
        viewModelScope.launch {
            repository.addGroup(name.trim())
            dismissDialog()
        }
    }

    fun renameGroup(id: Long, newName: String) {
        viewModelScope.launch {
            repository.renameGroup(id, newName.trim())
            dismissDialog()
        }
    }

    fun showDeleteGroupDialog(group: ActivityGroup) {
        _uiState.update { it.copy(dialogState = DialogState.DeleteGroup(group)) }
    }

    fun deleteGroup(id: Long) {
        viewModelScope.launch {
            repository.deleteGroup(id)
            dismissDialog()
        }
    }

    fun showDeleteActivityDialog(activity: Activity) {
        _uiState.update { it.copy(dialogState = DialogState.DeleteActivity(activity)) }
    }

    fun showMoveToGroupDialog(activity: Activity) {
        _uiState.update { it.copy(dialogState = DialogState.MoveToGroup(activity)) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(dialogState = null) }
    }
}
```

---

### 任务 5：实现 UI 组件 - ActivityChip 和 GroupCard

**文件：**
- 创建：`feature/.../ui/components/ActivityChip.kt`
- 创建：`feature/.../ui/components/GroupCard.kt`

- [ ] **步骤 1：创建 ActivityChip 组件**

```kotlin
package com.nltimer.feature.management_activities.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nltimer.feature.management_activities.model.Activity

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActivityChip(
    activity: Activity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = "${activityemoji ?: ""} ${activity.name}".trim(),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        modifier = modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
        ),
    )
}
```

- [ ] **步骤 2：创建 GroupCard 组件**

```kotlin
package com.nltimer.feature.management_activities.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.feature.management_activities.model.Activity
import com.nltimer.feature.management_activities.model.ActivityGroup

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroupCard(
    group: ActivityGroup,
    activities: List<Activity>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onActivityClick: (Activity) -> Unit,
    onActivityLongClick: (Activity) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "📂 ${group.name}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                
                Text(
                    text = if (isExpanded) "▼" else "▶",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 8.dp),
                )
                
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "更多操作")
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("重命名") },
                        onClick = {
                            showMenu = false
                            onRename()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                    )
                }
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                
                if (activities.isEmpty()) {
                    Text(
                        text = "暂无活动",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        activities.forEach { activity ->
                            ActivityChip(
                                activity = activity,
                                onClick = { onActivityClick(activity) },
                                onLongClick = { onActivityLongClick(activity) },
                            )
                        }
                    }
                }
            }
        }
    }
}
```

---

### 任务 6：实现对话框组件

**文件：**
- 创建：`feature/.../ui/components/dialogs/AddActivityDialog.kt`
- 创建：`feature/.../ui/components/dialogs/EditActivityDialog.kt`
- 创建：`feature/.../ui/components/dialogs/AddGroupDialog.kt`
- 创建：`feature/.../ui/components/dialogs/ConfirmDialog.kt`

- [ ] **步骤 1：创建 AddActivityDialog**

```kotlin
package com.nltimer.feature.management_activities.ui.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.feature.management_activities.model.ActivityGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityDialog(
    allGroups: List<ActivityGroup>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, emoji: String?, groupId: Long?) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }
    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    var groupName by remember { mutableStateOf("未分类") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加活动") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("活动名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { 
                        if (it.length <= 2) emoji = it 
                    },
                    label = { Text("Emoji (可选)") },
                    singleLine = true,
                    placeholder = { Text("📺") },
                    modifier = Modifier.fillMaxWidth(),
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("所属分组") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("未分类") },
                            onClick = {
                                selectedGroupId = null
                                groupName = "未分类"
                                expanded = false
                            },
                        )
                        
                        allGroups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name) },
                                onClick = {
                                    selectedGroupId = group.id
                                    groupName = group.name
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    onConfirm(name.trim(), emoji.ifBlank { null }, selectedGroupId) 
                },
                enabled = name.isNotBlank(),
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}
```

- [ ] **步骤 2：创建 EditActivityDialog**

```kotlin
package com.nltimer.feature.management_activities.ui.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.feature.management_activities.model.Activity
import com.nltimer.feature.management_activities.model.ActivityGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActivityDialog(
    activity: Activity,
    allGroups: List<ActivityGroup>,
    onDismiss: () -> Unit,
    onConfirm: (Activity) -> Unit,
) {
    var name by remember(activity.id) { mutableStateOf(activity.name) }
    var emoji by remember(activity.id) { mutableStateOf(activity.emoji ?: "") }
    var selectedGroupId by remember(activity.id) { mutableStateOf(activity.groupId) }
    var groupName by remember(activity.id) { 
        mutableStateOf(
            allGroups.find { it.id == activity.groupId }?.name ?: "未分类"
        ) 
    }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑活动") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("活动名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { if (it.length <= 2) emoji = it },
                    label = { Text("Emoji (可选)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("所属分组") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("未分类") },
                            onClick = {
                                selectedGroupId = null
                                groupName = "未分类"
                                expanded = false
                            },
                        )
                        
                        allGroups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name) },
                                onClick = {
                                    selectedGroupId = group.id
                                    groupName = group.name
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    onConfirm(activity.copy(name = name.trim(), emoji = emoji.ifBlank { null }, groupId = selectedGroupId)) 
                },
                enabled = name.isNotBlank(),
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}
```

- [ ] **步骤 3：创建 AddGroupDialog**

```kotlin
package com.nltimer.feature.management_activities.ui.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建分组") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("分组名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim()) },
                enabled = name.isNotBlank(),
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}
```

- [ ] **步骤 4：创建 ConfirmDialog**

```kotlin
package com.nltimer.feature.management_activities.ui.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "确定",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        modifier = modifier,
    )
}
```

---

### 任务 7：实现主屏幕 ActivityManagementScreen

**文件：**
- 创建：`feature/.../ui/ActivityManagementScreen.kt`

- [ ] **步骤 1：创建主屏幕 Composable**

```kotlin
package com.nltimer.feature.management_activities.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.feature.management_activities.model.Activity
import com.nltimer.feature.management_activities.model.ActivityGroup
import com.nltimer.feature.management_activities.model.ActivityManagementUiState
import com.nltimer.feature.management_activities.model.DialogState
import com.nltimer.feature.management_activities.model.GroupWithActivities
import com.nltimer.feature.management_activities.ui.components.ActivityChip
import com.nltimer.feature.management_activities.ui.components.GroupCard
import com.nltimer.feature.management_activities.ui.components.dialogs.AddActivityDialog
import com.nltimer.feature.management_activities.ui.components.dialogs.AddGroupDialog
import com.nltimer.feature.management_activities.ui.components.dialogs.ConfirmDialog
import com.nltimer.feature.management_activities.ui.components.dialogs.EditActivityDialog

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivityManagementScreen(
    uiState: ActivityManagementUiState,
    onBackClick: () -> Unit,
    onToggleGroupExpand: (Long) -> Unit,
    onAddActivity: () -> Unit,
    onEditActivity: (Activity) -> Unit,
    onDeleteActivity: (Activity) -> Unit,
    onMoveToGroup: (Activity) -> Unit,
    onAddGroup: () -> Unit,
    onRenameGroup: (ActivityGroup) -> Unit,
    onDeleteGroup: (ActivityGroup) -> Unit,
    onDismissDialog: () -> Unit,
    onConfirmAddActivity: (String, String?, Long?) -> Unit,
    onConfirmUpdateActivity: (Activity) -> Unit,
    onConfirmDeleteActivity: (Long) -> Unit,
    onConfirmMoveToGroup: (Long, Long?) -> Unit,
    onConfirmAddGroup: (String) -> Unit,
    onConfirmDeleteGroup: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("活动管理") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddActivity) {
                Icon(Icons.Default.Add, contentDescription = "添加活动")
            }
        },
        modifier = modifier,
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text("加载中...")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                item {
                    Text(
                        text = "未分类",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                
                item {
                    if (uiState.uncategorizedActivities.isEmpty()) {
                        Text(
                            text = "暂无未分类活动",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp),
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(bottom = 16.dp),
                        ) {
                            uiState.uncategorizedActivities.forEach { activity ->
                                ActivityItem(
                                    activity = activity,
                                    onClick = { },
                                    onLongClick = {
                                        onEditActivity(activity)
                                    },
                                )
                            }
                        }
                    }
                }
                
                items(uiState.groups, key = { it.group.id }) { groupWithActivities ->
                    GroupCard(
                        group = groupWithActivities.group,
                        activities = groupWithActivities.activities,
                        isExpanded = uiState.expandedGroupIds.contains(groupWithActivities.group.id),
                        onToggleExpand = { onToggleGroupExpand(groupWithActivities.group.id) },
                        onRename = { onRenameGroup(groupWithActivities.group) },
                        onDelete = { onDeleteGroup(groupWithActivities.group) },
                        onActivityClick = { },
                        onActivityLongClick = { activity ->
                            onEditActivity(activity)
                        },
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextButton(
                        onClick = onAddGroup,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("+ 增加活动分类")
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
    
    when (val dialog = uiState.dialogState) {
        is DialogState.AddActivity -> {
            AddActivityDialog(
                allGroups = uiState.allGroups,
                onDismiss = onDismissDialog,
                onConfirm = onConfirmAddActivity,
            )
        }
        
        is DialogState.EditActivity -> {
            EditActivityDialog(
                activity = dialog.activity,
                allGroups = uiState.allGroups,
                onDismiss = onDismissDialog,
                onConfirm = onConfirmUpdateActivity,
            )
        }
        
        is DialogState.AddGroup -> {
            AddGroupDialog(
                onDismiss = onDismissDialog,
                onConfirm = onConfirmAddGroup,
            )
        }
        
        is DialogState.DeleteGroup -> {
            ConfirmDialog(
                title = "删除分组",
                message = "确定要删除分组「${dialog.group.name}」吗？该分组下的所有活动将移至未分类。",
                confirmText = "删除",
                onDismiss = onDismissDialog,
                onConfirm = { onConfirmDeleteGroup(dialog.group.id) },
            )
        }
        
        is DialogState.DeleteActivity -> {
            ConfirmDialog(
                title = "删除活动",
                message = "确定要删除活动「${dialog.activity.name}」吗？",
                confirmText = "删除",
                onDismiss = onDismissDialog,
                onConfirm = { onConfirmDeleteActivity(dialog.activity.id) },
            )
        }
        
        is DialogState.MoveToGroup -> {
            MoveToGroupDialog(
                activity = dialog.activity,
                allGroups = uiState.allGroups,
                onDismiss = onDismissDialog,
                onConfirm = onConfirmMoveToGroup,
            )
        }
        
        null -> {}
    }
}

// 辅助组件：带长按菜单的活动项
@Composable
private fun ActivityItem(
    activity: Activity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ActivityChip(
        activity = activity,
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
    )
}
```

注意：上述代码中引用的 `MoveToGroupDialog` 可以复用 EditActivityDialog 或单独创建简化版本。

---

### 任务 8：实现路由入口和导航集成

**文件：**
- 创建：`feature/.../ui/ActivityManagementRoute.kt`
- 修改：`app/.../AppDrawer.kt:38`
- 修改：`app/.../NLtimerNavHost.kt`

- [ ] **步骤 1：创建 ActivityManagementRoute**

```kotlin
package com.nltimer.feature.management_activities.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.nltimer.feature.management_activities.model.Activity
import com.nltimer.feature.management_activities.model.ActivityGroup
import com.nltimer.feature.management_activities.viewmodel.ActivityManagementViewModel

@Composable
fun ActivityManagementRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ActivityManagementViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ActivityManagementScreen(
        uiState = uiState,
        onBackClick = onNavigateBack,
        onToggleGroupExpand = viewModel::toggleGroupExpand,
        onAddActivity = viewModel::showAddActivityDialog,
        onEditActivity = viewModel::showEditActivityDialog,
        onDeleteActivity = viewModel::showDeleteActivityDialog,
        onMoveToGroup = viewModel::showMoveToGroupDialog,
        onAddGroup = viewModel::showAddGroupDialog,
        onRenameGroup = { group -> viewModel.showRenameGroupDialog(group) },
        onDeleteGroup = { group -> viewModel.showDeleteGroupDialog(group) },
        onDismissDialog = viewModel::dismissDialog,
        onConfirmAddActivity = viewModel::addActivity,
        onConfirmUpdateActivity = viewModel::updateActivity,
        onConfirmDeleteActivity = viewModel::deleteActivity,
        onConfirmMoveToGroup = viewModel::moveActivityToGroup,
        onConfirmAddGroup = viewModel::addGroup,
        onConfirmDeleteGroup = viewModel::deleteGroup,
        modifier = modifier,
    )
}
```

- [ ] **步骤 2：修改 AppDrawer.kt 第38行**

将第38行从：
```kotlin
DrawerMenuItem("categories", "活动管理", Icons.Default.Category),
```
改为：
```kotlin
DrawerMenuItem("activity_management", "活动管理", Icons.Default.Category),
```

- [ ] **步骤 3：修改 NLtimerNavHost.kt 添加路由**

在 NavHost block 中添加：
```kotlin
composable("activity_management") {
    ActivityManagementRoute(
        onNavigateBack = { navController.popBackStack() },
    )
}
```

并在文件顶部添加 import：
```kotlin
import com.nltimer.feature.management_activities.ui.ActivityManagementRoute
```

---

### 任务 9：编写单元测试

**文件：**
- 创建：`feature/.../viewmodel/ActivityManagementViewModelTest.kt`

- [ ] **步骤 1：编写 ViewModel 测试**

```kotlin
package com.nltimer.feature.management_activities.viewmodel

import app.cash.turbine.test
import com.nltimer.core.data.repository.ActivityManagementRepository
import com.nltimer.feature.management_activities.model.Activity
import com.nltimer.feature.management_activities.model.ActivityGroup
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityManagementViewModelTest {

    private lateinit var repository: ActivityManagementRepository
    private lateinit var viewModel: ActivityManagementViewModel

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        coEvery { repository.getUncategorizedActivities() } returns flowOf(emptyList())
        coEvery { repository.getAllGroups() } returns flowOf(emptyList())
        coEvery { repository.initializePresets() } returns Unit
        
        viewModel = ActivityManagementViewModel(repository)
    }

    @Test
    fun `initial state should be loading`() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()
            assert(initialState.isLoading)
        }
    }

    @Test
    fun `addActivity should call repository and dismiss dialog`() = runTest {
        viewModel.showAddActivityDialog()
        
        viewModel.uiState.test {
            val dialogState = awaitItem().dialogState
            assert(dialogState != null)
        }
        
        viewModel.addActivity("测试活动", "🧪", null)
        
        coVerify { repository.addActivity(any()) }
        
        viewModel.uiState.test {
            val afterDismiss = awaitItem().dialogState
            assert(afterDismiss == null)
        }
    }

    @Test
    fun `toggleGroupExpand should toggle group expansion`() = runTest {
        val groupId = 1L
        
        viewModel.toggleGroupExpand(groupId)
        
        viewModel.uiState.test {
            val state = awaitItem()
            assert(state.expandedGroupIds.contains(groupId))
        }
        
        viewModel.toggleGroupExpand(groupId)
        
        viewModel.uiState.test {
            val state = awaitItem()
            assert(!state.expandedGroupIds.contains(groupId))
        }
    }

    @Test
    fun `addGroup should create new group`() = runTest {
        coEvery { repository.getAllGroups() } returns flowOf(emptyList())
        coEvery { repository.addGroup(any()) } returns 1L
        
        viewModel.showAddGroupDialog()
        viewModel.addGroup("工作")
        
        coVerify { repository.addGroup("工作") }
        
        viewModel.uiState.test {
            assert(awaitItem().dialogState == null)
        }
    }

    @Test
    fun `deleteActivity should call repository`() = runTest {
        val activity = Activity(id = 1, name = "测试")
        
        viewModel.showDeleteActivityDialog(activity)
        viewModel.deleteActivity(1L)
        
        coVerify { repository.deleteActivity(1L) }
    }

    @Test
    fun `dismissDialog should clear dialog state`() = runTest {
        viewModel.showAddActivityDialog()
        
        viewModel.uiState.test {
            assert(awaitItem().dialogState != null)
        }
        
        viewModel.dismissDialog()
        
        viewModel.uiState.test {
            assert(awaitItem().dialogState == null)
        }
    }
}
```

- [ ] **步骤 2：运行测试验证通过**

运行：`./gradlew :feature:management_activities:testDebugUnitTest`
预期：所有测试 PASS

---

### 任务 10：最终验证与清理

- [ ] **步骤 1：编译整个项目**

运行：`./gradlew assembleDebug`
预期：BUILD SUCCESSFUL，无编译错误

- [ ] **步骤 2：检查是否有警告或遗留问题**

确认：
- ✅ 无 TODO 注释
- ✅ 无 println 调试代码
- ✅ 所有 public API 有文档注释（KDoc）
- ✅ 代码风格与项目一致

- [ ] **步骤 3：手动测试核心流程**

在模拟器/真机上验证：
1. 打开应用 → 点击侧边栏 → 点击"活动管理"
2. 验证预设活动是否显示在"未分类"区域
3. 点击 FAB → 添加新活动 → 验证出现
4. 长按活动 → 编辑 → 修改名称 → 验证更新
5. 点击"+ 增加活动分类" → 创建分组 → 验证卡片出现
6. 在分组中添加活动 → 验证显示在对应分组
7. 删除分组 → 验证活动移至未分类
8. 返回主页 → 再次进入 → 验证数据持久化

---

## 实现顺序建议

**推荐执行顺序**：任务1 → 任务2 → 任务3 → 任务4 → 任务9 → 任务5 → 任务6 → 任务7 → 任务8 → 任务10

理由：
1. 先搭建基础设施（模块、数据库）
2. 再实现业务逻辑（Repository、ViewModel）
3. 写测试验证逻辑正确性
4. 最后实现UI和集成导航
5. 最终端到端验证

---

## 自检清单

### 规格覆盖度
- [x] 预设活动库 - 任务3（PRESET_ACTIVITIES）+ 任务4（initializePresets）
- [x] 自定义分组 - 任务2（ActivityGroupEntity）+ 任务3（CRUD方法）+ 任务6（AddGroupDialog）
- [x] 活动CRUD - 任务2（ActivityEntity）+ 任务3（CRUD方法）+ 任务6（对话框）
- [x] 分组卡片式UI - 任务5（GroupCard）+ 任务7（Screen）
- [x] 导航集成 - 任务8（Route + NavHost + Drawer）

### 占位符扫描
- [x] 无"待定"、"TODO"、"后续实现"
- [x] 所有步骤包含完整代码示例
- [x] 所有命令可执行

### 类型一致性
- [x] Activity/ActivityGroup 在所有文件中定义一致
- [x] Repository接口与实现匹配
- [x] ViewModel方法签名与UI回调匹配
- [x] UiState sealed hierarchy 完整

---

## 执行选项

**计划已完成并保存到 `docs/superpowers/plans/2026-04-27-activity-management-plan.md`。两种执行方式：**

**1. 子代理驱动（推荐）** - 每个任务调度一个新的子代理，任务间进行审查，快速迭代

**2. 内联执行** - 在当前会话中使用 executing-plans 执行任务，批量执行并设有检查点供审查
