package com.yangdai.opennote.presentation.component.note

import android.net.Uri
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.presentation.util.getOrCreateDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TemplateFilesList(
    rootUri: Uri,
    saveCurrentNoteAsTemplate: () -> Unit,
    onFileSelected: (String) -> Unit
) {
    var templateFiles by remember {
        mutableStateOf<List<DocumentFile>>(emptyList())
    }

    val context = LocalContext.current.applicationContext

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val openNoteDir = getOrCreateDirectory(
                context, rootUri, Constants.File.OPENNOTE
            )
            val templatesFolder = openNoteDir?.let { dir ->
                getOrCreateDirectory(
                    context, dir.uri, Constants.File.OPENNOTE_TEMPLATES
                )
            }

            val files = templatesFolder?.listFiles()?.filter {
                it.name?.endsWith(".md") == true ||
                        it.name?.endsWith(".markdown") == true
            } ?: emptyList()

            templateFiles = files
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(templateFiles) { file ->
            ListItem(
                headlineContent = {
                    Text(
                        text = file.name ?: "Untitled",
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        // 读取文件内容
                        context.contentResolver.openInputStream(file.uri)?.use { input ->
                            val content = input.bufferedReader().use { it.readText() }
                            onFileSelected(content)
                        }
                    }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = {
                    saveCurrentNoteAsTemplate()
                }
            ) {
                Text(stringResource(R.string.saveAsTemplate))
            }
        }
    }
}
