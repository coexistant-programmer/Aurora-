package com.auroracalendar.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Singleton row (id = 1) holding the currently-active timer state. Stored so the
 * timer survives app close, process death, and device restart. Reconstructed at
 * render time from timestamps (no live UI counter dependency).
 */
@Entity(tableName = "timer_state")
data class TimerState(
    @PrimaryKey
    val id: Int = 1, // singleton

    @ColumnInfo(name = "active_task_id") val activeTaskId: Int? = null,
    @ColumnInfo(name = "started_at_ms") val startedAtMs: Long? = null,
    @ColumnInfo(name = "is_running") val isRunning: Boolean = false,
    @ColumnInfo(name = "paused_at_ms") val pausedAtMs: Long? = null, // null when not paused
    @ColumnInfo(name = "paused_accumulated_ms") val pausedAccumulatedMs: Long = 0,
    @ColumnInfo(name = "updated_at_ms") val updatedAtMs: Long = Instant.now().toEpochMilli()
)
