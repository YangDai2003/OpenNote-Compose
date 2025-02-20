package com.yangdai.opennote.presentation.component.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DividerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Timeline(
    modifier: Modifier = Modifier,
    color: Color = DividerDefaults.color,
    thickness: Dp = 2.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxHeight()
            .width(thickness)
    ) {
        val canvasHeight = size.height
        val lineThicknessPx = thickness.toPx()
        val halfLineThicknessPx = lineThicknessPx / 2

        // Defining the gradient colors outside for better readability and reusability
        val gradientColors = listOf(
            color.copy(alpha = 0.2f),
            color.copy(alpha = 1f),
            color.copy(alpha = 1f),
            color.copy(alpha = 0.4f)
        )

        // Drawing the main vertical line with a gradient
        drawLine(
            brush = Brush.verticalGradient(
                colors = gradientColors,
                startY = 0f,
                endY = canvasHeight
            ),
            strokeWidth = lineThicknessPx,
            start = Offset(halfLineThicknessPx, 0f),
            end = Offset(halfLineThicknessPx, canvasHeight),
            cap = StrokeCap.Round
        )
    }
}
