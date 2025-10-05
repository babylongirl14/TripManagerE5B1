package com.example.tripmanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class Document(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tripId: Long,
    val title: String,
    val fileName: String,
    val filePath: String,
    val fileType: String,
    val createdAt: Long = System.currentTimeMillis()
)

