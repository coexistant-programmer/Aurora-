package com.auroracalendar.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Per-widget-instance configuration (widget type, size, user preferences).
 * Supports multiple independent widget instances.
 */
@Entity(tableName = "widget_config")
data class WidgetConfig(
    @PrimaryKey
    val widgetId: Int,

    @ColumnInfo(name = "size") val size: String = "medium", // small, medium, large
    @ColumnInfo(name = "widget_type") val widgetType: String = "daily_planner", // daily_planner, task_timer, monthly_calendar, agenda, quick_add, focus
    @ColumnInfo(name = "config_json") val configJson: String = "{}"
)
