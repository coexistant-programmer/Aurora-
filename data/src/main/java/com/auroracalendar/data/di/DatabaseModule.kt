package com.auroracalendar.data.di

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.dagger.hilt.android.qualifiers.ApplicationContext
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent

/**
 * Hilt module providing the Room database and DAOs as application singletons.
 *
 * Decision: a single database instance (not per-DAO) keeps transactions consistent
 * across events, tasks, and timer state — critical for timer accuracy and atomic
 * task-completion updates. Hilt @Singleton scope guarantees one instance.
 */
@Module
@InstallIn(ApplicationComponent::class)
object DatabaseModule {

    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AuroraDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AuroraDatabase::class.java,
            "aurora_calendar.db"
        )
            .fallbackToDestructiveMigration() // placeholder; real migrations added in Milestone 12
            .build()
    }

    @Provides
    fun provideEventDao(db: AuroraDatabase): EventDao = db.eventDao()

    @Provides
    fun provideTaskDao(db: AuroraDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideCategoryDao(db: AuroraDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideTimerStateDao(db: AuroraDatabase): TimerStateDao = db.timerStateDao()

    @Provides
    fun provideTimerSessionDao(db: AuroraDatabase): TimerSessionDao = db.timerSessionDao()

    @Provides
    fun provideWidgetConfigDao(db: AuroraDatabase): WidgetConfigDao = db.widgetConfigDao()
}
