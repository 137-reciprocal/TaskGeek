package com.taskhero.core.common.performance

import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Performance monitoring utility for tracking app performance metrics
 *
 * This class provides methods to track various performance metrics including:
 * - Application startup time
 * - Database query execution times
 * - UI frame drops and rendering performance
 * - Custom operation timing
 *
 * Metrics are only logged in debug builds to avoid performance overhead in production.
 */
@Singleton
class PerformanceMonitor @Inject constructor() {

    companion object {
        private const val TAG = "PerformanceMonitor"
        private const val STARTUP_MARKER = "app_startup"
        private const val DB_QUERY_PREFIX = "db_query_"
        private const val UI_RENDER_PREFIX = "ui_render_"

        // Performance thresholds (in milliseconds)
        private const val SLOW_STARTUP_THRESHOLD = 2000L
        private const val SLOW_DB_QUERY_THRESHOLD = 100L
        private const val SLOW_UI_RENDER_THRESHOLD = 16L // 60 FPS = 16ms per frame

        // Enable/disable based on build type (set via BuildConfig in production)
        private var isDebugMode = true // Set to BuildConfig.DEBUG in production
    }

    // Store start times for ongoing measurements
    private val startTimes = ConcurrentHashMap<String, Long>()

    // Store completed measurements for analysis
    private val measurements = ConcurrentHashMap<String, MutableList<Long>>()

    /**
     * Track application startup time
     * Call this at the beginning of Application.onCreate()
     */
    fun trackStartup() {
        startMeasurement(STARTUP_MARKER)
    }

    /**
     * End startup tracking and log the result
     * Call this when the app is fully initialized and first screen is rendered
     */
    fun endStartup() {
        val duration = endMeasurement(STARTUP_MARKER)
        if (duration != null) {
            logMetric(
                metric = "App Startup",
                duration = duration,
                threshold = SLOW_STARTUP_THRESHOLD
            )
        }
    }

    /**
     * Track database query execution time
     *
     * @param queryName Name/identifier of the query being executed
     * @param block The database operation to track
     * @return The result of the database operation
     */
    suspend fun <T> trackDatabaseQuery(queryName: String, block: suspend () -> T): T {
        val marker = "$DB_QUERY_PREFIX$queryName"
        startMeasurement(marker)

        return try {
            block()
        } finally {
            val duration = endMeasurement(marker)
            if (duration != null) {
                logMetric(
                    metric = "DB Query: $queryName",
                    duration = duration,
                    threshold = SLOW_DB_QUERY_THRESHOLD
                )
                recordMeasurement(marker, duration)
            }
        }
    }

    /**
     * Track UI rendering/composable recomposition time
     *
     * @param componentName Name of the UI component being rendered
     * @param block The UI operation to track
     * @return The result of the UI operation
     */
    inline fun <T> trackUIRender(componentName: String, block: () -> T): T {
        if (!isDebugMode) return block()

        val marker = "$UI_RENDER_PREFIX$componentName"
        startMeasurement(marker)

        return try {
            block()
        } finally {
            val duration = endMeasurement(marker)
            if (duration != null) {
                logMetric(
                    metric = "UI Render: $componentName",
                    duration = duration,
                    threshold = SLOW_UI_RENDER_THRESHOLD
                )
                recordMeasurement(marker, duration)
            }
        }
    }

    /**
     * Track a custom operation
     *
     * @param operationName Name of the operation being tracked
     * @param block The operation to track
     * @return The result of the operation
     */
    suspend fun <T> trackOperation(operationName: String, block: suspend () -> T): T {
        if (!isDebugMode) return block()

        startMeasurement(operationName)

        return try {
            block()
        } finally {
            val duration = endMeasurement(operationName)
            if (duration != null) {
                logMetric(
                    metric = "Operation: $operationName",
                    duration = duration,
                    threshold = 500L // Default threshold for custom operations
                )
                recordMeasurement(operationName, duration)
            }
        }
    }

    /**
     * Start measuring a timed operation
     *
     * @param marker Unique identifier for the measurement
     */
    fun startMeasurement(marker: String) {
        if (!isDebugMode) return
        startTimes[marker] = System.currentTimeMillis()
    }

    /**
     * End a measurement and return the elapsed time
     *
     * @param marker Unique identifier for the measurement
     * @return Elapsed time in milliseconds, or null if no start time was found
     */
    fun endMeasurement(marker: String): Long? {
        if (!isDebugMode) return null

        val startTime = startTimes.remove(marker) ?: return null
        return System.currentTimeMillis() - startTime
    }

    /**
     * Record a measurement for later analysis
     */
    private fun recordMeasurement(marker: String, duration: Long) {
        if (!isDebugMode) return

        measurements.getOrPut(marker) { mutableListOf() }.add(duration)
    }

    /**
     * Log a performance metric with appropriate warning level
     */
    private fun logMetric(metric: String, duration: Long, threshold: Long) {
        if (!isDebugMode) return

        val message = "$metric took ${duration}ms"

        if (duration > threshold) {
            Log.w(TAG, "SLOW: $message (threshold: ${threshold}ms)")
        } else {
            Log.d(TAG, message)
        }
    }

    /**
     * Get performance statistics for a specific metric
     *
     * @param marker The metric identifier
     * @return Performance statistics or null if no data available
     */
    fun getStatistics(marker: String): PerformanceStats? {
        if (!isDebugMode) return null

        val durations = measurements[marker] ?: return null
        if (durations.isEmpty()) return null

        return PerformanceStats(
            marker = marker,
            count = durations.size,
            min = durations.minOrNull() ?: 0L,
            max = durations.maxOrNull() ?: 0L,
            average = durations.average().toLong(),
            total = durations.sum()
        )
    }

    /**
     * Get all performance statistics
     */
    fun getAllStatistics(): Map<String, PerformanceStats> {
        if (!isDebugMode) return emptyMap()

        return measurements.keys.mapNotNull { marker ->
            getStatistics(marker)?.let { marker to it }
        }.toMap()
    }

    /**
     * Log all collected performance statistics
     */
    fun logAllStatistics() {
        if (!isDebugMode) return

        Log.i(TAG, "=== Performance Statistics ===")
        getAllStatistics().forEach { (marker, stats) ->
            Log.i(TAG, """
                $marker:
                  Count: ${stats.count}
                  Min: ${stats.min}ms
                  Max: ${stats.max}ms
                  Avg: ${stats.average}ms
                  Total: ${stats.total}ms
            """.trimIndent())
        }
        Log.i(TAG, "=============================")
    }

    /**
     * Clear all collected performance data
     */
    fun clearStatistics() {
        if (!isDebugMode) return

        startTimes.clear()
        measurements.clear()
        Log.d(TAG, "Performance statistics cleared")
    }

    /**
     * Track frame drops in UI rendering
     *
     * @param frameTimeMillis Time taken to render the frame
     */
    fun trackFrameTime(frameTimeMillis: Long) {
        if (!isDebugMode) return

        if (frameTimeMillis > SLOW_UI_RENDER_THRESHOLD) {
            val droppedFrames = (frameTimeMillis / SLOW_UI_RENDER_THRESHOLD).toInt()
            Log.w(TAG, "Dropped ~$droppedFrames frames (${frameTimeMillis}ms)")
        }

        recordMeasurement("frame_time", frameTimeMillis)
    }

    /**
     * Enable or disable debug mode
     */
    fun setDebugMode(enabled: Boolean) {
        isDebugMode = enabled
    }

    /**
     * Check if debug mode is enabled
     */
    fun isDebugEnabled(): Boolean = isDebugMode
}

/**
 * Data class containing performance statistics for a metric
 */
data class PerformanceStats(
    val marker: String,
    val count: Int,
    val min: Long,
    val max: Long,
    val average: Long,
    val total: Long
)
