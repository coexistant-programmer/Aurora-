package com.auroracalendar.domain.repository

import com.auroracalendar.domain.model.TaskCategory
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    suspend fun insert(category: TaskCategory): Long
    suspend fun insertAll(categories: List<TaskCategory>)
    suspend fun update(category: TaskCategory)
    suspend fun delete(category: TaskCategory)
    suspend fun deleteById(id: Int)
    suspend fun getById(id: Int): TaskCategory?
    suspend fun getByName(name: String): TaskCategory?
    fun getAll(): Flow<List<TaskCategory>>
}
