package com.yangdai.opennote.presentation.component.setting

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatAlignLeft
import androidx.compose.material.icons.automirrored.outlined.FormatAlignRight
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FormatAlignCenter
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Save
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

    Column(Modifier.verticalScroll(rememberScrollState())) {

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
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            viewOptions.forEachIndexed { index, option ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = viewOptions.size
                    ),
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        sharedViewModel.putPreferenceValue(
                            Constants.Preferences.IS_DEFAULT_VIEW_FOR_READING,
                            index == 1
                        )
                    },
                    selected = settingsState.isDefaultViewForReading == (index == 1)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (index == 0) Icons.Outlined.EditNote else Icons.AutoMirrored.Outlined.MenuBook,
                            contentDescription = option
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(option, maxLines = 1, modifier = Modifier.basicMarquee())
                    }
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
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            modeOptions.forEachIndexed { index, option ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = modeOptions.size
                    ),
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        sharedViewModel.putPreferenceValue(
                            Constants.Preferences.IS_DEFAULT_LITE_MODE,
                            index == 1
                        )
                    },
                    selected = settingsState.isDefaultLiteMode == (index == 1)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = if (index == 0) painterResource(id = R.drawable.standard)
                            else painterResource(id = R.drawable.lite),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(option, maxLines = 1, modifier = Modifier.basicMarquee())
                    }
                }
            }
        }

        HorizontalDivider()

        ListItem(headlineContent = { Text(text = stringResource(R.string.title_align)) })

        val alignOptions = listOf(
            stringResource(R.string.left),
            stringResource(R.string.center),
            stringResource(R.string.right)
        )
        val alignIcons = remember {
            listOf(
                Icons.AutoMirrored.Outlined.FormatAlignLeft,
                Icons.Outlined.FormatAlignCenter,
                Icons.AutoMirrored.Outlined.FormatAlignRight
            )
        }

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            alignOptions.forEachIndexed { index, option ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = alignOptions.size
                    ),
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        sharedViewModel.putPreferenceValue(Constants.Preferences.TITLE_ALIGN, index)
                    },
                    selected = settingsState.titleAlignment == index
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = alignIcons[index],
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(option, maxLines = 1, modifier = Modifier.basicMarquee())
                    }
                }
            }
        }

        HorizontalDivider()

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Save,
                    contentDescription = "Auto save"
                )
            },
            headlineContent = { Text(text = stringResource(R.string.auto_save)) },
            trailingContent = {
                Switch(
                    checked = settingsState.isAutoSaveEnabled,
                    onCheckedChange = { checked ->
                        if (checked)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        else
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                        sharedViewModel.putPreferenceValue(
                            Constants.Preferences.IS_AUTO_SAVE_ENABLED,
                            checked
                        )
                    }
                )
            }
        )

        HorizontalDivider()

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.FormatListNumbered,
                    contentDescription = "Line Numbers"
                )
            },
            headlineContent = { Text(text = stringResource(R.string.line_numbers)) },
            trailingContent = {
                Switch(
                    checked = settingsState.showLineNumbers,
                    onCheckedChange = { checked ->
                        if (checked)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        else
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                        sharedViewModel.putPreferenceValue(
                            Constants.Preferences.SHOW_LINE_NUMBERS,
                            checked
                        )
                    }
                )
            }
        )

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

        Spacer(Modifier.navigationBarsPadding())
    }
}
