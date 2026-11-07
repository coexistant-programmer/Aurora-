package com.auroracalendar.data.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.auroracalendar.data.db.AuroraDatabase
import com.auroracalendar.data.entity.Task
import com.auroracalendar.widget.TimerForegroundService

/**
 * Re-registers time-sensitive alarms when the timezone or system clock changes,
 * and keeps a running timer's foreground notification consistent.
 */
class TimeChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val db = AuroraDatabase.getInstance(context.applicationContext)
        val state = db.timerStateDao().get()
        if (state != null && state.isRunning && state.activeTaskId != null) {
            val task = db.taskDao().getById(state.activeTaskId)
            task?.let {
                scheduleTimerExpiry(context, it)
            }
            context.startService(
                Intent(context.applicationContext, TimerForegroundService::class.java)
            )
        }
    }

    private fun scheduleTimerExpiry(context: Context, task: Task) {
        val db = AuroraDatabase.getInstance(context.applicationContext)
        val state = db.timerStateDao().get() ?: return
        val started = state.startedAtMs ?: return
        val expiry = started + task.allocatedDurationMs
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TimerExpiryReceiver::class.java).apply {
            action = "notify"
            putExtra("taskId", task.id)
        }
        val pending = PendingIntent.getBroadcast(
            context, 2000 + task.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, expiry, pending)
    }
}
