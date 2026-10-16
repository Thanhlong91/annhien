package com.annhien.womenshealth.data.repository

import com.annhien.womenshealth.core.database.dao.ReminderDao
import com.annhien.womenshealth.core.database.entity.ReminderEntity
import com.annhien.womenshealth.core.notifications.NotificationScheduler
import com.annhien.womenshealth.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao,
    private val notificationScheduler: NotificationScheduler
) : ReminderRepository {

    override fun observeReminders(): Flow<List<ReminderEntity>> {
        return reminderDao.observeActive()
    }

    override suspend fun saveReminder(reminder: ReminderEntity) {
        reminderDao.upsert(reminder)
        if (reminder.isActive) {
            notificationScheduler.scheduleReminder(reminder)
        } else {
            notificationScheduler.cancelReminder(reminder.id)
        }
    }

    override suspend fun toggleReminder(reminderId: String, isActive: Boolean) {
        val now = Instant.now()
        if (isActive) {
            val list = reminderDao.getActiveReminders()
            val reminder = list.find { it.id == reminderId }
            if (reminder != null) {
                reminderDao.update(reminder.copy(isActive = true, updatedAt = now))
                notificationScheduler.scheduleReminder(reminder)
            }
        } else {
            reminderDao.deactivate(reminderId, now.toEpochMilli())
            notificationScheduler.cancelReminder(reminderId)
        }
    }

    override suspend fun deleteReminder(reminderId: String) {
        reminderDao.deactivate(reminderId, Instant.now().toEpochMilli())
        notificationScheduler.cancelReminder(reminderId)
    }
}
