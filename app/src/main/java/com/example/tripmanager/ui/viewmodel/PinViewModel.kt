package com.example.tripmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripmanager.data.repository.UserRepository
import com.example.tripmanager.data.security.PinManager
import com.example.tripmanager.data.session.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PinViewModel(
    private val userRepository: UserRepository,
    private val pinManager: PinManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinUiState())
    val uiState: StateFlow<PinUiState> = _uiState.asStateFlow()

    companion object {
        private const val MAX_ATTEMPTS = 3
    }

    init {
        _uiState.value = _uiState.value.copy(
            hasPin = pinManager.hasPin(),
            isLocked = pinManager.isLocked(),
            isFirstTime = !pinManager.hasPin()
        )
    }

    fun onPinDigitEntered(digit: String) {
        val currentPin = _uiState.value.currentPin
        if (currentPin.length < 4) {
            val newPin = currentPin + digit
            _uiState.value = _uiState.value.copy(currentPin = newPin, errorMessage = null)
            if (newPin.length == 4) {
                if (_uiState.value.isFirstTime) {
                    if (_uiState.value.isConfirming) {
                        if (newPin == _uiState.value.firstPin) {
                            viewModelScope.launch {
                                pinManager.savePin(newPin)
                                _uiState.value = _uiState.value.copy(
                                    isPinSet = true,
                                    isFirstTime = false,
                                    isConfirming = false,
                                    currentPin = "",
                                    isPinVerified = true
                                )
                            }
                        } else {
                            _uiState.value = _uiState.value.copy(
                                errorMessage = "Los PINs no coinciden",
                                currentPin = "",
                                isConfirming = false
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            firstPin = newPin,
                            isConfirming = true,
                            currentPin = ""
                        )
                    }
                } else {
                    verifyPin(newPin)
                }
            }
        }
    }

    fun onBackspaceClick() {
        val currentPin = _uiState.value.currentPin
        if (currentPin.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(currentPin = currentPin.dropLast(1), errorMessage = null)
        }
    }

    private fun verifyPin(enteredPin: String) {
        viewModelScope.launch {
            if (pinManager.getPin() == enteredPin) {
                pinManager.resetAttempts()
                _uiState.value = _uiState.value.copy(isPinVerified = true, currentPin = "")
            } else {
                pinManager.recordAttempt()
                _uiState.value = _uiState.value.copy(
                    errorMessage = "PIN incorrecto. Intentos restantes: ${MAX_ATTEMPTS - pinManager.getAttempts()}",
                    currentPin = "",
                    isLocked = pinManager.isLocked()
                )
            }
        }
    }

    fun resetPinAttempts() {
        pinManager.resetAttempts()
        _uiState.value = _uiState.value.copy(isLocked = false, errorMessage = null, currentPin = "")
    }

    fun validateLoginForReset(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val user = userRepository.login(username, password)
                if (user != null && user.username == UserSession.getCurrentUser()) {
                    _uiState.value = _uiState.value.copy(
                        isLoginValidatedForReset = true,
                        isLoading = false,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Usuario o contraseña incorrectos",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al validar login: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun changePin(newPin: String) {
        viewModelScope.launch {
            pinManager.savePin(newPin)
            _uiState.value = _uiState.value.copy(
                isPinSet = true,
                isPinVerified = true, // Consider it verified after changing
                isLoginValidatedForReset = false,
                currentPin = "",
                errorMessage = null
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun updateCurrentPin(pin: String) {
        _uiState.value = _uiState.value.copy(currentPin = pin, errorMessage = null)
    }

    fun clearCurrentPin() {
        _uiState.value = _uiState.value.copy(currentPin = "")
    }

    fun showError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message, currentPin = "")
    }
}

data class PinUiState(
    val currentPin: String = "",
    val firstPin: String = "", // Used for confirmation during creation
    val hasPin: Boolean = false,
    val isPinSet: Boolean = false,
    val isPinVerified: Boolean = false,
    val isFirstTime: Boolean = false, // True if no PIN is set yet
    val isConfirming: Boolean = false, // True when confirming the new PIN
    val isLocked: Boolean = false,
    val isLoginValidatedForReset: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)