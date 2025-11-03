package com.taskhero.core.ui.accessibility

/**
 * Constants for accessibility content descriptions throughout the app.
 * These descriptions are read aloud by screen readers like TalkBack
 * to help users with visual impairments navigate the application.
 */
object AccessibilityConstants {

    /**
     * Task List Screen descriptions
     */
    object TaskList {
        const val SCREEN_TITLE = "Task Hero - Task List"
        const val FAB_ADD_TASK = "Add new task"
        const val TASK_CARD_PREFIX = "Task: "
        const val COMPLETE_TASK_BUTTON = "Mark task as complete"
        const val DELETE_TASK_BUTTON = "Delete task"
        const val EMPTY_LIST = "No tasks found. Tap the add button to create your first task."
        const val FILTER_BUTTON = "Filter tasks"
        const val SORT_BUTTON = "Sort tasks"

        fun taskCard(description: String, status: String, priority: String?): String {
            val priorityText = priority?.let { ", Priority: $it" } ?: ""
            return "Task: $description, Status: $status$priorityText"
        }

        fun completedTask(description: String): String {
            return "Completed task: $description"
        }
    }

    /**
     * Task Detail Screen descriptions
     */
    object TaskDetail {
        const val SCREEN_TITLE = "Task Details"
        const val BACK_BUTTON = "Navigate back"
        const val SAVE_BUTTON = "Save task changes"
        const val DELETE_BUTTON = "Delete this task"
        const val DESCRIPTION_FIELD = "Task description"
        const val STATUS_DROPDOWN = "Task status"
        const val PRIORITY_SELECTOR = "Task priority"
        const val DUE_DATE_FIELD = "Due date"
        const val CLEAR_DUE_DATE = "Clear due date"
        const val PROJECT_FIELD = "Project name"
        const val TAG_INPUT = "Add tag"
        const val ADD_TAG_BUTTON = "Add tag to task"
        const val REMOVE_TAG_PREFIX = "Remove tag: "
        const val DEPENDENCY_DROPDOWN = "Add dependency"
        const val REMOVE_DEPENDENCY_PREFIX = "Remove dependency: "
        const val ANNOTATION_INPUT = "Add annotation"
        const val ADD_ANNOTATION_BUTTON = "Add annotation to task"
        const val DELETE_ANNOTATION_PREFIX = "Delete annotation: "
        const val UDA_SECTION = "User Defined Attributes"
        const val ADD_UDA_BUTTON = "Add custom attribute"
        const val EDIT_UDA_PREFIX = "Edit attribute: "
        const val DELETE_UDA_PREFIX = "Delete attribute: "

        fun priorityButton(priority: String?, isSelected: Boolean): String {
            val selected = if (isSelected) "selected" else "not selected"
            return "Priority ${priority ?: "none"}, $selected"
        }

        fun tagChip(tag: String): String {
            return "Tag: $tag, tap to remove"
        }

        fun annotation(text: String, timestamp: String): String {
            return "Annotation added on $timestamp: $text"
        }
    }

    /**
     * Hero Screen descriptions
     */
    object Hero {
        const val SCREEN_TITLE = "Hero Profile"
        const val AVATAR_IMAGE = "Hero avatar"
        const val LEVEL_BADGE = "Hero level badge"
        const val XP_PROGRESS_BAR = "Experience points progress"
        const val STATS_SECTION = "Hero statistics"

        fun heroLevel(level: Int): String {
            return "Hero Level $level"
        }

        fun xpProgress(current: Int, needed: Int, percentage: Int): String {
            return "Experience: $current out of $needed XP, $percentage percent to next level"
        }

        fun statCard(statName: String, value: String): String {
            return "$statName: $value"
        }

        fun totalTasksCompleted(count: Int): String {
            return "Total tasks completed: $count"
        }

        fun currentStreak(days: Int): String {
            return "Current streak: $days days"
        }

        fun longestStreak(days: Int): String {
            return "Longest streak: $days days"
        }
    }

    /**
     * Reports Screen descriptions
     */
    object Reports {
        const val SCREEN_TITLE = "Reports and Analytics"
        const val TAB_OVERVIEW = "Overview tab"
        const val TAB_CALENDAR = "Calendar tab"
        const val TAB_CHARTS = "Charts tab"
        const val BURNDOWN_CHART = "Task burndown chart"
        const val CALENDAR_VIEW = "Calendar view"
        const val STATISTICS_SECTION = "Task statistics"

        fun tabSelected(tabName: String): String {
            return "$tabName tab selected"
        }

        fun statsCard(title: String, value: String): String {
            return "$title: $value"
        }

        fun calendarDay(day: String, tasksCount: Int): String {
            return "$day, $tasksCount tasks"
        }

        fun chartDataPoint(label: String, value: String): String {
            return "$label: $value"
        }
    }

    /**
     * Settings Screen descriptions
     */
    object Settings {
        const val SCREEN_TITLE = "Settings"
        const val BACK_BUTTON = "Navigate back"
        const val CATEGORY_GENERAL = "General settings"
        const val CATEGORY_APPEARANCE = "Appearance settings"
        const val CATEGORY_NOTIFICATIONS = "Notification settings"
        const val CATEGORY_DATA = "Data settings"
        const val CATEGORY_ABOUT = "About section"
        const val THEME_SETTING = "Theme preference"
        const val SYNC_SETTING = "Sync preferences"
        const val NOTIFICATION_TOGGLE = "Enable notifications"
        const val EXPORT_BUTTON = "Export data"
        const val IMPORT_BUTTON = "Import data"
        const val CLEAR_DATA_BUTTON = "Clear all data"

        fun settingItem(title: String, value: String): String {
            return "$title: $value"
        }

        fun toggleSetting(title: String, isEnabled: Boolean): String {
            val state = if (isEnabled) "enabled" else "disabled"
            return "$title, $state"
        }
    }

    /**
     * Navigation descriptions
     */
    object Navigation {
        const val BOTTOM_NAV_TASKS = "Tasks navigation"
        const val BOTTOM_NAV_HERO = "Hero profile navigation"
        const val BOTTOM_NAV_REPORTS = "Reports navigation"
        const val BOTTOM_NAV_SETTINGS = "Settings navigation"

        fun bottomNavItem(label: String, isSelected: Boolean): String {
            val selected = if (isSelected) "selected" else "not selected"
            return "$label, $selected"
        }
    }

    /**
     * Common UI element descriptions
     */
    object Common {
        const val LOADING = "Loading content"
        const val ERROR_RETRY = "Retry loading"
        const val SEARCH = "Search"
        const val FILTER = "Filter"
        const val SORT = "Sort"
        const val CLOSE = "Close"
        const val CANCEL = "Cancel"
        const val CONFIRM = "Confirm"
        const val MORE_OPTIONS = "More options"
        const val EXPAND = "Expand"
        const val COLLAPSE = "Collapse"

        fun loading(item: String): String {
            return "Loading $item"
        }

        fun error(message: String): String {
            return "Error: $message"
        }
    }
}
