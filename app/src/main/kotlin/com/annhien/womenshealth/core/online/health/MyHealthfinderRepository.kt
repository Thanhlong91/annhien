package com.annhien.womenshealth.core.online.health

import com.annhien.womenshealth.core.connectivity.OnlineGateway
import com.annhien.womenshealth.core.connectivity.OnlineResult
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyHealthfinderRepository @Inject constructor(
    private val api: MyHealthfinderApi,
    private val gateway: OnlineGateway
) {

    companion object {
        const val SOURCE_ID = "myhealthfinder"
        private const val CACHE_TTL_HOURS = 72L
    }

    suspend fun getHealthTopics(topicId: String? = null): OnlineResult<List<MyHealthfinderTopic>> {
        return gateway.execute(
            sourceId = SOURCE_ID,
            cacheKey = "myhealthfinder_topics_${topicId ?: "all"}",
            ttl = Duration.ofHours(CACHE_TTL_HOURS)
        ) {
            val result = api.getTopics(topicId = topicId).Result
            val items = result?.Items?.Item
            val resources = result?.Resources?.Resource
            (items ?: resources).orEmpty()
        }
    }
}
