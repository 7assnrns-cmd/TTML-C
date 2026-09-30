package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.remote.FetchedLyricsResult
import com.example.domain.model.LyricLine
import com.example.domain.parser.TTMLExporter
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

data class PendingSongCandidate(
    val title: String,
    val artist: String,
    val album: String = "",
    val coverUrl: String = "",
    val durationMs: Long = 180000L,
    val audioUri: String? = null,
    val fetchedLyrics: FetchedLyricsResult? = null,
    val parsedLines: List<LyricLine> = emptyList()
)

@Composable
fun LyricsConfirmationScreen(
    candidate: PendingSongCandidate,
    onConfirm: () -> Unit,
    onReject: () -> Unit,
    onTryAnotherSource: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val scrollState = rememberScrollState()
    val isWordSynced = candidate.parsedLines.any { it.tokens.size > 1 && it.tokens.any { t -> t.beginMs > 0 } }
    val isLineSynced = candidate.parsedLines.any { it.beginMs > 0 }

    Scaffold(
        containerColor = StudioDarkBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("btn_confirm_back")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }
                Text(
                    text = "تأكيد الكلمات المستخرجة",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(AppleMusicCyan.copy(alpha = 0.2f), AppleMusicPurple.copy(alpha = 0.2f))
                        )
                    )
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(AppleMusicCyan, AppleMusicPurple)),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AppleMusicCyan,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "🔍 وجدنا الكلمات!",
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Track & Metadata Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (candidate.coverUrl.isNotBlank()) {
                            AsyncImage(
                                model = candidate.coverUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(AppleMusicRed, AppleMusicPurple)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = candidate.title.ifBlank { "Untitled Track" },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = candidate.artist.ifBlank { "Unknown Artist" },
                                fontSize = 14.sp,
                                color = AppleMusicCyan,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (candidate.album.isNotBlank()) {
                                Text(
                                    text = candidate.album,
                                    fontSize = 12.sp,
                                    color = TextDim,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Metadata Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "المصدر:", color = TextDim, fontSize = 12.sp)
                            Text(
                                text = candidate.fetchedLyrics?.source?.displayName ?: "LRCLIB",
                                color = AppleMusicGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column {
                            Text(text = "نوع التزامن:", color = TextDim, fontSize = 12.sp)
                            val typeLabel = if (isWordSynced) "متزامن كلمة-بكلمة ✅" else if (isLineSynced) "متزامن سطر-بسطر ⏱️" else "نص عادي 📝"
                            Text(
                                text = typeLabel,
                                color = if (isWordSynced) AppleMusicCyan else TextWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column {
                            Text(text = "عدد الأسطر:", color = TextDim, fontSize = 12.sp)
                            Text(
                                text = "${candidate.parsedLines.size} سطر",
                                color = TextWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Preview Box (First 5-8 Lines)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StudioDarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "── معاينة الكلمات (أول 5 أسطر) ──",
                        color = TextDim,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val previewLines = candidate.parsedLines.take(6)
                    if (previewLines.isEmpty()) {
                        Text(
                            text = "لا توجد أسطر متاحة للمعاينة",
                            color = TextGray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        previewLines.forEachIndexed { idx, line ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = TTMLExporter.formatTime(line.beginMs),
                                    fontSize = 12.sp,
                                    color = AppleMusicCyan,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(StudioDarkSurfaceVariant)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                                Text(
                                    text = line.fullText.ifBlank { "(موسيقى / فاصل)" },
                                    fontSize = 14.sp,
                                    color = TextWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons (3 Buttons per Specification)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Button 1: Confirm & Edit (Primary)
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_confirm_lyrics_yes"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppleMusicGreen)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "✅ نعم، ابدأ التحرير والمزامنة",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Button 2: Try another source
                OutlinedButton(
                    onClick = onTryAnotherSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_confirm_try_another"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = AppleMusicCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🔄 البحث من مصدر آخر (Musixmatch / Genius)",
                        fontSize = 14.sp,
                        color = AppleMusicCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Button 3: Reject / Edit query
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_confirm_reject"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = AppleMusicRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "❌ لا، هذه ليست الكلمات (تعديل البحث)",
                        fontSize = 14.sp,
                        color = AppleMusicRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
