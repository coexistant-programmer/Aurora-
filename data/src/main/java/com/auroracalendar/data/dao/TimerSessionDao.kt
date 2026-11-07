package com.auroracalendar.data.dao

import androidx.room.*
import com.auroracalendar.data.entity.TimerSession
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: TimerSession): Long

    @Update
    suspend fun update(session: TimerSession)

    @Query("SELECT * FROM timer_sessions WHERE task_id = :taskId ORDER BY started_at_ms DESC")
    fun getSessionsForTask(taskId: Int): Flow<List<TimerSession>>

    @Query("""
        SELECT SUM(duration_ms) FROM timer_sessions WHERE task_id = :taskId AND ended_at_ms IS NOT NULL
    """)
    suspend fun getTotalSessionDuration(taskId: Int): Long?

    @Query("""
        SELECT started_at_ms FROM timer_sessions
        WHERE task_id = :taskId AND ended_at_ms IS NULL
        ORDER BY started_at_ms DESC LIMIT 1
    """)
    suspend fun getOpenSessionStart(taskId: Int): Long?
}
