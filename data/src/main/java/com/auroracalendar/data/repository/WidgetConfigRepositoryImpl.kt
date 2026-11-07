package com.auroracalendar.data.repository

import com.auroracalendar.data.dao.WidgetConfigDao
import com.auroracalendar.data.mapper.EntityMappers.toDomain
import com.auroracalendar.data.mapper.EntityMappers.toEntity
import com.auroracalendar.domain.model.WidgetConfig
import com.auroracalendar.domain.repository.WidgetConfigRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WidgetConfigRepositoryImpl @Inject constructor(private val widgetConfigDao: WidgetConfigDao) : WidgetConfigRepository {

    override suspend fun insert(config: WidgetConfig) = widgetConfigDao.insert(config.toEntity())

    override suspend fun update(config: WidgetConfig) = widgetConfigDao.update(config.toEntity())

    override suspend fun getById(widgetId: Int): WidgetConfig? =
        widgetConfigDao.getById(widgetId)?.toDomain()

    override fun getAll(): Flow<List<WidgetConfig>> =
        widgetConfigDao.getAll().map { it.map { it.toDomain() } }

    override suspend fun deleteById(widgetId: Int) = widgetConfigDao.deleteById(widgetId)
}
