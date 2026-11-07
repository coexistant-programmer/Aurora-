package com.auroracalendar.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import com.auroracalendar.data.db.AuroraDatabase
import java.text.DateFormat
import java.util.*
import java.util.concurrent.Executors

/**
 * Home-screen app widget provider for Aurora Calendar.
 *
 * Threading model: AppWidgetProvider callbacks run on the main thread, but Room
 * forbids main-thread database access. All DB reads happen on a single background
 * executor; RemoteViews are built there and applied on the main thread via Handler.
 *
 * Widgets read directly from Room (AuroraDatabase) rather than through Hilt-injected
 * repositories, because AppWidgetProvider instances are created by the system and
 * Hilt injection of receivers adds fragility. The repository pattern is still used
 * by the UI layer; widgets treat the database as the source of truth.
 *
 * Timer values are rendered timestamp-based at view-bind time (remaining =
 * now - started - paused), so they never go stale between widget refresh windows.
 */
class AuroraWidgetProvider : AppWidgetProvider() {

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    // Widget action keys for timer control buttons
    const val ACTION_TIMER_PAUSE = "com.auroracalendar.widget.action.TIMER_PAUSE"
    const val ACTION_TIMER_COMPLETE = "com.auroracalendar.widget.action.TIMER_COMPLETE"
    const val EXTRA_WIDGET_ID = "com.auroracalendar.widget.extra.WIDGET_ID"
    const val EXTRA_TASK_ID = "com.auroracalendar.widget.extra.TASK_ID"

    private fun layoutId(widgetType: String): Int = when (widgetType) {
        "task_timer" -> R.layout.widget_task_timer
        "monthly_calendar" -> R.layout.widget_monthly_calendar
        "agenda" -> R.layout.widget_agenda
        "quick_add" -> R.layout.widget_quick_add
        "focus" -> R.layout.widget_focus
        else -> R.layout.widget_daily_planner // daily_planner default
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            refreshWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        refreshWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        executor.execute {
            for (id in appWidgetIds) {
                AuroraDatabase.getInstance(context.applicationContext).widgetConfigDao().deleteById(id)
            }
        }
    }

    override fun onEnabled(context: Context) {
        refreshAllWidgets(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        when (action) {
            ACTION_TIMER_PAUSE, ACTION_TIMER_COMPLETE -> {
                val widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                val taskId = intent.getIntExtra(EXTRA_TASK_ID, 0)
                executor.execute {
                    val db = AuroraDatabase.getInstance(context.applicationContext)
                    val state = db.timerStateDao().get()
                    if (state != null && state.activeTaskId == taskId) {
                        if (action == ACTION_TIMER_PAUSE && state.isRunning) {
                            db.timerStateDao().pause(taskId, System.currentTimeMillis(), System.currentTimeMillis())
                        } else if (action == ACTION_TIMER_COMPLETE) {
                            db.timerStateDao().clear()
                        }
                    }
                    mainHandler.post {
                        val mgr = AppWidgetManager.getInstance(context)
                        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                            refreshWidget(context, mgr, widgetId)
                        } else {
                            refreshAllWidgets(context)
                        }
                    }
                }
            }
        }
    }

    private fun refreshWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        executor.execute {
            val db = AuroraDatabase.getInstance(context.applicationContext)
            val config = db.widgetConfigDao().getById(appWidgetId)
            val widgetType = config?.widgetType ?: "daily_planner"

            val views = RemoteViews(context.packageName, layoutId(widgetType))
            val now = System.currentTimeMillis()
            val today = java.util.Date(now)
            val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
            views.setTextViewText(R.id.widget_dp_date, dateFormat.format(today))

            // Tap whole widget → open app today (deep link passes epoch ms as dateMs)
            val tapIntent = Intent(Intent.ACTION_VIEW).apply {
                setPackage(context.packageName)
                data = android.net.Uri.parse("auroracalendar://date/$now")
            }
            val tapPending = PendingIntent.getActivity(
                context, appWidgetId, tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_dp_date, tapPending)

            when (widgetType) {
                "task_timer" -> buildTaskTimerViews(db, views, appWidgetId, context)
                "daily_planner" -> buildDailyPlannerViews(db, views)
                "monthly_calendar" -> buildMonthlyCalendarViews(views)
                "agenda" -> buildAgendaViews(db, views)
                "quick_add" -> buildQuickAddViews(context, views, appWidgetId)
                "focus" -> buildFocusViews(db, views)
            }

            mainHandler.post {
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    private fun buildTaskTimerViews(
        db: AuroraDatabase, views: RemoteViews, appWidgetId: Int, context: Context
    ) {
        val state = db.timerStateDao().get()
        if (state != null && state.isRunning && state.activeTaskId != null) {
            val task = db.taskDao().getById(state.activeTaskId)
            if (task != null) {
                views.setTextViewText(R.id.widget_tt_task, task.title)
                val elapsed = (System.currentTimeMillis() - (state.startedAtMs ?: 0L)) - state.pausedAccumulatedMs
                val remaining = (task.allocatedDurationMs - elapsed).coerceAtLeast(0)
                views.setTextViewText(R.id.widget_tt_remaining, formatRemaining(remaining))
                val progress = if (task.allocatedDurationMs > 0)
                    (elapsed.coerceAtLeast(0).toFloat() / task.allocatedDurationMs * 1000).coerceIn(0f, 1000f).toInt()
                else 0
                views.setProgressBar(R.id.widget_tt_progress, 1000, progress, false)

                val pauseIntent = Intent(context, AuroraWidgetProvider::class.java).apply {
                    action = ACTION_TIMER_PAUSE
                    putExtra(EXTRA_WIDGET_ID, appWidgetId)
                    putExtra(EXTRA_TASK_ID, state.activeTaskId)
                }
                val completeIntent = Intent(context, AuroraWidgetProvider::class.java).apply {
                    action = ACTION_TIMER_COMPLETE
                    putExtra(EXTRA_WIDGET_ID, appWidgetId)
                    putExtra(EXTRA_TASK_ID, state.activeTaskId)
                }
                views.setOnClickPendingIntent(
                    R.id.widget_tt_pause,
                    PendingIntent.getBroadcast(context, appWidgetId, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                )
                views.setOnClickPendingIntent(
                    R.id.widget_tt_complete,
                    PendingIntent.getBroadcast(context, appWidgetId, completeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                )
                views.setViewVisibility(R.id.widget_tt_pause, android.view.View.VISIBLE)
                views.setViewVisibility(R.id.widget_tt_complete, android.view.View.VISIBLE)
            } else {
                views.setTextViewText(R.id.widget_tt_task, "No task running")
            }
        } else {
            views.setTextViewText(R.id.widget_tt_task, "No task running")
            views.setTextViewText(R.id.widget_tt_remaining, "—")
            views.setProgressBar(R.id.widget_tt_progress, 1000, 0, false)
        }
    }

    private fun buildDailyPlannerViews(db: AuroraDatabase, views: RemoteViews) {
        views.setTextViewText(R.id.widget_dp_task1, "Open Aurora to view today's plan")
        views.setTextViewText(R.id.widget_dp_task2, "")
        views.setTextViewText(R.id.widget_dp_task3, "")
    }

    private fun buildMonthlyCalendarViews(views: RemoteViews) {
        views.setTextViewText(android.R.id.text1, "Monthly")
    }

    private fun buildAgendaViews(db: AuroraDatabase, views: RemoteViews) {
        views.setTextViewText(R.id.widget_ag_item1, "Open Aurora to view your agenda")
        views.setTextViewText(R.id.widget_ag_item2, "")
        views.setTextViewText(R.id.widget_ag_item3, "")
    }

    private fun buildQuickAddViews(context: Context, views: RemoteViews, appWidgetId: Int) {
        val addIntent = Intent(Intent.ACTION_VIEW).apply {
            setPackage(context.packageName)
            data = android.net.Uri.parse("auroracalendar://task/0")
        }
        views.setOnClickPendingIntent(
            R.id.widget_qa_add,
            PendingIntent.getActivity(context, appWidgetId, addIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        )
    }

    private fun buildFocusViews(db: AuroraDatabase, views: RemoteViews) {
        val state = db.timerStateDao().get()
        if (state != null && state.isRunning && state.activeTaskId != null) {
            val task = db.taskDao().getById(state.activeTaskId)
            task?.let {
                views.setTextViewText(R.id.widget_focus_task, it.title)
                val elapsed = (System.currentTimeMillis() - (state.startedAtMs ?: 0L)) - state.pausedAccumulatedMs
                val remaining = (it.allocatedDurationMs - elapsed).coerceAtLeast(0)
                views.setTextViewText(R.id.widget_focus_remaining, formatRemaining(remaining))
            }
        } else {
            views.setTextViewText(R.id.widget_focus_task, "No focus task")
            views.setTextViewText(R.id.widget_focus_remaining, "—")
        }
        views.setTextViewText(R.id.widget_focus_done, "Completed today: 0")
    }

    private fun formatRemaining(ms: Long): String {
        val totalSecs = ms / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return "%02d:%02d".format(mins, secs)
    }

    private fun refreshAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, AuroraWidgetProvider::class.java)
        val ids = appWidgetManager.getAppWidgetIds(component)
        for (id in ids) {
            refreshWidget(context, appWidgetManager, id)
        }
    }

    companion object {
        fun notifyWidgetsUpdated(context: Context) {
            val provider = AuroraWidgetProvider()
            provider.refreshAllWidgets(context)
        }
    }
}
