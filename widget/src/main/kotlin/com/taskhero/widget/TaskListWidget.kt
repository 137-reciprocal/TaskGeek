package com.taskhero.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.model.TaskPriority
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Glance widget showing the next 5 tasks by urgency.
 * Auto-refreshes every 15 minutes.
 */
class TaskListWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TaskListWidgetEntryPoint {
        fun widgetRepository(): WidgetRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Get widget repository through Hilt entry point
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            TaskListWidgetEntryPoint::class.java
        )
        val repository = entryPoint.widgetRepository()

        // Fetch tasks
        val tasks = try {
            repository.getNextTasksForWidget(limit = 5)
        } catch (e: Exception) {
            emptyList()
        }

        provideContent {
            GlanceTheme {
                TaskListWidgetContent(tasks = tasks, context = context)
            }
        }
    }
}

@Composable
private fun TaskListWidgetContent(tasks: List<Task>, context: Context) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
            .cornerRadius(16.dp)
            .padding(16.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Next Tasks",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurface
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                // FAB to add task
                Box(
                    modifier = GlanceModifier
                        .size(32.dp)
                        .background(Color(0xFF4CAF50))
                        .cornerRadius(16.dp)
                        .clickable(
                            onClick = actionStartActivity(
                                Intent(context, Class.forName("com.taskhero.MainActivity")).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    putExtra("navigate_to", "add_task")
                                }
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            // Task list
            if (tasks.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks found",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
            } else {
                LazyColumn {
                    items(tasks) { task ->
                        TaskItem(task = task, context = context)
                        Spacer(modifier = GlanceModifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskItem(task: Task, context: Context) {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(Color(0xFF2C2C2E))
            .cornerRadius(8.dp)
            .padding(12.dp)
            .clickable(
                onClick = actionStartActivity(
                    Intent(context, Class.forName("com.taskhero.MainActivity")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("navigate_to", "task_detail")
                        putExtra("task_uuid", task.uuid)
                    }
                )
            )
    ) {
        Column {
            // Description
            Text(
                text = task.description,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onSurface
                ),
                maxLines = 2
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            // Due date and priority row
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Due date
                task.due?.let { dueTimestamp ->
                    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                    val dueDate = dateFormat.format(Date(dueTimestamp))
                    Text(
                        text = "Due: $dueDate",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }

                // Priority badge
                task.priority?.let { priority ->
                    if (task.due != null) {
                        Spacer(modifier = GlanceModifier.width(8.dp))
                    }
                    val priorityColor = when (priority) {
                        TaskPriority.HIGH -> Color(0xFFEF5350)
                        TaskPriority.MEDIUM -> Color(0xFFFFA726)
                        TaskPriority.LOW -> Color(0xFF66BB6A)
                        TaskPriority.NONE -> Color(0xFF9E9E9E)
                    }
                    Box(
                        modifier = GlanceModifier
                            .background(priorityColor)
                            .cornerRadius(4.dp)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = priority.name,
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Urgency score
                Text(
                    text = "%.1f".format(task.urgency),
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                )
            }
        }
    }
}
