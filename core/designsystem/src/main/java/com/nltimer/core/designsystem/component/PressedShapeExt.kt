@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.nltimer.core.designsystem.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonShapes
import androidx.compose.runtime.Composable
import com.nltimer.core.designsystem.theme.PressedShapeLevel

@Composable
fun buttonShapesForLevel(level: PressedShapeLevel): ButtonShapes = when (level) {
    PressedShapeLevel.OFF -> ButtonDefaults.shapes()
    PressedShapeLevel.MILD -> ButtonDefaults.shapes(pressedShape = RoundedCornerShape(24))
    PressedShapeLevel.FULL_MORPH -> ButtonDefaults.shapes(pressedShape = ButtonDefaults.pressedShape)
}

@Composable
fun iconButtonShapesForLevel(level: PressedShapeLevel): IconButtonShapes = when (level) {
    PressedShapeLevel.OFF -> IconButtonDefaults.shapes()
    PressedShapeLevel.MILD -> IconButtonDefaults.shapes(pressedShape = RoundedCornerShape(24))
    PressedShapeLevel.FULL_MORPH -> IconButtonDefaults.shapes(pressedShape = IconButtonDefaults.largePressedShape)
}
