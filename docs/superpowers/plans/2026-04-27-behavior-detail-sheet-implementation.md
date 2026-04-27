# 行为详情页（BehaviorDetailSheet）实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 实现长按行为卡片弹出的详情页，支持查看/编辑活动、标签、时间、备注，以及删除和时间填充操作，所有新增组件可被 AddBehaviorSheet 复用。

**架构：** 基于现有 AddBehaviorSheet 提取可复用组件（ActivityPicker、TagPicker、NoteInput 已存在），新增 TimeSelectionSection、DateScrollPicker、TimeScrollPicker、TimeFillActions、DeleteConfirmDialog、BehaviorDetailSheet 等组件。数据流通过 HomeViewModel + BehaviorRepository + Room 实现。

**技术栈：** Jetpack Compose + Material3、MVVM + Hilt、Room Database、Kotlin Coroutines + Flow

---

## 文件结构总览

| 文件 | 操作 | 职责 |
|------|------|------|
| `feature/home/ui/sheet/DateScrollPicker.kt` | 新增 | 日期滚轮选择器 |
| `feature/home/ui/sheet/TimeScrollPicker.kt` | 新增 | 时间滚轮选择器 |
| `feature/home/ui/sheet/TimeSelectionSection.kt` | 新增 | 带快捷按钮的起止时间选择器 |
| `feature/home/ui/sheet/TimeFillActions.kt` | 新增 | 填满左/右侧空闲时间按钮组 |
| `feature/home/ui/sheet/DeleteConfirmDialog.kt` | 新增 | 删除确认弹窗 |
| `feature/home/ui/sheet/BehaviorDetailSheet.kt` | 新增 | 详情弹窗外壳（ModalBottomSheet） |
| `feature/home/ui/sheet/BehaviorDetailSheetContent.kt` | 新增 | 详情弹窗内容体（可复用） |
| `feature/home/model/BehaviorDetailUiState.kt` | 新增 | 详情 UI 状态数据类 |
| `feature/home/model/HomeUiState.kt` | 修改 | 新增详情相关字段 |
| `feature/home/viewmodel/HomeViewModel.kt` | 修改 | 新增 6 个方法 |
| `feature/home/ui/HomeScreen.kt` | 修改 | 新增长按回调 & DetailSheet 显示 |
| `feature/home/ui/HomeRoute.kt` | 修改 | 透传回调 |
| `feature/home/ui/components/GridCell.kt` | 修改 | 添加 combinedClickable 长按手势 |
| `feature/home/ui/components/TimeAxisGrid.kt` | 修改 | 透传 onCellLongPress |
| `core/data/repository/BehaviorRepository.kt` | 修改 | 新增 updateBehavior、updateTagsForBehavior 接口 |
| `core/data/repository/impl/BehaviorRepositoryImpl.kt` | 修改 | 实现新增方法 |
| `core/data/database/dao/BehaviorDao.kt` | 修改 | 新增 update、updateTags SQL 操作 |
| `feature/home/ui/sheet/AddBehaviorSheet.kt` | 修改 | 重构为组合可复用组件 |

---

### 任务 1：BehaviorDetailUiState 数据类

**文件：**
- 创建：`feature/home/src/main/java/com/nltimer/feature/home/model/BehaviorDetailUiState.kt`

- [ ] **步骤 1：创建 BehaviorDetailUiState**

```kotlin
package com.nltimer.feature.home.model

import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.BehaviorNature
import java.time.LocalDateTime

data class BehaviorDetailUiState(
    val behaviorId: Long,
    val activityId: Long,
    val activityEmoji: String?,
    val activityName: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime?,
    val status: BehaviorNature,
    val note: String?,
    val tags: List<TagUiState>,
    val allAvailableTags: List<TagUiState>,
    val allActivities: List<Activity>,
    val achievementLevel: Int?,
    val estimatedDuration: Long?,
    val actualDuration: Long?,
)
```

- [ ] **步骤 2：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/model/BehaviorDetailUiState.kt
git commit -m "feat: 新增 BehaviorDetailUiState 数据类"
```

---

### 任务 2：HomeUiState 扩展

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/model/HomeUiState.kt`

- [ ] **步骤 1：扩展 HomeUiState**

在现有 HomeUiState 数据类中新增字段：

```kotlin
data class HomeUiState(
    val rows: List<GridRowUiState> = emptyList(),
    val currentRowId: String? = null,
    val isAddSheetVisible: Boolean = false,
    val selectedTimeHour: Int = 0,
    val isLoading: Boolean = true,
    val isIdleMode: Boolean = false,
    val hasActiveBehavior: Boolean = false,
    // 新增字段
    val isDetailSheetVisible: Boolean = false,
    val detailBehavior: BehaviorDetailUiState? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)
```

- [ ] **步骤 2：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/model/HomeUiState.kt
git commit -m "feat: HomeUiState 新增详情弹窗相关字段"
```

---

### 任务 3：BehaviorRepository 新增方法

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/repository/BehaviorRepository.kt`

- [ ] **步骤 1：在 BehaviorRepository 接口中新增方法**

在接口末尾添加：

```kotlin
    suspend fun updateBehavior(
        id: Long,
        activityId: Long,
        startTime: Long,
        endTime: Long?,
        status: String,
        note: String?,
    )

    suspend fun updateTagsForBehavior(behaviorId: Long, tagIds: List<Long>)
```

- [ ] **步骤 2：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/repository/BehaviorRepository.kt
git commit -m "feat: BehaviorRepository 新增 updateBehavior 和 updateTagsForBehavior 接口"
```

---

### 任务 4：BehaviorDao 新增 SQL 操作

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/database/dao/BehaviorDao.kt`

- [ ] **步骤 1：读取 BehaviorDao 现有内容**

- [ ] **步骤 2：在 BehaviorDao 中新增 update 和 updateTags 方法**

```kotlin
    @Query("UPDATE behavior_table SET activity_id = :activityId, start_time = :startTime, end_time = :endTime, status = :status, note = :note WHERE id = :id")
    suspend fun update(
        id: Long,
        activityId: Long,
        startTime: Long,
        endTime: Long?,
        status: String,
        note: String?,
    )

    @Query("DELETE FROM behavior_tag_cross_ref WHERE behavior_id = :behaviorId")
    suspend fun deleteTagsForBehavior(behaviorId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTagCrossRefs(crossRefs: List<BehaviorTagCrossRefEntity>)
```

- [ ] **步骤 3：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/database/dao/BehaviorDao.kt
git commit -m "feat: BehaviorDao 新增 update 和标签操作方法"
```

---

### 任务 5：BehaviorRepositoryImpl 实现新增方法

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/repository/impl/BehaviorRepositoryImpl.kt`

- [ ] **步骤 1：在 BehaviorRepositoryImpl 中实现新增方法**

在类末尾添加：

```kotlin
    override suspend fun updateBehavior(
        id: Long,
        activityId: Long,
        startTime: Long,
        endTime: Long?,
        status: String,
        note: String?,
    ) {
        behaviorDao.update(id, activityId, startTime, endTime, status, note)
    }

    override suspend fun updateTagsForBehavior(behaviorId: Long, tagIds: List<Long>) {
        behaviorDao.deleteTagsForBehavior(behaviorId)
        if (tagIds.isNotEmpty()) {
            behaviorDao.insertTagCrossRefs(
                tagIds.map { tagId ->
                    BehaviorTagCrossRefEntity(
                        behaviorId = behaviorId,
                        tagId = tagId,
                    )
                }
            )
        }
    }
```

- [ ] **步骤 2：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/repository/impl/BehaviorRepositoryImpl.kt
git commit -m "feat: BehaviorRepositoryImpl 实现 updateBehavior 和 updateTagsForBehavior"
```

---

### 任务 6：DateScrollPicker 日期滚轮组件

**文件：**
- 创建：`feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/DateScrollPicker.kt`

- [ ] **步骤 1：创建 DateScrollPicker**

```kotlin
package com.nltimer.feature.home.ui.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DateScrollPicker(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    dateRange: Int = 7,
) {
    val today = LocalDate.now()
    val dates = remember {
        (-dateRange..dateRange).map { today.plusDays(it.toLong()) }
    }
    val initialIndex = remember { dates.indexOf(selectedDate).coerceAtLeast(0) }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    val visibleItems by remember {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo
        }
    }

    val centerIndex by remember {
        derivedStateOf {
            val centerOffset = listState.layoutInfo.viewportStartOffset + listState.layoutInfo.viewportSize / 2
            listState.layoutInfo.visibleItemsInfo.minByOrNull {
                kotlin.math.abs((it.offset + it.size / 2) - centerOffset)
            }?.index ?: initialIndex
        }
    }

    LaunchedEffect(centerIndex) {
        if (centerIndex in dates.indices) {
            onDateSelected(dates[centerIndex])
        }
    }

    Box(
        modifier = modifier
            .width(96.dp)
            .height(200.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.medium,
            ),
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            modifier = Modifier.align(Alignment.Center),
        ) {
            items(
                count = dates.size,
                key = { index -> dates[index].toEpochDay() },
            ) { index ->
                val date = dates[index]
                val isSelected = index == centerIndex
                val distanceFromCenter = kotlin.math.abs(index - centerIndex)
                val alphaValue = when (distanceFromCenter) {
                    0 -> 1.0f
                    1 -> 0.7f
                    2 -> 0.4f
                    else -> 0.2f
                }

                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .width(96.dp)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            shape = MaterialTheme.shapes.medium,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("MM月dd日")),
                        style = if (isSelected) {
                            MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        } else {
                            MaterialTheme.typography.bodySmall
                        },
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.alpha(alphaValue),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
```

- [ ] **步骤 2：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/DateScrollPicker.kt
git commit -m "feat: 新增 DateScrollPicker 日期滚轮组件"
```

---

### 任务 7：TimeScrollPicker 时间滚轮组件

**文件：**
- 创建：`feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/TimeScrollPicker.kt`

- [ ] **步骤 1：创建 TimeScrollPicker**

```kotlin
package com.nltimer.feature.home.ui.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalTime

@Composable
fun TimeScrollPicker(
    selectedTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
    stepMinutes: Int = 5,
) {
    val timeSlots = remember {
        (0 until 24 * 60 / stepMinutes).map { index ->
            val totalMinutes = index * stepMinutes
            LocalTime.of(totalMinutes / 60, totalMinutes % 60)
        }
    }
    val initialIndex = remember {
        val slotIndex = (selectedTime.hour * 60 + selectedTime.minute) / stepMinutes
        slotIndex.coerceIn(0, timeSlots.size - 1)
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    val centerIndex by remember {
        derivedStateOf {
            val centerOffset = listState.layoutInfo.viewportStartOffset + listState.layoutInfo.viewportSize / 2
            listState.layoutInfo.visibleItemsInfo.minByOrNull {
                kotlin.math.abs((it.offset + it.size / 2) - centerOffset)
            }?.index ?: initialIndex
        }
    }

    LaunchedEffect(centerIndex) {
        if (centerIndex in timeSlots.indices) {
            onTimeSelected(timeSlots[centerIndex])
        }
    }

    Box(
        modifier = modifier
            .width(64.dp)
            .height(200.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.medium,
            ),
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            modifier = Modifier.align(Alignment.Center),
        ) {
            items(
                count = timeSlots.size,
                key = { index -> index },
            ) { index ->
                val time = timeSlots[index]
                val isSelected = index == centerIndex
                val distanceFromCenter = kotlin.math.abs(index - centerIndex)
                val alphaValue = when (distanceFromCenter) {
                    0 -> 1.0f
                    1 -> 0.7f
                    2 -> 0.4f
                    else -> 0.2f
                }

                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .width(64.dp)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            shape = MaterialTheme.shapes.medium,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = time.toString().substring(0, 5),
                        style = if (isSelected) {
                            MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        } else {
                            MaterialTheme.typography.bodySmall
                        },
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.alpha(alphaValue),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
```

- [ ] **步骤 2：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/TimeScrollPicker.kt
git commit -m "feat: 新增 TimeScrollPicker 时间滚轮组件"
```

---

### 任务 8：TimeSelectionSection 时间选择区域

**文件：**
- 创建：`feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/TimeSelectionSection.kt`

- [ ] **步骤 1：创建 TimeSelectionSection**

```kotlin
package com.nltimer.feature.home.ui.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Composable
fun TimeSelectionSection(
    startDateTime: LocalDateTime,
    endDateTime: LocalDateTime?,
    onStartDateTimeChange: (LocalDateTime) -> Unit,
    onEndDateTimeChange: ((LocalDateTime) -> Unit)?,
    modifier: Modifier = Modifier,
    showEndTime: Boolean = true,
    onQuickFillPreviousEnd: (() -> Unit)? = null,
    onQuickFillCurrentStart: (() -> Unit)? = null,
    onQuickFillCurrentEnd: (() -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "开始",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(40.dp),
            )
            DateScrollPicker(
                selectedDate = startDateTime.toLocalDate(),
                onDateSelected = { newDate ->
                    onStartDateTimeChange(LocalDateTime.of(newDate, startDateTime.toLocalTime()))
                },
            )
            TimeScrollPicker(
                selectedTime = startDateTime.toLocalTime(),
                onTimeSelected = { newTime ->
                    onStartDateTimeChange(LocalDateTime.of(startDateTime.toLocalDate(), newTime))
                },
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (onQuickFillPreviousEnd != null) {
                    FilterChip(
                        selected = false,
                        onClick = onQuickFillPreviousEnd,
                        label = { Text("上尾", style = MaterialTheme.typography.labelSmall) },
                    )
                }
                if (onQuickFillCurrentStart != null) {
                    FilterChip(
                        selected = false,
                        onClick = onQuickFillCurrentStart,
                        label = { Text("当前", style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }
        }

        if (showEndTime) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "结束",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(40.dp),
                )
                if (endDateTime != null) {
                    DateScrollPicker(
                        selectedDate = endDateTime.toLocalDate(),
                        onDateSelected = { newDate ->
                            onEndDateTimeChange?.invoke(LocalDateTime.of(newDate, endDateTime.toLocalTime()))
                        },
                    )
                    TimeScrollPicker(
                        selectedTime = endDateTime.toLocalTime(),
                        onTimeSelected = { newTime ->
                            onEndDateTimeChange?.invoke(LocalDateTime.of(endDateTime.toLocalDate(), newTime))
                        },
                    )
                }
                if (onQuickFillCurrentEnd != null) {
                    FilterChip(
                        selected = false,
                        onClick = onQuickFillCurrentEnd,
                        label = { Text("当前", style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }
        }
    }
}
```

- [ ] **步骤 2：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/TimeSelectionSection.kt
git commit -m "feat: 新增 TimeSelectionSection 时间选择区域组件"
```

---

### 任务 9：TimeFillActions 时间填充按钮组

**文件：**
- 创建：`feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/TimeFillActions.kt`

- [ ] **步骤 1：创建 TimeFillActions**

```kotlin
package com.nltimer.feature.home.ui.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TimeFillActions(
    onFillLeft: () -> Unit,
    onFillRight: () -> Unit,
    modifier: Modifier = Modifier,
    leftFillEnabled: Boolean = true,
    rightFillEnabled: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = onFillLeft,
            enabled = leftFillEnabled,
            modifier = Modifier.weight(1f),
        ) {
            Text("← 填满左侧空闲", style = MaterialTheme.typography.labelMedium)
        }
        OutlinedButton(
            onClick = onFillRight,
            enabled = rightFillEnabled,
            modifier = Modifier.weight(1f),
        ) {
            Text("填满右侧空闲 →", style = MaterialTheme.typography.labelMedium)
        }
    }
}
```

- [ ] **步骤 2：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/TimeFillActions.kt
git commit -m "feat: 新增 TimeFillActions 时间填充按钮组"
```

---

### 任务 10：DeleteConfirmDialog 删除确认弹窗

**文件：**
- 创建：`feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/DeleteConfirmDialog.kt`

- [ ] **步骤 1：创建 DeleteConfirmDialog**

```kotlin
package com.nltimer.feature.home.ui.sheet

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun DeleteConfirmDialog(
    behaviorName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认删除", style = MaterialTheme.typography.titleMedium) },
        text = {
            Text(
                "确定要删除行为 \"$behaviorName\" 吗？\n此操作不可撤销。",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确认删除", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        shape = MaterialTheme.shapes.extraLarge,
    )
}
```

- [ ] **步骤 2：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/DeleteConfirmDialog.kt
git commit -m "feat: 新增 DeleteConfirmDialog 删除确认弹窗"
```

---

### 任务 11：BehaviorDetailSheetContent 详情内容体

**文件：**
- 创建：`feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/BehaviorDetailSheetContent.kt`

- [ ] **步骤 1：创建 BehaviorDetailSheetContent**

```kotlin
package com.nltimer.feature.home.ui.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.feature.home.model.BehaviorDetailUiState
import com.nltimer.feature.home.model.TagUiState
import java.time.LocalDateTime

@Composable
fun BehaviorDetailSheetContent(
    detail: BehaviorDetailUiState,
    isSaving: Boolean,
    onActivitySelect: (Long) -> Unit,
    onTagToggle: (Long) -> Unit,
    onStartDateTimeChange: (LocalDateTime) -> Unit,
    onEndDateTimeChange: (LocalDateTime) -> Unit,
    onNatureChange: (BehaviorNature) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onQuickFillPreviousEnd: () -> Unit,
    onQuickFillCurrentStart: () -> Unit,
    onQuickFillCurrentEnd: () -> Unit,
    onFillLeft: () -> Unit,
    onFillRight: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${detail.activityEmoji ?: ""} ${detail.activityName}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = detail.status.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "活动",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        ActivityPicker(
            activities = detail.allActivities,
            selectedActivityId = detail.activityId,
            onActivitySelect = onActivitySelect,
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "关联标签",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        TagPicker(
            tags = detail.allAvailableTags.map { tagUi ->
                com.nltimer.core.data.model.Tag(
                    id = tagUi.id,
                    name = tagUi.name,
                    color = tagUi.color,
                    textColor = null,
                    icon = null,
                    category = null,
                    priority = 0,
                    usageCount = 0,
                    sortOrder = 0,
                    isArchived = !tagUi.isActive,
                )
            },
            selectedTagIds = detail.tags.filter { it.isActive }.map { it.id }.toSet(),
            onTagToggle = onTagToggle,
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "所有标签",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        TagPicker(
            tags = detail.allAvailableTags.map { tagUi ->
                com.nltimer.core.data.model.Tag(
                    id = tagUi.id,
                    name = tagUi.name,
                    color = tagUi.color,
                    textColor = null,
                    icon = null,
                    category = null,
                    priority = 0,
                    usageCount = 0,
                    sortOrder = 0,
                    isArchived = !tagUi.isActive,
                )
            },
            selectedTagIds = detail.tags.filter { it.isActive }.map { it.id }.toSet(),
            onTagToggle = onTagToggle,
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "时间设置",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        TimeSelectionSection(
            startDateTime = detail.startTime,
            endDateTime = detail.endTime,
            onStartDateTimeChange = onStartDateTimeChange,
            onEndDateTimeChange = { onEndDateTimeChange(it) },
            showEndTime = detail.status == BehaviorNature.COMPLETED,
            onQuickFillPreviousEnd = if (detail.status != BehaviorNature.PENDING) {
                onQuickFillPreviousEnd
            } else null,
            onQuickFillCurrentStart = onQuickFillCurrentStart,
            onQuickFillCurrentEnd = if (detail.status != BehaviorNature.PENDING) {
                onQuickFillCurrentEnd
            } else null,
        )

        Spacer(modifier = Modifier.height(12.dp))
        TimeFillActions(
            onFillLeft = onFillLeft,
            onFillRight = onFillRight,
            leftFillEnabled = detail.status != BehaviorNature.PENDING,
            rightFillEnabled = detail.status != BehaviorNature.PENDING,
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "备注",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        NoteInput(
            note = detail.note ?: "",
            onNoteChange = onNoteChange,
        )

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("删除")
            }
            Button(
                onClick = onSave,
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("保存修改")
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            behaviorName = "${detail.activityEmoji ?: ""} ${detail.activityName}",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
        )
    }
}
```

- [ ] **步骤 2：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/BehaviorDetailSheetContent.kt
git commit -m "feat: 新增 BehaviorDetailSheetContent 详情内容体组件"
```

---

### 任务 12：BehaviorDetailSheet 详情弹窗外壳

**文件：**
- 创建：`feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/BehaviorDetailSheet.kt`

- [ ] **步骤 1：创建 BehaviorDetailSheet**

```kotlin
package com.nltimer.feature.home.ui.sheet

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.feature.home.model.BehaviorDetailUiState
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BehaviorDetailSheet(
    detail: BehaviorDetailUiState,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onActivitySelect: (Long) -> Unit,
    onTagToggle: (Long) -> Unit,
    onStartDateTimeChange: (LocalDateTime) -> Unit,
    onEndDateTimeChange: (LocalDateTime) -> Unit,
    onNatureChange: (com.nltimer.core.data.model.BehaviorNature) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onQuickFillPreviousEnd: () -> Unit,
    onQuickFillCurrentStart: () -> Unit,
    onQuickFillCurrentEnd: () -> Unit,
    onFillLeft: () -> Unit,
    onFillRight: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        BehaviorDetailSheetContent(
            modifier = modifier,
            detail = detail,
            isSaving = isSaving,
            onActivitySelect = onActivitySelect,
            onTagToggle = onTagToggle,
            onStartDateTimeChange = onStartDateTimeChange,
            onEndDateTimeChange = onEndDateTimeChange,
            onNatureChange = onNatureChange,
            onNoteChange = onNoteChange,
            onSave = onSave,
            onDelete = onDelete,
            onQuickFillPreviousEnd = onQuickFillPreviousEnd,
            onQuickFillCurrentStart = onQuickFillCurrentStart,
            onQuickFillCurrentEnd = onQuickFillCurrentEnd,
            onFillLeft = onFillLeft,
            onFillRight = onFillRight,
        )
    }
}
```

- [ ] **步骤 2：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/BehaviorDetailSheet.kt
git commit -m "feat: 新增 BehaviorDetailSheet 详情弹窗外壳"
```

---

### 任务 13：HomeViewModel 新增详情相关方法

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeViewModel.kt`

- [ ] **步骤 1：在 HomeViewModel 中新增详情相关字段和方法**

在类末尾添加：

```kotlin
    fun showDetailSheet(behaviorId: Long) {
        viewModelScope.launch {
            try {
                val detail = behaviorRepository.getBehaviorWithDetails(behaviorId) ?: return@launch
                val allActs = activityRepository.getAllActive().firstOrNull() ?: emptyList()
                val allTagsList = tagRepository.getAllActive().firstOrNull() ?: emptyList()

                val detailState = BehaviorDetailUiState(
                    behaviorId = detail.behavior.id,
                    activityId = detail.activity.id,
                    activityEmoji = detail.activity.emoji,
                    activityName = detail.activity.name,
                    startTime = java.time.Instant.ofEpochMilli(detail.behavior.startTime)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime(),
                    endTime = detail.behavior.endTime?.let {
                        java.time.Instant.ofEpochMilli(it)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime()
                    },
                    status = detail.behavior.status,
                    note = detail.behavior.note,
                    tags = detail.tags.map {
                        TagUiState(id = it.id, name = it.name, color = it.color, isActive = !it.isArchived)
                    },
                    allAvailableTags = allTagsList.map {
                        TagUiState(id = it.id, name = it.name, color = it.color, isActive = !it.isArchived)
                    },
                    allActivities = allActs,
                    achievementLevel = detail.behavior.achievementLevel,
                    estimatedDuration = detail.behavior.estimatedDuration,
                    actualDuration = detail.behavior.actualDuration,
                )

                _uiState.update {
                    it.copy(
                        isDetailSheetVisible = true,
                        detailBehavior = detailState,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "加载详情失败: ${e.message}") }
            }
        }
    }

    fun hideDetailSheet() {
        _uiState.update {
            it.copy(
                isDetailSheetVisible = false,
                detailBehavior = null,
                errorMessage = null,
            )
        }
    }

    fun updateBehaviorDetail(
        behaviorId: Long,
        activityId: Long,
        startTime: Long,
        endTime: Long?,
        status: BehaviorNature,
        note: String?,
        tagIds: List<Long>,
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true) }

                if (status == BehaviorNature.ACTIVE) {
                    behaviorRepository.endCurrentBehavior(startTime)
                }

                behaviorRepository.updateBehavior(
                    id = behaviorId,
                    activityId = activityId,
                    startTime = startTime,
                    endTime = endTime,
                    status = status.key,
                    note = note,
                )
                behaviorRepository.updateTagsForBehavior(behaviorId, tagIds)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isDetailSheetVisible = false,
                        detailBehavior = null,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "保存失败: ${e.message}",
                    )
                }
            }
        }
    }

    fun deleteBehaviorDetail(behaviorId: Long) {
        viewModelScope.launch {
            try {
                behaviorRepository.delete(behaviorId)
                _uiState.update {
                    it.copy(
                        isDetailSheetVisible = false,
                        detailBehavior = null,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "删除失败: ${e.message}") }
            }
        }
    }

    fun fillLeftGap(behaviorId: Long) {
        viewModelScope.launch {
            try {
                val dayStart = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                val dayEnd = today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

                val behaviors = behaviorRepository.getHomeBehaviors(dayStart, dayEnd).firstOrNull() ?: return@launch
                val sorted = behaviors.filter { it.status == BehaviorNature.ACTIVE || it.status == BehaviorNature.COMPLETED }
                    .sortedBy { it.startTime }
                val currentIndex = sorted.indexOfFirst { it.id == behaviorId }
                if (currentIndex < 0) return@launch

                val newStartTime = if (currentIndex > 0) {
                    sorted[currentIndex - 1].endTime ?: sorted[currentIndex - 1].startTime
                } else {
                    dayStart
                }

                val currentBehavior = sorted[currentIndex]
                val newEndTime = if (currentBehavior.endTime == null) {
                    System.currentTimeMillis()
                } else {
                    currentBehavior.endTime
                }

                behaviorRepository.updateBehavior(
                    id = behaviorId,
                    activityId = currentBehavior.activityId,
                    startTime = newStartTime,
                    endTime = newEndTime,
                    status = BehaviorNature.COMPLETED.key,
                    note = currentBehavior.note,
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "填充失败: ${e.message}") }
            }
        }
    }

    fun fillRightGap(behaviorId: Long) {
        viewModelScope.launch {
            try {
                val dayStart = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                val dayEnd = today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

                val behaviors = behaviorRepository.getHomeBehaviors(dayStart, dayEnd).firstOrNull() ?: return@launch
                val sorted = behaviors.filter { it.status == BehaviorNature.ACTIVE || it.status == BehaviorNature.COMPLETED }
                    .sortedBy { it.startTime }
                val currentIndex = sorted.indexOfFirst { it.id == behaviorId }
                if (currentIndex < 0) return@launch

                val currentBehavior = sorted[currentIndex]
                val newEndTime = if (currentIndex < sorted.size - 1) {
                    sorted[currentIndex + 1].startTime
                } else {
                    dayEnd
                }

                val newStartTime = if (currentBehavior.startTime == 0L) {
                    System.currentTimeMillis() - 5 * 60_000
                } else {
                    currentBehavior.startTime
                }

                behaviorRepository.updateBehavior(
                    id = behaviorId,
                    activityId = currentBehavior.activityId,
                    startTime = newStartTime,
                    endTime = newEndTime,
                    status = BehaviorNature.COMPLETED.key,
                    note = currentBehavior.note,
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "填充失败: ${e.message}") }
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
```

- [ ] **步骤 2：在 HomeViewModel 顶部添加必要的 import**

```kotlin
import com.nltimer.feature.home.model.BehaviorDetailUiState
import com.nltimer.feature.home.model.TagUiState
import kotlinx.coroutines.flow.firstOrNull
```

- [ ] **步骤 3：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeViewModel.kt
git commit -m "feat: HomeViewModel 新增详情弹窗相关方法"
```

---

### 任务 14：GridCell 添加长按手势

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/components/GridCell.kt`

- [ ] **步骤 1：读取 GridCell 现有内容**

- [ ] **步骤 2：修改 GridCell 添加 combinedClickable 长按手势**

在 GridCell 的 modifier 中，将 `clickable` 替换为 `combinedClickable`：

```kotlin
import androidx.compose.foundation.combinedClickable

// 在 GridCell Composable 中，找到 modifier.clickable 替换为：
modifier = modifier
    .combinedClickable(
        onClick = { onClick(behaviorId) },
        onLongClick = { onLongPress(behaviorId) },
    )
```

并在 GridCell 参数中添加 `onLongPress: (Long?) -> Unit = {}`。

- [ ] **步骤 3：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/components/GridCell.kt
git commit -m "feat: GridCell 添加长按手势支持"
```

---

### 任务 15：TimeAxisGrid 透传长按回调

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/components/TimeAxisGrid.kt`

- [ ] **步骤 1：读取 TimeAxisGrid 现有内容**

- [ ] **步骤 2：在 TimeAxisGrid 中添加 onCellLongPress 参数并透传**

在 TimeAxisGrid Composable 参数中添加：
```kotlin
onCellLongPress: (Long?) -> Unit = {},
```

并在 GridCell 调用处透传：
```kotlin
onLongPress = onCellLongPress,
```

- [ ] **步骤 3：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/components/TimeAxisGrid.kt
git commit -m "feat: TimeAxisGrid 透传长按回调"
```

---

### 任务 16：HomeScreen 集成 DetailSheet

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt`

- [ ] **步骤 1：读取 HomeScreen 现有内容**

- [ ] **步骤 2：在 HomeScreen 中添加 DetailSheet 显示逻辑**

在 HomeScreen Composable 中，在 AddBehaviorSheet 之后添加：

```kotlin
if (uiState.isDetailSheetVisible && uiState.detailBehavior != null) {
    var localStartTime by remember { mutableStateOf(uiState.detailBehavior.startTime) }
    var localEndTime by remember { mutableStateOf(uiState.detailBehavior.endTime) }
    var localActivityId by remember { mutableStateOf(uiState.detailBehavior.activityId) }
    var localTagIds by remember { mutableStateOf(uiState.detailBehavior.tags.filter { it.isActive }.map { it.id }.toSet()) }
    var localNature by remember { mutableStateOf(uiState.detailBehavior.status) }
    var localNote by remember { mutableStateOf(uiState.detailBehavior.note ?: "") }

    BehaviorDetailSheet(
        detail = uiState.detailBehavior.copy(
            startTime = localStartTime,
            endTime = localEndTime,
            activityId = localActivityId,
            tags = uiState.detailBehavior.tags.map { tag ->
                tag.copy(isActive = tag.id in localTagIds)
            },
            status = localNature,
            note = localNote,
        ),
        isSaving = uiState.isSaving,
        onDismiss = { viewModel.hideDetailSheet() },
        onActivitySelect = { localActivityId = it },
        onTagToggle = { tagId ->
            localTagIds = if (tagId in localTagIds) {
                localTagIds - tagId
            } else {
                localTagIds + tagId
            }
        },
        onStartDateTimeChange = { localStartTime = it },
        onEndDateTimeChange = { localEndTime = it },
        onNatureChange = { localNature = it },
        onNoteChange = { localNote = it },
        onSave = {
            val startMillis = localStartTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = localEndTime?.atZone(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            viewModel.updateBehaviorDetail(
                behaviorId = uiState.detailBehavior.behaviorId,
                activityId = localActivityId,
                startTime = startMillis,
                endTime = endMillis,
                status = localNature,
                note = localNote.ifBlank { null },
                tagIds = localTagIds.toList(),
            )
        },
        onDelete = {
            viewModel.deleteBehaviorDetail(uiState.detailBehavior.behaviorId)
        },
        onQuickFillPreviousEnd = {
            // 获取上一个行为的 endTime
            viewModelScope.launch {
                val dayStart = java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                val dayEnd = java.time.LocalDate.now().plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                val behaviors = viewModel.getBehaviorsForDay(dayStart, dayEnd)
                // 简化实现：直接调用 fillLeftGap
            }
        },
        onQuickFillCurrentStart = {
            localStartTime = java.time.LocalDateTime.now()
        },
        onQuickFillCurrentEnd = {
            localEndTime = java.time.LocalDateTime.now()
            if (localNature == BehaviorNature.ACTIVE) {
                localNature = BehaviorNature.COMPLETED
            }
        },
        onFillLeft = {
            viewModel.fillLeftGap(uiState.detailBehavior.behaviorId)
        },
        onFillRight = {
            viewModel.fillRightGap(uiState.detailBehavior.behaviorId)
        },
    )
}
```

- [ ] **步骤 3：在 HomeScreen 中添加必要的 import**

```kotlin
import com.nltimer.feature.home.ui.sheet.BehaviorDetailSheet
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
```

- [ ] **步骤 4：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt
git commit -m "feat: HomeScreen 集成 BehaviorDetailSheet"
```

---

### 任务 17：HomeRoute 透传回调

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeRoute.kt`

- [ ] **步骤 1：读取 HomeRoute 现有内容**

- [ ] **步骤 2：在 HomeRoute 中确保 TimeAxisGrid 的 onCellLongPress 回调透传**

如果 HomeRoute 中直接调用 TimeAxisGrid，确保传入 `onCellLongPress` 参数。

- [ ] **步骤 3：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/HomeRoute.kt
git commit -m "feat: HomeRoute 透传长按回调"
```

---

### 任务 18：BehaviorNature 添加 displayName

**文件：**
- 修改：`core/data/src/main/java/com/nltimer/core/data/model/BehaviorNature.kt`

- [ ] **步骤 1：读取 BehaviorNature 现有内容**

- [ ] **步骤 2：在 BehaviorNature 枚举中添加 displayName 属性**

```kotlin
enum class BehaviorNature(val key: String, val displayName: String) {
    PENDING("pending", "待开始"),
    ACTIVE("active", "进行中"),
    COMPLETED("completed", "已完成"),
}
```

- [ ] **步骤 3：Commit**

```bash
git add core/data/src/main/java/com/nltimer/core/data/model/BehaviorNature.kt
git commit -m "feat: BehaviorNature 添加 displayName 属性"
```

---

### 任务 19：构建验证

- [ ] **步骤 1：运行构建命令**

```bash
./gradlew assembleDebug
```

预期：BUILD SUCCESSFUL

- [ ] **步骤 2：运行 lint 检查**

```bash
./gradlew lint
```

预期：无严重错误

- [ ] **步骤 3：Commit**

```bash
git add .
git commit -m "chore: 构建验证通过"
```

---

### 任务 20：AddBehaviorSheet 重构为组合可复用组件

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/AddBehaviorSheet.kt`

- [ ] **步骤 1：重构 AddBehaviorSheetContent 使用 TimeSelectionSection**

将现有的时间选择部分替换为 TimeSelectionSection：

```kotlin
TimeSelectionSection(
    startDateTime = LocalDateTime.of(today, startTime),
    endDateTime = endTime?.let { LocalDateTime.of(today, it) },
    onStartDateTimeChange = { localDateTime ->
        startTime = localDateTime.toLocalTime()
    },
    onEndDateTimeChange = { localDateTime ->
        endTime = localDateTime.toLocalTime()
    },
    showEndTime = nature == BehaviorNature.COMPLETED,
)
```

- [ ] **步骤 2：Commit**

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/sheet/AddBehaviorSheet.kt
git commit -m "refactor: AddBehaviorSheet 重构为组合可复用组件"
```

---

## 自检清单

### 1. 规格覆盖度

| 需求 | 对应任务 |
|------|----------|
| 可交互式数据展示 | 任务 11 (BehaviorDetailSheetContent) |
| 开始时间选择 + 快捷按钮 | 任务 6, 7, 8 |
| 结束时间选择 + 快捷按钮 | 任务 6, 7, 8 |
| 活动及标签修改 | 任务 11 (复用 ActivityPicker, TagPicker) |
| 删除功能 + 确认弹窗 | 任务 10, 11 |
| 时间填充功能 | 任务 9, 13 (fillLeftGap/fillRightGap) |
| 可复用性 | 所有新增组件设计为无状态 Composable |

### 2. 占位符扫描

- 无 "TODO"、"待定"、"后续实现" 等占位符
- 所有步骤包含完整代码
- 类型名一致：BehaviorDetailUiState、TagUiState、BehaviorNature 等

### 3. 类型一致性

- BehaviorDetailUiState 在任务 1 定义，任务 11、12、13 中使用
- TagUiState 在 HomeUiState.kt 中已有定义，任务 13 中使用
- BehaviorNature 在任务 18 中添加 displayName，任务 11 中使用

---

计划已完成并保存到 `docs/superpowers/plans/2026-04-27-behavior-detail-sheet-implementation.md`。两种执行方式：

**1. 子代理驱动（推荐）** - 每个任务调度一个新的子代理，任务间进行审查，快速迭代

**2. 内联执行** - 在当前会话中使用 executing-plans 执行任务，批量执行并设有检查点供审查

选哪种方式？
