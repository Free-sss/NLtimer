package com.nltimer.feature.home.ui.sheet

import android.graphics.PathMeasure as AndroidPathMeasure
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.theme.PathDrawMode
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private const val WrigglingMaggotAmplitudeRatio = 0.03f
private const val PathSegmentCount = 24
private const val PathSegmentLength = 12f
private const val PathWaveCount = 2
private const val AnimationDurationMs = 1500

@Composable
fun BehaviorSheetBackground(
    pathDrawMode: PathDrawMode,
    emphasisColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var targetProgress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = AnimationDurationMs),
        label = "path_draw_progress"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "jumping_transition")
    val jumpProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = AnimationDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavy_line_progress"
    )

    LaunchedEffect(pathDrawMode) {
        targetProgress = 0f
        targetProgress = 1f
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawWithContent {
                drawContent()
                val strokeWidthPx = 3.dp.toPx()
                val halfStroke = strokeWidthPx / 2
                val radius = 28.dp.toPx()
                val width = size.width
                val height = size.height
                val extendedH = height * 1.2f
                val path = Path().apply {
                    moveTo(halfStroke, extendedH)
                    lineTo(halfStroke, radius)
                    arcTo(
                        rect = Rect(
                            halfStroke,
                            halfStroke,
                            radius * 2 - halfStroke,
                            radius * 2 - halfStroke
                        ),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                    lineTo(width - radius, halfStroke)
                    arcTo(
                        rect = Rect(
                            width - radius * 2 + halfStroke,
                            halfStroke,
                            width - halfStroke,
                            radius * 2 - halfStroke
                        ),
                        startAngleDegrees = 270f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                    lineTo(width - halfStroke, extendedH)
                }

                val pathMeasure = PathMeasure()
                pathMeasure.setPath(path, false)
                val totalLength = pathMeasure.length

                if (pathDrawMode != PathDrawMode.None && animatedProgress > 0f) {
                    when (pathDrawMode) {
                        PathDrawMode.StartToEnd -> {
                            val stopDistance = totalLength * animatedProgress
                            val animatedPath = Path()
                            pathMeasure.getSegment(0f, stopDistance, animatedPath)
                            drawPath(
                                path = animatedPath,
                                color = emphasisColor,
                                style = Stroke(width = strokeWidthPx)
                            )
                        }

                        PathDrawMode.BothSidesToMiddle -> {
                            val halfLength = totalLength / 2f
                            val drawLength = halfLength * animatedProgress
                            val startSegment = Path()
                            pathMeasure.getSegment(0f, drawLength, startSegment)
                            drawPath(
                                path = startSegment,
                                color = emphasisColor,
                                style = Stroke(width = strokeWidthPx)
                            )
                            val endSegment = Path()
                            pathMeasure.getSegment(
                                totalLength - drawLength,
                                totalLength,
                                endSegment
                            )
                            drawPath(
                                path = endSegment,
                                color = emphasisColor,
                                style = Stroke(width = strokeWidthPx)
                            )
                        }

                        PathDrawMode.WrigglingMaggot -> {
                            val androidPathMeasure = AndroidPathMeasure().apply {
                                setPath(path.asAndroidPath(), false)
                            }
                            val position = FloatArray(2)
                            val tangent = FloatArray(2)

                            for (i in 0 until PathSegmentCount) {
                                val baseRatio = i.toFloat() / PathSegmentCount
                                val offset = sin(
                                    (baseRatio * PathWaveCount + jumpProgress) * 2 * PI.toFloat()
                                ) * WrigglingMaggotAmplitudeRatio
                                val sampleRatio = (baseRatio + offset).coerceIn(0f, 1f)
                                val distance = sampleRatio * totalLength

                                androidPathMeasure.getPosTan(distance, position, tangent)

                                val angle = atan2(
                                    tangent[1].toDouble(),
                                    tangent[0].toDouble()
                                ).toFloat()
                                val cosA = cos(angle)
                                val sinA = sin(angle)

                                val halfLen = PathSegmentLength / 2f
                                val startX = position[0] - halfLen * cosA
                                val startY = position[1] - halfLen * sinA
                                val endX = position[0] + halfLen * cosA
                                val endY = position[1] + halfLen * sinA

                                drawLine(
                                    color = emphasisColor,
                                    start = Offset(startX, startY),
                                    end = Offset(endX, endY),
                                    strokeWidth = 3.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }

                        PathDrawMode.Random, PathDrawMode.None -> {}
                    }
                }
            }
    ) {
        content()
    }
}
