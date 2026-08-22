package com.example.tts

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import kotlin.random.Random

class TtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "TtsManager"
        private const val UTTERANCE_ID_SPEAK = "UTTERANCE_SPEAK"
        private const val UTTERANCE_ID_EXPORT = "UTTERANCE_EXPORT"
    }

    private var textToSpeech: TextToSpeech? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var visualizerJob: Job? = null

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _pitch = MutableStateFlow(1.0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _speed = MutableStateFlow(1.0f)
    val speed: StateFlow<Float> = _speed.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(PresetData.DEFAULT_LANGUAGE)
    val selectedLanguage: StateFlow<TtsLanguage> = _selectedLanguage.asStateFlow()

    private val _availableLanguages = MutableStateFlow<List<TtsLanguage>>(PresetData.SUPPORTED_LANGUAGES)
    val availableLanguages: StateFlow<List<TtsLanguage>> = _availableLanguages.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<Voice>>(emptyList())
    val availableVoices: StateFlow<List<Voice>> = _availableVoices.asStateFlow()

    private val _selectedVoiceName = MutableStateFlow<String?>(null)
    val selectedVoiceName: StateFlow<String?> = _selectedVoiceName.asStateFlow()

    private val _currentRange = MutableStateFlow<Pair<Int, Int>?>(null)
    val currentRange: StateFlow<Pair<Int, Int>?> = _currentRange.asStateFlow()

    // Real-time audio amplitude wave heights (normalized 0.1f - 1.0f)
    private val _amplitudes = MutableStateFlow(List(16) { 0.15f })
    val amplitudes: StateFlow<List<Float>> = _amplitudes.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var pendingExportCallback: ((Boolean, File?, String?) -> Unit)? = null
    private var currentExportFile: File? = null

    init {
        initTts()
    }

    private fun initTts() {
        try {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TTS", e)
            _errorMessage.value = "Failed to start TTS engine: ${e.localizedMessage}"
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val tts = textToSpeech ?: return
            _isInitialized.value = true

            // Set progress listener
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Handler(Looper.getMainLooper()).post {
                        if (utteranceId?.startsWith(UTTERANCE_ID_SPEAK) == true) {
                            _isSpeaking.value = true
                            startVisualizerAnimation()
                        }
                    }
                }

                override fun onDone(utteranceId: String?) {
                    Handler(Looper.getMainLooper()).post {
                        if (utteranceId?.startsWith(UTTERANCE_ID_SPEAK) == true) {
                            _isSpeaking.value = false
                            _currentRange.value = null
                            stopVisualizerAnimation()
                        } else if (utteranceId?.startsWith(UTTERANCE_ID_EXPORT) == true) {
                            val file = currentExportFile
                            pendingExportCallback?.invoke(true, file, null)
                            pendingExportCallback = null
                            currentExportFile = null
                        }
                    }
                }

                override fun onError(utteranceId: String?) {
                    Handler(Looper.getMainLooper()).post {
                        if (utteranceId?.startsWith(UTTERANCE_ID_SPEAK) == true) {
                            _isSpeaking.value = false
                            _currentRange.value = null
                            stopVisualizerAnimation()
                        } else if (utteranceId?.startsWith(UTTERANCE_ID_EXPORT) == true) {
                            pendingExportCallback?.invoke(false, null, "Export failed during synthesis")
                            pendingExportCallback = null
                            currentExportFile = null
                        }
                    }
                }

                override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                    Handler(Looper.getMainLooper()).post {
                        _currentRange.value = Pair(start, end)
                    }
                }
            })

            // Set initial language
            applyLanguage(_selectedLanguage.value)
            applyPitchAndSpeed()
            queryAvailableVoices()
        } else {
            _isInitialized.value = false
            _errorMessage.value = "Text to speech engine initialization failed."
        }
    }

    private fun queryAvailableVoices() {
        val tts = textToSpeech ?: return
        try {
            val voices = tts.voices?.toList() ?: emptyList()
            _availableVoices.value = voices
        } catch (e: Exception) {
            Log.e(TAG, "Voices query error", e)
        }
    }

    fun setLanguage(lang: TtsLanguage) {
        _selectedLanguage.value = lang
        applyLanguage(lang)
    }

    private fun applyLanguage(lang: TtsLanguage) {
        val tts = textToSpeech ?: return
        try {
            val result = tts.setLanguage(lang.locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "Language ${lang.code} missing data or not supported")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed setting language ${lang.code}", e)
        }
    }

    fun setPitch(pitchVal: Float) {
        val clamped = pitchVal.coerceIn(0.5f, 2.0f)
        _pitch.value = clamped
        textToSpeech?.setPitch(clamped)
    }

    fun setSpeed(speedVal: Float) {
        val clamped = speedVal.coerceIn(0.5f, 2.0f)
        _speed.value = clamped
        textToSpeech?.setSpeechRate(clamped)
    }

    fun setVoice(voice: Voice?) {
        _selectedVoiceName.value = voice?.name
        if (voice != null) {
            textToSpeech?.voice = voice
        }
    }

    private fun applyPitchAndSpeed() {
        textToSpeech?.setPitch(_pitch.value)
        textToSpeech?.setSpeechRate(_speed.value)
    }

    fun speak(text: String, onStart: () -> Unit = {}) {
        if (text.isBlank()) return
        val tts = textToSpeech ?: return

        stop()
        applyLanguage(_selectedLanguage.value)
        applyPitchAndSpeed()

        val utteranceId = "${UTTERANCE_ID_SPEAK}_${System.currentTimeMillis()}"
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        onStart()
    }

    fun stop() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }
        _isSpeaking.value = false
        _currentRange.value = null
        stopVisualizerAnimation()
    }

    fun exportAudio(
        text: String,
        title: String,
        onResult: (Boolean, File?, String?) -> Unit
    ) {
        if (text.isBlank()) {
            onResult(false, null, "Text is empty")
            return
        }
        val tts = textToSpeech
        if (tts == null || !_isInitialized.value) {
            onResult(false, null, "TTS Engine is not ready")
            return
        }

        try {
            val audioDir = File(context.cacheDir, "exported_audio")
            if (!audioDir.exists()) {
                audioDir.mkdirs()
            }
            val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9_]"), "_").take(25)
            val fileName = "tts_${if (sanitizedTitle.isNotBlank()) sanitizedTitle else "audio"}_${System.currentTimeMillis()}.wav"
            val file = File(audioDir, fileName)

            currentExportFile = file
            pendingExportCallback = onResult

            applyLanguage(_selectedLanguage.value)
            applyPitchAndSpeed()

            val utteranceId = "${UTTERANCE_ID_EXPORT}_${System.currentTimeMillis()}"
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)

            val result = tts.synthesizeToFile(text, params, file, utteranceId)
            if (result != TextToSpeech.SUCCESS) {
                pendingExportCallback = null
                currentExportFile = null
                onResult(false, null, "Synthesis request failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export audio", e)
            pendingExportCallback = null
            currentExportFile = null
            onResult(false, null, e.localizedMessage ?: "Unknown error")
        }
    }

    fun shareAudioFile(file: File, title: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/wav"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Generated with Free Text to Speech app: $title")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Audio via").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing audio file", e)
        }
    }

    fun shareText(text: String, title: String = "Free TTS Text") {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "Share Text").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing text", e)
        }
    }

    private fun startVisualizerAnimation() {
        visualizerJob?.cancel()
        visualizerJob = scope.launch {
            while (isActive && _isSpeaking.value) {
                // Generate dynamic realistic audio waveform pulses
                val newAmps = List(16) { index ->
                    val centerFactor = 1.0f - (Math.abs(index - 7.5f) / 8f) * 0.4f
                    val randomFluctuation = Random.nextFloat() * 0.7f + 0.3f
                    (centerFactor * randomFluctuation).coerceIn(0.12f, 1.0f)
                }
                _amplitudes.value = newAmps
                delay(90)
            }
            _amplitudes.value = List(16) { 0.15f }
        }
    }

    private fun stopVisualizerAnimation() {
        visualizerJob?.cancel()
        visualizerJob = null
        _amplitudes.value = List(16) { 0.15f }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun release() {
        stop()
        try {
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS", e)
        }
        textToSpeech = null
    }
}
