package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.remote.AudioStream
import com.example.data.remote.LrclibTrack
import com.example.data.remote.YtMusicTrack
import com.example.data.repository.AudioStreamRepository
import com.example.data.repository.LyricsRepository
import com.example.domain.model.AgentInfo
import com.example.domain.model.ExportFormat
import com.example.domain.model.KaraokeAlignment
import com.example.domain.model.KaraokePreviewStyle
import com.example.domain.model.LyricLine
import com.example.domain.model.LyricToken
import com.example.domain.model.LyricsProject
import com.example.domain.model.SettingsState
import com.example.domain.model.SongPart
import com.example.domain.parser.AutoParser
import com.example.domain.parser.LrcExporter
import com.example.domain.parser.TTMLExporter
import com.example.domain.usecase.AudioMetadataExtractor
import com.example.player.AudioPlaybackManager
import com.example.player.WaveformGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = LyricsRepository(database.projectDao())
    val audioStreamRepository = AudioStreamRepository()

    val audioManager = AudioPlaybackManager(application)

    val projects: StateFlow<List<LyricsProject>> = repository.projects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentProject = MutableStateFlow<LyricsProject?>(null)
    val currentProject: StateFlow<LyricsProject?> = _currentProject.asStateFlow()

    private val _pendingCandidate = MutableStateFlow<com.example.ui.screens.PendingSongCandidate?>(null)
    val pendingCandidate: StateFlow<com.example.ui.screens.PendingSongCandidate?> = _pendingCandidate.asStateFlow()

    private val _activeLineIndex = MutableStateFlow(0)
    val activeLineIndex: StateFlow<Int> = _activeLineIndex.asStateFlow()

    private val _activeTokenIndex = MutableStateFlow(0)
    val activeTokenIndex: StateFlow<Int> = _activeTokenIndex.asStateFlow()

    private val _waveformPeaks = MutableStateFlow<List<Float>>(emptyList())
    val waveformPeaks: StateFlow<List<Float>> = _waveformPeaks.asStateFlow()

    private val _zoomLevel = MutableStateFlow(1.0f)
    val zoomLevel: StateFlow<Float> = _zoomLevel.asStateFlow()

    private val _settings = MutableStateFlow(SettingsState())
    val settings: StateFlow<SettingsState> = _settings.asStateFlow()

    private val _searchResults = MutableStateFlow<List<LrclibTrack>>(emptyList())
    val searchResults: StateFlow<List<LrclibTrack>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    // Undo / Redo history
    private val undoStack = mutableListOf<List<LyricLine>>()
    private val redoStack = mutableListOf<List<LyricLine>>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedSamplesIfNeeded()
        }
    }

    fun selectProject(project: LyricsProject) {
        _currentProject.value = project
        _activeLineIndex.value = 0
        _activeTokenIndex.value = 0
        undoStack.clear()
        redoStack.clear()
        updateUndoRedoState()

        _waveformPeaks.value = WaveformGenerator.generateWaveform(
            sampleCount = 350,
            seed = project.title + project.artist
        )

        audioManager.loadAudio(project.audioUri, project.durationMs)
    }

    fun closeProject() {
        audioManager.stop()
        _currentProject.value = null
    }

    fun selectLine(index: Int) {
        val proj = _currentProject.value ?: return
        if (index in proj.lines.indices) {
            _activeLineIndex.value = index
            _activeTokenIndex.value = 0
            val line = proj.lines[index]
            if (line.beginMs > 0L) {
                audioManager.seekTo(line.beginMs)
            }
        }
    }

    fun selectToken(tokenIndex: Int) {
        val proj = _currentProject.value ?: return
        val line = proj.lines.getOrNull(_activeLineIndex.value) ?: return
        if (tokenIndex in line.tokens.indices) {
            _activeTokenIndex.value = tokenIndex
            val tok = line.tokens[tokenIndex]
            if (tok.beginMs > 0L) {
                audioManager.seekTo(tok.beginMs)
            }
        }
    }

    private fun pushUndoState(currentLines: List<LyricLine>) {
        undoStack.add(currentLines)
        if (undoStack.size > 50) undoStack.removeAt(0)
        redoStack.clear()
        updateUndoRedoState()
    }

    private fun updateUndoRedoState() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    fun undo() {
        val proj = _currentProject.value ?: return
        if (undoStack.isNotEmpty()) {
            redoStack.add(proj.lines)
            val prevLines = undoStack.removeAt(undoStack.lastIndex)
            _currentProject.value = proj.copy(lines = prevLines)
            updateUndoRedoState()
        }
    }

    fun redo() {
        val proj = _currentProject.value ?: return
        if (redoStack.isNotEmpty()) {
            undoStack.add(proj.lines)
            val nextLines = redoStack.removeAt(redoStack.lastIndex)
            _currentProject.value = proj.copy(lines = nextLines)
            updateUndoRedoState()
        }
    }

    // --- TIMING ACTIONS ---

    fun markLineStart() {
        val proj = _currentProject.value ?: return
        val lIdx = _activeLineIndex.value
        val line = proj.lines.getOrNull(lIdx) ?: return
        val pos = audioManager.currentPositionMs.value

        pushUndoState(proj.lines)
        val updatedLines = proj.lines.toMutableList()

        // Rule 3: If previous line exists, automatically close it at current position
        if (lIdx > 0) {
            val prevLine = updatedLines[lIdx - 1]
            val updatedPrevTokens = prevLine.tokens.mapIndexed { idx, tok ->
                if (idx == prevLine.tokens.lastIndex) tok.copy(endMs = pos) else tok
            }
            updatedLines[lIdx - 1] = prevLine.copy(
                endMs = pos,
                tokens = updatedPrevTokens
            )
        }

        // Rule 1: Set line start and first token start
        val updatedTokens = line.tokens.mapIndexed { idx, tok ->
            if (idx == 0) tok.copy(beginMs = pos) else tok
        }
        val newEnd = if (line.endMs > pos) line.endMs else pos + 3000L
        updatedLines[lIdx] = line.copy(
            beginMs = pos,
            endMs = newEnd,
            tokens = updatedTokens
        )
        _currentProject.value = proj.copy(lines = updatedLines)
        _activeTokenIndex.value = 0
    }

    fun markLineEnd() {
        val proj = _currentProject.value ?: return
        val lIdx = _activeLineIndex.value
        val line = proj.lines.getOrNull(lIdx) ?: return
        val pos = audioManager.currentPositionMs.value

        pushUndoState(proj.lines)
        val updatedLines = proj.lines.toMutableList()
        val newEnd = pos.coerceAtLeast(line.beginMs + 100L)
        val updatedTokens = line.tokens.mapIndexed { idx, tok ->
            if (idx == line.tokens.lastIndex) tok.copy(endMs = newEnd) else tok
        }
        updatedLines[lIdx] = line.copy(endMs = newEnd, tokens = updatedTokens)
        _currentProject.value = proj.copy(lines = updatedLines)

        // Advance to next line if available
        if (lIdx + 1 < updatedLines.size) {
            _activeLineIndex.value = lIdx + 1
            _activeTokenIndex.value = 0
        }
    }

    fun markWordStart() {
        val proj = _currentProject.value ?: return
        val lIdx = _activeLineIndex.value
        val line = proj.lines.getOrNull(lIdx) ?: return
        val tIdx = _activeTokenIndex.value
        val token = line.tokens.getOrNull(tIdx) ?: return
        val pos = audioManager.currentPositionMs.value

        pushUndoState(proj.lines)
        val updatedTokens = line.tokens.toMutableList()
        val newEnd = if (token.endMs > pos) token.endMs else pos + 400L
        updatedTokens[tIdx] = token.copy(beginMs = pos, endMs = newEnd)

        val updatedLines = proj.lines.toMutableList()
        updatedLines[lIdx] = line.copy(
            beginMs = if (line.beginMs == 0L || pos < line.beginMs) pos else line.beginMs,
            tokens = updatedTokens
        )
        _currentProject.value = proj.copy(lines = updatedLines)
    }

    fun markWordEnd() {
        val proj = _currentProject.value ?: return
        val lIdx = _activeLineIndex.value
        val line = proj.lines.getOrNull(lIdx) ?: return
        val tIdx = _activeTokenIndex.value
        val token = line.tokens.getOrNull(tIdx) ?: return
        val pos = audioManager.currentPositionMs.value

        pushUndoState(proj.lines)
        val updatedTokens = line.tokens.toMutableList()
        val tokenStart = if (token.beginMs > 0) token.beginMs else line.beginMs
        val tokenEnd = pos.coerceAtLeast(tokenStart + 50L)
        updatedTokens[tIdx] = token.copy(beginMs = tokenStart, endMs = tokenEnd)

        // Rule 2: Next token automatically begins at this end time!
        if (tIdx + 1 < updatedTokens.size) {
            val nextTok = updatedTokens[tIdx + 1]
            updatedTokens[tIdx + 1] = nextTok.copy(beginMs = tokenEnd)
        }

        val updatedLines = proj.lines.toMutableList()
        val isLastWordInLine = (tIdx == updatedTokens.lastIndex)
        val lineEnd = if (isLastWordInLine) tokenEnd else if (tokenEnd > line.endMs) tokenEnd + 1000L else line.endMs

        updatedLines[lIdx] = line.copy(
            beginMs = if (line.beginMs == 0L || tokenStart < line.beginMs) tokenStart else line.beginMs,
            endMs = lineEnd,
            tokens = updatedTokens
        )
        _currentProject.value = proj.copy(lines = updatedLines)

        // Auto-advance
        if (tIdx + 1 < updatedTokens.size) {
            _activeTokenIndex.value = tIdx + 1
        } else if (lIdx + 1 < updatedLines.size) {
            _activeLineIndex.value = lIdx + 1
            _activeTokenIndex.value = 0
        }
    }

    fun applyGlobalSyncOffset(deltaMs: Long) {
        val proj = _currentProject.value ?: return
        pushUndoState(proj.lines)
        val shiftedLines = proj.lines.map { line ->
            val shiftedTokens = line.tokens.map { tok ->
                tok.copy(
                    beginMs = (tok.beginMs + deltaMs).coerceAtLeast(0L),
                    endMs = (tok.endMs + deltaMs).coerceAtLeast(0L)
                )
            }
            line.copy(
                beginMs = (line.beginMs + deltaMs).coerceAtLeast(0L),
                endMs = (line.endMs + deltaMs).coerceAtLeast(0L),
                tokens = shiftedTokens
            )
        }
        _currentProject.value = proj.copy(lines = shiftedLines)
    }

    fun splitActiveWord() {
        val proj = _currentProject.value ?: return
        val lIdx = _activeLineIndex.value
        val line = proj.lines.getOrNull(lIdx) ?: return
        val tIdx = _activeTokenIndex.value
        val token = line.tokens.getOrNull(tIdx) ?: return
        if (token.text.length <= 1) return

        pushUndoState(proj.lines)
        val text = token.text
        val mid = text.length / 2
        val part1 = text.substring(0, mid)
        val part2 = text.substring(mid)

        val halfDuration = token.durationMs / 2
        val tok1 = token.copy(
            text = part1,
            endMs = token.beginMs + halfDuration
        )
        val tok2 = LyricToken(
            text = part2,
            beginMs = token.beginMs + halfDuration,
            endMs = token.endMs,
            isBackground = token.isBackground,
            syllableIndex = token.syllableIndex + 1
        )

        val updatedTokens = line.tokens.toMutableList()
        updatedTokens.removeAt(tIdx)
        updatedTokens.add(tIdx, tok1)
        updatedTokens.add(tIdx + 1, tok2)

        val updatedLines = proj.lines.toMutableList()
        updatedLines[lIdx] = line.copy(tokens = updatedTokens)
        _currentProject.value = proj.copy(lines = updatedLines)
    }

    fun autoSyncLine() {
        val proj = _currentProject.value ?: return
        val lIdx = _activeLineIndex.value
        val line = proj.lines.getOrNull(lIdx) ?: return
        if (line.tokens.isEmpty()) return

        pushUndoState(proj.lines)
        val lineBegin = if (line.beginMs > 0L) line.beginMs else audioManager.currentPositionMs.value
        val lineEnd = if (line.endMs > lineBegin) line.endMs else lineBegin + (line.tokens.size * 500L)
        val totalSpan = (lineEnd - lineBegin).coerceAtLeast(100L * line.tokens.size)

        // Syllable weight by text character length
        val totalChars = line.tokens.sumOf { it.text.length }.coerceAtLeast(1)
        var cursor = lineBegin

        val newTokens = line.tokens.mapIndexed { idx, tok ->
            val weight = tok.text.length.toFloat() / totalChars
            val duration = (totalSpan * weight).toLong().coerceAtLeast(120L)
            val tokStart = cursor
            val tokEnd = if (idx == line.tokens.lastIndex) lineEnd else tokStart + duration
            cursor = tokEnd
            tok.copy(beginMs = tokStart, endMs = tokEnd)
        }

        val updatedLines = proj.lines.toMutableList()
        updatedLines[lIdx] = line.copy(
            beginMs = lineBegin,
            endMs = lineEnd,
            tokens = newTokens
        )
        _currentProject.value = proj.copy(lines = updatedLines)
    }

    fun toggleBackgroundVocal(isBackground: Boolean) {
        val proj = _currentProject.value ?: return
        val lIdx = _activeLineIndex.value
        val line = proj.lines.getOrNull(lIdx) ?: return
        val tIdx = _activeTokenIndex.value
        val token = line.tokens.getOrNull(tIdx) ?: return

        pushUndoState(proj.lines)
        val updatedTokens = line.tokens.toMutableList()
        updatedTokens[tIdx] = token.copy(isBackground = isBackground)

        val updatedLines = proj.lines.toMutableList()
        updatedLines[lIdx] = line.copy(tokens = updatedTokens)
        _currentProject.value = proj.copy(lines = updatedLines)
    }

    fun setLineAgent(agentId: String) {
        val proj = _currentProject.value ?: return
        val lIdx = _activeLineIndex.value
        val line = proj.lines.getOrNull(lIdx) ?: return

        pushUndoState(proj.lines)
        val updatedLines = proj.lines.toMutableList()
        updatedLines[lIdx] = line.copy(agentId = agentId)
        _currentProject.value = proj.copy(lines = updatedLines)
    }

    fun setLineSongPart(songPart: SongPart) {
        val proj = _currentProject.value ?: return
        val lIdx = _activeLineIndex.value
        val line = proj.lines.getOrNull(lIdx) ?: return

        pushUndoState(proj.lines)
        val updatedLines = proj.lines.toMutableList()
        updatedLines[lIdx] = line.copy(songPart = songPart)
        _currentProject.value = proj.copy(lines = updatedLines)
    }

    fun addNewAgent(name: String, type: String = "person", colorHex: String = "#00D2FF") {
        val proj = _currentProject.value ?: return
        val newId = "v${proj.agents.size + 1}"
        val alignment = when {
            proj.agents.isEmpty() -> KaraokeAlignment.START
            type == "group" -> KaraokeAlignment.CENTER
            else -> KaraokeAlignment.END
        }
        val newAgent = AgentInfo(
            id = newId,
            name = name.ifBlank { "Singer ${proj.agents.size + 1}" },
            type = type,
            alignment = alignment,
            colorHex = colorHex
        )
        val updatedAgents = proj.agents + newAgent
        _currentProject.value = proj.copy(agents = updatedAgents)
    }

    fun addLine(text: String) {
        val proj = _currentProject.value ?: return
        pushUndoState(proj.lines)
        val words = text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        val lastLine = proj.lines.lastOrNull()
        val start = lastLine?.endMs ?: 0L
        val newLine = LyricLine(
            beginMs = start,
            endMs = start + 3000L,
            agentId = "v1",
            songPart = SongPart.VERSE,
            tokens = words.mapIndexed { idx, w ->
                LyricToken(
                    text = w,
                    beginMs = start + idx * 400L,
                    endMs = start + (idx + 1) * 400L,
                    isBackground = w.startsWith("(") && w.endsWith(")"),
                    syllableIndex = idx
                )
            }
        )
        val updatedLines = proj.lines + newLine
        _currentProject.value = proj.copy(lines = updatedLines)
        _activeLineIndex.value = updatedLines.lastIndex
    }

    fun deleteLine(index: Int) {
        val proj = _currentProject.value ?: return
        if (index in proj.lines.indices) {
            pushUndoState(proj.lines)
            val updatedLines = proj.lines.toMutableList()
            updatedLines.removeAt(index)
            _currentProject.value = proj.copy(lines = updatedLines)
            _activeLineIndex.value = (_activeLineIndex.value - 1).coerceAtLeast(0)
        }
    }

    fun updateLineTranslation(lineIndex: Int, translation: String) {
        val proj = _currentProject.value ?: return
        val line = proj.lines.getOrNull(lineIndex) ?: return
        val updatedLines = proj.lines.toMutableList()
        updatedLines[lineIndex] = line.copy(translation = translation.ifBlank { null })
        _currentProject.value = proj.copy(lines = updatedLines)
    }

    fun saveCurrentProject() {
        val proj = _currentProject.value ?: return
        viewModelScope.launch {
            repository.saveProject(proj)
        }
    }

    fun createNewProject(title: String, artist: String, language: String = "en", durationSec: Long = 180L) {
        val project = LyricsProject(
            title = title.ifBlank { "New Song" },
            artist = artist,
            language = language,
            durationMs = durationSec * 1000L
        )
        viewModelScope.launch {
            repository.saveProject(project)
            selectProject(project)
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            repository.deleteProject(id)
            if (_currentProject.value?.id == id) {
                closeProject()
            }
        }
    }

    // --- SEARCH & IMPORT ---

    fun searchLrclib(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = null
            val result = repository.searchLrclib(query)
            result.onSuccess { tracks ->
                _searchResults.value = tracks
                _isSearching.value = false
            }.onFailure { err ->
                _searchError.value = err.message ?: "Failed to fetch from LRCLIB"
                _isSearching.value = false
            }
        }
    }

    // --- AUDIO STREAM & WAVEFORM RESOLUTION ---

    suspend fun prepareAudioStream(mediaId: String): com.example.data.remote.AudioStream {
        val request = com.example.data.remote.AudioStreamRequest(
            mediaId = mediaId,
            quality = com.example.data.remote.AudioQuality.HIGH,
            networkMetered = false,
            purpose = com.example.data.remote.StreamPurpose.PLAYBACK,
            authState = com.example.data.remote.AuthState.ANONYMOUS
        )
        val stream = audioStreamRepository.resolve(request)

        // Load stream into AudioPlaybackManager with headers
        audioManager.loadAudioStream(stream)

        // Extract waveform from stream URL / contentLength
        val peaks = com.example.player.WaveformExtractor.extractFromStream(
            streamUrl = stream.url,
            requestHeaders = stream.requestHeaders,
            seed = mediaId,
            sampleCount = 360
        )
        _waveformPeaks.value = peaks

        return stream
    }

    fun importFromDeviceAudio(context: Context, uri: Uri) {
        viewModelScope.launch {
            val metadata = AudioMetadataExtractor.extractAndCopy(context, uri)

            // Auto-fetch lyrics via Multi-Source Fallback Chain (LRCLIB -> NetEase -> Musixmatch)
            val fetchedLyrics = com.example.data.remote.MultiSourceLyricsFetcher.fetchLyricsAuto(
                title = metadata.title,
                artist = metadata.artist,
                durationMs = metadata.durationMs
            )

            val parsed = if (fetchedLyrics != null && fetchedLyrics.rawLyrics.isNotBlank()) {
                AutoParser.parse(fetchedLyrics.rawLyrics, metadata.title)
            } else {
                AutoParser.parse("", metadata.title)
            }

            // Extract audio waveform for local file
            val peaks = com.example.player.WaveformExtractor.extractFromStream(
                streamUrl = metadata.localAudioPath,
                seed = metadata.title + metadata.artist,
                sampleCount = 360
            )
            _waveformPeaks.value = peaks
            audioManager.loadAudio(metadata.localAudioPath, metadata.durationMs)

            _pendingCandidate.value = com.example.ui.screens.PendingSongCandidate(
                title = metadata.title,
                artist = metadata.artist,
                album = metadata.album,
                coverUrl = metadata.coverPath ?: "",
                durationMs = metadata.durationMs,
                audioUri = metadata.localAudioPath,
                fetchedLyrics = fetchedLyrics,
                parsedLines = parsed.lines
            )
        }
    }

    fun importFromYtMusicTrack(track: YtMusicTrack) {
        viewModelScope.launch {
            // Resolve Audio Stream via AudioStreamRepository / InnerTube
            val stream = prepareAudioStream(track.videoId.ifBlank { "yt_" + track.title.hashCode() })

            // Auto-fetch matching lyrics from multi-source search
            val fetchedLyrics = com.example.data.remote.MultiSourceLyricsFetcher.fetchLyricsAuto(
                title = track.title,
                artist = track.artist,
                durationMs = track.durationMs
            )

            val parsed = if (fetchedLyrics != null && fetchedLyrics.rawLyrics.isNotBlank()) {
                AutoParser.parse(fetchedLyrics.rawLyrics, track.title)
            } else {
                AutoParser.parse("", track.title)
            }

            _pendingCandidate.value = com.example.ui.screens.PendingSongCandidate(
                title = track.title,
                artist = track.artist,
                album = track.album,
                coverUrl = track.thumbnailUrl,
                durationMs = if (stream.durationMs > 0) stream.durationMs else track.durationMs,
                audioUri = stream.url,
                fetchedLyrics = fetchedLyrics,
                parsedLines = parsed.lines
            )
        }
    }

    fun confirmPendingCandidate(candidate: com.example.ui.screens.PendingSongCandidate) {
        val project = LyricsProject(
            title = candidate.title.ifBlank { "New Song" },
            artist = candidate.artist,
            album = candidate.album,
            durationMs = candidate.durationMs,
            audioUri = candidate.audioUri,
            coverUri = candidate.coverUrl.ifBlank { null },
            lines = if (candidate.parsedLines.isNotEmpty()) candidate.parsedLines else listOf(
                LyricLine(
                    beginMs = 0L,
                    endMs = 3000L,
                    agentId = "v1",
                    songPart = SongPart.VERSE,
                    tokens = listOf(LyricToken(text = candidate.title, beginMs = 0L, endMs = 3000L))
                )
            )
        )
        viewModelScope.launch {
            repository.saveProject(project)
            _pendingCandidate.value = null
            selectProject(project)
        }
    }

    fun rejectPendingCandidate() {
        val candidate = _pendingCandidate.value
        _pendingCandidate.value = null
        if (candidate != null) {
            // Open empty project or custom lyrics for user to edit manually
            val project = LyricsProject(
                title = candidate.title,
                artist = candidate.artist,
                album = candidate.album,
                durationMs = candidate.durationMs,
                audioUri = candidate.audioUri,
                coverUri = candidate.coverUrl.ifBlank { null },
                lines = listOf(
                    LyricLine(
                        beginMs = 0L,
                        endMs = 3000L,
                        agentId = "v1",
                        songPart = SongPart.VERSE,
                        tokens = listOf(LyricToken(text = "Add lyrics here...", beginMs = 0L, endMs = 3000L))
                    )
                )
            )
            viewModelScope.launch {
                repository.saveProject(project)
                selectProject(project)
            }
        }
    }

    fun tryAnotherSourceForPending() {
        val candidate = _pendingCandidate.value ?: return
        viewModelScope.launch {
            _isSearching.value = true
            // Rotate to next source in MultiSourceLyricsFetcher (e.g. NetEase / Musixmatch / Genius)
            val fallback = com.example.data.remote.MultiSourceLyricsFetcher.fetchLyricsAuto(
                title = candidate.title,
                artist = candidate.artist,
                durationMs = candidate.durationMs
            )
            if (fallback != null && fallback.rawLyrics.isNotBlank()) {
                val parsed = AutoParser.parse(fallback.rawLyrics, candidate.title)
                _pendingCandidate.value = candidate.copy(
                    fetchedLyrics = fallback,
                    parsedLines = parsed.lines
                )
            }
            _isSearching.value = false
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.1f, 2.0f)
        audioManager.setSpeed(clamped)
    }

    fun importFromDirectLink(url: String, title: String, artist: String) {
        val project = LyricsProject(
            title = title.ifBlank { "Stream Audio" },
            artist = artist,
            audioUri = url
        )
        viewModelScope.launch {
            repository.saveProject(project)
            selectProject(project)
        }
    }

    fun importTrack(track: LrclibTrack) {
        val project = repository.convertLrclibToProject(track)
        viewModelScope.launch {
            repository.saveProject(project)
            selectProject(project)
        }
    }

    fun importRawContent(title: String, content: String) {
        val project = AutoParser.parse(content, title)
        viewModelScope.launch {
            repository.saveProject(project)
            selectProject(project)
        }
    }

    fun attachAudioUri(uriString: String) {
        val proj = _currentProject.value ?: return
        val updated = proj.copy(audioUri = uriString)
        _currentProject.value = updated
        audioManager.loadAudio(uriString, proj.durationMs)
        viewModelScope.launch {
            repository.saveProject(updated)
        }
    }

    // --- ZOOM & SPEED ---

    fun zoomIn() {
        _zoomLevel.value = (_zoomLevel.value + 0.5f).coerceAtMost(5.0f)
    }

    fun zoomOut() {
        _zoomLevel.value = (_zoomLevel.value - 0.5f).coerceAtLeast(1.0f)
    }

    fun cycleSpeed() {
        val currentSpeed = audioManager.playbackSpeed.value
        val nextSpeed = when {
            currentSpeed < 0.74f -> 0.75f
            currentSpeed < 0.99f -> 1.0f
            currentSpeed < 1.24f -> 1.25f
            currentSpeed < 1.49f -> 1.5f
            else -> 0.5f
        }
        audioManager.setSpeed(nextSpeed)
    }

    // --- SETTINGS ---

    fun setPreviewStyle(style: KaraokePreviewStyle) {
        _settings.value = _settings.value.copy(previewStyle = style)
    }

    fun setDefaultExportFormat(format: ExportFormat) {
        _settings.value = _settings.value.copy(defaultExportFormat = format)
    }

    fun setFontScale(scale: Float) {
        _settings.value = _settings.value.copy(fontScale = scale)
    }

    fun setAutoScroll(enabled: Boolean) {
        _settings.value = _settings.value.copy(autoScrollEnabled = enabled)
    }

    // --- EXPORT ---

    fun exportCurrentProject(format: ExportFormat = _settings.value.defaultExportFormat): String {
        val proj = _currentProject.value ?: return ""
        return when (format) {
            ExportFormat.TTML -> TTMLExporter.export(proj)
            ExportFormat.LRC -> LrcExporter.export(proj)
            ExportFormat.ELRC -> LrcExporter.exportEnhanced(proj)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioManager.release()
    }
}
