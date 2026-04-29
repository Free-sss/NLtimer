package com.nltimer.feature.debug.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * 双列时间选择器调试预览入口
 * 包裹 [DualTimePicker] 到一个全屏 Surface 中，用于在调试页面中独立渲染
 */
@Composable
fun DualTimePickerDebugPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        DualTimePicker()
    }
}

/**
 * 双列时间选择器
 * 提供左右两列日期+时间滚轮选择器，通过 LazyColumn 实现的滚轮效果进行日期和时间的选取
 */
@Composable
private fun DualTimePicker() {
    // 左侧日期选项，格式 MM/DD
    val leftDates = listOf("03/16", "03/17", "03/18", "03/19", "03/20")
    // 右侧日期选项，使用中文相对日期
    val rightDates = listOf("前天", "昨天", "今天", "明天", "后天")
    // 小时选项 00-23
    val hours = (0..23).map { it.toString().padStart(2, '0') }
    // 分钟选项 00-59
    val minutes = (0..59).map { it.toString().padStart(2, '0') }

    var leftSelectedDate by remember { mutableStateOf("03/18") }
    var leftSelectedHour by remember { mutableStateOf("20") }
    var leftSelectedMinute by remember { mutableStateOf("08") }

    var rightSelectedDate by remember { mutableStateOf("今天") }
    var rightSelectedHour by remember { mutableStateOf("09") }
    var rightSelectedMinute by remember { mutableStateOf("44") }

    // 左右两列布局，中间用竖线分隔
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        TimePickerSection(
            title = "上尾",
            dates = leftDates,
            hours = hours,
            minutes = minutes,
            selectedDate = leftSelectedDate,
            selectedHour = leftSelectedHour,
            selectedMinute = leftSelectedMinute,
            onDateChanged = { leftSelectedDate = it },
            onHourChanged = { leftSelectedHour = it },
            onMinuteChanged = { leftSelectedMinute = it },
            modifier = Modifier.weight(1f),
        )

        // 左右两列之间的分隔线
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .padding(top = 40.dp, bottom = 16.dp),
        )

        TimePickerSection(
            title = "当前",
            dates = rightDates,
            hours = hours,
            minutes = minutes,
            selectedDate = rightSelectedDate,
            selectedHour = rightSelectedHour,
            selectedMinute = rightSelectedMinute,
            onDateChanged = { rightSelectedDate = it },
            onHourChanged = { rightSelectedHour = it },
            onMinuteChanged = { rightSelectedMinute = it },
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * 时间选择分段组件
 * 包含标题标签和日期/小时/分钟三个滚轮选择器，组合成一个完整的选取区
 *
 * @param title 顶部黑色标题标签的文本
 * @param dates 日期选项列表
 * @param hours 小时选项列表
 * @param minutes 分钟选项列表
 * @param selectedDate 当前选中的日期
 * @param selectedHour 当前选中的小时
 * @param selectedMinute 当前选中的分钟
 * @param onDateChanged 日期变动回调
 * @param onHourChanged 小时变动回调
 * @param onMinuteChanged 分钟变动回调
 * @param modifier 可选的修饰符
 */
@Composable
private fun TimePickerSection(
    title: String,
    dates: List<String>,
    hours: List<String>,
    minutes: List<String>,
    selectedDate: String,
    selectedHour: String,
    selectedMinute: String,
    onDateChanged: (String) -> Unit,
    onHourChanged: (String) -> Unit,
    onMinuteChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 每个选项行的高度
    val itemHeight = 40.dp

    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 顶部黑色标题标签
        Box(
            modifier = Modifier
                .background(Color.Black, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .padding(horizontal = 24.dp, vertical = 6.dp),
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 滚轮选择区域：背景高亮条 + 三个滚轮叠加
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            // 当前选中行的浅灰色高亮背景
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF2F2F2)),
            )

            // 日期滚轮 | 小时滚轮 | 冒号分隔符 | 分钟滚轮
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
                    modifier = Modifier.weight(1.5f),
                )
                WheelPicker(
                    items = hours,
                    selectedItem = selectedHour,
                    onItemSelected = onHourChanged,
                    itemHeight = itemHeight,
                    modifier = Modifier.weight(1f),
                )
                // 小时和分钟之间的冒号分隔符
                Text(
                    text = ":",
                    color = Color(0xFF0A1034),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 2.dp),
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

/**
 * 通用滚轮选择器
 * 基于 LazyColumn 实现的滚动选择器，支持吸附定位和滚动联动选中
 *
 * @param items 可选项列表
 * @param selectedItem 当前选中项
 * @param onItemSelected 选中项变化回调
 * @param itemHeight 每个选项的高度
 * @param visibleItemsCount 可视区域内的选项数量，默认为 3
 * @param modifier 可选的修饰符
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T> WheelPicker(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    itemHeight: Dp = 40.dp,
    visibleItemsCount: Int = 3,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    // 吸附滚动行为，使滚动停止时精确对齐到某一项
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val paddingCount = visibleItemsCount / 2

    // 在列表首尾添加空占位，使首尾选项也能居中显示
    val paddedItems = remember(items) {
        val list = mutableListOf<T?>()
        repeat(paddingCount) { list.add(null) }
        list.addAll(items)
        repeat(paddingCount) { list.add(null) }
        list
    }

    // 外部选中项变化时同步滚动位置
    LaunchedEffect(selectedItem) {
        val index = items.indexOf(selectedItem)
        if (index != -1 && listState.firstVisibleItemIndex != index) {
            listState.scrollToItem(index)
        }
    }

    // 监听滚动位置变化，反向同步选中状态
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
                modifier = Modifier.height(itemHeight),
                contentAlignment = Alignment.Center,
            ) {
                if (item != null) {
                    // 选中项使用深色加粗字体，未选中项使用浅灰色字体
                    Text(
                        text = item.toString(),
                        style = TextStyle(
                            fontSize = if (isSelected) 18.sp else 16.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF0A1034) else Color(0xFFC4C4C4),
                            textAlign = TextAlign.Center,
                        ),
                    )
                }
            }
        }
    }
}
