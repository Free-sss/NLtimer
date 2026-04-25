package com.nltimer.feature.timer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nltimer.core.designsystem.theme.AppTheme
import com.nltimer.feature.timer.model.TimerState
import com.nltimer.feature.timer.viewmodel.TimerViewModel

@Composable
fun TimerRoute(
    viewModel: TimerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TimerScreen(
        state = state,
        onToggle = viewModel::toggleTimer,
        onReset = viewModel::resetTimer,
    )
}

@Composable
fun TimerScreen(
    state: TimerState,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = formatTime(state.elapsedSeconds),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { contentDescription = "Elapsed time" },
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                FilledTonalButton(
                    onClick = onReset,
                    modifier = Modifier.semantics { contentDescription = "Reset timer" },
                ) {
                    Text("Reset")
                }
                Button(
                    onClick = onToggle,
                    modifier = Modifier.semantics { contentDescription = "Toggle timer" },
                ) {
                    Text(if (state.isRunning) "Pause" else "Start")
                }
            }
        }
    }
}

fun formatTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return "%02d:%02d:%02d".format(h, m, s)
}

@Preview(showBackground = true)
@Composable
private fun TimerScreenPreview() {
    AppTheme {
        var state by remember { mutableStateOf(TimerState(elapsedSeconds = 3661, isRunning = true)) }
        TimerScreen(
            state = state,
            onToggle = {
                state = state.copy(isRunning = !state.isRunning)
            },
            onReset = {
                state = TimerState()
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimerScreenInitialPreview() {
    AppTheme {
        TimerScreen(
            state = TimerState(),
            onToggle = {},
            onReset = {},
        )
    }
}
