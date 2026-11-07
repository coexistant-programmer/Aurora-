package com.auroracalendar.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.auroracalendar.data.db.AuroraDatabase
import com.auroracalendar.data.entity.WidgetConfig

/**
 * Configuration activity for a new widget instance. Lets the user pick the widget
 * type (daily planner / task timer / monthly calendar / agenda / quick add / focus)
 * and size, then saves the config and finishes.
 */
class WidgetConfigurationActivity : AppCompatActivity() {

    private lateinit var appWidgetId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras
        appWidgetId = extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        setContentView(R.layout.widget_config)

        val spinner = findViewById<Spinner>(R.id.widget_config_type)
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Daily Planner", "Task Timer", "Monthly Calendar", "Agenda", "Quick Add", "Focus"))
        spinner.adapter = adapter

        val btn = findViewById<Button>(R.id.widget_config_save)
        btn.setOnClickListener {
            val type = when (spinner.selectedItem.toString()) {
                "Task Timer" -> "task_timer"
                "Monthly Calendar" -> "monthly_calendar"
                "Agenda" -> "agenda"
                "Quick Add" -> "quick_add"
                "Focus" -> "focus"
                else -> "daily_planner"
            }
            val size = "medium"

            val db = AuroraDatabase.getInstance(this)
            db.widgetConfigDao().insert(WidgetConfig(widgetId = appWidgetId, size = size, widgetType = type))

            val appWidgetManager = AppWidgetManager.getInstance(this)
            appWidgetManager.updateAppWidgetOptions(appWidgetId, Bundle().apply {
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 40)
            })
            AuroraWidgetProvider.notifyWidgetsUpdated(this)

            val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }
}
