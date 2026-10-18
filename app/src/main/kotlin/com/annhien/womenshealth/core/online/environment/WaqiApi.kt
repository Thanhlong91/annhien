package com.annhien.womenshealth.core.online.environment

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WaqiApi {

    @GET("feed/geo:{lat};{lon}/")
    suspend fun getFeedByGeo(
        @Path("lat") lat: Double,
        @Path("lon") lon: Double,
        @Query("token") token: String
    ): WaqiFeedResponse

    @GET("feed/{city}/")
    suspend fun getFeedByCity(
        @Path("city") city: String,
        @Query("token") token: String
    ): WaqiFeedResponse

    @GET("feed/here/")
    suspend fun getFeedHere(
        @Query("token") token: String
    ): WaqiFeedResponse
}
