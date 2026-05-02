package com.nltimer.feature.debug.ui.preview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.abs
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 双列时间选择器调试预览入口
 * 包裹 [DualTimePicker] 到一个全屏 Surface 中，用于在调试页面中独立渲染
 */
@Preview
@Composable
fun DualTimePickerDebugPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        val now = LocalDateTime.now()
        DualTimePicker(
            startTime = now,
            endTime = now.plusHours(1),
            onDurationChanged = {},
        )
    }
}

/**
 * 单列时间滚轮调试预览入口
 * 仅包含时:分滚轮（无日期），复用内部 [WheelPicker] 组件
 */
@Composable
fun SingleTimePickerDebugPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        SingleTimePicker()
    }
}

/**
 * 单列时间滚轮 — 仅时:分，无日期
 * @param baseTime 初始锚点时间
 */
@Composable
internal fun SingleTimePicker(
    baseTime: LocalDateTime = LocalDateTime.now(),
) {
    val hours = remember { (0..23).map { it.toString().padStart(2, '0') } }
    val minutes = remember { (0..59).map { it.toString().padStart(2, '0') } }

    var selectedHour by remember { mutableStateOf(baseTime.hour.toString().padStart(2, '0')) }
    var selectedMinute by remember { mutableStateOf(baseTime.minute.toString().padStart(2, '0')) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WheelPicker(
            items = hours,
            selectedItem = selectedHour,
            onItemSelected = { selectedHour = it },
            itemHeight = 32.dp,
            modifier = Modifier.width(56.dp),
        )
        Text(
            text = ":",
            color = Color(0xFF0A1034),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
        WheelPicker(
            items = minutes,
            selectedItem = selectedMinute,
            onItemSelected = { selectedMinute = it },
            itemHeight = 32.dp,
            modifier = Modifier.width(56.dp),
        )
    }
}

private val dateFormatter = DateTimeFormatter.ofPattern("MM/dd")

/**
 * 双列时间选择器
 * 提供左右两列日期+时间滚轮选择器，通过 LazyColumn 实现的滚轮效果进行日期和时间的选取。
 * 左侧为实际日期滚轮（懒加载 ±365 天），右侧为相对日期标签滚轮。
 *
 * @param startTime 左侧初始锚点时间
 * @param endTime 右侧初始锚点时间
 * @param onDurationChanged 时长变化回调
 */
@Composable
internal fun DualTimePicker(
    startTime: LocalDateTime = LocalDateTime.now(),
    endTime: LocalDateTime = LocalDateTime.now(),
    onDurationChanged: (Duration) -> Unit = {},
) {
    val today = LocalDate.now()
    val threeDaysAgo = today.minusDays(3)

    val sharedDates = remember(today) {
        (0..6).map { offset ->
            val date = threeDaysAgo.plusDays(offset.toLong())
            val isToday = date == today
            Pair(date, isToday)
        }
    }

    val todayIndex = sharedDates.indexOfFirst { it.second }

    val hours = remember { (0..23).map { it.toString().padStart(2, '0') } }
    val minutes = remember { (0..59).map { it.toString().padStart(2, '0') } }

    var leftSelectedDate by remember { mutableStateOf(sharedDates[todayIndex].first) }
    var leftSelectedHour by remember { mutableStateOf(startTime.hour.toString().padStart(2, '0')) }
    var leftSelectedMinute by remember { mutableStateOf(startTime.minute.toString().padStart(2, '0')) }

    var rightSelectedDate by remember { mutableStateOf(sharedDates[todayIndex].first) }
    var rightSelectedHour by remember { mutableStateOf(endTime.hour.toString().padStart(2, '0')) }
    var rightSelectedMinute by remember { mutableStateOf(endTime.minute.toString().padStart(2, '0')) }

    val leftDateTime = remember(leftSelectedDate, leftSelectedHour, leftSelectedMinute) {
        leftSelectedDate.atTime(leftSelectedHour.toInt(), leftSelectedMinute.toInt())
    }
    val rightDateTime = remember(rightSelectedDate, rightSelectedHour, rightSelectedMinute) {
        rightSelectedDate.atTime(rightSelectedHour.toInt(), rightSelectedMinute.toInt())
    }

    LaunchedEffect(leftDateTime, rightDateTime) {
        val duration = Duration.between(leftDateTime, rightDateTime)
        onDurationChanged(duration)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        TimePickerSection(
            dates = sharedDates.map { (date, isToday) ->
                if (isToday) "今天" else date.format(dateFormatter)
            },
            hours = hours,
            minutes = minutes,
            selectedDate = "今天",
            selectedHour = leftSelectedHour,
            selectedMinute = leftSelectedMinute,
            initialDateIndex = todayIndex,
            onDateChanged = { leftSelectedDate = sharedDates[it].first },
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
            dates = sharedDates.map { (date, isToday) ->
                if (isToday) "今天" else date.format(dateFormatter)
            },
            hours = hours,
            minutes = minutes,
            selectedDate = "今天",
            selectedHour = rightSelectedHour,
            selectedMinute = rightSelectedMinute,
            initialDateIndex = todayIndex,
            onDateChanged = { rightSelectedDate = sharedDates[it].first },
            onHourChanged = { rightSelectedHour = it },
            onMinuteChanged = { rightSelectedMinute = it },
            modifier = Modifier.weight(1f),
        )
    }
}

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
    onDateChanged: (Int) -> Unit,
    onHourChanged: (String) -> Unit,
    onMinuteChanged: (String) -> Unit,
) {
    val itemHeight = 32.dp
    val primaryColor = MaterialTheme.colorScheme.onPrimary
    Column(
        modifier = modifier.fillMaxHeight(),
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
                    ,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                IndexWheelPicker(
                    items = dates,
                    selectedIndex = dates.indexOf(selectedDate).coerceAtLeast(0),
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
private fun <T> WheelPicker(
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
        if (!listState.isScrollInProgress) {
            val index = items.indexOf(selectedItem)
            if (index != -1 && listState.firstVisibleItemIndex != index) {
                listState.animateScrollToItem(index)
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
                        style = TextStyle(
                            fontSize = if (isSelected) 14.sp else 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                            color = if (isSelected) selectedColor else textColor,
                            textAlign = TextAlign.Center,
                            letterSpacing = if (isSelected) 0.5.sp else 0.sp,
                        ),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IndexWheelPicker(
    modifier: Modifier = Modifier,
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    itemHeight: Dp = 40.dp,
    visibleItemsCount: Int = 3,
    initialScrollIndex: Int = 0,
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialScrollIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val paddingCount = visibleItemsCount / 2

    val paddedItems = remember(items) {
        val list = mutableListOf<String?>()
        repeat(paddingCount) { list.add(null) }
        list.addAll(items)
        repeat(paddingCount) { list.add(null) }
        list
    }

    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress) {
            if (listState.firstVisibleItemIndex != selectedIndex) {
                listState.animateScrollToItem(selectedIndex)
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                if (index in items.indices && index != selectedIndex) {
                    onItemSelected(index)
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
        items(paddedItems.size) { index ->
            val item = paddedItems[index]
            val isSelected = index - paddingCount == selectedIndex

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
                        text = item,
                        style = TextStyle(
                            fontSize = if (isSelected) 14.sp else 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                            color = if (isSelected) selectedColor else textColor,
                            textAlign = TextAlign.Center,
                            letterSpacing = if (isSelected) 0.5.sp else 0.sp,
                        ),
                    )
                }
            }
        }
    }
}
