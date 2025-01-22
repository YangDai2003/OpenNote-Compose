package com.yangdai.opennote.presentation.component.text

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.AddChart
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DataArray
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatPaint
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.HorizontalRule
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.util.Constants

@Preview(showBackground = true)
@Composable
fun RichTextEditorRowPreview() {
    RichTextEditorRow(
        onTitle1Click = {},
        onTitle2Click = {},
        onTitle3Click = {},
        onTitle4Click = {},
        onTitle5Click = {},
        onTitle6Click = {},
        onBoldClick = {},
        onItalicClick = {},
        onUnderlineClick = {},
        onStrikeThroughClick = {},
        onCodeClick = {},
        onBracketsClick = {},
        onBracesClick = {},
        onScanButtonClick = {}
    )
}
@Preview(showBackground = true)
@Composable
fun MarkdownEditorRowPreview() {
    MarkdownEditorRow(
        canUndo = true,
        canRedo = true,
        onEdit = {},
        onTableButtonClick = {},
        onScanButtonClick = {},
        onTaskButtonClick = {},
        onLinkButtonClick = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconButtonWithTooltip(
    enabled: Boolean = true,
    imageVector: ImageVector? = null,
    painter: Int? = null,
    contentDescription: String,
    shortCutDescription: String? = null,
    onClick: () -> Unit
) = TooltipBox(
    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
    tooltip = {
        if (shortCutDescription != null) {
            PlainTooltip(
                content = { Text(shortCutDescription) }
            )
        }
    },
    state = rememberTooltipState(),
    focusable = false,
    enableUserInput = true
) {
    IconButton(onClick = onClick, enabled = enabled) {
        if (imageVector != null) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription
            )
        } else {
            Icon(
                painter = painterResource(id = painter!!),
                contentDescription = contentDescription
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectableButtonWithTooltip(
    enabled: Boolean = true,
    imageVector: ImageVector? = null,
    painter: Int? = null,
    contentDescription: String,
    shortCutDescription: String? = null,
    onClick: (Boolean) -> Unit
) = TooltipBox(
    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
    tooltip = {
        if (shortCutDescription != null) {
            PlainTooltip(
                content = { Text(shortCutDescription) }
            )
        }
    },
    state = rememberTooltipState(),
    focusable = false,
    enableUserInput = true
) {

    var selected by rememberSaveable { mutableStateOf(false) }

    IconButton(
        onClick = {
            onClick(selected)
            selected = !selected
        }, enabled = enabled, colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Unspecified
        )
    ) {
        if (imageVector != null) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription
            )
        } else {
            Icon(
                painter = painterResource(id = painter!!),
                contentDescription = contentDescription
            )
        }
    }
}

@Composable
fun RichTextEditorRow(
    onTitle1Click: (Boolean) -> Unit,
    onTitle2Click: (Boolean) -> Unit,
    onTitle3Click: (Boolean) -> Unit,
    onTitle4Click: (Boolean) -> Unit,
    onTitle5Click: (Boolean) -> Unit,
    onTitle6Click: (Boolean) -> Unit,
    onBoldClick: (Boolean) -> Unit,
    onItalicClick: (Boolean) -> Unit,
    onUnderlineClick: (Boolean) -> Unit,
    onStrikeThroughClick: (Boolean) -> Unit,
    onCodeClick: (Boolean) -> Unit,
    onBracketsClick: (Boolean) -> Unit,
    onBracesClick: (Boolean) -> Unit,
    onScanButtonClick: () -> Unit
) {

    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {

        HorizontalDivider(
            Modifier.fillMaxWidth().padding(bottom = 4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            thickness = 2.dp
        )

        Row(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(bottom = 4.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButtonWithTooltip(
                imageVector = Icons.Outlined.Title,
                contentDescription = "Heading Level"
            ) {
                isExpanded = !isExpanded
            }

            AnimatedVisibility(visible = isExpanded) {
                Row(
                    modifier = Modifier.fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {

                    SelectableButtonWithTooltip(
                        painter = R.drawable.format_h1,
                        contentDescription = "H1",
                        shortCutDescription = "Ctrl + 1",
                        onClick = onTitle1Click
                    )

                    SelectableButtonWithTooltip(
                        painter = R.drawable.format_h2,
                        contentDescription = "H2",
                        shortCutDescription = "Ctrl + 2",
                        onClick = onTitle2Click
                    )

                    SelectableButtonWithTooltip(
                        painter = R.drawable.format_h3,
                        contentDescription = "H3",
                        shortCutDescription = "Ctrl + 3",
                        onClick = onTitle3Click
                    )

                    SelectableButtonWithTooltip(
                        painter = R.drawable.format_h4,
                        contentDescription = "H4",
                        shortCutDescription = "Ctrl + 4",
                        onClick = onTitle4Click
                    )

                    SelectableButtonWithTooltip(
                        painter = R.drawable.format_h5,
                        contentDescription = "H5",
                        shortCutDescription = "Ctrl + 5",
                        onClick = onTitle5Click
                    )

                    SelectableButtonWithTooltip(
                        painter = R.drawable.format_h6,
                        contentDescription = "H6",
                        shortCutDescription = "Ctrl + 6",
                        onClick = onTitle6Click
                    )
                }
            }

            SelectableButtonWithTooltip(
                imageVector = Icons.Outlined.FormatBold,
                contentDescription = stringResource(id = R.string.bold),
                shortCutDescription = "Ctrl + B",
                onClick = onBoldClick
            )
            SelectableButtonWithTooltip(
                imageVector = Icons.Outlined.FormatItalic,
                contentDescription = stringResource(id = R.string.italic),
                shortCutDescription = "Ctrl + I",
                onClick = onItalicClick
            )

            SelectableButtonWithTooltip(
                imageVector = Icons.Outlined.FormatUnderlined,
                contentDescription = stringResource(id = R.string.underline),
                shortCutDescription = "Ctrl + U",
                onClick = onUnderlineClick
            )

            SelectableButtonWithTooltip(
                imageVector = Icons.Outlined.FormatStrikethrough,
                contentDescription = stringResource(id = R.string.strikethrough),
                shortCutDescription = "Ctrl + D",
                onClick = onStrikeThroughClick
            )

            SelectableButtonWithTooltip(
                imageVector = Icons.Outlined.Code,
                contentDescription = stringResource(id = R.string.code),
                shortCutDescription = "Ctrl + Shift + K",
                onClick = onCodeClick
            )

            SelectableButtonWithTooltip(
                imageVector = Icons.Outlined.DataArray,
                contentDescription = "Brackets",
                onClick = onBracketsClick
            )

            SelectableButtonWithTooltip(
                imageVector = Icons.Outlined.DataObject,
                contentDescription = "Braces",
                onClick = onBracesClick
            )

//            IconButtonWithTooltip(
//                imageVector = Icons.Outlined.DocumentScanner,
//                contentDescription = stringResource(id = R.string.scan),
//                shortCutDescription = "Ctrl + S",
//                onClick = onScanButtonClick
//            )
        }
    }
}

@Composable
fun MarkdownEditorRow(
    canUndo: Boolean,
    canRedo: Boolean,
    onEdit: (String) -> Unit,
    onTableButtonClick: () -> Unit,
    onScanButtonClick: () -> Unit,
    onTaskButtonClick: () -> Unit,
    onLinkButtonClick: () -> Unit
) {

    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {

        HorizontalDivider(
            Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            thickness = 2.dp
        )

        Row(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButtonWithTooltip(
                enabled = canUndo,
                imageVector = Icons.AutoMirrored.Outlined.Undo,
                contentDescription = stringResource(id = R.string.undo),
                shortCutDescription = "Ctrl + Z"
            ) {
                onEdit(Constants.Editor.UNDO)
            }

            IconButtonWithTooltip(
                enabled = canRedo,
                imageVector = Icons.AutoMirrored.Outlined.Redo,
                contentDescription = stringResource(id = R.string.redo),
                shortCutDescription = "Ctrl + Y"
            ) {
                onEdit(Constants.Editor.REDO)
            }


            IconButtonWithTooltip(
                imageVector = Icons.Outlined.Title,
                contentDescription = "Heading Level"
            ) {
                isExpanded = !isExpanded
            }

            AnimatedVisibility(visible = isExpanded) {
                Row(
                    modifier = Modifier.fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {

                    IconButtonWithTooltip(
                        painter = R.drawable.format_h1,
                        contentDescription = "H1",
                        shortCutDescription = "Ctrl + 1"
                    ) {
                        onEdit(Constants.Editor.H1)
                    }

                    IconButtonWithTooltip(
                        painter = R.drawable.format_h2,
                        contentDescription = "H2",
                        shortCutDescription = "Ctrl + 2"
                    ) {
                        onEdit(Constants.Editor.H2)
                    }

                    IconButtonWithTooltip(
                        painter = R.drawable.format_h3,
                        contentDescription = "H3",
                        shortCutDescription = "Ctrl + 3"
                    ) {
                        onEdit(Constants.Editor.H3)
                    }

                    IconButtonWithTooltip(
                        painter = R.drawable.format_h4,
                        contentDescription = "H4",
                        shortCutDescription = "Ctrl + 4"
                    ) {
                        onEdit(Constants.Editor.H4)
                    }

                    IconButtonWithTooltip(
                        painter = R.drawable.format_h5,
                        contentDescription = "H5",
                        shortCutDescription = "Ctrl + 5"
                    ) {
                        onEdit(Constants.Editor.H5)
                    }

                    IconButtonWithTooltip(
                        painter = R.drawable.format_h6,
                        contentDescription = "H6",
                        shortCutDescription = "Ctrl + 6"
                    ) {
                        onEdit(Constants.Editor.H6)
                    }
                }
            }

            IconButtonWithTooltip(
                imageVector = Icons.Outlined.FormatBold,
                contentDescription = stringResource(id = R.string.bold),
                shortCutDescription = "Ctrl + B"
            ) {
                onEdit(Constants.Editor.BOLD)
            }

            IconButtonWithTooltip(
                imageVector = Icons.Outlined.FormatItalic,
                contentDescription = stringResource(id = R.string.italic),
                shortCutDescription = "Ctrl + I"
            ) {
                onEdit(Constants.Editor.ITALIC)
            }

            IconButtonWithTooltip(
                imageVector = Icons.Outlined.FormatUnderlined,
                contentDescription = stringResource(id = R.string.underline),
                shortCutDescription = "Ctrl + U"
            ) {
                onEdit(Constants.Editor.UNDERLINE)
            }

            IconButtonWithTooltip(
                imageVector = Icons.Outlined.FormatStrikethrough,
                contentDescription = stringResource(id = R.string.strikethrough),
                shortCutDescription = "Ctrl + D"
            ) {
                onEdit(Constants.Editor.STRIKETHROUGH)
            }

            IconButtonWithTooltip(
                imageVector = Icons.Outlined.FormatPaint,
                contentDescription = stringResource(id = R.string.mark),
                shortCutDescription = "Ctrl + M"
            ) {
                onEdit(Constants.Editor.MARK)
            }

            IconButtonWithTooltip(
                imageVector = Icons.Outlined.Code,
                contentDescription = stringResource(id = R.string.code),
                shortCutDescription = "Ctrl + Shift + K"
            ) {
                onEdit(Constants.Editor.INLINE_CODE)
            }

            IconButtonWithTooltip(
                imageVector = Icons.Outlined.DataArray,
                contentDescription = "Brackets"
            ) {
                onEdit(Constants.Editor.INLINE_BRACKETS)
            }

            IconButtonWithTooltip(
                imageVector = Icons.Outlined.DataObject,
                contentDescription = "Braces"
            ) {
                onEdit(Constants.Editor.INLINE_BRACES)
            }

            IconButtonWithTooltip(
                painter = R.drawable.function,
                contentDescription = stringResource(id = R.string.math),
                shortCutDescription = "Ctrl + Shift + M"
            ) {
                onEdit(Constants.Editor.INLINE_MATH)
            }

            IconButtonWithTooltip(
                imageVector = Icons.Outlined.FormatQuote,
                contentDescription = stringResource(id = R.string.quote),
                shortCutDescription = "Ctrl + Shift + Q"
            ) {
                onEdit(Constants.Editor.QUOTE)
            }

            IconButtonWithTooltip(
                imageVector = Icons.Outlined.HorizontalRule,
                contentDescription = stringResource(id = R.string.horizontal_rule),
                shortCutDescription = "Ctrl + Shift + R"
            ) {
                onEdit(Constants.Editor.RULE)
            }

            IconButtonWithTooltip(
                imageVector = Icons.Outlined.TableChart,
                contentDescription = stringResource(id = R.string.table),
                shortCutDescription = "Ctrl + T",
                onClick = onTableButtonClick
            )

            IconButtonWithTooltip(
                imageVector = Icons.Outlined.AddChart,
                contentDescription = stringResource(id = R.string.mermaid_diagram),
                shortCutDescription = "Ctrl + Shift + D"
            ) {
                onEdit(Constants.Editor.DIAGRAM)
            }

            IconButtonWithTooltip(
                imageVector = Icons.Outlined.CheckBox,
                contentDescription = stringResource(id = R.string.task_list),
                shortCutDescription = "Ctrl + Shift + T",
                onClick = onTaskButtonClick
            )

            IconButtonWithTooltip(
                imageVector = Icons.Outlined.Link,
                contentDescription = stringResource(id = R.string.link),
                shortCutDescription = "Ctrl + K",
                onClick = onLinkButtonClick
            )

            IconButtonWithTooltip(
                imageVector = Icons.Outlined.DocumentScanner,
                contentDescription = stringResource(id = R.string.scan),
                shortCutDescription = "Ctrl + S",
                onClick = onScanButtonClick
            )
        }
    }
}
