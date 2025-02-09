package com.yangdai.opennote.presentation.util

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BackupManager(
    private val workManager: WorkManager
) {
    fun scheduleBackup(frequency: BackupFrequency) {
        if (frequency == BackupFrequency.NEVER) {
            // 如果设置为不备份，取消现有的备份任务
            workManager.cancelUniqueWork(BACKUP_WORK_NAME)
            return
        }

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            .build()

        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(
            frequency.days.toLong(), TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        // 使用 UPDATE 策略而不是 REPLACE
        workManager.enqueueUniquePeriodicWork(
            BACKUP_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            backupRequest
        )
    }

    companion object {
        const val BACKUP_WORK_NAME = "database_backup_work"
        const val BACKUP_FREQUENCY_KEY = "backup_frequency"
    }
}