package com.taskhero.core.common.model

/**
 * Configuration for urgency calculation coefficients.
 * Contains all 14 coefficients used in the urgency algorithm.
 */
data class UrgencyConfig(
    // Priority coefficients
    val priorityHigh: Double = 6.0,
    val priorityMedium: Double = 3.9,
    val priorityLow: Double = 1.8,

    // Tag coefficients
    val tagNext: Double = 15.0,

    // Status coefficients
    val activeCoefficient: Double = 4.0,
    val scheduledCoefficient: Double = 5.0,

    // Due date coefficients (based on proximity)
    val dueImminent: Double = 12.0,      // < 24 hours
    val dueVerySoon: Double = 9.0,       // < 3 days
    val dueSoon: Double = 6.0,           // < 7 days
    val dueNear: Double = 3.0,           // < 14 days
    val dueFar: Double = 1.5,            // < 30 days
    val dueDistant: Double = 0.2,        // > 30 days

    // Dependency coefficients
    val blockingCoefficient: Double = 8.0,
    val blockedCoefficient: Double = -5.0,

    // Age coefficient (per day)
    val ageCoefficient: Double = 0.1,
    val maxAgeBonus: Double = 2.0
) {
    companion object {
        /**
         * Default urgency configuration matching Taskwarrior's defaults.
         */
        val DEFAULT = UrgencyConfig()
    }
}
