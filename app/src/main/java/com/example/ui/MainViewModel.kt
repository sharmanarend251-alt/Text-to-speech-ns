package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SavedSpeech
import com.example.data.local.SpeechRepository
import com.example.tts.PresetData
import com.example.tts.PresetPhrase
import com.example.tts.TtsLanguage
import com.example.tts.TtsManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SpeechRepository
    val ttsManager: TtsManager = TtsManager(application.applicationContext)

    private val _inputText = MutableStateFlow("नमस्ते! यह एक बिल्कुल मुफ़्त टेक्स्ट टू स्पीच ऐप है। यहाँ आप कोई भी पाठ लिख सकते हैं और आवाज़ सुन सकते हैं।")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterFavoritesOnly = MutableStateFlow(false)
    val filterFavoritesOnly: StateFlow<Boolean> = _filterFavoritesOnly.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val isSpeaking: StateFlow<Boolean> = ttsManager.isSpeaking
    val pitch: StateFlow<Float> = ttsManager.pitch
    val speed: StateFlow<Float> = ttsManager.speed
    val selectedLanguage: StateFlow<TtsLanguage> = ttsManager.selectedLanguage
    val availableLanguages: StateFlow<List<TtsLanguage>> = ttsManager.availableLanguages
    val amplitudes: StateFlow<List<Float>> = ttsManager.amplitudes
    val currentRange: StateFlow<Pair<Int, Int>?> = ttsManager.currentRange

    init {
        val db = AppDatabase.getInstance(application)
        repository = SpeechRepository(db.savedSpeechDao())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val savedSpeeches: StateFlow<List<SavedSpeech>> = combine(_searchQuery, _filterFavoritesOnly) { query, favOnly ->
        Pair(query, favOnly)
    }.flatMapLatest { (query, favOnly) ->
        if (query.isNotBlank()) {
            repository.searchSpeeches(query)
        } else if (favOnly) {
            repository.favoriteSpeeches
        } else {
            repository.allSpeeches
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun clearInput() {
        _inputText.value = ""
        stopSpeaking()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterFavoritesOnly(favoritesOnly: Boolean) {
        _filterFavoritesOnly.value = favoritesOnly
    }

    fun speakCurrentText() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) {
            showSnackbar("Please enter text first / पहले टेक्स्ट लिखें")
            return
        }
        ttsManager.speak(text)
    }

    fun speakText(text: String, languageCode: String? = null) {
        if (text.isBlank()) return
        if (languageCode != null) {
            val lang = availableLanguages.value.find { it.code == languageCode }
            if (lang != null) {
                ttsManager.setLanguage(lang)
            }
        }
        ttsManager.speak(text)
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun setPitch(pitch: Float) {
        ttsManager.setPitch(pitch)
    }

    fun setSpeed(speed: Float) {
        ttsManager.setSpeed(speed)
    }

    fun setLanguage(lang: TtsLanguage) {
        ttsManager.setLanguage(lang)
    }

    fun resetVoiceSettings() {
        ttsManager.setPitch(1.0f)
        ttsManager.setSpeed(1.0f)
        showSnackbar("Voice settings reset to default / सेटिंग्स रीसेट हो गईं")
    }

    fun saveCurrentSpeech(title: String) {
        val text = _inputText.value.trim()
        if (text.isEmpty()) {
            showSnackbar("Cannot save empty text / खाली टेक्स्ट सेव नहीं हो सकता")
            return
        }

        viewModelScope.launch {
            val speech = SavedSpeech(
                title = title.ifBlank { "Speech #${System.currentTimeMillis() % 1000}" },
                text = text,
                languageCode = selectedLanguage.value.code,
                languageDisplayName = selectedLanguage.value.displayName,
                pitch = pitch.value,
                speed = speed.value,
                isFavorite = false,
                createdAt = System.currentTimeMillis()
            )
            repository.insertSpeech(speech)
            showSnackbar("Saved to library! / लाइब्रेरी में सुरक्षित कर दिया गया")
        }
    }

    fun toggleFavorite(speech: SavedSpeech) {
        viewModelScope.launch {
            repository.toggleFavorite(speech.id, !speech.isFavorite)
        }
    }

    fun deleteSpeech(speech: SavedSpeech) {
        viewModelScope.launch {
            repository.deleteSpeech(speech)
            showSnackbar("Deleted from library / हटा दिया गया")
        }
    }

    fun loadSpeechIntoStudio(speech: SavedSpeech) {
        _inputText.value = speech.text
        val lang = availableLanguages.value.find { it.code == speech.languageCode }
        if (lang != null) {
            ttsManager.setLanguage(lang)
        }
        ttsManager.setPitch(speech.pitch)
        ttsManager.setSpeed(speech.speed)
        showSnackbar("Loaded into Studio / स्टूडियो में लोड किया गया")
    }

    fun loadPresetPhraseIntoStudio(phrase: PresetPhrase) {
        _inputText.value = phrase.text
        val lang = availableLanguages.value.find { it.code == phrase.languageCode }
        if (lang != null) {
            ttsManager.setLanguage(lang)
        }
        showSnackbar("Loaded: ${phrase.title}")
    }

    fun exportAudio(customTitle: String? = null) {
        val text = _inputText.value.trim()
        if (text.isEmpty()) {
            showSnackbar("Please enter text first to export audio")
            return
        }

        val title = customTitle ?: "TTS_Audio_${System.currentTimeMillis() % 10000}"
        _isExporting.value = true

        ttsManager.exportAudio(text, title) { success, file, error ->
            _isExporting.value = false
            if (success && file != null) {
                showSnackbar("Audio file generated successfully!")
                ttsManager.shareAudioFile(file, title)
            } else {
                showSnackbar("Failed to export audio: ${error ?: "Unknown error"}")
            }
        }
    }

    fun shareCurrentText() {
        val text = _inputText.value.trim()
        if (text.isNotEmpty()) {
            ttsManager.shareText(text, "Text to Speech Message")
        }
    }

    fun shareText(text: String) {
        if (text.isNotBlank()) {
            ttsManager.shareText(text, "Text to Speech Message")
        }
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.release()
    }
}
