package com.auroracalendar.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.auroracalendar.ui.navigation.AppNavigation
import com.auroracalendar.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hilt (via @AndroidEntryPoint) supplies the ViewModelProvider.Factory,
        // so viewModel<SettingsViewModel>() resolves the Hilt-injected instance.
        setContent {
            val settingsViewModel = viewModel<SettingsViewModel>()
            AppNavigation(
                onThemeModeChange = { settingsViewModel.setThemeMode(it) },
                currentThemeMode = settingsViewModel.themeMode.value,
                customPrimaryColor = settingsViewModel.customPrimaryColor.value
            )
        }
    }
}
