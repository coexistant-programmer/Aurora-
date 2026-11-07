package com.auroracalendar.domain.repository

import com.auroracalendar.domain.model.CalendarEvent
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    suspend fun insert(event: CalendarEvent): Long
    suspend fun insertAll(events: List<CalendarEvent>)
    suspend fun update(event: CalendarEvent)
    suspend fun delete(event: CalendarEvent)
    suspend fun deleteById(id: Int)
    suspend fun deleteSeries(masterId: Int)
    suspend fun getById(id: Int): CalendarEvent?

    fun getEventsInRange(startMs: Long, endMs: Long): Flow<List<CalendarEvent>>
    fun getEventsForDay(dayMs: Long): Flow<List<CalendarEvent>>
    fun getSeriesMasters(): Flow<List<CalendarEvent>>
    fun getOccurrencesForSeries(masterId: Int): Flow<List<CalendarEvent>>
    fun search(query: String): Flow<List<CalendarEvent>>
}
