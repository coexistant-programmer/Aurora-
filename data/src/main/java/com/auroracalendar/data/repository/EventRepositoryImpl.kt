package com.auroracalendar.data.repository

import android.app.Application
import com.auroracalendar.data.dao.EventDao
import com.auroracalendar.data.mapper.EntityMappers.toDomain
import com.auroracalendar.data.mapper.EntityMappers.toEntity
import com.auroracalendar.domain.model.CalendarEvent
import com.auroracalendar.domain.repository.EventRepository
import com.google.dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    @ApplicationContext private val context: Application
) : EventRepository {

    override suspend fun insert(event: CalendarEvent): Long = eventDao.insert(event.toEntity())

    override suspend fun insertAll(events: List<CalendarEvent>) =
        eventDao.insertAll(events.map { it.toEntity() })

    override suspend fun update(event: CalendarEvent) = eventDao.update(event.toEntity())

    override suspend fun delete(event: CalendarEvent) = eventDao.delete(event.toEntity())

    override suspend fun deleteById(id: Int) = eventDao.deleteById(id)

    override suspend fun deleteSeries(masterId: Int) = eventDao.deleteSeries(masterId)

    override suspend fun getById(id: Int): CalendarEvent? =
        eventDao.getById(id)?.toDomain()

    override fun getEventsInRange(startMs: Long, endMs: Long): Flow<List<CalendarEvent>> =
        eventDao.getEventsInRange(startMs, endMs).map { it.map { it.toDomain() } }

    override fun getEventsForDay(dayMs: Long): Flow<List<CalendarEvent>> =
        eventDao.getEventsForDay(dayMs).map { it.map { it.toDomain() } }

    override fun getSeriesMasters(): Flow<List<CalendarEvent>> =
        eventDao.getSeriesMasters().map { it.map { it.toDomain() } }

    override fun getOccurrencesForSeries(masterId: Int): Flow<List<CalendarEvent>> =
        eventDao.getOccurrencesForSeries(masterId).map { it.map { it.toDomain() } }

    override fun search(query: String): Flow<List<CalendarEvent>> =
        eventDao.search("%$query%").map { it.map { it.toDomain() } }
}
