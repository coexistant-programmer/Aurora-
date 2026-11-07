package com.auroracalendar.data.mapper

import com.auroracalendar.data.entity.CalendarEvent as EventEntity
import com.auroracalendar.data.entity.Task as TaskEntity
import com.auroracalendar.data.entity.TaskCategory as CategoryEntity
import com.auroracalendar.data.entity.TimerState as TimerStateEntity
import com.auroracalendar.data.entity.WidgetConfig as WidgetConfigEntity
import com.auroracalendar.domain.model.CalendarEvent
import com.auroracalendar.domain.model.Task
import com.auroracalendar.domain.model.TaskCategory
import com.auroracalendar.domain.model.TimerState
import com.auroracalendar.domain.model.WidgetConfig
import java.time.Instant

object EntityMappers {

    // ---------- CalendarEvent ----------

    fun EventEntity.toDomain(): CalendarEvent =
        CalendarEvent(
            id = id,
            title = title,
            description = description,
            location = location,
            startTime = Instant.ofEpochMilli(startTimeMs),
            endTime = endTimeMs?.let { Instant.ofEpochMilli(it) },
            allDay = allDay,
            categoryId = categoryId,
            color = color,
            reminderMinutes = reminderMinutes,
            recurrenceRule = recurrenceRule,
            recurrenceId = recurrenceId,
            originalEventId = originalEventId,
            status = status,
            createdAt = Instant.ofEpochMilli(createdAtMs),
            updatedAt = Instant.ofEpochMilli(updatedAtMs)
        )

    fun CalendarEvent.toEntity(): EventEntity =
        EventEntity(
            id = id,
            title = title,
            description = description,
            location = location,
            startTimeMs = startTime.toEpochMilli(),
            endTimeMs = endTime?.toEpochMilli(),
            allDay = allDay,
            categoryId = categoryId,
            color = color,
            reminderMinutes = reminderMinutes,
            recurrenceRule = recurrenceRule,
            recurrenceId = recurrenceId,
            originalEventId = originalEventId,
            status = status,
            createdAtMs = createdAt.toEpochMilli(),
            updatedAtMs = updatedAt.toEpochMilli()
        )

    // ---------- Task ----------

    fun TaskEntity.toDomain(): Task =
        Task(
            id = id,
            title = title,
            description = description,
            dateMs = dateMs,
            startTimeMs = startTimeMs,
            allocatedDurationMs = allocatedDurationMs,
            endTimeMs = endTimeMs,
            priority = priority,
            categoryId = categoryId,
            color = color,
            status = status,
            deadlineMs = deadlineMs,
            reminderMinutes = reminderMinutes,
            subtasks = subtasks,
            notes = notes,
            actualDurationMs = actualDurationMs,
            sessionCount = sessionCount,
            pausedDurationMs = pausedDurationMs,
            overtimeMs = overtimeMs,
            completedAtMs = completedAtMs,
            createdAt = Instant.ofEpochMilli(createdAtMs),
            updatedAt = Instant.ofEpochMilli(updatedAtMs)
        )

    fun Task.toEntity(): TaskEntity =
        TaskEntity(
            id = id,
            title = title,
            description = description,
            dateMs = dateMs,
            startTimeMs = startTimeMs,
            allocatedDurationMs = allocatedDurationMs,
            endTimeMs = endTimeMs,
            priority = priority,
            categoryId = categoryId,
            color = color,
            status = status,
            deadlineMs = deadlineMs,
            reminderMinutes = reminderMinutes,
            subtasks = subtasks,
            notes = notes,
            actualDurationMs = actualDurationMs,
            sessionCount = sessionCount,
            pausedDurationMs = pausedDurationMs,
            overtimeMs = overtimeMs,
            completedAtMs = completedAtMs,
            createdAtMs = createdAt.toEpochMilli(),
            updatedAtMs = updatedAt.toEpochMilli()
        )

    // ---------- TaskCategory ----------

    fun CategoryEntity.toDomain(): TaskCategory =
        TaskCategory(id = id, name = name, icon = icon, color = color, sortOrder = sortOrder)

    fun TaskCategory.toEntity(): CategoryEntity =
        CategoryEntity(id = id, name = name, icon = icon, color = color, sortOrder = sortOrder)

    // ---------- TimerState ----------

    fun TimerStateEntity.toDomain(): TimerState =
        TimerState(
            id = id,
            activeTaskId = activeTaskId,
            startedAtMs = startedAtMs,
            isRunning = isRunning,
            pausedAtMs = pausedAtMs,
            pausedAccumulatedMs = pausedAccumulatedMs,
            updatedAt = Instant.ofEpochMilli(updatedAtMs)
        )

    fun TimerState.toEntity(): TimerStateEntity =
        TimerStateEntity(
            id = id,
            activeTaskId = activeTaskId,
            startedAtMs = startedAtMs,
            isRunning = isRunning,
            pausedAtMs = pausedAtMs,
            pausedAccumulatedMs = pausedAccumulatedMs,
            updatedAtMs = updatedAt.toEpochMilli()
        )

    // ---------- WidgetConfig ----------

    fun WidgetConfigEntity.toDomain(): WidgetConfig =
        WidgetConfig(widgetId = widgetId, size = size, widgetType = widgetType, configJson = configJson)

    fun WidgetConfig.toEntity(): WidgetConfigEntity =
        WidgetConfigEntity(widgetId = widgetId, size = size, widgetType = widgetType, configJson = configJson)
}
