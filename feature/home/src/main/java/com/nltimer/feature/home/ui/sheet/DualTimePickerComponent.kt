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
import kotlinx.coroutines.flow.map
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
internal fun DualTimePicker(
    startTime: LocalDateTime = LocalDateTime.now(),
    endTime: LocalDateTime = LocalDateTime.now(),
    onDurationChanged: (Duration) -> Unit = {},
) {
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)
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

    var leftSelectedDate by remember { mutableStateOf(sharedDates[todayIndex]) }
    var leftSelectedHour by remember { mutableStateOf(startTime.hour.toString().padStart(2, '0')) }
    var leftSelectedMinute by remember { mutableStateOf(startTime.minute.toString().padStart(2, '0')) }

    var rightSelectedDate by remember { mutableStateOf(sharedDates[todayIndex]) }
    var rightSelectedHour by remember { mutableStateOf(endTime.hour.toString().padStart(2, '0')) }
    var rightSelectedMinute by remember { mutableStateOf(endTime.minute.toString().padStart(2, '0')) }

    val leftDateTime = remember(leftSelectedDate, leftSelectedHour, leftSelectedMinute) {
        leftSelectedDate.date.atTime(leftSelectedHour.toInt(), leftSelectedMinute.toInt())
    }
    val rightDateTime = remember(rightSelectedDate, rightSelectedHour, rightSelectedMinute) {
        rightSelectedDate.date.atTime(rightSelectedHour.toInt(), rightSelectedMinute.toInt())
    }

    LaunchedEffect(leftDateTime, rightDateTime) {
        val duration = Duration.between(leftDateTime, rightDateTime)
        onDurationChanged(duration)
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
                    modifier = Modifier.weight(1.5f),
                )
                WheelPicker(
                    items = hours,
                    selectedItem = selectedHour,
                    onItemSelected = onHourChanged,
                    itemHeight = itemHeight,
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
        val index = items.indexOfFirst { it.date == selectedItem.date }
        if (index != -1 && listState.firstVisibleItemIndex != index) {
            listState.scrollToItem(index)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { index -> items.getOrNull(index) }
            .distinctUntilChanged()
            .collect { item ->
                if (item != null) {
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
        modifier = modifier.height(itemHeight * visibleItemsCount),
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
internal fun <T> WheelPicker(
    modifier: Modifier = Modifier,
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    itemHeight: Dp = 40.dp,
    visibleItemsCount: Int = 3,
    initialScrollIndex: Int = 0,
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
        val index = items.indexOf(selectedItem)
        if (index != -1 && listState.firstVisibleItemIndex != index) {
            listState.scrollToItem(index)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { index -> items.getOrNull(index) }
            .distinctUntilChanged()
            .collect { item ->
                if (item != null) {
                    onItemSelected(item)
                }
            }
    }

    val textColor = MaterialTheme.colorScheme.onSecondaryContainer
    val selectedColor = MaterialTheme.colorScheme.onPrimaryContainer
    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = modifier.height(itemHeight * visibleItemsCount),
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
