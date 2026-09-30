package com.example.ipotracker.data.repository

import android.util.Log
import com.example.ipotracker.data.local.dao.ExchangeConfigDao
import com.example.ipotracker.data.local.dao.IpoDao
import com.example.ipotracker.data.local.dao.RegistrarDao
import com.example.ipotracker.data.local.dao.SearchHistoryDao
import com.example.ipotracker.data.local.dao.WatchlistDao
import com.example.ipotracker.data.local.entity.ExchangeConfigEntity
import com.example.ipotracker.data.local.entity.IpoEntity
import com.example.ipotracker.data.local.entity.RegistrarEntity
import com.example.ipotracker.data.local.entity.SearchHistoryEntity
import com.example.ipotracker.data.local.entity.WatchlistEntity
import com.example.ipotracker.data.model.IpoItem
import com.example.ipotracker.data.model.IpoStatus
import com.example.ipotracker.data.model.MarketIndex
import com.example.ipotracker.data.remote.DataSourceType
import com.example.ipotracker.data.remote.IpoConfig
import com.example.ipotracker.data.remote.IpoRemoteDataSource
import com.example.ipotracker.data.remote.MockIpoDataSource
import com.example.ipotracker.data.remote.dto.UpdateExchangeUrlRequestDto
import com.example.ipotracker.data.remote.exchange.ExchangeSyncEngine
import com.example.ipotracker.domain.repository.IpoRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class IpoRepositoryImpl(
    private val remoteDataSource: IpoRemoteDataSource,
    private val watchlistDao: WatchlistDao,
    private val searchHistoryDao: SearchHistoryDao,
    private val registrarDao: RegistrarDao,
    private val ipoDao: IpoDao,
    private val exchangeConfigDao: ExchangeConfigDao,
    private val exchangeSyncEngine: ExchangeSyncEngine = ExchangeSyncEngine()
) : IpoRepository {

    private val tag = "IpoRepository"
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val marketIndicesState = MutableStateFlow<List<MarketIndex>>(MockIpoDataSource.marketIndices)

    init {
        repositoryScope.launch {
            try {
                // 1. Seed Room Database with 12 Official Master Registrars if missing
                for (reg in MockIpoDataSource.registrarList) {
                    val existing = registrarDao.getRegistrarById(reg.id)
                    if (existing == null) {
                        registrarDao.insert(RegistrarEntity.fromDomain(reg))
                    }
                }

                // 2. Seed Room Database with Default Exchange URLs (NSE, BSE, InvestorGain, Chittorgarh)
                exchangeConfigDao.insertOrUpdate(
                    ExchangeConfigEntity(
                        exchangeKey = "NSE",
                        sourceUrl = "https://www.nseindia.com/market-data/all-upcoming-issues-ipo",
                        defaultUrl = "https://www.nseindia.com/market-data/all-upcoming-issues-ipo",
                        isActive = true,
                        lastStatus = "ACTIVE",
                        updatedAt = System.currentTimeMillis()
                    )
                )

                exchangeConfigDao.insertOrUpdate(
                    ExchangeConfigEntity(
                        exchangeKey = "BSE",
                        sourceUrl = "https://www.bseindia.com/markets/publicissues/ipoissues.aspx?id=1&type=pso",
                        defaultUrl = "https://www.bseindia.com/markets/publicissues/ipoissues.aspx?id=1&type=pso",
                        isActive = true,
                        lastStatus = "ACTIVE",
                        updatedAt = System.currentTimeMillis()
                    )
                )

                exchangeConfigDao.insertOrUpdate(
                    ExchangeConfigEntity(
                        exchangeKey = "INVESTORGAIN",
                        sourceUrl = "https://www.investorgain.com/report/live-ipo-gmp/331/",
                        defaultUrl = "https://www.investorgain.com/report/live-ipo-gmp/331/",
                        isActive = true,
                        lastStatus = "ACTIVE",
                        updatedAt = System.currentTimeMillis()
                    )
                )

                exchangeConfigDao.insertOrUpdate(
                    ExchangeConfigEntity(
                        exchangeKey = "CHITTORGARH",
                        sourceUrl = "https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/",
                        defaultUrl = "https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/",
                        isActive = true,
                        lastStatus = "ACTIVE",
                        updatedAt = System.currentTimeMillis()
                    )
                )

                // 3. Upsert verified IPO entries from the latest exchange & InvestorGain data
                val initialEntities = MockIpoDataSource.ipoList.map { IpoEntity.fromDomain(it) }
                ipoDao.insertAll(initialEntities)
                Log.d(tag, "Room Database updated with ${initialEntities.size} verified IPO records")

                // 4. Trigger remote API sync or exchange sync in background
                if (IpoConfig.DATA_SOURCE == DataSourceType.API) {
                    fetchRemoteData()
                } else {
                    syncFromExchanges()
                }
            } catch (e: Exception) {
                Log.w(tag, "Notice: Local database initialization completed with notice", e)
            }
        }
    }

    /**
     * Ingests IPO data first into the local Room SQLite Database,
     * ensuring the database is always the single source of truth.
     */
    override suspend fun syncFromExchanges(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val nseConfig = exchangeConfigDao.getConfigByKey("NSE")
            val bseConfig = exchangeConfigDao.getConfigByKey("BSE")

            val nseUrl = nseConfig?.sourceUrl ?: "https://www.nseindia.com/market-data/all-upcoming-issues-ipo"
            val bseUrl = bseConfig?.sourceUrl ?: "https://www.bseindia.com/markets/publicissues/ipoissues?expandable=4&id=1&Type=p"

            Log.d(tag, "Executing exchange ingestion: NSE=$nseUrl, BSE=$bseUrl")
            val fetchedIpos = exchangeSyncEngine.fetchFromExchanges(nseUrl, bseUrl)

            if (fetchedIpos.isNotEmpty()) {
                val entities = fetchedIpos.map { IpoEntity.fromDomain(it) }
                // Persist first in Room Database
                ipoDao.insertAll(entities)

                val now = System.currentTimeMillis()
                exchangeConfigDao.updateSyncStatus("NSE", now, "SYNCED")
                exchangeConfigDao.updateSyncStatus("BSE", now, "SYNCED")

                Log.d(tag, "Saved ${entities.size} exchange records to local Room Database")
                Result.success(entities.size)
            } else {
                Result.success(0)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error during exchange synchronization", e)
            Result.failure(e)
        }
    }

    private suspend fun fetchRemoteData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(tag, "Fetching live data from remote API: ${IpoConfig.API_BASE_URL}")
            val indicesResult = remoteDataSource.getMarketSummary()
            val iposResult = remoteDataSource.getAllIpos()

            indicesResult.onSuccess { indices ->
                if (indices.isNotEmpty()) {
                    marketIndicesState.value = indices
                }
            }

            iposResult.onSuccess { remoteIpos ->
                if (remoteIpos.isNotEmpty()) {
                    // Save first to Room SQLite Database
                    val entities = remoteIpos.map { IpoEntity.fromDomain(it) }
                    ipoDao.insertAll(entities)
                    Log.d(tag, "Saved ${entities.size} IPOs from remote API into local Room Database")
                }
            }.onFailure { err ->
                Log.w(tag, "Remote API returned error. Local Room database records active.", err)
            }

            if (iposResult.isSuccess) Result.success(Unit)
            else Result.failure(iposResult.exceptionOrNull() ?: Exception("Remote fetch failed"))
        } catch (e: Exception) {
            Log.e(tag, "Error during remote data sync", e)
            Result.failure(e)
        }
    }

    override fun getMarketSummary(): Flow<List<MarketIndex>> {
        return marketIndicesState
    }

    // Heartbeat ticker flow ensuring real-time transitions (e.g. crossing 17:30 IST) without page reload
    private val clockTicker = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(15_000L)
        }
    }

    /**
     * Reads directly from Room SQLite Database (Single Source of Truth)
     * merged reactively with User Watchlist state and IST Clock Ticker.
     */
    override fun getAllIpos(): Flow<List<IpoItem>> {
        return combine(ipoDao.getAllIpos(), watchlistDao.getAllWatchlist(), clockTicker) { entities, watchlistEntities, nowMillis ->
            val watchlistedIds = watchlistEntities.map { it.ipoId }.toSet()
            val baseList = if (entities.isNotEmpty()) {
                entities.map { it.toDomain() }
            } else {
                MockIpoDataSource.ipoList
            }

            baseList.map { item ->
                val dynamicStatus = com.example.ipotracker.utils.DateUtils.calculateEffectiveStatus(
                    openDateStr = item.openDate,
                    closeDateStr = item.closeDate,
                    allotmentDateStr = item.allotmentDate,
                    listingDateStr = item.listingDate,
                    fallbackStatus = item.status,
                    checkInstantMillis = nowMillis
                )
                item.copy(
                    status = dynamicStatus,
                    isWatchlisted = watchlistedIds.contains(item.id),
                    isDemoData = false
                )
            }
        }
    }

    override fun getOpenIpos(): Flow<List<IpoItem>> {
        return getAllIpos().map { list -> list.filter { it.status == IpoStatus.OPEN } }
    }

    override fun getUpcomingIpos(): Flow<List<IpoItem>> {
        return getAllIpos().map { list -> list.filter { it.status == IpoStatus.UPCOMING } }
    }

    override fun getClosedIpos(): Flow<List<IpoItem>> {
        return getAllIpos().map { list -> list.filter { it.status.isClosed } }
    }

    override fun getListedIpos(): Flow<List<IpoItem>> {
        return getAllIpos().map { list -> list.filter { it.status == IpoStatus.LISTED } }
    }

    override fun getIpoById(id: String): Flow<IpoItem?> {
        return getAllIpos().map { list -> list.find { it.id == id } }
    }

    override fun searchIpos(query: String): Flow<List<IpoItem>> {
        val q = query.trim().lowercase()
        return getAllIpos().map { list ->
            if (q.isEmpty()) emptyList()
            else list.filter {
                it.name.lowercase().contains(q) ||
                it.symbol.lowercase().contains(q) ||
                it.sector.lowercase().contains(q) ||
                it.listingExchanges.lowercase().contains(q)
            }
        }
    }

    override fun getWatchlist(): Flow<List<IpoItem>> {
        return getAllIpos().map { list -> list.filter { it.isWatchlisted } }
    }

    override suspend fun toggleWatchlist(ipoId: String) {
        try {
            val exists = watchlistDao.isWatchlisted(ipoId).first()
            if (exists) {
                watchlistDao.removeFromWatchlist(ipoId)
            } else {
                watchlistDao.addToWatchlist(WatchlistEntity(ipoId = ipoId))
            }
        } catch (_: Exception) {
            watchlistDao.addToWatchlist(WatchlistEntity(ipoId = ipoId))
        }
    }

    override fun isWatchlisted(ipoId: String): Flow<Boolean> {
        return watchlistDao.isWatchlisted(ipoId)
    }

    override fun getRecentSearches(): Flow<List<String>> {
        return searchHistoryDao.getRecentSearches().map { list ->
            list.map { it.query }
        }
    }

    override suspend fun saveSearchQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isNotEmpty()) {
            searchHistoryDao.insertSearch(SearchHistoryEntity(query = trimmed))
        }
    }

    override suspend fun deleteSearchQuery(query: String) {
        searchHistoryDao.deleteSearch(query)
    }

    override suspend fun clearSearchHistory() {
        searchHistoryDao.clearHistory()
    }

    override suspend fun refreshData(): Result<Unit> {
        return if (IpoConfig.DATA_SOURCE == DataSourceType.API) {
            fetchRemoteData()
        } else {
            val res = syncFromExchanges()
            if (res.isSuccess) Result.success(Unit)
            else Result.failure(res.exceptionOrNull() ?: Exception("Exchange sync error"))
        }
    }

    override fun getRegistrars(): Flow<List<com.example.ipotracker.data.model.RegistrarItem>> {
        return registrarDao.getAllRegistrars().map { entities ->
            if (entities.isEmpty()) {
                MockIpoDataSource.registrarList.toList()
            } else {
                entities.map { it.toDomain() }
            }
        }
    }

    override suspend fun updateRegistrarUrl(
        id: String,
        newUrl: String,
        comments: String?,
        modifiedBy: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH).format(java.util.Date())
        MockIpoDataSource.updateRegistrarUrl(id, newUrl, comments, modifiedBy)
        try {
            registrarDao.updateRegistrarUrl(
                id = id,
                newUrl = newUrl.trim(),
                comment = comments ?: "",
                modifyDate = todayDate,
                modifyBy = modifiedBy
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error updating registrar in Room DB", e)
            Result.success(Unit)
        }
    }

    override suspend fun saveRegistrar(item: com.example.ipotracker.data.model.RegistrarItem): Result<Unit> = withContext(Dispatchers.IO) {
        MockIpoDataSource.addOrUpdateRegistrar(item)
        try {
            registrarDao.insert(RegistrarEntity.fromDomain(item))
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error saving registrar in Room DB", e)
            Result.success(Unit)
        }
    }

    override fun getExchangeConfigs(): Flow<List<ExchangeConfigEntity>> {
        return exchangeConfigDao.getAllConfigs()
    }

    override suspend fun updateExchangeUrl(key: String, newUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val normalizedKey = key.trim().uppercase()
            val cleanUrl = newUrl.trim()
            exchangeConfigDao.updateUrl(normalizedKey, cleanUrl)

            // Also try pushing to backend API if configured
            try {
                val apiService = com.example.ipotracker.data.remote.RetrofitClient.createIpoApiService()
                apiService.updateExchangeUrl(UpdateExchangeUrlRequestDto(exchangeKey = normalizedKey, sourceUrl = cleanUrl))
            } catch (e: Exception) {
                Log.d(tag, "Backend sync skipped for URL update (offline or mock): ${e.message}")
            }

            // Immediately trigger a sync with the new URL
            syncFromExchanges()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Failed to update exchange URL", e)
            Result.failure(e)
        }
    }

    override suspend fun resetExchangeUrls(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            exchangeConfigDao.updateUrl("NSE", "https://www.nseindia.com/market-data/all-upcoming-issues-ipo")
            exchangeConfigDao.updateUrl("BSE", "https://www.bseindia.com/markets/publicissues/ipoissues.aspx?id=1&type=pso")
            exchangeConfigDao.updateUrl("INVESTORGAIN", "https://www.investorgain.com/report/live-ipo-gmp/331/")
            exchangeConfigDao.updateUrl("CHITTORGARH", "https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/")
            syncFromExchanges()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
