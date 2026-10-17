package com.annhien.womenshealth.core.online.food

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenFoodFactsProduct(
    val code: String? = null,
    @SerialName("product_name") val productName: String? = null,
    @SerialName("product_name_vi") val productNameVi: String? = null,
    val brands: String? = null,
    val quantity: String? = null,
    @SerialName("image_front_small_url") val imageFrontSmallUrl: String? = null,
    val nutriments: OpenFoodFactsNutriments? = null
)

@Serializable
data class OpenFoodFactsNutriments(
    @SerialName("energy-kcal_100g") val energyKcalPer100g: Double? = null,
    @SerialName("proteins_100g") val proteinsPer100g: Double? = null,
    @SerialName("carbohydrates_100g") val carbohydratesPer100g: Double? = null,
    @SerialName("fat_100g") val fatPer100g: Double? = null,
    @SerialName("fiber_100g") val fiberPer100g: Double? = null,
    @SerialName("sugars_100g") val sugarsPer100g: Double? = null,
    @SerialName("sodium_100g") val sodiumPer100g: Double? = null
)

@Serializable
data class OpenFoodFactsSearchResponse(
    val count: Int? = null,
    val products: List<OpenFoodFactsProduct> = emptyList()
)
