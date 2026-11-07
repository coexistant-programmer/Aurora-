package com.auroracalendar.data.repository

import com.auroracalendar.data.dao.TimerStateDao
import com.auroracalendar.data.mapper.EntityMappers.toDomain
import com.auroracalendar.data.mapper.EntityMappers.toEntity
import com.auroracalendar.domain.model.TimerState
import com.auroracalendar.domain.repository.TimerRepository
import javax.inject.Inject

class TimerRepositoryImpl @Inject constructor(private val timerStateDao: TimerStateDao) : TimerRepository {

    override suspend fun getState(): TimerState? = timerStateDao.get()?.toDomain()

    override suspend fun insert(state: TimerState) = timerStateDao.insert(state.toEntity())

    override suspend fun update(state: TimerState) = timerStateDao.update(state.toEntity())

    override suspend fun clear() = timerStateDao.clear()

    override suspend fun start(taskId: Int, startedAtMs: Long, updatedAtMs: Long) =
        timerStateDao.start(taskId, startedAtMs, updatedAtMs)

    override suspend fun pause(taskId: Int, pausedAtMs: Long, updatedAtMs: Long) =
        timerStateDao.pause(taskId, pausedAtMs, updatedAtMs)

    override suspend fun resume(taskId: Int, resumedAtMs: Long, updatedAtMs: Long) =
        timerStateDao.resume(taskId, resumedAtMs, updatedAtMs)

    override suspend fun stop() = timerStateDao.stop()
}
