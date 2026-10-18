package com.annhien.womenshealth.core.online.environment

import kotlinx.serialization.Serializable

@Serializable
data class OpenAqLocation(
    val id: Int? = null,
    val name: String? = null,
    val locality: String? = null,
    val timezone: String? = null,
    val country: OpenAqCountry? = null,
    val coordinates: OpenAqCoordinates? = null,
    val datetimeLast: OpenAqDateTime? = null,
    val sensors: List<OpenAqSensor> = emptyList()
) {
    val formattedLastUpdated: String?
        get() = datetimeLast?.local ?: datetimeLast?.utc
}

@Serializable
data class OpenAqDateTime(
    val utc: String? = null,
    val local: String? = null
)

@Serializable
data class OpenAqCountry(
    val id: Int? = null,
    val code: String? = null,
    val name: String? = null
)

@Serializable
data class OpenAqCoordinates(
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class OpenAqSensor(
    val id: Int? = null,
    val name: String? = null,
    val parameter: OpenAqParameter? = null
)

@Serializable
data class OpenAqParameter(
    val id: Int? = null,
    val name: String? = null,
    val units: String? = null,
    val displayName: String? = null
)

@Serializable
data class OpenAqResultsResponse(
    val results: List<OpenAqLocation> = emptyList()
)
