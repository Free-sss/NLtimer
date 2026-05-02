package com.nltimer.feature.home.ui.sheet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.input.nestedscroll.nestedScroll
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal data class DateItem(
    val displayText: String,
    val date: LocalDate,
    val isSpecial: Boolean = false,
)

@Composable
fun DualTimePicker(
    startTime: LocalDateTime = LocalDateTime.now(),
    endTime: LocalDateTime = LocalDateTime.now(),
    animate: Boolean = true,
    onDurationChanged: (Duration) -> Unit = {},
    onTimesChanged: (LocalDateTime, LocalDateTime) -> Unit = { _, _ -> },
) {
    // 抹平秒和纳秒，防止微小差异导致的无限重绘
    val sProp = remember(startTime) { startTime.withSecond(0).withNano(0) }
    val eProp = remember(endTime) { endTime.withSecond(0).withNano(0) }

    val today = LocalDate.now()
    val threeDaysAgo = today.minusDays(3)
    val dateFormatter = DateTimeFormatter.ofPattern("MM/dd")

    val sharedDates = remember(today) {
        (0..6).map { offset ->
            val date = threeDaysAgo.plusDays(offset.toLong())
            val isToday = date == today
            DateItem(
                displayText = if (isToday) "今天" else date.format(dateFormatter),
                date = date,
                isSpecial = isToday,
            )
        }
    }

    val todayIndex = sharedDates.indexOfFirst { it.date == today }
    val hours = remember { (0..23).map { it.toString().padStart(2, '0') } }
    val minutes = remember { (0..59).map { it.toString().padStart(2, '0') } }

    // 内部状态
    var leftSelectedDate by remember { mutableStateOf(sharedDates.find { it.date == sProp.toLocalDate() } ?: sharedDates[todayIndex]) }
    var leftSelectedHour by remember { mutableStateOf(sProp.hour.toString().padStart(2, '0')) }
    var leftSelectedMinute by remember { mutableStateOf(sProp.minute.toString().padStart(2, '0')) }

    var rightSelectedDate by remember { mutableStateOf(sharedDates.find { it.date == eProp.toLocalDate() } ?: sharedDates[todayIndex]) }
    var rightSelectedHour by remember { mutableStateOf(eProp.hour.toString().padStart(2, '0')) }
    var rightSelectedMinute by remember { mutableStateOf(eProp.minute.toString().padStart(2, '0')) }

    // 核心：记录最后一次“对外通知”的时间，用于打破双向绑定的死循环
    val lastNotifiedStart = remember { mutableStateOf(sProp) }
    val lastNotifiedEnd = remember { mutableStateOf(eProp) }

    // 当外部 Prop 改变时（例如通过快捷按钮），同步内部状态
    LaunchedEffect(sProp, eProp) {
        if (sProp != lastNotifiedStart.value) {
            sharedDates.find { it.date == sProp.toLocalDate() }?.let { leftSelectedDate = it }
            leftSelectedHour = sProp.hour.toString().padStart(2, '0')
            leftSelectedMinute = sProp.minute.toString().padStart(2, '0')
            lastNotifiedStart.value = sProp
        }
        if (eProp != lastNotifiedEnd.value) {
            sharedDates.find { it.date == eProp.toLocalDate() }?.let { rightSelectedDate = it }
            rightSelectedHour = eProp.hour.toString().padStart(2, '0')
            rightSelectedMinute = eProp.minute.toString().padStart(2, '0')
            lastNotifiedEnd.value = eProp
        }
    }

    val leftDateTime = remember(leftSelectedDate, leftSelectedHour, leftSelectedMinute) {
        leftSelectedDate.date.atTime(leftSelectedHour.toInt(), leftSelectedMinute.toInt())
    }
    val rightDateTime = remember(rightSelectedDate, rightSelectedHour, rightSelectedMinute) {
        rightSelectedDate.date.atTime(rightSelectedHour.toInt(), rightSelectedMinute.toInt())
    }

    // 当内部选择改变时（滚动滚轮），通知外部
    LaunchedEffect(leftDateTime, rightDateTime) {
        if (leftDateTime != lastNotifiedStart.value || rightDateTime != lastNotifiedEnd.value) {
            lastNotifiedStart.value = leftDateTime
            lastNotifiedEnd.value = rightDateTime
            onDurationChanged(Duration.between(leftDateTime, rightDateTime))
            onTimesChanged(leftDateTime, rightDateTime)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        TimePickerSection(
            dates = sharedDates,
            hours = hours,
            minutes = minutes,
            selectedDate = leftSelectedDate,
            selectedHour = leftSelectedHour,
            selectedMinute = leftSelectedMinute,
            initialDateIndex = todayIndex,
            animate = animate,
            onDateChanged = { leftSelectedDate = it },
            onHourChanged = { leftSelectedHour = it },
            onMinuteChanged = { leftSelectedMinute = it },
            modifier = Modifier.weight(1f),
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .padding(top = 40.dp, bottom = 16.dp),
        )

        TimePickerSection(
            dates = sharedDates,
            hours = hours,
            minutes = minutes,
            selectedDate = rightSelectedDate,
            selectedHour = rightSelectedHour,
            selectedMinute = rightSelectedMinute,
            initialDateIndex = todayIndex,
            animate = animate,
            onDateChanged = { rightSelectedDate = it },
            onHourChanged = { rightSelectedHour = it },
            onMinuteChanged = { rightSelectedMinute = it },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TimePickerSection(
    modifier: Modifier = Modifier,
    dates: List<DateItem>,
    hours: List<String>,
    minutes: List<String>,
    selectedDate: DateItem,
    selectedHour: String,
    selectedMinute: String,
    initialDateIndex: Int = 0,
    animate: Boolean = true,
    onDateChanged: (DateItem) -> Unit,
    onHourChanged: (String) -> Unit,
    onMinuteChanged: (String) -> Unit,
) {
    val itemHeight = 32.dp
    Column(
        modifier = modifier
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                DateWheelPicker(
                    items = dates,
                    selectedItem = selectedDate,
                    onItemSelected = onDateChanged,
                    itemHeight = itemHeight,
                    initialScrollIndex = initialDateIndex,
                    animate = animate,
                    modifier = Modifier.weight(1.5f),
                )
                WheelPicker(
                    items = hours,
                    selectedItem = selectedHour,
                    onItemSelected = onHourChanged,
                    itemHeight = itemHeight,
                    animate = animate,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = ":",
                    color = Color(0xFF0A1034),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 0.dp),
                )
                WheelPicker(
                    items = minutes,
                    selectedItem = selectedMinute,
                    onItemSelected = onMinuteChanged,
                    itemHeight = itemHeight,
                    animate = animate,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DateWheelPicker(
    modifier: Modifier = Modifier,
    items: List<DateItem>,
    selectedItem: DateItem,
    onItemSelected: (DateItem) -> Unit,
    itemHeight: Dp = 40.dp,
    visibleItemsCount: Int = 3,
    initialScrollIndex: Int = 0,
    animate: Boolean = true,
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialScrollIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val paddingCount = visibleItemsCount / 2

    val paddedItems = remember(items) {
        val list = mutableListOf<DateItem?>()
        repeat(paddingCount) { list.add(null) }
        list.addAll(items)
        repeat(paddingCount) { list.add(null) }
        list
    }

    LaunchedEffect(selectedItem) {
        if (!listState.isScrollInProgress) {
            val index = items.indexOfFirst { it.date == selectedItem.date }
            if (index != -1 && listState.firstVisibleItemIndex != index) {
                if (animate) {
                    listState.animateScrollToItem(index)
                } else {
                    listState.scrollToItem(index)
                }
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                val item = items.getOrNull(index)
                if (item != null && item.date != selectedItem.date) {
                    onItemSelected(item)
                }
            }
    }

    val textColor = MaterialTheme.colorScheme.onSecondaryContainer
    val selectedColor = MaterialTheme.colorScheme.onPrimaryContainer
    val specialColor = MaterialTheme.colorScheme.primary
    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = modifier
            .height(itemHeight * visibleItemsCount)
            .nestedScroll(remember { object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {} }),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(
            count = paddedItems.size,
            key = { index -> index },
        ) { index ->
            val item = paddedItems[index]
            val isSelected = item?.date == selectedItem.date

            Box(
                modifier = Modifier
                    .height(itemHeight)
                    .graphicsLayer {
                        val layoutInfo = listState.layoutInfo
                        val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == index }
                        if (itemInfo != null) {
                            val viewportCenter =
                                (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2f
                            val itemCenter = itemInfo.offset + itemInfo.size / 2f
                            val distanceFromCenter = itemCenter - viewportCenter

                            val fraction = (distanceFromCenter / (itemHeight.toPx() * (visibleItemsCount / 2f))).coerceIn(-1f, 1f)

                            rotationX = fraction * -35f
                            scaleY = 1f - abs(fraction) * 0.45f
                            scaleX = 1f + abs(fraction) * 0.45f
                            alpha = 1f - abs(fraction) * 0.6f
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (item != null) {
                    Text(
                        text = item.displayText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = if (isSelected) 14.sp else 12.sp,
                            fontWeight = if (item.isSpecial && isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isSelected && item.isSpecial -> specialColor
                                isSelected -> selectedColor
                                else -> textColor
                            },
                        ),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> WheelPicker(
    modifier: Modifier = Modifier,
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    itemHeight: Dp = 40.dp,
    visibleItemsCount: Int = 3,
    initialScrollIndex: Int = 0,
    animate: Boolean = true,
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialScrollIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val paddingCount = visibleItemsCount / 2

    val paddedItems = remember(items) {
        val list = mutableListOf<T?>()
        repeat(paddingCount) { list.add(null) }
        list.addAll(items)
        repeat(paddingCount) { list.add(null) }
        list
    }

    LaunchedEffect(selectedItem) {
        if (!listState.isScrollInProgress) {
            val index = items.indexOf(selectedItem)
            if (index != -1 && listState.firstVisibleItemIndex != index) {
                if (animate) {
                    listState.animateScrollToItem(index)
                } else {
                    listState.scrollToItem(index)
                }
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                val item = items.getOrNull(index)
                if (item != null && item != selectedItem) {
                    onItemSelected(item)
                }
            }
    }

    val textColor = MaterialTheme.colorScheme.onSecondaryContainer
    val selectedColor = MaterialTheme.colorScheme.onPrimaryContainer
    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = modifier
            .height(itemHeight * visibleItemsCount)
            .nestedScroll(remember { object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {} }),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(
            count = paddedItems.size,
            key = { index -> index },
        ) { index ->
            val item = paddedItems[index]
            val isSelected = item == selectedItem

            Box(
                modifier = Modifier
                    .height(itemHeight)
                    .graphicsLayer {
                        val layoutInfo = listState.layoutInfo
                        val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == index }
                        if (itemInfo != null) {
                            val viewportCenter =
                                (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2f
                            val itemCenter = itemInfo.offset + itemInfo.size / 2f
                            val distanceFromCenter = itemCenter - viewportCenter

                            val fraction = (distanceFromCenter / (itemHeight.toPx() * (visibleItemsCount / 2f))).coerceIn(-1f, 1f)

                            rotationX = fraction * -35f
                            scaleY = 1f - abs(fraction) * 0.45f
                            scaleX = 1f + abs(fraction) * 0.45f
                            alpha = 1f - abs(fraction) * 0.6f
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (item != null) {
                    Text(
                        text = item.toString(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = if (isSelected) 14.sp else 12.sp,
                            color = if (isSelected) selectedColor else textColor,
                        ),
                    )
                }
            }
        }
    }
}
