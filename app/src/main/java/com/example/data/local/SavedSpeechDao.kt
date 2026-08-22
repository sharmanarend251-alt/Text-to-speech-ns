package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedSpeechDao {

    @Query("SELECT * FROM saved_speeches ORDER BY createdAt DESC")
    fun getAllSpeeches(): Flow<List<SavedSpeech>>

    @Query("SELECT * FROM saved_speeches WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteSpeeches(): Flow<List<SavedSpeech>>

    @Query("SELECT * FROM saved_speeches WHERE title LIKE '%' || :query || '%' OR text LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchSpeeches(query: String): Flow<List<SavedSpeech>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeech(speech: SavedSpeech): Long

    @Update
    suspend fun updateSpeech(speech: SavedSpeech)

    @Delete
    suspend fun deleteSpeech(speech: SavedSpeech)

    @Query("DELETE FROM saved_speeches WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE saved_speeches SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM saved_speeches")
    suspend fun getCount(): Int
}
