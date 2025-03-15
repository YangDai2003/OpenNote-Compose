package com.yangdai.opennote.presentation.component.setting

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Commit
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.component.ConfettiEffect
import com.yangdai.opennote.presentation.component.CurlyCornerShape
import com.yangdai.opennote.presentation.component.dialog.RatingDialog
import com.yangdai.opennote.presentation.util.rememberCustomTabsIntent
import java.io.InputStream

@Composable
fun AboutPane() {

    val context = LocalContext.current
    val customTabsIntent = rememberCustomTabsIntent()
    var showRatingDialog by rememberSaveable { mutableStateOf(false) }
    var showSponsorDialog by rememberSaveable { mutableStateOf(false) }
    var showConfetti by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier
            .padding(horizontal = 8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName, 0
        )
        val version = packageInfo.versionName
        var pressAMP by remember { mutableFloatStateOf(16f) }
        val animatedPress by animateFloatAsState(
            targetValue = pressAMP, animationSpec = tween(), label = ""
        )

        val haptic = LocalHapticFeedback.current

        Box(
            modifier = Modifier
                .size(240.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CurlyCornerShape(curlAmplitude = animatedPress.toDouble()),
                )
                .shadow(
                    elevation = 10.dp,
                    shape = CurlyCornerShape(curlAmplitude = animatedPress.toDouble()),
                    ambientColor = MaterialTheme.colorScheme.primaryContainer,
                    spotColor = MaterialTheme.colorScheme.primaryContainer,
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            pressAMP = 0f
                            tryAwaitRelease()
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            pressAMP = 16f
                        },
                        onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showConfetti = true
                        }
                    )
                }
        ) {
            Image(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                contentDescription = "Icon"
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.version) + " " + version,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ListItem(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clip(CircleShape)
                .clickable { showSponsorDialog = true },
            colors = ListItemDefaults.colors()
                .copy(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.AttachMoney, contentDescription = "Donate"
                )
            },
            headlineContent = { Text(text = stringResource(R.string.sponsor)) })

        ListItem(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clip(CircleShape)
                .clickable {
                    customTabsIntent.launchUrl(
                        context, "https://github.com/YangDai2003/OpenNote-Compose".toUri()
                    )
                },
            colors = ListItemDefaults.colors()
                .copy(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Commit, contentDescription = "code"
                )
            },
            headlineContent = {
                Text(text = stringResource(R.string.source_code))
            })

        ListItem(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clip(CircleShape)
                .clickable {
                    customTabsIntent.launchUrl(
                        context, "https://github.com/YangDai2003/OpenNote-Compose/issues".toUri()
                    )
                },
            colors = ListItemDefaults.colors()
                .copy(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.BugReport, contentDescription = "bug"
                )
            },
            headlineContent = {
                Text(text = stringResource(R.string.report_a_bug_or_request_a_feature))
            })

        ListItem(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clip(CircleShape)
                .clickable {
                    customTabsIntent.launchUrl(
                        context,
                        "https://github.com/YangDai2003/OpenNote-Compose/blob/master/Guide.md".toUri()
                    )
                },
            colors = ListItemDefaults.colors()
                .copy(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.TipsAndUpdates, contentDescription = "Guide"
                )
            },
            headlineContent = {
                Text(text = stringResource(R.string.guide))
            })

        ListItem(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clip(CircleShape)
                .clickable {
                    customTabsIntent.launchUrl(
                        context,
                        "https://github.com/YangDai2003/OpenNote-Compose/blob/master/PRIVACY_POLICY.md".toUri()
                    )
                },
            colors = ListItemDefaults.colors()
                .copy(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.PrivacyTip, contentDescription = "Privacy Policy"
                )
            },
            headlineContent = { Text(text = stringResource(R.string.privacy_policy)) })

        ListItem(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clip(CircleShape)
                .clickable {
                    showRatingDialog = true
                },
            colors = ListItemDefaults.colors()
                .copy(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.StarRate, contentDescription = "Rate"
                )
            },
            headlineContent = { Text(text = stringResource(R.string.rate_this_app)) })

        ListItem(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clip(CircleShape)
                .clickable {
                    val sendIntent = Intent(Intent.ACTION_SEND)
                    sendIntent.setType("text/plain")
                    sendIntent.putExtra(
                        Intent.EXTRA_TITLE, context.getString(R.string.app_name)
                    )
                    sendIntent.putExtra(
                        Intent.EXTRA_TEXT, context.getString(R.string.shareContent)
                    )
                    context.startActivity(Intent.createChooser(sendIntent, null))
                },
            colors = ListItemDefaults.colors()
                .copy(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.IosShare, contentDescription = "Share"
                )
            },
            headlineContent = { Text(text = stringResource(R.string.share_this_app)) })

        Spacer(Modifier.navigationBarsPadding())
    }

    if (showSponsorDialog) {
        val assetManager = context.applicationContext.assets
        val inputStream1: InputStream = assetManager.open("alipay.jpg")
        val bitmap1: Bitmap = BitmapFactory.decodeStream(inputStream1)
        val inputStream2: InputStream = assetManager.open("wechat.png")
        val bitmap2: Bitmap = BitmapFactory.decodeStream(inputStream2)
        val inputStream3: InputStream = assetManager.open("paypal.jpg")
        val bitmap3: Bitmap = BitmapFactory.decodeStream(inputStream3)
        Dialog(
            onDismissRequest = { showSponsorDialog = false }, properties = DialogProperties(
                usePlatformDefaultWidth = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            ) {
                SettingsHeader("Alipay")
                Image(
                    bitmap = bitmap1.asImageBitmap(), contentDescription = "Alipay"
                )
                SettingsHeader("Wechat")
                Image(
                    bitmap = bitmap2.asImageBitmap(), contentDescription = "Wechat"
                )
                SettingsHeader("Paypal")
                Image(
                    bitmap = bitmap3.asImageBitmap(), contentDescription = "Paypal"
                )
            }
        }
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

    if (showConfetti) {
        ConfettiEffect()
    }
}
