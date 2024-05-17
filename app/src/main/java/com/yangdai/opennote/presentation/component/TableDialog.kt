package com.yangdai.opennote.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R

@Composable
fun TableDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (row: Int, column: Int) -> Unit
) {

    var row by remember { mutableStateOf("") }
    var column by remember { mutableStateOf("") }

    var rowError by remember { mutableStateOf(false) }
    var columnError by remember { mutableStateOf(false) }

    AlertDialog(
        title = {
            Text(text = stringResource(R.string.table))
        },
        text = {
            Column {
                OutlinedTextField(
                    value = row,
                    onValueChange = {
                        row = if (it.length > 3) it.substring(0, 3) else it
                        rowError = !row.all { char -> char.isDigit() }
                    },
                    isError = rowError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    label = { Text(text = stringResource(R.string.row)) })

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = column,
                    onValueChange = {
                        column = if (it.length > 3) it.substring(0, 3) else it
                        columnError = !column.all { char -> char.isDigit() }
                    },
                    isError = columnError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    label = { Text(text = stringResource(R.string.column)) }
                )
            }
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (row.isBlank()) {
                        rowError = true
                    }
                    if (column.isBlank()) {
                        columnError = true
                    }
                    if (!rowError && !columnError) {
                        row = row.trim()
                        column = column.trim()
                        onConfirm(row.toInt(), column.toInt())
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

@Preview
@Composable
fun TableDialogPreview() {
    TableDialog({}) { _, _ ->
    }
}
