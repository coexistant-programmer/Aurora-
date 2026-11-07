package com.auroracalendar.ui.viewmodel

import android.app.Application
import androidx.hilt.lifecycle.HiltViewModel
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.auroracalendar.domain.model.CalendarEvent
import com.auroracalendar.domain.model.Task
import com.auroracalendar.domain.repository.EventRepository
import com.auroracalendar.domain.repository.TaskRepository
import com.auroracalendar.domain.usecase.GetEventsInRange
import com.auroracalendar.domain.usecase.GetTasksInRange
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*
import javax.inject.Inject

private const val DAY_IN_MILLIS = 24L * 60 * 60 * 1000L

/**
 * A day within the week view, with its events and tasks.
 */
data class WeekDay(
    val date: LocalDate,
    val isToday: Boolean,
    val isSelected: Boolean,
    val events: List<CalendarEvent>,
    val tasks: List<Task>
)

/**
 * ViewModel for the Week calendar view. Generates a 7-day week grid and loads
 * events/tasks for the week.
 */
@HiltViewModel
class WeekViewModel @Inject constructor(
    application: Application,
    private val eventRepository: EventRepository,
    private val taskRepository: TaskRepository,
    private val getEventsInRange: GetEventsInRange,
    private val getTasksInRange: GetTasksInRange
) : AndroidViewModel(application) {

    private val _weekStart = MutableStateFlow(LocalDate.now().with(java.time.DayOfWeek.MONDAY))
    val weekStart: StateFlow<LocalDate> = _weekStart.asStateFlow()

    private val _weekDays = MutableStateFlow<List<WeekDay>>(emptyList())
    val weekDays: StateFlow<List<WeekDay>> = _weekDays.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    init {
        loadWeek(LocalDate.now().with(java.time.DayOfWeek.MONDAY))

        viewModelScope.launch {
            weekStart.distinctUntilChanged().flatMapLatest { start ->
                val startMs = start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endMs = startMs + 6L * DAY_IN_MILLIS + DAY_IN_MILLIS
                combine(
                    getEventsInRange(startMs, endMs),
                    getTasksInRange(startMs, endMs)
                ) { events, tasks -> Pair(events, tasks) }
            }.collect { (events, tasks) ->
                val days = buildWeekDays(_weekStart.value, events, tasks)
                _weekDays.value = days
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun previousWeek() {
        _weekStart.value = _weekStart.value.minusWeeks(1)
    }

    fun nextWeek() {
        _weekStart.value = _weekStart.value.plusWeeks(1)
    }

    fun jumpToToday() {
        val today = LocalDate.now()
        _selectedDate.value = today
        _weekStart.value = today.with(java.time.DayOfWeek.MONDAY)
    }

    private fun loadWeek(start: LocalDate) {
        _weekStart.value = start
    }

    private fun buildWeekDays(
        start: LocalDate,
        events: List<CalendarEvent>,
        tasks: List<Task>
    ): List<WeekDay> {
        val today = LocalDate.now()
        val selected = _selectedDate.value

        val eventByDay = groupEventsByDay(events)
        val taskByDay = groupTasksByDay(tasks)

        return (0 until 7).map { offset ->
            val d = start.plusDays(offset.toLong())
            WeekDay(
                date = d,
                isToday = d == today,
                isSelected = d == selected,
                events = eventByDay[d] ?: emptyList(),
                tasks = taskByDay[d] ?: emptyList()
            )
        }
    }

    private fun groupEventsByDay(events: List<CalendarEvent>): Map<LocalDate, List<CalendarEvent>> {
        return events.groupBy { e ->
            e.startTime.atZone(ZoneId.systemDefault()).toLocalDate()
        }
    }

    private fun groupTasksByDay(tasks: List<Task>): Map<LocalDate, List<Task>> {
        return tasks.groupBy { t ->
            Instant.ofEpochMilli(t.dateMs).atZone(ZoneId.systemDefault()).toLocalDate()
        }
    }
}
