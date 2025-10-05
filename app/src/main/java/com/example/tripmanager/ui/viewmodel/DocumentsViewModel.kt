package com.example.tripmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripmanager.data.model.Document
import com.example.tripmanager.data.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DocumentsViewModel(
    private val documentRepository: DocumentRepository,
    private val tripId: Long
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DocumentsUiState())
    val uiState: StateFlow<DocumentsUiState> = _uiState.asStateFlow()

    init {
        loadDocuments()
    }

    private fun loadDocuments() {
        viewModelScope.launch {
            documentRepository.getDocumentsByTrip(tripId).collect { documents ->
                _uiState.value = _uiState.value.copy(
                    documents = documents,
                    isLoading = false
                )
            }
        }
    }

    fun deleteDocument(document: Document) {
        viewModelScope.launch {
            try {
                documentRepository.deleteDocument(document)
                // Also delete the physical file
                try {
                    val file = java.io.File(document.filePath)
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    // File deletion failed, but document removed from DB
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al eliminar documento: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class DocumentsUiState(
    val documents: List<Document> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)


