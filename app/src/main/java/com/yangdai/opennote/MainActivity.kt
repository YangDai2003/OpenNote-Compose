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
import com.yangdai.opennote.presentation.screen.BaseScreen
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val sharedViewModel: SharedViewModel by viewModels()

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

                animation.duration = 300L

                // Call SplashScreenView.remove at the end of your custom animation.
                animation.doOnEnd { splashScreenView.remove() }

                // Run your animation.
                animation.start()
            }
        }

        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        super.onCreate(savedInstanceState)

        setContent {
            BaseScreen()
        }
    }
}
