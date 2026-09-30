package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.AgentInfo
import com.example.domain.model.KaraokeAlignment
import com.example.domain.model.KaraokePreviewStyle
import com.example.domain.model.LyricLine
import com.example.domain.model.LyricToken
import com.example.ui.theme.AppleMusicCyan
import com.example.ui.theme.AppleMusicGreen
import com.example.ui.theme.AppleMusicPurple
import com.example.ui.theme.AppleMusicRed
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioMotion
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite

@Composable
fun LyricsEnhanced(
    lines: List<LyricLine>,
    agents: List<AgentInfo>,
    currentTimeMs: Long,
    coverUri: String? = null,
    previewStyle: KaraokePreviewStyle = KaraokePreviewStyle.MASK_REVEAL,
    autoScroll: Boolean = true,
    fontScale: Float = 1.0f,
    onSeekTo: (Long) -> Unit,
    onShareSelectedLines: (List<LyricLine>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val selectedLines = remember { mutableStateListOf<LyricLine>() }
    var isSelectionMode by remember { mutableStateOf(false) }

    val activeLineIndex = lines.indexOfFirst { it.isActiveAt(currentTimeMs) }

    // Auto-scroll centering at ~38% screen height (Apple Music spec)
    LaunchedEffect(activeLineIndex, autoScroll, isSelectionMode) {
        if (autoScroll && !isSelectionMode && activeLineIndex >= 0) {
            listState.animateScrollToItem(
                index = (activeLineIndex - 1).coerceAtLeast(0),
                scrollOffset = -180
            )
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBg)
    ) {
        val screenHeight = maxHeight

        // Dynamic Apple Music Gradient & Cover Blur Mesh
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(46.dp)
                .alpha(0.62f)
        ) {
            if (!coverUri.isNullOrBlank()) {
                AsyncImage(
                    model = coverUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(320.dp)
                        .align(Alignment.TopStart)
                        .background(Brush.radialGradient(listOf(AppleMusicRed.copy(alpha = 0.4f), Color.Transparent)), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .align(Alignment.CenterEnd)
                        .background(Brush.radialGradient(listOf(AppleMusicPurple.copy(alpha = 0.4f), Color.Transparent)), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(340.dp)
                        .align(Alignment.BottomStart)
                        .background(Brush.radialGradient(listOf(AppleMusicCyan.copy(alpha = 0.3f), Color.Transparent)), CircleShape)
                )
            }
        }

        // Dark scrim overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f))
        )

        // Main Lyrics Column
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = (screenHeight * 0.30f).coerceAtLeast(80.dp),
                bottom = 220.dp,
                start = 24.dp,
                end = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(26.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("lyrics_enhanced_column")
        ) {
            itemsIndexed(lines, key = { _, line -> line.id }) { index, line ->
                val isActive = line.isActiveAt(currentTimeMs)
                val isPast = line.isPassedAt(currentTimeMs)
                val isSelected = selectedLines.contains(line)
                val agent = agents.firstOrNull { it.id == line.agentId }
                    ?: AgentInfo(id = line.agentId, name = line.agentId)

                EnhancedLyricLineRow(
                    line = line,
                    agent = agent,
                    currentTimeMs = currentTimeMs,
                    isActive = isActive,
                    isPast = isPast,
                    isSelected = isSelected,
                    isSelectionMode = isSelectionMode,
                    previewStyle = previewStyle,
                    fontScale = fontScale,
                    onClick = {
                        if (isSelectionMode) {
                            if (isSelected) {
                                selectedLines.remove(line)
                                if (selectedLines.isEmpty()) isSelectionMode = false
                            } else if (selectedLines.size < 5) {
                                selectedLines.add(line)
                            }
                        } else {
                            onSeekTo(line.beginMs)
                        }
                    },
                    onLongClick = {
                        isSelectionMode = true
                        if (!selectedLines.contains(line)) {
                            selectedLines.add(line)
                        }
                    }
                )
            }
        }

        // Selection Share Floating Bar
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppleMusicGreen),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedLines.size}/5 lines selected",
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Button(
                        onClick = {
                            onShareSelectedLines(selectedLines.toList())
                            isSelectionMode = false
                            selectedLines.clear()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppleMusicGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share Quote", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EnhancedLyricLineRow(
    line: LyricLine,
    agent: AgentInfo,
    currentTimeMs: Long,
    isActive: Boolean,
    isPast: Boolean,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    previewStyle: KaraokePreviewStyle,
    fontScale: Float,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val lineScale by animateFloatAsState(
        targetValue = if (isActive) 1.05f else 1.0f,
        animationSpec = StudioMotion.expressiveSpatialSpec(),
        label = "lineScale"
    )

    val lineAlpha by animateFloatAsState(
        targetValue = when {
            isActive -> 1.0f
            isPast -> 0.72f
            else -> 0.38f
        },
        animationSpec = StudioMotion.expressiveEffectsSpec(),
        label = "lineAlpha"
    )

    val alignment = when (agent.alignment) {
        KaraokeAlignment.START -> Alignment.Start
        KaraokeAlignment.END -> Alignment.End
        KaraokeAlignment.CENTER -> Alignment.CenterHorizontally
    }

    val textAlign = when (agent.alignment) {
        KaraokeAlignment.START -> TextAlign.Start
        KaraokeAlignment.END -> TextAlign.End
        KaraokeAlignment.CENTER -> TextAlign.Center
    }

    val agentColor = try {
        Color(android.graphics.Color.parseColor(agent.colorHex))
    } catch (_: Exception) {
        AppleMusicRed
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(lineScale)
            .alpha(lineAlpha)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) AppleMusicCyan.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 6.dp),
        horizontalAlignment = alignment
    ) {
        // Singer badge & song part
        if (agent.name.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = agentColor.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = agent.name.uppercase(),
                        color = agentColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                if (line.songPart.name != "VERSE") {
                    Text(
                        text = line.songPart.displayName,
                        color = TextDim,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Syllable tokens flow
        FlowRow(
            horizontalArrangement = when (agent.alignment) {
                KaraokeAlignment.START -> Arrangement.Start
                KaraokeAlignment.END -> Arrangement.End
                KaraokeAlignment.CENTER -> Arrangement.Center
            },
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            for (token in line.tokens) {
                EnhancedTokenWord(
                    token = token,
                    currentTimeMs = currentTimeMs,
                    isLineActive = isActive,
                    previewStyle = previewStyle,
                    agentColor = agentColor,
                    fontScale = fontScale
                )
            }
        }

        // Subtitle Translation / Romanization
        if (!line.translation.isNullOrBlank()) {
            Text(
                text = line.translation,
                color = TextGray.copy(alpha = 0.8f),
                fontSize = (14.3f * fontScale).sp,
                fontStyle = FontStyle.Italic,
                textAlign = textAlign,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun EnhancedTokenWord(
    token: LyricToken,
    currentTimeMs: Long,
    isLineActive: Boolean,
    previewStyle: KaraokePreviewStyle,
    agentColor: Color,
    fontScale: Float
) {
    val isActive = token.isActiveAt(currentTimeMs)
    val isPast = token.isPassedAt(currentTimeMs)
    val progress = token.progressAt(currentTimeMs)

    // Base font sizes: headline 26sp for lead, 21.3sp for background
    val mainFontSize = (26f * fontScale).sp
    val bgFontSize = (21.3f * fontScale).sp

    val tokenScale by animateFloatAsState(
        targetValue = if (isActive) 1.08f else 1.0f,
        animationSpec = StudioMotion.expressiveSpatialSpec(),
        label = "wordScale"
    )

    Box(
        modifier = Modifier
            .scale(tokenScale)
            .padding(horizontal = 3.dp, vertical = 2.dp)
    ) {
        if (token.isBackground) {
            // Background Vocals (x-bg): 75% size (21.3sp), 0.55 opacity, lighter tint, italic
            val bgAlpha by animateFloatAsState(
                targetValue = if (isActive) 1.0f else if (isPast) 0.85f else 0.55f,
                animationSpec = StudioMotion.expressiveEffectsSpec(),
                label = "bgAlpha"
            )
            Text(
                text = token.text,
                color = if (isActive) agentColor else Color.White.copy(alpha = bgAlpha),
                fontSize = bgFontSize,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                fontStyle = FontStyle.Italic
            )
        } else {
            when (previewStyle) {
                KaraokePreviewStyle.MASK_REVEAL -> {
                    // Apple Music Progressive Mask Reveal:
                    // Dimmed base text beneath + vibrant color text clipped by animated progress!
                    val animatedProgress by animateFloatAsState(
                        targetValue = if (isPast) 1.0f else if (isActive) progress else 0.0f,
                        animationSpec = StudioMotion.expressiveSpatialSpec(),
                        label = "maskProgress"
                    )

                    Box {
                        // Dimmed base layer
                        Text(
                            text = token.text,
                            color = Color.White.copy(alpha = if (isPast) 0.9f else 0.40f),
                            fontSize = mainFontSize,
                            fontWeight = FontWeight.Bold
                        )

                        // Mask reveal active layer
                        if (animatedProgress > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress.coerceIn(0.01f, 1f))
                                    .clipToBounds()
                            ) {
                                Text(
                                    text = token.text,
                                    color = if (isActive) Color.White else Color.White.copy(alpha = 0.9f),
                                    fontSize = mainFontSize,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                KaraokePreviewStyle.COLOR_CHANGE -> {
                    val color by animateColorAsState(
                        targetValue = if (isActive) agentColor else if (isPast) Color.White else Color.White.copy(alpha = 0.40f),
                        animationSpec = StudioMotion.expressiveEffectsSpec(),
                        label = "colorPop"
                    )
                    Text(
                        text = token.text,
                        color = color,
                        fontSize = mainFontSize,
                        fontWeight = if (isActive || isPast) FontWeight.Bold else FontWeight.Medium
                    )
                }

                KaraokePreviewStyle.OPACITY_WIPE -> {
                    val alpha by animateFloatAsState(
                        targetValue = if (isActive || isPast) 1.0f else 0.40f,
                        animationSpec = StudioMotion.expressiveEffectsSpec(),
                        label = "opacityWipe"
                    )
                    Text(
                        text = token.text,
                        color = Color.White.copy(alpha = alpha),
                        fontSize = mainFontSize,
                        fontWeight = if (isActive || isPast) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
