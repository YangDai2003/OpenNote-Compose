package com.yangdai.opennote.presentation.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
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
        TextButton(
            onClick = {
                onConfirm()
                onDismissRequest()
            },
            colors = ButtonDefaults.textButtonColors().copy(
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
