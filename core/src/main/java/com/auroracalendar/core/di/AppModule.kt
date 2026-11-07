package com.auroracalendar.core.di

import android.content.Context
import android.app.Application
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.PreferencesDataStore
import com.google.dagger.hilt.android.qualifiers.ApplicationContext
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DI modules providing application-scoped dependencies shared across modules:
 * DataStore, application context, and app-wide settings flows.
 *
 * Decision: preferences live in :core (not :data) so that :data depends only on
 * :domain/:core and never on UI concerns; :data exposes repositories while :core
 * owns raw persisted primitives. This keeps the dependency direction clean.
 */
@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @ApplicationContext
    fun provideContext(application: Application): Context = application

    /**
     * DataStore for user preferences (theme, week start, defaults, notifications).
     * Exposed as both the raw DataStore and typed flows for specific keys.
     */
    @Provides
    fun provideDataStore(@ApplicationContext context: Context): PreferencesDataStore {
        return context.createDataStore(name = "aurora_preferences")
    }

    // Preference keys
    val THEME_MODE = preferencesKey<Int>("theme_mode")               // 0=light,1=dark,2=system,3=dynamic,4=custom
    val WEEK_START_MONDAY = preferencesKey<Boolean>("week_start_monday")
    val DEFAULT_TASK_DURATION_MIN = preferencesKey<Int>("default_task_duration_min")
    val DEFAULT_REMINDER_MIN = preferencesKey<Int>("default_reminder_min")
    val NOTIFICATIONS_ENABLED = preferencesKey<Boolean>("notifications_enabled")
    val WIDGET_REFRESH_ENABLED = preferencesKey<Boolean>("widget_refresh_enabled")
    val CUSTOM_PRIMARY_COLOR = preferencesKey<Int>("custom_primary_color")
    val FIRST_LAUNCH = preferencesKey<Boolean>("first_launch")

    @Provides
    fun provideThemeModeFlow(store: PreferencesDataStore): Flow<Int> =
        store.data.map { it[THEME_MODE] ?: 0 }

    @Provides
    fun provideWeekStartMondayFlow(store: PreferencesDataStore): Flow<Boolean> =
        store.data.map { it[WEEK_START_MONDAY] ?: true }

    @Provides
    fun provideDefaultTaskDurationFlow(store: PreferencesDataStore): Flow<Int> =
        store.data.map { it[DEFAULT_TASK_DURATION_MIN] ?: 90 }

    @Provides
    fun provideNotificationsEnabledFlow(store: PreferencesDataStore): Flow<Boolean> =
        store.data.map { it[NOTIFICATIONS_ENABLED] ?: true }

    suspend fun setThemeMode(store: PreferencesDataStore, value: Int) =
        store.edit { it[THEME_MODE] = value }

    suspend fun setWeekStartMonday(store: PreferencesDataStore, value: Boolean) =
        store.edit { it[WEEK_START_MONDAY] = value }

    suspend fun setDefaultTaskDuration(store: PreferencesDataStore, value: Int) =
        store.edit { it[DEFAULT_TASK_DURATION_MIN] = value }

    suspend fun setDefaultReminderMinutes(store: PreferencesDataStore, value: Int) =
        store.edit { it[DEFAULT_REMINDER_MIN] = value }

    suspend fun setNotificationsEnabled(store: PreferencesDataStore, value: Boolean) =
        store.edit { it[NOTIFICATIONS_ENABLED] = value }

    suspend fun setCustomPrimaryColor(store: PreferencesDataStore, value: Int) =
        store.edit { it[CUSTOM_PRIMARY_COLOR] = value }
}
