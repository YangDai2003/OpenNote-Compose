package com.yangdai.opennote

import android.app.Application
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import com.yangdai.opennote.presentation.util.Constants
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NoteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val shortcut = ShortcutInfoCompat.Builder(applicationContext, "id1")
            .setShortLabel(getString(R.string.compose))
            .setLongLabel(getString(R.string.compose))
            .setIcon(IconCompat.createWithResource(applicationContext, R.drawable.compose))
            .setIntent(
                Intent(applicationContext, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = ("${Constants.LINK}/note/-1").toUri()
                }
            )
            .build()
        ShortcutManagerCompat.pushDynamicShortcut(applicationContext, shortcut)
    }
}
