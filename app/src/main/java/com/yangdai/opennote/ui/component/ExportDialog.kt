package com.yangdai.opennote.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R
import com.yangdai.opennote.ui.util.exportNote
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

@Composable
fun ExportDialog(
    showExportDialog: Boolean,
    title: String,
    content: String,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    if (showExportDialog) {
        val modeOptions = listOf(
            "TXT",
            "MARKDOWN",
            "HTML"
        )
        val (selectedMode, onModeSelected) = rememberSaveable {
            mutableStateOf(
                modeOptions[0]
            )
        }
        AlertDialog(
            title = {
                Text(text = stringResource(R.string.export))
            },
            text = {
                Column(Modifier.selectableGroup()) {
                    modeOptions.forEach { text ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (text == selectedMode),
                                    onClick = {
                                        onModeSelected(text)
                                    },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (text == selectedMode),
                                onClick = null
                            )
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .padding(start = 16.dp)

                            )
                        }
                    }
                }
            },
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(
                    onClick = {
                        if (selectedMode == "HTML") {

                            val flavour = GFMFlavourDescriptor()
                            val parsedTree =
                                MarkdownParser(flavour).buildMarkdownTreeFromString(content)
                            val html =
                                HtmlGenerator(content, parsedTree, flavour).generateHtml()

                            exportNote(
                                context.applicationContext,
                                title,
                                html,
                                selectedMode
                            )
                        } else {
                            exportNote(
                                context.applicationContext,
                                title,
                                content,
                                selectedMode
                            )
                        }
                        onDismissRequest()
                    }
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismissRequest
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            })
    }
}
