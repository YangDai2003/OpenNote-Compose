package com.yangdai.opennote.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Brush.Companion.linearGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

@Preview
@Composable
fun LoginButtonPreview() {
    LoginButton(
        onClick = {},
        content = {
            Text("Login")
        }
    )
}

@Composable
fun LoginButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = CircleShape,
    borderColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .defaultMinSize(minWidth = 80.dp, minHeight = 48.dp)
            .clickable(
                enabled = enabled,
                indication = NeonIndication(shape, 2.dp),
                interactionSource = interactionSource,
                onClick = onClick
            )
            .border(width = 2.dp, color = borderColor, shape = shape)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

data class NeonIndication(private val shape: Shape, private val borderWidth: Dp) :
    IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return NeonNode(
            shape,
            // Double the border size for a stronger press effect
            borderWidth * 2,
            interactionSource
        )
    }
}

private class NeonNode(
    private val shape: Shape,
    private val borderWidth: Dp,
    private val interactionSource: InteractionSource
) : Modifier.Node(), DrawModifierNode {
    var currentPressPosition: Offset = Offset.Zero
    val animatedProgress = Animatable(0f)
    val animatedPressAlpha = Animatable(1f)

    var pressedAnimation: Job? = null
    var restingAnimation: Job? = null


    private fun animateToPressed(pressPosition: Offset) {
        // Finish any existing animations, in case of a new press while we are still showing
        // an animation for a previous one
        restingAnimation?.cancel()
        pressedAnimation?.cancel()
        pressedAnimation = coroutineScope.launch {
            currentPressPosition = pressPosition
            animatedPressAlpha.snapTo(1f)
            animatedProgress.snapTo(0f)
            animatedProgress.animateTo(1f, tween(1500))
        }
    }

    private fun animateToResting() {
        restingAnimation = coroutineScope.launch {
            // Wait for the existing press animation to finish if it is still ongoing
            pressedAnimation?.join()
            animatedPressAlpha.animateTo(0f, tween(250))
            animatedProgress.snapTo(0f)
        }
    }

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> animateToPressed(interaction.pressPosition)
                    is PressInteraction.Release -> animateToResting()
                    is PressInteraction.Cancel -> animateToResting()
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        val (startPosition, endPosition) = calculateGradientStartAndEndFromPressPosition(
            currentPressPosition, size
        )
        val brush = animateBrush(
            startPosition = startPosition,
            endPosition = endPosition,
            progress = animatedProgress.value
        )
        val alpha = animatedPressAlpha.value

        drawContent()

        val outline = shape.createOutline(size, layoutDirection, this)
        // Draw overlay on top of content
        drawOutline(
            outline = outline,
            brush = brush,
            alpha = alpha * 0.1f
        )
        // Draw border on top of overlay
        drawOutline(
            outline = outline,
            brush = brush,
            alpha = alpha,
            style = Stroke(width = borderWidth.toPx())
        )
    }

    /**
     * Calculates a gradient start / end where start is the point on the bounding rectangle of
     * size [size] that intercepts with the line drawn from the center to [pressPosition],
     * and end is the intercept on the opposite end of that line.
     */
    private fun calculateGradientStartAndEndFromPressPosition(
        pressPosition: Offset,
        size: Size
    ): Pair<Offset, Offset> {
        // Convert to offset from the center
        val offset = pressPosition - size.center
        // y = mx + c, c is 0, so just test for x and y to see where the intercept is
        val gradient = offset.y / offset.x
        // We are starting from the center, so halve the width and height - convert the sign
        // to match the offset
        val width = (size.width / 2f) * sign(offset.x)
        val height = (size.height / 2f) * sign(offset.y)
        val x = height / gradient
        val y = gradient * width

        // Figure out which intercept lies within bounds
        val intercept = if (abs(y) <= abs(height)) {
            Offset(width, y)
        } else {
            Offset(x, height)
        }

        // Convert back to offsets from 0,0
        val start = intercept + size.center
        val end = Offset(size.width - start.x, size.height - start.y)
        return start to end
    }

    private fun animateBrush(
        startPosition: Offset,
        endPosition: Offset,
        progress: Float
    ): Brush {
        if (progress == 0f) return TransparentBrush

        val colorStops = buildList {
            when {
                progress < 1 / 6f -> {
                    val adjustedProgress = progress * 6f
                    add(0f to Blue)
                    add(adjustedProgress to Color.Transparent)
                }

                progress < 2 / 6f -> {
                    val adjustedProgress = (progress - 1 / 6f) * 6f
                    add(0f to Purple)
                    add(adjustedProgress * MAXBLUESTOP to Blue)
                    add(adjustedProgress to Blue)
                    add(1f to Color.Transparent)
                }

                progress < 3 / 6f -> {
                    val adjustedProgress = (progress - 2 / 6f) * 6f
                    add(0f to Pink)
                    add(adjustedProgress * MAXPURPLESTOP to Purple)
                    add(MAXBLUESTOP to Blue)
                    add(1f to Blue)
                }

                progress < 4 / 6f -> {
                    val adjustedProgress = (progress - 3 / 6f) * 6f
                    add(0f to Orange)
                    add(adjustedProgress * MAXPINKSTOP to Pink)
                    add(MAXPURPLESTOP to Purple)
                    add(MAXBLUESTOP to Blue)
                    add(1f to Blue)
                }

                progress < 5 / 6f -> {
                    val adjustedProgress = (progress - 4 / 6f) * 6f
                    add(0f to Yellow)
                    add(adjustedProgress * MAXORANGESTOP to Orange)
                    add(MAXPINKSTOP to Pink)
                    add(MAXPURPLESTOP to Purple)
                    add(MAXBLUESTOP to Blue)
                    add(1f to Blue)
                }

                else -> {
                    val adjustedProgress = (progress - 5 / 6f) * 6f
                    add(0f to Yellow)
                    add(adjustedProgress * MAXYELLOWSTOP to Yellow)
                    add(MAXORANGESTOP to Orange)
                    add(MAXPINKSTOP to Pink)
                    add(MAXPURPLESTOP to Purple)
                    add(MAXBLUESTOP to Blue)
                    add(1f to Blue)
                }
            }
        }

        return linearGradient(
            colorStops = colorStops.toTypedArray(),
            start = startPosition,
            end = endPosition
        )
    }

    companion object {
        val TransparentBrush = SolidColor(Color.Transparent)
        val Blue = Color(0xFF4285F4)
        val Purple = Color(0xFF9B72CB)
        val Pink = Color(0xFFD96570)
        val Orange = Color(0xFFBF360C)
        val Yellow = Color(0xFFEF6C00)
        const val MAXYELLOWSTOP = 0.12f
        const val MAXORANGESTOP = 0.24f
        const val MAXPINKSTOP = 0.48f
        const val MAXPURPLESTOP = 0.67f
        const val MAXBLUESTOP = 0.83f
    }
}
