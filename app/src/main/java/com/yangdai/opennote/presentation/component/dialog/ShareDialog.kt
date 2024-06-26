package com.yangdai.opennote.presentation.component.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.component.TextOptionButton

enum class ShareType {
    FILE,
    TEXT
}

@Composable
fun ShareDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (ShareType) -> Unit
) = AlertDialog(
    onDismissRequest = onDismissRequest,
    title = {
        Text(text = stringResource(R.string.share_note_as))
    },
    text = {
        Column(modifier = Modifier.fillMaxWidth()) {
            TextOptionButton(text = stringResource(R.string.file)) {
                onConfirm(ShareType.FILE)
            }

            TextOptionButton(text = stringResource(R.string.text)) {
                onConfirm(ShareType.TEXT)
            }
        }
    },
    confirmButton = {}
)

@Composable
@Preview
fun ShareDialogPreview() {
    ShareDialog(
        onDismissRequest = {},
        onConfirm = {}
    )
}
