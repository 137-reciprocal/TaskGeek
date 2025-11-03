package com.taskhero.data.backup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.taskhero.domain.backup.DriveBackupRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager worker for automatic Google Drive backups.
 * Runs daily at configured time to backup database.
 */
@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val backupRepository: DriveBackupRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "BackupWorker"
        private const val NOTIFICATION_CHANNEL_ID = "backup_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "Backup Notifications"
        private const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        createNotificationChannel()

        return try {
            // Perform backup
            val result = backupRepository.backupToGoogleDrive()

            if (result.isSuccess) {
                val fileId = result.getOrNull()
                showNotification(
                    title = "Backup Successful",
                    message = "Database backed up to Google Drive",
                    isSuccess = true
                )
                Result.success()
            } else {
                val error = result.exceptionOrNull()
                showNotification(
                    title = "Backup Failed",
                    message = error?.message ?: "Unknown error occurred",
                    isSuccess = false
                )
                Result.retry()
            }
        } catch (e: Exception) {
            showNotification(
                title = "Backup Failed",
                message = e.message ?: "Unknown error occurred",
                isSuccess = false
            )
            Result.retry()
        }
    }

    /**
     * Create notification channel for backup notifications.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for automatic backups"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Show notification for backup result.
     */
    private fun showNotification(title: String, message: String, isSuccess: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
