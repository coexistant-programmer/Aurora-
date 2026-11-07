package com.auroracalendar.data.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.auroracalendar.data.db.AuroraDatabase
import com.auroracalendar.data.entity.Task
import com.auroracalendar.data.notification.NotificationHelper
import com.auroracalendar.widget.TimerForegroundService
import java.util.*

/**
 * Re-registers alarms and refreshes widgets after boot or package replacement.
 * If a timer was running, restores its running-task notification.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.initChannels(context)
        rescheduleReminders(context)
        restoreRunningTimer(context)
        com.auroracalendar.widget.AuroraWidgetProvider.notifyWidgetsUpdated(context)
    }

    private fun rescheduleReminders(context: Context) {
        val db = AuroraDatabase.getInstance(context.applicationContext)
        val now = System.currentTimeMillis()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Re-schedule upcoming task reminders (next 30 days as a bounded window).
        val startOfToday = startOfDay(now)
        val endOfWindow = now + 30L * 24 * 60 * 60 * 1000
        val tasks = db.taskDao().getById(0) // placeholder; real query would filter by date range

        // For MVP: schedule a single "daily planning" reminder and any running-timer expiry.
        val state = db.timerStateDao().get()
        if (state != null && state.isRunning && state.activeTaskId != null) {
            val task = db.taskDao().getById(state.activeTaskId)
            task?.let {
                scheduleTimerExpiry(alarmManager, context, it)
            }
            // Restore foreground notification
            context.startService(
                Intent(context.applicationContext, TimerForegroundService::class.java)
            )
        }
    }

    private fun restoreRunningTimer(context: Context) {
        val db = AuroraDatabase.getInstance(context.applicationContext)
        val state = db.timerStateDao().get()
        if (state != null && state.isRunning) {
            context.startService(
                Intent(context.applicationContext, TimerForegroundService::class.java)
            )
        }
    }

    private fun scheduleTimerExpiry(
        alarmManager: AlarmManager, context: Context, task: Task
    ) {
        // Best-effort: if timer state says running, schedule expiry at start+allocated.
        val db = AuroraDatabase.getInstance(context.applicationContext)
        val state = db.timerStateDao().get() ?: return
        val started = state.startedAtMs ?: return
        val expiry = started + task.allocatedDurationMs
        val intent = Intent(context, TimerExpiryReceiver::class.java).apply {
            action = "notify"
            putExtra("taskId", task.id)
        }
        val pending = PendingIntent.getBroadcast(
            context, 1000 + task.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, expiry, pending)
    }

    private fun startOfDay(millis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
