package com.yangdai.opennote.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow


@Composable
fun TopBarTitle(
    title: String
) = Text(
    text = title,
    overflow = TextOverflow.Ellipsis,
    maxLines = 1,
    style = MaterialTheme.typography.headlineMedium
)
