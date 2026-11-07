package com.auroracalendar.data.repository

import com.auroracalendar.data.dao.CategoryDao
import com.auroracalendar.data.mapper.EntityMappers.toDomain
import com.auroracalendar.data.mapper.EntityMappers.toEntity
import com.auroracalendar.domain.model.TaskCategory
import com.auroracalendar.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(private val categoryDao: CategoryDao) : CategoryRepository {

    override suspend fun insert(category: TaskCategory): Long =
        categoryDao.insert(category.toEntity())

    override suspend fun insertAll(categories: List<TaskCategory>) =
        categoryDao.insertAll(categories.map { it.toEntity() })

    override suspend fun update(category: TaskCategory) =
        categoryDao.update(category.toEntity())

    override suspend fun delete(category: TaskCategory) =
        categoryDao.delete(category.toEntity())

    override suspend fun deleteById(id: Int) = categoryDao.deleteById(id)

    override suspend fun getById(id: Int): TaskCategory? =
        categoryDao.getById(id)?.toDomain()

    override suspend fun getByName(name: String): TaskCategory? =
        categoryDao.getByName(name)?.toDomain()

    override fun getAll(): Flow<List<TaskCategory>> =
        categoryDao.getAll().map { it.map { it.toDomain() } }
}
