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
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Initialize notification channels on app start
        com.annhien.womenshealth.core.notifications.NotificationChannelManager
            .createChannels(this)
    }
}
