package com.example.ipotracker.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExchangeConfigRemoteDto(
    @Json(name = "exchangeKey") val exchangeKey: String = "",
    @Json(name = "sourceUrl") val sourceUrl: String = "",
    @Json(name = "defaultUrl") val defaultUrl: String = "",
    @Json(name = "isActive") val isActive: Boolean = true,
    @Json(name = "lastSyncedAt") val lastSyncedAt: String? = null,
    @Json(name = "lastStatus") val lastStatus: String? = "ACTIVE",
    @Json(name = "updatedAt") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateExchangeUrlRequestDto(
    @Json(name = "exchangeKey") val exchangeKey: String = "",
    @Json(name = "sourceUrl") val sourceUrl: String = ""
)
