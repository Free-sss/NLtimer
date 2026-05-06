package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.theme.appBorder
import com.nltimer.feature.home.model.TagUiState

@Composable
fun Modifier.behaviorCardStyle(
    cardBackground: Color,
    borderColor: Color,
): Modifier = this
    .clip(RoundedCornerShape(16.dp))
    .background(cardBackground)
    .appBorder(
        borderProducer = { BorderStroke(1.dp, borderColor) },
        shape = RoundedCornerShape(16.dp)
    )
    .padding(12.dp)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BehaviorTagRow(tags: List<TagUiState>) {
    if (tags.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tags.forEach { tag ->
                TagChipSmall(tag.name)
            }
        }
    }
}
