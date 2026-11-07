package com.auroracalendar.ui.viewmodel

import android.app.Application
import androidx.hilt.lifecycle.HiltViewModel
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.auroracalendar.domain.model.CalendarEvent
import com.auroracalendar.domain.model.Task
import com.auroracalendar.domain.repository.EventRepository
import com.auroracalendar.domain.repository.TaskRepository
import com.auroracalendar.domain.usecase.GetEventsForDay
import com.auroracalendar.domain.usecase.GetTasksForDay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*
import javax.inject.Inject

private const val DAY_IN_MILLIS = 24L * 60 * 60 * 1000L

/**
 * A single item shown in the day schedule timeline: either a calendar event or a
 * task, sorted by start time.
 */
data class DayScheduleItem(
    val type: String, // "event" | "task"
    val id: Int,
    val title: String,
    val subtitle: String?,
    val startTimeMs: Long?,
    val endTimeMs: Long?,
    val categoryId: Int?,
    val color: Int,
    val status: String?,
    val allDay: Boolean
)

/**
 * ViewModel for the Day schedule screen. Loads all events and tasks for a given
 * date and merges them into a single sorted timeline.
 */
@HiltViewModel
class DayScheduleViewModel @Inject constructor(
    application: Application,
    private val eventRepository: EventRepository,
    private val taskRepository: TaskRepository,
    private val getEventsForDay: GetEventsForDay,
    private val getTasksForDay: GetTasksForDay
) : AndroidViewModel(application) {

    private val _dateMs = MutableStateFlow(System.currentTimeMillis())
    val dateMs: StateFlow<Long> = _dateMs.asStateFlow()

    private val _items = MutableStateFlow<List<DayScheduleItem>>(emptyList())
    val items: StateFlow<List<DayScheduleItem>> = _items.asStateFlow()

    private val _dateLabel = MutableStateFlow("")
    val dateLabel: StateFlow<String> = _dateLabel.asStateFlow()

    init {
        viewModelScope.launch {
            dateMs.distinctUntilChanged().flatMapLatest { ms ->
                val start = startOfDay(ms)
                val end = start + DAY_IN_MILLIS
                combine(
                    eventRepository.getEventsInRange(start, end),
                    taskRepository.getTasksInRange(start, end)
                ) { events, tasks -> Pair(events, tasks) }
            }.collect { (events, tasks) ->
                val date = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate()
                _dateLabel.value = date.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM d"))
                _items.value = buildTimeline(events, tasks, date)
            }
        }
    }

    fun setDate(dateMs: Long) {
        _dateMs.value = dateMs
    }

    private fun buildTimeline(
        events: List<CalendarEvent>,
        tasks: List<Task>,
        date: LocalDate
    ): List<DayScheduleItem> {
        val startOfDayMs = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val eventItems = events.map { e ->
            DayScheduleItem(
                type = "event",
                id = e.id,
                title = e.title,
                subtitle = e.description.takeIf { it.isNotEmpty() } ?: e.location.takeIf { it.isNotEmpty() },
                startTimeMs = e.startTime.toEpochMilli(),
                endTimeMs = e.endTime?.toEpochMilli(),
                categoryId = e.categoryId,
                color = e.color,
                status = e.status,
                allDay = e.allDay
            )
        }

        val taskItems = tasks.map { t ->
            DayScheduleItem(
                type = "task",
                id = t.id,
                title = t.title,
                subtitle = t.notes.takeIf { it.isNotEmpty() },
                startTimeMs = t.startTimeMs,
                endTimeMs = t.endTimeMs,
                categoryId = t.categoryId,
                color = t.color,
                status = t.status,
                allDay = t.startTimeMs == null
            )
        }

        return (eventItems + taskItems)
            .sortedWith(compareBy({ it.startTimeMs ?: startOfDayMs }, { -it.color }))
    }

    private fun startOfDay(ms: Long): Long {
        return Instant.ofEpochMilli(ms)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}
