package com.auroracalendar.data.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.auroracalendar.data.db.AuroraDatabase
import com.auroracalendar.data.notification.NotificationHelper
import java.util.*

/**
 * Fires a scheduled event/task reminder. The alarm PendingIntent carries extras:
 * type ("event"/"task"), id, and title.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("type") ?: "task"
        val id = intent.getIntExtra("id", 0)
        val title = intent.getStringExtra("title") ?: "Aurora reminder"

        NotificationHelper.initChannels(context)
        NotificationHelper.postReminder(context, id, title, intent.getStringExtra("body"))

        // If this was a one-shot reminder and the item is now past, no further action.
    }
}
