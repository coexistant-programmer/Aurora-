package com.auroracalendar.domain.usecase

import com.auroracalendar.domain.model.Task
import com.auroracalendar.domain.model.TimerState
import com.auroracalendar.domain.repository.TaskRepository
import com.auroracalendar.domain.repository.TimerRepository
import kotlin.math.coerceAtLeast
import kotlin.math.coerceAtMost

/**
 * Timer use cases. All timer math is timestamp-based: elapsed/remaining are
 * recomputed from `now - started - paused`, never from a live UI counter. This is
 * what makes the timer survive process death and device restart.
 */
class GetTimerState(private val timerRepository: TimerRepository) {
    operator fun invoke(): TimerState? = timerRepository.getState()
}

class StartTimer(
    private val timerRepository: TimerRepository,
    private val taskRepository: TaskRepository
) {
    operator fun invoke(taskId: Int, startedAtMs: Long, updatedAtMs: Long) {
        timerRepository.start(taskId, startedAtMs, updatedAtMs)
        // Ensure task status reflects running state.
        val task = taskRepository.getById(taskId) ?: return
        if (task.status != "running") {
            taskRepository.update(task.copy(status = "running", updatedAtMs = updatedAtMs))
        }
    }
}

class PauseTimer(
    private val timerRepository: TimerRepository,
    private val taskRepository: TaskRepository
) {
    operator fun invoke(taskId: Int, pausedAtMs: Long, updatedAtMs: Long) {
        timerRepository.pause(taskId, pausedAtMs, updatedAtMs)
        val task = taskRepository.getById(taskId) ?: return
        taskRepository.update(task.copy(status = "paused", updatedAtMs = updatedAtMs))
    }
}

class ResumeTimer(
    private val timerRepository: TimerRepository,
    private val taskRepository: TaskRepository
) {
    operator fun invoke(taskId: Int, resumedAtMs: Long, updatedAtMs: Long) {
        timerRepository.resume(taskId, resumedAtMs, updatedAtMs)
        val task = taskRepository.getById(taskId) ?: return
        taskRepository.update(task.copy(status = "running", updatedAtMs = updatedAtMs))
    }
}

class StopTimer(private val timerRepository: TimerRepository) {
    operator fun invoke() {
        timerRepository.stop()
    }
}

class CompleteTimer(
    private val timerRepository: TimerRepository,
    private val taskRepository: TaskRepository
) {
    operator fun invoke(taskId: Int, completedAtMs: Long, updatedAtMs: Long) {
        timerRepository.stop()
        val task = taskRepository.getById(taskId) ?: return
        taskRepository.update(task.copy(status = "completed", completedAtMs = completedAtMs, updatedAtMs = updatedAtMs))
    }
}

class ResetTimer(
    private val timerRepository: TimerRepository,
    private val taskRepository: TaskRepository
) {
    operator fun invoke(taskId: Int, updatedAtMs: Long) {
        timerRepository.stop()
        val task = taskRepository.getById(taskId) ?: return
        taskRepository.update(task.copy(
            status = "not_started",
            actualDurationMs = 0,
            sessionCount = 0,
            pausedDurationMs = 0,
            overtimeMs = 0,
            updatedAtMs = updatedAtMs
        ))
    }
}

/**
 * Computes timer progress from timestamps at render time.
 *
 * Returns: (elapsedMs, remainingMs, progress, isExpired)
 */
data class TimerProgress(
    val elapsedMs: Long,
    val remainingMs: Long,
    val progress: Float, // 0.0f to 1.0f (may exceed 1.0f when overtime)
    val isExpired: Boolean
)

class ComputeTimerProgress {
    operator fun invoke(
        task: Task,
        timerState: TimerState?,
        nowMs: Long
    ): TimerProgress {
        val allocated = task.allocatedDurationMs.coerceAtLeast(0)
        val started = timerState?.startedAtMs ?: return TimerProgress(0, allocated, 0.0f, false)
        val pausedAccumulated = timerState.pausedAccumulatedMs
        val currentlyPaused = !timerState.isRunning && timerState.pausedAtMs != null
        val pauseElapsed = if (currentlyPaused) nowMs - (timerState.pausedAtMs ?: nowMs) else 0L

        val elapsed = (nowMs - started) - pausedAccumulated - pauseElapsed
        val effectiveElapsed = elapsed.coerceAtLeast(0)
        val remaining = allocated - effectiveElapsed
        val progress = if (allocated == 0) 0.0f else (effectiveElapsed.toFloat() / allocated).coerceAtMost(2.0f)
        val isExpired = effectiveElapsed >= allocated && allocated > 0

        return TimerProgress(effectiveElapsed, remaining, progress, isExpired)
    }
}
