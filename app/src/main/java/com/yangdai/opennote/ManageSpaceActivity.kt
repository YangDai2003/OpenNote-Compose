package com.yangdai.opennote

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import com.yangdai.opennote.presentation.util.Constants.LINK

class ManageSpaceActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, MainActivity::class.java).setData("$LINK/settings".toUri())
        val pendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        pendingIntent?.send()
        finish()
    }
}