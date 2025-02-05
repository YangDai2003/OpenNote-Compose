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

@Composable
fun MaskBox(
    animTime: Long = 650L,
    maskComplete: (MaskAnimModel) -> Unit,
    animFinish: () -> Unit,
    content: @Composable (MaskAnimActive) -> Unit,
) {
    var maskAnimModel by remember { mutableStateOf(MaskAnimModel.EXPEND) }
    var viewBounds by remember { mutableStateOf<Rect?>(null) }
    val paint by remember { mutableStateOf(Paint(Paint.ANTI_ALIAS_FLAG)) }

    var animState by remember { mutableStateOf(AnimState()) }
    var viewScreenshot by remember { mutableStateOf<Bitmap?>(null) }
    DisposableEffect(Unit) {
        onDispose {
            viewScreenshot?.recycle()
        }
    }

    val rootView = LocalView.current.rootView
    val maskAnimActive: MaskAnimActive = clickEvent@{ animModel, x, y ->
        val bitmapBound = viewBounds ?: return@clickEvent
        animState = animState.copy(
            clickX = x,
            clickY = y,
            maskRadius = if (animModel == MaskAnimModel.EXPEND) 0f else hypot(
                rootView.width.toFloat(),
                rootView.height.toFloat()
            )
        )
        maskAnimModel = animModel
        viewScreenshot?.recycle()
        viewScreenshot = createBitmap(
            bitmapBound.width.roundToInt(),
            bitmapBound.height.roundToInt()
        ).applyCanvas {
            translate(-bitmapBound.left, -bitmapBound.top)
            rootView.draw(this)
            maskComplete(animModel)
        }
        ValueAnimator.ofFloat(
            animState.maskRadius,
            if (animModel == MaskAnimModel.EXPEND) hypot(
                rootView.width.toFloat(),
                rootView.height.toFloat()
            )
            else 0f
        ).apply {
            duration = animTime
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                animState = animState.copy(maskRadius = it.animatedValue as Float)
            }
            addListener(onEnd = {
                viewScreenshot?.recycle()
                viewScreenshot = null
                animFinish()
            })
        }.start()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                viewBounds = it.boundsInWindow()
            }
            .drawWithCache {
                onDrawWithContent {
                    clipRect {
                        this@onDrawWithContent.drawContent()
                    }
                    viewScreenshot?.let { bitmap ->
                        with(drawContext.canvas.nativeCanvas) {
                            val layer = saveLayer(null, null)
                            when (maskAnimModel) {
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
