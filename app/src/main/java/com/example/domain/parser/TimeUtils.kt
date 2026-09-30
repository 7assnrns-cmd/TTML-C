package com.example.domain.parser

import java.util.Locale

object TimeUtils {

    /**
     * Formats milliseconds strictly as MM:SS.mmm
     * Example: 15902 -> "00:15.902", 83456 -> "01:23.456", 213067 -> "03:33.067"
     */
    fun formatMMSSmmm(timeMs: Long): String {
        val totalMs = timeMs.coerceAtLeast(0L)
        val minutes = totalMs / 60000
        val seconds = (totalMs % 60000) / 1000
        val millis = totalMs % 1000
        return String.format(Locale.US, "%02d:%02d.%03d", minutes, seconds, millis)
    }

    /**
     * Formats milliseconds for compact tags as MM:SS.ss
     */
    fun formatMMSSss(timeMs: Long): String {
        val totalMs = timeMs.coerceAtLeast(0L)
        val minutes = totalMs / 60000
        val seconds = (totalMs % 60000) / 1000
        val centis = (totalMs % 1000) / 10
        return String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, centis)
    }

    /**
     * Parses MM:SS.mmm or SS.mmm to milliseconds
     */
    fun parseToMillis(timeStr: String?): Long {
        if (timeStr.isNullOrBlank()) return 0L
        val clean = timeStr.trim().removeSuffix("s")
        return try {
            if (clean.contains(":")) {
                val parts = clean.split(":")
                if (parts.size == 2) {
                    val minutes = parts[0].toLong()
                    val seconds = parts[1].toDouble()
                    (minutes * 60000 + seconds * 1000).toLong()
                } else if (parts.size == 3) {
                    val hours = parts[0].toLong()
                    val minutes = parts[1].toLong()
                    val seconds = parts[2].toDouble()
                    (hours * 3600000 + minutes * 60000 + seconds * 1000).toLong()
                } else {
                    0L
                }
            } else {
                (clean.toDouble() * 1000).toLong()
            }
        } catch (_: Exception) {
            0L
        }
    }
}
