package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_speeches")
data class SavedSpeech(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val text: String,
    val languageCode: String = "hi-IN",
    val languageDisplayName: String = "Hindi (हिन्दी)",
    val pitch: Float = 1.0f,
    val speed: Float = 1.0f,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
