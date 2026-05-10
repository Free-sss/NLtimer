@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.nltimer.core.designsystem.component

import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.theme.WavyProgressLevel

@Composable
fun AppProgressIndicator(
    progress: Float,
    level: WavyProgressLevel,
    modifier: Modifier = Modifier,
) {
    when (level) {
        WavyProgressLevel.OFF -> LinearProgressIndicator(
            progress = { progress },
            modifier = modifier.height(4.dp),
        )
        WavyProgressLevel.ON -> LinearWavyProgressIndicator(
            progress = { progress },
            modifier = modifier.height(8.dp),
        )
        WavyProgressLevel.PROMINENT -> LinearWavyProgressIndicator(
            progress = { progress },
            modifier = modifier.height(12.dp),
        )
    }
}
