package com.example.data.local

import kotlinx.coroutines.flow.Flow

class SpeechRepository(private val dao: SavedSpeechDao) {

    val allSpeeches: Flow<List<SavedSpeech>> = dao.getAllSpeeches()
    val favoriteSpeeches: Flow<List<SavedSpeech>> = dao.getFavoriteSpeeches()

    fun searchSpeeches(query: String): Flow<List<SavedSpeech>> = dao.searchSpeeches(query)

    suspend fun insertSpeech(speech: SavedSpeech): Long = dao.insertSpeech(speech)

    suspend fun updateSpeech(speech: SavedSpeech) = dao.updateSpeech(speech)

    suspend fun deleteSpeech(speech: SavedSpeech) = dao.deleteSpeech(speech)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) = dao.updateFavoriteStatus(id, isFavorite)

    suspend fun getCount(): Int = dao.getCount()
}
