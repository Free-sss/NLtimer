package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

/**
 * 表示被锁定、不可编辑的单元格 Composable。
 * 以暗淡的样式显示"+"占位符。
 *
 * @param modifier 修饰符
 */
@Composable
fun GridCellLocked(
    modifier: Modifier = Modifier,
) {
    // Mark-style-main
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.surfaceDim.copy(alpha = styledAlpha(0.35f)),
                RoundedCornerShape(styledCorner(ShapeTokens.CORNER_LARGE)),
            )
            .appBorder(
                borderProducer = {
                    BorderStroke(styledBorder(BorderTokens.THIN), MaterialTheme.colorScheme.outlineVariant.copy(alpha = styledAlpha(0.3f)))
                },
                shape = RoundedCornerShape(styledCorner(ShapeTokens.CORNER_LARGE))
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "+",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = styledAlpha(0.3f)),
        )
    }
}
