package com.auroracalendar.domain.model

import java.time.Instant

/**
 * Domain model for the singleton timer state. Reconstructed from timestamps at
 * render time; no live UI counter dependency.
 */
data class TimerState(
    val id: Int = 1,
    val activeTaskId: Int? = null,
    val startedAtMs: Long? = null,
    val isRunning: Boolean = false,
    val pausedAtMs: Long? = null,
    val pausedAccumulatedMs: Long = 0,
    val updatedAt: Instant = Instant.now()
)
