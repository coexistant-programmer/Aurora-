package com.auroracalendar.ui.viewmodel

import android.app.Application
import androidx.hilt.lifecycle.HiltViewModel
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.auroracalendar.domain.model.Task
import com.auroracalendar.domain.repository.TaskRepository
import com.auroracalendar.domain.repository.EventRepository
import com.auroracalendar.domain.usecase.GetRunningTask
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    application: Application,
    private val taskRepository: TaskRepository,
    private val eventRepository: EventRepository,
    private val getRunningTask: GetRunningTask
) : AndroidViewModel(application) {

    private val _todayTasks = MutableStateFlow<List<Task>>(emptyList())
    val todayTasks: StateFlow<List<Task>> = _todayTasks.asStateFlow()

    private val _runningTask = MutableStateFlow<Task?>(null)
    val runningTask: StateFlow<Task?> = _runningTask.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    init {
        loadToday()
        viewModelScope.launch {
            getRunningTask().collect { _runningTask.value = it }
        }
    }

    private fun loadToday() {
        val now = Instant.now()
        val todayStart = now.atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val todayEnd = todayStart + DateUtils.DAY_IN_MILLIS
        viewModelScope.launch {
            taskRepository.getTasksForDay(todayStart).collect { tasks ->
                _todayTasks.value = tasks
                val completed = tasks.count { it.status == "completed" }
                _progress.value = if (tasks.isEmpty()) 0f else completed / tasks.size.toFloat()
            }
        }
    }

    fun refresh() = loadToday()

    companion object {
        private const val DAY_IN_MILLIS = 24L * 60 * 60 * 1000
    }
}

private object DateUtils {
    const val DAY_IN_MILLIS = 24L * 60 * 60 * 1000
}
