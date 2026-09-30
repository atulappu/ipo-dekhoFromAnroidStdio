package com.example.ipotracker.presentation.allotment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ipotracker.data.model.IpoItem
import com.example.ipotracker.domain.repository.IpoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AllotmentUiState(
    val ipos: List<IpoItem> = emptyList(),
    val selectedIpo: IpoItem? = null,
    val panNumber: String = "",
    val applicationNumber: String = "",
    val registrarName: String = "",
    val registrarUrl: String = "https://in.mpms.mufg.com/Initial_Offer/public-issues.html",
    val isAllotmentOut: Boolean = false,
    val infoMessage: String? = null,
    val registrars: List<com.example.ipotracker.data.model.RegistrarItem> = emptyList(),
    val registrarSearchQuery: String = "",
    val editingRegistrar: com.example.ipotracker.data.model.RegistrarItem? = null,
    val isEditUrlDialogOpen: Boolean = false,
    val isAddRegistrarDialogOpen: Boolean = false
)

class AllotmentViewModel(
    private val repository: IpoRepository,
    initialIpoId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AllotmentUiState())
    val uiState: StateFlow<AllotmentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllIpos().collect { list ->
                val ipo = if (initialIpoId != null) {
                    list.find { it.id == initialIpoId }
                } else {
                    list.find { it.allotmentInfo?.isAvailable == true } ?: list.firstOrNull()
                }

                _uiState.update { current ->
                    current.copy(
                        ipos = list,
                        selectedIpo = ipo,
                        registrarName = ipo?.allotmentInfo?.registrarName ?: ipo?.registrar ?: "Official Registrar",
                        registrarUrl = ipo?.getEffectiveRegistrarUrl() ?: "https://in.mpms.mufg.com/Initial_Offer/public-issues.html",
                        isAllotmentOut = ipo?.allotmentInfo?.isAvailable == true
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.getRegistrars().collect { regList ->
                _uiState.update { it.copy(registrars = regList) }
            }
        }
    }

    fun selectIpo(ipo: IpoItem) {
        _uiState.update {
            it.copy(
                selectedIpo = ipo,
                registrarName = ipo.allotmentInfo?.registrarName ?: ipo.registrar,
                registrarUrl = ipo.getEffectiveRegistrarUrl(),
                isAllotmentOut = ipo.allotmentInfo?.isAvailable == true
            )
        }
    }

    fun onRegistrarSearchChange(query: String) {
        _uiState.update { it.copy(registrarSearchQuery = query) }
    }

    fun openEditUrlDialog(registrar: com.example.ipotracker.data.model.RegistrarItem) {
        _uiState.update { it.copy(editingRegistrar = registrar, isEditUrlDialogOpen = true) }
    }

    fun closeEditUrlDialog() {
        _uiState.update { it.copy(editingRegistrar = null, isEditUrlDialogOpen = false) }
    }

    fun openAddRegistrarDialog() {
        _uiState.update { it.copy(isAddRegistrarDialogOpen = true) }
    }

    fun closeAddRegistrarDialog() {
        _uiState.update { it.copy(isAddRegistrarDialogOpen = false) }
    }

    fun saveRegistrarUrl(id: String, newUrl: String, comments: String? = null) {
        viewModelScope.launch {
            repository.updateRegistrarUrl(id, newUrl, comments, "Admin")
            closeEditUrlDialog()
        }
    }

    fun saveFullRegistrar(
        id: String,
        name: String,
        url: String,
        issuesManaged: Int,
        issueAmountCr: Double,
        comment: String,
        modifiedBy: String = "Admin"
    ) {
        viewModelScope.launch {
            val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH).format(java.util.Date())
            val existing = _uiState.value.registrars.find { it.id == id }
            val item = com.example.ipotracker.data.model.RegistrarItem(
                id = id,
                name = name.trim(),
                url = url.trim(),
                issuesManaged = issuesManaged,
                issueAmountCr = issueAmountCr,
                comments = comment.trim(),
                createdDate = existing?.createdDate ?: todayDate,
                modifiedDate = todayDate,
                modifiedBy = modifiedBy.ifBlank { "Admin" }
            )
            repository.saveRegistrar(item)
            closeEditUrlDialog()
        }
    }

    fun addNewRegistrar(
        name: String,
        url: String,
        issuesManaged: Int,
        issueAmountCr: Double,
        comment: String
    ) {
        viewModelScope.launch {
            val cleanId = name.lowercase().replace(" ", "-").replace(".", "").take(30) + "-${System.currentTimeMillis() % 1000}"
            val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH).format(java.util.Date())
            val item = com.example.ipotracker.data.model.RegistrarItem(
                id = cleanId,
                name = name.trim(),
                url = url.trim(),
                issuesManaged = issuesManaged,
                issueAmountCr = issueAmountCr,
                comments = comment.trim(),
                createdDate = todayDate,
                modifiedDate = todayDate,
                modifiedBy = "Admin"
            )
            repository.saveRegistrar(item)
            closeAddRegistrarDialog()
        }
    }

    fun updatePan(pan: String) {
        _uiState.update { it.copy(panNumber = pan.uppercase().take(10)) }
    }

    fun updateApplicationNumber(appNum: String) {
        _uiState.update { it.copy(applicationNumber = appNum) }
    }

    companion object {
        fun provideFactory(repository: IpoRepository, initialIpoId: String? = null): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AllotmentViewModel(repository, initialIpoId) as T
                }
            }
    }
}
