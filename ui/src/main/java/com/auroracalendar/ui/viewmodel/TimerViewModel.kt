package com.auroracalendar.ui.viewmodel

import androidx.hilt.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auroracalendar.domain.model.Task
import com.auroracalendar.domain.model.TimerProgress
import com.auroracalendar.domain.model.TimerState
import com.auroracalendar.domain.repository.TaskRepository
import com.auroracalendar.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val getTimerState: GetTimerState,
    private val computeTimerProgress: ComputeTimerProgress,
    private val startTimer: StartTimer,
    private val pauseTimer: PauseTimer,
    private val resumeTimer: ResumeTimer,
    private val stopTimer: StopTimer,
    private val completeTimer: CompleteTimer,
    private val resetTimer: ResetTimer,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _timerState = MutableStateFlow<TimerState?>(null)
    val timerState: StateFlow<TimerState?> = _timerState.asStateFlow()

    private val _progress = MutableStateFlow(TimerProgress(0, 0, 0f, false))
    val progress: StateFlow<TimerProgress> = _progress.asStateFlow()

    private val _activeTask = MutableStateFlow<Task?>(null)
    val activeTask: StateFlow<Task?> = _activeTask.asStateFlow()

    init {
        viewModelScope.launch {
            _timerState.value = getTimerState()
            refresh()
        }
        // Display ticker: refreshes remaining-time UI once per second while running.
        // The source of truth remains timestamps; this only drives recomposition.
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                refresh()
            }
        }
    }

    private fun refresh() {
        val state = _timerState.value ?: return
        val taskId = state.activeTaskId ?: return
        val task = taskRepository.getById(taskId)
        if (task == null) {
            _activeTask.value = null
            _progress.value = TimerProgress(0, 0, 0f, false)
            return
        }
        _activeTask.value = task
        _progress.value = computeTimerProgress(task, state, System.currentTimeMillis())
    }

    fun start(taskId: Int) =
        viewModelScope.launch { startTimer(taskId, System.currentTimeMillis(), System.currentTimeMillis()) }

    fun pause(taskId: Int) =
        viewModelScope.launch { pauseTimer(taskId, System.currentTimeMillis(), System.currentTimeMillis()) }

    fun resume(taskId: Int) =
        viewModelScope.launch { resumeTimer(taskId, System.currentTimeMillis(), System.currentTimeMillis()) }

    fun stop() = viewModelScope.launch { stopTimer() }

    fun complete(taskId: Int) =
        viewModelScope.launch {
            completeTimer(taskId, System.currentTimeMillis(), System.currentTimeMillis())
            refresh()
        }

    fun reset(taskId: Int) =
        viewModelScope.launch { resetTimer(taskId, System.currentTimeMillis()) }

    fun refreshState() = viewModelScope.launch {
        _timerState.value = getTimerState()
        refresh()
    }
}
