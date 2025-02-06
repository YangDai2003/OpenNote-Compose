package com.yangdai.opennote.presentation.screen

import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.component.TypingText
import com.yangdai.opennote.presentation.component.login.LogoText
import com.yangdai.opennote.presentation.component.login.NeonIndication
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.presentation.util.getOrCreateDirectory

@Preview(showBackground = true)
@Composable
fun PermissionScreenPreview() {
    PermissionScreen(
        onPermissionResult = {}
    )
}

@Composable
fun PermissionScreen(onPermissionResult: (String) -> Unit) {

    val context = LocalContext.current

    // 文件夹选择器
    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            // 获取持久化权限
            context.applicationContext.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            // 获取Open Note目录
            val openNoteDir =
                getOrCreateDirectory(context, uri, Constants.File.OPENNOTE)
            openNoteDir?.let { dir ->
                getOrCreateDirectory(context, dir.uri, Constants.File.OPENNOTE_IMAGES)
                getOrCreateDirectory(context, dir.uri, Constants.File.OPENNOTE_BACKUP)
                getOrCreateDirectory(context, dir.uri, Constants.File.OPENNOTE_TEMPLATES)
            }
            onPermissionResult(uri.toString())
        }
    }

    Column(
        modifier = Modifier
            .pointerInput(Unit) {}
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LogoText()

        Spacer(modifier = Modifier.height(64.dp))

        Text(
            modifier = Modifier.widthIn(max = 600.dp),
            text = stringResource(R.string.storage),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(16.dp))

        TypingText(
            modifier = Modifier.widthIn(max = 600.dp),
            text = stringResource(R.string.storage_detail, Constants.File.OPENNOTE),
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainer, shape = CircleShape)
                .defaultMinSize(
                    minWidth = 58.dp,
                    minHeight = 48.dp
                )
                .clickable(
                    enabled = true,
                    indication = NeonIndication(CircleShape, 3.dp),
                    interactionSource = null,
                    onClick = {
                        folderPicker.launch(getDocumentDirectoryUri())
                    }
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.open_file_manager),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

/**
 * Helper function to get the URI for the Documents directory.
 */
private fun getDocumentDirectoryUri(): Uri? {
    val documentsDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    return if (documentsDir != null) {
        Uri.fromFile(documentsDir)
    } else {
        null
    }
}