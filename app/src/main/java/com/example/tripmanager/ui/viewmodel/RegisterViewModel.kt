package com.example.tripmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripmanager.data.model.User
import com.example.tripmanager.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun cambiarUsuario(usuario: String) {
        _uiState.value = _uiState.value.copy(username = usuario)
    }

    fun cambiarClave(clave: String) {
        _uiState.value = _uiState.value.copy(password = clave)
    }

    fun register() {
        val currentState = _uiState.value
        if (currentState.username.isBlank() || currentState.password.isBlank()) {
            _uiState.value = currentState.copy(mensajeError = "Faltan datos por completar")
            return
        }

        if (currentState.password.length < 6) {
            _uiState.value = currentState.copy(mensajeError = "La clave debe tener al menos 6 caracteres")
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(cargando = true, mensajeError = null)
            try {
                val userExists = userRepository.userExists(currentState.username)
                if (userExists) {
                    _uiState.value = currentState.copy(
                        cargando = false,
                        mensajeError = "Este usuario ya existe"
                    )
                    return@launch
                }

                val user = User(currentState.username, currentState.password)
                val success = userRepository.register(user)
                if (success) {
                    _uiState.value = currentState.copy(
                        cargando = false,
                        registroExitoso = true,
                        mensajeError = null
                    )
                } else {
                    _uiState.value = currentState.copy(
                        cargando = false,
                        mensajeError = "No se pudo crear la cuenta"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    cargando = false,
                    mensajeError = "Error al registrarse: ${e.message}"
                )
            }
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(mensajeError = null)
    }
}

data class RegisterUiState(
    val username: String = "",
    val password: String = "",
    val cargando: Boolean = false,
    val registroExitoso: Boolean = false,
    val mensajeError: String? = null
)
