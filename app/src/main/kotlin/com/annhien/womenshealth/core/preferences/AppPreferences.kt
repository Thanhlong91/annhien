package com.annhien.womenshealth.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "annhien_preferences")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val CURRENT_LIFE_STAGE = stringPreferencesKey("current_life_stage")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val LOCK_TIMEOUT_SECONDS = intPreferencesKey("lock_timeout_seconds")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SECURE_SCREEN_ENABLED = booleanPreferencesKey("secure_screen_enabled")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val PIN_SALT = stringPreferencesKey("pin_salt")
        val CAMOUFLAGE_MODE_ENABLED = booleanPreferencesKey("camouflage_mode_enabled")
    }

    val isOnboardingCompleted: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }

    val currentLifeStage: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.CURRENT_LIFE_STAGE] ?: "CYCLE_TRACKING"
        }

    val isAppLockEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.APP_LOCK_ENABLED] ?: false
        }

    val isBiometricEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_ENABLED] ?: false
        }

    val pinHash: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.PIN_HASH]
        }

    val pinSalt: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.PIN_SALT]
        }

    val isCamouflageEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.CAMOUFLAGE_MODE_ENABLED] ?: false
        }

    val lockTimeoutSeconds: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.LOCK_TIMEOUT_SECONDS] ?: 0
        }

    val themeMode: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM"
        }

    val secureScreenEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.SECURE_SCREEN_ENABLED] ?: true
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setCurrentLifeStage(stage: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_LIFE_STAGE] = stage
        }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_LOCK_ENABLED] = enabled
        }
    }

    suspend fun savePin(hash: String, salt: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PIN_HASH] = hash
            preferences[PreferencesKeys.PIN_SALT] = salt
            preferences[PreferencesKeys.APP_LOCK_ENABLED] = true
        }
    }

    suspend fun clearPin() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.PIN_HASH)
            preferences.remove(PreferencesKeys.PIN_SALT)
            preferences[PreferencesKeys.APP_LOCK_ENABLED] = false
        }
    }

    suspend fun setCamouflageEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CAMOUFLAGE_MODE_ENABLED] = enabled
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setLockTimeoutSeconds(seconds: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCK_TIMEOUT_SECONDS] = seconds
        }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun setSecureScreenEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SECURE_SCREEN_ENABLED] = enabled
        }
    }
}
