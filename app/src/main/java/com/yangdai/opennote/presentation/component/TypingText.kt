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

/**
 * A composable function that displays text with a typing animation effect.
 *
 * This composable simulates the effect of text being typed out character by character.
 * It achieves this by animating an index that controls how many characters of the target
 * text are displayed at any given time. The animation can be customized using
 * [animationSpec], and the text's appearance can be modified with [style].
 *
 * @param text The text to be displayed with the typing effect.
 * @param modifier Modifiers to be applied to the root layout (Box) of this composable.
 * @param animationSpec The animation specification for controlling the typing speed and easing.
 *                      Defaults to a linear animation that takes 80 milliseconds per character.
 * @param style The text style to be applied to the displayed text. Defaults to the current
 *              [LocalTextStyle].
 * @param reserveSpace Whether to reserve space for the full text even during the animation.
 *                     If true, a transparent version of the full text is rendered in the background,
 *                     ensuring that the layout doesn't shift as the text is typed. Defaults to true.
 *
 * Example Usage:
 * ```
 * TypingText(
 *     text = "Hello, World!",
 *     modifier = Modifier.padding(16.dp),
 *     animationSpec = tween(durationMillis = 5000, easing = LinearEasing), // Slower animation
 */
@Composable
fun TypingText(
    text: String,
    modifier: Modifier = Modifier,
    animationSpec: AnimationSpec<Int> = tween(durationMillis = text.length * 80, easing = LinearEasing),
    style: TextStyle = LocalTextStyle.current,
    reserveSpace: Boolean = true
) {
    var animatedText by remember { mutableStateOf("") }
    val index = remember {
        Animatable(initialValue = 0, typeConverter = Int.VectorConverter)
    }

    LaunchedEffect(text) {
        index.snapTo(0)
        animatedText = text
        index.animateTo(text.length, animationSpec)
    }

    Box(modifier = modifier) {
        if (reserveSpace && index.isRunning) {
            Text(
                text = text,
                style = style,
                modifier = Modifier.alpha(0f)
            )
        }

        Text(
            text = animatedText.substring(0, index.value) + if (index.isRunning) " |" else "",
            style = style
        )
    }
}
