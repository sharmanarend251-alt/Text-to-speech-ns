package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WaveColor1
import com.example.ui.theme.WaveColor2
import com.example.ui.theme.WaveColor3

@Composable
fun AudioVisualizer(
    isSpeaking: Boolean,
    amplitudes: List<Float>,
    languageName: String,
    charCount: Int,
    wordCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("audio_visualizer_card"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header row with status badge and language
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Pill
                val statusBgColor by animateColorAsState(
                    targetValue = if (isSpeaking) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    label = "status_color"
                )
                val statusTextColor by animateColorAsState(
                    targetValue = if (isSpeaking) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "status_text_color"
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusBgColor,
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isSpeaking) WaveColor2 else MaterialTheme.colorScheme.outline)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSpeaking) "Speaking / आवाज़ चालू है" else "Ready / तैयार",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = statusTextColor
                        )
                    }
                }

                // Stats badge
                Text(
                    text = "$wordCount words • $charCount chars",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Audio Waveform Bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val waveBrush = Brush.verticalGradient(
                    colors = listOf(WaveColor2, WaveColor1, WaveColor3)
                )

                amplitudes.forEachIndexed { index, amp ->
                    val targetHeight = if (isSpeaking) {
                        (amp * 48f).coerceIn(6f, 52f).dp
                    } else {
                        // Resting wave pattern
                        val base = (index % 4 + 1) * 3f + 4f
                        base.dp
                    }

                    val animatedHeight by animateDpAsState(
                        targetValue = targetHeight,
                        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
                        label = "wave_bar_$index"
                    )

                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(animatedHeight)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (isSpeaking) {
                                    waveBrush
                                } else {
                                    Brush.verticalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        )
                                    )
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Language Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Voice language",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Active Voice: $languageName",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
