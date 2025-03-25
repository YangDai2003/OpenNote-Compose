package com.yangdai.opennote.presentation.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.component.TextOptionButton

enum class ShareType {
    FILE,
    TEXT,
    COPY,
    IMAGE
}

@Composable
fun ShareDialog(
    isStandard: Boolean,
    onDismissRequest: () -> Unit,
    onConfirm: (ShareType) -> Unit
) = AlertDialog(
    onDismissRequest = onDismissRequest,
    title = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = stringResource(R.string.share_note_as))
            IconButton(
                onClick = {
                    onConfirm(ShareType.COPY)
                }
            ) {
                Icon(painter = painterResource(R.drawable.markdown_copy), contentDescription = null)
            }
        }
    },
    text = {
        Column(modifier = Modifier.fillMaxWidth()) {
            TextOptionButton(buttonText = stringResource(R.string.file)) {
                onConfirm(ShareType.FILE)
            }

            TextOptionButton(buttonText = stringResource(R.string.text)) {
                onConfirm(ShareType.TEXT)
            }

            if (isStandard)
                TextOptionButton(buttonText = stringResource(R.string.image)) {
                    onConfirm(ShareType.IMAGE)
                }
        }
    },
    confirmButton = {}
)

@Composable
@Preview
fun ShareDialogPreview() {
    ShareDialog(
        isStandard = true,
        onDismissRequest = {},
        onConfirm = {}
    )
}
