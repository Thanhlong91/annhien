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
class WaqiRepository @Inject constructor(
    private val api: WaqiApi,
    private val gateway: OnlineGateway,
    private val preferences: OnlineUtilityPreferences? = null
) {
    companion object {
        const val SOURCE_ID = "waqi"
        private const val CACHE_TTL_MINUTES = 30L
    }

    suspend fun getFeedByGeo(
        latitude: Double,
        longitude: Double
    ): OnlineResult<WaqiData> {
        val customToken = preferences?.getAqicnToken()?.firstOrNull() ?: ""
        val token = customToken.ifBlank { BuildConfig.AQICN_TOKEN }
        if (token.isBlank()) {
            return OnlineResult.Error.InvalidApiKey
        }

        return gateway.execute(
            sourceId = SOURCE_ID,
            cacheKey = "waqi_geo_${latitude}_${longitude}",
            ttl = Duration.ofMinutes(CACHE_TTL_MINUTES)
        ) {
            val response = api.getFeedByGeo(lat = latitude, lon = longitude, token = token)
            response.data ?: throw IllegalStateException("Không có dữ liệu trạm quan trắc từ AQICN")
        }
    }

    suspend fun getFeedByCity(
        city: String
    ): OnlineResult<WaqiData> {
        val customToken = preferences?.getAqicnToken()?.firstOrNull() ?: ""
        val token = customToken.ifBlank { BuildConfig.AQICN_TOKEN }
        if (token.isBlank()) {
            return OnlineResult.Error.InvalidApiKey
        }

        return gateway.execute(
            sourceId = SOURCE_ID,
            cacheKey = "waqi_city_$city",
            ttl = Duration.ofMinutes(CACHE_TTL_MINUTES)
        ) {
            val response = api.getFeedByCity(city = city, token = token)
            response.data ?: throw IllegalStateException("Không có dữ liệu trạm quan trắc cho $city")
        }
    }
}
