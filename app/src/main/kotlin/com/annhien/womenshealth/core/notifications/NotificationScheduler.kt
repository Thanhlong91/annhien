package com.annhien.womenshealth.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.annhien.womenshealth.core.database.entity.ReminderEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline alarm scheduler using Android AlarmManager.
 * Schedules exact alarms when permitted, falling back to standard alarms.
 */
@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(reminder: ReminderEntity) {
        val triggerInstant = reminder.scheduledAt ?: return
        if (triggerInstant.isBefore(Instant.now())) {
            // Already passed
            return
        }

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_REMINDER_ID, reminder.id)
            putExtra(ReminderAlarmReceiver.EXTRA_TITLE, reminder.title)
            putExtra(ReminderAlarmReceiver.EXTRA_MESSAGE, getReminderMessage(reminder))
            putExtra(ReminderAlarmReceiver.EXTRA_CHANNEL_ID, getChannelIdForType(reminder.reminderType))
            putExtra(ReminderAlarmReceiver.EXTRA_NOTIFICATION_ID, reminder.id.hashCode())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = triggerInstant.toEpochMilli()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelReminder(reminderId: String) {
        val intent = Intent(context, ReminderAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun getReminderMessage(reminder: ReminderEntity): String {
        return when (reminder.reminderType) {
            "PERIOD_FORECAST" -> "Dự đoán kỳ kinh nguyệt có thể bắt đầu trong vài ngày tới."
            "PERIOD_DELAYED" -> "Kỳ kinh nguyệt của bạn có thể đang chậm hơn thường lệ."
            "PILL_DAILY" -> "Đến giờ uống thuốc tránh thai hàng ngày."
            "MEDICATION" -> "Đến giờ uống thuốc hoặc vi chất bổ sung."
            "HYDRATION" -> "Đừng quên uống một ly nước để giữ cơ thể tươi trẻ."
            "JOURNAL_LOG" -> "Hãy dành một phút ghi lại cảm nhận cơ thể hôm nay."
            "DOCTOR_VISIT" -> "Bạn có lịch hẹn khám sức khỏe sắp tới."
            "CONTRACTIONS_WARN" -> "Lưu ý tần suất cơn co thắt chuyển dạ."
            else -> reminder.title
        }
    }

    private fun getChannelIdForType(type: String): String {
        return when (type) {
            "PERIOD_FORECAST", "PERIOD_DELAYED" -> ReminderAlarmReceiver.CHANNEL_ID_PERIOD
            "PILL_DAILY", "MEDICATION" -> ReminderAlarmReceiver.CHANNEL_ID_MEDICATION
            "DOCTOR_VISIT" -> ReminderAlarmReceiver.CHANNEL_ID_APPOINTMENT
            else -> ReminderAlarmReceiver.CHANNEL_ID_DAILY_HEALTH
        }
    }
}
