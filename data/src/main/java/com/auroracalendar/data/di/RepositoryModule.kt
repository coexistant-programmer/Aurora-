package com.auroracalendar.data.di

import com.auroracalendar.data.repository.CategoryRepositoryImpl
import com.auroracalendar.data.repository.EventRepositoryImpl
import com.auroracalendar.data.repository.TaskRepositoryImpl
import com.auroracalendar.data.repository.TimerRepositoryImpl
import com.auroracalendar.data.repository.WidgetConfigRepositoryImpl
import com.auroracalendar.domain.repository.CategoryRepository
import com.auroracalendar.domain.repository.EventRepository
import com.auroracalendar.domain.repository.TaskRepository
import com.auroracalendar.domain.repository.TimerRepository
import com.auroracalendar.domain.repository.WidgetConfigRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent

/**
 * Binds repository interfaces to their Room-backed implementations.
 *
 * Decision: @Binds (no implementation body) lets Hilt wire the interface→impl
 * mapping without factory boilerplate. All repositories are application singletons
 * since they wrap a single Room database instance.
 */
@Module
@InstallIn(ApplicationComponent::class)
abstract class RepositoryModule {

    @Binds abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository
    @Binds abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository
    @Binds abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository
    @Binds abstract fun bindTimerRepository(impl: TimerRepositoryImpl): TimerRepository
    @Binds abstract fun bindWidgetConfigRepository(impl: WidgetConfigRepositoryImpl): WidgetConfigRepository
}
