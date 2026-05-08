package com.nltimer.feature.home.ui.components

import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.util.formatDuration
import com.nltimer.feature.home.model.GridCellUiState

@Composable
fun MomentFocusCard(
    activeCell: GridCellUiState?,
    nextPendingCell: GridCellUiState?,
    onCompleteBehavior: (Long) -> Unit,
    onStartNextPending: () -> Unit,
    onEmptyCellClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasActive = activeCell != null
    val hasPending = nextPendingCell != null

    when {
        hasActive -> ActiveCard(
            cell = activeCell!!,
            onComplete = { activeCell.behaviorId?.let(onCompleteBehavior) },
            modifier = modifier,
        )
        hasPending -> PendingCard(
            cell = nextPendingCell!!,
            onStart = onStartNextPending,
            modifier = modifier,
        )
        else -> EmptyCard(
            onClick = onEmptyCellClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun ActiveCard(
    cell: GridCellUiState,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val startMs = cell.startEpochMs ?: System.currentTimeMillis()
    val elapsedTime by produceState(initialValue = System.currentTimeMillis() - startMs) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            value = System.currentTimeMillis() - startMs
        }
    }
    val durationText = formatDuration(elapsedTime)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val cardShape = RoundedCornerShape(32.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(cardShape)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha),
                shape = cardShape
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .height(260.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SlideActionPill(
                onActivate = onComplete,
                activeLabel = "滑动完成",
                activatedLabel = "释放完成",
                leadingIcon = Icons.Filled.Check,
                activatedIcon = Icons.Filled.Check,
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "${cell.activityIconKey ?: ""} ${cell.activityName ?: ""}",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = durationText,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "正在专注...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PendingCard(
    cell: GridCellUiState,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val estimatedText = cell.estimatedDuration?.let { "预计 ${formatDuration(it)}" } ?: ""

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .height(260.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SlideActionPill(
                onActivate = onStart,
                activeLabel = "滑动开启",
                activatedLabel = "释放开启",
                leadingIcon = Icons.Filled.PlayArrow,
                activatedIcon = Icons.Filled.Check,
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "${cell.activityIconKey ?: ""} ${cell.activityName ?: ""}",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(Modifier.height(8.dp))

            if (estimatedText.isNotEmpty()) {
                Text(
                    text = estimatedText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
            }

            Text(
                text = "滑动开启目标",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun EmptyCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .height(260.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "添加行为",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "点击开始记录你的行为",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
