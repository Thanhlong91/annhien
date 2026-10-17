package com.annhien.womenshealth.core.online.health

import retrofit2.http.GET
import retrofit2.http.Query

interface MyHealthfinderApi {

    @GET("myhealthfinder/api/v4/itemlist.json")
    suspend fun getTopics(
        @Query("type") type: String = "topic",
        @Query("lang") language: String = "vi",
        @Query("topicId") topicId: String? = null
    ): MyHealthfinderResponse
}
