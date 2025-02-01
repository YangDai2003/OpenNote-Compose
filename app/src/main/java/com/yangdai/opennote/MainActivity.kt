package com.yangdai.opennote

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyboardShortcutGroup
import android.view.KeyboardShortcutInfo
import android.view.Menu
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
                sharedViewModel.isLoading.value
            }
            setOnExitAnimationListener { splashScreenView ->
                val view = splashScreenView.view
                // Create your custom animation.
                val animation = ObjectAnimator.ofFloat(
                    view, View.ALPHA, 1f, 0f
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

    override fun onProvideKeyboardShortcuts(
        data: MutableList<KeyboardShortcutGroup>?, menu: Menu?, deviceId: Int
    ) {

        val noteEdit = KeyboardShortcutGroup(
            getString(R.string.note_editing), listOf(
                KeyboardShortcutInfo(
                    getString(android.R.string.selectAll), KeyEvent.KEYCODE_A, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(android.R.string.cut), KeyEvent.KEYCODE_X, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(android.R.string.copy), KeyEvent.KEYCODE_C, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(android.R.string.paste), KeyEvent.KEYCODE_V, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.undo), KeyEvent.KEYCODE_Z, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.redo), KeyEvent.KEYCODE_Y, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.bold), KeyEvent.KEYCODE_B, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.italic), KeyEvent.KEYCODE_I, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.underline), KeyEvent.KEYCODE_U, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.strikethrough), KeyEvent.KEYCODE_D, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.mark), KeyEvent.KEYCODE_M, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.table), KeyEvent.KEYCODE_T, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.link), KeyEvent.KEYCODE_K, KeyEvent.META_CTRL_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.preview), KeyEvent.KEYCODE_P, KeyEvent.META_CTRL_ON
                ),


                KeyboardShortcutInfo(
                    getString(R.string.list),
                    KeyEvent.KEYCODE_L,
                    KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.scan),
                    KeyEvent.KEYCODE_S,
                    KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.code),
                    KeyEvent.KEYCODE_K,
                    KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.quote),
                    KeyEvent.KEYCODE_Q,
                    KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.math),
                    KeyEvent.KEYCODE_M,
                    KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.horizontal_rule),
                    KeyEvent.KEYCODE_R,
                    KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.task_list),
                    KeyEvent.KEYCODE_T,
                    KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.mermaid_diagram),
                    KeyEvent.KEYCODE_D,
                    KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON
                ), KeyboardShortcutInfo(
                    getString(R.string.image),
                    KeyEvent.KEYCODE_I,
                    KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON
                )
            )
        )

        data?.add(noteEdit)
    }
}
