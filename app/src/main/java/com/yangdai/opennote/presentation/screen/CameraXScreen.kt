package com.yangdai.opennote.presentation.screen

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Done
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.yangdai.opennote.MainActivity
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraXScreen(
    sharedViewModel: SharedViewModel = hiltViewModel(LocalActivity.current as MainActivity),
    navigateUp: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val bottomSheetState =
        rememberStandardBottomSheetState(initialValue = SheetValue.Hidden, skipHiddenState = false)
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(bottomSheetState)

    if (!hasRequiredPermissions(context)) {
        ActivityCompat.requestPermissions(
            context as Activity, CAMERAX_PERMISSIONS, 0
        )
    }

    BackHandler(bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
        scope.launch {
            bottomSheetScaffoldState.bottomSheetState.hide()
        }
    }

    var flashMode by rememberSaveable {
        mutableIntStateOf(ImageCapture.FLASH_MODE_OFF)
    }

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            bindToLifecycle(lifecycleOwner)
            imageCaptureFlashMode = flashMode
        }
    }

    LaunchedEffect(flashMode) {
        cameraController.imageCaptureFlashMode = flashMode
    }

    val executor = remember { Executors.newSingleThreadExecutor() }

    var scannedText by rememberSaveable { mutableStateOf("") }

    var isLoading by rememberSaveable { mutableStateOf(false) }

    fun processImage(image: InputImage) {

        isLoading = true

        sharedViewModel.textRecognizer.process(image)
            .addOnCompleteListener { task ->

                isLoading = false

                scannedText =
                    if (!task.isSuccessful) {
                        val msg = task.exception?.localizedMessage.toString()
                        Toast.makeText(
                            context,
                            msg,
                            Toast.LENGTH_LONG
                        ).show()
                        ""
                    } else {
                        val text = task.result.text
                        if (text.isEmpty()) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.no_text_found),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        text
                    }

                if (scannedText.isNotEmpty())
                    scope.launch {
                        bottomSheetScaffoldState.bottomSheetState.expand()
                    }
            }
    }

    // Registers a photo picker activity launcher in single-select mode.
    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                processImage(InputImage.fromFilePath(context, uri))
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetPeekHeight = 0.dp,
        sheetSwipeEnabled = false,
        sheetDragHandle = {},
        sheetContent = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .imePadding()
                    .padding(horizontal = 12.dp)
            ) {
                if (!isLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            scope.launch {
                                keyboardController?.hide()
                                bottomSheetScaffoldState.bottomSheetState.hide()
                            }
                        }) {
                            Icon(imageVector = Icons.Outlined.Close, contentDescription = "Cancel")
                        }
                        IconButton(onClick = {
                            sharedViewModel.scannedTextStateFlow.value = scannedText
                            navigateUp()
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Done,
                                contentDescription = "Confirm"
                            )
                        }
                    }
                }

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                    value = scannedText,
                    onValueChange = { scannedText = it })
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            AndroidView(
                factory = {
                    PreviewView(it).apply {
                        controller = cameraController
                        scaleType = PreviewView.ScaleType.FIT_CENTER
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            FilledTonalIconButton(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 32.dp, top = 32.dp),
                onClick = navigateUp
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back)
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

            if (scannedText.isNotEmpty()) {
                FilledTonalIconButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(32.dp),
                    onClick = {
                        scope.launch {
                            bottomSheetScaffoldState.bottomSheetState.expand()
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
                val orientation = LocalConfiguration.current.orientation

                val modifier = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    Modifier
                        .padding(bottom = 24.dp)
                        .navigationBarsPadding()
                        .align(Alignment.BottomCenter)
                } else {
                    Modifier
                        .padding(end = 24.dp)
                        .align(Alignment.CenterEnd)
                }

                // 由于IconButton被限制了大小，所以直接使用Icon
                Icon(
                    modifier = Modifier
                        .then(modifier)
                        .size(64.dp)
                        .clickable {
                            cameraController.takePicture(
                                executor,
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                        super.onCaptureSuccess(imageProxy)

                                        imageProxy.image?.let { image ->
                                            processImage(
                                                InputImage.fromMediaImage(
                                                    image,
                                                    imageProxy.imageInfo.rotationDegrees
                                                )
                                            )
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
