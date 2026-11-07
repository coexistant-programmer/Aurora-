package com.auroracalendar.domain.repository

import com.auroracalendar.domain.model.TimerState

interface TimerRepository {
    suspend fun getState(): TimerState?
    suspend fun insert(state: TimerState)
    suspend fun update(state: TimerState)
    suspend fun clear()
    suspend fun start(taskId: Int, startedAtMs: Long, updatedAtMs: Long)
    suspend fun pause(taskId: Int, pausedAtMs: Long, updatedAtMs: Long)
    suspend fun resume(taskId: Int, resumedAtMs: Long, updatedAtMs: Long)
    suspend fun stop()
}
