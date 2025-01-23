package com.yangdai.opennote.presentation.component.setting

import android.app.KeyguardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.component.dialog.FolderListDialog
import com.yangdai.opennote.presentation.component.dialog.ProgressDialog
import com.yangdai.opennote.presentation.component.dialog.WarningDialog
import com.yangdai.opennote.presentation.event.DatabaseEvent
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel

@Composable
fun DataPane(sharedViewModel: SharedViewModel) {

    val context = LocalContext.current
    val folderNoteCountsState by sharedViewModel.folderWithNoteCountsFlow.collectAsStateWithLifecycle()
    val actionState by sharedViewModel.dataActionStateFlow.collectAsStateWithLifecycle()
    val settingsState by sharedViewModel.settingsStateFlow.collectAsStateWithLifecycle()

    var showWarningDialog by rememberSaveable { mutableStateOf(false) }
    var showFolderDialog by rememberSaveable { mutableStateOf(false) }

    var folderId: Long? by rememberSaveable { mutableStateOf(null) }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uriList ->
        if (uriList.isNotEmpty()) {
            val contentResolver = context.contentResolver
            sharedViewModel.onDatabaseEvent(
                DatabaseEvent.Import(
                    contentResolver,
                    folderId,
                    uriList
                )
            )
        }
    }

    val recoveryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val contentResolver = context.contentResolver
            sharedViewModel.onDatabaseEvent(DatabaseEvent.Recovery(contentResolver, it))
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        SettingsHeader(text = stringResource(R.string.data))

        ListItem(
            modifier = Modifier.clickable {
                showFolderDialog = true
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.FileDownload,
                    contentDescription = "Import"
                )
            },
            headlineContent = { Text(text = stringResource(R.string.import_files)) },
            supportingContent = {
                Text(
                    text = stringResource(R.string.import_files_description)
                )
            }
        )

        ListItem(
            modifier = Modifier.clickable {
                sharedViewModel.onDatabaseEvent(DatabaseEvent.Backup(context.contentResolver))
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Backup,
                    contentDescription = "Backup"
                )
            },
            headlineContent = { Text(text = stringResource(id = R.string.backup)) },
            supportingContent = {
                Text(
                    text = stringResource(R.string.backup_description)
                )
            }
        )

        ListItem(
            modifier = Modifier.clickable {
                recoveryLauncher.launch(arrayOf("application/json"))
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Restore,
                    contentDescription = "Restore"
                )
            },
            headlineContent = { Text(text = stringResource(id = R.string.recovery)) },
            supportingContent = {
                Text(
                    text = stringResource(R.string.restore_description)
                )
            }
        )

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.CleaningServices,
                    contentDescription = "Clear"
                )
            },
            headlineContent = { Text(text = stringResource(R.string.reset_database)) },
            trailingContent = {
                TextButton(
                    onClick = {
                        showWarningDialog = true
                    },
                    colors = ButtonDefaults.textButtonColors().copy(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(text = stringResource(id = R.string.reset))
                }
            },
            supportingContent = {
                Text(
                    text = stringResource(R.string.reset_database_description)
                )
            }
        )

        SettingsHeader(text = stringResource(R.string.security))

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Fingerprint,
                    contentDescription = "Password"
                )
            },
            headlineContent = { Text(text = stringResource(R.string.password)) },
            trailingContent = {
                Switch(
                    checked = settingsState.needPassword,
                    onCheckedChange = { checked ->
                        val keyguardManager =
                            context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                        if (keyguardManager.isKeyguardSecure) {
                            sharedViewModel.putPreferenceValue(
                                Constants.Preferences.NEED_PASSWORD,
                                checked
                            )
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.no_password_set),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )
            },
            supportingContent = {
                Text(
                    text = stringResource(R.string.password_description)
                )
            }
        )
    }

    if (showWarningDialog) {
        WarningDialog(
            message = stringResource(R.string.reset_database_warning),
            onDismissRequest = { showWarningDialog = false }
        ) {
            sharedViewModel.onDatabaseEvent(DatabaseEvent.Reset)
        }
    }

    if (showFolderDialog) {
        FolderListDialog(
            hint = stringResource(R.string.destination_folder),
            oFolderId = folderId,
            folders = folderNoteCountsState.map { it.first },
            onDismissRequest = { showFolderDialog = false }
        ) { id ->
            folderId = id
            importLauncher.launch(arrayOf("text/*"))
        }
    }

    ProgressDialog(
        isLoading = actionState.loading,
        progress = actionState.progress,
        infinite = actionState.infinite,
        errorMessage = actionState.error,
        onDismissRequest = sharedViewModel::cancelDataAction
    )
}
