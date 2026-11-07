package com.auroracalendar.data.dao

import androidx.room.*
import com.auroracalendar.data.entity.TaskCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: TaskCategory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<TaskCategory>)

    @Update
    suspend fun update(category: TaskCategory)

    @Delete
    suspend fun delete(category: TaskCategory)

    @Query("DELETE FROM task_categories WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM task_categories WHERE id = :id")
    suspend fun getById(id: Int): TaskCategory?

    @Query("SELECT * FROM task_categories ORDER BY sort_order ASC, name ASC")
    fun getAll(): Flow<List<TaskCategory>>

    @Query("SELECT * FROM task_categories WHERE name = :name")
    suspend fun getByName(name: String): TaskCategory?
}
