package com.yangdai.opennote.presentation.component.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R


@Composable
fun LinkDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (name: String, uri: String) -> Unit
) {

    var name by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }

    val linkError by remember {
        derivedStateOf {
            if (link.isNotEmpty()) {
                // Email is considered erroneous until it completely matches EMAIL_ADDRESS.
                !android.util.Patterns.WEB_URL.matcher(link).matches()
            } else {
                false
            }
        }
    }

    AlertDialog(
        title = {
            Text(text = stringResource(R.string.link))
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = stringResource(R.string.name)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    singleLine = true,
                    isError = linkError,
                    label = { Text(text = stringResource(id = R.string.web_url)) },
                    placeholder = { Text(text = stringResource(R.string.uri_example)) },
                    supportingText = {
                        if (linkError) {
                            Text(text = stringResource(R.string.incorrect_link_format))
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = true,
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done
                    )
                )
            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            val haptic = LocalHapticFeedback.current
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    if (!linkError) {
                        name = name.trim()
                        link = link.trim()
                        if (!link.startsWith("http://") && !link.startsWith("https://")
                            && link.startsWith("www.")
                        ) {
                            link = "https://$link"
                        }
                        onConfirm(name, link)
                        onDismissRequest()
                    }
                }
            ) {
                Text(stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
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
