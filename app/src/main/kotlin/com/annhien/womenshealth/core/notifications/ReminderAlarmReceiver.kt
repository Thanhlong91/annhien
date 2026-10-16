package com.annhien.womenshealth.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.annhien.womenshealth.MainActivity
import com.annhien.womenshealth.R

/**
 * Handles alarm triggers for reminders (period forecast, medication, hydration, visits).
 * Runs 100% offline using local AlarmManager and NotificationCompat.
 */
class ReminderAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID_PERIOD = "channel_period_reminders"
        const val CHANNEL_ID_MEDICATION = "channel_medication_reminders"
        const val CHANNEL_ID_DAILY_HEALTH = "channel_daily_health_reminders"
        const val CHANNEL_ID_APPOINTMENT = "channel_appointment_reminders"

        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_CHANNEL_ID = "extra_channel_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val periodChannel = NotificationChannel(
                    CHANNEL_ID_PERIOD,
                    "Nhắc nhở chu kỳ kinh nguyệt",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Thông báo dự đoán kỳ kinh sắp tới và theo dõi chu kỳ"
                }

                val medChannel = NotificationChannel(
                    CHANNEL_ID_MEDICATION,
                    "Nhắc uống thuốc & vitamin",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Nhắc nhở uống thuốc, sắt, acid folic đúng giờ"
                }

                val healthChannel = NotificationChannel(
                    CHANNEL_ID_DAILY_HEALTH,
                    "Nhắc nhở sức khỏe hằng ngày",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Nhắc uống nước, ghi chép nhật ký, đi ngủ"
                }

                val appointmentChannel = NotificationChannel(
                    CHANNEL_ID_APPOINTMENT,
                    "Nhắc lịch khám bệnh",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Thông báo trước lịch hẹn khám thai hoặc khám phụ khoa"
                }

                notificationManager.createNotificationChannels(
                    listOf(periodChannel, medChannel, healthChannel, appointmentChannel)
                )
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: context.getString(R.string.app_name)
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Đã đến giờ nhắc nhở sức khỏe của bạn"
        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID) ?: CHANNEL_ID_DAILY_HEALTH
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1001)

        createNotificationChannels(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
}
