package com.auroracalendar.ui.viewmodel

import android.app.Application
import androidx.hilt.lifecycle.HiltViewModel
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.auroracalendar.domain.model.CalendarEvent
import com.auroracalendar.domain.model.Task
import com.auroracalendar.domain.model.TaskCategory
import com.auroracalendar.domain.repository.EventRepository
import com.auroracalendar.domain.repository.TaskRepository
import com.auroracalendar.domain.usecase.GetAllCategories
import com.auroracalendar.domain.usecase.GetEventsForDay
import com.auroracalendar.domain.usecase.GetEventsInRange
import com.auroracalendar.domain.usecase.GetTasksForDay
import com.auroracalendar.domain.usecase.GetTasksInRange
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*
import javax.inject.Inject

private const val DAY_IN_MILLIS = 24L * 60 * 60 * 1000L

/**
 * A single day cell in the calendar grid. Holds display state plus the
 * category-colored dots for events and tasks falling on that day.
 */
data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    val eventCategoryColors: List<Int>,
    val taskCategoryColors: List<Int>
)

/**
 * ViewModel for the Month calendar view. Generates the month grid, tracks the
 * selected date, and loads events/tasks for the selected day.
 */
@HiltViewModel
class MonthViewModel @Inject constructor(
    application: Application,
    private val eventRepository: EventRepository,
    private val taskRepository: TaskRepository,
    private val getEventsForDay: GetEventsForDay,
    private val getTasksForDay: GetTasksForDay,
    private val getEventsInRange: GetEventsInRange,
    private val getTasksInRange: GetTasksInRange,
    private val getAllCategories: GetAllCategories
) : AndroidViewModel(application) {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _monthDays = MutableStateFlow<List<CalendarDay>>(emptyList())
    val monthDays: StateFlow<List<CalendarDay>> = _monthDays.asStateFlow()

    private val _categories = MutableStateFlow<List<TaskCategory>>(emptyList())
    val categories: StateFlow<List<TaskCategory>> = _categories.asStateFlow()

    private val _selectedDayEvents = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val selectedDayEvents: StateFlow<List<CalendarEvent>> = _selectedDayEvents.asStateFlow()

    private val _selectedDayTasks = MutableStateFlow<List<Task>>(emptyList())
    val selectedDayTasks: StateFlow<List<Task>> = _selectedDayTasks.asStateFlow()

    init {
        loadCategories()
        loadMonth(LocalDate.now())

        // Reload the month grid whenever the selected date moves to a new month.
        viewModelScope.launch { reloadMonthGridForSelectedDate() }

        // Reactively load events/tasks for the selected day. flatMapLatest cancels
        // the previous collection whenever the date changes.
        viewModelScope.launch {
            selectedDate.distinctUntilChanged().flatMapLatest { date ->
                val startMs = startOfDay(date)
                combine(
                    getEventsForDay(startMs),
                    getTasksForDay(startMs)
                ) { events, tasks -> Pair(events, tasks) }
            }.collect { (events, tasks) ->
                _selectedDayEvents.value = events
                _selectedDayTasks.value = tasks
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun previousMonth() {
        _selectedDate.value = _selectedDate.value.minusMonths(1)
    }

    fun nextMonth() {
        _selectedDate.value = _selectedDate.value.plusMonths(1)
    }

    fun jumpToToday() {
        _selectedDate.value = LocalDate.now()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getAllCategories().collect { _categories.value = it }
        }
    }

    private fun loadMonth(date: LocalDate) {
        viewModelScope.launch {
            val firstOfMonth = date.withDayOfMonth(1)
            val lastOfMonth = date.withDayOfMonth(date.lengthOfMonth())

            val startMs = startOfDay(firstOfMonth)
            val endMs = startOfDay(lastOfMonth) + DAY_IN_MILLIS

            val events = getEventsInRange(startMs, endMs).first()
            val tasks = getTasksInRange(startMs, endMs).first()

            val days = generateMonthDays(firstOfMonth, events, tasks)
            _monthDays.value = days
        }
    }

    /**
     * Rebuilds the month grid when the selected date moves to a different month.
     * Called from navigation functions (previousMonth / nextMonth / jumpToToday)
     * after updating _selectedDate.
     */
    private fun reloadMonthGridForSelectedDate() {
        viewModelScope.launch {
            selectedDate.distinctUntilChanged().collect { date ->
                loadMonth(date)
            }
        }
    }

    private fun generateMonthDays(
        firstOfMonth: LocalDate,
        events: List<CalendarEvent>,
        tasks: List<Task>
    ): List<CalendarDay> {
        val today = LocalDate.now()
        val selected = _selectedDate.value

        val firstDayOfWeek = firstOfMonth.dayOfWeek.value // 1=Mon ... 7=Sun
        val offset = firstDayOfWeek - 1 // days to skip from Monday

        val days = mutableListOf<CalendarDay>()

        // Leading days from previous month
        val prevMonthLast = firstOfMonth.minusDays(1)
        for (i in 0 until offset) {
            val d = prevMonthLast.minusDays((offset - 1 - i).toLong())
            days.add(CalendarDay(d, false, d == today, d == selected, emptyList(), emptyList()))
        }

        // Current month days
        val eventColorsByDay = groupCategoryColorsByDay(events)
        val taskColorsByDay = groupCategoryColorsByDay(tasks)

        for (day in 1..firstOfMonth.lengthOfMonth()) {
            val d = firstOfMonth.withDayOfMonth(day)
            days.add(CalendarDay(
                date = d,
                isCurrentMonth = true,
                isToday = d == today,
                isSelected = d == selected,
                eventCategoryColors = eventColorsByDay[d] ?: emptyList(),
                taskCategoryColors = taskColorsByDay[d] ?: emptyList()
            ))
        }

        // Trailing days from next month to fill a complete last row
        val totalCells = ((days.size + 6) / 7.0).ceil().toInt() * 7
        val nextMonthFirst = firstOfMonth.plusMonths(1).withDayOfMonth(1)
        for (i in days.size until totalCells) {
            val d = nextMonthFirst.plusDays((i - days.size).toLong())
            days.add(CalendarDay(d, false, d == today, d == selected, emptyList(), emptyList()))
        }

        return days
    }

    private fun groupCategoryColorsByDay(events: List<CalendarEvent>): Map<LocalDate, List<Int>> {
        val result = mutableMapOf<LocalDate, MutableList<Int>>()
        for (e in events) {
            val d = e.startTime.atZone(ZoneId.systemDefault()).toLocalDate()
            result.getOrPut(d) { mutableListOf() }.add(e.color)
        }
        return result.mapValues { it.value.toList() }
    }

    private fun groupCategoryColorsByDay(tasks: List<Task>): Map<LocalDate, List<Int>> {
        val result = mutableMapOf<LocalDate, MutableList<Int>>()
        for (t in tasks) {
            val d = Instant.ofEpochMilli(t.dateMs).atZone(ZoneId.systemDefault()).toLocalDate()
            result.getOrPut(d) { mutableListOf() }.add(t.color)
        }
        return result.mapValues { it.value.toList() }
    }

    private fun startOfDay(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
