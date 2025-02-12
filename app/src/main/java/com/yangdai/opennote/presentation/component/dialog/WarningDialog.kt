package com.yangdai.opennote.presentation.component.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.yangdai.opennote.R

@Composable
fun WarningDialog(
    message: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) = AlertDialog(
    title = { Text(text = stringResource(id = R.string.warning)) },
    text = { Text(text = message) },
    onDismissRequest = onDismissRequest,
    confirmButton = {
        val haptic = LocalHapticFeedback.current
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                onConfirm()
                onDismissRequest()
            },
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(text = stringResource(id = android.R.string.ok))
        }
    },
    dismissButton = {
        TextButton(onClick = onDismissRequest) {
            Text(text = stringResource(id = android.R.string.cancel))
        }
    }
)

@Preview
@Composable
fun WarningDialogPreview() {
    WarningDialog(
        message = "This is a warning message",
        onDismissRequest = {},
        onConfirm = {}
    )
}
