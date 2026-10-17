package com.annhien.womenshealth.core.online.health

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyHealthfinderTopic(
    val id: String? = null,
    val title: String? = null,
    @SerialName("last_updated") val lastUpdated: String? = null,
    val url: String? = null,
    val summary: String? = null
)

@Serializable
data class MyHealthfinderResponse(
    val Result: MyHealthfinderResult? = null
)

@Serializable
data class MyHealthfinderResult(
    val Resources: MyHealthfinderResources? = null
)

@Serializable
data class MyHealthfinderResources(
    val Resource: List<MyHealthfinderTopic> = emptyList()
)
