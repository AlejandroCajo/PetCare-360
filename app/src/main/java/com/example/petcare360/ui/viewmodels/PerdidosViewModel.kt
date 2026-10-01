package com.example.petcare360.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare360.data.model.SosAlertEntity
import com.example.petcare360.data.remote.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerdidosUiState(
    val alerts: List<SosAlertEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val currentUserId: String? = null
)

class PerdidosViewModel(
    private val supabaseClient: SupabaseClient?
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerdidosUiState())
    val uiState: StateFlow<PerdidosUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(currentUserId = supabaseClient?.getCurrentUserId()) }
        loadAlerts()
    }

    fun loadAlerts() {
        if (supabaseClient == null) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = supabaseClient.getSosAlerts()
            result.onSuccess { alerts ->
                _uiState.update { it.copy(alerts = alerts, isLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(errorMessage = "Error al cargar alertas: ${e.message}", isLoading = false) }
            }
        }
    }

    fun markAsFound(alertId: String) {
        if (supabaseClient == null) return
        viewModelScope.launch {
            val result = supabaseClient.updateSosAlertStatus(alertId, "resolved")
            if (result.isSuccess) {
                _uiState.update { state ->
                    val updatedList = state.alerts.map {
                        if (it.id == alertId) it.copy(status = "resolved") else it
                    }
                    state.copy(alerts = updatedList)
                }
            }
        }
    }
}
