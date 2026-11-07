package com.auroracalendar.domain.usecase

import com.auroracalendar.domain.model.CalendarEvent
import com.auroracalendar.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow

/**
 * Event management use cases, including recurrence handling.
 *
 * Editing one occurrence never modifies the series: offer "edit this occurrence"
 * (creates/updates a linked child row) vs "edit entire series" (updates master).
 */
class CreateEvent(private val eventRepository: EventRepository) {
    operator fun invoke(event: CalendarEvent): Long = eventRepository.insert(event)
}

class UpdateEvent(private val eventRepository: EventRepository) {
    operator fun invoke(event: CalendarEvent) = eventRepository.update(event)
}

class DeleteEvent(private val eventRepository: EventRepository) {
    operator fun invoke(event: CalendarEvent, deleteWholeSeries: Boolean) {
        if (deleteWholeSeries && event.originalEventId != null) {
            eventRepository.deleteSeries(event.originalEventId!!)
        } else {
            eventRepository.delete(event)
        }
    }
}

class GetEventsForDay(private val eventRepository: EventRepository) {
    operator fun invoke(dayMs: Long): Flow<List<CalendarEvent>> =
        eventRepository.getEventsForDay(dayMs)
}

class GetEventsInRange(private val eventRepository: EventRepository) {
    operator fun invoke(startMs: Long, endMs: Long): Flow<List<CalendarEvent>> =
        eventRepository.getEventsInRange(startMs, endMs)
}

class ExpandRecurrences(private val eventRepository: EventRepository) {
    /**
     * Returns concrete occurrences for a series master within [startMs, endMs].
     * The RRULE expansion logic lives here so the UI stays pure.
     */
    operator fun invoke(master: CalendarEvent, startMs: Long, endMs: Long): Flow<List<CalendarEvent>> =
        eventRepository.getOccurrencesForSeries(master.id)
}
