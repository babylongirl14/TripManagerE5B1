package com.example.tripmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripmanager.data.model.AlertType
import com.example.tripmanager.data.model.ItineraryItem
import com.example.tripmanager.data.repository.ItineraryRepository
import com.example.tripmanager.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ItineraryEditViewModel(
    private val itineraryRepository: ItineraryRepository,
    private val tripRepository: TripRepository,
    private val tripId: Long,
    private val itineraryId: Long?
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ItineraryEditUiState())
    val uiState: StateFlow<ItineraryEditUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    init {
        if (itineraryId != null) {
            loadItineraryItem()
        } else {
            // Set default values for new item
            _uiState.value = _uiState.value.copy(
                activityDateTime = System.currentTimeMillis(),
                activityDateTimeText = dateFormat.format(Date(System.currentTimeMillis())),
                hasReminder = false,
                reminderTimeBefore = "1 hora antes",
                alertType = AlertType.NORMAL
            )
        }
        loadTripInfo()
    }

    private fun loadItineraryItem() {
        viewModelScope.launch {
            try {
                val item = itineraryRepository.getItineraryById(itineraryId!!)
                if (item != null) {
                    _uiState.value = _uiState.value.copy(
                        activityDateTime = item.activityDateTime,
                        activityDateTimeText = dateFormat.format(Date(item.activityDateTime)),
                        description = item.description,
                        hasReminder = item.hasReminder,
                        reminderTimeBefore = item.reminderTimeBefore,
                        alertType = item.alertType
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al cargar la actividad: ${e.message}"
                )
            }
        }
    }

    private fun loadTripInfo() {
        viewModelScope.launch {
            try {
                val trip = tripRepository.getTripById(tripId)
                if (trip != null) {
                    _uiState.value = _uiState.value.copy(
                        tripStartDate = trip.startDate,
                        tripEndDate = trip.endDate
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al cargar información del viaje: ${e.message}"
                )
            }
        }
    }

    fun onActivityDateTimeChange(dateTime: Long) {
        _uiState.value = _uiState.value.copy(
            activityDateTime = dateTime,
            activityDateTimeText = dateFormat.format(Date(dateTime)),
            errorMessage = null // Clear error when date changes
        )
    }

    fun onDescriptionChange(description: String) {
        _uiState.value = _uiState.value.copy(
            description = description,
            errorMessage = null // Clear error when description changes
        )
    }

    fun onHasReminderChange(hasReminder: Boolean) {
        _uiState.value = _uiState.value.copy(hasReminder = hasReminder)
    }

    fun onReminderTimeChange(reminderTime: String) {
        _uiState.value = _uiState.value.copy(reminderTimeBefore = reminderTime)
    }

    fun onAlertTypeChange(alertType: AlertType) {
        _uiState.value = _uiState.value.copy(alertType = alertType)
    }

    fun saveItineraryItem() {
        val currentState = _uiState.value
        
        if (!isValid()) {
            val errorMessage = when {
                currentState.description.trim().isEmpty() -> "Por favor completa todos los campos requeridos"
                currentState.description.length > 200 -> "La descripción no puede exceder 200 caracteres"
                !isDateTimeValid(currentState.activityDateTime) -> "La fecha de la actividad debe estar dentro del rango permitido: desde 3 días antes del inicio del viaje hasta 3 días después del final del viaje"
                else -> "Por favor completa todos los campos requeridos"
            }
            
            _uiState.value = currentState.copy(
                errorMessage = errorMessage
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
            try {
                val itineraryItem = ItineraryItem(
                    id = itineraryId ?: 0,
                    tripId = tripId,
                    activityDateTime = currentState.activityDateTime,
                    description = currentState.description.trim(),
                    hasReminder = currentState.hasReminder,
                    reminderTimeBefore = if (currentState.hasReminder) currentState.reminderTimeBefore else "",
                    alertType = currentState.alertType
                )

                if (itineraryId == null) {
                    itineraryRepository.insertItineraryItem(itineraryItem)
                } else {
                    itineraryRepository.updateItineraryItem(itineraryItem)
                }

                _uiState.value = currentState.copy(
                    isLoading = false,
                    isSaveSuccessful = true
                )
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Error al guardar la actividad: ${e.message}"
                )
            }
        }
    }

    private fun isValid(): Boolean {
        val state = _uiState.value
        return state.description.trim().isNotEmpty() &&
                state.description.length <= 200 &&
                isDateTimeValid(state.activityDateTime)
    }

    private fun isDateTimeValid(dateTime: Long): Boolean {
        val state = _uiState.value
        val tripStart = state.tripStartDate
        val tripEnd = state.tripEndDate
        
        if (tripStart == 0L || tripEnd == 0L) return true
        
        // 3 días antes del inicio del viaje
        val minDate = tripStart - (3 * 24 * 60 * 60 * 1000)
        // 3 días después del fin del viaje
        val maxDate = tripEnd + (3 * 24 * 60 * 60 * 1000)
        
        return dateTime in minDate..maxDate
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class ItineraryEditUiState(
    val activityDateTime: Long = 0L,
    val activityDateTimeText: String = "",
    val description: String = "",
    val hasReminder: Boolean = false,
    val reminderTimeBefore: String = "1 hora antes",
    val alertType: AlertType = AlertType.NORMAL,
    val tripStartDate: Long = 0L,
    val tripEndDate: Long = 0L,
    val isLoading: Boolean = false,
    val isSaveSuccessful: Boolean = false,
    val errorMessage: String? = null
) {
    val isValid: Boolean
        get() = description.trim().isNotEmpty() &&
                description.length <= 200 &&
                activityDateTime > 0
}
