package com.yangdai.opennote.presentation.component.setting

import androidx.compose.foundation.basicMarquee
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp

@Composable
fun SettingItem(
    headlineText: String,
    supportingText: String,
    modifier: Modifier = Modifier,
    overlineContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(containerColor = Color.Transparent),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation
) = ListItem(
    headlineContent = { Text(text = headlineText) },
    supportingContent = {
        Text(
            text = supportingText,
            maxLines = 1,
            modifier = Modifier.basicMarquee()
        )
    },
    modifier = modifier,
    overlineContent = overlineContent,
    leadingContent = leadingContent,
    trailingContent = trailingContent,
    colors = colors,
    tonalElevation = tonalElevation,
    shadowElevation = shadowElevation
)

@Preview
@Composable
fun SettingItemPreview() {
    SettingItem(
        headlineText = "Headline",
        supportingText = "Supporting",
        leadingContent = { Text("Leading") },
        trailingContent = { Text("Trailing") }
    )
}