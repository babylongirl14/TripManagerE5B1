package com.example.tripmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripmanager.data.model.Trip
import com.example.tripmanager.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TripsViewModel(
    private val tripRepository: TripRepository,
    private val currentUser: String
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TripsUiState())
    val uiState: StateFlow<TripsUiState> = _uiState.asStateFlow()

    init {
        loadTrips()
    }

    private fun loadTrips() {
        viewModelScope.launch {
            tripRepository.getTripsByUser(currentUser).collect { trips ->
                _uiState.value = _uiState.value.copy(trips = trips)
            }
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            try {
                tripRepository.deleteTrip(trip)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al eliminar el viaje: ${e.message}"
                )
            }
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class TripsUiState(
    val trips: List<Trip> = emptyList(),
    val errorMessage: String? = null
)
