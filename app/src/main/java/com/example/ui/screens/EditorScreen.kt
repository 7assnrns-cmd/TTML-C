package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AgentInfo
import com.example.domain.model.ExportFormat
import com.example.domain.model.LyricLine
import com.example.domain.model.LyricToken
import com.example.domain.model.LyricsProject
import com.example.domain.model.SongPart
import com.example.domain.parser.TimeUtils
import com.example.ui.components.EditorActionBar
import com.example.ui.components.WaveformTimeline
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
import java.util.Locale

@Composable
fun EditorScreen(
    project: LyricsProject,
    currentPositionMs: Long,
    isPlaying: Boolean,
    playbackSpeed: Float,
    waveformPeaks: List<Float>,
    zoomLevel: Float,
    activeLineIndex: Int,
    activeTokenIndex: Int,
    canUndo: Boolean,
    canRedo: Boolean,
    onBack: () -> Unit,
    onNavigateToPreview: () -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onStepOffset: (Long) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit,
    onSelectLine: (Int) -> Unit,
    onSelectToken: (Int) -> Unit,
    onMarkLineStart: () -> Unit,
    onMarkLineEnd: () -> Unit,
    onMarkWordStart: () -> Unit,
    onMarkWordEnd: () -> Unit,
    onSplitWord: () -> Unit,
    onAutoSyncLine: () -> Unit,
    onApplySyncOffset: (Long) -> Unit,
    onToggleBackgroundVocal: (Boolean) -> Unit,
    onSelectAgent: (String) -> Unit,
    onAddNewAgent: (String, String, String) -> Unit,
    onSelectSongPart: (SongPart) -> Unit,
    onAddLine: (String) -> Unit,
    onDeleteLine: (Int) -> Unit,
    onUpdateTranslation: (Int, String) -> Unit,
    onExportTtml: () -> String,
    onExportLrc: () -> String
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()

    var showAddLineDialog by remember { mutableStateOf(false) }
    var showAddSingerDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showOffsetDialog by remember { mutableStateOf(false) }
    var exportFormat by remember { mutableStateOf(ExportFormat.TTML) }

    val activeLine = project.lines.getOrNull(activeLineIndex)
    val activeToken = activeLine?.tokens?.getOrNull(activeTokenIndex)

    LaunchedEffect(activeLineIndex) {
        if (activeLineIndex in project.lines.indices) {
            listState.animateScrollToItem(activeLineIndex)
        }
    }

    Scaffold(
        containerColor = StudioDarkBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_editor_back")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    Column {
                        Text(
                            text = project.title.ifBlank { "Untitled Project" },
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (project.artist.isNotBlank()) project.artist else "Smart TTML Sync",
                            color = AppleMusicCyan,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Undo
                    IconButton(
                        onClick = onUndo,
                        enabled = canUndo,
                        modifier = Modifier.size(34.dp).testTag("btn_top_undo")
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
                        modifier = Modifier.size(34.dp).testTag("btn_top_redo")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (canRedo) TextWhite else TextDim,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Save
                    IconButton(
                        onClick = {
                            onSave()
                            Toast.makeText(context, "Saved Successfully! 💾", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(34.dp).testTag("btn_top_save")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Save",
                            tint = AppleMusicGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Export
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.size(34.dp).testTag("btn_export_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export TTML",
                            tint = AppleMusicCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Live Preview
                    Button(
                        onClick = onNavigateToPreview,
                        colors = ButtonDefaults.buttonColors(containerColor = AppleMusicRed),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp).testTag("btn_live_preview")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Preview", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        bottomBar = {
            EditorActionBar(
                isPlaying = isPlaying,
                playbackSpeed = playbackSpeed,
                canUndo = canUndo,
                canRedo = canRedo,
                currentAgentId = activeLine?.agentId ?: "v1",
                agents = project.agents,
                isCurrentTokenBackground = activeToken?.isBackground ?: false,
                onPlayPause = onPlayPause,
                onStepOffset = onStepOffset,
                onZoomIn = onZoomIn,
                onZoomOut = onZoomOut,
                onCycleSpeed = { showSpeedDialog = true },
                onUndo = onUndo,
                onRedo = onRedo,
                onMarkLineStart = onMarkLineStart,
                onMarkLineEnd = onMarkLineEnd,
                onMarkWordStart = onMarkWordStart,
                onMarkWordEnd = onMarkWordEnd,
                onSplitWord = onSplitWord,
                onAutoSyncLine = onAutoSyncLine,
                onToggleBackgroundVocal = onToggleBackgroundVocal,
                onSelectAgent = onSelectAgent,
                onOpenAddAgentDialog = { showAddSingerDialog = true }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
        ) {
            // Waveform Timeline
            WaveformTimeline(
                waveformPeaks = waveformPeaks,
                currentPositionMs = currentPositionMs,
                durationMs = project.durationMs,
                activeLine = activeLine,
                activeToken = activeToken,
                zoomLevel = zoomLevel,
                onSeek = onSeek,
                onZoomIn = onZoomIn,
                onZoomOut = onZoomOut,
                onOffsetStep = onStepOffset,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Header info & Add line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LYRICS (${project.lines.size} LINES) • FORMAT: MM:SS.mmm",
                    color = TextGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                TextButton(
                    onClick = { showAddLineDialog = true },
                    modifier = Modifier.testTag("btn_add_line_dialog")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = AppleMusicCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("+ Add Line", color = AppleMusicCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Lyrics Lines List
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("editor_lyrics_column")
            ) {
                itemsIndexed(project.lines, key = { _, line -> line.id }) { index, line ->
                    val isLineSelected = index == activeLineIndex
                    val agent = project.getAgent(line.agentId)

                    EditorLineCard(
                        line = line,
                        index = index,
                        isSelected = isLineSelected,
                        activeTokenIndex = if (isLineSelected) activeTokenIndex else -1,
                        currentPositionMs = currentPositionMs,
                        agent = agent,
                        onSelectLine = { onSelectLine(index) },
                        onSelectToken = { tokIdx ->
                            onSelectLine(index)
                            onSelectToken(tokIdx)
                        },
                        onDelete = { onDeleteLine(index) },
                        onUpdateTranslation = { tr -> onUpdateTranslation(index, tr) }
                    )
                }
            }
        }
    }

    // Speed Slider Dialog
    if (showSpeedDialog) {
        SpeedControlDialog(
            currentSpeed = playbackSpeed,
            onDismiss = { showSpeedDialog = false },
            onSpeedSelected = { newSpeed ->
                onSetSpeed(newSpeed)
                showSpeedDialog = false
            }
        )
    }

    // Sync Offset Dialog (±0.05s)
    if (showOffsetDialog) {
        SyncOffsetDialog(
            onDismiss = { showOffsetDialog = false },
            onApplyOffset = { deltaMs ->
                onApplySyncOffset(deltaMs)
                Toast.makeText(context, "Applied offset ${deltaMs}ms", Toast.LENGTH_SHORT).show()
                showOffsetDialog = false
            }
        )
    }

    // Add Line Dialog
    if (showAddLineDialog) {
        AddLineDialog(
            onDismiss = { showAddLineDialog = false },
            onConfirm = { text ->
                onAddLine(text)
                showAddLineDialog = false
            }
        )
    }

    // Add Singer Dialog
    if (showAddSingerDialog) {
        AddSingerDialog(
            onDismiss = { showAddSingerDialog = false },
            onConfirm = { name, type, color ->
                onAddNewAgent(name, type, color)
                showAddSingerDialog = false
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        ExportLyricsDialog(
            exportFormat = exportFormat,
            onFormatChange = { exportFormat = it },
            exportedContent = if (exportFormat == ExportFormat.TTML) onExportTtml() else onExportLrc(),
            onDismiss = { showExportDialog = false },
            onCopy = { content ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Lyrics", content)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditorLineCard(
    line: LyricLine,
    index: Int,
    isSelected: Boolean,
    activeTokenIndex: Int,
    currentPositionMs: Long,
    agent: AgentInfo,
    onSelectLine: () -> Unit,
    onSelectToken: (Int) -> Unit,
    onDelete: () -> Unit,
    onUpdateTranslation: (String) -> Unit
) {
    var showTranslationEdit by remember { mutableStateOf(false) }
    var translationText by remember(line.translation) { mutableStateOf(line.translation ?: "") }

    val agentColor = try {
        Color(android.graphics.Color.parseColor(agent.colorHex))
    } catch (_: Exception) {
        AppleMusicRed
    }

    val cardBorder = if (isSelected) AppleMusicCyan else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelectLine)
            .testTag("line_card_$index"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) StudioDarkSurfaceVariant else StudioCardBg
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else 0.dp,
            cardBorder
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header: Number + SongPart + Agent + Timing in MM:SS.mmm + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "L${index + 1}",
                        color = TextDim,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AppleMusicPurple.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = line.songPart.displayName,
                            color = AppleMusicPurple,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = agentColor.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "${agent.id}: ${agent.name}",
                            color = agentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timing formatted strictly as MM:SS.mmm
                    Text(
                        text = "${TimeUtils.formatMMSSmmm(line.beginMs)} - ${TimeUtils.formatMMSSmmm(line.endMs)}",
                        color = if (line.durationMs > 0) AppleMusicCyan else TextDim,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )

                    IconButton(
                        onClick = { showTranslationEdit = !showTranslationEdit },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Translation",
                            tint = if (line.translation.isNullOrBlank()) TextDim else AppleMusicAmber,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Line",
                            tint = TextDim,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Syllable Tokens Flow
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for ((tokIdx, token) in line.tokens.withIndex()) {
                    val isTokenSelected = isSelected && tokIdx == activeTokenIndex
                    val isTokenPlaying = token.isActiveAt(currentPositionMs)

                    val tokBg = when {
                        isTokenPlaying -> AppleMusicRed
                        isTokenSelected -> AppleMusicCyan.copy(alpha = 0.35f)
                        token.isBackground -> AppleMusicAmber.copy(alpha = 0.2f)
                        else -> StudioDarkBg
                    }

                    Surface(
                        onClick = { onSelectToken(tokIdx) },
                        shape = RoundedCornerShape(6.dp),
                        color = tokBg,
                        border = if (isTokenSelected) androidx.compose.foundation.BorderStroke(1.dp, AppleMusicCyan) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = token.text,
                                color = if (isTokenPlaying) Color.White else if (isTokenSelected) AppleMusicCyan else TextWhite,
                                fontSize = 13.sp,
                                fontWeight = if (isTokenPlaying || isTokenSelected) FontWeight.Bold else FontWeight.Normal,
                                fontStyle = if (token.isBackground) FontStyle.Italic else FontStyle.Normal
                            )
                            if (token.beginMs > 0L) {
                                Text(
                                    text = " (${TimeUtils.formatMMSSss(token.beginMs)})",
                                    color = TextDim,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            } else {
                                Text(
                                    text = " (--:--.---)",
                                    color = TextDim.copy(alpha = 0.4f),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            if (token.isBackground) {
                                Text(
                                    text = " [bg]",
                                    color = AppleMusicAmber,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Translation Edit Section
            AnimatedVisibility(visible = showTranslationEdit) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = translationText,
                        onValueChange = {
                            translationText = it
                            onUpdateTranslation(it)
                        },
                        placeholder = { Text("Line translation (ttm:role=\"x-translation\")...", color = TextDim, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppleMusicAmber,
                            unfocusedBorderColor = StudioDarkSurface,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        singleLine = true
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedControlDialog(
    currentSpeed: Float,
    onDismiss: () -> Unit,
    onSpeedSelected: (Float) -> Unit
) {
    var speed by remember { mutableFloatStateOf(currentSpeed) }
    val presets = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioDarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = AppleMusicCyan, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("🎚️ سرعة التشغيل (Playback Speed)", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Current Speed", color = TextGray, fontSize = 13.sp)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AppleMusicCyan.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${String.format(Locale.US, "%.2f", speed)}x",
                            color = AppleMusicCyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Continuous Slider: 0.1x to 2.0x in 0.05x steps
                Slider(
                    value = speed,
                    onValueChange = { speed = it },
                    valueRange = 0.1f..2.0f,
                    steps = 37,
                    colors = SliderDefaults.colors(
                        thumbColor = AppleMusicCyan,
                        activeTrackColor = AppleMusicCyan,
                        inactiveTrackColor = StudioDarkSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0.1x (Slow Sync)", color = TextDim, fontSize = 10.sp)
                    Text("1.0x", color = TextDim, fontSize = 10.sp)
                    Text("2.0x (Fast)", color = TextDim, fontSize = 10.sp)
                }

                Text("Preset Buttons:", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (preset in presets) {
                        val isSel = (Math.abs(speed - preset) < 0.02f)
                        Surface(
                            onClick = { speed = preset },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) AppleMusicCyan else StudioDarkSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${preset}x",
                                color = if (isSel) Color.Black else TextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSpeedSelected(speed) },
                colors = ButtonDefaults.buttonColors(containerColor = AppleMusicCyan)
            ) {
                Text("Apply Speed", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}

@Composable
private fun SyncOffsetDialog(
    onDismiss: () -> Unit,
    onApplyOffset: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioDarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = AppleMusicAmber, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("إزاحة المزامنة العامة (Sync Offset)", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Shift all lines and syllables by ±0.05s (50ms) or ±0.1s (100ms) to calibrate latency:",
                    color = TextGray,
                    fontSize = 12.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onApplyOffset(-100L) },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioDarkSurfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("-0.10s", color = TextWhite, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { onApplyOffset(-50L) },
                        colors = ButtonDefaults.buttonColors(containerColor = AppleMusicAmber),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("-0.05s", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { onApplyOffset(50L) },
                        colors = ButtonDefaults.buttonColors(containerColor = AppleMusicAmber),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+0.05s", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { onApplyOffset(100L) },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioDarkSurfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+0.10s", color = TextWhite, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = AppleMusicCyan)
            }
        }
    )
}

@Composable
private fun AddLineDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioDarkSurface,
        title = { Text("Add Lyric Line", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Enter the line text. Words will automatically be split into syllables for timing.",
                    color = TextGray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("e.g. Turn up the radio blast your stereo", color = TextDim) },
                    modifier = Modifier.fillMaxWidth().testTag("input_add_line"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleMusicRed,
                        unfocusedBorderColor = StudioDarkSurfaceVariant,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (text.isNotBlank()) onConfirm(text.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = AppleMusicRed),
                enabled = text.isNotBlank()
            ) {
                Text("Add Line", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}

@Composable
private fun AddSingerDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var isGroup by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf("#00D2FF") }
    val colorOptions = listOf("#00D2FF", "#10B981", "#F59E0B", "#A855F7", "#FA2D48", "#EC4899")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioDarkSurface,
        title = { Text("Add Singer / Agent", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Singer Name (e.g. Lead, Fergie, Choir)", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleMusicRed,
                        unfocusedBorderColor = StudioDarkSurfaceVariant,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_singer_name")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = { isGroup = false },
                        shape = RoundedCornerShape(8.dp),
                        color = if (!isGroup) AppleMusicRed else StudioDarkSurfaceVariant
                    ) {
                        Text(
                            text = "Person (v#)",
                            color = if (!isGroup) Color.White else TextGray,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            fontSize = 12.sp
                        )
                    }
                    Surface(
                        onClick = { isGroup = true },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isGroup) AppleMusicPurple else StudioDarkSurfaceVariant
                    ) {
                        Text(
                            text = "Group / Chorus",
                            color = if (isGroup) Color.White else TextGray,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            fontSize = 12.sp
                        )
                    }
                }

                Text("Singer Accent Color:", color = TextGray, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (colorHex in colorOptions) {
                        val parsed = Color(android.graphics.Color.parseColor(colorHex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parsed)
                                .clickable { selectedColor = colorHex }
                                .padding(2.dp)
                        ) {
                            if (selectedColor == colorHex) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.4f))
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) onConfirm(name.trim(), if (isGroup) "group" else "person", selectedColor)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppleMusicRed),
                enabled = name.isNotBlank()
            ) {
                Text("Save Singer", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}

@Composable
private fun ExportLyricsDialog(
    exportFormat: ExportFormat,
    onFormatChange: (ExportFormat) -> Unit,
    exportedContent: String,
    onDismiss: () -> Unit,
    onCopy: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioDarkSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Export Lyrics", color = TextWhite, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        onClick = { onFormatChange(ExportFormat.TTML) },
                        shape = RoundedCornerShape(6.dp),
                        color = if (exportFormat == ExportFormat.TTML) AppleMusicRed else StudioDarkSurfaceVariant
                    ) {
                        Text(
                            text = "TTML",
                            color = if (exportFormat == ExportFormat.TTML) Color.White else TextGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        onClick = { onFormatChange(ExportFormat.LRC) },
                        shape = RoundedCornerShape(6.dp),
                        color = if (exportFormat == ExportFormat.LRC) AppleMusicRed else StudioDarkSurfaceVariant
                    ) {
                        Text(
                            text = "LRC",
                            color = if (exportFormat == ExportFormat.LRC) Color.White else TextGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column {
                Text(
                    text = if (exportFormat == ExportFormat.TTML)
                        "Apple Music XML format with itunes:timing=\"Word\", multi-agents, and background vocal spans."
                    else
                        "Standard timestamped LRC lyrics format.",
                    color = TextGray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(8.dp)) {
                        item {
                            Text(
                                text = exportedContent,
                                color = TextWhite,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCopy(exportedContent) },
                colors = ButtonDefaults.buttonColors(containerColor = AppleMusicGreen)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy Output", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextGray)
            }
        }
    )
}
