package com.taskhero.feature.reports

import com.taskhero.domain.report.model.DateRange

/**
 * User intents for Reports screen following MVI pattern.
 * Represents all possible user actions on the reports screen.
 */
sealed interface ReportsIntent {
    /**
     * Load or reload reports data.
     */
    data object LoadReports : ReportsIntent

    /**
     * Change the date range for reports.
     *
     * @property dateRange The new date range to apply
     */
    data class ChangeDateRange(val dateRange: DateRange) : ReportsIntent
}
