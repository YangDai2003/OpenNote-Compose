package com.yangdai.opennote.presentation.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.yangdai.opennote.R

@Composable
fun RatingDialog(
    onDismissRequest: () -> Unit,
    onRatingChanged: (Int) -> Unit
) {

    var rating by remember { mutableIntStateOf(0) }

    AlertDialog(
        title = { Text(text = stringResource(id = R.string.rate_this_app) + " 💖") },
        text = {
            Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                for (i in 1..5) {
                    IconButton(onClick = {
                        rating = i
                    }) {
                        Icon(
                            imageVector = Icons.Filled.StarRate,
                            contentDescription = "Star",
                            tint = if (i <= rating) Color.Yellow else MaterialTheme.colorScheme.surfaceDim
                        )
                    }
                }
            }
        },
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        confirmButton = {
            val haptic = LocalHapticFeedback.current
            Button(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                onDismissRequest()
                onRatingChanged(rating)
            }) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
    )
}

@Preview
@Composable
fun RatingDialogPreview() {
    RatingDialog(onDismissRequest = {}, onRatingChanged = {})
}
