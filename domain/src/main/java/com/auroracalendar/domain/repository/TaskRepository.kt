package com.auroracalendar.domain.repository

import com.auroracalendar.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun insert(task: Task): Long
    suspend fun insertAll(tasks: List<Task>)
    suspend fun update(task: Task)
    suspend fun delete(task: Task)
    suspend fun deleteById(id: Int)
    suspend fun getById(id: Int): Task?

    fun getTasksForDay(dayMs: Long): Flow<List<Task>>
    fun getTasksInRange(startMs: Long, endMs: Long): Flow<List<Task>>
    fun getTasksByStatus(status: String): Flow<List<Task>>
    fun getTasksByCategory(categoryId: Int): Flow<List<Task>>
    suspend fun getRunningTask(): Task?
    fun search(query: String): Flow<List<Task>>

    suspend fun updateTimeTracking(
        id: Int,
        actualDurationMs: Long,
        sessionCount: Int,
        pausedDurationMs: Long,
        overtimeMs: Long,
        completedAtMs: Long?,
        updatedAtMs: Long
    )
}
