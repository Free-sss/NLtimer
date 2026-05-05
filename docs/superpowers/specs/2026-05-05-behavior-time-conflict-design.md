# 行为记录时间冲突检测 设计文档

## 目标
添加行为记录时，检测并阻止与已有记录的时间交叉，确保行为记录之间时间区间互不重叠。

## 架构
采用**前端实时提示 + ViewModel 保存前最终校验**的双重检测机制。冲突检测算法抽成纯函数，前后端复用。ACTIVE 行为视为未闭合区间 `[startTime, +∞)`，即只要存在进行中的行为，其后任何新行为均冲突。

## 技术栈
Kotlin, Jetpack Compose, Room, Coroutines/Flow

---

## 1. 冲突定义

### 1.1 时间区间语义

行为时间区间使用**半开区间** `[startTime, endTime)`：

- `startTime` 包含在区间内
- `endTime` 不包含在区间内
- 两个行为 `A: [10:00, 11:00)` 和 `B: [11:00, 12:00)` **不冲突**（边界相接允许）

### 1.2 各状态行为的时间区间

| 状态 | startTime | endTime | 有效区间 |
|------|-----------|---------|----------|
| COMPLETED | 有 | 有 | `[startTime, endTime)` |
| ACTIVE | 有 | null | `[startTime, +∞)` —— 视为无限区间，因为"正在进行中"的未来时间不确定 |
| PENDING | 0/null | null | 不参与冲突检测 |

### 1.3 冲突条件

两个行为存在冲突，当且仅当：

```
startA < endB && startB < endA
```

其中 `endTime` 根据状态取实际值或 `Long.MAX_VALUE`。

### 1.4 跨天行为

跨天行为按其实际时间区间参与检测。例如：
- 行为 A: 昨日 23:50 - 今日 00:30
- 行为 B: 今日 00:10 - 今日 01:00

A 的有效区间 `[昨日23:50, 今日00:30)`，B 的有效区间 `[今日00:10, 今日01:00)`，两者重叠（00:10-00:30），**冲突**。

---

## 2. 冲突检测算法

### 2.1 纯函数签名

```kotlin
/**
 * 检测新行为与已有行为列表是否存在时间冲突
 *
 * @param newStart 新行为开始时间（epoch millis）
 * @param newEnd 新行为结束时间，null 表示未结束
 * @param newStatus 新行为状态
 * @param existingBehaviors 已有行为列表
 * @param currentTime 当前时间，用于计算 ACTIVE 行为的结束时间
 * @param ignoreBehaviorId 需要忽略的行为 ID（编辑场景使用，新增时传 null）
 * @return true 表示存在冲突
 */
fun hasTimeConflict(
    newStart: Long,
    newEnd: Long?,
    newStatus: BehaviorNature,
    existingBehaviors: List<Behavior>,
    currentTime: Long = System.currentTimeMillis(),
    ignoreBehaviorId: Long? = null,
): Boolean
```

### 2.2 算法实现

```kotlin
fun hasTimeConflict(
    newStart: Long,
    newEnd: Long?,
    newStatus: BehaviorNature,
    existingBehaviors: List<Behavior>,
    currentTime: Long = System.currentTimeMillis(),
    ignoreBehaviorId: Long? = null,
): Boolean {
    // PENDING 行为不参与冲突检测
    if (newStatus == BehaviorNature.PENDING) return false
    
    // 开始时间非法
    if (newStart <= 0L) return false
    
    // 计算新行为的有效结束时间
    val effectiveNewEnd = when (newStatus) {
        BehaviorNature.ACTIVE -> Long.MAX_VALUE
        BehaviorNature.COMPLETED -> newEnd ?: return false
        BehaviorNature.PENDING -> return false
    }
    
    // 自身时间区间非法（结束不晚于开始）
    if (effectiveNewEnd <= newStart) return false
    
    return existingBehaviors.any { existing ->
        // 忽略指定行为（编辑自身）
        if (ignoreBehaviorId != null && existing.id == ignoreBehaviorId) return@any false
        
        // PENDING 行为不参与冲突检测
        if (existing.status == BehaviorNature.PENDING) return@any false
        
        // 已有行为开始时间非法
        if (existing.startTime <= 0L) return@any false
        
        // 计算已有行为的有效结束时间
        val existingEnd = when {
            existing.endTime != null -> existing.endTime
            existing.status == BehaviorNature.ACTIVE -> Long.MAX_VALUE
            else -> return@any false
        }
        
        // 已有行为自身时间区间非法
        if (existingEnd <= existing.startTime) return@any false
        
        // 半开区间重叠检测
        newStart < existingEnd && existing.startTime < effectiveNewEnd
    }
}
```

---

## 3. 前端实时检测

### 3.1 数据来源

`HomeScreen` 将 `uiState.rows` 中的行为数据（已包含当天所有行为的 startTime/endTime/status）传入弹窗组件：

```kotlin
AddBehaviorSheet(
    // ... 其他参数
    existingBehaviors = uiState.rows.mapNotNull { row ->
        // 从 RowData 中提取 Behavior 信息
        row.behavior
    },
)
```

**注意**：`uiState.rows` 可能不包含跨天行为的前半段（如果 startTime 不在当天）。前端检测作为**最佳-effort 提示**，最终可靠性以后端为准。

### 3.2 实时检测触发时机

在 `AddBehaviorSheetContent` 中，每次 `startTime` 或 `endTime` 变化时触发检测：

```kotlin
val hasTimeConflict by remember(startTime, endTime, mode) {
    derivedStateOf {
        if (mode == BehaviorNature.PENDING) return@derivedStateOf false
        val now = System.currentTimeMillis()
        hasTimeConflict(
            newStart = startTime.toEpochMillis(),
            newEnd = if (mode == BehaviorNature.COMPLETED) endTime.toEpochMillis() else null,
            newStatus = mode,
            existingBehaviors = existingBehaviors,
            currentTime = now,
        )
    }
}
```

### 3.3 UI 反馈

#### 冲突状态
- 确认按钮：`enabled = !hasTimeConflict && selectedActivityId != null`
- 按钮下方显示红色提示文字：`"该时间段与已有记录冲突"`
- 时间选择器区域边框变红（`MaterialTheme.colorScheme.error`）

#### 时间非法状态（结束时间不晚于开始时间）
- 确认按钮禁用
- 显示提示：`"结束时间必须晚于开始时间"`

两种错误状态同时存在时，优先显示时间非法提示（因为冲突检测在非法时间下无意义）。

---

## 4. 后端最终校验

### 4.1 查询逻辑

Repository 增加方法，查询与目标时间区间有交叉的所有行为（而非仅 startTime 落在当天的行为）：

```kotlin
// BehaviorDao.kt
/**
 * 查询与指定时间区间有交叉的行为记录
 * 条件：existing.startTime < rangeEnd AND effectiveExistingEnd > rangeStart
 */
@Query(
    """
    SELECT * FROM behaviors
    WHERE startTime < :rangeEnd
      AND (
          endTime IS NULL
          OR endTime > :rangeStart
      )
      AND status != 'pending'
      AND startTime > 0
    """
)
fun getBehaviorsOverlappingRange(
    rangeStart: Long,
    rangeEnd: Long,
): Flow<List<BehaviorEntity>>
```

**说明**：
- `endTime IS NULL` 表示 ACTIVE 行为，视为与任何后续区间交叉
- `status != 'pending'` 排除待办行为
- `startTime > 0` 排除非法开始时间

### 4.2 ViewModel 校验逻辑

在 `HomeViewModel.addBehavior()` 中，保存前执行最终校验：

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
        // ... 原有逻辑：结束当前 ACTIVE 行为 ...
        
        if (status != BehaviorNature.PENDING) {
            val now = System.currentTimeMillis()
            val effectiveNewEnd = when (status) {
                BehaviorNature.ACTIVE -> Long.MAX_VALUE
                BehaviorNature.COMPLETED -> endTime ?: startTime
                BehaviorNature.PENDING -> null
            }
            
            if (effectiveNewEnd != null && effectiveNewEnd > startTime) {
                // 查询与目标区间交叉的所有行为
                val overlappingBehaviors = behaviorRepository
                    .getBehaviorsOverlappingRange(startTime, effectiveNewEnd)
                    .firstOrNull()
                    .orEmpty()
                
                if (hasTimeConflict(
                        newStart = startTime,
                        newEnd = endTime,
                        newStatus = status,
                        existingBehaviors = overlappingBehaviors,
                        currentTime = now,
                    )
                ) {
                    _uiState.update { 
                        it.copy(errorMessage = "该时间段与已有行为记录冲突") 
                    }
                    return@launch
                }
            }
        }
        
        // ... 正常保存逻辑 ...
    }
}
```

### 4.3 错误展示

`HomeUiState` 增加 `errorMessage` 字段：

```kotlin
data class HomeUiState(
    // ... 现有字段
    val errorMessage: String? = null,
)
```

`HomeScreen` 中使用 `LaunchedEffect` 监听并显示 Snackbar：

```kotlin
val snackbarHostState = remember { SnackbarHostState() }

LaunchedEffect(uiState.errorMessage) {
    uiState.errorMessage?.let { message ->
        snackbarHostState.showSnackbar(message)
        viewModel.clearErrorMessage()
    }
}
```

---

## 5. 文件修改清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `core/data/.../TimeConflictUtils.kt` | 新建 | 冲突检测纯函数 |
| `core/data/.../dao/BehaviorDao.kt` | 修改 | 增加 `getBehaviorsOverlappingRange` 查询 |
| `core/data/.../repository/BehaviorRepository.kt` | 修改 | 增加 `getBehaviorsOverlappingRange` 接口方法 |
| `core/data/.../repository/impl/BehaviorRepositoryImpl.kt` | 修改 | 实现新方法 |
| `feature/home/.../model/HomeUiState.kt` | 修改 | 增加 `errorMessage` 字段 |
| `feature/home/.../ui/sheet/AddBehaviorSheet.kt` | 修改 | 接收 `existingBehaviors`，实时检测，UI 反馈 |
| `feature/home/.../ui/HomeScreen.kt` | 修改 | 传入 `existingBehaviors`，显示 Snackbar |
| `feature/home/.../ui/HomeRoute.kt` | 修改 | 如有需要传递新参数 |
| `feature/home/.../viewmodel/HomeViewModel.kt` | 修改 | 保存前最终校验，错误状态管理 |

---

## 6. 边界情况处理

| 场景 | 处理方式 |
|------|----------|
| 两个 COMPLETED 行为边界相接（如 10:00-11:00 和 11:00-12:00） | 不冲突，允许 |
| ACTIVE 行为与后续 COMPLETED 行为 | 冲突，因为 ACTIVE 视为 `[start, +∞)` |
| 跨天行为（昨日 23:50 - 今日 00:30）与今日行为 | 按实际时间区间检测，重叠则冲突 |
| PENDING 行为与任何行为 | PENDING 不参与冲突检测 |
| 编辑已有行为 | 预留 `ignoreBehaviorId` 参数，排除自身 |
| 零长度行为（start == end） | 视为非法，不允许保存 |
| 并发添加两个冲突行为 | 后端最终校验阻止第二个 |

---

## 7. 测试要点

1. **不冲突场景**：边界相接、不相交区间、PENDING 行为
2. **冲突场景**：重叠区间、ACTIVE 与后续行为、跨天重叠
3. **非法时间**：零长度、结束早于开始
4. **编辑场景**：修改自身时间不与自己冲突
5. **并发场景**：快速连续添加两个冲突行为，第二个被阻止
