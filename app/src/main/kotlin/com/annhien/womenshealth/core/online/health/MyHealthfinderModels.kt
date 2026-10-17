package com.annhien.womenshealth.core.online.health

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyHealthfinderTopic(
    @SerialName("Id") val id: String? = null,
    @SerialName("Title") val title: String? = null,
    @SerialName("Type") val type: String? = null,
    @SerialName("ParentId") val parentId: String? = null,
    @SerialName("TranslationId") val translationId: String? = null,
    @SerialName("LastUpdate") val lastUpdated: String? = null,
    @SerialName("AccessibleVersion") val url: String? = null,
    @SerialName("Categories") val summary: String? = null
)

@Serializable
data class MyHealthfinderResponse(
    @SerialName("Result") val Result: MyHealthfinderResult? = null
)

@Serializable
data class MyHealthfinderResult(
    @SerialName("Error") val Error: String? = null,
    @SerialName("Total") val Total: Int? = null,
    @SerialName("Items") val Items: MyHealthfinderItems? = null,
    @SerialName("Resources") val Resources: MyHealthfinderResources? = null
)

@Serializable
data class MyHealthfinderItems(
    @SerialName("Item") val Item: List<MyHealthfinderTopic> = emptyList()
)

@Serializable
data class MyHealthfinderResources(
    @SerialName("Resource") val Resource: List<MyHealthfinderTopic> = emptyList()
)
