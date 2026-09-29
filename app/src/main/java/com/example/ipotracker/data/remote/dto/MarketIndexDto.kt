package com.example.ipotracker.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MarketIndexDto(
    @Json(name = "name") val name: String? = null,
    @Json(name = "value") val value: String? = null,
    @Json(name = "change") val change: String? = null,
    @Json(name = "percentChange") val percentChangeCamel: Double? = null,
    @Json(name = "percent_change") val percentChangeSnake: Double? = null,
    @Json(name = "isPositive") val isPositiveCamel: Boolean? = null,
    @Json(name = "is_positive") val isPositiveSnake: Boolean? = null
) {
    val percentChange: Double? get() = percentChangeCamel ?: percentChangeSnake
    val isPositive: Boolean? get() = isPositiveCamel ?: isPositiveSnake
}
