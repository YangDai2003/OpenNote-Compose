package com.yangdai.opennote

import android.app.Activity
import android.os.Bundle
import com.yangdai.opennote.presentation.navigation.Screen
import com.yangdai.opennote.presentation.util.Constants.LINK
import com.yangdai.opennote.presentation.util.sendPendingIntent

class ManageSpaceActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.sendPendingIntent("$LINK/${Screen.Settings.route}")
        finish()
    }
}
