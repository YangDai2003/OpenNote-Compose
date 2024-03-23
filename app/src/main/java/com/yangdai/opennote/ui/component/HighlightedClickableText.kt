package com.yangdai.opennote.ui.component

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

@Composable
fun HighlightedClickableText(text: String) {
    val pattern = "\\[(.*?)]\\((.*?)\\)".toRegex()
    val matches = pattern.findAll(text)

    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        matches.forEach { matchResult ->
            val range = matchResult.range
            val title = matchResult.groupValues[1]
            val link = matchResult.groupValues[2]

            // Append plain text
            append(text.substring(lastIndex, range.first))

            // Append link text
            withStyle(
                style = SpanStyle(
                    textDecoration = TextDecoration.Underline,
                    color = MaterialTheme.colorScheme.primary
                )
            ) {
                pushStringAnnotation(tag = "URL", annotation = link)
                append(title)
                pop()
            }

            lastIndex = range.last + 1
        }

        // Append the rest of the text
        append(text.substring(lastIndex, text.length))
    }
    val context = LocalContext.current
    SelectionContainer {
        ClickableText(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            text = annotatedString,
            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            onClick = { offset ->
                try {
                    annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                            context.startActivity(intent)
                        }
                } catch (e: Exception) {
                    e.printStackTrace()
                    try {
                        annotatedString.getStringAnnotations(
                            tag = "URL",
                            start = offset,
                            end = offset
                        )
                            .firstOrNull()?.let { annotation ->
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://" + annotation.item)
                                )
                                context.startActivity(intent)
                            }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        )
    }
}