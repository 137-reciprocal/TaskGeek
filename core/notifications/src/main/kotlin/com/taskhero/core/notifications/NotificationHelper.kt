package com.taskhero.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.taskhero.domain.task.model.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for creating and managing notifications.
 * Handles notification channels, permission checks, and notification display.
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    companion object {
        private const val CHANNEL_ID_TASK_DUE = "task_due_channel"
        private const val CHANNEL_ID_TASK_REMINDER = "task_reminder_channel"
        private const val CHANNEL_ID_LEVEL_UP = "level_up_channel"

        private const val NOTIFICATION_ID_TASK_DUE = 1001
        private const val NOTIFICATION_ID_TASK_REMINDER = 1002
        private const val NOTIFICATION_ID_LEVEL_UP = 1003

        const val ACTION_COMPLETE_TASK = "com.taskhero.action.COMPLETE_TASK"
        const val ACTION_SNOOZE = "com.taskhero.action.SNOOZE"
        const val EXTRA_TASK_UUID = "extra_task_uuid"
    }

    /**
     * Creates notification channels for different notification types.
     * Should be called when the app starts.
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Task Due Channel
            val taskDueChannel = NotificationChannel(
                CHANNEL_ID_TASK_DUE,
                "Task Due",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for tasks that are due soon or overdue"
                enableVibration(true)
            }

            // Task Reminder Channel
            val taskReminderChannel = NotificationChannel(
                CHANNEL_ID_TASK_REMINDER,
                "Task Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for upcoming tasks"
                enableVibration(true)
            }

            // Level Up Channel
            val levelUpChannel = NotificationChannel(
                CHANNEL_ID_LEVEL_UP,
                "Level Up",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for level ups and achievements"
                enableVibration(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(taskDueChannel)
            manager.createNotificationChannel(taskReminderChannel)
            manager.createNotificationChannel(levelUpChannel)
        }
    }

    /**
     * Shows a notification for a task that is due.
     */
    fun showTaskDueNotification(task: Task) {
        if (!hasNotificationPermission()) {
            return
        }

        val completeIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_COMPLETE_TASK
            putExtra(EXTRA_TASK_UUID, task.uuid)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            task.uuid.hashCode(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_TASK_UUID, task.uuid)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            task.uuid.hashCode() + 1,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TASK_DUE)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Task Due: ${task.description}")
            .setContentText(getTaskDueMessage(task))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Complete",
                completePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_recent_history,
                "Snooze",
                snoozePendingIntent
            )
            .build()

        notificationManager.notify(NOTIFICATION_ID_TASK_DUE + task.uuid.hashCode(), notification)
    }

    /**
     * Shows a reminder notification for an upcoming task.
     */
    fun showTaskReminderNotification(task: Task) {
        if (!hasNotificationPermission()) {
            return
        }

        val completeIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_COMPLETE_TASK
            putExtra(EXTRA_TASK_UUID, task.uuid)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            task.uuid.hashCode(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TASK_REMINDER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Reminder: ${task.description}")
            .setContentText("This task is coming up soon")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Complete",
                completePendingIntent
            )
            .build()

        notificationManager.notify(NOTIFICATION_ID_TASK_REMINDER + task.uuid.hashCode(), notification)
    }

    /**
     * Shows a notification when the user levels up.
     */
    fun showLevelUpNotification(level: Int) {
        if (!hasNotificationPermission()) {
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_LEVEL_UP)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Level Up!")
            .setContentText("Congratulations! You've reached level $level")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_LEVEL_UP, notification)
    }

    /**
     * Checks if the app has notification permission.
     * For Android 13+, requires POST_NOTIFICATIONS permission.
     */
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Gets a message describing when a task is due.
     */
    private fun getTaskDueMessage(task: Task): String {
        val due = task.due ?: return "This task is due"
        val now = System.currentTimeMillis()
        val diff = due - now

        return when {
            diff < 0 -> "This task is overdue"
            diff < 60 * 60 * 1000 -> "Due in less than an hour"
            diff < 24 * 60 * 60 * 1000 -> {
                val hours = (diff / (60 * 60 * 1000)).toInt()
                "Due in $hours hour${if (hours > 1) "s" else ""}"
            }
            else -> {
                val days = (diff / (24 * 60 * 60 * 1000)).toInt()
                "Due in $days day${if (days > 1) "s" else ""}"
            }
        }
    }
}
