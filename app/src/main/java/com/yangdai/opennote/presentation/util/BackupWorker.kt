package com.yangdai.opennote.presentation.util

import android.content.Context
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yangdai.opennote.R
import com.yangdai.opennote.data.di.AppModule
import com.yangdai.opennote.data.local.entity.BackupData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter

enum class BackupFrequency(val days: Int, val textRes: Int) {
    NEVER(0, R.string.never),
    DAILY(1, R.string.daily),
    WEEKLY(7, R.string.weekly),
    MONTHLY(30, R.string.monthly);
}

class BackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val context = applicationContext
            val dataStoreRepository = AppModule.provideAppDataStoreRepository(context)
            val database = AppModule.provideNoteDatabase(context)
            val noteRepository = AppModule.provideNoteRepository(database)
            val folderRepository = AppModule.provideFolderRepository(database)
            val useCases = AppModule.provideNoteUseCases(noteRepository, folderRepository)

            val rootUri =
                dataStoreRepository.getStringValue(Constants.Preferences.STORAGE_PATH, "")
                    .toUri()
            // 获取Open Note目录
            val openNoteDir =
                getOrCreateDirectory(context, rootUri, Constants.File.OPENNOTE)
            // 获取Backup目录
            val backupDir = openNoteDir?.let { dir ->
                getOrCreateDirectory(context, dir.uri, Constants.File.OPENNOTE_BACKUP)
            }
            backupDir?.let { dir ->
                val notes = useCases.getNotes().first()
                val folders = useCases.getFolders().first()
                val backupData = BackupData(notes, folders)
                val json = Json.encodeToString(backupData)
                val encryptedJson = encryptBackupData(json)

                val fileName = "${System.currentTimeMillis()}.json"
                val file = dir.createFile("application/json", fileName)

                file?.let { docFile ->
                    context.contentResolver.openOutputStream(docFile.uri)
                        ?.use { outputStream ->
                            OutputStreamWriter(outputStream).use { writer ->
                                writer.write(encryptedJson)
                            }
                        }
                }
            }

            Result.success()
        } catch (_: Exception) {
            Result.failure()
        }
    }
}
