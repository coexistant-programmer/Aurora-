package com.auroracalendar.domain.usecase

import com.auroracalendar.domain.model.Task
import com.auroracalendar.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

/**
 * Task management use cases.
 */
class CreateTask(private val taskRepository: TaskRepository) {
    operator fun invoke(task: Task): Long = taskRepository.insert(task)
}

class UpdateTask(private val taskRepository: TaskRepository) {
    operator fun invoke(task: Task) = taskRepository.update(task)
}

class DeleteTask(private val taskRepository: TaskRepository) {
    operator fun invoke(id: Int) {
        val task = taskRepository.getById(id) ?: return
        taskRepository.delete(task)
    }
}

class GetTasksForDay(private val taskRepository: TaskRepository) {
    operator fun invoke(dayMs: Long): Flow<List<Task>> = taskRepository.getTasksForDay(dayMs)
}

class GetTasksInRange(private val taskRepository: TaskRepository) {
    operator fun invoke(startMs: Long, endMs: Long): Flow<List<Task>> =
        taskRepository.getTasksInRange(startMs, endMs)
}

class RescheduleTask(private val taskRepository: TaskRepository) {
    /**
     * Move a task to a different date/time without losing any of its information.
     */
    operator fun invoke(
        task: Task,
        newDateMs: Long,
        newStartTimeMs: Long?,
        allocatedDurationMs: Long? = null
    ) {
        val updated = task.copy(
            dateMs = newDateMs,
            startTimeMs = newStartTimeMs,
            allocatedDurationMs = allocatedDurationMs ?: task.allocatedDurationMs,
            endTimeMs = if (newStartTimeMs != null && allocatedDurationMs != null)
                newStartTimeMs + allocatedDurationMs else task.endTimeMs,
            updatedAtMs = System.currentTimeMillis()
        )
        taskRepository.update(updated)
    }
}

class CompleteTask(private val taskRepository: TaskRepository) {
    operator fun invoke(task: Task, completedAtMs: Long, updatedAtMs: Long) {
        taskRepository.update(task.copy(
            status = "completed",
            completedAtMs = completedAtMs,
            updatedAtMs = updatedAtMs
        ))
    }
}

class SkipTask(private val taskRepository: TaskRepository) {
    operator fun invoke(task: Task, updatedAtMs: Long) {
        taskRepository.update(task.copy(status = "skipped", updatedAtMs = updatedAtMs))
    }
}

class GetRunningTask(private val taskRepository: TaskRepository) {
    operator fun invoke() = taskRepository.getRunningTask()
}
