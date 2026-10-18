package com.annhien.womenshealth.core.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.onlineUtilityPreferences by preferencesDataStore(
    name = "online_utility_preferences"
)

@Singleton
class OnlineUtilityPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        fun enabled(sourceId: String) = booleanPreferencesKey("enabled_$sourceId")
        val openAqApiKey = stringPreferencesKey("openaq_api_key")
        val aqicnToken = stringPreferencesKey("aqicn_token")
    }

    fun isEnabled(sourceId: String): Flow<Boolean> {
        return context.onlineUtilityPreferences.data.map { preferences ->
            preferences[Keys.enabled(sourceId)] ?: true
        }
    }

    suspend fun setEnabled(sourceId: String, enabled: Boolean) {
        context.onlineUtilityPreferences.edit { preferences ->
            preferences[Keys.enabled(sourceId)] = enabled
        }
    }

    fun getOpenAqApiKey(): Flow<String> {
        return context.onlineUtilityPreferences.data.map { preferences ->
            preferences[Keys.openAqApiKey] ?: ""
        }
    }

    suspend fun setOpenAqApiKey(apiKey: String) {
        context.onlineUtilityPreferences.edit { preferences ->
            preferences[Keys.openAqApiKey] = apiKey.trim()
        }
    }

    fun getAqicnToken(): Flow<String> {
        return context.onlineUtilityPreferences.data.map { preferences ->
            preferences[Keys.aqicnToken] ?: ""
        }
    }

    suspend fun setAqicnToken(token: String) {
        context.onlineUtilityPreferences.edit { preferences ->
            preferences[Keys.aqicnToken] = token.trim()
        }
    }
}
