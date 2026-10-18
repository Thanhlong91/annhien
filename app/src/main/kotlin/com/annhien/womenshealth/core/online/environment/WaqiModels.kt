package com.annhien.womenshealth.core.online.environment

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull

@Serializable
data class WaqiFeedResponse(
    val status: String,
    val data: WaqiData? = null
)

@Serializable
data class WaqiData(
    val aqi: JsonElement? = null,
    val idx: Int? = null,
    val city: WaqiCity? = null,
    val dominentpol: String? = null,
    val iaqi: WaqiIaqi? = null,
    val time: WaqiTime? = null
) {
    fun getAqiValue(): Int? {
        val prim = aqi as? JsonPrimitive ?: return null
        return prim.intOrNull
    }
}

@Serializable
data class WaqiCity(
    val name: String? = null,
    val url: String? = null,
    val geo: List<Double>? = null
)

@Serializable
data class WaqiIaqi(
    val pm25: WaqiParamValue? = null,
    val pm10: WaqiParamValue? = null,
    val o3: WaqiParamValue? = null,
    val no2: WaqiParamValue? = null,
    val so2: WaqiParamValue? = null,
    val co: WaqiParamValue? = null,
    val t: WaqiParamValue? = null,
    val h: WaqiParamValue? = null,
    val p: WaqiParamValue? = null,
    val w: WaqiParamValue? = null
)

@Serializable
data class WaqiParamValue(
    val v: Double? = null
)

@Serializable
data class WaqiTime(
    val s: String? = null,
    val tz: String? = null,
    val iso: String? = null
)
