package com.auroracalendar.domain.model

/**
 * Domain model for per-widget-instance configuration.
 */
data class WidgetConfig(
    val widgetId: Int,
    val size: String = "medium", // small, medium, large
    val widgetType: String = "daily_planner", // daily_planner, task_timer, monthly_calendar, agenda, quick_add, focus
    val configJson: String = "{}"
)
