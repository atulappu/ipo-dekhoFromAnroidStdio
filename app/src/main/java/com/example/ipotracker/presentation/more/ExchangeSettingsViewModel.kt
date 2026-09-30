package com.example.ipotracker.presentation.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ipotracker.data.local.entity.ExchangeConfigEntity
import com.example.ipotracker.domain.repository.IpoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExchangeSettingsUiState(
    val configs: List<ExchangeConfigEntity> = emptyList(),
    val isSyncing: Boolean = false,
    val statusMessage: String? = null
)

class ExchangeSettingsViewModel(
    private val ipoRepository: IpoRepository
) : ViewModel() {

    val configs: StateFlow<List<ExchangeConfigEntity>> = ipoRepository.getExchangeConfigs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun updateUrl(exchangeKey: String, newUrl: String) {
        viewModelScope.launch {
            _isSyncing.value = true
            _message.value = "Updating $exchangeKey URL and syncing database..."
            val result = ipoRepository.updateExchangeUrl(exchangeKey, newUrl)
            _isSyncing.value = false
            if (result.isSuccess) {
                _message.value = "Successfully updated $exchangeKey endpoint and synced into database!"
            } else {
                _message.value = "Failed to update $exchangeKey URL: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            _isSyncing.value = true
            _message.value = "Restoring verified default exchange URLs..."
            val result = ipoRepository.resetExchangeUrls()
            _isSyncing.value = false
            if (result.isSuccess) {
                _message.value = "Restored official exchange URLs and synced database!"
            } else {
                _message.value = "Failed to restore defaults."
            }
        }
    }

    fun triggerExchangeSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            _message.value = "Ingesting live schedules from NSE & BSE into database..."
            val result = ipoRepository.syncFromExchanges()
            _isSyncing.value = false
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                _message.value = "Sync complete! Persisted $count records in Room Database."
            } else {
                _message.value = "Sync notice: Cached records active in Room database."
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    companion object {
        fun provideFactory(ipoRepository: IpoRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ExchangeSettingsViewModel(ipoRepository) as T
                }
            }
    }
}
