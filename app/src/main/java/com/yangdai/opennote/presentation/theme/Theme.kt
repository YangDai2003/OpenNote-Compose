package com.yangdai.opennote.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.yangdai.opennote.presentation.state.AppColor

fun darkenColor(color: Color, factor: Float): Color {
    return lerp(color, Color.Black, factor)
}

@Composable
fun OpenNoteTheme(
    darkMode: Boolean = isSystemInDarkTheme(),
    amoledMode: Boolean = false,
    color: AppColor = AppColor.DYNAMIC,
    content: @Composable () -> Unit
) {
    var colorScheme = when (color) {
        AppColor.PURPLE -> if (darkMode) DarkPurpleColors else LightPurpleColors
        AppColor.BLUE -> if (darkMode) DarkBlueColors else LightBlueColors
        AppColor.GREEN -> if (darkMode) DarkGreenColors else LightGreenColors
        AppColor.ORANGE -> if (darkMode) DarkOrangeColors else LightOrangeColors
        AppColor.RED -> if (darkMode) DarkRedColors else LightRedColors

        else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val context = LocalContext.current
            if (darkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            if (darkMode) DarkPurpleColors else LightPurpleColors
        }
    }

    if (darkMode && amoledMode) {
        val factor = 0.5f
        colorScheme = colorScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceVariant = darkenColor(colorScheme.surfaceVariant, factor),
            surfaceContainerLowest = darkenColor(colorScheme.surfaceContainerLowest, factor),
            surfaceContainerLow = darkenColor(colorScheme.surfaceContainerLow, factor),
            surfaceContainer = darkenColor(colorScheme.surfaceContainer, factor),
            surfaceContainerHigh = darkenColor(colorScheme.surfaceContainerHigh, factor),
            surfaceContainerHighest = darkenColor(colorScheme.surfaceContainerHighest, factor),
            surfaceDim = darkenColor(colorScheme.surfaceDim, factor),
            surfaceBright = darkenColor(colorScheme.surfaceBright, factor),
            scrim = darkenColor(colorScheme.scrim, factor),
            inverseSurface = darkenColor(colorScheme.inverseSurface, factor),
            errorContainer = darkenColor(colorScheme.errorContainer, factor),
            tertiaryContainer = darkenColor(colorScheme.tertiaryContainer, factor),
            secondaryContainer = darkenColor(colorScheme.secondaryContainer, factor),
            primaryContainer = darkenColor(colorScheme.primaryContainer, factor)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                !darkMode
        }
    }

//    val factor = 0.6f
//
//    val openNoteTypography = MaterialTheme.typography.copy(
//        displayLarge = MaterialTheme.typography.displayLarge.copy(fontSize = MaterialTheme.typography.displayLarge.fontSize * factor),
//        displayMedium = MaterialTheme.typography.displayMedium.copy(fontSize = MaterialTheme.typography.displayMedium.fontSize * factor),
//        displaySmall = MaterialTheme.typography.displaySmall.copy(fontSize = MaterialTheme.typography.displaySmall.fontSize * factor),
//        headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontSize = MaterialTheme.typography.headlineLarge.fontSize * factor),
//        headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontSize = MaterialTheme.typography.headlineMedium.fontSize * factor),
//        headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontSize = MaterialTheme.typography.headlineSmall.fontSize * factor),
//        titleLarge = MaterialTheme.typography.titleLarge.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize * factor),
//        titleMedium = MaterialTheme.typography.titleMedium.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize * factor),
//        titleSmall = MaterialTheme.typography.titleSmall.copy(fontSize = MaterialTheme.typography.titleSmall.fontSize * factor),
//        bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontSize = MaterialTheme.typography.bodyLarge.fontSize * factor),
//        bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize * factor),
//        bodySmall = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * factor),
//        labelLarge = MaterialTheme.typography.labelLarge.copy(fontSize = MaterialTheme.typography.labelLarge.fontSize * factor),
//        labelMedium = MaterialTheme.typography.labelMedium.copy(fontSize = MaterialTheme.typography.labelMedium.fontSize * factor),
//        labelSmall = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * factor)
//    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
