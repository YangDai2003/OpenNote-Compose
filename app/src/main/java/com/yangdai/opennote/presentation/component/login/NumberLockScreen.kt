package com.yangdai.opennote.presentation.component.login

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R
import kotlin.math.PI
import kotlin.math.sin

@PreviewScreenSizes
@Composable
fun NumberLockScreenPreview() {
    NumberLockScreen()
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun NumberLockScreen(
    storedPassword: String = "1234",
    isBiometricAuthEnabled: Boolean = false,
    isCreatingPassword: Boolean = false,
    onCreatingCanceled: () -> Unit = {},
    onPassCreated: (String) -> Unit = {},
    onFingerprintClick: () -> Unit = {},
    onAuthenticated: () -> Unit = {}
) {

    // 判断系统版本是否大于android 12
    val modifier: Modifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.background(MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.25f))
    } else {
        Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {}
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val isLandscape = screenWidth > screenHeight

        val buttonSize = if (isLandscape) {
            minOf(screenHeight.times(0.15f), 80.dp)
        } else {
            minOf(screenWidth.times(0.2f), 80.dp)
        }

        val padding = buttonSize.times(0.25f)

        val hapticFeedback = LocalHapticFeedback.current
        var inputPassword by remember { mutableStateOf("") }
        var inputPassword2 by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }

        val animatedProgress by animateFloatAsState(
            targetValue = if (isError) 1f else 0f,
            animationSpec = repeatable(
                iterations = 2,
                animation = tween(200),
                repeatMode = RepeatMode.Reverse
            ),
            finishedListener = {
                isError = false
                inputPassword = ""
                inputPassword2 = ""
            },
            label = "shakeProgress"
        )

        val offsetX = remember(animatedProgress) {
            if (isError) {
                sin(animatedProgress * 2 * PI.toFloat()) * 30f
            } else {
                0f
            }
        }

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = padding),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isCreatingPassword) {
                        if (inputPassword.length < 4)
                            Text(
                                stringResource(R.string.create_password),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        else
                            Text(
                                stringResource(R.string.enter_again),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                    } else LogoText()
                    Spacer(modifier = Modifier.height(48.dp))
                    PasswordCircles(
                        modifier = Modifier.graphicsLayer { translationX = offsetX },
                        passwordLength = if (isCreatingPassword) {
                            if (inputPassword.length < 4) inputPassword.length else inputPassword2.length
                        } else inputPassword.length,
                        isError = isError
                    )
                    if (isCreatingPassword) {
                        Spacer(modifier = Modifier.height(padding))
                        TextButton(
                            onClick = onCreatingCanceled
                        ) {
                            Text(stringResource(android.R.string.cancel))
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    NumberPad(
                        biometricAuthEnabled = isBiometricAuthEnabled,
                        size = buttonSize,
                        padding = padding,
                        onNumberClick = { number ->
                            if (isCreatingPassword)
                                if (inputPassword.length < 4) {
                                    inputPassword += number
                                } else {
                                    if (inputPassword2.length < 4) {
                                        inputPassword2 += number
                                        if (inputPassword2.length == 4) {
                                            if (inputPassword == inputPassword2) {
                                                onPassCreated(inputPassword)
                                            } else {
                                                hapticFeedback.performHapticFeedback(
                                                    HapticFeedbackType.Reject
                                                )
                                                isError = true
                                            }
                                        }
                                    }
                                }
                            else
                                if (inputPassword.length < 4) {
                                    inputPassword += number
                                    if (inputPassword.length == 4) {
                                        if (inputPassword == storedPassword) {
                                            onAuthenticated()
                                        } else {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
                                            isError = true
                                        }
                                    }
                                }
                        },
                        onDeleteClick = {
                            if (isCreatingPassword) {
                                if (inputPassword.length < 4 && inputPassword.isNotEmpty())
                                    inputPassword = inputPassword.dropLast(1)
                                else
                                    if (inputPassword2.isNotEmpty()) {
                                        inputPassword2 = inputPassword2.dropLast(1)
                                    }
                            } else {
                                if (inputPassword.isNotEmpty()) {
                                    inputPassword = inputPassword.dropLast(1)
                                }
                            }
                        },
                        onFingerprintClick = onFingerprintClick
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Section: LogoText (Centered Vertically)
                Column(
                    modifier = Modifier
                        .weight(1f) // Take up the top portion of the screen
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isCreatingPassword) {
                        if (inputPassword.length < 4)
                            Text(
                                stringResource(R.string.create_password),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        else
                            Text(
                                stringResource(R.string.enter_again),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                    } else LogoText()
                    Spacer(modifier = Modifier.height(48.dp))
                    PasswordCircles(
                        modifier = Modifier.graphicsLayer { translationX = offsetX },
                        passwordLength = if (isCreatingPassword) {
                            if (inputPassword.length < 4) inputPassword.length else inputPassword2.length
                        } else inputPassword.length,
                        isError = isError
                    )
                }

                // Bottom Section: NumberPad (Aligned to the Bottom)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    NumberPad(
                        biometricAuthEnabled = isBiometricAuthEnabled,
                        size = buttonSize,
                        padding = padding,
                        onNumberClick = { number ->
                            if (isCreatingPassword)
                                if (inputPassword.length < 4) {
                                    inputPassword += number
                                } else {
                                    if (inputPassword2.length < 4) {
                                        inputPassword2 += number
                                        if (inputPassword2.length == 4) {
                                            if (inputPassword == inputPassword2) {
                                                onPassCreated(inputPassword)
                                            } else {
                                                hapticFeedback.performHapticFeedback(
                                                    HapticFeedbackType.Reject
                                                )
                                                isError = true
                                            }
                                        }
                                    }
                                }
                            else
                                if (inputPassword.length < 4) {
                                    inputPassword += number
                                    if (inputPassword.length == 4) {
                                        if (inputPassword == storedPassword) {
                                            onAuthenticated()
                                        } else {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
                                            isError = true
                                        }
                                    }
                                }
                        },
                        onDeleteClick = {
                            if (isCreatingPassword) {
                                if (inputPassword.length < 4 && inputPassword.isNotEmpty())
                                    inputPassword = inputPassword.dropLast(1)
                                else
                                    if (inputPassword2.isNotEmpty()) {
                                        inputPassword2 = inputPassword2.dropLast(1)
                                    }
                            } else {
                                if (inputPassword.isNotEmpty()) {
                                    inputPassword = inputPassword.dropLast(1)
                                }
                            }
                        },
                        onFingerprintClick = onFingerprintClick
                    )
                    if (isCreatingPassword) {
                        Spacer(modifier = Modifier.height(padding))
                        TextButton(
                            onClick = onCreatingCanceled
                        ) {
                            Text(stringResource(android.R.string.cancel))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordCircles(
    modifier: Modifier = Modifier,
    passwordLength: Int,
    isError: Boolean
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(4) { index ->
            Circle(
                isFilled = index < passwordLength,
                isError = isError
            )
        }
    }
}

@Composable
fun Circle(
    isFilled: Boolean,
    isError: Boolean
) {
    val circleColor by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.errorContainer
            isFilled -> MaterialTheme.colorScheme.primaryContainer
            else -> Color.Transparent
        },
        label = "circleColor"
    )

    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(circleColor)
            .border(
                2.dp,
                if (isError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary,
                CircleShape
            )
    )
}

@Composable
fun NumberPad(
    size: Dp,
    padding: Dp,
    biometricAuthEnabled: Boolean,
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onFingerprintClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val numbers = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"))
        numbers.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(padding, Alignment.CenterHorizontally)
            ) {
                row.forEach { number ->
                    NumberButton(size, number, onNumberClick)
                }
            }
            Spacer(modifier = Modifier.height(padding))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(padding, Alignment.CenterHorizontally)
        ) {
            if (biometricAuthEnabled)
                IconButton(
                    size,
                    30.dp,
                    imageVector = Icons.Filled.Fingerprint,
                    onClick = onFingerprintClick
                )
            else EmptyButton(size)
            NumberButton(size, "0", onNumberClick)
            IconButton(
                size,
                24.dp,
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                onClick = onDeleteClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NumberButton(
    size: Dp,
    number: String,
    onNumberClick: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val hapticFeedback = LocalHapticFeedback.current

    val animatedColor by animateColorAsState(
        targetValue = if (isPressed) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceBright,
        label = ""
    )

    TextButton(
        modifier = Modifier.size(size),
        shapes = ButtonShapes(shape = CircleShape, pressedShape = RoundedCornerShape(16.dp)),
        colors = ButtonDefaults.textButtonColors(containerColor = animatedColor),
        interactionSource = interactionSource,
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
            onNumberClick(number)
        }
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun EmptyButton(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {}
}

@Composable
fun IconButton(
    size: Dp,
    iconSize: Dp,
    imageVector: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val hapticFeedback = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        label = ""
    )

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.VirtualKey)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.size(iconSize)
        )
    }
}
