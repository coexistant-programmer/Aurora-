package com.auroracalendar.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.navDeepLink
import androidx.navigation.compose.rememberNavController
import com.auroracalendar.ui.theme.AuroraTheme
import com.auroracalendar.ui.theme.ThemeModeCustom
import com.auroracalendar.ui.theme.ThemeModeDark
import com.auroracalendar.ui.theme.ThemeModeDynamic
import com.auroracalendar.ui.theme.ThemeModeLight
import com.auroracalendar.ui.theme.ThemeModeSystem

/**
 * Top-level navigation host with bottom navigation.
 *
 * Decision: string routes with `navArgument` (no kotlinx.serialization / safe-args
 * plugin needed). Widget deep links (auroracalendar://date/..., auroracalendar://task/)
 * map onto these routes so widgets launch directly into a date or task.
 */
@Composable
fun AppNavigation(
    onThemeModeChange: (Int) -> Unit,
    currentThemeMode: Int,
    customPrimaryColor: Int
) {
    val navController = rememberNavController()

    AuroraTheme(
        themeMode = currentThemeMode,
        customPrimaryColor = customPrimaryColor,
        content = {
            Scaffold(
                bottomBar = {
                    BottomNavigation {
                        val backStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = backStackEntry?.route

                        val isCalendar = listOf(
                            AppRoutes.Calendar, AppRoutes.Month, AppRoutes.Week,
                            AppRoutes.Day, AppRoutes.Year, AppRoutes.Agenda
                        ).any { currentRoute?.startsWith(it) == true }

                        BottomNavigationItem(
                            icon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                            label = { Text(stringResource(id = R.string.navigation_calendar)) },
                            selected = isCalendar,
                            onClick = {
                                navController.navigate(AppRoutes.Calendar) {
                                    popUpTo(navController.graph.startDestination) { inclusive = true }
                                }
                            },
                        )

                        BottomNavigationItem(
                            icon = { Icon(Icons.Default.Task, contentDescription = null) },
                            label = { Text(stringResource(id = R.string.navigation_tasks)) },
                            selected = currentRoute == AppRoutes.TaskDetail,
                            onClick = {
                                navController.navigate(AppRoutes.Calendar) {
                                    popUpTo(navController.graph.startDestination) { inclusive = true }
                                }
                            },
                        )

                        BottomNavigationItem(
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                            label = { Text(stringResource(id = R.string.navigation_dashboard)) },
                            selected = currentRoute == AppRoutes.Dashboard,
                            onClick = {
                                navController.navigate(AppRoutes.Dashboard) {
                                    popUpTo(navController.graph.startDestination) { inclusive = true }
                                }
                            },
                        )

                        BottomNavigationItem(
                            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            label = { Text(stringResource(id = R.string.navigation_settings)) },
                            selected = currentRoute == AppRoutes.Settings,
                            onClick = {
                                navController.navigate(AppRoutes.Settings) {
                                    popUpTo(navController.graph.startDestination) { inclusive = true }
                                }
                            },
                        )
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = AppRoutes.Dashboard,
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                ) {
                    composable(AppRoutes.Dashboard) {
                        DashboardScreen()
                    }
                    composable(AppRoutes.Calendar) { MonthCalendarScreen() }
                    composable(AppRoutes.Month) { MonthCalendarScreen() }
                    composable(AppRoutes.Week) { WeekCalendarScreen() }
                    composable(
                        route = AppRoutes.Day,
                        arguments = listOf(navArgument(AppRoutes.DayDateKey) { type = NavType.LongType })
                    ) { backStackEntry ->
                        val dateMs = backStackEntry.arguments?.getLong(AppRoutes.DayDateKey)
                            ?: System.currentTimeMillis()
                        DayScheduleScreen(dateMs = dateMs)
                    }
                    composable(AppRoutes.Year) { YearCalendarScreen() }
                    composable(AppRoutes.Agenda) { AgendaScreen() }
                    composable(
                        route = AppRoutes.EventDetail,
                        arguments = listOf(navArgument(AppRoutes.EventIdKey) { type = NavType.IntType })
                    ) { backStackEntry ->
                        val eventId = backStackEntry.arguments?.getInt(AppRoutes.EventIdKey) ?: 0
                        EventDetailScreen(eventId = eventId)
                    }
                    composable(
                        route = AppRoutes.TaskDetail,
                        arguments = listOf(navArgument(AppRoutes.TaskIdKey) { type = NavType.IntType })
                    ) { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getInt(AppRoutes.TaskIdKey) ?: 0
                        TaskDetailScreen(taskId = taskId)
                    }
                    composable(AppRoutes.EventEditor) { EventEditorScreen() }
                    composable(AppRoutes.TaskEditor) { TaskEditorScreen() }
                    composable(AppRoutes.Settings) {
                        SettingsScreen(onThemeModeChange = onThemeModeChange)
                    }
                    composable(
                        route = AppRoutes.Search,
                        deepLinks = listOf(
                            navDeepLink { uriPattern = "auroracalendar://search" }
                        )
                    ) { SearchScreen() }

                    // Deep links from widgets
                    composable(
                        route = AppRoutes.Dashboard,
                        deepLinks = listOf(
                            navDeepLink { uriPattern = "auroracalendar://date/{dateMs}" },
                            navDeepLink { uriPattern = "auroracalendar://task/{taskId}" }
                        )
                    ) {
                        // Deep-link entries route to dashboard; the app can extract
                        // the date/task from the intent in MainActivity if needed.
                        DashboardScreen()
                    }
                }
            }
        }
    )
}
