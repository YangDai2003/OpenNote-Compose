package com.yangdai.opennote.presentation.component

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
        val lineThickness = thickness.toPx()

        // 绘制主体部分
        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    color.copy(alpha = 0.2f),
                    color.copy(alpha = 1f),
                    color.copy(alpha = 1f),
                    color.copy(alpha = 0.4f)
                ),
                startY = 0f,
                endY = size.height
            ),
            strokeWidth = lineThickness,
            start = Offset(thickness.toPx() / 2, 0f),
            end = Offset(thickness.toPx() / 2, size.height),
            cap = StrokeCap.Round
        )
    }
}
