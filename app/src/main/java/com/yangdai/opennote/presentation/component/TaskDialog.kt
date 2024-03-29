package com.yangdai.opennote.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.state.TaskState

@Composable
fun TaskDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (TaskState) -> Unit
) {

    var task by remember {
        mutableStateOf("")
    }

    var checked by remember {
        mutableStateOf(false)
    }

    var taskError by remember { mutableStateOf(false) }

    AlertDialog(
        title = {
            Text(text = stringResource(R.string.task))
        },
        text = {
            Column {

                OutlinedTextField(
                    value = task,
                    onValueChange = {
                        task = it
                        taskError = it.isBlank()
                    },
                    singleLine = true,
                    isError = taskError
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = checked, onCheckedChange = { checked = it })
                    Text(text = stringResource(R.string.completed))
                }
            }
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (task.isBlank()) {
                        taskError = true
                    }

                    if (!taskError) {
                        task = task.trim()
                        onConfirm(TaskState(task, checked))
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
