package com.example.ipotracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ipotracker.data.model.IpoItem
import com.example.ipotracker.data.model.MarketIndex
import com.example.ipotracker.data.remote.DataSourceType
import com.example.ipotracker.data.remote.IpoConfig
import com.example.ipotracker.domain.repository.IpoRepository
import com.example.ipotracker.utils.DateUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val marketIndices: List<MarketIndex> = emptyList(),
    val openIpos: List<IpoItem> = emptyList(),
    val upcomingIpos: List<IpoItem> = emptyList(),
    val closedIpos: List<IpoItem> = emptyList(),
    val listedIpos: List<IpoItem> = emptyList(),
    val lastUpdated: String = DateUtils.formatLastUpdated(),
    val error: String? = null,
    val isApiConnected: Boolean = true,
    val isApiMode: Boolean = IpoConfig.DATA_SOURCE == DataSourceType.API,
    val apiEndpoint: String = IpoConfig.API_BASE_URL
)

data class HomeNavigationContext(
    val selectedTab: Int = 0, // 0 = OPEN, 1 = UPCOMING, 2 = CLOSED
    val filterMainboard: Boolean = true,
    val filterSme: Boolean = true,
    val sortOption: com.example.ipotracker.presentation.ipo.IpoSortOption = com.example.ipotracker.presentation.ipo.IpoSortOption.DEFAULT,
    val searchQuery: String = "",
    val selectedExchange: String = "ALL",
    val lastSelectedIpoId: String? = null,
    val scrollIndex: Int = 0,
    val scrollOffset: Int = 0,
    val page: Int = 1
)

class HomeViewModel(private val repository: IpoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            isLoading = true,
            isApiMode = IpoConfig.DATA_SOURCE == DataSourceType.API,
            apiEndpoint = IpoConfig.API_BASE_URL
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var periodicJob: Job? = null

    init {
        loadData()
        startPeriodicRefresh()
    }

    fun startPeriodicRefresh(intervalMs: Long = 30_000L) {
        periodicJob?.cancel()
        periodicJob = viewModelScope.launch {
            while (isActive) {
                delay(intervalMs)
                try {
                    repository.refreshData()
                } catch (_: Exception) {
                    // Retain existing state on transient background failure
                }
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = it.openIpos.isEmpty() && it.upcomingIpos.isEmpty() && it.closedIpos.isEmpty(), 
                    error = null
                ) 
            }
            combine(
                repository.getMarketSummary(),
                repository.getOpenIpos(),
                repository.getUpcomingIpos(),
                repository.getClosedIpos(),
                repository.getListedIpos()
            ) { indices, open, upcoming, closed, listed ->
                HomeUiState(
                    isLoading = false,
                    marketIndices = indices,
                    openIpos = open,
                    upcomingIpos = upcoming,
                    closedIpos = closed,
                    listedIpos = listed,
                    lastUpdated = DateUtils.formatLastUpdated(),
                    isApiMode = IpoConfig.DATA_SOURCE == DataSourceType.API,
                    apiEndpoint = IpoConfig.API_BASE_URL,
                    isApiConnected = true,
                    error = null
                )
            }.catch { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isApiConnected = false,
                        error = e.localizedMessage ?: "Failed to load data"
                    )
                }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = repository.refreshData()
            res.onSuccess {
                _uiState.update { it.copy(isLoading = false, isApiConnected = true, error = null) }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isApiConnected = false,
                        error = err.localizedMessage ?: "Could not connect to ${IpoConfig.API_BASE_URL}"
                    )
                }
            }
        }
    }

    fun toggleWatchlist(ipoId: String) {
        viewModelScope.launch {
            repository.toggleWatchlist(ipoId)
        }
    }

    // Preserved navigation and list context (Issue 2)
    private val _navContext = MutableStateFlow(HomeNavigationContext())
    val navContext: StateFlow<HomeNavigationContext> = _navContext.asStateFlow()

    fun updateSelectedTab(tab: Int) {
        _navContext.update { it.copy(selectedTab = tab) }
    }

    fun updateFilters(mainboard: Boolean, sme: Boolean) {
        _navContext.update { it.copy(filterMainboard = mainboard, filterSme = sme) }
    }

    fun updateSortOption(sort: com.example.ipotracker.presentation.ipo.IpoSortOption) {
        _navContext.update { it.copy(sortOption = sort) }
    }

    fun updateSearchQuery(query: String) {
        _navContext.update { it.copy(searchQuery = query) }
    }

    fun updateExchangeFilter(exchange: String) {
        _navContext.update { it.copy(selectedExchange = exchange) }
    }

    fun saveNavigationState(
        ipoId: String,
        scrollIndex: Int,
        scrollOffset: Int,
        page: Int = 1
    ) {
        _navContext.update {
            it.copy(
                lastSelectedIpoId = ipoId,
                scrollIndex = scrollIndex,
                scrollOffset = scrollOffset,
                page = page
            )
        }
    }

    fun clearLastSelectedIpo() {
        _navContext.update { it.copy(lastSelectedIpoId = null) }
    }

    companion object {
        fun provideFactory(repository: IpoRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(repository) as T
                }
            }
    }
}
