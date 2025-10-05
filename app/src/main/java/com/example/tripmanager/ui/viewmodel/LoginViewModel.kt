package com.example.tripmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripmanager.data.model.User
import com.example.tripmanager.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun cambiarUsuario(usuario: String) {
        _uiState.value = _uiState.value.copy(username = usuario)
    }

    fun cambiarClave(clave: String) {
        _uiState.value = _uiState.value.copy(password = clave)
    }

    fun login() {
        val currentState = _uiState.value
        if (currentState.username.isBlank() || currentState.password.isBlank()) {
            _uiState.value = currentState.copy(mensajeError = "Faltan datos por completar")
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(cargando = true, mensajeError = null)
            try {
                val user = userRepository.login(currentState.username, currentState.password)
                if (user != null) {
                    _uiState.value = currentState.copy(
                        cargando = false,
                        loginExitoso = true,
                        mensajeError = null
                    )
                } else {
                    _uiState.value = currentState.copy(
                        cargando = false,
                        mensajeError = "Credenciales incorrectas"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    cargando = false,
                    mensajeError = "Error al ingresar: ${e.message}"
                )
            }
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(mensajeError = null)
    }
}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val cargando: Boolean = false,
    val loginExitoso: Boolean = false,
    val mensajeError: String? = null
)
