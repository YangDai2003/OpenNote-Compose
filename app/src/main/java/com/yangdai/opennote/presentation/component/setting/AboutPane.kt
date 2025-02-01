package com.yangdai.opennote.presentation.component.setting

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.component.CurlyCornerShape
import com.yangdai.opennote.presentation.component.dialog.RatingDialog
import com.yangdai.opennote.presentation.util.PaymentUtil
import com.yangdai.opennote.presentation.util.rememberCustomTabsIntent

@Composable
fun AboutPane() {

    val context = LocalContext.current
    val activity = LocalActivity.current
    val customTabsIntent = rememberCustomTabsIntent()
    var showRatingDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName, 0
        )
        val version = packageInfo.versionName
        var pressAMP by remember { mutableFloatStateOf(16f) }
        val animatedPress by animateFloatAsState(
            targetValue = pressAMP, animationSpec = tween(), label = ""
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val haptic = LocalHapticFeedback.current

            Box(
                modifier = Modifier
                    .size(240.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CurlyCornerShape(amp = animatedPress.toDouble()),
                    )
                    .shadow(
                        elevation = 10.dp,
                        shape = CurlyCornerShape(amp = animatedPress.toDouble()),
                        ambientColor = MaterialTheme.colorScheme.primaryContainer,
                        spotColor = MaterialTheme.colorScheme.primaryContainer,
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(onPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            pressAMP = 0f
                            tryAwaitRelease()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            pressAMP = 16f
                        })
                    },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    modifier = Modifier.size(180.dp),
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    contentDescription = "Icon"
                )
            }

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.version) + " " + version,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        ListItem(modifier = Modifier.clickable {
            customTabsIntent.launchUrl(
                context,
                "https://github.com/YangDai2003/OpenNote-Compose/blob/master/Guide.md".toUri()
            )
        }, leadingContent = {
            Icon(
                imageVector = Icons.Outlined.TipsAndUpdates, contentDescription = "Guide"
            )
        }, headlineContent = {
            Text(text = stringResource(R.string.guide))
        })

        ListItem(modifier = Modifier.clickable {
            customTabsIntent.launchUrl(
                context,
                "https://github.com/YangDai2003/OpenNote-Compose/blob/master/PRIVACY_POLICY.md".toUri()
            )
        }, leadingContent = {
            Icon(
                imageVector = Icons.Outlined.PrivacyTip, contentDescription = "Privacy Policy"
            )
        }, headlineContent = { Text(text = stringResource(R.string.privacy_policy)) })

        ListItem(modifier = Modifier.clickable {
            showRatingDialog = true
        }, leadingContent = {
            Icon(
                imageVector = Icons.Outlined.StarRate, contentDescription = "Rate"
            )
        }, headlineContent = { Text(text = stringResource(R.string.rate_this_app)) })

        ListItem(modifier = Modifier.clickable {
            val sendIntent = Intent(Intent.ACTION_SEND)
            sendIntent.setType("text/plain")
            sendIntent.putExtra(
                Intent.EXTRA_TITLE, context.getString(R.string.app_name)
            )
            sendIntent.putExtra(
                Intent.EXTRA_TEXT, context.getString(R.string.shareContent)
            )
            context.startActivity(Intent.createChooser(sendIntent, null))
        }, leadingContent = {
            Icon(
                imageVector = Icons.Outlined.IosShare, contentDescription = "Share"
            )
        }, headlineContent = { Text(text = stringResource(R.string.share_this_app)) })

        ListItem(modifier = Modifier.clickable {
            runCatching {
                if (PaymentUtil.isInstalledPackage(context.applicationContext)) {
                    PaymentUtil.startAlipayClient(activity, "fkx17585uzuubvggq3eij18")
                } else {
                    val donateIntent = Intent(
                        Intent.ACTION_VIEW,
                        "https://paypal.me/YangDaiDevelpoer?country.x=DE&locale.x=de_DE".toUri()
                    )
                    context.startActivity(donateIntent)
                }
            }.onFailure {
                Toast.makeText(context, "Please install Paypal or Alipay.", Toast.LENGTH_SHORT)
                    .show()
            }
        }, leadingContent = {
            Icon(
                imageVector = Icons.Outlined.AttachMoney, contentDescription = "Donate"
            )
        }, headlineContent = { Text(text = stringResource(R.string.sponsor)) })
    }

    if (showRatingDialog) {
        RatingDialog(onDismissRequest = { showRatingDialog = false }) { stars ->
            if (stars > 3) {
                customTabsIntent.launchUrl(
                    context,
                    "https://play.google.com/store/apps/details?id=com.yangdai.opennote".toUri()
                )
            } else {
                if (stars == 0) return@RatingDialog
                // 获取当前应用的版本号
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val appVersion = packageInfo.versionName
                val deviceModel = Build.MODEL
                val systemVersion = Build.VERSION.SDK_INT

                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:".toUri()
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("dy15800837435@gmail.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "Feedback - Open Note")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Version: $appVersion\nDevice: $deviceModel\nSystem: $systemVersion\n"
                    )
                }
                context.startActivity(
                    Intent.createChooser(
                        emailIntent, "Feedback (E-mail)"
                    )
                )
            }
        }
    }
}
