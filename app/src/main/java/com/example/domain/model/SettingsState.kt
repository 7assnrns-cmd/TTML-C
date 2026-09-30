package com.example.domain.model

enum class KaraokePreviewStyle(val displayName: String) {
    MASK_REVEAL("Mask Reveal"),
    OPACITY_WIPE("Opacity Wipe"),
    COLOR_CHANGE("Color Change")
}

enum class ExportFormat(val displayName: String, val extension: String) {
    TTML("Apple Music TTML", "ttml"),
    LRC("Standard LRC", "lrc"),
    ELRC("Enhanced Syllable LRC", "elrc")
}

data class SettingsState(
    val previewStyle: KaraokePreviewStyle = KaraokePreviewStyle.MASK_REVEAL,
    val defaultExportFormat: ExportFormat = ExportFormat.TTML,
    val timeOffsetMs: Long = 0L,
    val fontScale: Float = 1.0f,
    val autoScrollEnabled: Boolean = true,
    val showWaveformRulers: Boolean = true
)
