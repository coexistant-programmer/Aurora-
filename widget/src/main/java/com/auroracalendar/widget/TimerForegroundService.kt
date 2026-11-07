package com.auroracalendar.widget

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.auroracalendar.data.db.AuroraDatabase
import com.auroracalendar.data.entity.Task

/**
 * Foreground service shown only while a task timer is actively running. Posts the
 * running-task notification with Pause / Resume / Complete actions. Stopped when the
 * timer is paused/completed/stopped — per spec, not kept running when timestamps suffice.
 */
class TimerForegroundService : Service() {

    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "aurora_timer_running"

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val db = AuroraDatabase.getInstance(this)
        val state = db.timerStateDao().get()
        val task = state?.activeTaskId?.let { db.taskDao().getById(it) }

        val channel = android.app.NotificationChannel(
            CHANNEL_ID, "Running task",
            android.app.NotificationManager.IMPORTANCE_DEFAULT
        )
        NotificationManagerCompat.from(this).createNotificationChannel(channel)

        val pauseIntent = PendingIntent.getBroadcast(
            this, 1002,
            Intent(this, AuroraWidgetProvider::class.java).apply {
                action = AuroraWidgetProvider.ACTION_TIMER_PAUSE
                putExtra(AuroraWidgetProvider.EXTRA_TASK_ID, state?.activeTaskId ?: 0)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val completeIntent = PendingIntent.getBroadcast(
            this, 1003,
            Intent(this, AuroraWidgetProvider::class.java).apply {
                action = AuroraWidgetProvider.ACTION_TIMER_COMPLETE
                putExtra(AuroraWidgetProvider.EXTRA_TASK_ID, state?.activeTaskId ?: 0)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_pause)
            .setContentTitle(task?.title ?: "Timer running")
            .setContentText("Task in progress")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", pauseIntent))
            .addAction(NotificationCompat.Action(android.R.drawable.ic_media_play, "Resume", PendingIntent.getBroadcast(
                this, 1004,
                Intent(this, AuroraWidgetProvider::class.java).apply {
                    action = AuroraWidgetProvider.ACTION_TIMER_PAUSE // resume handled by toggle logic
                    putExtra(AuroraWidgetProvider.EXTRA_TASK_ID, state?.activeTaskId ?: 0)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )))
            .addAction(NotificationCompat.Action(android.R.drawable.ic_menu_close_clear_cancel, "Complete", completeIntent))
            .build()

        startForeground(NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
    }
}
