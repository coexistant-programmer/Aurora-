package com.auroracalendar.data.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.auroracalendar.data.db.AuroraDatabase
import com.auroracalendar.data.entity.Task
import com.auroracalendar.data.notification.NotificationHelper

/**
 * Fires when a task timer's allocated duration expires. Posts the timer-expiry
 * notification (user may complete or continue). Does NOT auto-complete the task.
 */
class TimerExpiryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: "notify"
        val taskId = intent.getIntExtra("taskId", 0)

        val db = AuroraDatabase.getInstance(context.applicationContext)
        val task: Task? = db.taskDao().getById(taskId)

        when (action) {
            "notify" -> {
                task?.let {
                    NotificationHelper.postTimerExpiry(context, it.title, it.id)
                }
            }
            "complete" -> {
                task?.let {
                    db.timerStateDao().clear()
                    db.taskDao().update(
                        it.copy(status = "completed", completedAtMs = System.currentTimeMillis(), updatedAtMs = System.currentTimeMillis())
                    )
                }
                NotificationHelper.cancelTimerExpiry(context)
            }
            "continue" -> {
                // Leave the timer running (overtime will be recorded on completion).
                NotificationHelper.cancelTimerExpiry(context)
                // Re-show running-task foreground notification if a service should persist.
                val state = db.timerStateDao().get()
                if (state != null && state.isRunning) {
                    context.startService(
                        Intent(context.applicationContext, com.auroracalendar.widget.TimerForegroundService::class.java)
                    )
                }
            }
        }
    }
}
