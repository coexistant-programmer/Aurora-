package com.auroracalendar.ui.viewmodel

import android.app.Application
import androidx.datastore.preferences.PreferencesDataStore
import androidx.hilt.lifecycle.HiltViewModel
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.auroracalendar.core.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val store: PreferencesDataStore
) : AndroidViewModel(application) {

    private val _themeMode = MutableStateFlow(0)
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    private val _customPrimaryColor = MutableStateFlow(0xFF7C4DFF.toInt())
    val customPrimaryColor: StateFlow<Int> = _customPrimaryColor.asStateFlow()

    init {
        viewModelScope.launch {
            store.data
                .map { it[AppModule.THEME_MODE] ?: 0 }
                .collect { _themeMode.value = it }
        }
    }

    fun setThemeMode(value: Int) {
        viewModelScope.launch { AppModule.setThemeMode(store, value) }
    }

    fun setCustomPrimaryColor(value: Int) {
        viewModelScope.launch { AppModule.setCustomPrimaryColor(store, value) }
        _customPrimaryColor.value = value
    }
}
