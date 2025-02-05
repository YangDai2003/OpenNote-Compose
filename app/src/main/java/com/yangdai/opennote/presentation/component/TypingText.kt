package com.yangdai.opennote.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle

@Composable
fun TypingText(
    text: String,
    modifier: Modifier = Modifier,
    spec: AnimationSpec<Int> = tween(durationMillis = text.length * 80, easing = LinearEasing),
    style: TextStyle = LocalTextStyle.current,
    preoccupySpace: Boolean = true
) {
    var textToAnimate by remember { mutableStateOf("") }
    val index = remember {
        Animatable(initialValue = 0, typeConverter = Int.VectorConverter)
    }

    LaunchedEffect(text) {
        index.snapTo(0)
        textToAnimate = text
        index.animateTo(text.length, spec)
    }

    Box(modifier = modifier) {
        if (preoccupySpace && index.isRunning) {
            Text(
                text = text,
                style = style,
                modifier = Modifier.alpha(0f)
            )
        }

        Text(
            text = textToAnimate.substring(0, index.value) + if (index.isRunning) " |" else "",
            style = style
        )
    }
}
