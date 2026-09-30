package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LyricLine
import com.example.domain.model.LyricToken
import com.example.ui.theme.AppleMusicCyan
import com.example.ui.theme.AppleMusicRed
import com.example.ui.theme.StudioDarkSurfaceVariant
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WaveformMarkerLine
import com.example.ui.theme.WaveformMarkerWord
import com.example.ui.theme.WaveformPlayed
import com.example.ui.theme.WaveformUnplayed
import java.util.Locale

@Composable
fun WaveformTimeline(
    waveformPeaks: List<Float>,
    currentPositionMs: Long,
    durationMs: Long,
    activeLine: LyricLine?,
    activeToken: LyricToken?,
    zoomLevel: Float,
    onSeek: (Long) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onOffsetStep: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDuration = durationMs.coerceAtLeast(1000L)
    val progress = (currentPositionMs.toFloat() / totalDuration).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(StudioDarkSurfaceVariant)
            .padding(12.dp)
    ) {
        // Time & Zoom Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position / Duration
            Text(
                text = "${formatTimeWithMillis(currentPositionMs)} / ${formatTimeWithMillis(totalDuration)}",
                color = TextWhite,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag("time_display")
            )

            // Zoom & Offset controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onOffsetStep(-100L) },
                    modifier = Modifier.size(32.dp).testTag("offset_minus_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Rewind 0.1s",
                        tint = TextGray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = "±0.1s",
                        color = TextDim,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                IconButton(
                    onClick = { onOffsetStep(100L) },
                    modifier = Modifier.size(32.dp).testTag("offset_plus_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Forward 0.1s",
                        tint = TextGray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onZoomOut,
                    modifier = Modifier.size(32.dp).testTag("zoom_out_btn"),
                    enabled = zoomLevel > 1.0f
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = if (zoomLevel > 1.0f) TextWhite else TextDim,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "${String.format(Locale.US, "%.1f", zoomLevel)}x",
                    color = AppleMusicCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onZoomIn,
                    modifier = Modifier.size(32.dp).testTag("zoom_in_btn"),
                    enabled = zoomLevel < 5.0f
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = if (zoomLevel < 5.0f) TextWhite else TextDim,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Active context badge
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val lineDesc = if (activeLine != null) {
                "Line: ${activeLine.fullText.take(24)}..."
            } else {
                "No line active"
            }
            val wordDesc = if (activeToken != null) {
                "Word: \"${activeToken.text}\""
            } else {
                ""
            }
            Text(
                text = lineDesc,
                color = AppleMusicCyan,
                fontSize = 11.sp,
                maxLines = 1
            )
            if (wordDesc.isNotBlank()) {
                Text(
                    text = wordDesc,
                    color = AppleMusicRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        // Waveform Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F121F))
                .pointerInput(totalDuration, zoomLevel) {
                    detectTapGestures { offset ->
                        val tapRatio = (offset.x / size.width).coerceIn(0f, 1f)
                        val targetMs = (tapRatio * totalDuration).toLong()
                        onSeek(targetMs)
                    }
                }
                .pointerInput(totalDuration, zoomLevel) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val dragRatio = (change.position.x / size.width).coerceIn(0f, 1f)
                        val targetMs = (dragRatio * totalDuration).toLong()
                        onSeek(targetMs)
                    }
                }
                .testTag("waveform_canvas")
        ) {
            val peaks = remember(waveformPeaks) {
                if (waveformPeaks.isEmpty()) List(200) { 0.5f } else waveformPeaks
            }

            Canvas(modifier = Modifier.matchParentSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val midY = canvasHeight / 2f

                // Draw background grid lines (seconds intervals)
                val totalSeconds = (totalDuration / 1000).toInt()
                val secInterval = if (totalSeconds > 60) 10 else 5
                for (s in 0..totalSeconds step secInterval) {
                    val secX = (s * 1000f / totalDuration) * canvasWidth
                    drawLine(
                        color = Color(0x22FFFFFF),
                        start = Offset(secX, 0f),
                        end = Offset(secX, canvasHeight),
                        strokeWidth = 1f
                    )
                }

                // Draw active line highlighted region
                if (activeLine != null && activeLine.durationMs > 0) {
                    val startX = (activeLine.beginMs.toFloat() / totalDuration) * canvasWidth
                    val endX = (activeLine.endMs.toFloat() / totalDuration) * canvasWidth
                    drawRect(
                        color = Color(0x3300D2FF),
                        topLeft = Offset(startX, 0f),
                        size = Size((endX - startX).coerceAtLeast(2f), canvasHeight)
                    )
                }

                // Draw active word highlighted region
                if (activeToken != null && activeToken.durationMs > 0) {
                    val wStartX = (activeToken.beginMs.toFloat() / totalDuration) * canvasWidth
                    val wEndX = (activeToken.endMs.toFloat() / totalDuration) * canvasWidth
                    drawRect(
                        color = Color(0x66FA2D48),
                        topLeft = Offset(wStartX, 0f),
                        size = Size((wEndX - wStartX).coerceAtLeast(3f), canvasHeight)
                    )
                }

                // Draw Waveform bars
                val barCount = peaks.size
                val barWidth = (canvasWidth / barCount) * 0.7f
                val spacing = (canvasWidth / barCount) * 0.3f

                for (i in peaks.indices) {
                    val barX = i * (barWidth + spacing)
                    val barHeight = peaks[i] * (canvasHeight * 0.85f)
                    val isPast = (barX / canvasWidth) <= progress
                    val barColor = if (isPast) WaveformPlayed else WaveformUnplayed

                    drawLine(
                        color = barColor,
                        start = Offset(barX, midY - barHeight / 2f),
                        end = Offset(barX, midY + barHeight / 2f),
                        strokeWidth = barWidth.coerceAtLeast(1.5f)
                    )
                }

                // Draw active Line boundaries markers
                if (activeLine != null) {
                    val lStartX = (activeLine.beginMs.toFloat() / totalDuration) * canvasWidth
                    val lEndX = (activeLine.endMs.toFloat() / totalDuration) * canvasWidth
                    // Line start flag
                    drawLine(
                        color = WaveformMarkerLine,
                        start = Offset(lStartX, 0f),
                        end = Offset(lStartX, canvasHeight),
                        strokeWidth = 2f
                    )
                    // Line end flag
                    drawLine(
                        color = WaveformMarkerLine,
                        start = Offset(lEndX, 0f),
                        end = Offset(lEndX, canvasHeight),
                        strokeWidth = 2f
                    )
                }

                // Draw active Word boundary marker
                if (activeToken != null) {
                    val tokX = (activeToken.beginMs.toFloat() / totalDuration) * canvasWidth
                    val tokEndX = (activeToken.endMs.toFloat() / totalDuration) * canvasWidth
                    drawLine(
                        color = WaveformMarkerWord,
                        start = Offset(tokX, 4f),
                        end = Offset(tokX, canvasHeight - 4f),
                        strokeWidth = 2.5f
                    )
                    drawLine(
                        color = WaveformMarkerWord,
                        start = Offset(tokEndX, 4f),
                        end = Offset(tokEndX, canvasHeight - 4f),
                        strokeWidth = 2.5f
                    )
                }

                // Draw current Playhead cursor
                val playheadX = progress * canvasWidth
                drawLine(
                    color = Color.White,
                    start = Offset(playheadX, 0f),
                    end = Offset(playheadX, canvasHeight),
                    strokeWidth = 2.5f
                )
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(playheadX, 6.dp.toPx())
                )
                drawCircle(
                    color = AppleMusicRed,
                    radius = 3.dp.toPx(),
                    center = Offset(playheadX, 6.dp.toPx())
                )
            }
        }
    }
}

private fun formatTimeWithMillis(timeMs: Long): String {
    return com.example.domain.parser.TimeUtils.formatMMSSmmm(timeMs)
}
