package com.auroracalendar.domain.model

import java.time.Instant

/**
 * Domain model for a calendar event. Free of Android/Room annotations so the
 * domain layer stays pure and testable without the database.
 */
data class CalendarEvent(
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val location: String = "",
    val startTime: Instant,
    val endTime: Instant? = null,
    val allDay: Boolean = false,
    val categoryId: Int? = null,
    val color: Int = 0,
    val reminderMinutes: List<Int> = emptyList(),
    val recurrenceRule: String? = null,
    val recurrenceId: String? = null,
    val originalEventId: Int? = null,
    val status: String = "scheduled",
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
