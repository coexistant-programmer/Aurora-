package com.auroracalendar.data.dao

import androidx.room.*
import com.auroracalendar.data.entity.TimerState

@Dao
interface TimerStateDao {

    // Singleton row (id = 1) representing the currently active timer, if any.
    @Query("SELECT * FROM timer_state WHERE id = 1")
    suspend fun get(): TimerState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(state: TimerState)

    @Update
    suspend fun update(state: TimerState)

    @Query("DELETE FROM timer_state WHERE id = 1")
    suspend fun clear()

    /**
     * Atomically start a timer: insert/replace the singleton row with running=true.
     */
    @Query("""
        INSERT OR REPLACE INTO timer_state (id, active_task_id, started_at_ms, is_running, paused_at_ms, paused_accumulated_ms, updated_at_ms)
        VALUES (1, :taskId, :startedAtMs, 1, NULL, 0, :updatedAtMs)
    """)
    suspend fun start(taskId: Int, startedAtMs: Long, updatedAtMs: Long)

    /**
     * Pause the running timer: record paused_at_ms.
     */
    @Query("""
        UPDATE timer_state SET
          is_running = 0,
          paused_at_ms = :pausedAtMs,
          updated_at_ms = :updatedAtMs
        WHERE id = 1 AND active_task_id = :taskId
    """)
    suspend fun pause(taskId: Int, pausedAtMs: Long, updatedAtMs: Long)

    /**
     * Resume: add (now - paused_at_ms) to paused_accumulated_ms, clear paused_at_ms, set running.
     */
    @Query("""
        UPDATE timer_state SET
          is_running = 1,
          paused_accumulated_ms = paused_accumulated_ms + (:resumedAtMs - COALESCE(paused_at_ms, :resumedAtMs)),
          paused_at_ms = NULL,
          updated_at_ms = :updatedAtMs
        WHERE id = 1 AND active_task_id = :taskId
    """)
    suspend fun resume(taskId: Int, resumedAtMs: Long, updatedAtMs: Long)

    /**
     * Stop/complete: clear the running timer singleton.
     */
    suspend fun stop() = clear()
}
