package com.yangdai.opennote.presentation.component.note

import android.util.Log
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.ViewSidebar
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowDpSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.yangdai.opennote.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun NoteSideSheet(
    modifier: Modifier = Modifier,
    isDrawerOpen: Boolean,
    onDismiss: () -> Unit,
    isLargeScreen: Boolean,
    animationDuration: Int = 300,
    maskColor: Color = Color.Black.copy(alpha = 0.6f),
    showMask: Boolean = false,
    cornerRadius: Dp = 32.dp,
    drawerContent: @Composable ColumnScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val windowSize = currentWindowDpSize()

    val drawerWidth = remember(windowSize, isLargeScreen) {
        if (isLargeScreen) {
            windowSize.width / 3
        } else {
            windowSize.width * 2 / 3
        }
    }

    // Width of the drawer in pixels
    val drawerWidthPx = remember(density, drawerWidth) { with(density) { drawerWidth.toPx() } }

    // Offset for the drawer animation
    val offsetX = remember { Animatable(if (isDrawerOpen) 0f else drawerWidthPx) }

    // Launch animation when the drawer state changes
    LaunchedEffect(isDrawerOpen) {
        val targetOffsetX = if (isDrawerOpen) 0f else drawerWidthPx
        offsetX.animateTo(
            targetValue = targetOffsetX, animationSpec = tween(durationMillis = animationDuration)
        )
    }

    PredictiveBackHandler(enabled = isDrawerOpen) { progress: Flow<BackEventCompat> ->      // code for gesture back started
        try {
            progress.collect { event ->
                scope.launch {
                    val newOffset = drawerWidthPx + drawerWidthPx * (event.progress - 1f)
                    offsetX.snapTo(newOffset)
                }
            }
            onDismiss()
        } catch (_: CancellationException) {
            offsetX.animateTo(0f)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {

        // Mask overlay when the drawer is open
        if (isDrawerOpen && showMask) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(maskColor)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onDismiss() })
                    }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(drawerWidth)
                .offset { IntOffset(x = offsetX.value.roundToInt(), y = 0) }
                .align(Alignment.CenterEnd)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = cornerRadius, bottomStart = cornerRadius)
                )
                .pointerInput(Unit) {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.overview),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = painterResource(R.drawable.right_panel_close),
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = "Close"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                drawerContent()
            }
        }
    }
}

@Composable
fun NoteSideSheetItem(
    key: String,
    value: String,
    shouldFormat: Boolean = true
) = Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Start
) {
    val formattedValue = remember(value, shouldFormat) {
        if (value.isBlank()) return@remember ""
        if (!value.isDigitsOnly() || !shouldFormat) return@remember value
        try {
            NumberFormat.getNumberInstance(Locale.getDefault()).format(value.toInt())
        } catch (e: IllegalArgumentException) {
            Log.e("NoteSideSheetItem", "Error formatting number: $value", e)
            value
        } catch (e: NumberFormatException) {
            Log.e(
                "NoteSideSheetItem",
                "String is not a valid representation of a number: $value",
                e
            )
            value
        }
    }

    val annotatedString = buildAnnotatedString {
        withStyle(
            SpanStyle(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            append("$key: ")
        }
        withStyle(
            SpanStyle(
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            append(formattedValue)
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge.copy(
            lineHeightStyle = LineHeightStyle(
                trim = LineHeightStyle.Trim.None,
                alignment = LineHeightStyle.Alignment.Bottom
            )
        )
    )
}

data class HeaderNode(
    val title: String,
    val level: Int,
    val range: IntRange,
    val children: MutableList<HeaderNode> = mutableListOf()
)

@Composable
fun OutlineView(
    outline: HeaderNode,
    onHeaderClick: (IntRange) -> Unit
) {
    LazyColumn {
        outline.children.forEach { header ->
            item {
                HeaderItem(header, 0, onHeaderClick)
            }
        }
    }
}

@Composable
private fun HeaderItem(
    header: HeaderNode,
    depth: Int,
    onHeaderClick: (IntRange) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(true) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 8).dp)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (header.children.isNotEmpty()) {
            Icon(
                modifier = Modifier.clickable {
                    if (header.children.isNotEmpty()) {
                        expanded = !expanded
                    }
                },
                imageVector = if (expanded)
                    Icons.Default.ArrowDropDown
                else
                    Icons.AutoMirrored.Filled.ArrowRight,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                contentDescription = null
            )
        } else {
            Spacer(modifier = Modifier.width(24.dp))
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onHeaderClick(header.range)
                },
            text = header.title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )
    }

    if (expanded) {
        header.children.forEach { child ->
            HeaderItem(child, depth + 1, onHeaderClick)
        }
    }
}

@Preview
@Composable
private fun CustomSideDrawerOverlayPreview() {
    var isDrawerOpen by remember { mutableStateOf(false) }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isDrawerOpen = true }
            ) {
                Icon(Icons.AutoMirrored.Filled.ViewSidebar, contentDescription = "Open Drawer")
            }
        }
    ) { padding ->
        NoteSideSheet(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            isDrawerOpen = isDrawerOpen,
            onDismiss = { isDrawerOpen = false },
            isLargeScreen = false,
            showMask = true,
            drawerContent = {
                NoteSideSheetItem("Key", "Value")
            })
    }
}
