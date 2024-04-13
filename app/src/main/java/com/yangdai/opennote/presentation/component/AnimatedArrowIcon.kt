package com.yangdai.opennote.presentation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate

@Composable
fun AnimatedArrowIcon(
    expended: Boolean
) {
    val rotationDegree by animateFloatAsState(if (expended) 90f else 0f,
        label = ""
    )
    Icon(
        modifier = Modifier.rotate(rotationDegree),
        imageVector = Icons.AutoMirrored.Outlined.ArrowRight,
        contentDescription = "Arrow"
    )
}