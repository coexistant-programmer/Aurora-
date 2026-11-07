package com.auroracalendar.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * A calendar event (appointment, meeting, class, etc.). May be part of a recurring series.
 */
@Entity(
    tableName = "calendar_events",
    indices = [
        Index(value = ["start_time_ms"]),
        Index(value = ["original_event_id"]),
        Index(value = ["recurrence_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = TaskCategory::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "location") val location: String = "",

    @ColumnInfo(name = "start_time_ms") val startTimeMs: Long,
    @ColumnInfo(name = "end_time_ms") val endTimeMs: Long? = null,
    @ColumnInfo(name = "all_day") val allDay: Boolean = false,

    @ColumnInfo(name = "category_id") val categoryId: Int? = null,
    @ColumnInfo(name = "color") val color: Int = 0,

    @ColumnInfo(name = "reminder_minutes") val reminderMinutes: List<Int> = emptyList(),
    @ColumnInfo(name = "recurrence_rule") val recurrenceRule: String? = null,
    @ColumnInfo(name = "recurrence_id") val recurrenceId: String? = null, // id of occurrence within series
    @ColumnInfo(name = "original_event_id") val originalEventId: Int? = null, // series master

    @ColumnInfo(name = "status") val status: String = "scheduled", // scheduled, cancelled
    @ColumnInfo(name = "created_at_ms") val createdAtMs: Long = Instant.now().toEpochMilli(),
    @ColumnInfo(name = "updated_at_ms") val updatedAtMs: Long = Instant.now().toEpochMilli()
)
