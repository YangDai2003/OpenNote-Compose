package com.yangdai.opennote.presentation.component.setting

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.state.WidgetBackgroundColor.Companion.toInt
import com.yangdai.opennote.presentation.state.WidgetTextSize.Companion.toInt
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.presentation.viewmodel.WidgetViewModel

@Composable
fun WidgetPane(
    widgetViewModel: WidgetViewModel = hiltViewModel()
) {

    val settingsState by widgetViewModel.widgetSettingsState.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        ListItem(
            headlineContent = { Text(text = stringResource(R.string.font_size)) }
        )

        val fontSizeOptions = listOf(
            stringResource(R.string.small),
            stringResource(R.string.medium),
            stringResource(R.string.large)
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = fontSizeOptions.size),
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    widgetViewModel.putPreferenceValue(Constants.Widget.WIDGET_TEXT_SIZE, 0)
                },
                selected = settingsState.textSize.toInt() == 0
            ) {
                Text(fontSizeOptions[0], maxLines = 1, modifier = Modifier.basicMarquee())
            }
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = fontSizeOptions.size),
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    widgetViewModel.putPreferenceValue(Constants.Widget.WIDGET_TEXT_SIZE, 1)
                },
                selected = settingsState.textSize.toInt() == 1
            ) {
                Text(fontSizeOptions[1], maxLines = 1, modifier = Modifier.basicMarquee())
            }
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = fontSizeOptions.size),
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    widgetViewModel.putPreferenceValue(Constants.Widget.WIDGET_TEXT_SIZE, 2)
                },
                selected = settingsState.textSize.toInt() == 2
            ) {
                Text(fontSizeOptions[2], maxLines = 1, modifier = Modifier.basicMarquee())
            }
        }

        HorizontalDivider()

        ListItem(
            headlineContent = { Text(text = stringResource(R.string.lines) + ": " + settingsState.textLines) }
        )

        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            value = settingsState.textLines.toFloat(),
            onValueChange = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                widgetViewModel.putPreferenceValue(
                    Constants.Widget.WIDGET_TEXT_LINES,
                    it.toInt()
                )
            },
            valueRange = 0f..20f,
            steps = 19
        )

        HorizontalDivider()

        ListItem(
            headlineContent = { Text(text = stringResource(R.string.color)) }
        )

        val colorOptions = listOf(
            stringResource(R.string.transparent),
            stringResource(R.string.system_default)
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = colorOptions.size),
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    widgetViewModel.putPreferenceValue(Constants.Widget.WIDGET_BACKGROUND_COLOR, 0)
                },
                selected = settingsState.backgroundColor.toInt() == 0
            ) {
                Text(colorOptions[0], maxLines = 1, modifier = Modifier.basicMarquee())
            }
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = colorOptions.size),
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    widgetViewModel.putPreferenceValue(Constants.Widget.WIDGET_BACKGROUND_COLOR, 1)
                },
                selected = settingsState.backgroundColor.toInt() == 1
            ) {
                Text(colorOptions[1], maxLines = 1, modifier = Modifier.basicMarquee())
            }
        }

        Spacer(Modifier.navigationBarsPadding())
    }
}
