package com.yangdai.opennote.presentation.component.setting

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Spellcheck
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel
import kotlin.math.roundToInt

@Composable
fun EditorPane(sharedViewModel: SharedViewModel) {

    val settingsState by sharedViewModel.settingsStateFlow.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.FormatSize,
                    contentDescription = "Font size"
                )
            },
            headlineContent = { Text(text = stringResource(R.string.font_size)) },
            supportingContent = {
                Text(
                    text = stringResource(R.string.font_size_detail)
                )
            }
        )

        var sliderPosition by remember { mutableFloatStateOf(settingsState.fontScale) }

        Slider(
            value = sliderPosition,
            onValueChange = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                // Snap to the nearest step
                val roundedValue = (it / 0.05f).roundToInt() * 0.05f
                if (roundedValue in 0.75f..1.25f) {
                    sliderPosition = roundedValue
                }
            },
            onValueChangeFinished = {
                sharedViewModel.putPreferenceValue(
                    Constants.Preferences.FONT_SCALE,
                    sliderPosition
                )
            },
            valueRange = 0.75f..1.25f,
            steps = 9, // (1.3 - 0.7) / 0.05 = 12, but steps should be one less than the number of intervals
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        )

        HorizontalDivider()

        ListItem(
            headlineContent = { Text(text = stringResource(R.string.default_view)) },
            supportingContent = {
                Text(
                    text = stringResource(R.string.default_view_for_note)
                )
            }
        )

        val viewOptions =
            listOf(stringResource(R.string.editing_view), stringResource(R.string.reading_view))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = viewOptions.size),
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    sharedViewModel.putPreferenceValue(
                        Constants.Preferences.IS_DEFAULT_VIEW_FOR_READING,
                        false
                    )
                },
                selected = !settingsState.isDefaultViewForReading
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EditNote, contentDescription = "EditNote"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(viewOptions[0], maxLines = 1, modifier = Modifier.basicMarquee())
                }
            }
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = viewOptions.size),
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    sharedViewModel.putPreferenceValue(
                        Constants.Preferences.IS_DEFAULT_VIEW_FOR_READING,
                        true
                    )
                },
                selected = settingsState.isDefaultViewForReading
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                        contentDescription = "ReadMode"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(viewOptions[1], maxLines = 1, modifier = Modifier.basicMarquee())
                }
            }
        }

        HorizontalDivider()

        ListItem(
            headlineContent = { Text(text = stringResource(R.string.default_editing_mode)) },
            supportingContent = {
                Text(
                    text = stringResource(R.string.default_editing_mode_for_note)
                )
            }
        )

        val modeOptions =
            listOf(stringResource(R.string.standard_mode), stringResource(R.string.lite_mode))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = modeOptions.size),
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    sharedViewModel.putPreferenceValue(
                        Constants.Preferences.IS_DEFAULT_LITE_MODE,
                        false
                    )
                },
                selected = !settingsState.isDefaultLiteMode
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.standard),
                        contentDescription = "StandardMode"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(modeOptions[0], maxLines = 1, modifier = Modifier.basicMarquee())
                }
            }
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = modeOptions.size),
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    sharedViewModel.putPreferenceValue(
                        Constants.Preferences.IS_DEFAULT_LITE_MODE,
                        true
                    )
                },
                selected = settingsState.isDefaultLiteMode
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.lite),
                        contentDescription = "LiteMode"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(modeOptions[1], maxLines = 1, modifier = Modifier.basicMarquee())
                }
            }
        }

        HorizontalDivider()

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Spellcheck,
                    contentDescription = "Lint"
                )
            },
            headlineContent = { Text(text = "Markdown " + stringResource(R.string.lint)) },
            trailingContent = {
                Switch(
                    checked = settingsState.isLintActive,
                    onCheckedChange = { checked ->
                        if (checked)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        else
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                        sharedViewModel.putPreferenceValue(
                            Constants.Preferences.IS_LINT_ACTIVE,
                            checked
                        )
                    }
                )
            },
            supportingContent = {
                Text(
                    text = stringResource(R.string.lint_description)
                )
            }
        )
    }
}
