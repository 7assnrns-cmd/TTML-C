package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SampleData
import com.example.data.remote.LrclibTrack
import com.example.domain.model.LyricsProject
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
fun SearchImportScreen(
    searchResults: List<LrclibTrack>,
    isSearching: Boolean,
    searchError: String?,
    onBack: () -> Unit,
    onSearch: (String) -> Unit,
    onImportTrack: (LrclibTrack) -> Unit,
    onImportRaw: (title: String, content: String) -> Unit,
    onLoadSample: (LyricsProject) -> Unit
) {
    BackHandler { onBack() }
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    // Raw Paste State
    var rawTitle by remember { mutableStateOf("") }
    var rawContent by remember { mutableStateOf("") }

    Scaffold(
        containerColor = StudioDarkBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("btn_import_back")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Import & Search Lyrics",
                        color = TextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "LRCLIB API • TTML • LRC • Raw Text",
                        color = AppleMusicCyan,
                        fontSize = 11.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs: LRCLIB Search vs Paste Text vs Demo Samples
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = StudioDarkSurface,
                contentColor = AppleMusicRed,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = AppleMusicRed
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("LRCLIB Search", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Paste XML / LRC", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Demo Songs", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // LRCLIB Search Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search song or artist on LRCLIB...", color = TextDim) },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = TextGray)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_lrclib_search"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppleMusicRed,
                                    unfocusedBorderColor = StudioDarkSurfaceVariant,
                                    focusedContainerColor = StudioDarkSurface,
                                    unfocusedContainerColor = StudioDarkSurface,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                ),
                                singleLine = true
                            )

                            Button(
                                onClick = { onSearch(searchQuery.trim()) },
                                enabled = searchQuery.isNotBlank() && !isSearching,
                                colors = ButtonDefaults.buttonColors(containerColor = AppleMusicRed),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(54.dp).testTag("btn_do_search")
                            ) {
                                if (isSearching) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Search")
                                }
                            }
                        }

                        if (!searchError.isNullOrBlank()) {
                            Text(
                                text = "Notice: $searchError",
                                color = AppleMusicAmber,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (searchResults.isEmpty() && !isSearching) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = TextDim,
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Search millions of synced lyrics on LRCLIB",
                                        color = TextGray,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize().testTag("lrclib_results_list")
                            ) {
                                items(searchResults, key = { it.id }) { track ->
                                    LrclibResultCard(track = track, onImport = { onImportTrack(track) })
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Paste Raw Lyrics Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = rawTitle,
                            onValueChange = { rawTitle = it },
                            label = { Text("Song Title *", color = TextGray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppleMusicRed,
                                unfocusedBorderColor = StudioDarkSurfaceVariant,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("input_raw_title")
                        )

                        OutlinedTextField(
                            value = rawContent,
                            onValueChange = { rawContent = it },
                            label = { Text("Paste TTML XML, LRC, or Plain text", color = TextGray) },
                            placeholder = { Text("<tt xmlns=\"...\"> or [00:12.34] lyric line...", color = TextDim) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppleMusicRed,
                                unfocusedBorderColor = StudioDarkSurfaceVariant,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("input_raw_content")
                        )

                        Button(
                            onClick = {
                                if (rawTitle.isNotBlank() && rawContent.isNotBlank()) {
                                    onImportRaw(rawTitle.trim(), rawContent.trim())
                                }
                            },
                            enabled = rawTitle.isNotBlank() && rawContent.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = AppleMusicGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_import_raw_confirm")
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Parse & Open in Editor", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                2 -> {
                    // Demo Songs Tab
                    val samples = remember { SampleData.getSampleProjects() }
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = "Pre-configured sample projects with Apple Music word-by-word timing, multi-agents, and background vocal spans:",
                                color = TextGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(samples) { sample ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onLoadSample(sample) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = StudioCardBg)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = sample.title,
                                            color = TextWhite,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${sample.artist} • ${sample.album}",
                                            color = TextGray,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${sample.lines.size} lines • ${sample.agents.size} singers • ${sample.language.uppercase()}",
                                            color = AppleMusicCyan,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Button(
                                        onClick = { onLoadSample(sample) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AppleMusicRed),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Open", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LrclibResultCard(
    track: LrclibTrack,
    onImport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.displayTitle,
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "${track.displayArtist}${if (track.displayAlbum.isNotBlank()) " • ${track.displayAlbum}" else ""}",
                    color = TextGray,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (track.hasSynced) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AppleMusicGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Synced LRC",
                                color = AppleMusicGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = StudioDarkSurfaceVariant
                        ) {
                            Text(
                                text = "Plain Lyrics",
                                color = TextDim,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (track.duration != null) {
                        val durMs = (track.duration * 1000).toLong()
                        val min = durMs / 60000
                        val sec = (durMs % 60000) / 1000
                        Text(
                            text = String.format(Locale.US, "%02d:%02d", min, sec),
                            color = TextDim,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Button(
                onClick = onImport,
                colors = ButtonDefaults.buttonColors(containerColor = AppleMusicCyan),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Import", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
