package com.yangdai.opennote.ui.screen

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.navigation.NavHostController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.Executors

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraXScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    if (!hasRequiredPermissions(context)) {
        ActivityCompat.requestPermissions(
            context as Activity, CAMERAX_PERMISSIONS, 0
        )
    }

    val controller = remember { LifecycleCameraController(context) }
    controller.bindToLifecycle(lifecycleOwner)

    val previewView = remember { PreviewView(context) }
    previewView.controller = controller

    val executor = remember { Executors.newSingleThreadExecutor() }

    var textRecognizer: TextRecognizer = remember {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    val locale = context.resources.configuration.locales[0].language

    when (locale) {
        Locale.CHINESE.language -> {
            textRecognizer =
                TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
        }

        Locale.KOREAN.language -> {
            textRecognizer =
                TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
        }

        Locale.JAPANESE.language -> {
            textRecognizer =
                TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
        }
    }

    var text by rememberSaveable {
        mutableStateOf("")
    }
    var isLoading by remember { mutableStateOf(false) }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = text)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

            FilledTonalIconButton(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(32.dp),
                onClick = {
                    navController.navigateUp()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.CloseFullscreen,
                    contentDescription = "Close"
                )
            }

            FilledTonalIconButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(32.dp),
                onClick = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("scannedText", text)
                    navController.navigateUp()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "Done"
                )
            }

            FilledTonalIconButton(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(32.dp),
                onClick = {
                    scope.launch {
                        scaffoldState.bottomSheetState.expand()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.TextFields,
                    contentDescription = "Open Sheet"
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
                IconButton(
                    modifier = Modifier
                        .align(if (orientation == Configuration.ORIENTATION_PORTRAIT) Alignment.BottomCenter else Alignment.CenterEnd)
                        .padding(32.dp)
                        .size(64.dp),
                    onClick = {
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
                                                    if (!task.isSuccessful) task.exception?.localizedMessage.toString()
                                                    else {
                                                        task.result.text
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
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(64.dp),
                        imageVector = Icons.Default.Camera,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "Capture"
                    )
                }
            }
        }
    }
}

fun hasRequiredPermissions(
    context: Context
): Boolean {
    return CAMERAX_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            context,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }
}


val CAMERAX_PERMISSIONS = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO,
)
