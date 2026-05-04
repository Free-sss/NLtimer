# 数据库调试工具 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 在调试页面新增「数据库工具」组件，支持清除/插入/查询数据库数据

**架构：** 在 feature/debug 模块新增 DebugDatabaseHelper（Hilt 单例）封装数据库操作，新增 DatabaseToolsPreview Composable 展示 UI，通过 DebugComponentRegistry 注册到调试页面。需在 core/data 的 4 个 DAO 中补充 deleteAll 和关联表查询方法。

**技术栈：** Room DAO + Hilt DI + Jetpack Compose + Kotlin Coroutines

---

## 文件结构

| 文件 | 操作 | 职责 |
|------|------|------|
| `core/data/.../dao/ActivityDao.kt` | 修改 | + deleteAll() |
| `core/data/.../dao/ActivityGroupDao.kt` | 修改 | + deleteAll() |
| `core/data/.../dao/TagDao.kt` | 修改 | + deleteAll() |
| `core/data/.../dao/BehaviorDao.kt` | 修改 | + deleteAll()、deleteAllTagCrossRefs()、deleteAllActivityTagBindings()、getAllCrossRefsSync()、getAllActivityTagBindingsSync() |
| `feature/debug/.../data/DebugDatabaseHelper.kt` | 新建 | 数据库操作辅助类 |
| `feature/debug/.../ui/preview/DatabaseToolsPreview.kt` | 新建 | UI 组件 |
| `feature/debug/.../FeatureDebugComponents.kt` | 修改 | 注册 DatabaseTools 组件 |

---

### 任务 1：ActivityDao 新增 deleteAll 方法

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/database/dao/ActivityDao.kt`

- [ ] **步骤 1：在 ActivityDao 末尾添加 deleteAll 方法**

在 `ActivityDao.kt` 文件末尾（`deleteById` 方法之后）添加：

```kotlin
    @Query("DELETE FROM activities")
    suspend fun deleteAll()
```

- [ ] **步骤 2：编译验证**

运行：`.\gradlew :core:data:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/database/dao/ActivityDao.kt
git commit -m "feat(data): ActivityDao 新增 deleteAll 方法"
```

---

### 任务 2：ActivityGroupDao 新增 deleteAll 方法

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/database/dao/ActivityGroupDao.kt`

- [ ] **步骤 1：在 ActivityGroupDao 末尾添加 deleteAll 方法**

在 `ActivityGroupDao.kt` 文件末尾（`deleteByName` 方法之后）添加：

```kotlin
    @Query("DELETE FROM activity_groups")
    suspend fun deleteAll()
```

- [ ] **步骤 2：编译验证**

运行：`.\gradlew :core:data:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/database/dao/ActivityGroupDao.kt
git commit -m "feat(data): ActivityGroupDao 新增 deleteAll 方法"
```

---

### 任务 3：TagDao 新增 deleteAll 方法

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/database/dao/TagDao.kt`

- [ ] **步骤 1：在 TagDao 末尾添加 deleteAll 方法**

在 `TagDao.kt` 文件末尾（`resetCategory` 方法之后）添加：

```kotlin
    @Query("DELETE FROM tags")
    suspend fun deleteAll()
```

- [ ] **步骤 2：编译验证**

运行：`.\gradlew :core:data:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/database/dao/TagDao.kt
git commit -m "feat(data): TagDao 新增 deleteAll 方法"
```

---

### 任务 4：BehaviorDao 新增 deleteAll 及关联表操作方法

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/database/dao/BehaviorDao.kt`

- [ ] **步骤 1：在 BehaviorDao 末尾添加 5 个方法**

在 `BehaviorDao.kt` 文件末尾（`getTagsForBehaviorSync` 方法之后）添加：

```kotlin
    @Query("DELETE FROM behaviors")
    suspend fun deleteAll()

    @Query("DELETE FROM behavior_tag_cross_ref")
    suspend fun deleteAllTagCrossRefs()

    @Query("DELETE FROM activity_tag_binding")
    suspend fun deleteAllActivityTagBindings()

    @Query("SELECT * FROM behavior_tag_cross_ref")
    suspend fun getAllCrossRefsSync(): List<BehaviorTagCrossRefEntity>

    @Query("SELECT * FROM activity_tag_binding")
    suspend fun getAllActivityTagBindingsSync(): List<ActivityTagBindingEntity>
```

- [ ] **步骤 2：编译验证**

运行：`.\gradlew :core:data:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/database/dao/BehaviorDao.kt
git commit -m "feat(data): BehaviorDao 新增 deleteAll 及关联表操作方法"
```

---

### 任务 5：创建 DebugDatabaseHelper

**文件：**
- 创建：`feature/debug/src/main/java/com/nltimer/feature/debug/data/DebugDatabaseHelper.kt`

- [ ] **步骤 1：创建 DebugDatabaseHelper 类**

```kotlin
package com.nltimer.feature.debug.data

import com.nltimer.core.data.database.NLtimerDatabase
import com.nltimer.core.data.database.dao.ActivityDao
import com.nltimer.core.data.database.dao.ActivityGroupDao
import com.nltimer.core.data.database.dao.BehaviorDao
import com.nltimer.core.data.database.dao.TagDao
import com.nltimer.core.data.database.entity.ActivityEntity
import com.nltimer.core.data.database.entity.ActivityGroupEntity
import com.nltimer.core.data.database.entity.ActivityTagBindingEntity
import com.nltimer.core.data.database.entity.BehaviorEntity
import com.nltimer.core.data.database.entity.BehaviorTagCrossRefEntity
import com.nltimer.core.data.database.entity.TagEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据库调试操作辅助类
 * 封装调试页面所需的数据库清除、插入测试数据、查询全部数据等操作
 */
@Singleton
class DebugDatabaseHelper @Inject constructor(
    private val activityDao: ActivityDao,
    private val activityGroupDao: ActivityGroupDao,
    private val tagDao: TagDao,
    private val behaviorDao: BehaviorDao,
    private val database: NLtimerDatabase,
) {
    // 按外键依赖顺序清除所有表数据
    suspend fun clearAllTables() {
        behaviorDao.deleteAllTagCrossRefs()
        behaviorDao.deleteAllActivityTagBindings()
        behaviorDao.deleteAll()
        activityDao.deleteAll()
        tagDao.deleteAll()
        activityGroupDao.deleteAll()
    }

    // 为所有表批量插入测试数据，建立关联关系
    suspend fun insertAllTestData() {
        val groupIdWork = activityGroupDao.insert(ActivityGroupEntity(name = "工作", sortOrder = 0))
        val groupIdLife = activityGroupDao.insert(ActivityGroupEntity(name = "生活", sortOrder = 1))
        val groupIdSport = activityGroupDao.insert(ActivityGroupEntity(name = "运动", sortOrder = 2))

        val activityIdRun = activityDao.insert(ActivityEntity(name = "跑步", emoji = "🏃", groupId = groupIdSport))
        val activityIdRead = activityDao.insert(ActivityEntity(name = "阅读", emoji = "📖", groupId = groupIdLife))
        val activityIdCode = activityDao.insert(ActivityEntity(name = "编程", emoji = "💻", groupId = groupIdWork))
        val activityIdMeeting = activityDao.insert(ActivityEntity(name = "会议", emoji = "📋", groupId = groupIdWork))
        val activityIdMeditate = activityDao.insert(ActivityEntity(name = "冥想", emoji = "🧘", groupId = groupIdLife))

        val tagIdImportant = tagDao.insert(TagEntity(name = "重要", color = 0xFFFF4444, textColor = 0xFFFFFFFF, category = "优先级", priority = 3))
        val tagIdUrgent = tagDao.insert(TagEntity(name = "紧急", color = 0xFFFF9800, textColor = 0xFFFFFFFF, category = "优先级", priority = 2))
        val tagIdDaily = tagDao.insert(TagEntity(name = "日常", color = 0xFF2196F3, textColor = 0xFFFFFFFF, category = "类型", priority = 1))
        val tagIdFocus = tagDao.insert(TagEntity(name = "专注", color = 0xFF4CAF50, textColor = 0xFFFFFFFF, category = "类型", priority = 1))

        val now = System.currentTimeMillis()
        val behaviorIdRun = behaviorDao.insert(BehaviorEntity(activityId = activityIdRun, startTime = now - 3600000, endTime = now - 1800000, status = "completed", note = "晨跑30分钟"))
        val behaviorIdRead = behaviorDao.insert(BehaviorEntity(activityId = activityIdRead, startTime = now - 7200000, endTime = null, status = "active", note = "正在阅读"))
        val behaviorIdCode = behaviorDao.insert(BehaviorEntity(activityId = activityIdCode, startTime = 0, endTime = null, status = "pending", note = "待开始编程"))

        behaviorDao.insertActivityTagBindings(
            listOf(
                ActivityTagBindingEntity(activityId = activityIdRun, tagId = tagIdDaily),
                ActivityTagBindingEntity(activityId = activityIdCode, tagId = tagIdFocus),
                ActivityTagBindingEntity(activityId = activityIdMeeting, tagId = tagIdUrgent),
            )
        )

        behaviorDao.insertTagCrossRefs(
            listOf(
                BehaviorTagCrossRefEntity(behaviorId = behaviorIdRun, tagId = tagIdDaily),
                BehaviorTagCrossRefEntity(behaviorId = behaviorIdRead, tagId = tagIdFocus),
            )
        )
    }

    // 清除指定表数据
    suspend fun clearTable(tableName: String) {
        when (tableName) {
            "activities" -> activityDao.deleteAll()
            "activity_groups" -> activityGroupDao.deleteAll()
            "tags" -> tagDao.deleteAll()
            "behaviors" -> behaviorDao.deleteAll()
            "activity_tag_binding" -> behaviorDao.deleteAllActivityTagBindings()
            "behavior_tag_cross_ref" -> behaviorDao.deleteAllTagCrossRefs()
        }
    }

    // 为指定表插入测试数据
    suspend fun insertTestData(tableName: String) {
        when (tableName) {
            "activity_groups" -> {
                activityGroupDao.insert(ActivityGroupEntity(name = "工作", sortOrder = 0))
                activityGroupDao.insert(ActivityGroupEntity(name = "生活", sortOrder = 1))
                activityGroupDao.insert(ActivityGroupEntity(name = "运动", sortOrder = 2))
            }
            "activities" -> {
                activityDao.insert(ActivityEntity(name = "跑步", emoji = "🏃"))
                activityDao.insert(ActivityEntity(name = "阅读", emoji = "📖"))
                activityDao.insert(ActivityEntity(name = "编程", emoji = "💻"))
                activityDao.insert(ActivityEntity(name = "会议", emoji = "📋"))
                activityDao.insert(ActivityEntity(name = "冥想", emoji = "🧘"))
            }
            "tags" -> {
                tagDao.insert(TagEntity(name = "重要", color = 0xFFFF4444, textColor = 0xFFFFFFFF, category = "优先级", priority = 3))
                tagDao.insert(TagEntity(name = "紧急", color = 0xFFFF9800, textColor = 0xFFFFFFFF, category = "优先级", priority = 2))
                tagDao.insert(TagEntity(name = "日常", color = 0xFF2196F3, textColor = 0xFFFFFFFF, category = "类型", priority = 1))
                tagDao.insert(TagEntity(name = "专注", color = 0xFF4CAF50, textColor = 0xFFFFFFFF, category = "类型", priority = 1))
            }
            "behaviors" -> {
                val now = System.currentTimeMillis()
                behaviorDao.insert(BehaviorEntity(activityId = 1, startTime = now - 3600000, endTime = now - 1800000, status = "completed", note = "测试行为1"))
                behaviorDao.insert(BehaviorEntity(activityId = 2, startTime = now - 7200000, endTime = null, status = "active", note = "测试行为2"))
                behaviorDao.insert(BehaviorEntity(activityId = 3, startTime = 0, endTime = null, status = "pending", note = "测试行为3"))
            }
            "activity_tag_binding" -> {
                behaviorDao.insertActivityTagBindings(
                    listOf(
                        ActivityTagBindingEntity(activityId = 1, tagId = 1),
                        ActivityTagBindingEntity(activityId = 2, tagId = 2),
                        ActivityTagBindingEntity(activityId = 3, tagId = 3),
                    )
                )
            }
            "behavior_tag_cross_ref" -> {
                behaviorDao.insertTagCrossRefs(
                    listOf(
                        BehaviorTagCrossRefEntity(behaviorId = 1, tagId = 1),
                        BehaviorTagCrossRefEntity(behaviorId = 2, tagId = 2),
                    )
                )
            }
        }
    }

    // 查询所有表数据，返回 表名 → 行列表
    suspend fun queryAllData(): Map<String, List<Map<String, Any?>>> {
        return mapOf(
            "activities" to activityDao.getAll().first().map { entityToMap(it) },
            "activity_groups" to activityGroupDao.getAll().first().map { entityToMap(it) },
            "tags" to tagDao.getAll().first().map { entityToMap(it) },
            "behaviors" to database.behaviorDao().let { dao ->
                // behaviors 没有 getAll 的同步版本，用 Flow.first()
                dao.getByDayRange(0, Long.MAX_VALUE).first().map { entityToMap(it) }
            },
            "activity_tag_binding" to behaviorDao.getAllActivityTagBindingsSync().map { entityToMap(it) },
            "behavior_tag_cross_ref" to behaviorDao.getAllCrossRefsSync().map { entityToMap(it) },
        )
    }

    // 将 Entity 转为 Map（反射读取属性）
    private fun entityToMap(entity: Any): Map<String, Any?> {
        return entity::class.members
            .filterIsInstance<kotlin.reflect.KProperty1<Any, *>>()
            .associate { prop ->
                prop.name to prop.get(entity)
            }
    }
}
```

注意：`entityToMap` 使用 Kotlin 反射，需要在 `feature/debug/build.gradle.kts` 中添加 `implementation(kotlin("reflect"))` 依赖。

- [ ] **步骤 2：修改 feature/debug/build.gradle.kts 添加反射依赖**

在 `dependencies` 块中添加：

```kotlin
    implementation(kotlin("reflect"))
```

- [ ] **步骤 3：编译验证**

运行：`.\gradlew :feature:debug:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add feature/debug/src/main/java/com/nltimer/feature/debug/data/DebugDatabaseHelper.kt feature/debug/build.gradle.kts
git commit -m "feat(debug): 新增 DebugDatabaseHelper 数据库操作辅助类"
```

---

### 任务 6：创建 DatabaseToolsPreview UI 组件

**文件：**
- 创建：`feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/DatabaseToolsPreview.kt`

- [ ] **步骤 1：创建 DatabaseToolsPreview Composable**

```kotlin
package com.nltimer.feature.debug.ui.preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nltimer.feature.debug.data.DebugDatabaseHelper
import kotlinx.coroutines.launch

/**
 * 数据库工具调试预览入口
 * 提供清除/插入/查询数据库数据的调试操作界面，
 * 支持全局操作和按表粒度的单独操作
 */
@Composable
fun DatabaseToolsPreview() {
    val helper = remember { getDebugDatabaseHelper() }
    val scope = rememberCoroutineScope()

    // 各表记录数状态
    var activityCount by remember { mutableStateOf(0) }
    var groupCount by remember { mutableStateOf(0) }
    var tagCount by remember { mutableStateOf(0) }
    var behaviorCount by remember { mutableStateOf(0) }
    var bindingCount by remember { mutableStateOf(0) }
    var crossRefCount by remember { mutableStateOf(0) }

    // 查询结果状态
    var queryResult by remember { mutableStateOf<Map<String, List<Map<String, Any?>>>?>(null) }
    var expandedTables by remember { mutableStateOf(setOf<String>()) }

    // 确认对话框状态
    var confirmAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var confirmMessage by remember { mutableStateOf("") }

    // 操作反馈
    var snackbarMessage by remember { mutableStateOf("") }

    // 刷新各表记录数
    suspend fun refreshCounts() {
        val data = helper.queryAllData()
        activityCount = data["activities"]?.size ?: 0
        groupCount = data["activity_groups"]?.size ?: 0
        tagCount = data["tags"]?.size ?: 0
        behaviorCount = data["behaviors"]?.size ?: 0
        bindingCount = data["activity_tag_binding"]?.size ?: 0
        crossRefCount = data["behavior_tag_cross_ref"]?.size ?: 0
        // 如果已查询过数据，自动刷新查询结果
        if (queryResult != null) {
            queryResult = data
        }
    }

    // 初始加载记录数
    LaunchedEffect(Unit) {
        refreshCounts()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 全局操作区域
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "全局操作",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // 全部清除按钮
                    OutlinedButton(
                        onClick = {
                            confirmMessage = "确定要清除所有数据库数据吗？此操作不可撤销。"
                            confirmAction = {
                                scope.launch {
                                    helper.clearAllTables()
                                    refreshCounts()
                                    snackbarMessage = "已清除所有数据"
                                }
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("全部清除")
                    }
                    // 批量插入全部按钮
                    Button(
                        onClick = {
                            scope.launch {
                                helper.insertAllTestData()
                                refreshCounts()
                                snackbarMessage = "已插入全部测试数据"
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("批量插入全部")
                    }
                }
            }
        }

        // 每表操作卡片
        val tableCards = listOf(
            Triple("activities", "活动", activityCount),
            Triple("activity_groups", "活动分组", groupCount),
            Triple("tags", "标签", tagCount),
            Triple("behaviors", "行为记录", behaviorCount),
            Triple("activity_tag_binding", "活动-标签绑定", bindingCount),
            Triple("behavior_tag_cross_ref", "行为-标签关联", crossRefCount),
        )

        tableCards.forEach { (tableName, displayName, count) ->
            TableOperationCard(
                tableName = tableName,
                displayName = displayName,
                recordCount = count,
                onInsert = {
                    scope.launch {
                        helper.insertTestData(tableName)
                        refreshCounts()
                        snackbarMessage = "已为 $displayName 插入测试数据"
                    }
                },
                onClear = {
                    confirmMessage = "确定要清除 $displayName 的所有数据吗？"
                    confirmAction = {
                        scope.launch {
                            helper.clearTable(tableName)
                            refreshCounts()
                            snackbarMessage = "已清除 $displayName 数据"
                        }
                    }
                },
            )
        }

        // 查询全部数据按钮
        Button(
            onClick = {
                scope.launch {
                    queryResult = helper.queryAllData()
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("查询全部数据")
        }

        // 查询结果展示
        queryResult?.let { data ->
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = "查询结果",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            )

            data.forEach { (tableName, rows) ->
                val displayName = when (tableName) {
                    "activities" -> "活动"
                    "activity_groups" -> "活动分组"
                    "tags" -> "标签"
                    "behaviors" -> "行为记录"
                    "activity_tag_binding" -> "活动-标签绑定"
                    "behavior_tag_cross_ref" -> "行为-标签关联"
                    else -> tableName
                }
                val isExpanded = tableName in expandedTables

                Surface(
                    onClick = {
                        expandedTables = if (isExpanded) expandedTables - tableName
                        else expandedTables + tableName
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // 卡片标题行
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "$displayName (${rows.size}条)",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                            )
                            Text(
                                text = if (isExpanded) "▼" else "▶",
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }

                        // 展开后的详细数据
                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier.padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                if (rows.isEmpty()) {
                                    Text(
                                        text = "（空表）",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                } else {
                                    rows.forEachIndexed { index, row ->
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainer,
                                        ) {
                                            Text(
                                                text = row.entries.joinToString(", ") { (k, v) ->
                                                    "$k=$v"
                                                },
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 11.sp,
                                                ),
                                                modifier = Modifier.padding(6.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Snackbar 反馈
        if (snackbarMessage.isNotEmpty()) {
            LaunchedEffect(snackbarMessage) {
                kotlinx.coroutines.delay(2000)
                snackbarMessage = ""
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.inverseSurface,
            ) {
                Text(
                    text = snackbarMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }

    // 确认对话框
    confirmAction?.let {
        AlertDialog(
            onDismissRequest = { confirmAction = null },
            title = { Text("确认操作") },
            text = { Text(confirmMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        it.invoke()
                        confirmAction = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmAction = null }) {
                    Text("取消")
                }
            },
        )
    }
}

/**
 * 单表操作卡片
 * 展示表名、记录数，提供插入测试数据和清除数据按钮
 *
 * @param tableName 数据库表名
 * @param displayName 中文显示名
 * @param recordCount 当前记录数
 * @param onInsert 插入测试数据回调
 * @param onClear 清除数据回调
 * @param modifier 可选的修饰符
 */
@Composable
private fun TableOperationCard(
    tableName: String,
    displayName: String,
    recordCount: Int,
    onInsert: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 标题行：表名 + 记录数
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                )
                Text(
                    text = "(${recordCount}条)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // 操作按钮行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onInsert,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("插入测试数据")
                }
                OutlinedButton(
                    onClick = onClear,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("清除数据")
                }
            }
        }
    }
}
```

注意：`getDebugDatabaseHelper()` 需要通过 Hilt 获取实例，在 Composable 中使用 `@HiltViewModel` 包装。但由于 DebugComponent 的 content 是纯 Composable lambda，无法直接使用 Hilt 注入。这里采用一个辅助函数，通过 `LocalContext` 获取 Application 再从 Hilt 获取实例。

- [ ] **步骤 2：在 DatabaseToolsPreview.kt 中添加 Hilt 获取辅助函数**

在 `DatabaseToolsPreview.kt` 文件中添加：

```kotlin
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.HiltAndroidApp

@Composable
private fun getDebugDatabaseHelper(): DebugDatabaseHelper {
    val context = LocalContext.current
    val app = context.applicationContext
    return (app as? HiltAndroidAppProvider)?.debugDatabaseHelper
        ?: throw IllegalStateException("Application 必须实现 HiltAndroidAppProvider")
}

interface HiltAndroidAppProvider {
    val debugDatabaseHelper: DebugDatabaseHelper
}
```

这种方式需要 Application 实现接口，耦合较重。更好的方式是直接使用 Hilt 的 `EntryPointAccessors`：

```kotlin
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.EntryPointAccessors

@Composable
private fun getDebugDatabaseHelper(): DebugDatabaseHelper {
    val context = LocalContext.current
    return EntryPointAccessors.fromApplication(
        context.applicationContext,
        DebugDatabaseHelperEntryPoint::class.java
    ).debugDatabaseHelper()
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DebugDatabaseHelperEntryPoint {
    fun debugDatabaseHelper(): DebugDatabaseHelper
}
```

- [ ] **步骤 3：编译验证**

运行：`.\gradlew :feature:debug:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add feature/debug/src/main/java/com/nltimer/feature/debug/ui/preview/DatabaseToolsPreview.kt
git commit -m "feat(debug): 新增 DatabaseToolsPreview UI 组件"
```

---

### 任务 7：注册 DatabaseTools 组件到调试页面

**文件：**
- 修改：`feature/debug/src/main/java/com/nltimer/feature/debug/FeatureDebugComponents.kt`

- [ ] **步骤 1：在 FeatureDebugComponents.registerAll() 末尾添加注册代码**

在 `registerAll()` 方法的最后一个 `DebugComponentRegistry.register(...)` 之后添加：

```kotlin
        DebugComponentRegistry.register(
            DebugComponent(
                id = "DatabaseTools",
                name = "数据库工具",
                group = "Database",
                description = "清除/插入/查询数据库数据",
                implemented = true,
            ) {
                DatabaseToolsPreview()
            }
        )
```

同时在文件顶部添加 import：

```kotlin
import com.nltimer.feature.debug.ui.preview.DatabaseToolsPreview
```

- [ ] **步骤 2：编译验证**

运行：`.\gradlew :feature:debug:compileDebugKotlin`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add feature/debug/src/main/java/com/nltimer/feature/debug/FeatureDebugComponents.kt
git commit -m "feat(debug): 注册 DatabaseTools 组件到调试页面"
```

---

### 任务 8：全量构建验证

- [ ] **步骤 1：Debug 构建验证**

运行：`.\gradlew assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 2：Release 构建验证（确保零残留）**

运行：`.\gradlew assembleRelease`
预期：BUILD SUCCESSFUL

- [ ] **步骤 3：最终确认 — 运行 lint**

运行：`.\gradlew :feature:debug:lintDebug`
预期：无新增 lint 错误

---

## 自检结果

1. **规格覆盖度** ✅ — 设计规格中每个需求都有对应任务：
   - 清除所有数据库数据 → 任务 5 的 `clearAllTables()` + 任务 6 的「全部清除」按钮
   - 批量为所有表插入测试数据 → 任务 5 的 `insertAllTestData()` + 任务 6 的「批量插入全部」按钮
   - 为单个表插入测试数据 → 任务 5 的 `insertTestData()` + 任务 6 的每表「插入测试数据」按钮
   - 为单个表清除数据 → 任务 5 的 `clearTable()` + 任务 6 的每表「清除数据」按钮
   - 查询所有数据库数据 → 任务 5 的 `queryAllData()` + 任务 6 的「查询全部数据」按钮 + 分组卡片展示

2. **占位符扫描** ✅ — 无 TODO/TBD/待定，所有步骤包含完整代码

3. **类型一致性** ✅ — 方法签名在任务 5 定义，任务 6 使用时完全一致；DAO 方法名在任务 1-4 定义，任务 5 引用时一致
