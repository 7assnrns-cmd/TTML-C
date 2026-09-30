package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AgentInfo
import com.example.domain.model.KaraokeAlignment
import com.example.domain.model.SongPart
import com.example.ui.theme.AppleMusicAmber
import com.example.ui.theme.AppleMusicCyan
import com.example.ui.theme.AppleMusicGreen
import com.example.ui.theme.AppleMusicPurple
import com.example.ui.theme.AppleMusicRed
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.theme.StudioDarkSurfaceVariant
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite

@Composable
fun EditorActionBar(
    isPlaying: Boolean,
    playbackSpeed: Float,
    canUndo: Boolean,
    canRedo: Boolean,
    agents: List<AgentInfo>,
    currentAgentId: String,
    isCurrentTokenBackground: Boolean,
    onPlayPause: () -> Unit,
    onStepOffset: (Long) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onCycleSpeed: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onMarkLineStart: () -> Unit,
    onMarkLineEnd: () -> Unit,
    onMarkWordStart: () -> Unit,
    onMarkWordEnd: () -> Unit,
    onSplitWord: () -> Unit,
    onAutoSyncLine: () -> Unit,
    onToggleBackgroundVocal: (Boolean) -> Unit,
    onSelectAgent: (String) -> Unit,
    onOpenAddAgentDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = StudioDarkSurface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ==========================================
            // 🔴 SECTION 1: TIMING TOOLS (Top Priority)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔴 أدوات التوقيت والمزامنة",
                    color = AppleMusicRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                // Auto Sync Assistant Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppleMusicPurple.copy(alpha = 0.2f))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAutoSyncLine()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = AppleMusicPurple,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "مزامنة تلقائية للسطر",
                        color = TextWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Sub-row 1A: Line Timing [⟦ بداية السطر] [نهاية السطر ⟧]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimingActionButton(
                    text = "⟦ بداية السطر",
                    subtitle = "Mark Line Start",
                    containerColor = AppleMusicRed,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_mark_line_start",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onMarkLineStart()
                    }
                )
                TimingActionButton(
                    text = "نهاية السطر ⟧",
                    subtitle = "Mark Line End",
                    containerColor = Color(0xFFC41E3A),
                    modifier = Modifier.weight(1f),
                    testTag = "btn_mark_line_end",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onMarkLineEnd()
                    }
                )
            }

            // Sub-row 1B: Word Timing [⟨ بداية الكلمة] [نهاية الكلمة ⟩] [✂️ تقسيم]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimingActionButton(
                    text = "⟨ بداية الكلمة",
                    subtitle = "Word Start",
                    containerColor = AppleMusicCyan,
                    contentColor = Color.Black,
                    modifier = Modifier.weight(1.2f),
                    testTag = "btn_mark_word_start",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onMarkWordStart()
                    }
                )
                TimingActionButton(
                    text = "نهاية الكلمة ⟩",
                    subtitle = "Word End",
                    containerColor = AppleMusicGreen,
                    contentColor = Color.Black,
                    modifier = Modifier.weight(1.2f),
                    testTag = "btn_mark_word_end",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onMarkWordEnd()
                    }
                )
                TimingActionButton(
                    text = "✂️ تقسيم",
                    subtitle = "Split Syllable",
                    containerColor = StudioDarkSurfaceVariant,
                    contentColor = TextWhite,
                    modifier = Modifier.weight(0.9f),
                    testTag = "btn_split_word",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSplitWord()
                    }
                )
            }

            // ==========================================
            // 🎤 SECTION 2: VOICES & AGENTS & BACKING
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🎤 الأصوات والمغنون",
                    color = AppleMusicCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                // Backing Vocals Quick Toggle
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isBg = isCurrentTokenBackground
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isBg) AppleMusicAmber else StudioDarkSurfaceVariant)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleBackgroundVocal(true)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🎵 BG (خلفي)",
                            color = if (isBg) Color.Black else TextGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isBg) AppleMusicCyan.copy(alpha = 0.2f) else StudioDarkSurfaceVariant)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleBackgroundVocal(false)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "✕ BG (رئيسي)",
                            color = if (!isBg) AppleMusicCyan else TextDim,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Singer Agents Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(agents) { agent ->
                    val isSelected = agent.id == currentAgentId
                    val agentColor = try {
                        Color(android.graphics.Color.parseColor(agent.colorHex))
                    } catch (_: Exception) {
                        AppleMusicRed
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) agentColor else StudioDarkSurfaceVariant)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSelectAgent(agent.id)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (agent.type == "group") Icons.Default.MusicNote else Icons.Default.Mic,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else TextGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${agent.id}: ${agent.name}",
                            color = if (isSelected) Color.White else TextGray,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (agent.alignment == KaraokeAlignment.END) {
                            Text("→", color = if (isSelected) Color.White else TextDim, fontSize = 11.sp)
                        }
                    }
                }

                // Add Agent Button
                item {
                    IconButton(
                        onClick = onOpenAddAgentDialog,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(StudioDarkSurfaceVariant)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Singer",
                            tint = AppleMusicCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // ==========================================
            // 🎛️ CONTROLS & PLAYBACK (Single Compact Row)
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(StudioCardBg)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Undo
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) TextWhite else TextDim,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Redo
                IconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) TextWhite else TextDim,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Rewind -100ms
                IconButton(
                    onClick = { onStepOffset(-100L) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.FastRewind,
                        contentDescription = "-100ms",
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Play / Pause Central Action
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(AppleMusicRed, Color(0xFFFF5252))
                            )
                        )
                        .clickable { onPlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Fast Forward +100ms
                IconButton(
                    onClick = { onStepOffset(100L) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.FastForward,
                        contentDescription = "+100ms",
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Speed Pill (1.0x, 0.75x, etc.)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioDarkSurfaceVariant)
                        .clickable { onCycleSpeed() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${playbackSpeed}x",
                        color = AppleMusicCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Zoom Controls
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onZoomOut, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = TextGray, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onZoomIn, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = TextGray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TimingActionButton(
    text: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color = Color.White,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = contentColor.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
    }
}
