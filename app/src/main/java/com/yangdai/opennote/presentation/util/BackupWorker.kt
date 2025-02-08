package com.yangdai.opennote.presentation.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

// TODO 自动备份功能

enum class BackupFrequency(val days: Int) {
    NEVER(0),
    DAILY(1),
    WEEKLY(7),
    MONTHLY(30)
}

class BackupWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            // 调用现有的备份逻辑
            // 这里需要注入相关依赖
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}