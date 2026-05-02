package com.nltimer.feature.home.ui.sheet

import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
internal fun DualTimePicker(
    baseTime: LocalDateTime = LocalDateTime.now(),
) {
    val baseDate = baseTime.toLocalDate()
    val todayStr = baseDate.format(dateFormatter)
    val leftDates = remember(baseDate) {
        val dateList = baseDate.plusDays(-365)
        (0..730).map { offset ->
            dateList.plusDays(offset.toLong()).format(dateFormatter)
        }
    }
    val leftInitialIndex = 365
    val rightDates = listOf("前天", "昨天", "今天", "明天", "后天")
    val hours = remember { (0..23).map { it.toString().padStart(2, '0') } }
    val minutes = remember { (0..59).map { it.toString().padStart(2, '0') } }

    var leftSelectedDate by remember { mutableStateOf(todayStr) }
    var leftSelectedHour by remember { mutableStateOf(baseTime.hour.toString().padStart(2, '0')) }
    var leftSelectedMinute by remember { mutableStateOf(baseTime.minute.toString().padStart(2, '0')) }

    var rightSelectedDate by remember { mutableStateOf("今天") }
    var rightSelectedHour by remember { mutableStateOf(baseTime.hour.toString().padStart(2, '0')) }
    var rightSelectedMinute by remember { mutableStateOf(baseTime.minute.toString().padStart(2, '0')) }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        TimePickerSection(
            dates = leftDates,
            hours = hours,
            minutes = minutes,
            selectedDate = leftSelectedDate,
            selectedHour = leftSelectedHour,
            selectedMinute = leftSelectedMinute,
            initialDateIndex = leftInitialIndex,
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
            dates = rightDates,
            hours = hours,
            minutes = minutes,
            selectedDate = rightSelectedDate,
            selectedHour = rightSelectedHour,
            selectedMinute = rightSelectedMinute,
            initialDateIndex = 2,
            onDateChanged = { rightSelectedDate = it },
            onHourChanged = { rightSelectedHour = it },
            onMinuteChanged = { rightSelectedMinute = it },
            modifier = Modifier.weight(1f),
        )
    }
}

private val dateFormatter = DateTimeFormatter.ofPattern("MM/dd")

@Composable
private fun TimePickerSection(
    modifier: Modifier = Modifier,
    dates: List<String>,
    hours: List<String>,
    minutes: List<String>,
    selectedDate: String,
    selectedHour: String,
    selectedMinute: String,
    initialDateIndex: Int = 0,
    onDateChanged: (String) -> Unit,
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
                WheelPicker(
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
        items(paddedItems.size) { index ->
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
                            fontSize = if (item == selectedItem) 14.sp else 12.sp,
//                            fontWeight = if (item == selectedItem) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) selectedColor else textColor,
                        ),
                    )
                }
            }
        }
    }
}
