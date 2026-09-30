package com.example.domain.usecase

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class ExtractedAudioMetadata(
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val localAudioPath: String,
    val coverPath: String?
)

object AudioMetadataExtractor {

    fun extractAndCopy(context: Context, uri: Uri): ExtractedAudioMetadata {
        val retriever = MediaMetadataRetriever()
        var title: String? = null
        var artist: String? = null
        var album: String? = null
        var durationMs = 180000L
        var coverPath: String? = null

        val uniqueId = UUID.randomUUID().toString()
        val audioDir = File(context.filesDir, "audio").apply { if (!exists()) mkdirs() }
        val destinationFile = File(audioDir, "$uniqueId.audio")

        try {
            // Copy audio stream to internal storage
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }

            retriever.setDataSource(destinationFile.absolutePath)
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            
            val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            if (!durStr.isNullOrBlank()) {
                durationMs = durStr.toLongOrNull() ?: 180000L
            }

            // Extract embedded album art
            val pictureBytes = retriever.embeddedPicture
            if (pictureBytes != null && pictureBytes.isNotEmpty()) {
                val coversDir = File(context.filesDir, "covers").apply { if (!exists()) mkdirs() }
                val coverFile = File(coversDir, "$uniqueId.jpg")
                FileOutputStream(coverFile).use { out ->
                    out.write(pictureBytes)
                }
                coverPath = coverFile.absolutePath
            }
        } catch (_: Exception) {
            // Fallback
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }

        // Fallback title from file name if metadata is absent
        val finalTitle = if (!title.isNullOrBlank()) {
            title
        } else {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Imported Track"
            fileName.substringBeforeLast('.')
        }

        return ExtractedAudioMetadata(
            title = finalTitle,
            artist = artist ?: "Unknown Artist",
            album = album ?: "",
            durationMs = durationMs.coerceAtLeast(1000L),
            localAudioPath = destinationFile.absolutePath,
            coverPath = coverPath
        )
    }
}
