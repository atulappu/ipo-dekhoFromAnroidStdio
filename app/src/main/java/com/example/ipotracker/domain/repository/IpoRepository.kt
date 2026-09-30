package com.example.ipotracker.domain.repository

import com.example.ipotracker.data.model.IpoItem
import com.example.ipotracker.data.model.MarketIndex
import kotlinx.coroutines.flow.Flow

interface IpoRepository {
    fun getMarketSummary(): Flow<List<MarketIndex>>
    fun getAllIpos(): Flow<List<IpoItem>>
    fun getOpenIpos(): Flow<List<IpoItem>>
    fun getUpcomingIpos(): Flow<List<IpoItem>>
    fun getClosedIpos(): Flow<List<IpoItem>>
    fun getListedIpos(): Flow<List<IpoItem>>
    fun getIpoById(id: String): Flow<IpoItem?>
    fun searchIpos(query: String): Flow<List<IpoItem>>
    fun getWatchlist(): Flow<List<IpoItem>>
    suspend fun toggleWatchlist(ipoId: String)
    fun isWatchlisted(ipoId: String): Flow<Boolean>
    fun getRecentSearches(): Flow<List<String>>
    suspend fun saveSearchQuery(query: String)
    suspend fun deleteSearchQuery(query: String)
    suspend fun clearSearchHistory()
    suspend fun refreshData(): Result<Unit>
    fun getRegistrars(): Flow<List<com.example.ipotracker.data.model.RegistrarItem>>
    suspend fun updateRegistrarUrl(id: String, newUrl: String, comments: String? = null, modifiedBy: String = "Admin"): Result<Unit>
    suspend fun saveRegistrar(item: com.example.ipotracker.data.model.RegistrarItem): Result<Unit>
    fun getExchangeConfigs(): Flow<List<com.example.ipotracker.data.local.entity.ExchangeConfigEntity>>
    suspend fun updateExchangeUrl(key: String, newUrl: String): Result<Unit>
    suspend fun resetExchangeUrls(): Result<Unit>
    suspend fun syncFromExchanges(): Result<Int>
}
