package com.auroracalendar.data.dao

import androidx.room.*
import com.auroracalendar.data.entity.CalendarEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: CalendarEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<CalendarEvent>)

    @Update
    suspend fun update(event: CalendarEvent)

    @Delete
    suspend fun delete(event: CalendarEvent)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM calendar_events WHERE original_event_id = :masterId")
    suspend fun deleteSeries(masterId: Int)

    @Query("SELECT * FROM calendar_events WHERE id = :id")
    suspend fun getById(id: Int): CalendarEvent?

    @Query("SELECT * FROM calendar_events WHERE start_time_ms >= :start AND start_time_ms < :end ORDER BY start_time_ms ASC")
    fun getEventsInRange(start: Long, end: Long): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE date(start_time_ms, 'unixepoch') = date(:dayMs, 'unixepoch') ORDER BY start_time_ms ASC")
    fun getEventsForDay(dayMs: Long): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE original_event_id IS NULL ORDER BY start_time_ms ASC")
    fun getSeriesMasters(): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE original_event_id = :masterId ORDER BY start_time_ms ASC")
    fun getOccurrencesForSeries(masterId: Int): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE title LIKE :query OR description LIKE :query OR location LIKE :query ORDER BY start_time_ms ASC")
    fun search(query: String): Flow<List<CalendarEvent>>
}
