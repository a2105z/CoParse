package com.coparse.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedAnalysisDao {
    @Query("SELECT * FROM saved_analyses ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<SavedAnalysisEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SavedAnalysisEntity)

    @Query("DELETE FROM saved_analyses WHERE documentId = :id")
    suspend fun delete(id: String)
}
