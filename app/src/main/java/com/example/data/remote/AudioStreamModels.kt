package com.example.data.remote

enum class AudioQuality(val bitrateKbps: Int) {
    LOW(64),
    MEDIUM(128),
    HIGH(256)
}

enum class StreamPurpose {
    PLAYBACK,
    PREVIEW,
    WAVEFORM_EXTRACTION,
    CACHE
}

enum class AuthState {
    ANONYMOUS,
    AUTHENTICATED
}

data class AudioStreamRequest(
    val mediaId: String,
    val quality: AudioQuality = AudioQuality.HIGH,
    val networkMetered: Boolean = false,
    val purpose: StreamPurpose = StreamPurpose.PLAYBACK,
    val authState: AuthState = AuthState.ANONYMOUS
)

data class AudioStream(
    val mediaId: String,
    val url: String,
    val requestHeaders: Map<String, String> = emptyMap(),
    val contentLength: Long = 0L,
    val durationMs: Long = 180000L,
    val format: String = "audio/mp4",
    val mimeType: String = "audio/mp4"
)
