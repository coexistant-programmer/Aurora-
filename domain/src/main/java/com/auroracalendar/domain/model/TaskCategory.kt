package com.auroracalendar.domain.model

/**
 * Domain model for a user-defined task category.
 */
data class TaskCategory(
    val id: Int = 0,
    val name: String,
    val icon: String = "category",
    val color: Int = 0xFF6200EE,
    val sortOrder: Int = 0
)
