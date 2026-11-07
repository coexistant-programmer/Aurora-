package com.auroracalendar.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User-defined task category with name, icon, and color.
 */
@Entity(tableName = "task_categories")
data class TaskCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "icon") val icon: String = "category", // icon name/codepoint
    @ColumnInfo(name = "color") val color: Int = 0xFF6200EE, // default purple
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0
)
