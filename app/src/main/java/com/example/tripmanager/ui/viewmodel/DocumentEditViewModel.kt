package com.example.tripmanager.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripmanager.data.model.Document
import com.example.tripmanager.data.model.DocumentType
import com.example.tripmanager.data.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class DocumentEditViewModel(
    private val documentRepository: DocumentRepository,
    private val tripId: Long,
    private val documentId: Long? = null
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DocumentEditUiState())
    val uiState: StateFlow<DocumentEditUiState> = _uiState.asStateFlow()

    init {
        if (documentId != null) {
            loadDocument(documentId)
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private fun loadDocument(documentId: Long) {
        viewModelScope.launch {
            try {
                val document = documentRepository.getDocumentById(documentId)
                if (document != null) {
                    _uiState.value = _uiState.value.copy(
                        title = document.title,
                        selectedFileUri = null, // Don't show URI for existing files
                        selectedFileName = document.fileName,
                        documentType = document.fileType,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar el documento: ${e.message}"
                )
            }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun onFileSelected(uri: Uri, fileName: String, context: Context) {
        val documentType = if (isImageFile(fileName)) "image" else "pdf"
        
        _uiState.value = _uiState.value.copy(
            selectedFileUri = uri,
            selectedFileName = fileName,
            documentType = documentType,
            errorMessage = null
        )
    }

    private fun isImageFile(fileName: String): Boolean {
        val imageExtensions = listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return imageExtensions.contains(extension)
    }

    fun saveDocument(context: Context) {
        val currentState = _uiState.value
        
        if (currentState.title.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "El título es obligatorio")
            return
        }

        if (documentId == null && currentState.selectedFileUri == null) {
            _uiState.value = currentState.copy(errorMessage = "Debes seleccionar un archivo")
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
            
            try {
                val filePath = if (currentState.selectedFileUri != null) {
                    // Save new file
                    saveFileToInternalStorage(context, currentState.selectedFileUri, currentState.selectedFileName)
                } else {
                    // Keep existing file path for edits
                    documentRepository.getDocumentById(documentId!!)?.filePath ?: ""
                }

                val document = Document(
                    id = documentId ?: 0,
                    tripId = tripId,
                    title = currentState.title,
                    fileName = currentState.selectedFileName,
                    filePath = filePath,
                    fileType = currentState.documentType
                )

                if (documentId != null) {
                    documentRepository.updateDocument(document)
                } else {
                    documentRepository.insertDocument(document)
                }

                _uiState.value = currentState.copy(
                    isLoading = false,
                    isSaveSuccessful = true
                )
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Error al guardar documento: ${e.message}"
                )
            }
        }
    }

    private fun saveFileToInternalStorage(context: Context, uri: Uri, fileName: String): String {
        val documentsDir = File(context.filesDir, "documents")
        if (!documentsDir.exists()) {
            documentsDir.mkdirs()
        }

        val uniqueFileName = "${UUID.randomUUID()}_$fileName"
        val file = File(documentsDir, uniqueFileName)

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return file.absolutePath
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class DocumentEditUiState(
    val title: String = "",
    val selectedFileUri: Uri? = null,
    val selectedFileName: String = "",
    val documentType: String = "image",
    val isLoading: Boolean = true,
    val isSaveSuccessful: Boolean = false,
    val errorMessage: String? = null
)
