package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AgentInfo
import com.example.domain.model.KaraokeAlignment
import com.example.domain.model.KaraokePreviewStyle
import com.example.domain.model.LyricLine
import com.example.domain.model.LyricToken
import com.example.ui.theme.AppleMusicCyan
import com.example.ui.theme.AppleMusicPurple
import com.example.ui.theme.AppleMusicRed
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite

@Composable
fun LyricsPreviewView(
    lines: List<LyricLine>,
    agents: List<AgentInfo>,
    currentTimeMs: Long,
    previewStyle: KaraokePreviewStyle = KaraokePreviewStyle.MASK_REVEAL,
    autoScroll: Boolean = true,
    fontScale: Float = 1.0f,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Find currently active line
    val activeLineIndex = lines.indexOfFirst { it.isActiveAt(currentTimeMs) }

    LaunchedEffect(activeLineIndex, autoScroll) {
        if (autoScroll && activeLineIndex >= 0) {
            listState.animateScrollToItem(
                index = (activeLineIndex - 1).coerceAtLeast(0),
                scrollOffset = -150
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBg)
    ) {
        // Apple Music dynamic blurred gradient background mesh
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .align(Alignment.TopStart)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(AppleMusicRed.copy(alpha = 0.35f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .align(Alignment.CenterEnd)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(AppleMusicPurple.copy(alpha = 0.35f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.BottomStart)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(AppleMusicCyan.copy(alpha = 0.25f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Lyrics Scrollable List
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(top = 100.dp, bottom = 200.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("karaoke_lyrics_list")
        ) {
            itemsIndexed(lines, key = { _, line -> line.id }) { index, line ->
                val isActiveLine = line.isActiveAt(currentTimeMs)
                val isPastLine = line.isPassedAt(currentTimeMs)
                val agent = agents.firstOrNull { it.id == line.agentId }
                    ?: AgentInfo(id = line.agentId, name = line.agentId)

                KaraokeLineItem(
                    line = line,
                    agent = agent,
                    currentTimeMs = currentTimeMs,
                    isActive = isActiveLine,
                    isPast = isPastLine,
                    previewStyle = previewStyle,
                    fontScale = fontScale,
                    onClick = { onSeekTo(line.beginMs) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KaraokeLineItem(
    line: LyricLine,
    agent: AgentInfo,
    currentTimeMs: Long,
    isActive: Boolean,
    isPast: Boolean,
    previewStyle: KaraokePreviewStyle,
    fontScale: Float,
    onClick: () -> Unit
) {
    val lineScale by animateFloatAsState(
        targetValue = if (isActive) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f),
        label = "line_scale"
    )

    val lineAlpha by animateFloatAsState(
        targetValue = when {
            isActive -> 1.0f
            isPast -> 0.70f
            else -> 0.35f
        },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f),
        label = "line_alpha"
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
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        // Singer badge if line is active or singer differs
        if (agent.name.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 4.dp)
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

        // Flow of Word Spans (Word-by-word karaoke synchronization)
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
                KaraokeTokenItem(
                    token = token,
                    currentTimeMs = currentTimeMs,
                    isLineActive = isActive,
                    previewStyle = previewStyle,
                    agentColor = agentColor,
                    fontScale = fontScale
                )
            }
        }

        // Translation or Romanization subtitle
        if (!line.translation.isNullOrBlank()) {
            Text(
                text = line.translation,
                color = TextGray.copy(alpha = 0.8f),
                fontSize = (14 * fontScale).sp,
                fontStyle = FontStyle.Italic,
                textAlign = textAlign,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun KaraokeTokenItem(
    token: LyricToken,
    currentTimeMs: Long,
    isLineActive: Boolean,
    previewStyle: KaraokePreviewStyle,
    agentColor: Color,
    fontScale: Float
) {
    val isActive = token.isActiveAt(currentTimeMs)
    val isPast = token.isPassedAt(currentTimeMs)

    val baseFontSize = if (token.isBackground) 18.sp else 26.sp
    val scaledFontSize = (baseFontSize.value * fontScale).sp

    val tokenScale by animateFloatAsState(
        targetValue = if (isActive) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "token_scale"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isActive -> Color.White
            isPast -> Color.White.copy(alpha = 0.9f)
            else -> TextGray.copy(alpha = 0.4f)
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "text_color"
    )

    Box(
        modifier = Modifier
            .scale(tokenScale)
            .padding(horizontal = 3.dp, vertical = 2.dp)
    ) {
        if (token.isBackground) {
            // Background vocal styling: 75% size + italic + distinctive soft tint
            Text(
                text = token.text,
                color = if (isActive) agentColor else textColor.copy(alpha = 0.65f),
                fontSize = (scaledFontSize.value * 0.82f).sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                fontStyle = FontStyle.Italic
            )
        } else {
            when (previewStyle) {
                KaraokePreviewStyle.MASK_REVEAL -> {
                    if (isActive && isLineActive) {
                        // Word progress highlight with glow
                        Text(
                            text = token.text,
                            color = Color.White,
                            fontSize = scaledFontSize,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = token.text,
                            color = textColor,
                            fontSize = scaledFontSize,
                            fontWeight = if (isActive || isPast) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
                KaraokePreviewStyle.COLOR_CHANGE -> {
                    Text(
                        text = token.text,
                        color = if (isActive) agentColor else textColor,
                        fontSize = scaledFontSize,
                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium
                    )
                }
                KaraokePreviewStyle.OPACITY_WIPE -> {
                    Text(
                        text = token.text,
                        color = textColor,
                        fontSize = scaledFontSize,
                        fontWeight = if (isActive || isPast) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
