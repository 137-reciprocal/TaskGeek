package com.taskhero.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Receiver for HeroStatsWidget.
 * Handles widget updates and broadcasts.
 */
class HeroStatsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HeroStatsWidget()
}
