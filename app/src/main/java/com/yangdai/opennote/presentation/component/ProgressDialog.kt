package com.yangdai.opennote.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R

@Composable
fun ProgressDialog(
    isLoading: Boolean,
    progress: Float,
    onDismissRequest: () -> Unit
) {
    if (isLoading) {
        AlertDialog(
            title = { Text(text = stringResource(id = R.string.progress)) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        progress = { progress }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "${(progress * 100).toInt()}%")
                }
            },
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            }
        )
    }
}