package com.nltimer.core.behaviorui.sheet

import android.graphics.DiscretePathEffect as AndroidDiscretePathEffect
import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nltimer.core.designsystem.theme.BorderTokens
import com.nltimer.core.designsystem.theme.ChipDisplayMode
import com.nltimer.core.designsystem.theme.ShapeTokens
import com.nltimer.core.designsystem.theme.styledAlpha
import com.nltimer.core.designsystem.theme.styledBorder
import com.nltimer.core.designsystem.theme.styledCorner

@Composable
internal fun AdaptiveActivityChip(
    chip: ChipItem,
    displayMode: ChipDisplayMode,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    fixedWidth: Dp? = null,
    maxWidth: Dp = 120.dp,
    useActivityColorForText: Boolean = true,
) {
    val baseColor = chip.color
    val containerColor = baseColor.copy(alpha = styledAlpha(0.15f))
    val contentColor = if (useActivityColorForText) {
        baseColor.copy(alpha = styledAlpha(0.9f))
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val borderColor = baseColor.copy(alpha = styledAlpha(0.5f))
    val selectedBorderColor = MaterialTheme.colorScheme.primary

    val shape = when (displayMode) {
        ChipDisplayMode.Capsules -> RoundedCornerShape(50)
        ChipDisplayMode.Squares -> RoundedCornerShape(0)
        ChipDisplayMode.SquareBorder -> RoundedCornerShape(0)
        ChipDisplayMode.None -> RoundedCornerShape(0)
        else -> RoundedCornerShape(styledCorner(ShapeTokens.CORNER_SMALL))
    }

    val surfaceColor = when (displayMode) {
        ChipDisplayMode.Filled, ChipDisplayMode.Capsules,
        ChipDisplayMode.RoundedCorners, ChipDisplayMode.Squares -> if (isSelected) MaterialTheme.colorScheme.primaryContainer else containerColor
        else -> Color.Transparent
    }

    val border = when (displayMode) {
        ChipDisplayMode.Capsules -> BorderStroke(styledBorder(BorderTokens.THIN), if (isSelected) selectedBorderColor else borderColor)
        else -> null
    }

    val chipModifier = if (fixedWidth != null) {
        Modifier.height(24.dp).width(fixedWidth)
    } else {
        Modifier.height(24.dp).widthIn(max = maxWidth)
    }

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        Surface(
            onClick = onClick,
            modifier = chipModifier
                .then(
                    when (displayMode) {
                        ChipDisplayMode.Underline -> {
                            Modifier.drawBehind {
                                val strokeWidth = 2.dp.toPx()
                                val y = size.height - strokeWidth / 2
                                val lineColor = if (isSelected) selectedBorderColor else containerColor
                                drawLine(
                                    color = lineColor,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = strokeWidth,
                                )
                            }
                        }
                        ChipDisplayMode.SquareBorder -> {
                            Modifier.drawBehind {
                                val strokeWidth = 1.5.dp.toPx()
                                val c = if (isSelected) selectedBorderColor else borderColor
                                drawRect(
                                    color = c,
                                    style = Stroke(
                                        width = strokeWidth,
                                        pathEffect = PathEffect.cornerPathEffect(2.5f),
                                        join = StrokeJoin.Round,
                                        cap = StrokeCap.Round,
                                    ),
                                )
                            }
                        }
                        ChipDisplayMode.HandDrawn -> {
                            Modifier.drawBehind {
                                val strokeWidth = 1.5.dp.toPx()
                                val c = if (isSelected) selectedBorderColor else borderColor
                                val paint = Paint().apply {
                                    isAntiAlias = true
                                    style = Paint.Style.STROKE
                                    this.strokeWidth = strokeWidth
                                    color = android.graphics.Color.argb(
                                        (c.alpha * 255).toInt(),
                                        (c.red * 255).toInt(),
                                        (c.green * 255).toInt(),
                                        (c.blue * 255).toInt(),
                                    )
                                    strokeJoin = Paint.Join.ROUND
                                    strokeCap = Paint.Cap.ROUND
                                    pathEffect = AndroidDiscretePathEffect(10f, 5f)
                                }
                                drawContext.canvas.nativeCanvas.drawRect(
                                    0f, 0f, size.width, size.height, paint,
                                )
                            }
                        }
                        ChipDisplayMode.DashedLines -> {
                            Modifier.drawBehind {
                                val strokeWidth = 1.5.dp.toPx()
                                val r = 6.dp.toPx()
                                val c = if (isSelected) selectedBorderColor else borderColor
                                drawRoundRect(
                                    color = c,
                                    cornerRadius = CornerRadius(r, r),
                                    style = Stroke(
                                        width = strokeWidth,
                                        pathEffect = PathEffect.dashPathEffect(
                                            floatArrayOf(8f, 4f),
                                        ),
                                    ),
                                )
                            }
                        }
                        else -> Modifier
                    }
                ),
            color = surfaceColor,
            contentColor = contentColor,
            shape = shape,
            border = border,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = if (displayMode == ChipDisplayMode.None) 0.dp else 8.dp),
            ) {
                Text(
                    text = chip.name,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = contentColor,
                )
            }
        }
    }
}
