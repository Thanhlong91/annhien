package com.annhien.womenshealth.data.repository

import com.annhien.womenshealth.core.database.dao.DailyHabitDao
import com.annhien.womenshealth.core.database.dao.HabitLogDao
import com.annhien.womenshealth.core.database.entity.DailyHabitEntity
import com.annhien.womenshealth.core.database.entity.HabitLogEntity
import com.annhien.womenshealth.domain.repository.DailyHealthRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyHealthRepositoryImpl @Inject constructor(
    private val dailyHabitDao: DailyHabitDao,
    private val habitLogDao: HabitLogDao
) : DailyHealthRepository {

    override fun observeHabits(): Flow<List<DailyHabitEntity>> {
        return dailyHabitDao.observeActive()
    }

    override fun observeHabitLogs(date: LocalDate): Flow<List<HabitLogEntity>> {
        return habitLogDao.observeByDate(date.toString())
    }

    override suspend fun toggleHabit(habitId: String, date: LocalDate, completed: Boolean) {
        val dateStr = date.toString()
        val existingLogs = habitLogDao.getByHabitInRange(habitId, dateStr, dateStr)
        if (completed) {
            if (existingLogs.isEmpty()) {
                habitLogDao.upsert(
                    HabitLogEntity(
                        id = UUID.randomUUID().toString(),
                        habitId = habitId,
                        logDate = date,
                        isCompleted = true,
                        numericValue = 1.0,
                        createdAt = Instant.now()
                    )
                )
            }
        } else {
            existingLogs.forEach { habitLogDao.delete(it) }
        }
    }

    override suspend fun recordWaterIntake(date: LocalDate, amountMl: Int) {
        val waterHabits = dailyHabitDao.getByCategory("WATER")
        val habitId = waterHabits.firstOrNull()?.id ?: run {
            val newId = UUID.randomUUID().toString()
            dailyHabitDao.upsert(
                DailyHabitEntity(
                    id = newId,
                    habitName = "Uống nước",
                    category = "WATER",
                    targetDailyCount = 2000,
                    unit = "ml",
                    isActive = true,
                    createdAt = Instant.now()
                )
            )
            newId
        }

        habitLogDao.upsert(
            HabitLogEntity(
                id = UUID.randomUUID().toString(),
                habitId = habitId,
                logDate = date,
                isCompleted = amountMl >= 2000,
                numericValue = amountMl.toDouble(),
                createdAt = Instant.now()
            )
        )
    }

    override suspend fun saveDailyHabit(habit: DailyHabitEntity) {
        dailyHabitDao.upsert(habit)
    }
}
