package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioPlaybackManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(180000L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private var simulatedStartTime = 0L
    private var simulatedBasePosition = 0L
    private var isSimulatedPlayback = true

    fun loadAudioStream(stream: com.example.data.remote.AudioStream) {
        stop()
        _durationMs.value = if (stream.durationMs > 0) stream.durationMs else 180000L

        try {
            isSimulatedPlayback = false
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                if (stream.requestHeaders.isNotEmpty()) {
                    setDataSource(context, Uri.parse(stream.url), stream.requestHeaders)
                } else {
                    setDataSource(context, Uri.parse(stream.url))
                }
                prepareAsync()
                setOnPreparedListener { mp ->
                    _durationMs.value = mp.duration.toLong().coerceAtLeast(1000L)
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPositionMs.value = 0L
                    stopProgressTracking()
                }
            }
        } catch (_: Exception) {
            isSimulatedPlayback = true
            mediaPlayer = null
        }
    }

    fun loadAudio(uriString: String?, fallbackDurationMs: Long = 180000L) {
        stop()
        _durationMs.value = fallbackDurationMs

        if (uriString.isNullOrBlank()) {
            isSimulatedPlayback = true
            mediaPlayer?.release()
            mediaPlayer = null
            return
        }

        try {
            isSimulatedPlayback = false
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, Uri.parse(uriString))
                prepareAsync()
                setOnPreparedListener { mp ->
                    _durationMs.value = mp.duration.toLong().coerceAtLeast(1000L)
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPositionMs.value = 0L
                    stopProgressTracking()
                }
            }
        } catch (_: Exception) {
            isSimulatedPlayback = true
            mediaPlayer = null
        }
    }

    fun play() {
        if (_isPlaying.value) return
        _isPlaying.value = true

        if (isSimulatedPlayback || mediaPlayer == null) {
            simulatedStartTime = System.currentTimeMillis()
            simulatedBasePosition = _currentPositionMs.value
        } else {
            try {
                applySpeedToPlayer()
                mediaPlayer?.start()
            } catch (_: Exception) {
                isSimulatedPlayback = true
                simulatedStartTime = System.currentTimeMillis()
                simulatedBasePosition = _currentPositionMs.value
            }
        }
        startProgressTracking()
    }

    fun pause() {
        if (!_isPlaying.value) return
        _isPlaying.value = false
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
        stopProgressTracking()
    }

    fun togglePlayPause() {
        if (_isPlaying.value) pause() else play()
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, _durationMs.value)
        _currentPositionMs.value = clamped
        if (isSimulatedPlayback || mediaPlayer == null) {
            simulatedStartTime = System.currentTimeMillis()
            simulatedBasePosition = clamped
        } else {
            try {
                mediaPlayer?.seekTo(clamped.toInt())
            } catch (_: Exception) {
                // Seek error fallback
            }
        }
    }

    fun stepOffset(deltaMs: Long) {
        seekTo(_currentPositionMs.value + deltaMs)
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        if (_isPlaying.value && !isSimulatedPlayback) {
            applySpeedToPlayer()
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        setSpeed(speed)
    }

    fun cycleSpeed() {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        val current = _playbackSpeed.value
        val nextIdx = (speeds.indexOfFirst { kotlin.math.abs(it - current) < 0.05f } + 1) % speeds.size
        setSpeed(speeds[nextIdx])
    }

    private fun applySpeedToPlayer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mediaPlayer != null) {
            try {
                val params = PlaybackParams().apply {
                    this.speed = _playbackSpeed.value.coerceIn(0.1f, 2.0f)
                    this.pitch = 1.0f
                }
                mediaPlayer?.playbackParams = params
            } catch (_: Exception) {}
        }
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                if (isSimulatedPlayback || mediaPlayer == null) {
                    val elapsed = ((System.currentTimeMillis() - simulatedStartTime) * _playbackSpeed.value).toLong()
                    val pos = simulatedBasePosition + elapsed
                    if (pos >= _durationMs.value) {
                        _currentPositionMs.value = _durationMs.value
                        _isPlaying.value = false
                        break
                    } else {
                        _currentPositionMs.value = pos
                    }
                } else {
                    try {
                        val pos = mediaPlayer?.currentPosition?.toLong() ?: 0L
                        _currentPositionMs.value = pos
                    } catch (_: Exception) {
                        break
                    }
                }
                delay(20) // ~50fps smooth tracking
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    fun stop() {
        pause()
        _currentPositionMs.value = 0L
    }

    fun release() {
        stopProgressTracking()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
