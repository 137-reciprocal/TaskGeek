package com.taskhero.core.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.taskhero.domain.task.model.TaskStatus
import com.taskhero.domain.task.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * WorkManager worker that checks for upcoming tasks and sends reminder notifications.
 * Runs periodically to notify users about tasks due in the next 24 hours.
 */
@HiltWorker
class TaskReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskRepository: TaskRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "task_reminder_worker"
        const val KEY_TASK_UUID = "task_uuid"
        private const val TWENTY_FOUR_HOURS_MILLIS = 24 * 60 * 60 * 1000L
        private const val ONE_HOUR_MILLIS = 60 * 60 * 1000L
    }

    override suspend fun doWork(): Result {
        return try {
            val taskUuid = inputData.getString(KEY_TASK_UUID)

            if (taskUuid != null) {
                // This is a specific task reminder
                checkSpecificTask(taskUuid)
            } else {
                // This is a daily check for all upcoming tasks
                checkUpcomingTasks()
            }

            Result.success()
        } catch (e: Exception) {
            // Log error and retry
            Result.retry()
        }
    }

    /**
     * Checks a specific task and sends a notification if it's due soon.
     */
    private suspend fun checkSpecificTask(taskUuid: String) {
        val task = taskRepository.getTaskByUuid(taskUuid).first()

        if (task != null && task.status == TaskStatus.PENDING) {
            val due = task.due
            if (due != null) {
                val now = System.currentTimeMillis()
                val timeToDue = due - now

                // Send notification if task is due within an hour or overdue
                if (timeToDue <= ONE_HOUR_MILLIS) {
                    notificationHelper.showTaskDueNotification(task)
                }
            }
        }
    }

    /**
     * Checks all tasks and sends notifications for those due in the next 24 hours.
     */
    private suspend fun checkUpcomingTasks() {
        val allTasks = taskRepository.getAllTasks().first()
        val now = System.currentTimeMillis()
        val oneDayFromNow = now + TWENTY_FOUR_HOURS_MILLIS

        // Filter pending tasks that are due within the next 24 hours
        val upcomingTasks = allTasks.filter { task ->
            task.status == TaskStatus.PENDING &&
            task.due != null &&
            task.due in now..oneDayFromNow
        }

        // Send notifications for upcoming tasks
        upcomingTasks.forEach { task ->
            val timeToDue = (task.due ?: return@forEach) - now

            when {
                // Task is overdue or due within an hour - high priority
                timeToDue <= ONE_HOUR_MILLIS -> {
                    notificationHelper.showTaskDueNotification(task)
                }
                // Task is due within 24 hours - regular reminder
                timeToDue <= TWENTY_FOUR_HOURS_MILLIS -> {
                    notificationHelper.showTaskReminderNotification(task)
                }
            }
        }

        // Schedule next check
        NotificationScheduler.scheduleDailyReminders(applicationContext)
    }
}
