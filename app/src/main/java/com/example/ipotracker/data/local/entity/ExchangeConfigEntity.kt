package com.example.ipotracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_configs")
data class ExchangeConfigEntity(
    @PrimaryKey
    val exchangeKey: String, // "NSE" or "BSE"
    val sourceUrl: String,
    val defaultUrl: String,
    val isActive: Boolean = true,
    val lastSyncedAt: Long = 0L,
    val lastStatus: String = "ACTIVE",
    val updatedAt: Long = System.currentTimeMillis()
)
