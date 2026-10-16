package com.annhien.womenshealth.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.annhien.womenshealth.core.database.dao.ReminderDao

/**
 * Automatically re-arms scheduled reminders after device reboot (BOOT_COMPLETED)
 * or application package replacement (MY_PACKAGE_REPLACED).
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderDao: ReminderDao

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val activeReminders = reminderDao.getActiveReminders()
                    activeReminders.forEach { reminderEntity ->
                        notificationScheduler.scheduleReminder(reminderEntity)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
