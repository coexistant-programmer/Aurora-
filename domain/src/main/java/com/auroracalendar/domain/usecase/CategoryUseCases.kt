package com.auroracalendar.domain.usecase

import com.auroracalendar.domain.model.TaskCategory
import com.auroracalendar.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

/**
 * Category use cases: CRUD for user-defined task categories (name, icon, color).
 */
class CreateCategory(private val categoryRepository: CategoryRepository) {
    operator fun invoke(category: TaskCategory): Long = categoryRepository.insert(category)
}

class UpdateCategory(private val categoryRepository: CategoryRepository) {
    operator fun invoke(category: TaskCategory) = categoryRepository.update(category)
}

class DeleteCategory(private val categoryRepository: CategoryRepository) {
    operator fun invoke(category: TaskCategory) = categoryRepository.delete(category)
}

class GetAllCategories(private val categoryRepository: CategoryRepository) {
    operator fun invoke(): Flow<List<TaskCategory>> = categoryRepository.getAll()
}

class GetCategoryById(private val categoryRepository: CategoryRepository) {
    operator fun invoke(id: Int) = categoryRepository.getById(id)
}
