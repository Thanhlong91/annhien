package com.annhien.womenshealth.core.online.environment

import com.annhien.womenshealth.BuildConfig
import com.annhien.womenshealth.core.connectivity.OnlineGateway
import com.annhien.womenshealth.core.connectivity.OnlineResult
import com.annhien.womenshealth.core.preferences.OnlineUtilityPreferences
import kotlinx.coroutines.flow.firstOrNull
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenAqRepository @Inject constructor(
    private val api: OpenAqApi,
    private val gateway: OnlineGateway,
    private val preferences: OnlineUtilityPreferences? = null
) {

    companion object {
        const val SOURCE_ID = "openaq"
        private const val CACHE_TTL_MINUTES = 30L
    }

    suspend fun getNearbyLocations(
        latitude: Double,
        longitude: Double
    ): OnlineResult<List<OpenAqLocation>> {
        val customKey = preferences?.getOpenAqApiKey()?.firstOrNull() ?: ""
        val effectiveKey = customKey.ifBlank { BuildConfig.OPENAQ_API_KEY }
        if (effectiveKey.isBlank()) {
            return OnlineResult.Error.InvalidApiKey
        }

        val coordinates = "$latitude,$longitude"

        return gateway.execute(
            sourceId = SOURCE_ID,
            cacheKey = "openaq_locations_$coordinates",
            ttl = Duration.ofMinutes(CACHE_TTL_MINUTES)
        ) {
            api.getLocations(coordinates = coordinates).results
        }
    }
}
