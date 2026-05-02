package com.nltimer.feature.debug.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.theme.ChipDisplayMode
import com.nltimer.core.designsystem.theme.GridLayoutMode
import com.nltimer.feature.home.ui.sheet.ActivityGridComponent
import com.nltimer.feature.home.ui.sheet.ChipItem

@Preview(showBackground = true)
@Composable
fun ActivityChipGridDebugPreview() {
    val sampleActivities = listOf(
        ChipItem(id = 1, name = "学习", color = Color(0xFF1B5E20)),
        ChipItem(id = 2, name = "读书", color = Color(0xFF43A047)),
        ChipItem(id = 3, name = "英语", color = Color(0xFF66BB6A)),
        ChipItem(id = 4, name = "c语言", color = Color(0xFF81C784)),
        ChipItem(id = 5, name = "微积分", color = Color(0xFF757575)),
        ChipItem(id = 6, name = "休息", color = Color(0xFF212121)),
        ChipItem(id = 7, name = "睡觉", color = Color(0xFF00BFA5)),
        ChipItem(id = 8, name = "Take a nap", color = Color(0xFF4DB6AC)),
        ChipItem(id = 9, name = "冥想", color = Color(0xFF006064)),
        ChipItem(id = 10, name = "信息流", color = Color(0xFFB71C1C)),
        ChipItem(id = 11, name = "生活", color = Color(0xFF8D6E63)),
        ChipItem(id = 12, name = "厕所", color = Color(0xFFD7CCC8)),
        ChipItem(id = 13, name = "金刚功", color = Color(0xFF795548)),
        ChipItem(id = 14, name = "吃饭", color = Color(0xFFD2B48C)),
        ChipItem(id = 15, name = "多巴胺", color = Color(0xFFB8860B))
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("自适应宽度 (Adaptive)", Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
            ActivityGridComponent(
                chips = sampleActivities,
                onChipClick = { },
                functionChipLabel = "活动",
                functionChipIcon = {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "管理",
                        modifier = Modifier.size(14.dp),
                    )
                },
                functionChipOnClick = { },
                useAdaptiveWidth = true,
                layoutMode = GridLayoutMode.Vertical
            )

            Text("固定宽度 (Fixed 80dp)", Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
            ActivityGridComponent(
                chips = sampleActivities,
                onChipClick = { },
                functionChipLabel = "活动",
                functionChipIcon = {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "管理",
                        modifier = Modifier.size(14.dp),
                    )
                },
                functionChipOnClick = { },
                useAdaptiveWidth = false,
                chipFixedWidth = 80.dp,
                layoutMode = GridLayoutMode.Vertical
            )

            Text("Horizontal Flow (Adaptive)", Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
            ActivityGridComponent(
                chips = sampleActivities,
                onChipClick = { },
                functionChipLabel = "活动",
                functionChipIcon = {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "管理",
                        modifier = Modifier.size(14.dp),
                    )
                },
                functionChipOnClick = { },
                useAdaptiveWidth = true,
                layoutMode = GridLayoutMode.Horizontal
            )
        }
    }
}
