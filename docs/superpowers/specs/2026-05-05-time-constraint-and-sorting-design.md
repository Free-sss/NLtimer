# 时间约束与排序插入设计文档

## 背景

当前系统存在两个问题：
1. **完成模式结束时间可以超过现在**：用户补录完成行为时，结束时间可以选到未来，不符合业务逻辑
2. **插入行为不按时间排序**：已有 08-09 点记录，插入 06-07 点记录后被放在最后，时间轴顺序错乱

## 需求

### 需求 1：时间约束
- **完成模式（COMPLETED）**：结束时间不能大于当前系统时间
- **当前模式（ACTIVE）**：开始时间不能大于当前系统时间
- 前端实时限制选择器范围，后端最终校验兜底

### 需求 2：按时间排序插入
- 新行为插入时，根据 `startTime` 计算正确的 `sequence`
- 保证时间轴上行为按开始时间先后顺序展示
- 更新后续行为的 `sequence` 值

## 设计

### 1. 数据库索引优化

将 `BehaviorEntity` 的索引从单列索引改为复合索引：

```kotlin
indices = [
    Index("activityId"),
    Index(value = ["startTime", "sequence"]),
    Index("status"),
]
```

**理由**：
- `(startTime, sequence)` 复合索引支持按时间范围查询和排序
- 覆盖 `getByDayRange`、`getHomeBehaviors`、`getBehaviorsOverlappingRange` 等查询
- `status` 单列索引保留，用于状态相关查询

### 2. 查询排序变更

修改 `BehaviorDao` 中的查询，按 `startTime` 排序：

**`getByDayRange`**：
```sql
SELECT * FROM behaviors
WHERE startTime >= :dayStart AND startTime < :dayEnd
ORDER BY startTime ASC
```

**`getHomeBehaviors`**：
```sql
SELECT * FROM behaviors
WHERE (startTime >= :dayStart AND startTime < :dayEnd)
   OR status = 'pending'
ORDER BY startTime ASC
```

### 3. 时间约束实现

#### 前端限制
- `AddBehaviorSheet` 的 `TimeAdjustmentComponent` 接收 `maxTime` 参数
- `COMPLETED` 模式：`endTime` 选择器最大值 = `LocalTime.now()`
- `ACTIVE` 模式：`startTime` 选择器最大值 = `LocalTime.now()`

#### 后端校验
`HomeViewModel.addBehavior()` 保存前校验：
```kotlin
val now = System.currentTimeMillis()
when (status) {
    BehaviorNature.COMPLETED -> {
        if (endTime != null && endTime > now) {
            _uiState.update { it.copy(errorMessage = "结束时间不能大于当前时间") }
            return@launch
        }
    }
    BehaviorNature.ACTIVE -> {
        if (startTime > now) {
            _uiState.update { it.copy(errorMessage = "开始时间不能大于当前时间") }
            return@launch
        }
    }
    BehaviorNature.PENDING -> {} // 无时间约束
}
```

### 4. 排序插入实现

#### 插入逻辑
```kotlin
// 查询同一天所有非 PENDING 行为，按 startTime 排序
val dayBehaviors = behaviorRepository
    .getByDayRange(dayStart, dayEnd)
    .firstOrNull()
    .orEmpty()
    .filter { it.status != BehaviorNature.PENDING }
    .sortedBy { it.startTime }

// 找到插入位置
val insertIndex = dayBehaviors.indexOfFirst { it.startTime > startTime }
val newSequence = if (insertIndex == -1) {
    dayBehaviors.size // 放在最后
} else {
    insertIndex // 插入到该位置
}

// 更新后续行为的 sequence
val behaviorsToUpdate = dayBehaviors.filterIndexed { index, _ ->
    index >= newSequence
}
behaviorsToUpdate.forEach { behavior ->
    behaviorRepository.setSequence(behavior.id, behavior.sequence + 1)
}

// 插入新行为
behaviorRepository.insert(
    Behavior(
        // ...
        sequence = newSequence,
        // ...
    )
)
```

#### sequence 语义
- `sequence` 表示行为在时间轴上的物理顺序索引
- 值越小，表示在时间轴上越靠前
- PENDING 行为的 `startTime = 0`，在 `ORDER BY startTime ASC` 中排在最前面

## 边界情况

1. **跨天行为**：`startTime` 和 `endTime` 可能不在同一天，排序以 `startTime` 为准
2. **PENDING 行为**：`startTime = 0`，按 `startTime` 排序会排在最前面，符合预期（目标队列在上方）
3. **并发插入**：数据库事务保证一致性，但可能出现 sequence 间隙（可接受）
4. **删除行为**：删除后 sequence 可能出现间隙，不影响展示（因为查询按 `startTime` 排序）

## 测试要点

1. 完成模式结束时间选未来时间，前端阻止、后端报错
2. 当前模式开始时间选未来时间，前端阻止、后端报错
3. 插入 06-07 到已有 08-09 的场景，06-07 排在前面
4. 插入 10-11 到已有 08-09 的场景，10-11 排在后面
5. 插入 09:00-09:30 到已有 07-08 和 10-11 的场景，排在中间（07-08 之后，10-11 之前）
6. PENDING 行为始终排在最前面
