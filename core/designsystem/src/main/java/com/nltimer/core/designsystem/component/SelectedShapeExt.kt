@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.nltimer.core.designsystem.component

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import com.nltimer.core.designsystem.theme.ExpressivenessPreset

@Composable
fun selectedShape(expressiveness: ExpressivenessPreset) = when (expressiveness) {
    ExpressivenessPreset.SUBDUED -> CircleShape
    ExpressivenessPreset.STANDARD -> CircleShape
    ExpressivenessPreset.EXPRESSIVE -> MaterialShapes.VerySunny.toShape()
}
