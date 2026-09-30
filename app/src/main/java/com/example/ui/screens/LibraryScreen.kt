package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.LyricsProject
import com.example.ui.library.AddSongBottomSheet
import com.example.ui.theme.AppleMusicAmber
import com.example.ui.theme.AppleMusicCyan
import com.example.ui.theme.AppleMusicGreen
import com.example.ui.theme.AppleMusicPurple
import com.example.ui.theme.AppleMusicRed
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.theme.StudioDarkSurfaceVariant
import com.example.ui.theme.StudioMotion
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite

@Composable
fun LibraryScreen(
    projects: List<LyricsProject>,
    onSelectProject: (LyricsProject) -> Unit,
    onPreviewProject: (LyricsProject) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToYtMusic: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onPickFromDevice: () -> Unit,
    onImportDirectLink: (url: String, title: String, artist: String) -> Unit,
    onCreateManualProject: (String, String, String) -> Unit,
    onDeleteProject: (String) -> Unit,
    onRecordVoiceTranscribe: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterCompletedOnly by remember { mutableStateOf(false) }

    var showAddSheet by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }

    val filteredProjects = projects.filter { proj ->
        val matchesQuery = proj.title.contains(searchQuery, ignoreCase = true) ||
                proj.artist.contains(searchQuery, ignoreCase = true)
        val matchesFilter = !filterCompletedOnly || proj.completionPercent >= 80
        matchesQuery && matchesFilter
    }

    Scaffold(
        containerColor = StudioDarkBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = AppleMusicRed,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_new_project")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Track / Lyrics")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Header: Icon + Title + Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(AppleMusicRed, AppleMusicPurple)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "TTML Studio",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Apple Music Karaoke Editor",
                            fontSize = 12.sp,
                            color = AppleMusicCyan
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier.testTag("btn_top_search_import")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Search LRCLIB",
                            tint = AppleMusicCyan
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("btn_top_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextGray
                        )
                    }
                }
            }

            // Search and Filter Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search songs, artists...", color = TextDim) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextGray)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_projects_input"),
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

            // Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { filterCompletedOnly = false },
                    shape = RoundedCornerShape(8.dp),
                    color = if (!filterCompletedOnly) AppleMusicRed.copy(alpha = 0.2f) else StudioDarkSurfaceVariant
                ) {
                    Text(
                        text = "All (${projects.size})",
                        color = if (!filterCompletedOnly) AppleMusicRed else TextGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    onClick = { filterCompletedOnly = true },
                    shape = RoundedCornerShape(8.dp),
                    color = if (filterCompletedOnly) AppleMusicGreen.copy(alpha = 0.2f) else StudioDarkSurfaceVariant
                ) {
                    Text(
                        text = "Synced (${projects.count { it.completionPercent >= 80 }})",
                        color = if (filterCompletedOnly) AppleMusicGreen else TextGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Projects List with Staggered Slide Animation
            if (filteredProjects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = TextDim,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No projects yet" else "No matches for \"$searchQuery\"",
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap (+) to import an audio track or fetch lyrics",
                            color = TextGray,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAddSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AppleMusicRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Track / Lyrics", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("project_list")
                ) {
                    itemsIndexed(filteredProjects, key = { _, it -> it.id }) { index, project ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(250, delayMillis = (index * 40).coerceAtMost(300))) +
                                    slideInVertically(
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        ),
                                        initialOffsetY = { it / 4 }
                                    )
                        ) {
                            ProjectItemCard(
                                project = project,
                                onEdit = { onSelectProject(project) },
                                onPreview = { onPreviewProject(project) },
                                onDelete = { onDeleteProject(project.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // 5-Option Modal Bottom Sheet
    if (showAddSheet) {
        AddSongBottomSheet(
            onDismiss = { showAddSheet = false },
            onPickFromDevice = onPickFromDevice,
            onImportFromYtMusic = onNavigateToYtMusic,
            onImportFromLink = { showLinkDialog = true },
            onCreateManual = { showManualDialog = true },
            onRecordVoiceTranscribe = onRecordVoiceTranscribe
        )
    }

    // Direct Link Dialog
    if (showLinkDialog) {
        DirectLinkDialog(
            onDismiss = { showLinkDialog = false },
            onConfirm = { url, title, artist ->
                onImportDirectLink(url, title, artist)
                showLinkDialog = false
            }
        )
    }

    // Manual Creation Dialog
    if (showManualDialog) {
        ManualProjectDialog(
            onDismiss = { showManualDialog = false },
            onConfirm = { title, artist, lang ->
                onCreateManualProject(title, artist, lang)
                showManualDialog = false
            }
        )
    }
}

@Composable
private fun ProjectItemCard(
    project: LyricsProject,
    onEdit: () -> Unit,
    onPreview: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .testTag("project_card_${project.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardBg)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cover Art / Icon + Title & Artist
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(StudioDarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!project.coverUri.isNullOrBlank()) {
                            AsyncImage(
                                model = project.coverUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = AppleMusicRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = project.title,
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (project.artist.isNotBlank()) project.artist else "Unknown Artist",
                            color = TextGray,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                }

                // Action Buttons: Preview Karaoke, Edit, Delete
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onPreview,
                        modifier = Modifier.size(36.dp).testTag("btn_preview_${project.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Preview Karaoke",
                            tint = AppleMusicRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp).testTag("btn_edit_${project.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Project",
                            tint = AppleMusicCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp).testTag("btn_delete_${project.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = TextDim,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StudioDarkSurfaceVariant
                ) {
                    Text(
                        text = "${project.lines.size} lines",
                        color = TextGray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StudioDarkSurfaceVariant
                ) {
                    Text(
                        text = "${project.agents.size} singers",
                        color = AppleMusicPurple,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StudioDarkSurfaceVariant
                ) {
                    Text(
                        text = project.language.uppercase(),
                        color = AppleMusicCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "${project.completionPercent}% Synced",
                    color = if (project.completionPercent >= 80) AppleMusicGreen else AppleMusicAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { project.completionPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (project.completionPercent >= 80) AppleMusicGreen else AppleMusicRed,
                trackColor = StudioDarkSurfaceVariant
            )
        }
    }
}

@Composable
private fun DirectLinkDialog(
    onDismiss: () -> Unit,
    onConfirm: (url: String, title: String, artist: String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioDarkSurface,
        title = { Text("Import from Direct Audio URL", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Direct Audio URL (MP3/M4A/Stream) *", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleMusicGreen,
                        unfocusedBorderColor = StudioDarkSurfaceVariant,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_link_url")
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Track Title", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleMusicGreen,
                        unfocusedBorderColor = StudioDarkSurfaceVariant,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_link_title")
                )
                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist Name", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleMusicGreen,
                        unfocusedBorderColor = StudioDarkSurfaceVariant,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_link_artist")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (url.isNotBlank()) onConfirm(url.trim(), title.trim(), artist.trim()) },
                enabled = url.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AppleMusicGreen)
            ) {
                Text("Import Link", color = Color.Black, fontWeight = FontWeight.Bold)
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
private fun ManualProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, artist: String, language: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("en") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioDarkSurface,
        title = { Text("Create Lyrics Project", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Song Title *", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleMusicRed,
                        unfocusedBorderColor = StudioDarkSurfaceVariant,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_new_title")
                )
                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist Name", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleMusicRed,
                        unfocusedBorderColor = StudioDarkSurfaceVariant,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_new_artist")
                )
                OutlinedTextField(
                    value = language,
                    onValueChange = { language = it },
                    label = { Text("Language Code (e.g. en, ar, fr)", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleMusicRed,
                        unfocusedBorderColor = StudioDarkSurfaceVariant,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_new_lang")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onConfirm(title.trim(), artist.trim(), language.trim()) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AppleMusicRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Create Project", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}
