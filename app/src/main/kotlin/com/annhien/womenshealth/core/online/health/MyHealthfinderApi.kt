package com.annhien.womenshealth.core.online.health

import retrofit2.http.GET
import retrofit2.http.Query

interface MyHealthfinderApi {

    @GET("api/myhealthfinder.json")
    suspend fun getTopics(
        @Query("lang") language: String = "vi",
        @Query("topicId") topicId: String? = null
    ): MyHealthfinderResponse
}
