package com.nltimer.feature.debug.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nltimer.feature.home.ui.sheet.DualTimePicker
import com.nltimer.feature.home.ui.sheet.WheelPicker
import java.time.LocalDateTime

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

@Composable
fun SingleTimePickerDebugPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        SingleTimePicker()
    }
}

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
