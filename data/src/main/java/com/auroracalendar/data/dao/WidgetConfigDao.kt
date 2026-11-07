package com.auroracalendar.data.dao

import androidx.room.*
import com.auroracalendar.data.entity.WidgetConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: WidgetConfig)

    @Update
    suspend fun update(config: WidgetConfig)

    @Query("SELECT * FROM widget_config WHERE widgetId = :widgetId")
    suspend fun getById(widgetId: Int): WidgetConfig?

    @Query("SELECT * FROM widget_config")
    fun getAll(): Flow<List<WidgetConfig>>

    @Query("DELETE FROM widget_config WHERE widgetId = :widgetId")
    suspend fun deleteById(widgetId: Int)
}
