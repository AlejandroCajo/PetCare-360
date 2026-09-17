package com.example.petcare360.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare360.data.remote.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPasswordVisible: Boolean = false,
    val termsAccepted: Boolean = false,
    val registerSuccess: Boolean = false
)

class RegisterViewModel(
    private val supabaseClient: SupabaseClient?
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun updateName(name: String) { _uiState.update { it.copy(name = name) } }
    fun updatePhone(phone: String) { _uiState.update { it.copy(phone = phone) } }
    fun updateEmail(email: String) { _uiState.update { it.copy(email = email) } }
    fun updatePassword(password: String) { _uiState.update { it.copy(password = password) } }

    fun togglePasswordVisibility() { _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) } }
    fun toggleTermsAccepted() { _uiState.update { it.copy(termsAccepted = !it.termsAccepted) } }

    fun register() {
        val state = _uiState.value
        if (state.name.isBlank() || state.phone.isBlank() || state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor completa todos los campos.") }
            return
        }

        if (!state.termsAccepted) {
            _uiState.update { it.copy(errorMessage = "Debes aceptar los términos y condiciones.") }
            return
        }

        if (supabaseClient == null) {
            _uiState.update { it.copy(errorMessage = "Supabase no está configurado.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = supabaseClient.signUp(state.email, state.password, state.name, state.phone)
            result.onSuccess {
                _uiState.update { s -> s.copy(isLoading = false, registerSuccess = true) }
            }.onFailure { error ->
                _uiState.update { s -> 
                    s.copy(isLoading = false, errorMessage = error.localizedMessage ?: "Error al registrarse")
                }
            }
        }
    }
}
