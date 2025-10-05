package com.example.tripmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripmanager.data.model.Trip
import com.example.tripmanager.data.model.TripType
import com.example.tripmanager.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class TripEditViewModel(
    private val tripRepository: TripRepository,
    private val currentUser: String,
    private val tripId: Long? = null
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TripEditUiState())
    val uiState: StateFlow<TripEditUiState> = _uiState.asStateFlow()

    init {
        if (tripId != null) {
            loadTrip(tripId)
        } else {
            // Para viajes nuevos, no necesitamos cargar nada
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private fun loadTrip(tripId: Long) {
        viewModelScope.launch {
            try {
                val trip = tripRepository.getTripById(tripId)
                if (trip != null) {
                    _uiState.value = _uiState.value.copy(
                        destination = trip.destination,
                        startDate = Date(trip.startDate),
                        endDate = Date(trip.endDate),
                        tripType = trip.tripType,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar el viaje: ${e.message}"
                )
            }
        }
    }

    fun onDestinationChange(destination: String) {
        _uiState.value = _uiState.value.copy(destination = destination)
    }

    fun onStartDateChange(date: Date) {
        _uiState.value = _uiState.value.copy(startDate = date)
    }

    fun onEndDateChange(date: Date) {
        _uiState.value = _uiState.value.copy(endDate = date)
    }

    fun onTripTypeChange(tripType: TripType) {
        _uiState.value = _uiState.value.copy(tripType = tripType)
    }

    fun saveTrip() {
        val currentState = _uiState.value
        if (currentState.destination.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "El destino es obligatorio")
            return
        }

        if (currentState.startDate >= currentState.endDate) {
            _uiState.value = currentState.copy(errorMessage = "La fecha de fin debe ser posterior a la fecha de inicio")
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
            try {
                val trip = Trip(
                    id = tripId ?: 0,
                    destination = currentState.destination,
                    startDate = currentState.startDate.time,
                    endDate = currentState.endDate.time,
                    tripType = currentState.tripType,
                    username = currentUser
                )

                if (tripId != null) {
                    tripRepository.updateTrip(trip)
                } else {
                    tripRepository.insertTrip(trip)
                }

                _uiState.value = currentState.copy(
                    isLoading = false,
                    isSaveSuccessful = true,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Error al guardar el viaje: ${e.message}"
                )
            }
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class TripEditUiState(
    val destination: String = "",
    val startDate: Date = Date(),
    val endDate: Date = Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L),
    val tripType: TripType = TripType.VACATION,
    val isLoading: Boolean = false,
    val isSaveSuccessful: Boolean = false,
    val errorMessage: String? = null
)
