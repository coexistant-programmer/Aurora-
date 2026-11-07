package com.auroracalendar.domain.repository

import com.auroracalendar.domain.model.WidgetConfig
import kotlinx.coroutines.flow.Flow

interface WidgetConfigRepository {
    suspend fun insert(config: WidgetConfig)
    suspend fun update(config: WidgetConfig)
    suspend fun getById(widgetId: Int): WidgetConfig?
    fun getAll(): Flow<List<WidgetConfig>>
    suspend fun deleteById(widgetId: Int)
}
