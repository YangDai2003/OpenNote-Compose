package com.yangdai.opennote.presentation.component

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.core.animation.addListener
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * 激活遮罩动画，
 * 动画模式
 */
enum class MaskAnimModel {
    EXPEND, SHRINK,
}

typealias MaskAnimActive = (MaskAnimModel, Float, Float) -> Unit

@Stable
private data class AnimState(
    val clickX: Float = 0f, val clickY: Float = 0f, val maskRadius: Float = 0f
)

/**
 * A composable function that creates a masking effect, either expanding or shrinking, around a click point.
 *
 * This function provides a visual transition effect where a circular mask either expands from or shrinks
 * towards a specified point on the screen. It captures a screenshot of the current view and then uses
 * this screenshot to create the masking effect.
 *
 * @param animationDurationMillis The duration of the mask animation in milliseconds. Defaults to 650ms.
 * @param onMaskAnimationComplete A callback function that is invoked when the mask animation type has completed the capture
 * process, but the animation is still running. It provides the [MaskAnimModel] representing whether
 * the animation is expanding or shrinking.
 * @param onAnimationFinished A callback function that is invoked when the entire mask animation is finished.
 * This is triggered after the expanding or shrinking animation has completed.
 */
@Composable
fun MaskBox(
    animationDurationMillis: Long = 650L,
    onMaskAnimationComplete: (MaskAnimModel) -> Unit,
    onAnimationFinished: () -> Unit,
    content: @Composable (MaskAnimActive) -> Unit,
) {
    var maskAnimationState by remember { mutableStateOf(MaskAnimModel.EXPEND) }
    var viewRect by remember { mutableStateOf<Rect?>(null) }
    val paint by remember { mutableStateOf(Paint(Paint.ANTI_ALIAS_FLAG)) }

    var animState by remember { mutableStateOf(AnimState()) }
    var screenshotBitmap by remember { mutableStateOf<Bitmap?>(null) }
    DisposableEffect(Unit) {
        onDispose {
            screenshotBitmap?.recycle()
        }
    }

    val windowRootView = LocalView.current.rootView
    val maskAnimActive: MaskAnimActive = clickEvent@{ animModel, clickX, clickY ->
        val viewRect = viewRect ?: return@clickEvent
        animState = animState.copy(
            clickX = clickX,
            clickY = clickY,
            maskRadius = if (animModel == MaskAnimModel.EXPEND) 0f else hypot(
                windowRootView.width.toFloat(),
                windowRootView.height.toFloat()
            )
        )
        maskAnimationState = animModel
        screenshotBitmap?.recycle()
        screenshotBitmap = createBitmap(
            viewRect.width.roundToInt(),
            viewRect.height.roundToInt()
        ).applyCanvas {
            translate(-viewRect.left, -viewRect.top)
            windowRootView.draw(this)
            onMaskAnimationComplete(animModel)
        }
        ValueAnimator.ofFloat(
            animState.maskRadius,
            if (animModel == MaskAnimModel.EXPEND) hypot(
                windowRootView.width.toFloat(),
                windowRootView.height.toFloat()
            )
            else 0f
        ).apply {
            duration = animationDurationMillis
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                animState = animState.copy(maskRadius = it.animatedValue as Float)
            }
            addListener(onEnd = {
                screenshotBitmap?.recycle()
                screenshotBitmap = null
                onAnimationFinished()
            })
        }.start()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                viewRect = it.boundsInWindow()
            }
            .drawWithCache {
                onDrawWithContent {
                    clipRect {
                        this@onDrawWithContent.drawContent()
                    }
                    screenshotBitmap?.let { bitmap ->
                        with(drawContext.canvas.nativeCanvas) {
                            val layer = saveLayer(null, null)
                            when (maskAnimationState) {
                                MaskAnimModel.EXPEND -> {
                                    drawBitmap(bitmap, 0f, 0f, null)
                                    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                                    drawCircle(
                                        animState.clickX,
                                        animState.clickY,
                                        animState.maskRadius,
                                        paint
                                    )
                                }

                                MaskAnimModel.SHRINK -> {
                                    drawCircle(
                                        animState.clickX,
                                        animState.clickY,
                                        animState.maskRadius,
                                        paint
                                    )
                                    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                                    drawBitmap(bitmap, 0f, 0f, paint)
                                }
                            }
                            paint.xfermode = null
                            restoreToCount(layer)

                        }
                    }
                }
            }) {
        content(maskAnimActive)
    }
}
