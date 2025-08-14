package com.yangdai.opennote.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A customizable text button that fills the available width and uses the Material Theme.
 *
 * This composable creates a `TextButton` with a specific styling:
 * - It fills the maximum available width.
 * - It has a top padding of 8.dp.
 * - It uses the `MaterialTheme.shapes.medium` for its shape.
 * - It uses `MaterialTheme.colorScheme.surfaceVariant` for the container color.
 * - It displays the provided `buttonText` in `titleMedium` style.
 * - Vertical padding of 4.dp applied to the button text
 *
 * @param buttonText The text to be displayed on the button.
 * @param onButtonClick The callback to be invoked when the button is clicked.
 */
@Composable
fun TextOptionButton(
    buttonText: String,
    onButtonClick: () -> Unit
) = TextButton(
    modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp),
    shape = MaterialTheme.shapes.medium,
    colors = ButtonDefaults.textButtonColors().copy(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ),
    onClick = onButtonClick
) {
    Text(
        modifier = Modifier.padding(vertical = 4.dp),
        text = buttonText,
        style = MaterialTheme.typography.titleMedium
    )
}