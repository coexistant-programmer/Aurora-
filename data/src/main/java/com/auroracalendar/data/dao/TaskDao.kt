package com.auroracalendar.data.dao

import androidx.room.*
import com.auroracalendar.data.entity.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<Task>)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Int): Task?

    @Query("SELECT * FROM tasks WHERE date_ms = :dayMs ORDER BY start_time_ms ASC, title ASC")
    fun getTasksForDay(dayMs: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE date_ms >= :start AND date_ms <= :end ORDER BY date_ms ASC, start_time_ms ASC")
    fun getTasksInRange(start: Long, end: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY date_ms ASC, start_time_ms ASC")
    fun getTasksByStatus(status: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE category_id = :categoryId ORDER BY date_ms ASC, start_time_ms ASC")
    fun getTasksByCategory(categoryId: Int): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status = 'running' LIMIT 1")
    suspend fun getRunningTask(): Task?

    @Query("""
        SELECT * FROM tasks
        WHERE title LIKE :query OR description LIKE :query OR notes LIKE :query
        ORDER BY date_ms DESC
    """)
    fun search(query: String): Flow<List<Task>>

    @Query("""
        UPDATE tasks SET
          actual_duration_ms = :actualDurationMs,
          session_count = :sessionCount,
          paused_duration_ms = :pausedDurationMs,
          overtime_ms = :overtimeMs,
          completed_at_ms = :completedAtMs,
          updated_at_ms = :updatedAtMs
        WHERE id = :id
    """)
    suspend fun updateTimeTracking(
        id: Int,
        actualDurationMs: Long,
        sessionCount: Int,
        pausedDurationMs: Long,
        overtimeMs: Long,
        completedAtMs: Long?,
        updatedAtMs: Long
    )
}
