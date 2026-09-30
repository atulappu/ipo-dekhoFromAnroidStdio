package com.example.ipotracker.data.local.dao

import androidx.room.*
import com.example.ipotracker.data.local.entity.ExchangeConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeConfigDao {
    @Query("SELECT * FROM exchange_configs ORDER BY exchangeKey ASC")
    fun getAllConfigs(): Flow<List<ExchangeConfigEntity>>

    @Query("SELECT * FROM exchange_configs WHERE exchangeKey = :key LIMIT 1")
    suspend fun getConfigByKey(key: String): ExchangeConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: ExchangeConfigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(configs: List<ExchangeConfigEntity>)

    @Query("UPDATE exchange_configs SET sourceUrl = :url, updatedAt = :updatedAt WHERE exchangeKey = :key")
    suspend fun updateUrl(key: String, url: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE exchange_configs SET lastSyncedAt = :syncedAt, lastStatus = :status WHERE exchangeKey = :key")
    suspend fun updateSyncStatus(key: String, syncedAt: Long, status: String)
}
