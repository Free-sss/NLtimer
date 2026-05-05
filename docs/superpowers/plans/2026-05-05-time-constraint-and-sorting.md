# 时间约束与排序插入实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 实现完成/当前模式的时间约束（不能选未来时间），以及按时间排序插入行为记录

**架构：** 前端限制时间选择器最大值，后端保存前校验；数据库查询改为按 startTime 排序，插入时计算正确的 sequence 并更新后续记录

**技术栈：** Kotlin, Jetpack Compose, Room, Coroutines

---

## 文件清单

| 文件 | 职责 |
|------|------|
| `core/data/.../entity/BehaviorEntity.kt` | 修改索引：单列索引 → 复合索引 `(startTime, sequence)` |
| `core/data/.../dao/BehaviorDao.kt` | 修改查询排序：`ORDER BY sequence ASC` → `ORDER BY startTime ASC`；新增 `setSequence()` 方法 |
| `core/data/.../repository/BehaviorRepository.kt` | 新增 `setSequence()` 接口方法 |
| `core/data/.../impl/BehaviorRepositoryImpl.kt` | 实现 `setSequence()` |
| `feature/home/.../sheet/AddBehaviorSheet.kt` | 前端时间限制：`TimeAdjustmentComponent` 传入 `maxTime` |
| `feature/home/.../viewmodel/HomeViewModel.kt` | 后端时间校验 + 排序插入逻辑 |
| `feature/home/.../viewmodel/HomeViewModelTest.kt` | 单元测试：时间校验 + 排序插入 |

---

### 任务 1：数据库索引优化

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/database/entity/BehaviorEntity.kt`

- [ ] **步骤 1：修改索引定义**

将单列索引改为复合索引：

```kotlin
@Entity(
    tableName = "behaviors",
    foreignKeys = [
        ForeignKey(
            entity = ActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index("activityId"),
        Index(value = ["startTime", "sequence"]),
        Index("status"),
    ],
)
```

- [ ] **步骤 2：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/database/entity/BehaviorEntity.kt
git commit -m "refactor: 将 startTime 和 sequence 改为复合索引"
```

---

### 任务 2：修改查询排序

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/database/dao/BehaviorDao.kt`

- [ ] **步骤 1：修改 getByDayRange 查询排序**

```kotlin
@Query(
    """
    SELECT * FROM behaviors
    WHERE startTime >= :dayStart AND startTime < :dayEnd
    ORDER BY startTime ASC
    """
)
fun getByDayRange(dayStart: Long, dayEnd: Long): Flow<List<BehaviorEntity>>
```

- [ ] **步骤 2：修改 getHomeBehaviors 查询排序**

```kotlin
@Query(
    """
    SELECT * FROM behaviors
    WHERE (startTime >= :dayStart AND startTime < :dayEnd)
       OR status = 'pending'
    ORDER BY startTime ASC
    """
)
fun getHomeBehaviors(dayStart: Long, dayEnd: Long): Flow<List<BehaviorEntity>>
```

- [ ] **步骤 3：新增 setSequence 方法**

```kotlin
@Query("UPDATE behaviors SET sequence = :sequence WHERE id = :id")
suspend fun setSequence(id: Long, sequence: Int)
```

- [ ] **步骤 4：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/database/dao/BehaviorDao.kt
git commit -m "refactor: 查询排序改为 startTime ASC，新增 setSequence 方法"
```

---

### 任务 3：Repository 层新增 setSequence

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/repository/BehaviorRepository.kt`
- 修改：`core/data/src/main/java/com/nltimer/core/data/repository/impl/BehaviorRepositoryImpl.kt`

- [ ] **步骤 1：在接口中新增方法**

```kotlin
// BehaviorRepository.kt
suspend fun setSequence(id: Long, sequence: Int)
```

- [ ] **步骤 2：在实现类中实现方法**

```kotlin
// BehaviorRepositoryImpl.kt
override suspend fun setSequence(id: Long, sequence: Int) {
    behaviorDao.setSequence(id, sequence)
}
```

- [ ] **步骤 3：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/repository/
git commit -m "feat: repository 层新增 setSequence 方法"
```

---

### 任务 4：前端时间限制

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/AddBehaviorSheet.kt`

- [ ] **步骤 1：修改 TimeAdjustmentComponent 调用，传入 maxTime**

在 `AddBehaviorSheetContent` 中，找到 `TimeAdjustmentComponent` 的调用位置：

对于 `COMPLETED` 模式的结束时间选择器：
```kotlin
TimeAdjustmentComponent(
    currentTime = endTime,
    onTimeChanged = { endTime = it },
    maxTime = if (mode == BehaviorNature.COMPLETED) LocalDateTime.now() else null,
)
```

对于 `ACTIVE` 模式的开始时间选择器：
```kotlin
TimeAdjustmentComponent(
    currentTime = startTime,
    onTimeChanged = { startTime = it },
    maxTime = if (mode == BehaviorNature.ACTIVE) LocalDateTime.now() else null,
)
```

- [ ] **步骤 2：修改 TimeAdjustmentComponent 支持 maxTime**

如果 `TimeAdjustmentComponent` 没有 `maxTime` 参数，需要添加：

```kotlin
@Composable
fun TimeAdjustmentComponent(
    currentTime: LocalDateTime,
    onTimeChanged: (LocalDateTime) -> Unit,
    maxTime: LocalDateTime? = null,
) {
    // ... 现有代码 ...
    
    // 在选择时间时限制最大值
    val effectiveTime = if (maxTime != null && selectedTime > maxTime) {
        maxTime
    } else {
        selectedTime
    }
}
```

- [ ] **步骤 3：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/AddBehaviorSheet.kt
git commit -m "feat: 前端时间选择器限制最大值为当前时间"
```

---

### 任务 5：后端时间校验

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeViewModel.kt`

- [ ] **步骤 1：在 addBehavior 方法中添加时间校验**

在现有的冲突检测之后、插入之前添加时间校验：

```kotlin
fun addBehavior(
    activityId: Long,
    tagIds: List<Long>,
    startTime: Long,
    endTime: Long?,
    status: BehaviorNature,
    note: String?,
) {
    viewModelScope.launch {
        // 现有冲突检测代码...
        
        // 新增：时间约束校验
        val now = System.currentTimeMillis()
        when (status) {
            BehaviorNature.COMPLETED -> {
                if (endTime != null && endTime > now) {
                    _uiState.update {
                        it.copy(errorMessage = "结束时间不能大于当前时间")
                    }
                    return@launch
                }
            }
            BehaviorNature.ACTIVE -> {
                if (startTime > now) {
                    _uiState.update {
                        it.copy(errorMessage = "开始时间不能大于当前时间")
                    }
                    return@launch
                }
            }
            BehaviorNature.PENDING -> {} // 无时间约束
        }
        
        // 现有插入代码...
    }
}
```

- [ ] **步骤 2：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeViewModel.kt
git commit -m "feat: 后端保存前校验时间不能超过当前时间"
```

---

### 任务 6：排序插入逻辑

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeViewModel.kt`

- [ ] **步骤 1：修改 addBehavior 中的 sequence 计算逻辑**

替换原有的 `val maxSeq = behaviorRepository.getMaxSequence()` 逻辑：

```kotlin
// 计算新行为的 sequence（按时间排序插入）
val newSequence = if (status == BehaviorNature.PENDING) {
    // PENDING 行为使用原来的逻辑
    behaviorRepository.getMaxSequence() + 1
} else {
    // 非 PENDING 行为按 startTime 排序插入
    val dayStart = getDayStartMillis(startTime)
    val dayEnd = dayStart + 24 * 60 * 60 * 1000
    
    val dayBehaviors = behaviorRepository
        .getByDayRange(dayStart, dayEnd)
        .firstOrNull()
        .orEmpty()
        .filter { it.status != BehaviorNature.PENDING.name }
        .sortedBy { it.startTime }
    
    val insertIndex = dayBehaviors.indexOfFirst { it.startTime > startTime }
    if (insertIndex == -1) {
        dayBehaviors.size
    } else {
        insertIndex
    }
}

// 更新后续行为的 sequence
if (status != BehaviorNature.PENDING) {
    val dayStart = getDayStartMillis(startTime)
    val dayEnd = dayStart + 24 * 60 * 60 * 1000
    
    val dayBehaviors = behaviorRepository
        .getByDayRange(dayStart, dayEnd)
        .firstOrNull()
        .orEmpty()
        .filter { it.status != BehaviorNature.PENDING.name }
        .sortedBy { it.startTime }
    
    dayBehaviors.forEachIndexed { index, behavior ->
        if (index >= newSequence) {
            behaviorRepository.setSequence(behavior.id, index + 1)
        }
    }
}
```

- [ ] **步骤 2：添加 getDayStartMillis 辅助函数**

```kotlin
private fun getDayStartMillis(timestamp: Long): Long {
    val instant = java.time.Instant.ofEpochMilli(timestamp)
    val zonedDateTime = instant.atZone(java.time.ZoneId.systemDefault())
    val startOfDay = zonedDateTime.toLocalDate().atStartOfDay(java.time.ZoneId.systemDefault())
    return startOfDay.toInstant().toEpochMilli()
}
```

- [ ] **步骤 3：修改 insert 调用，使用新的 sequence**

```kotlin
behaviorRepository.insert(
    Behavior(
        id = 0,
        activityId = activityId,
        startTime = if (status == BehaviorNature.PENDING) 0L else startTime,
        endTime = if (status == BehaviorNature.COMPLETED) endTime ?: startTime else null,
        status = status,
        note = note,
        pomodoroCount = 0,
        sequence = newSequence,
        estimatedDuration = null,
        actualDuration = null,
        achievementLevel = null,
        wasPlanned = wasPlanned,
    ),
    tagIds = tagIds,
)
```

- [ ] **步骤 4：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeViewModel.kt
git commit -m "feat: 按时间排序插入行为记录"
```

---

### 任务 7：单元测试

**文件：**
- 修改：`feature/home/src/test/java/com/nltimer/feature/home/viewmodel/HomeViewModelTest.kt`

- [ ] **步骤 1：编写时间校验测试**

```kotlin
@Test
fun `addBehavior with COMPLETED endTime in future should show error`() = runTest {
    // Given
    val futureTime = System.currentTimeMillis() + 3600_000 // 1小时后
    val startTime = System.currentTimeMillis() - 7200_000 // 2小时前
    
    // When
    viewModel.addBehavior(
        activityId = 1L,
        tagIds = emptyList(),
        startTime = startTime,
        endTime = futureTime,
        status = BehaviorNature.COMPLETED,
        note = null,
    )
    
    // Then
    val uiState = viewModel.uiState.value
    assertEquals("结束时间不能大于当前时间", uiState.errorMessage)
}

@Test
fun `addBehavior with ACTIVE startTime in future should show error`() = runTest {
    // Given
    val futureTime = System.currentTimeMillis() + 3600_000 // 1小时后
    
    // When
    viewModel.addBehavior(
        activityId = 1L,
        tagIds = emptyList(),
        startTime = futureTime,
        endTime = null,
        status = BehaviorNature.ACTIVE,
        note = null,
    )
    
    // Then
    val uiState = viewModel.uiState.value
    assertEquals("开始时间不能大于当前时间", uiState.errorMessage)
}
```

- [ ] **步骤 2：编写排序插入测试**

```kotlin
@Test
fun `addBehavior should insert at correct sequence based on startTime`() = runTest {
    // Given: 已有 08:00-09:00 的记录
    val existingBehavior = BehaviorEntity(
        id = 1L,
        activityId = 1L,
        startTime = getTodayAt(8, 0),
        endTime = getTodayAt(9, 0),
        status = "completed",
        sequence = 0,
    )
    // ... 设置 mock repository 返回已有记录
    
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
    
    // Then: 新行为的 sequence 应该是 0（排在前面）
    verify { mockBehaviorRepository.insert(
        match { it.sequence == 0 },
        any()
    ) }
    // 原有行为的 sequence 应该更新为 1
    verify { mockBehaviorRepository.setSequence(1L, 1) }
}

private fun getTodayAt(hour: Int, minute: Int): Long {
    return java.time.LocalDate.now()
        .atTime(hour, minute)
        .atZone(java.time.ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}
```

- [ ] **步骤 3：运行测试**

```bash
./gradlew :feature:home:testDebugUnitTest --tests "*HomeViewModelTest*"
```

预期：所有测试通过

- [ ] **步骤 4：Commit**

```bash
git add feature/home/src/test/java/com/nltimer/feature/home/viewmodel/HomeViewModelTest.kt
git commit -m "test: 添加时间校验和排序插入的单元测试"
```

---

### 任务 8：完整构建验证

- [ ] **步骤 1：运行完整构建**

```bash
./gradlew :app:assembleDebug
```

预期：BUILD SUCCESSFUL

- [ ] **步骤 2：运行所有单元测试**

```bash
./gradlew testDebugUnitTest
```

预期：所有测试通过

---

## 自检

**规格覆盖度：**
- [x] 时间约束（完成模式结束时间、当前模式开始时间）
- [x] 前端时间选择器限制
- [x] 后端保存前校验
- [x] 数据库复合索引
- [x] 查询排序改为 startTime
- [x] 按时间排序插入
- [x] sequence 更新逻辑

**占位符扫描：** 无 TODO、无待定、无未定义类型

**类型一致性：** `BehaviorNature`、`LocalDateTime`、`LocalDate` 使用一致
