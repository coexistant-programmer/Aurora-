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
 * A single month's summary for the year overview.
 */
data class YearMonth(
    val month: Month,
    val year: Int,
    val monthLabel: String,
    val eventCount: Int,
    val taskCount: Int
)

/**
 * ViewModel for the Year overview. Shows all 12 months with event/task counts.
 */
@HiltViewModel
class YearViewModel @Inject constructor(
    application: Application,
    private val eventRepository: EventRepository,
    private val taskRepository: TaskRepository,
    private val getEventsInRange: GetEventsInRange,
    private val getTasksInRange: GetTasksInRange
) : AndroidViewModel(application) {

    private val _yearMonths = MutableStateFlow<List<YearMonth>>(emptyList())
    val yearMonths: StateFlow<List<YearMonth>> = _yearMonths.asStateFlow()

    private val _currentYear = MutableStateFlow(Year.now().value)
    val currentYear: StateFlow<Int> = _currentYear.asStateFlow()

    init {
        loadYear(Year.now().value)
    }

    fun previousYear() {
        _currentYear.value = _currentYear.value - 1
        loadYear(_currentYear.value)
    }

    fun nextYear() {
        _currentYear.value = _currentYear.value + 1
        loadYear(_currentYear.value)
    }

    private fun loadYear(year: Int) {
        viewModelScope.launch {
            val start = LocalDate.of(year, 1, 1)
            val end = LocalDate.of(year, 12, 31)
            val startMs = start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMs = end.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() + DAY_IN_MILLIS

            val events = getEventsInRange(startMs, endMs).first()
            val tasks = getTasksInRange(startMs, endMs).first()

            val months = (1..12).map { m ->
                val month = Month.of(m)
                val monthEvents = events.count { e ->
                    e.startTime.atZone(ZoneId.systemDefault()).toLocalDate().month == month
                }
                val monthTasks = tasks.count { t ->
                    Instant.ofEpochMilli(t.dateMs).atZone(ZoneId.systemDefault()).toLocalDate().month == month
                }
                YearMonth(
                    month = month,
                    year = year,
                    monthLabel = month.displayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()),
                    eventCount = monthEvents,
                    taskCount = monthTasks
                )
            }
            _yearMonths.value = months
        }
    }
}
