package com.auroracalendar.ui.navigation

/**
 * Navigation routes for Aurora Calendar.
 *
 * Decision: string-based routes with typed arguments via `navArgument`. Deep links
 * (auroracalendar://date/..., auroracalendar://task/...) map onto these routes so
 * widgets can launch directly into a date or task.
 */
object AppRoutes {
    const val Dashboard = "dashboard"
    const val Calendar = "calendar"
    const val Month = "month"
    const val Week = "week"
    const val Day = "day/{dateMs}"
    const val DayDateKey = "dateMs"
    const val Year = "year"
    const val Agenda = "agenda"
    const val EventDetail = "event/{eventId}"
    const val EventIdKey = "eventId"
    const val TaskDetail = "task/{taskId}"
    const val TaskIdKey = "taskId"
    const val EventEditor = "event_editor"
    const val TaskEditor = "task_editor"
    const val Settings = "settings"
    const val Search = "search"
}
