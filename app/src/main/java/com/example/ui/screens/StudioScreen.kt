package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tts.TtsLanguage
import com.example.ui.MainViewModel
import com.example.ui.components.AudioVisualizer
import com.example.ui.components.LanguageSelectionSheet
import com.example.ui.components.SaveSpeechDialog
import com.example.ui.components.VoiceControlsCard
import com.example.ui.theme.WaveColor1
import com.example.ui.theme.WaveColor2

@Composable
fun StudioScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val pitch by viewModel.pitch.collectAsStateWithLifecycle()
    val speed by viewModel.speed.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val availableLanguages by viewModel.availableLanguages.collectAsStateWithLifecycle()
    val amplitudes by viewModel.amplitudes.collectAsStateWithLifecycle()
    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()

    var showLanguageSheet by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    val wordCount = remember(inputText) {
        if (inputText.isBlank()) 0 else inputText.trim().split(Regex("\\s+")).size
    }
    val charCount = inputText.length

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("studio_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Audio Visualizer Header Card
        AudioVisualizer(
            isSpeaking = isSpeaking,
            amplitudes = amplitudes,
            languageName = selectedLanguage.displayName,
            charCount = charCount,
            wordCount = wordCount
        )

        // 2. Main Text Input Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("text_input_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Actions Bar above input: Paste, Clear, Copy
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enter Text / टेक्स्ट लिखें",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Paste from clipboard
                        FilledTonalIconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clipData = clipboard.primaryClip
                                if (clipData != null && clipData.itemCount > 0) {
                                    val pasted = clipData.getItemAt(0).text?.toString() ?: ""
                                    if (pasted.isNotEmpty()) {
                                        viewModel.updateInputText(pasted)
                                        viewModel.showSnackbar("Pasted from clipboard / क्लिपबोर्ड से चिपकाया")
                                    }
                                } else {
                                    viewModel.showSnackbar("Clipboard is empty / क्लिपबोर्ड खाली है")
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("paste_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Clear input
                        if (inputText.isNotEmpty()) {
                            FilledTonalIconButton(
                                onClick = { viewModel.clearInput() },
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("clear_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Text Area
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.updateInputText(it) },
                    placeholder = {
                        Text(
                            "यहाँ कोई भी टेक्स्ट लिखें या पेस्ट करें, फिर प्ले बटन दबाएं...\n(Type or paste any text here in Hindi, English, etc.)",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp, max = 220.dp)
                        .testTag("tts_text_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Hindi/English Example Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        SuggestionChip(
                            onClick = {
                                viewModel.updateInputText("नमस्ते! आपका स्वागत है। आज आप क्या सुनना चाहते हैं?")
                            },
                            label = { Text("हिंदी अभिवादन", fontSize = 11.sp) }
                        )
                    }
                    item {
                        SuggestionChip(
                            onClick = {
                                viewModel.updateInputText("Hello and welcome! This is a 100% free text to speech converter with natural voice.")
                            },
                            label = { Text("English Greeting", fontSize = 11.sp) }
                        )
                    }
                    item {
                        SuggestionChip(
                            onClick = {
                                viewModel.updateInputText("सफलता का कोई शॉर्टकट नहीं होता, यह कठिन परिश्रम और लगन का परिणाम है।")
                            },
                            label = { Text("प्रेरणादायक विचार", fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // 3. Voice & Pitch Controls Card
        VoiceControlsCard(
            pitch = pitch,
            speed = speed,
            selectedLanguage = selectedLanguage,
            availableLanguages = availableLanguages,
            onPitchChange = { viewModel.setPitch(it) },
            onSpeedChange = { viewModel.setSpeed(it) },
            onLanguageChange = { viewModel.setLanguage(it) },
            onReset = { viewModel.resetVoiceSettings() },
            onOpenLanguageSheet = { showLanguageSheet = true }
        )

        // 4. Primary Playback & Action Controls
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("playback_action_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Big Play / Stop Button
                Button(
                    onClick = {
                        if (isSpeaking) {
                            viewModel.stopSpeaking()
                        } else {
                            viewModel.speakCurrentText()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("play_stop_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSpeaking) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isSpeaking) "Stop" else "Speak",
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSpeaking) "Stop Speaking / आवाज़ रोकें" else "Speak Text / आवाज़ सुनें",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Secondary Action Row: Export Audio (.wav), Save to Library, Share Text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Export Audio Button
                    FilledTonalButton(
                        onClick = { viewModel.exportAudio() },
                        enabled = !isExporting && inputText.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("export_audio_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Export Audio",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isExporting) "Saving..." else "Export Audio",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Save to Library Button
                    FilledTonalButton(
                        onClick = { showSaveDialog = true },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("save_to_library_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = "Save",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Save / सहेजें",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Share Text Button
                    FilledTonalButton(
                        onClick = { viewModel.shareCurrentText() },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("share_text_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Share / शेयर",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Dialogs & Sheets
    if (showLanguageSheet) {
        LanguageSelectionSheet(
            availableLanguages = availableLanguages,
            selectedLanguage = selectedLanguage,
            onLanguageSelected = { viewModel.setLanguage(it) },
            onDismiss = { showLanguageSheet = false }
        )
    }

    if (showSaveDialog) {
        SaveSpeechDialog(
            initialText = inputText,
            onSave = { title ->
                viewModel.saveCurrentSpeech(title)
                showSaveDialog = false
            },
            onDismiss = { showSaveDialog = false }
        )
    }
}
