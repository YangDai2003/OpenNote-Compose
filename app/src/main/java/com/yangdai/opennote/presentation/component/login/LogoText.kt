package com.yangdai.opennote.presentation.component.login

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.yangdai.opennote.R
import kotlinx.coroutines.delay

@Composable
fun LogoText() {

    val animationDurationMillis = 2500
    val animationDelayMillis = 5000L

    val animatable = remember { Animatable(-1f) }

    AnimatedGradientText(text = stringResource(id = R.string.app_name), offsetX = animatable.value)

    LaunchedEffect(Unit) {
        while (true) {
            animatable.stop()
            animatable.snapTo(-1f)
            animatable.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = animationDurationMillis,
                    easing = CubicBezierEasing(0.3f, 0f, 0.4f, 1f),
                )
            )
            delay(animationDelayMillis)
        }
    }
}

@Composable
fun AnimatedGradientText(
    modifier: Modifier = Modifier,
    text: String,
    textStyle: TextStyle = MaterialTheme.typography.displayMedium,
    textAlign: TextAlign = TextAlign.Center,
    offsetX: Float,
    gradientColors: List<Color> = colors,
    gradientStops: List<Float> = stops,
    gradientAngle: Double = -16.0,
    gradientXScale: Float = 4f
) {
    val brush =
        remember(gradientXScale, offsetX, gradientAngle, gradientColors, gradientStops) {
            GradientBrush(
                angleDegrees = gradientAngle,
                colors = gradientColors,
                stops = gradientStops,
                scaleX = gradientXScale,
                offset = Offset(offsetX, 0f),
            )
        }

    Text(
        modifier = modifier,
        text = text,
        textAlign = textAlign,
        style = textStyle.copy(brush = brush)
    )
}

private val brandColor1 = Color(0xFF4285F4)
private val brandColor2 = Color(0xFF9B72CB)
private val brandColor3 = Color(0xFFD96570)

private val colors = listOf(
    brandColor1,
    brandColor2,
    brandColor3,
    brandColor3,
    brandColor2,
    brandColor1,
    brandColor2,
    brandColor3,
    Color.Transparent,
    Color.Transparent
)
private val stops = listOf(0f, .09f, .2f, .24f, .35f, .44f, .5f, .56f, .75f, 1f)
