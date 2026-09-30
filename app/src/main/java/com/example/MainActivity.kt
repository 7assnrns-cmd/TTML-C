package com.example

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.LyricsConfirmationScreen
import com.example.ui.screens.PreviewScreen
import com.example.ui.screens.SearchImportScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StudioDarkBg
import com.example.ui.viewmodel.StudioViewModel
import com.example.ui.ytmusic.YtMusicSearchScreen

enum class StudioScreen {
    LIBRARY,
    CONFIRM_LYRICS,
    EDITOR,
    PREVIEW,
    SEARCH_IMPORT,
    YT_MUSIC_SEARCH,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val viewModel: StudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                StudioApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun StudioApp(viewModel: StudioViewModel) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(StudioScreen.LIBRARY) }

    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val currentProject by viewModel.currentProject.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.audioManager.currentPositionMs.collectAsStateWithLifecycle()
    val isPlaying by viewModel.audioManager.isPlaying.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.audioManager.playbackSpeed.collectAsStateWithLifecycle()
    val waveformPeaks by viewModel.waveformPeaks.collectAsStateWithLifecycle()
    val zoomLevel by viewModel.zoomLevel.collectAsStateWithLifecycle()
    val activeLineIndex by viewModel.activeLineIndex.collectAsStateWithLifecycle()
    val activeTokenIndex by viewModel.activeTokenIndex.collectAsStateWithLifecycle()
    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val searchError by viewModel.searchError.collectAsStateWithLifecycle()
    val pendingCandidate by viewModel.pendingCandidate.collectAsStateWithLifecycle()

    LaunchedEffect(pendingCandidate) {
        if (pendingCandidate != null) {
            currentScreen = StudioScreen.CONFIRM_LYRICS
        }
    }

    var isRecordingAudio by remember { mutableStateOf(false) }
    var audioRecorder by remember { mutableStateOf<com.example.usecase.AudioRecorder?>(null) }

    val recordPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val recorder = com.example.usecase.AudioRecorder(context)
            audioRecorder = recorder
            val file = recorder.startRecording()
            isRecordingAudio = true
            Toast.makeText(context, "🎤 Recording audio... Speak or sing into mic!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Microphone permission required for audio transcription", Toast.LENGTH_LONG).show()
        }
    }

    // SAF Audio File Picker (MP3, M4A, WAV, FLAC)
    val pickAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            Toast.makeText(context, "Extracting audio and searching lyrics...", Toast.LENGTH_SHORT).show()
            viewModel.importFromDeviceAudio(context, uri)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = StudioDarkBg
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                StudioScreen.LIBRARY -> {
                    LibraryScreen(
                        projects = projects,
                        onSelectProject = { proj ->
                            viewModel.selectProject(proj)
                            currentScreen = StudioScreen.EDITOR
                        },
                        onPreviewProject = { proj ->
                            viewModel.selectProject(proj)
                            currentScreen = StudioScreen.PREVIEW
                        },
                        onNavigateToSearch = {
                            currentScreen = StudioScreen.SEARCH_IMPORT
                        },
                        onNavigateToYtMusic = {
                            currentScreen = StudioScreen.YT_MUSIC_SEARCH
                        },
                        onNavigateToSettings = {
                            currentScreen = StudioScreen.SETTINGS
                        },
                        onPickFromDevice = {
                            pickAudioLauncher.launch(
                                arrayOf(
                                    "audio/mpeg",
                                    "audio/mp4",
                                    "audio/m4a",
                                    "audio/wav",
                                    "audio/x-wav",
                                    "audio/flac",
                                    "audio/*"
                                )
                            )
                        },
                        onImportDirectLink = { url, title, artist ->
                            viewModel.importFromDirectLink(url, title, artist)
                            currentScreen = StudioScreen.EDITOR
                        },
                        onCreateManualProject = { title, artist, lang ->
                            viewModel.createNewProject(title, artist, lang)
                            currentScreen = StudioScreen.EDITOR
                        },
                        onDeleteProject = { id ->
                            viewModel.deleteProject(id)
                        },
                        onRecordVoiceTranscribe = {
                            recordPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    )

                    if (isRecordingAudio) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = {},
                            title = {
                                androidx.compose.material3.Text("🎤 Recording Audio...", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            },
                            text = {
                                androidx.compose.material3.Text("Speak or sing clearly into your microphone. Tap stop when finished to transcribe with Gemini AI.")
                            },
                            confirmButton = {
                                androidx.compose.material3.Button(
                                    onClick = {
                                        val recordedFile = audioRecorder?.stopRecording()
                                        isRecordingAudio = false
                                        if (recordedFile != null && recordedFile.exists()) {
                                            Toast.makeText(context, "Transcribing audio with Gemini AI...", Toast.LENGTH_LONG).show()
                                            viewModel.transcribeAudioRecord(recordedFile)
                                            currentScreen = StudioScreen.EDITOR
                                        }
                                    },
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.AppleMusicRed)
                                ) {
                                    androidx.compose.material3.Text("Stop & Transcribe with Gemini")
                                }
                            },
                            dismissButton = {
                                androidx.compose.material3.TextButton(
                                    onClick = {
                                        audioRecorder?.stopRecording()
                                        isRecordingAudio = false
                                    }
                                ) {
                                    androidx.compose.material3.Text("Cancel")
                                }
                            }
                        )
                    }
                }

                StudioScreen.CONFIRM_LYRICS -> {
                    val candidate = pendingCandidate
                    if (candidate != null) {
                        LyricsConfirmationScreen(
                            candidate = candidate,
                            onConfirm = {
                                viewModel.confirmPendingCandidate(candidate)
                                currentScreen = StudioScreen.EDITOR
                            },
                            onReject = {
                                viewModel.rejectPendingCandidate()
                                currentScreen = StudioScreen.EDITOR
                            },
                            onTryAnotherSource = {
                                viewModel.tryAnotherSourceForPending()
                            },
                            onBack = {
                                currentScreen = StudioScreen.LIBRARY
                            }
                        )
                    } else {
                        currentScreen = StudioScreen.LIBRARY
                    }
                }

                StudioScreen.EDITOR -> {
                    val proj = currentProject
                    if (proj != null) {
                        EditorScreen(
                            project = proj,
                            currentPositionMs = currentPositionMs,
                            isPlaying = isPlaying,
                            playbackSpeed = playbackSpeed,
                            waveformPeaks = waveformPeaks,
                            zoomLevel = zoomLevel,
                            activeLineIndex = activeLineIndex,
                            activeTokenIndex = activeTokenIndex,
                            canUndo = canUndo,
                            canRedo = canRedo,
                            onBack = {
                                viewModel.closeProject()
                                currentScreen = StudioScreen.LIBRARY
                            },
                            onNavigateToPreview = {
                                currentScreen = StudioScreen.PREVIEW
                            },
                            onPlayPause = { viewModel.audioManager.togglePlayPause() },
                            onSeek = { ms -> viewModel.audioManager.seekTo(ms) },
                            onStepOffset = { delta -> viewModel.audioManager.stepOffset(delta) },
                            onZoomIn = { viewModel.zoomIn() },
                            onZoomOut = { viewModel.zoomOut() },
                            onSetSpeed = { speed -> viewModel.audioManager.setPlaybackSpeed(speed) },
                            onUndo = { viewModel.undo() },
                            onRedo = { viewModel.redo() },
                            onSave = { viewModel.saveCurrentProject() },
                            onSelectLine = { lIdx -> viewModel.selectLine(lIdx) },
                            onSelectToken = { tIdx -> viewModel.selectToken(tIdx) },
                            onMarkLineStart = { viewModel.markLineStart() },
                            onMarkLineEnd = { viewModel.markLineEnd() },
                            onMarkWordStart = { viewModel.markWordStart() },
                            onMarkWordEnd = { viewModel.markWordEnd() },
                            onSplitWord = { viewModel.splitActiveWord() },
                            onAutoSyncLine = { viewModel.autoSyncLine() },
                            onApplySyncOffset = { delta -> viewModel.applyGlobalSyncOffset(delta) },
                            onToggleBackgroundVocal = { isBg -> viewModel.toggleBackgroundVocal(isBg) },
                            onSelectAgent = { agentId -> viewModel.setLineAgent(agentId) },
                            onAddNewAgent = { name, type, color -> viewModel.addNewAgent(name, type, color) },
                            onSelectSongPart = { part -> viewModel.setLineSongPart(part) },
                            onAddLine = { text -> viewModel.addLine(text) },
                            onDeleteLine = { lIdx -> viewModel.deleteLine(lIdx) },
                            onUpdateTranslation = { lIdx, tr -> viewModel.updateLineTranslation(lIdx, tr) },
                            onExportTtml = { viewModel.exportCurrentProject(com.example.domain.model.ExportFormat.TTML) },
                            onExportLrc = { viewModel.exportCurrentProject(com.example.domain.model.ExportFormat.LRC) }
                        )
                    } else {
                        currentScreen = StudioScreen.LIBRARY
                    }
                }

                StudioScreen.PREVIEW -> {
                    val proj = currentProject
                    if (proj != null) {
                        PreviewScreen(
                            project = proj,
                            currentPositionMs = currentPositionMs,
                            isPlaying = isPlaying,
                            playbackSpeed = playbackSpeed,
                            previewStyle = settings.previewStyle,
                            onBack = {
                                currentScreen = StudioScreen.EDITOR
                            },
                            onPlayPause = { viewModel.audioManager.togglePlayPause() },
                            onSeek = { ms -> viewModel.audioManager.seekTo(ms) },
                            onCycleSpeed = { viewModel.cycleSpeed() },
                            onSelectPreviewStyle = { style -> viewModel.setPreviewStyle(style) }
                        )
                    } else {
                        currentScreen = StudioScreen.LIBRARY
                    }
                }

                StudioScreen.YT_MUSIC_SEARCH -> {
                    YtMusicSearchScreen(
                        onBack = { currentScreen = StudioScreen.LIBRARY },
                        onImportTrack = { ytTrack ->
                            Toast.makeText(context, "Importing ${ytTrack.title} & fetching lyrics...", Toast.LENGTH_SHORT).show()
                            viewModel.importFromYtMusicTrack(ytTrack)
                            currentScreen = StudioScreen.EDITOR
                        }
                    )
                }

                StudioScreen.SEARCH_IMPORT -> {
                    SearchImportScreen(
                        searchResults = searchResults,
                        isSearching = isSearching,
                        searchError = searchError,
                        onBack = { currentScreen = StudioScreen.LIBRARY },
                        onSearch = { q -> viewModel.searchLrclib(q) },
                        onImportTrack = { track ->
                            viewModel.importTrack(track)
                            currentScreen = StudioScreen.EDITOR
                        },
                        onImportRaw = { title, content ->
                            viewModel.importRawContent(title, content)
                            currentScreen = StudioScreen.EDITOR
                        },
                        onLoadSample = { sample ->
                            viewModel.selectProject(sample)
                            currentScreen = StudioScreen.EDITOR
                        }
                    )
                }

                StudioScreen.SETTINGS -> {
                    SettingsScreen(
                        settings = settings,
                        onBack = { currentScreen = StudioScreen.LIBRARY },
                        onSelectPreviewStyle = { viewModel.setPreviewStyle(it) },
                        onSelectExportFormat = { viewModel.setDefaultExportFormat(it) },
                        onSetFontScale = { viewModel.setFontScale(it) },
                        onToggleAutoScroll = { viewModel.setAutoScroll(it) }
                    )
                }
            }
        }
    }
}
