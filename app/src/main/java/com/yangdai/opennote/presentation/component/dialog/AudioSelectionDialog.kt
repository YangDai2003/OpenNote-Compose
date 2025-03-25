package com.yangdai.opennote.presentation.component.dialog

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.documentfile.provider.DocumentFile
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.component.TextOptionButton
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.presentation.util.getOrCreateDirectory
import com.yangdai.opennote.presentation.util.hasFileWithName

@Composable
fun AudioSelectionDialog(
    rootUri: Uri,
    onDismiss: () -> Unit,
    onAudioSelected: (String) -> Unit
) {
    val context = LocalContext.current

    // 文件选择器启动器
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            saveAudioToCustomFolder(context, it, rootUri, onAudioSelected)
        }
    }

    // 录音机启动器
    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.data?.let { uri ->
            saveAudioToCustomFolder(context, uri, rootUri, onAudioSelected)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.audio)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextOptionButton(
                    buttonText = stringResource(R.string.select_audio),
                    onButtonClick = {
                        audioPickerLauncher.launch("audio/*")
                    }
                )

                TextOptionButton(
                    buttonText = stringResource(R.string.record_audio),
                    onButtonClick = {
                        val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
                        try {
                            recordAudioLauncher.launch(intent)
                        } catch (e: ActivityNotFoundException) {
                            // No activity found to handle the intent
                            e.printStackTrace()
                        }
                    }
                )
            }
        },
        confirmButton = {}
    )
}

private fun saveAudioToCustomFolder(
    context: Context,
    sourceUri: Uri,
    rootUri: Uri,
    onAudioSelected: (String) -> Unit
) {
    try {
        val openNoteDir =
            getOrCreateDirectory(context, rootUri, Constants.File.OPENNOTE)
        val audioDir = openNoteDir?.let { dir ->
            getOrCreateDirectory(context, dir.uri, Constants.File.OPENNOTE_AUDIO)
        }

        audioDir?.let { dir ->
            val sourceFile = DocumentFile.fromSingleUri(context, sourceUri)
            val sourceFileName = sourceFile?.name

            // 检查目标文件夹中是否存在同名文件
            if (sourceFileName != null && hasFileWithName(dir, sourceFileName)) {
                onAudioSelected(sourceFileName)
                return
            }

            // 如果文件不在目标文件夹，继续原有的保存逻辑
            val extension = getFileExtension(context, sourceUri)
            val fileName = "audio_${System.currentTimeMillis()}.$extension"
            val mimeType = sourceFile?.type ?: "audio/*"

            dir.createFile(mimeType, fileName)?.let { newFile ->
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    context.contentResolver.openOutputStream(newFile.uri)?.use { output ->
                        input.copyTo(output)
                    }
                }
                onAudioSelected(fileName)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// 使用 DocumentFile API 获取扩展名
private fun getFileExtension(context: Context, uri: Uri): String {
    val sourceFile = DocumentFile.fromSingleUri(context, uri)

    // 尝试从文件名获取扩展名
    sourceFile?.name?.let { name ->
        val lastDot = name.lastIndexOf('.')
        if (lastDot > 0) {
            return name.substring(lastDot + 1)
        }
    }

    // 如果从文件名无法获取扩展名，则从 MIME 类型推断
    val mimeType = sourceFile?.type ?: context.contentResolver.getType(uri)
    return when (mimeType) {
        "audio/mpeg" -> "mp3"
        "audio/wav", "audio/x-wav" -> "wav"
        "audio/ogg" -> "ogg"
        "audio/aac" -> "aac"
        "audio/x-m4a" -> "m4a"
        else -> "mp3"  // 默认扩展名
    }
}
