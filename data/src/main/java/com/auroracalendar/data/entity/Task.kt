package com.auroracalendar.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * A task — the central feature of Aurora Calendar. Every scheduled task supports
 * allocated time and a built-in execution timer.
 */
@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["date_ms"]),
        Index(value = ["status"]),
        Index(value = ["category_id"])
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
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String = "",

    @ColumnInfo(name = "date_ms") val dateMs: Long, // start of day (local)
    @ColumnInfo(name = "start_time_ms") val startTimeMs: Long? = null,
    @ColumnInfo(name = "allocated_duration_ms") val allocatedDurationMs: Long = 0,
    @ColumnInfo(name = "end_time_ms") val endTimeMs: Long? = null, // derived

    @ColumnInfo(name = "priority") val priority: Int = 1, // 0 low, 1 medium, 2 high
    @ColumnInfo(name = "category_id") val categoryId: Int? = null,
    @ColumnInfo(name = "color") val color: Int = 0,

    @ColumnInfo(name = "status") val status: String = "not_started", // not_started, running, paused, completed, overdue, skipped
    @ColumnInfo(name = "deadline_ms") val deadlineMs: Long? = null,
    @ColumnInfo(name = "reminder_minutes") val reminderMinutes: List<Int> = emptyList(),
    @ColumnInfo(name = "subtasks") val subtasks: List<String> = emptyList(),
    @ColumnInfo(name = "notes") val notes: String = "",

    // Time tracking
    @ColumnInfo(name = "actual_duration_ms") val actualDurationMs: Long = 0,
    @ColumnInfo(name = "session_count") val sessionCount: Int = 0,
    @ColumnInfo(name = "paused_duration_ms") val pausedDurationMs: Long = 0,
    @ColumnInfo(name = "overtime_ms") val overtimeMs: Long = 0,
    @ColumnInfo(name = "completed_at_ms") val completedAtMs: Long? = null,

    @ColumnInfo(name = "created_at_ms") val createdAtMs: Long = Instant.now().toEpochMilli(),
    @ColumnInfo(name = "updated_at_ms") val updatedAtMs: Long = Instant.now().toEpochMilli()
)
