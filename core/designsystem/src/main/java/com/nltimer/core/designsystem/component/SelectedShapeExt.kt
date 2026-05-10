package com.nltimer.core.designsystem.component

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.shapes.MaterialShapes
import androidx.compose.runtime.Composable
import com.nltimer.core.designsystem.theme.ExpressivenessPreset

@Composable
fun selectedShape(expressiveness: ExpressivenessPreset) = when (expressiveness) {
    ExpressivenessPreset.SUBDUED -> CircleShape
    ExpressivenessPreset.STANDARD -> CircleShape
    ExpressivenessPreset.EXPRESSIVE -> MaterialShapes.VerySunny.toShape()
}
