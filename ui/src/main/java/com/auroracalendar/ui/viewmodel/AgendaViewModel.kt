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
 * A single upcoming item for the agenda view, grouped under a date header.
 */
data class AgendaItem(
    val date: LocalDate,
    val type: String, // "event" | "task"
    val id: Int,
    val title: String,
    val timeLabel: String?,
    val categoryId: Int?,
    val color: Int,
    val status: String?,
    val allDay: Boolean
)

/**
 * ViewModel for the Agenda (upcoming list) view. Loads events and tasks for the
 * next 30 days and groups them by date.
 */
@HiltViewModel
class AgendaViewModel @Inject constructor(
    application: Application,
    private val eventRepository: EventRepository,
    private val taskRepository: TaskRepository,
    private val getEventsInRange: GetEventsInRange,
    private val getTasksInRange: GetTasksInRange
) : AndroidViewModel(application) {

    private val _itemsByDate = MutableStateFlow<List<AgendaDay>>(emptyList())
    val itemsByDate: StateFlow<List<AgendaDay>> = _itemsByDate.asStateFlow()

    data class AgendaDay(
        val date: LocalDate,
        val dateLabel: String,
        val items: List<AgendaItem>
    )

    init {
        loadAgenda()
    }

    fun refresh() = loadAgenda()

    private fun loadAgenda() {
        viewModelScope.launch {
            val now = LocalDate.now()
            val startMs = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMs = startMs + 30L * DAY_IN_MILLIS

            val events = getEventsInRange(startMs, endMs).first()
            val tasks = getTasksInRange(startMs, endMs).first()

            val items = buildAgendaItems(events, tasks)
            _itemsByDate.value = items
        }
    }

    private fun buildAgendaItems(
        events: List<CalendarEvent>,
        tasks: List<Task>
    ): List<AgendaDay> {
        val items = mutableListOf<AgendaItem>()

        for (e in events) {
            val d = e.startTime.atZone(ZoneId.systemDefault()).toLocalDate()
            val time = if (e.allDay) null else timeLabel(e.startTime.toEpochMilli())
            items.add(AgendaItem(
                date = d,
                type = "event",
                id = e.id,
                title = e.title,
                timeLabel = time,
                categoryId = e.categoryId,
                color = e.color,
                status = e.status,
                allDay = e.allDay
            ))
        }

        for (t in tasks) {
            if (t.status == "completed") continue
            val d = Instant.ofEpochMilli(t.dateMs).atZone(ZoneId.systemDefault()).toLocalDate()
            val time = if (t.startTimeMs == null) null else timeLabel(t.startTimeMs)
            items.add(AgendaItem(
                date = d,
                type = "task",
                id = t.id,
                title = t.title,
                timeLabel = time,
                categoryId = t.categoryId,
                color = t.color,
                status = t.status,
                allDay = t.startTimeMs == null
            ))
        }

        return groupByDate(items)
    }

    private fun groupByDate(items: List<AgendaItem>): List<AgendaDay> {
        val grouped = items.groupBy { it.date }
        return grouped.entries.sortedBy { it.key }.map { entry ->
            AgendaDay(
                date = entry.key,
                dateLabel = entry.key.format(java.time.format.DateTimeFormatter.ofPattern("EEE, MMM d")),
                items = entry.value.sortedBy { it.timeLabel == null to it.timeLabel }
            )
        }
    }

    private fun timeLabel(ms: Long): String {
        return Instant.ofEpochMilli(ms)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"))
    }
}
