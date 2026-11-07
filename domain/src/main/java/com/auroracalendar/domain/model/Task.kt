package com.auroracalendar.domain.model

import java.time.Instant

/**
 * Domain model for a task — the central feature of Aurora Calendar.
 */
data class Task(
    val id: Int = 0,
    val title: String,
    val description: String = "",

    val dateMs: Long, // start of day (local)
    val startTimeMs: Long? = null,
    val allocatedDurationMs: Long = 0,
    val endTimeMs: Long? = null,

    val priority: Int = 1, // 0 low, 1 medium, 2 high
    val categoryId: Int? = null,
    val color: Int = 0,

    val status: String = "not_started", // not_started, running, paused, completed, overdue, skipped
    val deadlineMs: Long? = null,
    val reminderMinutes: List<Int> = emptyList(),
    val subtasks: List<String> = emptyList(),
    val notes: String = "",

    // Time tracking
    val actualDurationMs: Long = 0,
    val sessionCount: Int = 0,
    val pausedDurationMs: Long = 0,
    val overtimeMs: Long = 0,
    val completedAtMs: Long? = null,

    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
