package com.yangdai.opennote

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View

import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.yangdai.opennote.ui.screen.BaseScreen
import com.yangdai.opennote.ui.util.BiometricPromptManager
import com.yangdai.opennote.ui.viewmodel.BaseScreenViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val promptManager by lazy { BiometricPromptManager(this) }
    private val baseScreenViewModel: BaseScreenViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                baseScreenViewModel.isLoading.value
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
            BaseScreen(promptManager = promptManager, baseScreenViewModel = baseScreenViewModel)
        }
    }
}
