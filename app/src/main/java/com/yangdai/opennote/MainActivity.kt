package com.yangdai.opennote

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View

import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.yangdai.opennote.presentation.screen.BaseScreen
import com.yangdai.opennote.presentation.util.BiometricPromptManager
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val promptManager by lazy { BiometricPromptManager(this) }
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                sharedViewModel.isLoadingDate.value
            }
            setOnExitAnimationListener { splashScreenView ->
                val view = splashScreenView.view
                // Create your custom animation.
                val animation = ObjectAnimator.ofFloat(
                    view,
                    View.ALPHA,
                    1f,
                    0f
                )

                animation.duration = 200L

                // Call SplashScreenView.remove at the end of your custom animation.
                animation.doOnEnd { splashScreenView.remove() }

                // Run your animation.
                animation.start()
            }
        }

        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        setContent {
            val windowSize = calculateWindowSizeClass(this)
            val isLargeScreen by remember {
                derivedStateOf { windowSize.widthSizeClass == WindowWidthSizeClass.Expanded && windowSize.heightSizeClass > WindowHeightSizeClass.Compact }
            }
            BaseScreen(
                sharedViewModel = sharedViewModel,
                promptManager = promptManager,
                isLargeScreen = isLargeScreen
            )
        }
    }
}
