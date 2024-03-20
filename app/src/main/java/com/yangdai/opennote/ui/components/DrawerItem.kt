package com.yangdai.opennote.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DrawerItem(
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    label: String,
    badge: String,
    selected: Boolean,
    onClick: () -> Unit
) = NavigationDrawerItem(
    icon = {
        Icon(
            modifier = Modifier.padding(horizontal = 12.dp),
            imageVector = icon,
            tint = iconTint,
            contentDescription = ""
        )
    },
    label = {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    },
    badge = {
        Text(
            text = badge,
            style = MaterialTheme.typography.labelMedium
        )
    },
    selected = selected,
    onClick = onClick
)