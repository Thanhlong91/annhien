package com.annhien.womenshealth.core.online.environment

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenAqApi {

    @GET("v3/locations")
    suspend fun getLocations(
        @Query("coordinates") coordinates: String,
        @Query("radius") radius: Int = 10_000,
        @Query("limit") limit: Int = 10
    ): OpenAqResultsResponse
}
