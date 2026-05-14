package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.theme.appBorder
import com.nltimer.core.designsystem.theme.BorderTokens
import com.nltimer.core.designsystem.theme.ShapeTokens
import com.nltimer.core.designsystem.theme.styledAlpha
import com.nltimer.core.designsystem.theme.styledBorder
import com.nltimer.core.designsystem.theme.styledCorner

@Composable
fun GridCellEmpty(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = styledAlpha(0.3f)),
                RoundedCornerShape(styledCorner(ShapeTokens.CORNER_LARGE)),
            )
            .appBorder(
                borderProducer = {
                    BorderStroke(styledBorder(BorderTokens.STANDARD), MaterialTheme.colorScheme.outlineVariant.copy(alpha = styledAlpha(0.5f)))
                },
                shape = RoundedCornerShape(styledCorner(ShapeTokens.CORNER_LARGE))
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "+",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "添加行为",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
