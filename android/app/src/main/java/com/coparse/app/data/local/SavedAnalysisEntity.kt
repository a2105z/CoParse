package com.coparse.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_analyses")
data class SavedAnalysisEntity(
    @PrimaryKey val documentId: String,
    val title: String,
    val summaryLine: String,
    val score: Int,
    val payloadJson: String,
    val updatedAt: Long = System.currentTimeMillis(),
)
