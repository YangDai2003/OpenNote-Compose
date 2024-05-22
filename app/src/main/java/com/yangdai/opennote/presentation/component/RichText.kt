package com.yangdai.opennote.presentation.component

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import com.yangdai.opennote.presentation.theme.linkColor
import com.yangdai.opennote.presentation.util.rememberCustomTabsIntent

@Composable
fun RichText(str: String) {

    val context = LocalContext.current

    val customTabsIntent = rememberCustomTabsIntent()

    val pattern = remember {
        "\\[(.*?)]\\((.*?)\\)".toRegex()
    }

    val text by remember(str) {
        mutableStateOf(str.replace("- [ ]", "◎").replace("- [x]", "◉"))
    }

    val matches by remember(pattern, text) {
        mutableStateOf(
            pattern.findAll(text)
        )
    }

    SelectionContainer {
        Text(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            text = buildAnnotatedString {

                var lastIndex = 0

                matches.forEach { matchResult ->
                    val range = matchResult.range
                    val title = matchResult.groupValues[1]
                    val link = matchResult.groupValues[2]

                    // Append plain text
                    append(text.substring(lastIndex, range.first))

                    val url = LinkAnnotation.Url(link) {
                        // Handle click event
                        val url = (it as LinkAnnotation.Url).url

                        try {
                            if (url.startsWith("http://") || url.startsWith("https://")) {
                                customTabsIntent.launchUrl(context, Uri.parse(url))
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Show error message to the user
                            Toast.makeText(context, "Failed to open link: $url", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                    withLink(url) {
                        append(title)
                    }

                    lastIndex = range.last + 1
                }

                // Append the rest of the text
                append(text.substring(lastIndex, text.length))
            },
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                lineBreak = LineBreak.Paragraph
            ),
            linkStyles = TextDefaults.linkStyles().copy(
                linkStyle = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
            )
        )
    }
}
