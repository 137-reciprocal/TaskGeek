package com.taskhero.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.taskhero.domain.task.model.TaskStatus
import com.taskhero.domain.task.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver for handling notification actions.
 * Handles "Complete Task" and "Snooze" actions from notifications.
 */
@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var taskRepository: TaskRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val SNOOZE_DURATION_MILLIS = 60 * 60 * 1000L // 1 hour
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskUuid = intent.getStringExtra(NotificationHelper.EXTRA_TASK_UUID) ?: return

        when (intent.action) {
            NotificationHelper.ACTION_COMPLETE_TASK -> {
                handleCompleteTask(context, taskUuid)
            }
            NotificationHelper.ACTION_SNOOZE -> {
                handleSnooze(context, taskUuid)
            }
        }
    }

    /**
     * Handles the "Complete Task" action from a notification.
     * Marks the task as completed and dismisses the notification.
     */
    private fun handleCompleteTask(context: Context, taskUuid: String) {
        coroutineScope.launch {
            try {
                // Get the task
                val task = taskRepository.getTaskByUuid(taskUuid).first()

                if (task != null && task.status == TaskStatus.PENDING) {
                    // Update task status to completed
                    val completedTask = task.copy(
                        status = TaskStatus.COMPLETED,
                        end = System.currentTimeMillis()
                    )
                    taskRepository.updateTask(completedTask)

                    // Dismiss the notification
                    dismissNotification(context, taskUuid)

                    // Cancel any scheduled reminders for this task
                    NotificationScheduler.cancelTaskReminder(context, taskUuid)
                }
            } catch (e: Exception) {
                // Handle error - could log or show a toast
                e.printStackTrace()
            }
        }
    }

    /**
     * Handles the "Snooze" action from a notification.
     * Dismisses the current notification and schedules a new one after the snooze duration.
     */
    private fun handleSnooze(context: Context, taskUuid: String) {
        coroutineScope.launch {
            try {
                // Get the task
                val task = taskRepository.getTaskByUuid(taskUuid).first()

                if (task != null) {
                    // Dismiss the current notification
                    dismissNotification(context, taskUuid)

                    // Schedule a new reminder after snooze duration
                    val snoozeUntil = System.currentTimeMillis() + SNOOZE_DURATION_MILLIS
                    NotificationScheduler.scheduleTaskReminder(context, taskUuid, snoozeUntil)
                }
            } catch (e: Exception) {
                // Handle error - could log or show a toast
                e.printStackTrace()
            }
        }
    }

    /**
     * Dismisses a notification for a specific task.
     */
    private fun dismissNotification(context: Context, taskUuid: String) {
        val notificationManager = NotificationManagerCompat.from(context)
        // Dismiss both due and reminder notifications for this task
        notificationManager.cancel(1001 + taskUuid.hashCode()) // NOTIFICATION_ID_TASK_DUE
        notificationManager.cancel(1002 + taskUuid.hashCode()) // NOTIFICATION_ID_TASK_REMINDER
    }
}
