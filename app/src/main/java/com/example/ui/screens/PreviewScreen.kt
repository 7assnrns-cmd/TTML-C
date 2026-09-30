package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.KaraokePreviewStyle
import com.example.domain.model.LyricsProject
import com.example.ui.components.LyricsEnhanced
import com.example.ui.theme.AppleMusicCyan
import com.example.ui.theme.AppleMusicRed
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioDarkSurfaceVariant
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import java.util.Locale

@Composable
fun PreviewScreen(
    project: LyricsProject,
    currentPositionMs: Long,
    isPlaying: Boolean,
    playbackSpeed: Float,
    previewStyle: KaraokePreviewStyle,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onCycleSpeed: () -> Unit,
    onSelectPreviewStyle: (KaraokePreviewStyle) -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var showControls by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .clickable { showControls = !showControls }
    ) {
        // Apple Music Enhanced Karaoke Lyrics View with Mask Reveal and Blur
        LyricsEnhanced(
            lines = project.lines,
            agents = project.agents,
            currentTimeMs = currentPositionMs,
            coverUri = project.coverUri,
            previewStyle = previewStyle,
            autoScroll = true,
            fontScale = 1.0f,
            onSeekTo = onSeek,
            onShareSelectedLines = { selected ->
                val quote = selected.joinToString("\n") { it.fullText }
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "${project.title} - Lyrics")
                    putExtra(Intent.EXTRA_TEXT, "🎵 \"$quote\"\n— ${project.title} (${project.artist})")
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Lyrics"))
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top Navigation & Style Selector Pill
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 12.dp, end = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .testTag("btn_preview_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = project.title,
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = if (project.artist.isNotBlank()) project.artist else "Apple Music Preview",
                            color = TextGray,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.size(40.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Preview Style Selector Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (style in KaraokePreviewStyle.entries) {
                                val isSelected = style == previewStyle
                                Surface(
                                    onClick = { onSelectPreviewStyle(style) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) AppleMusicRed else Color.Transparent,
                                    modifier = Modifier.testTag("btn_style_${style.name}")
                                ) {
                                    Text(
                                        text = style.displayName,
                                        color = if (isSelected) Color.White else TextGray,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Floating Player Controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.75f),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioDarkSurfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Scrubber Slider
                    val totalDuration = project.durationMs.coerceAtLeast(1000L)
                    val sliderPos = currentPositionMs.toFloat().coerceIn(0f, totalDuration.toFloat())

                    Slider(
                        value = sliderPos,
                        onValueChange = { onSeek(it.toLong()) },
                        valueRange = 0f..totalDuration.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = AppleMusicRed,
                            inactiveTrackColor = StudioDarkSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("preview_slider")
                    )

                    // Timestamps
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(currentPositionMs),
                            color = TextGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = formatTime(totalDuration),
                            color = TextGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Buttons: Rewind, Play/Pause, Fast Forward, Speed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rewind 5s
                        IconButton(
                            onClick = { onSeek((currentPositionMs - 5000L).coerceAtLeast(0L)) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastRewind,
                                contentDescription = "Rewind 5s",
                                tint = TextWhite
                            )
                        }

                        // Play/Pause
                        Button(
                            onClick = onPlayPause,
                            colors = ButtonDefaults.buttonColors(containerColor = AppleMusicRed),
                            shape = CircleShape,
                            modifier = Modifier.size(54.dp).testTag("btn_preview_play_pause")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Fast Forward 5s
                        IconButton(
                            onClick = { onSeek((currentPositionMs + 5000L).coerceAtMost(totalDuration)) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "Forward 5s",
                                tint = TextWhite
                            )
                        }

                        // Speed Pill
                        Surface(
                            onClick = onCycleSpeed,
                            shape = RoundedCornerShape(8.dp),
                            color = StudioDarkSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = AppleMusicCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.2f", playbackSpeed).removeSuffix(".00")}x",
                                    fontSize = 11.sp,
                                    color = AppleMusicCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val minutes = (timeMs / 60000).toInt()
    val seconds = ((timeMs % 60000) / 1000).toInt()
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
