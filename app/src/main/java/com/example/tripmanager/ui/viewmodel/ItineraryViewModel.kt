package com.example.tripmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripmanager.data.model.ItineraryItem
import com.example.tripmanager.data.repository.ItineraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ItineraryViewModel(
    private val itineraryRepository: ItineraryRepository,
    private val tripId: Long
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ItineraryUiState())
    val uiState: StateFlow<ItineraryUiState> = _uiState.asStateFlow()

    init {
        loadItinerary()
    }

    private fun loadItinerary() {
        viewModelScope.launch {
            itineraryRepository.getItineraryByTrip(tripId).collect { itinerary ->
                _uiState.value = _uiState.value.copy(itinerary = itinerary)
            }
        }
    }

    fun deleteItineraryItem(itineraryItem: ItineraryItem) {
        viewModelScope.launch {
            try {
                itineraryRepository.deleteItineraryItem(itineraryItem)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al eliminar la actividad: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class ItineraryUiState(
    val itinerary: List<ItineraryItem> = emptyList(),
    val errorMessage: String? = null
)

