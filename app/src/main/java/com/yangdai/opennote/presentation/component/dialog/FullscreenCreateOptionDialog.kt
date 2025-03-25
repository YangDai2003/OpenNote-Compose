package com.yangdai.opennote.presentation.component.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.yangdai.opennote.R

enum class ActionType {
    CreateTextFile,
    EditTextFile,
    CreateNote,
    SampleNote
}

@Composable
fun FullscreenCreateOptionDialog(
    onDismiss: () -> Unit,
    onOptionSelected: (ActionType) -> Unit
) {

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = true
        )
    ) {

        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window

        SideEffect {
            dialogWindow.let { window ->
                window?.setWindowAnimations(-1)
            }
        }

        var graphicVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { graphicVisible = true }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalAlignment = Alignment.End
            ) {
                AnimatedVisibility(
                    visible = graphicVisible,
                    enter = EnterTransition.None,
                    exit = ExitTransition.None
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        FilledTonalButton(
                            modifier = Modifier.animateEnterExit(
                                enter = fadeIn(tween(delayMillis = 150)) + scaleIn(tween(delayMillis = 150)),
                                exit = ExitTransition.None
                            ),
                            colors = ButtonDefaults.filledTonalButtonColors()
                                .copy(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                            onClick = { onOptionSelected(ActionType.CreateTextFile) }
                        ) {
                            Text(text = stringResource(R.string.create_text_file))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        FilledTonalButton(
                            modifier = Modifier.animateEnterExit(
                                enter = fadeIn(tween(delayMillis = 100)) + scaleIn(tween(delayMillis = 100)),
                                exit = ExitTransition.None
                            ),
                            colors = ButtonDefaults.filledTonalButtonColors()
                                .copy(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                            onClick = { onOptionSelected(ActionType.EditTextFile) }
                        ) {
                            Text(text = stringResource(R.string.edit_text_file))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        FilledTonalButton(
                            modifier = Modifier.animateEnterExit(
                                enter = fadeIn(tween(delayMillis = 50)) + scaleIn(tween(delayMillis = 50)),
                                exit = ExitTransition.None
                            ),
                            colors = ButtonDefaults.filledTonalButtonColors()
                                .copy(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                            onClick = { onOptionSelected(ActionType.CreateNote) }
                        ) {
                            Text(text = stringResource(R.string.create_note))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Surface(
                    shape = FloatingActionButtonDefaults.shape,
                    color = FloatingActionButtonDefaults.containerColor,
                    tonalElevation = 6.dp,
                    shadowElevation = 6.dp,
                    onClick = onDismiss
                ) {
                    Box(
                        modifier = Modifier.defaultMinSize(56.dp, 56.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close")
                    }
                }
            }

            FilledTonalButton(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 4.dp),
                onClick = {
                    onOptionSelected(ActionType.SampleNote)
                }
            ) {
                Text(stringResource(R.string.sample_note))
            }
        }
    }
}
