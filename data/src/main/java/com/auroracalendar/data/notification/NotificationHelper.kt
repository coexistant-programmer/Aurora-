package com.auroracalendar.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    const val CHANNEL_REMINDERS = "aurora_reminders"
    const val CHANNEL_TIMER = "aurora_timer"
    const val CHANNEL_SYSTEM = "aurora_system"

    val NOTIFICATION_ID_TIMER_EXPIRY = 2001
    val NOTIFICATION_ID_RUNNING_TASK = 2002

    fun initChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_REMINDERS, "Reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Event and task reminders"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_TIMER, "Timer", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Running-task and timer-expiry notifications"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_SYSTEM, "System", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Daily planning, backup, and sync notices"
            }
        )
    }

    fun postReminder(context: Context, id: Int, title: String, body: String?, channel: String = CHANNEL_REMINDERS) {
        initChannels(context)
        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle(title)
            .setContentText(body ?: "")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        NotificationManagerCompat.from(context).notify(id, builder.build())
    }

    fun postTimerExpiry(context: Context, taskTitle: String, taskId: Int) {
        initChannels(context)
        val completeIntent = PendingIntentAction.build(context, "complete", taskId)
        val continueIntent = PendingIntentAction.build(context, "continue", taskId)
        val builder = NotificationCompat.Builder(context, CHANNEL_TIMER)
            .setSmallIcon(android.R.drawable.ic_media_pause)
            .setContentTitle("Time allocated")
            .setContentText("$taskTitle — allocated time has elapsed")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .addAction(NotificationCompat.Action(android.R.drawable.ic_input_get, "Complete", completeIntent))
            .addAction(NotificationCompat.Action(android.R.drawable.ic_menu_my_calendar, "Continue", continueIntent))
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_TIMER_EXPIRY, builder.build())
    }

    fun cancelTimerExpiry(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_TIMER_EXPIRY)
    }
}

/** Builds a PendingIntent that delivers a timer action to TimerExpiryReceiver. */
object PendingIntentAction {
    fun build(context: Context, action: String, taskId: Int): PendingIntent {
        val intent = android.content.Intent(context, com.auroracalendar.data.receiver.TimerExpiryReceiver::class.java).apply {
            this.action = action
            putExtra("taskId", taskId)
        }
        return android.app.PendingIntent.getBroadcast(
            context, taskId.hashCode(), intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
    }
}
