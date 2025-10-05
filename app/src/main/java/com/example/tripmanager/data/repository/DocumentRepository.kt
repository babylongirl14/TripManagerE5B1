package com.example.tripmanager.data.repository

import com.example.tripmanager.data.dao.DocumentDao
import com.example.tripmanager.data.model.Document
import kotlinx.coroutines.flow.Flow

class DocumentRepository(
    private val documentDao: DocumentDao
) {
    fun getDocumentsByTrip(tripId: Long): Flow<List<Document>> {
        return documentDao.getDocumentsByTrip(tripId)
    }

    suspend fun getDocumentById(documentId: Long): Document? {
        return documentDao.getDocumentById(documentId)
    }

    suspend fun insertDocument(document: Document): Long {
        return documentDao.insertDocument(document)
    }

    suspend fun updateDocument(document: Document) {
        documentDao.updateDocument(document)
    }

    suspend fun deleteDocument(document: Document) {
        documentDao.deleteDocument(document)
    }

    suspend fun deleteDocumentsByTrip(tripId: Long) {
        documentDao.deleteDocumentsByTrip(tripId)
    }
}


