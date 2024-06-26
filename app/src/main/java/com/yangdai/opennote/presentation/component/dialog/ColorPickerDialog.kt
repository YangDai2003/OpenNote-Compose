package com.yangdai.opennote.presentation.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.util.toHexColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerDialog(
    initialColor: Color? = null,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var color by remember {
        mutableStateOf(initialColor)
    }
    val controller = rememberColorPickerController()

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.color_picker),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = color?.toArgb()?.toHexColor() ?: "",
                    color = color ?: Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            HsvColorPicker(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(320.dp),
                controller = controller,
                onColorChanged = { colorEnvelope: ColorEnvelope ->
                    color = colorEnvelope.color // ARGB color value.
                },
                initialColor = initialColor
            )

            AlphaSlider(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(35.dp),
                borderRadius = 16.dp,
                controller = controller,
                initialColor = initialColor
            )

            BrightnessSlider(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(35.dp),
                borderRadius = 16.dp,
                controller = controller,
                initialColor = initialColor
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = { onConfirm(color?.toArgb() ?: Color.White.toArgb()) }) {
                    Text(stringResource(id = android.R.string.ok))
                }
            }
        }
    }
}
