package com.yangdai.opennote.presentation.component.dialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.yangdai.opennote.R

@Composable
fun ProgressDialog(
    isLoading: Boolean,
    progress: Float,
    infinite: Boolean = false,
    errorMessage: String = "",
    onDismissRequest: () -> Unit
) {

    if (!isLoading) return

    AlertDialog(
        title = { Text(text = stringResource(id = R.string.progress)) },
        text = {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {

                    if (infinite && progress < 1f)
                        CircularProgressIndicator(
                            modifier = Modifier.size(72.dp),
                            strokeWidth = 6.dp
                        )
                    else
                        CircularProgressIndicator(
                            modifier = Modifier.size(72.dp),
                            strokeWidth = 6.dp,
                            progress = { progress }
                        )

                    AnimatedContent(targetState = progress, label = "progress") {
                        if (it == 1f)
                            Icon(
                                modifier = Modifier.size(56.dp),
                                imageVector = Icons.Rounded.Done,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        else
                            if (!infinite)
                                Text(text = "${(progress * 100).toInt()}%")
                    }
                }
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage)
                }
            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = if (progress == 1f) android.R.string.ok else android.R.string.cancel))
            }
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}

@Composable
@Preview
fun ProgressDialogPreview1() {
    ProgressDialog(isLoading = true, progress = 1f, onDismissRequest = {})
}

@Composable
@Preview
fun ProgressDialogPreview2() {
    ProgressDialog(
        isLoading = true,
        progress = 0.3f,
        errorMessage = "exception",
        onDismissRequest = {})
}

@Composable
@Preview
fun ProgressDialogPreview3() {
    ProgressDialog(isLoading = true, progress = 0.3f, infinite = true, onDismissRequest = {})
}