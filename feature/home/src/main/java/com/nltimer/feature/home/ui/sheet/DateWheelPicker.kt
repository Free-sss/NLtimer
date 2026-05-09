package com.nltimer.feature.home.ui.sheet

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Immutable
internal data class DateItem(
    val displayText: String,
    val date: LocalDate,
    val isSpecial: Boolean = false,
)

@Composable
internal fun DateWheelPicker(
    modifier: Modifier = Modifier,
    items: List<DateItem>,
    selectedItem: DateItem,
    onItemSelected: (DateItem) -> Unit,
    itemHeight: Dp = 40.dp,
    visibleItemsCount: Int = 3,
    initialScrollIndex: Int = 0,
    animate: Boolean = true,
    onCenterClick: () -> Unit = {},
) {
    WheelPicker(
        modifier = modifier,
        items = items,
        selectedItem = selectedItem,
        onItemSelected = onItemSelected,
        itemHeight = itemHeight,
        visibleItemsCount = visibleItemsCount,
        initialScrollIndex = initialScrollIndex,
        animate = animate,
        onCenterClick = onCenterClick,
        content = { item, isSelected ->
            DateWheelPickerText(item = item, isSelected = isSelected)
        },
    )
}

@Composable
private fun DateWheelPickerText(
    item: DateItem,
    isSelected: Boolean,
) {
    val specialColor = MaterialTheme.colorScheme.primary
    val selectedColor = MaterialTheme.colorScheme.onPrimaryContainer
    val textColor = MaterialTheme.colorScheme.onSecondaryContainer
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
