package com.yangdai.opennote.presentation.screen

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.theme.Blue
import com.yangdai.opennote.presentation.theme.Cyan
import com.yangdai.opennote.presentation.theme.Green
import com.yangdai.opennote.presentation.theme.Orange
import com.yangdai.opennote.presentation.theme.Purple
import com.yangdai.opennote.presentation.theme.Red
import com.yangdai.opennote.presentation.theme.Yellow
import com.yangdai.opennote.presentation.util.BiometricPromptManager

@Composable
fun LoginOverlayScreen(
    promptManager: BiometricPromptManager
) {

    // 判断系统版本是否大于android 12
    val modifier: Modifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier
    } else {
        Modifier.background(MaterialTheme.colorScheme.surface)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {}
            .then(modifier),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineLarge.copy(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Red,
                        Orange,
                        Yellow,
                        Green,
                        Cyan,
                        Blue,
                        Purple
                    )
                )
            )
        )

        val title = stringResource(R.string.biometric_login)
        val subtitle = stringResource(R.string.log_in_using_your_biometric_credential)

        OutlinedButton(onClick = {
            promptManager.showBiometricPrompt(
                title = title,
                subtitle = subtitle
            )
        }) {
            Text(text = stringResource(R.string.login))
        }
    }
}