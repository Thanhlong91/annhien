package com.annhien.womenshealth

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.annhien.womenshealth.core.backup.BackupManager
import com.annhien.womenshealth.core.design.AnNhienTheme
import com.annhien.womenshealth.core.preferences.AppPreferences
import com.annhien.womenshealth.core.security.KeystoreManager
import com.annhien.womenshealth.feature.lock.LockScreen
import com.annhien.womenshealth.navigation.MainNavGraph
import com.annhien.womenshealth.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executor
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    @Inject
    lateinit var backupManager: BackupManager

    @Inject
    lateinit var keystoreManager: KeystoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isOnboardingComplete by appPreferences.isOnboardingCompleted.collectAsState(initial = null)
            val isLockEnabled by appPreferences.isAppLockEnabled.collectAsState(initial = false)
            val isBiometricEnabled by appPreferences.isBiometricEnabled.collectAsState(initial = false)
            val isSecureScreen by appPreferences.secureScreenEnabled.collectAsState(initial = true)
            val themeMode by appPreferences.themeMode.collectAsState(initial = "SYSTEM")

            // Dynamic FLAG_SECURE
            LaunchedEffect(isSecureScreen) {
                if (isSecureScreen) {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            val darkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            var isAppUnlocked by remember { mutableStateOf(!isLockEnabled) }

            // Update unlocked state if lock was initially false
            LaunchedEffect(isLockEnabled) {
                if (!isLockEnabled) {
                    isAppUnlocked = true
                }
            }

            AnNhienTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isOnboardingComplete == null) {
                        // Loading initial state
                    } else if (isLockEnabled && !isAppUnlocked) {
                        LockScreen(
                            expectedPinHash = null,
                            salt = null,
                            onUnlocked = { isAppUnlocked = true },
                            onBiometricRequested = if (isBiometricEnabled) {
                                { showBiometricPrompt { isAppUnlocked = true } }
                            } else null,
                            isBiometricAvailable = isBiometricEnabled
                        )
                    } else {
                        val navController = rememberNavController()
                        val startDestination = if (isOnboardingComplete == true) {
                            Screen.Home.route
                        } else {
                            Screen.Welcome.route
                        }

                        MainNavGraph(
                            navController = navController,
                            startDestination = startDestination,
                            appPreferences = appPreferences,
                            backupManager = backupManager
                        )
                    }
                }
            }
        }
    }

    private fun showBiometricPrompt(onSuccess: () -> Unit) {
        val executor: Executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.lock_biometric_prompt))
            .setSubtitle(getString(R.string.lock_biometric_subtitle))
            .setNegativeButtonText(getString(R.string.action_cancel))
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
