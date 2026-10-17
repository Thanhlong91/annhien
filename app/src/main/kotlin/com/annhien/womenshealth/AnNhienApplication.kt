package com.annhien.womenshealth

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class for An Nhiên.
 *
 * - Hilt entry point for dependency injection.
 * - Custom WorkManager configuration (Hilt-compatible).
 * - No network initialization.
 * - No analytics or crash reporting SDK.
 */
@HiltAndroidApp
class AnNhienApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() {
            val builder = Configuration.Builder()
            if (::workerFactory.isInitialized) {
                builder.setWorkerFactory(workerFactory)
            }
            return builder.build()
        }

    override fun onCreate() {
        super.onCreate()
        // Load SQLCipher native libraries before any database access
        net.sqlcipher.database.SQLiteDatabase.loadLibs(this)

        // Initialize notification channels on app start
        com.annhien.womenshealth.core.notifications.ReminderAlarmReceiver
            .createNotificationChannels(this)
    }
}
