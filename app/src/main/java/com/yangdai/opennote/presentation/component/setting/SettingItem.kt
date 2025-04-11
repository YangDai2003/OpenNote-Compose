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
    leadingContent: @Composable (() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(containerColor = Color.Transparent),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation
) = ListItem(
    modifier = modifier,
    headlineContent = {
        Text(
            text = headlineText,
            maxLines = 1
        )
    },
    supportingContent = {
        Text(
            text = supportingText,
            maxLines = 1,
            modifier = Modifier.basicMarquee()
        )
    },
    leadingContent = leadingContent,
    colors = colors,
    tonalElevation = tonalElevation,
    shadowElevation = shadowElevation
)

@Preview
@Composable
fun SettingItemPreview() {
    SettingItem(
        headlineText = "Headline",
        supportingText = "Supporting"
    )
}