package com.yangdai.opennote.presentation.component.setting

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Dataset
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkManager
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.component.dialog.FolderListDialog
import com.yangdai.opennote.presentation.component.dialog.ProgressDialog
import com.yangdai.opennote.presentation.component.dialog.WarningDialog
import com.yangdai.opennote.presentation.event.DatabaseEvent
import com.yangdai.opennote.presentation.util.BackupFrequency
import com.yangdai.opennote.presentation.util.BackupManager
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel
import java.io.File

@Composable
fun DataPane(sharedViewModel: SharedViewModel) {

    val context = LocalContext.current
    val folderNoteCountsState by sharedViewModel.folderWithNoteCountsFlow.collectAsStateWithLifecycle()
    val actionState by sharedViewModel.dataActionStateFlow.collectAsStateWithLifecycle()
    val settingsState by sharedViewModel.settingsStateFlow.collectAsStateWithLifecycle()

    var showWarningDialog by rememberSaveable { mutableStateOf(false) }
    var showFolderDialog by rememberSaveable { mutableStateOf(false) }

    var folderId: Long? by rememberSaveable { mutableStateOf(null) }
    val importFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uriList ->
        if (uriList.isNotEmpty()) {
            sharedViewModel.onDatabaseEvent(
                DatabaseEvent.ImportFiles(
                    context.applicationContext,
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

    val hapticFeedback = LocalHapticFeedback.current

    Column(Modifier.verticalScroll(rememberScrollState())) {

        val parent = remember(settingsState.storagePath) {
            if (settingsState.storagePath.isEmpty()) null
            else
                DocumentFile.fromTreeUri(context, settingsState.storagePath.toUri())
        }

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Dataset,
                    contentDescription = "File Location"
                )
            },
            overlineContent = { Text(text = parent?.name.toString() + File.separator + Constants.File.OPENNOTE) },
            headlineContent = { Text(text = stringResource(R.string.root_directory_location)) },
            supportingContent = {
                Text(
                    text = stringResource(R.string.current_root_directory_location_of_the_file_repository)
                )
            },
            trailingContent = {
                TextButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                        sharedViewModel.putPreferenceValue(Constants.Preferences.STORAGE_PATH, "")
                    },
                    colors = ButtonDefaults.textButtonColors().copy(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(text = stringResource(R.string.change))
                }
            },
        )

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
            modifier = Modifier.clickable {
                sharedViewModel.onDatabaseEvent(DatabaseEvent.Backup(context.applicationContext))
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

        val workManager = remember { WorkManager.getInstance(context.applicationContext) }
        val backupManager = remember(workManager) { BackupManager(workManager) }

        // 将 slider 位置映射到备份频率的转换函数
        fun mapDaysToSliderPosition(days: Int): Float = when (days) {
            0 -> 0f
            1 -> 1f
            7 -> 2f
            30 -> 3f
            else -> 0f
        }

        fun mapSliderPositionToFrequency(position: Float): BackupFrequency = when (position) {
            0f -> BackupFrequency.NEVER
            1f -> BackupFrequency.DAILY
            2f -> BackupFrequency.WEEKLY
            3f -> BackupFrequency.MONTHLY
            else -> BackupFrequency.NEVER
        }

        val sliderPosition = remember(settingsState.backupFrequency) {
            mapDaysToSliderPosition(settingsState.backupFrequency)
        }

        val selectedFrequency = remember(sliderPosition) {
            mapSliderPositionToFrequency(sliderPosition)
        }

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.EditCalendar,
                    contentDescription = "Auto backup"
                )
            },
            headlineContent = { Text(text = stringResource(R.string.frequency_backup)) },
            supportingContent = { Text(text = stringResource(selectedFrequency.textRes)) }
        )

        Slider(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            value = sliderPosition,
            onValueChange = { newPosition ->
                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                // 直接更新 ViewModel 中的值
                val newFrequency = mapSliderPositionToFrequency(newPosition)
                sharedViewModel.putPreferenceValue(
                    BackupManager.BACKUP_FREQUENCY_KEY,
                    newFrequency.days
                )
            },
            onValueChangeFinished = {
                // 仅在滑动结束时调度备份任务
                backupManager.scheduleBackup(selectedFrequency)
            },
            valueRange = 0f..3f,
            steps = 2
        )

        HorizontalDivider()

        ListItem(
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.quick_reference_all),
                    contentDescription = "Clean"
                )
            },
            headlineContent = { Text(text = stringResource(R.string.free_up_space)) },
            supportingContent = {
                Text(
                    text = stringResource(R.string.free_up_detail)
                )
            },
            trailingContent = {
                TextButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                        sharedViewModel.onDatabaseEvent(DatabaseEvent.RemoveUselessFiles(context.applicationContext))
                    },
                    colors = ButtonDefaults.textButtonColors().copy(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text(text = stringResource(R.string.clean))
                }
            },
        )

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.CleaningServices,
                    contentDescription = "Reset"
                )
            },
            headlineContent = { Text(text = stringResource(R.string.reset_database)) },
            trailingContent = {
                TextButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                        showWarningDialog = true
                    },
                    colors = ButtonDefaults.textButtonColors().copy(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
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

        Spacer(Modifier.navigationBarsPadding())
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
            importFilesLauncher.launch(arrayOf("text/*"))
        }
    }

    ProgressDialog(
        isLoading = actionState.loading,
        progress = actionState.progress,
        infinite = actionState.infinite,
        message = actionState.message,
        onDismissRequest = sharedViewModel::cancelDataAction
    )
}
