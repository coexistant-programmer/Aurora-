package com.auroracalendar.data.repository

import com.auroracalendar.data.dao.TaskDao
import com.auroracalendar.data.mapper.EntityMappers.toDomain
import com.auroracalendar.data.mapper.EntityMappers.toEntity
import com.auroracalendar.domain.model.Task
import com.auroracalendar.domain.repository.TaskRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(private val taskDao: TaskDao) : TaskRepository {

    override suspend fun insert(task: Task): Long = taskDao.insert(task.toEntity())

    override suspend fun insertAll(tasks: List<Task>) =
        taskDao.insertAll(tasks.map { it.toEntity() })

    override suspend fun update(task: Task) = taskDao.update(task.toEntity())

    override suspend fun delete(task: Task) = taskDao.delete(task.toEntity())

    override suspend fun deleteById(id: Int) = taskDao.deleteById(id)

    override suspend fun getById(id: Int): Task? = taskDao.getById(id)?.toDomain()

    override fun getTasksForDay(dayMs: Long): Flow<List<Task>> =
        taskDao.getTasksForDay(dayMs).map { it.map { it.toDomain() } }

    override fun getTasksInRange(startMs: Long, endMs: Long): Flow<List<Task>> =
        taskDao.getTasksInRange(startMs, endMs).map { it.map { it.toDomain() } }

    override fun getTasksByStatus(status: String): Flow<List<Task>> =
        taskDao.getTasksByStatus(status).map { it.map { it.toDomain() } }

    override fun getTasksByCategory(categoryId: Int): Flow<List<Task>> =
        taskDao.getTasksByCategory(categoryId).map { it.map { it.toDomain() } }

    override suspend fun getRunningTask(): Task? = taskDao.getRunningTask()?.toDomain()

    override fun search(query: String): Flow<List<Task>> =
        taskDao.search("%$query%").map { it.map { it.toDomain() } }

    override suspend fun updateTimeTracking(
        id: Int,
        actualDurationMs: Long,
        sessionCount: Int,
        pausedDurationMs: Long,
        overtimeMs: Long,
        completedAtMs: Long?,
        updatedAtMs: Long
    ) {
        taskDao.updateTimeTracking(
            id, actualDurationMs, sessionCount, pausedDurationMs, overtimeMs,
            completedAtMs, updatedAtMs
        )
    }
}
