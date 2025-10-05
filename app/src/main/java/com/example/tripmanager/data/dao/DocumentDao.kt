package com.example.tripmanager.data.dao

import androidx.room.*
import com.example.tripmanager.data.model.Document
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents WHERE tripId = :tripId ORDER BY createdAt DESC")
    fun getDocumentsByTrip(tripId: Long): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE id = :documentId")
    suspend fun getDocumentById(documentId: Long): Document?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document): Long

    @Update
    suspend fun updateDocument(document: Document)

    @Delete
    suspend fun deleteDocument(document: Document)

    @Query("DELETE FROM documents WHERE tripId = :tripId")
    suspend fun deleteDocumentsByTrip(tripId: Long)
}


