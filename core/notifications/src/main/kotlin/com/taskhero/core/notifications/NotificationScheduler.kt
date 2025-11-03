package com.taskhero.core.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

/**
 * Scheduler for notification-related WorkManager tasks.
 * Provides methods to schedule daily reminders and specific task reminders.
 */
object NotificationScheduler {

    private const val DAILY_REMINDER_WORK_NAME = "daily_task_reminder"
    private const val SPECIFIC_TASK_REMINDER_PREFIX = "task_reminder_"

    /**
     * Schedules a periodic worker to check for upcoming tasks daily.
     * The worker runs once every 24 hours.
     *
     * @param context Application context
     */
    fun scheduleDailyReminders(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val dailyWorkRequest = PeriodicWorkRequestBuilder<TaskReminderWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
            flexTimeInterval = 1,
            flexTimeIntervalUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }

    /**
     * Schedules a one-time reminder for a specific task at its due date.
     * The notification will be sent 1 hour before the task is due.
     *
     * @param context Application context
     * @param taskUuid UUID of the task to remind about
     * @param dueDate Due date of the task in milliseconds
     */
    fun scheduleTaskReminder(context: Context, taskUuid: String, dueDate: Long) {
        val now = System.currentTimeMillis()
        val oneHourBeforeDue = dueDate - (60 * 60 * 1000)

        // Calculate delay for notification (1 hour before due date)
        val delay = (oneHourBeforeDue - now).coerceAtLeast(0)

        val inputData = workDataOf(
            TaskReminderWorker.KEY_TASK_UUID to taskUuid
        )

        val constraints = Constraints.Builder()
            .setNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val taskReminderRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "$SPECIFIC_TASK_REMINDER_PREFIX$taskUuid",
            ExistingWorkPolicy.REPLACE,
            taskReminderRequest
        )
    }

    /**
     * Cancels a scheduled reminder for a specific task.
     *
     * @param context Application context
     * @param taskUuid UUID of the task
     */
    fun cancelTaskReminder(context: Context, taskUuid: String) {
        WorkManager.getInstance(context).cancelUniqueWork(
            "$SPECIFIC_TASK_REMINDER_PREFIX$taskUuid"
        )
    }

    /**
     * Cancels all scheduled reminders.
     *
     * @param context Application context
     */
    fun cancelAllReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_REMINDER_WORK_NAME)
    }
}
