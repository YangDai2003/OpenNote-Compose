package com.yangdai.opennote.presentation.component.image

import android.app.DownloadManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.HdrOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ZoomableImage(
    painter: Painter, contentDescription: String?
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var rotation by remember { mutableFloatStateOf(0f) }

    // 添加一个记录图片尺寸的状态
    var imageSize by remember { mutableStateOf(Size.Zero) }
    var containerSize by remember { mutableStateOf(Size.Zero) }

    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale = (scale * zoomChange).coerceIn(1f, 3f)
        // 根据缩放比例调整偏移量
        val adjustedOffset = offsetChange * scale
        val maxX = (imageSize.width * scale - containerSize.width) / 2
        val maxY = (imageSize.height * scale - containerSize.height) / 2
        offset = Offset(
            x = (offset.x + adjustedOffset.x).coerceIn(-maxX, maxX),
            y = (offset.y + adjustedOffset.y).coerceIn(-maxY, maxY)
        )
        rotation += rotationChange
    }

    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = Size(it.width.toFloat(), it.height.toFloat()) }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                rotationZ = rotation,
                translationX = offset.x,
                translationY = offset.y
            )
            .transformable(state = state)
            .onSizeChanged { imageSize = Size(it.width.toFloat(), it.height.toFloat()) }
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = {
                    // 长按复位
                    scale = 1f
                    offset = Offset.Zero
                    rotation = 0f
                }, onDoubleTap = { tapOffset ->
                    val targetScale = if (scale > 1f) 1f else 2f

                    // 计算双击点相对于容器中心的偏移
                    val containerCenter = Offset(
                        containerSize.width / 2f, containerSize.height / 2f
                    )

                    // 计算双击点在当前缩放下相对于图片原点的位置
                    val currentPointInImage = (tapOffset - containerCenter - offset) / scale

                    // 计算新的偏移量，保持双击点在缩放后的相同位置
                    val newOffset = if (targetScale > 1f) {
                        val targetPointInContainer =
                            containerCenter + currentPointInImage * targetScale
                        val newOffset = tapOffset - targetPointInContainer

                        // 应用边界限制
                        val maxX = (imageSize.width * targetScale - containerSize.width) / 2
                        val maxY = (imageSize.height * targetScale - containerSize.height) / 2
                        Offset(
                            x = newOffset.x.coerceIn(-maxX, maxX),
                            y = newOffset.y.coerceIn(-maxY, maxY)
                        )
                    } else {
                        Offset.Zero
                    }

                    scale = targetScale
                    offset = newOffset
                })
            })
}

@Composable
fun FullscreenImageDialog(
    imageUrl: String, onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = viewModel<ImageViewModel>()
    val imageState by viewModel.imageState.collectAsState()
    val activity = LocalActivity.current

    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(
            usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = true
        )
    ) {

        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.apply {
                setDimAmount(0.5f)
                addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                attributes.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        it.blurBehindRadius = 16
                    }
                    attributes = it
                }
            }
        }
        DisposableEffect(imageUrl) {
            viewModel.loadImage(context.applicationContext, imageUrl)
            onDispose {
                viewModel.clearCache(context.applicationContext)
                activity?.window?.colorMode = ActivityInfo.COLOR_MODE_DEFAULT
                dialogWindow?.colorMode = ActivityInfo.COLOR_MODE_DEFAULT
            }
        }
        var isHdr by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = imageState) {
                is ImageState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ImageState.Success -> {
                    val bitmap = rememberBitmap(
                        context.applicationContext, state.imagePath, state.isPath
                    )

                    bitmap?.let {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            if (bitmap.hasGainmap()) {
                                isHdr = true
                                activity?.window?.colorMode = ActivityInfo.COLOR_MODE_HDR
                                dialogWindow?.colorMode = ActivityInfo.COLOR_MODE_HDR
                            }
                        }
                    }

                    DisposableEffect(Unit) {
                        onDispose {
                            activity?.window?.colorMode = ActivityInfo.COLOR_MODE_DEFAULT
                            dialogWindow?.colorMode = ActivityInfo.COLOR_MODE_DEFAULT
                        }
                    }

                    val painter = remember(bitmap) {
                        bitmap?.asImageBitmap()?.let {
                            BitmapPainter(it)
                        } ?: ColorPainter(Color.Transparent)
                    }
                    ZoomableImage(painter = painter, contentDescription = null)
                    if (isHdr) {
                        Icon(
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp),
                            imageVector = Icons.Outlined.HdrOn,
                            tint = Color.White,
                            contentDescription = "HDR"
                        )
                    }
                    if (!state.isLocalFile) {
                        FilledTonalIconButton(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 16.dp),
                            onClick = {
                                downloadImage(context.applicationContext, imageUrl)
                            }) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Image"
                            )
                        }
                    }
                }

                is ImageState.Error -> onDismiss()

                is ImageState.Empty -> {
                    // 显示占位内容
                }
            }
            FilledTonalIconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }
    }
}

private fun downloadImage(context: Context, url: String) {
    try {
        val request = DownloadManager.Request(url.toUri()).setTitle("🖼️")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, "Image_${System.currentTimeMillis()}_OpenNote"
            ).setAllowedOverMetered(true).setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun rememberBitmap(context: Context, imageUrl: String, isPath: Boolean): Bitmap? {
    return remember(imageUrl) {
        if (isPath) BitmapFactory.decodeFile(imageUrl)
        else
            context.contentResolver.openInputStream(imageUrl.toUri())?.use {
                BitmapFactory.decodeStream(it)
            }
    }
}
