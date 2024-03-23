package com.yangdai.opennote.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.yangdai.opennote.R
import com.yangdai.opennote.ui.state.LinkState


@Composable
fun LinkDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (LinkState) -> Unit
) {

    var title by remember {
        mutableStateOf("")
    }

    var uri by remember {
        mutableStateOf("")
    }
    var titleError by remember { mutableStateOf(false) }
    var uriError by remember { mutableStateOf(false) }

    AlertDialog(
        title = {
            Text(text = stringResource(R.string.link))
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = it.isBlank()
                    },
                    singleLine = true,
                    isError = titleError,
                    placeholder = { Text(text = stringResource(R.string.title)) },
                )
                OutlinedTextField(
                    value = uri,
                    onValueChange = {
                        uri = it
                        uriError = it.isBlank()
                    },
                    singleLine = true,
                    isError = uriError,
                    placeholder = { Text(text = stringResource(R.string.link)) },
                )
            }
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    }
                    if (uri.isBlank()) {
                        uriError = true
                    }
                    if (!titleError && !uriError) {
                        onConfirm(LinkState(title.trim(), uri.trim()))
                        onDismissRequest()
                    }
                }
            ) {
                Text(stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        }
    )
}
