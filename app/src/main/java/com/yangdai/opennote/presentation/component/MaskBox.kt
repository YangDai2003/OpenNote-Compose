package com.yangdai.opennote.presentation.component

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import kotlin.math.hypot
import kotlin.math.roundToInt
import androidx.core.graphics.createBitmap

/**
 * 激活遮罩动画，
 * 动画模式，点击的 x坐标，点击的 y坐标
 */
enum class MaskAnimModel {
    EXPEND,
    SHRINK,
}

typealias MaskAnimActive = (MaskAnimModel, Float, Float) -> Unit

@SuppressLint("Recycle")
@Composable
fun MaskBox(
    animTime: Long = 650L,
    maskComplete: (MaskAnimModel) -> Unit,
    animFinish: () -> Unit,
    content: @Composable (MaskAnimActive) -> Unit,
) {
    var maskAnimModel by remember {
        mutableStateOf(MaskAnimModel.EXPEND)
    }
    val paint by remember {
        mutableStateOf(Paint(Paint.ANTI_ALIAS_FLAG))
    }
    var clickX by remember { mutableFloatStateOf(0f) }
    var clickY by remember { mutableFloatStateOf(0f) }
    var viewBounds by remember { mutableStateOf<Rect?>(null) }
    val rootView = LocalView.current.rootView
    var viewScreenshot by remember { mutableStateOf<Bitmap?>(null) }
    var maskRadius by remember { mutableFloatStateOf(0f) }

    val maskAnimActive: MaskAnimActive = clickEvent@{ animModel, x, y ->
        clickX = x
        clickY = y
        val bitmapBound = viewBounds ?: return@clickEvent
        val radiusRange = when (animModel) {
            MaskAnimModel.EXPEND -> Pair(
                0f,
                hypot(rootView.width.toFloat(), rootView.height.toFloat())
            )

            MaskAnimModel.SHRINK -> Pair(
                hypot(
                    rootView.width.toFloat(),
                    rootView.height.toFloat()
                ), 0f
            )
        }
        maskAnimModel = animModel
        viewScreenshot =
            createBitmap(bitmapBound.width.roundToInt(), bitmapBound.height.roundToInt())
            .applyCanvas {
                translate(-bitmapBound.left, -bitmapBound.top)
                rootView.draw(this)
                maskComplete(animModel)
            }
        ValueAnimator.ofFloat(radiusRange.first, radiusRange.second)
            .apply {
                duration = animTime
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { valueAnimator ->
                    maskRadius = valueAnimator.animatedValue as Float
                }
                addListener(onEnd = {
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
                    if (viewScreenshot == null) return@onDrawWithContent
                    with(drawContext.canvas.nativeCanvas) {
                        val layer = saveLayer(null, null)
                        when (maskAnimModel) {
                            MaskAnimModel.EXPEND -> {
                                drawBitmap(viewScreenshot!!, 0f, 0f, null)
                                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                                drawCircle(clickX, clickY, maskRadius, paint)
                            }

                            MaskAnimModel.SHRINK -> {
                                drawCircle(clickX, clickY, maskRadius, paint)
                                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                                drawBitmap(viewScreenshot!!, 0f, 0f, paint)
                            }
                        }
                        paint.xfermode = null
                        restoreToCount(layer)

                    }
                }
            }
    ) {
        content(maskAnimActive)
    }
}

