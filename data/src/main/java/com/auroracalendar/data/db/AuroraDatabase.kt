package com.auroracalendar.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.auroracalendar.data.db.typeconverters.TypeConverters
import com.auroracalendar.data.dao.CategoryDao
import com.auroracalendar.data.dao.EventDao
import com.auroracalendar.data.dao.TaskDao
import com.auroracalendar.data.dao.TimerSessionDao
import com.auroracalendar.data.dao.TimerStateDao
import com.auroracalendar.data.dao.WidgetConfigDao
import com.auroracalendar.data.entity.CalendarEvent
import com.auroracalendar.data.entity.Task
import com.auroracalendar.data.entity.TaskCategory
import com.auroracalendar.data.entity.TimerState
import com.auroracalendar.data.entity.TimerSession
import com.auroracalendar.data.entity.WidgetConfig

/**
 * Room database for Aurora Calendar. Single source of truth for all local data.
 *
 * Decision: Room over raw SQLite because it gives compile-checked SQL, live queries
 * (Flow) for reactive Compose UI, built-in migration support, and type converters —
 * all of which match the reliability and offline-first requirements.
 */
@Database(
    entities = [
        CalendarEvent::class,
        Task::class,
        TaskCategory::class,
        TimerState::class,
        TimerSession::class,
        WidgetConfig::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(TypeConverters::class)
abstract class AuroraDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
    abstract fun timerStateDao(): TimerStateDao
    abstract fun timerSessionDao(): TimerSessionDao
    abstract fun widgetConfigDao(): WidgetConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AuroraDatabase? = null

        fun getInstance(context: android.content.Context): AuroraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AuroraDatabase::class.java,
                    "aurora_calendar.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
