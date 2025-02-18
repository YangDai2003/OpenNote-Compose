package com.yangdai.opennote.presentation.component.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.component.note.CustomTextField
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.presentation.util.getOrCreateDirectory
import com.yangdai.opennote.presentation.util.rememberCustomTabsIntent
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun TemplatePane(sharedViewModel: SharedViewModel) {

    val settingsState by sharedViewModel.settingsStateFlow.collectAsStateWithLifecycle()

    var currentDateFormatter by remember { mutableStateOf(settingsState.dateFormatter) }
    var currentTimeFormatter by remember { mutableStateOf(settingsState.timeFormatter) }

    var currentDateFormatted by remember { mutableStateOf("") }
    var currentTimeFormatted by remember { mutableStateOf("") }

    var isDateInvalid by remember { mutableStateOf(false) }
    var isTimeInvalid by remember { mutableStateOf(false) }

    val customTabsIntent = rememberCustomTabsIntent()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val openNoteDir = getOrCreateDirectory(
            context.applicationContext, settingsState.storagePath.toUri(), Constants.File.OPENNOTE
        )
        openNoteDir?.let { dir ->
            getOrCreateDirectory(
                context.applicationContext, dir.uri, Constants.File.OPENNOTE_TEMPLATES
            )
        }
    }

    // Update formatted date and time whenever formatters change
    LaunchedEffect(currentDateFormatter, currentTimeFormatter) {
        runCatching {
            val now = LocalDateTime.now()
            val dateFormatter =
                DateTimeFormatter.ofPattern(if (currentDateFormatter.isBlank()) "yyyy-MM-DD" else currentDateFormatter)
            currentDateFormatted = now.format(dateFormatter)
        }.onFailure {
            isDateInvalid = true
        }.onSuccess {
            isDateInvalid = false
        }

        runCatching {
            val now = LocalDateTime.now()
            val timeFormatter =
                DateTimeFormatter.ofPattern(if (currentTimeFormatter.isBlank()) "HH:mm" else currentTimeFormatter)
            currentTimeFormatted = now.format(timeFormatter)
        }.onFailure {
            isTimeInvalid = true
        }.onSuccess {
            isTimeInvalid = false
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.template_folder_location)) },
            supportingContent = { Text(text = stringResource(R.string.files_in_this_folder_will_be_available_as_templates)) },
            overlineContent = { Text(text = "${Constants.File.OPENNOTE}/${Constants.File.OPENNOTE_TEMPLATES}") })

        HorizontalDivider()

        ListItem(
            headlineContent = { Text(text = stringResource(R.string.date_format)) },
            supportingContent = {
                val linkColor = MaterialTheme.colorScheme.primary
                val annotatedString = buildAnnotatedString {
                    append(stringResource(R.string.date_in_the_template_file_will_be_replaced_with_this_value))
                    append(stringResource(R.string.you_can_also_use_date_yyyy_mm_dd_to_override_the_format_once))
                    append(stringResource(R.string.for_more_syntax_refer_to))
                    withLink(
                        LinkAnnotation.Url(
                            "https://developer.android.com/reference/java/time/format/DateTimeFormatter",
                            TextLinkStyles(
                                style = SpanStyle(
                                    color = linkColor, textDecoration = TextDecoration.Underline
                                )
                            )
                        ) {
                            val url = (it as LinkAnnotation.Url).url
                            customTabsIntent.launchUrl(context, url.toUri())
                        }) {
                        append(stringResource(R.string.format_reference))
                    }
                    append(
                        stringResource(
                            R.string.your_current_syntax_looks_like_this, currentDateFormatted
                        )
                    )
                }
                Text(text = annotatedString)
            })

        Row(
            Modifier
                .widthIn(max = 600.dp)
                .padding(horizontal = 16.dp)
        ) {
            CustomTextField(
                value = currentDateFormatter,
                onValueChange = {
                    currentDateFormatter = it
                    sharedViewModel.putPreferenceValue(
                        Constants.Preferences.DATE_FORMATTER, it
                    )
                },
                isError = isDateInvalid,
                leadingIcon = Icons.Outlined.DateRange,
                placeholderText = "YYYY-MM-DD"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider()

        ListItem(
            headlineContent = { Text(text = stringResource(R.string.time_format)) },
            supportingContent = {
                val linkColor = MaterialTheme.colorScheme.primary
                val annotatedString = buildAnnotatedString {
                    append(stringResource(R.string.time_in_the_template_file_will_be_replaced_with_this_value))
                    append(stringResource(R.string.you_can_also_use_time_hh_mm_to_override_the_format_once))
                    append(stringResource(R.string.for_more_syntax_refer_to))
                    withLink(
                        LinkAnnotation.Url(
                            "https://developer.android.com/reference/java/time/format/DateTimeFormatter",
                            TextLinkStyles(
                                style = SpanStyle(
                                    color = linkColor, textDecoration = TextDecoration.Underline
                                )
                            )
                        ) {
                            val url = (it as LinkAnnotation.Url).url
                            customTabsIntent.launchUrl(context, url.toUri())
                        }) {
                        append(stringResource(R.string.format_reference))
                    }
                    append(
                        stringResource(
                            R.string.your_current_syntax_looks_like_this, currentTimeFormatted
                        )
                    )
                }
                Text(text = annotatedString)
            })

        Row(
            Modifier
                .widthIn(max = 600.dp)
                .padding(horizontal = 16.dp)
        ) {
            CustomTextField(
                value = currentTimeFormatter,
                onValueChange = {
                    currentTimeFormatter = it
                    sharedViewModel.putPreferenceValue(
                        Constants.Preferences.TIME_FORMATTER, it
                    )
                },
                isError = isTimeInvalid,
                leadingIcon = Icons.Outlined.AccessTime,
                placeholderText = "HH:mm"
            )
        }
    }
}
