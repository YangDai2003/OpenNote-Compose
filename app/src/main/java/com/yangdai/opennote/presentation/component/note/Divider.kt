package com.yangdai.opennote.presentation.component.note

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DividerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun EditorDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = DividerDefaults.Thickness,
    color: Color = DividerDefaults.color,
) =
    Canvas(
        modifier
            .fillMaxWidth()
            .height(thickness)
    ) {
        val gradient = Brush.verticalGradient(
            colors = listOf(
                color,                    // 底部完全不透明
                color.copy(alpha = 0f)    // 顶部完全透明
            ),
            startY = size.height,        // 从底部开始
            endY = 0f                    // 到顶部结束
        )

        drawRect(
            brush = gradient,
            size = size
        )
    }

@Composable
fun TitleDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = DividerDefaults.Thickness,
    color: Color = DividerDefaults.color,
) =
    Canvas(
        modifier
            .fillMaxWidth()
            .height(thickness)
            .padding(bottom = 2.dp)
    ) {
        val gradient = Brush.verticalGradient(
            colors = listOf(
                color,
                color.copy(alpha = 0f),
            ),
            startY = 0f,
            endY = size.height
        )

        drawRect(
            brush = gradient,
            size = size
        )
    }