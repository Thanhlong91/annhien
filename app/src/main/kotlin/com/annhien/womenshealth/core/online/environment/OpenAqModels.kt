package com.annhien.womenshealth.core.online.environment

import kotlinx.serialization.Serializable

@Serializable
data class OpenAqLocation(
    val id: String? = null,
    val name: String? = null,
    val country: OpenAqCountry? = null,
    val coordinates: OpenAqCoordinates? = null,
    val lastUpdated: String? = null
)

@Serializable
data class OpenAqCountry(
    val code: String? = null,
    val name: String? = null
)

@Serializable
data class OpenAqCoordinates(
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class OpenAqResultsResponse(
    val results: List<OpenAqLocation> = emptyList()
)
