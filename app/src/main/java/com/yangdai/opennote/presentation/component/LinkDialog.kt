package com.yangdai.opennote.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R


@Composable
fun LinkDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (name: String, uri: String) -> Unit
) {

    var name by remember { mutableStateOf("") }
    var uri by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }
    var uriError by remember { mutableStateOf(false) }

    AlertDialog(
        title = {
            Text(text = stringResource(R.string.link))
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    singleLine = true,
                    isError = nameError,
                    placeholder = { Text(text = stringResource(R.string.name)) })

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uri,
                    onValueChange = {
                        uri = it
                        uriError = it.isBlank()
                    },
                    singleLine = true,
                    isError = uriError,
                    placeholder = { Text(text = stringResource(R.string.uri_example)) }
                )
            }
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    }
                    if (uri.isBlank()) {
                        uriError = true
                    }
                    if (!nameError && !uriError) {
                        name = name.trim()
                        uri = uri.trim()
                        if (!uri.startsWith("http://") && !uri.startsWith("https://")
                            && uri.startsWith("www.")
                        ) {
                            uri = "https://$uri"
                        }
                        onConfirm(name, uri)
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

@Composable
@Preview
fun LinkDialogPreview() {
    LinkDialog(onDismissRequest = {}) { _, _ ->
    }
}
