package com.yangdai.opennote.presentation.screen

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.DoneOutline
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.yangdai.opennote.R
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.Executors

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraXScreen(
    onCloseClick: () -> Unit,
    onDoneClick: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val bottomSheetState =
        rememberStandardBottomSheetState(initialValue = SheetValue.Hidden, skipHiddenState = false)
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState)

    if (!hasRequiredPermissions(context)) {
        ActivityCompat.requestPermissions(
            context as Activity, CAMERAX_PERMISSIONS, 0
        )
    }

    BackHandler(scaffoldState.bottomSheetState.isVisible) {
        scope.launch {
            scaffoldState.bottomSheetState.hide()
        }
    }

    var flashMode by rememberSaveable {
        mutableIntStateOf(ImageCapture.FLASH_MODE_OFF)
    }

    val controller = remember { LifecycleCameraController(context) }
    controller.bindToLifecycle(lifecycleOwner)
    controller.imageCaptureFlashMode = flashMode

    val previewView = remember { PreviewView(context) }
    previewView.controller = controller
    previewView.scaleType = PreviewView.ScaleType.FIT_CENTER

    val executor = remember { Executors.newSingleThreadExecutor() }

    var textRecognizer: TextRecognizer = remember {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    val language = context.resources.configuration.locales[0].language

    if (language == Locale.CHINESE.language) {
        textRecognizer =
            TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    }

    var text by rememberSaveable { mutableStateOf("") }

    var isLoading by rememberSaveable { mutableStateOf(false) }

    // Registers a photo picker activity launcher in single-select mode.
    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                isLoading = true
                textRecognizer.process(InputImage.fromFilePath(context, uri))
                    .addOnCompleteListener { task ->
                        isLoading = false
                        text =
                            if (!task.isSuccessful) {
                                val msg =
                                    task.exception?.localizedMessage.toString()
                                Toast.makeText(
                                    context,
                                    msg,
                                    Toast.LENGTH_LONG
                                ).show()
                                ""
                            } else {
                                val scannedText = task.result.text
                                if (scannedText.isEmpty()) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.no_text_found),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                scannedText
                            }
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    }
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetDragHandle = {},
        sheetContent = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(horizontal = 12.dp)
            ) {
                if (text.isNotEmpty() && !isLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            scope.launch {
                                scaffoldState.bottomSheetState.hide()
                            }
                        }) {
                            Icon(imageVector = Icons.Outlined.Cancel, contentDescription = "Cancel")
                        }
                        IconButton(onClick = { onDoneClick(text) }) {
                            Icon(
                                imageVector = Icons.Outlined.DoneOutline,
                                contentDescription = "Confirm"
                            )
                        }
                    }
                }

                if (text.isNotEmpty()) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding(),
                        value = text,
                        onValueChange = { text = it })
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            FilledTonalIconButton(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 32.dp, top = 32.dp),
                onClick = onCloseClick
            ) {
                Icon(
                    imageVector = Icons.Default.CloseFullscreen,
                    contentDescription = "Close"
                )
            }

            FilledTonalIconButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(end = 32.dp, top = 32.dp),
                onClick = {
                    when (flashMode) {
                        ImageCapture.FLASH_MODE_OFF -> {
                            flashMode = ImageCapture.FLASH_MODE_ON
                        }

                        ImageCapture.FLASH_MODE_ON -> {
                            flashMode = ImageCapture.FLASH_MODE_AUTO
                        }

                        ImageCapture.FLASH_MODE_AUTO -> {
                            flashMode = ImageCapture.FLASH_MODE_OFF
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = when (flashMode) {
                        ImageCapture.FLASH_MODE_OFF -> Icons.Default.FlashOff
                        ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                        ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                        else -> Icons.Default.FlashOff
                    },
                    contentDescription = "Flash"
                )
            }

            if (text.isNotEmpty()) {
                FilledTonalIconButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(32.dp),
                    onClick = {
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.TextFields,
                        contentDescription = "Show Text"
                    )
                }
            }

            FilledTonalIconButton(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(32.dp),
                onClick = {
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Open Photo Picker"
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.Center)
                )
            } else {
                val orientation = context.resources.configuration.orientation

                val modifier = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    Modifier.padding(bottom = 24.dp).navigationBarsPadding().align(Alignment.BottomCenter)
                } else {
                    Modifier.padding(end = 24.dp).align(Alignment.CenterEnd)
                }

                // 由于IconButton被限制了大小，所以直接使用Icon
                Icon(
                    modifier = Modifier.then(modifier).size(64.dp).clickable {
                        controller.takePicture(
                            executor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                    super.onCaptureSuccess(imageProxy)

                                    isLoading = true

                                    imageProxy.image?.let { image ->
                                        val inputImage = InputImage.fromMediaImage(
                                            image,
                                            imageProxy.imageInfo.rotationDegrees
                                        )
                                        textRecognizer.process(inputImage)
                                            .addOnCompleteListener { task ->
                                                isLoading = false
                                                text =
                                                    if (!task.isSuccessful) {
                                                        val msg =
                                                            task.exception?.localizedMessage.toString()
                                                        Toast.makeText(
                                                            context,
                                                            msg,
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                        ""
                                                    } else {
                                                        val scannedText = task.result.text
                                                        if (scannedText.isEmpty()) {
                                                            Toast.makeText(
                                                                context,
                                                                context.getString(R.string.no_text_found),
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        }
                                                        scannedText
                                                    }
                                                scope.launch {
                                                    scaffoldState.bottomSheetState.expand()
                                                }
                                            }
                                    }
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    super.onError(exception)
                                    Log.e("Camera", "Couldn't take photo: ", exception)
                                }
                            }
                        )
                    },
                    imageVector = Icons.Default.Camera,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Capture"
                )
            }
        }
    }
}

fun hasRequiredPermissions(
    context: Context
): Boolean {
    return CAMERAX_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}


val CAMERAX_PERMISSIONS = arrayOf(
    Manifest.permission.CAMERA
)
